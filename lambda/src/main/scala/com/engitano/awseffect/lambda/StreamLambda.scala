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

abstract class StreamLambda[F[_]] extends RequestStreamHandler {

  protected val threadCount = 4
  implicit def contextShift(ec: ExecutionContext): ContextShift[F]
  implicit val F: ConcurrentEffect[F]

  implicit def unsafeLogger = Slf4jLogger.getLogger[F]
  protected def logger      = Logger[F]

  protected def threadPool: Resource[F, ExecutionContext] =
    Resource(F.delay {
      val executor = Executors.newCachedThreadPool()
      val ec       = ExecutionContext.fromExecutor(executor)
      (ec, F.delay(executor.shutdown()))
    })

  protected def handle(c: Context)(implicit ec: ExecutionContext): Pipe[F, Byte, Byte]

  private def handleCore(
      input: InputStream,
      output: OutputStream,
      context: Context
  ): F[Unit] =
    threadPool.use { implicit ec =>
      implicit val cs = contextShift(ec)
      val blocker = Blocker.liftExecutionContext(ec)
      _root_.fs2.io
        .readInputStream(F.delay(input), input.available(), blocker)
        .through(handle(context))
        .through(_root_.fs2.io.writeOutputStream(F.delay(output), blocker))
        .compile
        .drain
        .as(())
    }

  override def handleRequest(
      input: InputStream,
      output: OutputStream,
      context: Context
  ): Unit =
    (logger.info("Beginning handler fn") *>
      handleCore(input, output, context)
        .handleErrorWith { t =>
          logger.error(t)(
            s"Error while executing lambda: ${t.getMessage}"
          ) *>
            F.raiseError(t)
        } <* logger.info("Function complete")).toIO
      .unsafeRunSync()
}
