package com.engitano.awseffect.sqs

sealed trait MessageSystemAttributeName

object MessageSystemAttributeName {
  case object SenderId                         extends MessageSystemAttributeName
  case object SentTimestamp                    extends MessageSystemAttributeName
  case object ApproximateReceiveCount          extends MessageSystemAttributeName
  case object ApproximateFirstReceiveTimestamp extends MessageSystemAttributeName
  case object SequenceNumber                   extends MessageSystemAttributeName
  case object MessageDeduplicationId           extends MessageSystemAttributeName
  case object MessageGroupId                   extends MessageSystemAttributeName
  case object Unknown                          extends MessageSystemAttributeName
}
