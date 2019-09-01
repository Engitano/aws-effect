package com.engitano.awseffect.lambda

import java.io.{InputStream, OutputStream}
import java.util.concurrent.Executors

import cats.implicits._
import cats.effect.implicits._
import cats.effect.{Blocker, ConcurrentEffect, ContextShift, Resource, Sync}
import com.amazonaws.services.lambda.runtime.{Context, RequestStreamHandler}
import fs2.Pipe
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext

abstract class StreamLambda[F[_]: ConcurrentEffect: ContextShift] extends RequestStreamHandler{

  import StreamLambda._

  private val F = ConcurrentEffect[F]

  protected val errorLogger: (String, Option[Throwable]) => F[Unit] = Logger.error[F]

  protected def threadPool: Resource[F, ExecutionContext] =
    Resource(F.delay {
      val executor = Executors.newSingleThreadExecutor()
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
      _root_.fs2.io
        .readInputStream(F.delay(input), input.available(), Blocker.liftExecutionContext(ec))
        .through(handle(context))
        .through(_root_.fs2.io.writeOutputStream(F.delay(output), Blocker.liftExecutionContext(ec)))
        .compile
        .drain
        .as(())
    }

  override def handleRequest(
      input: InputStream,
      output: OutputStream,
      context: Context
  ): Unit =
    handleCore(input, output, context)
      .handleErrorWith { t =>
        errorLogger(
          s"Error while executing lambda: ${t.getMessage}",
          Some(t)
        ) *>
          F.raiseError(t)
      }
      .toIO
      .unsafeRunSync()
}

object StreamLambda {
  private val logger = LoggerFactory.getLogger(getClass)

  private object Logger {
    def debug[F[_]: Sync](msg: String) = Sync[F].delay(logger.debug(msg))
    def info[F[_]: Sync](msg: String)  = Sync[F].delay(logger.info(msg))
    def warn[F[_]: Sync](msg: String)  = Sync[F].delay(logger.warn(msg))
    def error[F[_]: Sync](msg: String, t: Option[Throwable]) =
      t.fold(Sync[F].delay(logger.error(msg)))(
        t => Sync[F].delay(logger.error(msg, t))
      )
  }
}