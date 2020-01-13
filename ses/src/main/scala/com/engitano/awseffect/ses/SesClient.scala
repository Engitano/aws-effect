package com.engitano.awseffect.ses

import cats.syntax.functor._
import cats.effect.Async
import com.engitano.awseffect.concurrent._
import com.engitano.awseffect.JDKCollectionConvertersCompat.Converters._
import com.engitano.awseffect.messaging.MessageAttributeValue
import software.amazon.awssdk.services.ses.SesAsyncClient
import io.circe.generic.auto._
import io.circe.syntax._
import com.engitano.awseffect.concurrent._
import com.engitano.awseffect.syntax.builder._
import software.amazon.awssdk.services.ses.model.DeleteConfigurationSetEventDestinationResponse

trait SesClient[F[_]] {
  def sendEmail(msg: SendEmailRequest): F[Unit]
}

object SesClient {
  def apply[F[_]: Async](wrapped: SesAsyncClient) = new SesClient[F] {
    def sendEmail(msg: SendEmailRequest): F[Unit] = 
      wrapped.sendEmail(msg.toSes).to[F].as(())
  }
}
