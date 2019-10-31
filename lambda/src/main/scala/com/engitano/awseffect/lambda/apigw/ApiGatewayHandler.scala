package com.engitano.awseffect.lambda.apigw

import cats.effect.{ConcurrentEffect, ContextShift}
import com.engitano.awseffect.lambda.LambdaHandler
import com.engitano.awseffect.lambda.EffectfulLambda
import io.circe.generic.auto._

object ApiGatewayHandler {

    import EffectfulLambda._

    def apply[F[_]: ConcurrentEffect: ContextShift](handler: EffectfulHandler[F, ProxyRequest, ProxyResponse]): LambdaHandler[F] = 
        EffectfulLambda(handler)
}