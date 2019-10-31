package com.engitano.awseffect.lambda

import java.io.{InputStream, OutputStream}
import java.util.concurrent.Executors

import cats.implicits._
import cats.effect.implicits._
import cats.effect.{Blocker, ConcurrentEffect, ContextShift, Resource, Sync}
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import com.amazonaws.services.lambda.runtime.{Context, RequestStreamHandler}
import fs2.Pipe

import scala.concurrent.ExecutionContext
import cats.effect.IO

object StreamLambda {

  type StreamHandler[F[_]] = Context => Pipe[F, Byte, Byte]

  def apply[F[_]: ConcurrentEffect: ContextShift](handler: StreamHandler[F]): LambdaHandler[F] =
    (
        input: InputStream,
        output: OutputStream,
        context: Context,
        blocker: Blocker
    ) =>
      _root_.fs2.io
        .readInputStream(Sync[F].delay(input), input.available(), blocker)
        .through(handler(context))
        .through(_root_.fs2.io.writeOutputStream(Sync[F].delay(output), blocker))
        .compile
        .drain
        .as(())
}
