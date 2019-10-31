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

trait IOLambda extends RequestStreamHandler {

  private def threadPool: Resource[IO, ExecutionContext] =
    Resource(IO.delay {
      val ec       = com.engitano.awseffect.lambda.internal.PoolUtils.ioLambdaGlobal
      (ec._1, IO.delay(ec._2.shutdown()))
    })

  override def handleRequest(
      input: InputStream,
      output: OutputStream,
      context: Context
  ): Unit = {
    (Blocker[IO], threadPool).tupled
      .use { threading =>
        val blocker     = threading._1
        handler(threading._2)(input, output, context, blocker)
      }
      .unsafeRunSync()
  }

  def handler(implicit ec: ExecutionContext): LambdaHandler[IO]
}