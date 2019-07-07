package com.engitano.awseffect.lambda.kinesis

case class Metadata(
                     kinesisSchemaVersion: String,
                     partitionKey: String,
                     sequenceNumber: String,
                     data: String,
                     approximateArrivalTimestamp: Double
                   )
case class KinesisRecord(
                          kinesis: Metadata,
                          eventSource: String,
                          eventVersion: String,
                          eventID: String,
                          eventName: String,
                          invokeIdentityArn: String,
                          awsRegion: String,
                          eventSourceARN: String
                        )
case class KinesisMessage(
                           Records: List[KinesisRecord]
                         )
