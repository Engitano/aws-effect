package com.engitano.awseffect.ses
import org.scalatest.WordSpec
import org.scalatest.Matchers

import scala.collection.JavaConverters._

class SendEmailRequestSpec extends WordSpec with Matchers {
  "SendEmailRequest" should {
    "#toSes" in {
      val sut = SendEmailRequest(
          "john@johnsmith.com", 
          Destination(List("me@me.com"), List(), List()),
          EmailMessage(EmailContent("Hell There"), EmailBody.text(EmailContent("Dass whassup"))),
          returnPath = Some("testers@returns.com"),
          messageTags = Map("Hello" -> "World"),
          sourceArn = Some("aws:arn:sommat/email")
          )
      val res = sut.toSes
      res.source() shouldBe "john@johnsmith.com"
      res.tags().asScala.head.value() shouldBe "World"
      res.sourceArn() shouldBe "aws:arn:sommat/email"
      res.returnPath() shouldBe "testers@returns.com"
      res.returnPathArn() shouldBe null
    }
  }
}
