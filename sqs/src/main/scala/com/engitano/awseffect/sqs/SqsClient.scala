package com.engitano.awseffect.sqs

import cats.effect.{Concurrent, Timer}
import cats.instances.list._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.traverse._
import com.engitano.awseffect.concurrent._
import com.engitano.awseffect.marshalling.Marshaller
import com.engitano.awseffect.messaging.MessageAttributeValue
import fs2._
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.{DeleteMessageRequest, SendMessageRequest}

import scala.concurrent.duration.FiniteDuration
import scala.jdk.CollectionConverters._

trait SqsClient[F[_]] {

  def poll[A](
      queueUrl: String,
      timeoutSeconds: Option[Int] = None,
      visibilityTimeoutSeconds: Option[Int] = None,
      receiveRequestAttemptId: Option[String] = None
  )(implicit M: Marshaller[F, Message, A]): F[List[WithReceipt[A]]]

  def deleteMessage(receiptHandle: String, queueUrl: String): F[Unit]

  def streamFrom[A: Marshaller[F, Message, ?], B](
      queueUrl: String,
      interval: FiniteDuration,
      timeoutSeconds: Option[Int] = None,
      visibilityTimeoutSeconds: Option[Int] = None,
      receiveRequestAttemptId: Option[String] = None
  )(handler: A => F[B])(implicit T: Timer[F]): Stream[F, B]

  def send[B](
      queueUrl: String,
      messageBody: B,
      delaySeconds: Option[Int] = None,
      messageAttributes: Map[String, MessageAttributeValue] = Map(),
      messageDeduplicationId: Option[String] = None,
      messageGroupId: Option[String] = None
  )(implicit M: Marshaller[F, B, String]): F[Unit]
}

object SqsClient {

  import com.engitano.awseffect.syntax.builder._
  import conversions._

  def apply[F[_]: Concurrent](wrapped: SqsAsyncClient): SqsClient[F] = new SqsClient[F] {
    override def poll[A](
        queueUrl: String,
        timeoutSeconds: Option[Int] = None,
        visibilityTimeoutSeconds: Option[Int] = None,
        receiveRequestAttemptId: Option[String] = None
    )(implicit M: Marshaller[F, Message, A]): F[List[WithReceipt[A]]] =
      wrapped
        .receiveMessage(
          r =>
            r.queueUrl(queueUrl)
              .appendOpt(timeoutSeconds.map(_.asInstanceOf[Integer]))(_.waitTimeSeconds)
              .appendOpt(visibilityTimeoutSeconds.map(_.asInstanceOf[Integer]))(_.visibilityTimeout)
              .appendOpt(receiveRequestAttemptId)(_.receiveRequestAttemptId)
              .build()
        )
        .to[F]
        .flatMap { r =>
          r.messages()
            .asScala
            .map(_.asScala)
            .toList
            .traverse(
              m =>
                M.marshall(m)
                  .map(a => WithReceipt(a, m.receiptHandle))
            )
        }

    def deleteMessage(receiptHandle: String, queueUrl: String): F[Unit] =
      wrapped.deleteMessage(DeleteMessageRequest.builder().receiptHandle(receiptHandle).queueUrl(queueUrl).build()).to[F].as(())

    override def streamFrom[A: Marshaller[F, Message, ?], B](
        queueUrl: String,
        interval: FiniteDuration,
        timeoutSeconds: Option[Int] = None,
        visibilityTimeoutSeconds: Option[Int] = None,
        receiveRequestAttemptId: Option[String] = None
    )(handler: A => F[B])(implicit T: Timer[F]): Stream[F, B] =
      Stream
        .awakeEvery[F](interval)
        .evalMap { _ =>
          poll(queueUrl, timeoutSeconds, visibilityTimeoutSeconds, receiveRequestAttemptId)
        }
        .flatMap(l => Stream.emits(l).covary[F])
        .flatMap(wr => Stream.emit(wr.wrapped).evalMap(handler).map(b => WithReceipt(b, wr.receiptHandle)))
        .evalTap(wr => deleteMessage(wr.receiptHandle, queueUrl))
        .map(wr => wr.wrapped)

    override def send[B](
        queueUrl: String,
        messageBody: B,
        delaySeconds: Option[Int] = None,
        messageAttributes: Map[String, MessageAttributeValue] = Map(),
        messageDeduplicationId: Option[String] = None,
        messageGroupId: Option[String] = None
    )(implicit M: Marshaller[F, B, String]): F[Unit] =
      M.marshall(messageBody)
        .flatMap { body =>
          wrapped
            .sendMessage(
              (r: SendMessageRequest.Builder) =>
                r.queueUrl(queueUrl)
                  .messageBody(body)
                  .appendOpt(delaySeconds.map(_.asInstanceOf[Integer]))(_.delaySeconds)
                  .messageAttributes(messageAttributes.map(p => p._1 -> p._2.asJava).asJava)
                  .appendOpt(messageDeduplicationId)(_.messageDeduplicationId)
                  .appendOpt(messageGroupId)(_.messageGroupId)
                  .build()
            )
            .to[F]
        }
        .as(())
  }
}
