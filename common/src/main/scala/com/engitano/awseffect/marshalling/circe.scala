package com.engitano.awseffect.marshalling

import cats.{Applicative, ApplicativeError}
import cats.syntax.applicative._
import io.circe.syntax._
import io.circe.parser._
import io.circe.{Decoder, Encoder, Json}

object circe {
  implicit def circeToJsonMarshaller[F[_]: Applicative, A: Encoder]: Marshaller[F, A, Json] = new Marshaller[F, A, Json] {
    override def marshall(t: A): F[Json] = t.asJson.pure
  }

  implicit def circeFromJsonMarshaller[F[_]: ApplicativeError[?[_], Throwable], B: Decoder]: Marshaller[F, Json, B] = new Marshaller[F, Json, B] {
    override def marshall(t: Json): F[B] = ApplicativeError[F, Throwable].fromEither(Decoder[B].decodeJson(t))
  }

  implicit def circeToStringMarshaller[F[_]: ApplicativeError[?[_], Throwable], A: Encoder]: Marshaller[F, A, String] =
    Marshaller[F, A, Json].map(_.toString())

  implicit def circeFromStringMarshaller[F[_]: ApplicativeError[?[_], Throwable], B: Decoder]: Marshaller[F, String, B] = new Marshaller[F, String, B] {
    override def marshall(t: String): F[B] = ApplicativeError[F, Throwable].fromEither(parse(t).flatMap(j => Decoder[B].decodeJson(j)))
  }
}
