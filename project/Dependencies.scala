import sbt._
import Keys._

object Dependencies {

  object Versions {
    val catsV      = "2.1.1"
    val circeV     = "0.13.0"
    val circeFs2V  = "0.13.0"
    val lambdaV    = "1.2.0"
    val fs2V       = "2.2.2"
    val slf4jV     = "1.7.25"
    val scalatestV = "3.0.8"
    val awsV       = "2.7.0"
    val http4sV    = "0.21.1"
    val log4catsV  = "1.0.1"
  }

  import Versions._
  val catsEffect = "org.typelevel"          %% "cats-effect"         % catsV
  val circe      = "io.circe"               %% "circe-parser"        % circeV
  val circeAuto  = "io.circe"               %% "circe-generic"       % circeV
  val circeFs2   = "io.circe"               %% "circe-fs2"           % circeFs2V
  val fs2        = "co.fs2"                 %% "fs2-core"            % fs2V
  val fs2io      = "co.fs2"                 %% "fs2-io"              % fs2V
  val lambda     = "com.amazonaws"          % "aws-lambda-java-core" % lambdaV
  val slf4j      = "org.slf4j"              % "slf4j-api"            % slf4jV
  val sqs        = "software.amazon.awssdk" % "sqs"                  % awsV
  val sns        = "software.amazon.awssdk" % "sns"                  % awsV
  val ses        = "software.amazon.awssdk" % "ses"                  % awsV
  val log4cats   = "io.chrisdavenport"      %% "log4cats-slf4j"      % log4catsV

  val http4sCore  = "org.http4s" %% "http4s-core"  % http4sV
  val http4sDsl   = "org.http4s" %% "http4s-dsl"   % http4sV
  val http4sCirce = "org.http4s" %% "http4s-circe" % http4sV

  val scalatest           = "org.scalatest"              %% "scalatest"                 % scalatestV
  val scalamock           = "org.scalamock"              %% "scalamock"                 % "4.3.0"
  val discipline          = "org.typelevel"              %% "discipline-scalatest"      % "1.0.1"
  val catsTestkit         = "org.typelevel"              %% "cats-laws"                 % catsV
  val scalacheckShapeless = "com.github.alexarchambault" %% "scalacheck-shapeless_1.14" % "1.2.3"
}
