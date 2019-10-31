package com.engitano.awseffect.lambda.http4s


import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import cats.effect.IO
import cats.syntax.option._
import com.amazonaws.services.lambda.runtime.Context
import com.engitano.awseffect.lambda.http4s.Messages.{Input, Output}
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}
import io.circe.generic.auto._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe.CirceEntityEncoder._

import scala.concurrent.ExecutionContext
import com.engitano.awseffect.lambda.catsio.IOLambda
import com.engitano.awseffect.lambda.apigw.ProxyResponse
import cats.effect.Blocker
import cats.effect.ContextShift

class Http4sHandlerSpec extends WordSpec with Matchers with MockFactory {


  import com.engitano.awseffect.lambda.apigw.ProxyMarshallers._

  "The Http4sHandler" should {
    "return a valid JSON response" in {
        import Dsl._
        import org.http4s.dsl.io._

        val svc = IO.pure(LambdaRoutes.of[IO] {
            case req @ PUT -> Root / "test" / "hello" λ _ => 
                req.req.as[Input].flatMap { i =>
                    Ok(Output(i.name + "!"))
                }
        })

      val sut = new IOLambda  {
        def handler(blocker: Blocker)(implicit ec: ExecutionContext, cs: ContextShift[IO]) = {
            Http4sHandler[IO](blocker)(svc)
        }
      }

      val inputStream =
        new ByteArrayInputStream(Messages.requestWithInputName.getBytes)
      val output = new ByteArrayOutputStream()
      sut.handleRequest(inputStream, output, mock[Context])
      val respJson = output.toString
      val response = decode[ProxyResponse](respJson)
      response should matchPattern {
        case Right(_) =>
      }
      val respBody = decode[Output](response.right.get.body.getOrElse(""))
      respBody.right.get.greeting shouldEqual "Hello Functional World!"

    }
  }
}
