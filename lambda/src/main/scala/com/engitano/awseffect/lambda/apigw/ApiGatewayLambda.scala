package com.engitano.awseffect.lambda.apigw

import cats.effect.{ConcurrentEffect, ContextShift}
import com.engitano.awseffect.lambda.EffectfulLambda
import io.circe.generic.auto._

abstract class ApiGatewayLambda[F[_]: ConcurrentEffect: ContextShift] extends EffectfulLambda[F, ProxyRequest, ProxyResponse]
