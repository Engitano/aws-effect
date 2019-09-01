package com.engitano.awseffect.lambda

import fs2._
import _root_.io.circe._
import _root_.io.circe.syntax._
import cats.effect.{ConcurrentEffect, ContextShift}
import com.amazonaws.services.lambda.runtime.Context
import fs2._

import scala.concurrent.ExecutionContext

abstract class EffectfulLambda[F[_]: ConcurrentEffect: ContextShift, Req: Decoder, Res: Encoder]
    extends StreamLambda[F] {

  protected def handle(i: Req, c: Context)(implicit ec: ExecutionContext): F[Res]

  override protected def handle(c: Context)(implicit ec: ExecutionContext): Pipe[F, Byte, Byte] =
    _.through(byteArrayParser)
      .through(decoder[F, Req])
      .mapAsync(1)(i => handle(i, c))
      .flatMap(j => Stream(j.asJson.toString.getBytes.toSeq: _*))
}
