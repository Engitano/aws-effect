package com.engitano.awseffect.lambda.apigw

import cats.syntax.option._
import io.circe.Decoder
import io.circe.HCursor


object RequestContextAuthorizer {
    implicit def decoderForRequestContextAuthorizer: Decoder[RequestContextAuthorizer] = new Decoder[RequestContextAuthorizer]{

      override def apply(c: HCursor): Decoder.Result[RequestContextAuthorizer] = c.get[String]("principalId").map(pId => new RequestContextAuthorizer {

        override val principalId: String = pId

        override def get[A: Decoder](key: String): Option[A] = c.get[A](key).fold(_ => None, _.some)
      })
    }

    def unapply(requestContextAuthorizer: RequestContextAuthorizer): Option[String] = requestContextAuthorizer.principalId.some
}
trait RequestContextAuthorizer {
    val principalId: String
    def get[A: Decoder](key: String): Option[A]
}

case class RequestIdentity(
    cognitoIdentityPoolId: Option[String],
    accountId: Option[String],
    caller: Option[String],
    apiKey: Option[String],
    sourceIp: Option[String],
    cognitoAuthenticationType: Option[String],
    cognitoAuthenticationProvider: Option[String],
    userArn: Option[String],
    userAgent: Option[String],
    user: Option[String]
)

case class RequestContext(
    accountId: String,
    resourceId: String,
    stage: String,
    requestId: String,
    resourcePath: String,
    httpMethod: String,
    apiId: String,
    identity: Option[RequestIdentity] = None,
    authorizer: Option[RequestContextAuthorizer] = None
)

case class ProxyRequest(
    resource: String,
    path: String,
    httpMethod: String,
    requestContext: RequestContext,
    headers: Option[Map[String, String]] = None,
    multiValueHeaders: Option[Map[String, List[String]]] = None,
    pathParameters: Option[Map[String, String]] = None,
    queryStringParameters: Option[Map[String, String]] = None,
    multiValueQueryStringParameters: Option[Map[String, List[String]]] = None,
    stageVariables: Option[Map[String, String]] = None,
    body: Option[String] = None,
    isBase64Encoded: Option[Boolean] = None
)

case class ProxyResponse(
    statusCode: Int,
    body: Option[String],
    headers: Map[String, String] = Map(),
    isBase64Encoded: Boolean = false
)