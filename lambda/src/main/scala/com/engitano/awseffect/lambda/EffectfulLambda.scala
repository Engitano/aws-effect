package com.engitano.awseffect.lambda

import fs2._
import _root_.io.circe._
import _root_.io.circe.syntax._
import cats.effect.{ConcurrentEffect, ContextShift}
import cats.syntax.apply._
import com.amazonaws.services.lambda.runtime.Context
import fs2._

import scala.concurrent.ExecutionContext

abstract class EffectfulLambda[F[_], Req: Decoder, Res: Encoder]
    extends StreamLambda[F] {

  protected def handle(i: Req, c: Context)(implicit ec: ExecutionContext): F[Res]

  override protected def handle(c: Context)(implicit ec: ExecutionContext): Pipe[F, Byte, Byte] =
    _.through(byteArrayParser)
      .through(decoder[F, Req])
      .evalTap(r => logger.info("Request Deserialized"))
      .mapAsync(1)(i => logger.info("Running effectful handler") *> handle(i, c) <* logger.info("Effectful handler complete"))
      .map(_.asJson.toString)
      .evalTap(j => logger.info(s"Returning json ${j}"))
      .flatMap(j => Stream(j.getBytes.toSeq: _*))
}
