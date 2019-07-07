package com.engitano.awseffect.sns

import cats.syntax.functor._
import cats.effect.Async
import com.engitano.awseffect.JDKCollectionConvertersCompat.Converters._
import com.engitano.awseffect.messaging.MessageAttributeValue
import software.amazon.awssdk.services.sns.SnsAsyncClient
import software.amazon.awssdk.services.sns.model.PublishRequest
import io.circe.generic.auto._
import io.circe.syntax._
import com.engitano.awseffect.concurrent._
import com.engitano.awseffect.sns.conversions._
import com.engitano.awseffect.syntax.builder._

trait SnsClient[F[_]] {}

object SnsClient {
  def apply[F[_]: Async](wrapped: SnsAsyncClient) = new SnsClient[F] {
    def publish(msg: SnsMessage, target: SnsTarget, subject: String, messageAttributes: Map[String, MessageAttributeValue] = Map()): F[Unit] = {
      val req = PublishRequest
        .builder()
        .message(msg.asJson.toString())
        .subject(subject)
        .messageAttributes(messageAttributes.map(p => p._1 -> p._2.asJava).asJava)
        .withTarget(target)
        .build()

      wrapped.publish(req).to[F].as(())
    }
  }
}
