package com.engitano.awseffect.lambda.http4s
import org.http4s.Request
import com.engitano.awseffect.lambda.apigw.ProxyRequest
import com.amazonaws.services.lambda.runtime.Context


trait Lambda {

    val λ = from

    object from {
      def unapply[F[_]](ar: LambdaRequest[F]): Option[(Request[F], (ProxyRequest, Context))] =
        Some(ar.req -> (ar.original -> ar.ctx))
    }
  }

  object Dsl extends Lambda