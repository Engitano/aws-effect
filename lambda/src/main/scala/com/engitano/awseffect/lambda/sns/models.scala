package com.engitano.awseffect.lambda.sns

case class MessageAttribute(
    Type: String,
    Value: String
)

case class SnsRecord(
    SignatureVersion: String,
    Timestamp: String,
    Signature: String,
    SigningCertUrl: Option[String],
    MessageId: String,
    Message: String,
    MessageAttributes: List[MessageAttribute],
    Type: String,
    UnsubscribeUrl: String,
    TopicArn: String,
    Subject: String
)
case class Record(
    EventVersion: String,
    EventSubscriptionArn: String,
    EventSource: String,
    Sns: SnsRecord
)
case class SnsMessage(
    Records: List[Record]
)
