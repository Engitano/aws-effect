package com.engitano.awseffect.lambda.http4s

import cats.syntax.flatMap._
import cats.syntax.functor._
import org.http4s.HttpApp
import com.engitano.awseffect.lambda.catsio.IOLambda
import cats.effect.{Blocker, IO}
import org.http4s.HttpApp
import io.chrisdavenport.vault.Key
import cats.effect.Sync
import cats.FlatMap
import cats.effect.ConcurrentEffect
import cats.effect.ContextShift

trait IOHttp4sLambda extends IOLambda with Http4sLambda[IO] {

    override def handler(blocker: Blocker): IO[com.engitano.awseffect.lambda.LambdaHandler[IO]] = _handler(blocker)
}

trait Http4sLambda[F[_]] {

    case class Http4sApp(service: HttpApp[F], vaultKey: Key[LambdaRequestParams])

    def lambdaRequestKey(implicit F: Sync[F]) = Key.newKey[F, LambdaRequestParams]

    def lambdaHandler(blocker: Blocker, key: Key[LambdaRequestParams]): F[HttpApp[F]]

    def _handler(blocker: Blocker)(implicit F: ConcurrentEffect[F], CS: ContextShift[F]): F[com.engitano.awseffect.lambda.LambdaHandler[F]] = 
        lambdaRequestKey.flatMap { key => 
        lambdaHandler(blocker, key).map(h => Http4sHandler(blocker)(h, key))
    }
}