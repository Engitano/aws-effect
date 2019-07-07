package com.engitano.awseffect.sns

import com.engitano.awseffect.sns.SnsTarget.{PhoneNumber, TargetArn, TopicArn}

sealed trait SnsTarget
object SnsTarget {
  case class TopicArn(arn: String)       extends SnsTarget
  case class TargetArn(arn: String)      extends SnsTarget
  case class PhoneNumber(number: String) extends SnsTarget
}

case class SnsMessage(
    default: String,
    email: Option[String],
    sqs: Option[String],
    lambda: Option[String],
    http: Option[String],
    https: Option[String],
    sms: Option[String]
)
object SnsMessage {
  def apply(
      default: String
  ): SnsMessage = SnsMessage(default, None, None, None, None, None, None)
}
