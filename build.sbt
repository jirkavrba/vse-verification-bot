name := "vse-verification-bot"

version := "0.1"

scalaVersion := "2.13.4"

resolvers += JCenterRepository

libraryDependencies += "net.dv8tion" % "JDA" % "4.2.0_227"
libraryDependencies += "com.softwaremill.sttp.client3" %% "core" % "3.8.12"

val circeVersion = "0.12.3"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)