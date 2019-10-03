package com.engitano.awseffect.lambda

import cats.data.{Kleisli, OptionT}
import cats.effect.Sync
import cats.instances.option._
import cats.syntax.traverse._

package object http4s {
  import com.amazonaws.services.lambda.runtime.Context
  import com.engitano.awseffect.lambda.apigw.ProxyRequest
  import org.http4s.Request
  import org.http4s.Response
  type LambdaRoutes[F[_]] = Kleisli[OptionT[F, ?], LambdaRequest[F], Response[F]]

  object LambdaRoutes {
    def of[F[_]](pf: PartialFunction[LambdaRequest[F], F[Response[F]]])(implicit F: Sync[F]): LambdaRoutes[F] =
      Kleisli(req => OptionT(F.suspend(pf.lift(req).sequence)))
  }

}
