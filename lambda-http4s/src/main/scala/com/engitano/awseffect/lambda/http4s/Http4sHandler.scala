package com.engitano.awseffect.lambda.http4s

import cats.~>
import cats.Functor
import cats.data.OptionT
import cats.effect.{ConcurrentEffect, ContextShift}
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.option._
import com.amazonaws.services.lambda.runtime.Context
import com.engitano.awseffect.lambda.apigw.{ApiGatewayHandler, ProxyRequest, ProxyResponse}
import com.engitano.awseffect.lambda.LambdaHandler
import fs2.Stream
import org.http4s._

import scala.concurrent.ExecutionContext
import cats.data.Kleisli
import cats.effect.Blocker
import io.chrisdavenport.vault.Key

final case class LambdaRequest[F[_]](req: Request[F], originalParams: Option[LambdaRequestParams]) {
  def mapK[G[_]](fk: F ~> G): LambdaRequest[G] =
    LambdaRequest(req.mapK(fk), originalParams)
}

case class LambdaRequestParams(proxyRequest: ProxyRequest, context: Context)

object Http4sHandler {

  def apply[F[_]: ConcurrentEffect: ContextShift](blocker: Blocker)(service: HttpApp[F], vaultKey: Key[LambdaRequestParams]): LambdaHandler[F] =
    ApiGatewayHandler(blocker) { (p, c) =>
      val F = ConcurrentEffect[F]

      def parseRequest(request: ProxyRequest): F[Request[F]] = F.fromEither {
        for {
          uri    <- Uri.fromString(reconstructPath(request))
          method <- Method.fromString(request.httpMethod)
        } yield
          Request[F](
            method,
            uri,
            headers = request.headers.map(toHeaders).getOrElse(Headers.empty),
            body = request.body.map(encodeBody).getOrElse(EmptyBody)
          )
      }

      def reconstructPath(request: ProxyRequest): String = {
        val requestString = request.queryStringParameters
          .map {
            _.map {
              case (k, v) => s"$k=$v"
            }.mkString("&")
          }
          .map { qs =>
            if (qs.isEmpty) "" else "?" + qs
          }
          .getOrElse("")

        request.path + requestString
      }

      def asProxyResponse(resp: Response[F]): F[ProxyResponse] =
        resp
          .as[String]
          .map { body =>
            ProxyResponse(
              resp.status.code,
              body.some,
              resp.headers.toList
                .map(h => h.name.value -> h.value)
                .toMap
            )
          }

      def toHeaders(headers: Map[String, String]): Headers =
        Headers {
          headers.map {
            case (k, v) => Header(k, v)
          }.toList
        }

      def encodeBody(body: String) = Stream(body).through(fs2.text.utf8Encode)

      parseRequest(p).flatMap { req => 
        val reqWithLambda = req.withAttribute(vaultKey, LambdaRequestParams(p, c))
        service
          .run(reqWithLambda)
          .flatMap(asProxyResponse)
      }
    }
}
