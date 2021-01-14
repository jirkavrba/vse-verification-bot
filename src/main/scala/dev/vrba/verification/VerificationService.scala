package dev.vrba.verification

import io.circe.parser._
import sttp.client3._
import sttp.model.StatusCode

sealed trait VerificationResult

case object Success extends VerificationResult

case class Failure(reason: String) extends VerificationResult

class VerificationService(private val portalToken: String) {
  private val portalUrl = "https://portal.fis-vse.cz"

  private val backend = HttpURLConnectionBackend()

  def verify(code: String, userId: Long): VerificationResult = {
    val request = basicRequest
      .header("Accept", "application/json")
      .body(Map("api_token" -> portalToken))
      .post(uri"$portalUrl/api/discord/complete_verification/$code/$userId")

    request.send(backend) match {
      case Response(body, code, _, _, _, _) =>
        code match {
          // Invalid token
          case StatusCode.UnprocessableEntity => handleUnprocessableEntity(body, userId)
          // Invalid verification code
          case StatusCode.NotFound => Failure("Verification code cannot be found in the server database.")
        }

      case _ => Failure("Cannot fetch portal API. Something might have gone wrong at server side.")
    }
  }

  private def handleUnprocessableEntity(body: Either[String, String], userId: Long): VerificationResult = {
    body.swap.map {
      response =>
        parse(response).map {
          json =>
            json.hcursor.downField("status").as[String].map {
              case "verification_code_already_used" => repeatedlyVerifyUser(userId)
              case "user_banned" => Failure("User banned. Verification cannot be completed.")
              case _ => Failure("Unknown response status code. This is most likely a server issue.")
            }
        }
    } match {
      case Right(Right(Right(result))) => result.asInstanceOf[VerificationResult]
      case _ => Failure("Received malformed JSON response. This is most likely a server issue.")
    }
  }

  private def repeatedlyVerifyUser(userId: Long): VerificationResult = {
    val request = basicRequest
      .header("Accept", "application/json")
      .get(uri"$portalUrl/api/discord/check_verification/$userId?api_token=$portalToken")

    request.send(backend) match {
      case Response(_, code, _, _, _, _) => code match {
        case StatusCode.Ok => Success
        case StatusCode.NotFound => Failure("Cannot find a valid verification for specified Discord ID.")
      }
      case _ => Failure("Cannot fetch portal API. Something might have gone wrong at server side.")
    }
  }
}
