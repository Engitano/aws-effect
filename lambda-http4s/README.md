# Http4s DSL for AWS Lambda


### Example Usage

build.sbt (with sbt-assembly):

```scala
import sbtassembly.Log4j2MergeStrategy

name := "hello-lambda"

resolvers ++= Seq(
  Resolver.sonatypeRepo("public"),
  Resolver.bintrayRepo("engitano", "maven")
)

assemblyJarName in assembly := "lambda.jar"

libraryDependencies ++= Seq(
  "com.engitano"          %% "aws-effect-lambda-http4s" % "0.1.23",
  "com.amazonaws"         %  "aws-lambda-java-log4j2"   % "1.1.0",
  "org.http4s"            %% "http4s-core"              % "0.21.0-M2",
  "org.http4s"            %% "http4s-dsl"               % "0.21.0-M2",
  "org.http4s"            %% "http4s-circe"             % "0.21.0-M2",
  "com.github.pureconfig" %% "pureconfig"               % "0.11.1"
)

assemblyMergeStrategy in assembly := {
  case PathList(ps @ _*) if ps.last == "Log4j2Plugins.dat" =>
    Log4j2MergeStrategy.plugincache
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

```

Application.scala: 
```scala
package lambda

import cats.effect.IO
import cats.instances.either._
import cats.syntax.bifunctor._
import com.engitano.awseffect.lambda.http4s.LambdaHost
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.circe._
import pureconfig.generic.auto._

import scala.concurrent.ExecutionContext

object Application extends Http4sDsl[IO] {
  implicit val ec = ExecutionContext.global
  implicit val cs = IO.contextShift(ec)

  case class ApplicationConfig(version: String)
  case class Environment(appConfig: ApplicationConfig)

  case class HealthCheckResponse(version: String, status: Option[String])

  val environment = IO.fromEither(pureconfig.loadConfig[Environment]
    .leftMap(e => new Exception(e.tail.foldLeft(e.head.description)(_ + _.description))))

  def routes(env: Environment) = HttpRoutes.of[IO] {
    case GET -> Root / "healthcheck" => Ok(HealthCheckResponse(env.appConfig.version, None).asJson)
  }

  class Handler extends LambdaHost[IO](environment.map(routes))
}

```

### Serverless Config

```yml
service: my-service
provider:
  name: aws
  runtime: java8
package:
  artifact: target/scala-2.13/lambda.jar
functions:
  hello:
    handler: lambda.Application$Handler
    events:
      - http: ANY /
      - http: 'ANY {proxy+}'
```