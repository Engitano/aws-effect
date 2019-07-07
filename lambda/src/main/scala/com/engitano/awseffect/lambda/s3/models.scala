package com.engitano.awseffect.lambda.s3

case class UserIdentity(
    principalId: String
)

case class Bucket(
    name: String,
    ownerIdentity: UserIdentity,
    arn: String
)
case class ObjectBis(
    key: String,
    size: Double,
    eTag: String,
    versionId: String
)
case class Metadata(
    s3SchemaVersion: String,
    configurationId: String,
    bucket: Bucket,
    `object`: ObjectBis
)
case class Record(
    eventVersion: String,
    eventSource: String,
    awsRegion: String,
    eventTime: String,
    eventName: String,
    userIdentity: UserIdentity,
    requestParameters: Map[String, String],
    responseElements: Map[String, String],
    s3: Metadata
)
case class S3Message(
    Records: List[Record]
)
