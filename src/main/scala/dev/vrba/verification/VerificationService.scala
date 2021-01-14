package dev.vrba.verification

import sttp.client3._
import sttp.model.StatusCode

sealed trait VerificationResult
case object Success extends VerificationResult
case class Failure(val reason: String) extends VerificationResult

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
          case StatusCode.UnprocessableEntity => Failure("Invalid portal API token supplied. Verification cannot be done.")
          // Invalid verification code
          case StatusCode.NotFound => Failure("Verification")
        }

      case _ => Failure("Cannot fetch portal API. Something might have gone wrong at server side.")
    }
  }

}
