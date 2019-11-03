package com.engitano.awseffect.lambda

import cats.~>
import cats.data.{Kleisli, OptionT}
import cats.syntax.functor._
import cats.syntax.contravariant._
import cats.syntax.profunctor._
import cats.effect.Sync
import cats.instances.option._
import cats.syntax.traverse._

package object http4s {
  import org.http4s.Status
  import cats.Applicative
  import org.http4s.Header
  import org.http4s.Headers
  import cats.Functor
  import com.amazonaws.services.lambda.runtime.Context
  import com.engitano.awseffect.lambda.apigw.ProxyRequest
  import org.http4s.Request
  import org.http4s.Response
  type LambdaRoutes[F[_]] = Kleisli[OptionT[F, ?], LambdaRequest[F], Response[F]]
  type LambdaApp[F[_]] = Kleisli[F, LambdaRequest[F], Response[F]]

  object LambdaRoutes {
    def of[F[_]](pf: PartialFunction[LambdaRequest[F], F[Response[F]]])(implicit F: Sync[F]): LambdaRoutes[F] =
      Kleisli(req => OptionT(F.suspend(pf.lift(req).sequence)))
  }

  implicit class LambdaRoutesExtensions[F[_]](routes: LambdaRoutes[F]) {
    def orNotFound(implicit F: Functor[F]): LambdaApp[F] = Kleisli { req => 
      routes(req).value.map {
        case Some(res) => res
        case None => Response[F](status = Status.NotFound)
      }
    }
  }
}
