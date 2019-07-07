package com.engitano.awseffect.lambda.apigw

case class RequestContextAuthorizer(
    principalId: String
)

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
    headers: Option[Map[String, String]] = None,
    isBase64Encoded: Boolean = false
)