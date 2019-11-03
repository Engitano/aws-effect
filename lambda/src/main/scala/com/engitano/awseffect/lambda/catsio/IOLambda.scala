package com.engitano.awseffect.lambda.catsio

import cats.syntax.apply._
import scala.concurrent.ExecutionContext
import cats.effect.IO
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import cats.effect.Resource
import java.io.InputStream
import java.io.OutputStream
import com.amazonaws.services.lambda.runtime.Context
import cats.effect.Blocker
import com.engitano.awseffect.lambda.LambdaHandler
import cats.effect.ConcurrentEffect
import cats.effect.ContextShift
import java.util.concurrent.Executors
import java.util.logging.Handler
import cats.effect.concurrent.MVar

trait IOLambda extends RequestStreamHandler {

  implicit val ec = com.engitano.awseffect.lambda.internal.PoolUtils.ioLambdaGlobal
  implicit val cs = IO.contextShift(ec)
  val blocker     = Blocker.liftExecutionContext(ExecutionContext.fromExecutor(Executors.newCachedThreadPool()))

  private val _handler: MVar[IO, LambdaHandler[IO]] = MVar.empty[IO, LambdaHandler[IO]].unsafeRunSync()

  override def handleRequest(
      input: InputStream,
      output: OutputStream,
      context: Context
  ): Unit =
    (for {
      fh <- _handler.tryTake
      h  <- fh.map { IO(_) }.getOrElse(handler(blocker)(ec, cs))
      _  <- _handler.tryPut(h)
      r  <- h(input, output, context)
    } yield r).unsafeRunSync()

  def handler(blocker: Blocker)(implicit ec: ExecutionContext, cs: ContextShift[IO]): IO[LambdaHandler[IO]]
}
