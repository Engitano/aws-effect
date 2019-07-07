package com.engitano.awseffect.sqs

import com.engitano.awseffect.messaging.MessageAttributeValue

case class Message(
    id: String,
    receiptHandle: String,
    md5OfBody: String,
    body: String,
    attributes: Map[MessageSystemAttributeName, String],
    md5OfMessageAttributes: String,
    messageAttributes: Map[String, MessageAttributeValue]
)