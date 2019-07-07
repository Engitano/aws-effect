package com.engitano.awseffect.sqs

import com.engitano.awseffect.messaging.MessageAttributeValue
import com.engitano.awseffect.messaging.MessageAttributeValue.{Binary, StringList}
import com.engitano.awseffect.sqs.MessageSystemAttributeName._
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.sqs.model.{Message => AwsMsg, MessageAttributeValue => AwsMav, MessageSystemAttributeName => AwsMsan}

import scala.collection.mutable
import scala.jdk.CollectionConverters._

object conversions {

  implicit class MavConversions(awsMav: AwsMav) {
    def asScala: MessageAttributeValue =
      Option(awsMav.stringValue())
        .map(s => MessageAttributeValue.String(s))
        .orElse(Option(awsMav.stringListValues()).map(s => StringList(s.asScala.toList)))
        .orElse(Option(awsMav.binaryValue()).map(b => b.asByteArray()).map(Binary))
        .getOrElse(
          Binary(
            Option(awsMav.binaryListValues().asScala)
              .getOrElse(mutable.Buffer())
              .flatMap(_.asByteArray())
              .toArray
          )
        )
  }

  implicit class MessageAttributeValueConversions(mav: MessageAttributeValue) {
    def asJava: AwsMav = mav match {
      case MessageAttributeValue.String(s) => AwsMav.builder().stringValue(s).build()
      case MessageAttributeValue.StringList(s) => AwsMav.builder().stringListValues(s.asJava).build()
      case MessageAttributeValue.Binary(s) => AwsMav.builder().binaryValue(SdkBytes.fromByteArray(s)).build()
      case MessageAttributeValue.Number(s) => AwsMav.builder().stringListValues(s).dataType("Number").build()
      case MessageAttributeValue.NumberList(s) => AwsMav.builder().stringListValues(s:_*).dataType("Number").build()
    }
  }

  implicit class PimpedRecieveMessage(msg: AwsMsg) {
    def asScala =
      Message(
        msg.messageId(),
        msg.receiptHandle(),
        msg.md5OfBody(),
        msg.body(),
        msg.attributes().asScala.toMap.map(p => p._1.asScala -> p._2),
        msg.md5OfMessageAttributes(),
        msg.messageAttributes().asScala.toMap.view.mapValues(v => v.asScala).toMap
      )
  }

  implicit class PimpedAwsMsan(m: AwsMsan) {
    def asScala = m match {
      case AwsMsan.APPROXIMATE_FIRST_RECEIVE_TIMESTAMP => ApproximateFirstReceiveTimestamp
      case AwsMsan.APPROXIMATE_RECEIVE_COUNT           => ApproximateReceiveCount
      case AwsMsan.MESSAGE_DEDUPLICATION_ID            => MessageDeduplicationId
      case AwsMsan.MESSAGE_GROUP_ID                    => MessageGroupId
      case AwsMsan.SENDER_ID                           => SenderId
      case AwsMsan.SENT_TIMESTAMP                      => SentTimestamp
      case AwsMsan.SEQUENCE_NUMBER                     => SequenceNumber
      case AwsMsan.UNKNOWN_TO_SDK_VERSION              => Unknown
    }
  }

  implicit class PimpedMessageSystemAttributeName(msan: MessageSystemAttributeName) {
    def asJava = msan match {
      case ApproximateFirstReceiveTimestamp => AwsMsan.APPROXIMATE_FIRST_RECEIVE_TIMESTAMP
      case ApproximateReceiveCount          => AwsMsan.APPROXIMATE_RECEIVE_COUNT
      case MessageDeduplicationId           => AwsMsan.MESSAGE_DEDUPLICATION_ID
      case MessageGroupId                   => AwsMsan.MESSAGE_GROUP_ID
      case SenderId                         => AwsMsan.SENDER_ID
      case SentTimestamp                    => AwsMsan.SENT_TIMESTAMP
      case SequenceNumber                   => AwsMsan.SENT_TIMESTAMP
      case Unknown                          => AwsMsan.UNKNOWN_TO_SDK_VERSION
    }
  }
}
