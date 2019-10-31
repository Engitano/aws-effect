package com.engitano.awseffect.lambda

import fs2._
import _root_.io.circe._
import _root_.io.circe.syntax._
import cats.effect.{ConcurrentEffect, ContextShift}
import cats.syntax.apply._
import com.amazonaws.services.lambda.runtime.Context
import fs2._

import scala.concurrent.ExecutionContext
import java.io.InputStream
import java.io.OutputStream
import cats.data.Kleisli
import cats.effect.Blocker

object EffectfulLambda {

  type EffectfulHandler[F[_], Req, Res] = (Req, Context) => F[Res]

  def apply[F[_]: ConcurrentEffect: ContextShift, Req: Decoder, Res: Encoder](blocker: Blocker)(handler: EffectfulHandler[F, Req, Res]): LambdaHandler[F] =
    StreamLambda[F](blocker) { c =>
      _.through(byteArrayParser)
        .through(decoder[F, Req])
        .mapAsync(1)(i => handler(i, c))
        .map(_.asJson.toString)
        .flatMap(j => Stream(j.getBytes.toSeq: _*))
    } _
}
