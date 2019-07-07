package com.engitano.awseffect.lambda.sqs

case class Attributes(
                       ApproximateReceiveCount: String,
                       SentTimestamp: String,
                       SenderId: String,
                       ApproximateFirstReceiveTimestamp: String
                     )
case class MessageAttribute(
                             Type: String,
                             Value: String
                           )
case class Record(
                    messageId: String,
                    receiptHandle: String,
                    body: String,
                    attributes: Attributes,
                    messageAttributes: List[MessageAttribute],
                    md5OfBody: String,
                    eventSource: String,
                    eventSourceARN: String,
                    awsRegion: String
                  )
case class SqsMessage(
                           Records: List[Record]
                         )
