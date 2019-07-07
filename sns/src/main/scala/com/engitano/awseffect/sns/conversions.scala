package com.engitano.awseffect.sns

import com.engitano.awseffect.messaging.MessageAttributeValue
import com.engitano.awseffect.messaging.MessageAttributeValue.{Binary, String}
import com.engitano.awseffect.sns.SnsTarget.{PhoneNumber, TargetArn, TopicArn}
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.sns.model.{PublishRequest, MessageAttributeValue => AwsMav}

object conversions {

  implicit class MavConversions(awsMav: AwsMav) {
    def asScala: MessageAttributeValue =
      Option(awsMav.stringValue())
        .map(s => MessageAttributeValue.String(s))
        .getOrElse(Binary(awsMav.binaryValue().asByteArray()))
  }

  implicit class MessageAttributeValueConversions(mav: MessageAttributeValue) {
    def asJava: AwsMav = mav match {
      case MessageAttributeValue.String(s) => AwsMav.builder().stringValue(s).build()
      case MessageAttributeValue.Binary(s) => AwsMav.builder().binaryValue(SdkBytes.fromByteArray(s)).build()
    }
  }

  implicit class PimpedPublishRequest(pr: PublishRequest.Builder) {
    def withTarget(target: SnsTarget) = target match {
      case TopicArn(arn)       => pr.topicArn(arn)
      case TargetArn(arn)      => pr.targetArn(arn)
      case PhoneNumber(number) => pr.phoneNumber(number)
    }
  }
}
