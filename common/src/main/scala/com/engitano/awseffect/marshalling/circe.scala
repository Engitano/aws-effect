package com.engitano.awseffect.marshalling

import cats.effect.Sync
import cats.syntax.applicative._
import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json}

object circe {
  implicit def circeToJsonMarshaller[F[_]: Sync, A: Encoder]: Marshaller[F, A, Json] = new Marshaller[F, A, Json] {
    override def marshall(t: A): F[Json] = t.asJson.pure
  }

  implicit def circeFromJsonMarshaller[F[_]: Sync, B: Decoder]: Marshaller[F, Json, B] = new Marshaller[F, Json, B] {
    override def marshall(t: Json): F[B] = Sync[F].fromEither(Decoder[B].decodeJson(t))
  }
}
