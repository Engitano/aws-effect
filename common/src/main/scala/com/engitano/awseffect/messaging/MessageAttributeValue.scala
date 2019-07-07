package com.engitano.awseffect.messaging

trait MessageAttributeValue

object MessageAttributeValue {
  case class String(value: java.lang.String)           extends MessageAttributeValue
  case class Number(value: java.lang.String)           extends MessageAttributeValue
  case class StringList(value: List[java.lang.String]) extends MessageAttributeValue
  case class NumberList(value: List[java.lang.String]) extends MessageAttributeValue
  case class Binary(bytes: Array[Byte])                extends MessageAttributeValue
}