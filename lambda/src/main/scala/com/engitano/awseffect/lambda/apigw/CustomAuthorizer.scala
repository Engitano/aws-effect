package com.engitano.awseffect.lambda.apigw

import cats.data.State

case class CustomAuthorizerTokenRequest(`type`: String, authorizationToken: String, methodArn: String)

object IAMStatement {
  def allow(action: String, resource: String) = IAMStatement(action, "Allow", resource)
  def deny(action: String, resource: String)  = IAMStatement(action, "Deny", resource)
}
case class IAMStatement private (Action: String, Effect: String, Resource: String)

object IAMPolicyDocument {
  def apply(statements: IAMStatement*): IAMPolicyDocument = IAMPolicyDocument("2012-10-17", statements.toList)
}
case class IAMPolicyDocument(Version: String, Statement: List[IAMStatement])

case class CustomAuthorizerResponse(
    principalId: String,
    policyDocument: IAMPolicyDocument,
    context: Option[Map[String, String]] = None,
    usageIdentifierKey: Option[String] = None
)
