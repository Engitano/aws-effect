package com.engitano.awseffect.lambda.apigw

import cats.effect.Sync
import cats.implicits._
import com.engitano.awseffect.marshalling.{Marshaller, MarshallingException}
import io.circe.Decoder
import io.circe.parser._

trait ProxyMarshallers {

  implicit def circeProxyRequestMarshaller[F[_]: Sync, I: Decoder]: Marshaller[F, ProxyRequest, I] = new Marshaller[F, ProxyRequest, I] {
    override def marshall(t: ProxyRequest): F[I] = t.body match {
        case Some(b) => decode[I](b).liftTo[F]
        case None => MarshallingException("Empty request body").raiseError[F, I]
      }
    }
}

object ProxyMarshallers extends ProxyMarshallers