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
import cats.effect.concurrent.Ref

class Http4sHandlerSpec extends WordSpec with Matchers with MockFactory {

  import com.engitano.awseffect.lambda.apigw.ProxyMarshallers._

  "The Http4sHandler" should {
    "return a valid JSON response" in {
      import Dsl._
      import org.http4s.dsl.io._

      val svc = LambdaRoutes.of[IO] {
        case req @ PUT -> Root / "test" / "hello" λ _ =>
          req.req.as[Input].flatMap { i =>
            Ok(Output(i.name + "!"))
          }
      }

      val check = Ref[IO].of(0).unsafeRunSync
      val sut = new IOLambda {
        def handler(blocker: Blocker) =
          for {
            v <- check.get
            _ <- check.set(v + 1)
            h = Http4sHandler[IO](blocker)(svc.orNotFound)
          } yield h
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

      val inputStream2 =
        new ByteArrayInputStream(Messages.requestWithInputName.getBytes)
      val output2 = new ByteArrayOutputStream()
      sut.handleRequest(inputStream2, output2, mock[Context])
      check.get.unsafeRunSync() shouldBe 1

    }
  }
}
