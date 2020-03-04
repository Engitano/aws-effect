package com.engitano.awseffect.lambda.apigw

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import cats.effect.IO
import cats.syntax.option._
import com.amazonaws.services.lambda.runtime.Context
import com.engitano.awseffect.lambda.apigw.Messages.{Input, Output}
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.ExecutionContext
import com.engitano.awseffect.lambda.catsio.IOLambda
import cats.effect.Blocker
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import cats.effect.ContextShift

class ProxyLambdaSpec extends WordSpec with Matchers with MockFactory {

  import ProxyMarshallers._
  "The ApiGatewayLambda" should {
    "return a valid JSON response" in {

      val sut = new IOLambda with Dsl[IO] {
        def handler(blocker: Blocker) = {
          IO(ApiGatewayHandler[IO](blocker) { (p, _) =>
            p.as[Input].map { ip =>
              ProxyResponse(
                200,
                Output(ip.name + "!").asJson.toString().some
              )
            }
          })
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
