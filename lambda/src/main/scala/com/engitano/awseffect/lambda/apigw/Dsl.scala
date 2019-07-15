package com.engitano.awseffect.lambda.apigw

import cats.effect.Sync
import cats.syntax.functor._
import cats.syntax.option._
import shapeless.the
import com.engitano.awseffect.marshalling.Marshaller

trait Dsl[F[_]] {
  def ok[A: Marshaller[F, ?, String]](a: A)(implicit F: Sync[F]): F[ProxyResponse] =
    buildResponse(200, a)
  def created[A: Marshaller[F, ?, String]](a: A)(implicit F: Sync[F]): F[ProxyResponse] =
    buildResponse(201, a)
  def noContent[A: Marshaller[F, ?, String]](a: A)(implicit F: Sync[F]): F[ProxyResponse] =
    buildResponse(204, a)

  def movedPermanently(location: String)(implicit F: Sync[F]): F[ProxyResponse] =
    F.pure(ProxyResponse(301, None, Map("location" -> location)))
  def found(location: String) =
    ProxyResponse(302, None, Map("location" -> location))
  def temporaryRedirect(location: String)(implicit F: Sync[F]): F[ProxyResponse] =
    F.pure(ProxyResponse(307, None, Map("location" -> location)))
  def permanentRedirect(location: String)(implicit F: Sync[F]): F[ProxyResponse] =
    F.pure(ProxyResponse(308, None, Map("location" -> location)))

  def badRequest[A: Marshaller[F, ?, String]](a: A)(implicit F: Sync[F]): F[ProxyResponse] =
    buildResponse(400, a)
  def unauthorised[A: Marshaller[F, ?, String]](a: A)(implicit F: Sync[F]): F[ProxyResponse] =
    buildResponse(400, a)

  def buildResponse[A: Marshaller[F, ?, String]](code: Int, a: A)(
      implicit F: Sync[F]
  ) =
    the[Marshaller[F, A, String]].marshall(a).map { b =>
      ProxyResponse(
        code,
        b.some,
        Map("content-type" -> "application/json")
      )
    }
  implicit class SyntaxForRequest(req: ProxyRequest) {
    def as[T](
        implicit M: Marshaller[F, ProxyRequest, T]
    ): F[T] = M.marshall(req)
  }
}
