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
import com.engitano.awseffect.lambda.apigw.{ApiGatewayLambda, ProxyRequest, ProxyResponse}
import fs2.Stream
import org.http4s._

import scala.concurrent.ExecutionContext
import cats.data.Kleisli

final case class LambdaRequest[F[_]](req: Request[F], original: ProxyRequest, ctx: Context) {
  def mapK[G[_]](fk: F ~> G): LambdaRequest[G] =
    LambdaRequest(req.mapK(fk), original, ctx)
}

// Mad props to https://github.com/howardjohn/scala-server-lambda
abstract class LambdaHost[F[_]: ConcurrentEffect: ContextShift] extends ApiGatewayLambda[F] {

  protected def service(implicit ec: ExecutionContext): F[LambdaRoutes[F]]

  override protected def handle(proxyReq: ProxyRequest, c: Context)(implicit ec: ExecutionContext): F[ProxyResponse] =
    parseRequest(proxyReq).flatMap { req =>
      service.flatMap {
        _.run(LambdaRequest(req,proxyReq, c))
          .getOrElse(Response.notFound)
          .flatMap(asProxyResponse)
      }
    }

  private def parseRequest(request: ProxyRequest): F[Request[F]] = F.fromEither {
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

  private def reconstructPath(request: ProxyRequest): String = {
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

  private def asProxyResponse(resp: Response[F]): F[ProxyResponse] =
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

  private def toHeaders(headers: Map[String, String]): Headers =
    Headers {
      headers.map {
        case (k, v) => Header(k, v)
      }.toList
    }

  private def encodeBody(body: String) = Stream(body).through(fs2.text.utf8Encode)
}
