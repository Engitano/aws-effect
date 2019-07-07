package com.engitano.awseffect.sqs

class instances {
  object all extends AllInstances
  object withReceipt extends WithReceiptInstances
}

trait AllInstances extends WithReceiptInstances
