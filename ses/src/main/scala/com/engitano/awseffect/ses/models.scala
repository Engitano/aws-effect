package com.engitano.awseffect.ses

import cats.data.Ior
import cats.data.State
import software.amazon.awssdk.services.ses.model.{
  SendEmailRequest => SesSendEmailRequest,
  Message => SesMessage,
  Content => SesContent,
  Body => SesBody,
  Destination => SesDestination,
  MessageTag => SesMessageTag
}

import scala.collection.JavaConverters._

case class EmailContent(data: String, charset: Option[String] = None) {
  def toSes = SesContent.builder().data(data).charset(charset.getOrElse("UTF-8")).build()
}
case class EmailBody private (content: Ior[EmailContent, EmailContent]) {
  def text = content.left
  def html = content.right
  def toSes =
    content.fold(
      txt => SesBody.builder().text(txt.toSes).build(),
      html => SesBody.builder().html(html.toSes).build(),
      (txt, html) => SesBody.builder().text(txt.toSes).html(html.toSes).build()
    )
}

object EmailBody {
  def text(txt: EmailContent) = EmailBody(Ior.left(txt))
  def html(html: EmailContent, text: Option[EmailContent]) = text match {
    case Some(t) => EmailBody(Ior.both(t, html))
    case _       => EmailBody(Ior.right(html))
  }
}

case class EmailMessage(subject: EmailContent, body: EmailBody) {
  def toSes: SesMessage = SesMessage.builder().subject(subject.toSes).body(body.toSes).build()
}

case class Destination(to: List[String], cc: List[String], bcc: List[String]) {
  def toSes: SesDestination = SesDestination.builder().toAddresses(to.asJava).ccAddresses(cc.asJava).bccAddresses(bcc.asJava).build()
}

case class SendEmailRequest(
    from: String,
    to: Destination,
    message: EmailMessage,
    replyTo: List[String] = List(),
    returnPath: Option[String] = None,
    sourceArn: Option[String] = None,
    returnPathArn: Option[String] = None,
    messageTags: Map[String, String] = Map(),
    configurationSetName: Option[String] = None
) {
  private def optionalBuilder[T](opt: Option[T])(
      op: SesSendEmailRequest.Builder => T => SesSendEmailRequest.Builder
  ): State[SesSendEmailRequest.Builder, SesSendEmailRequest.Builder] =
    State(b => (opt.fold(b)(op(b)), b))

  private def setupOpts: State[SesSendEmailRequest.Builder, SesSendEmailRequest.Builder] =
    for {
      _ <- optionalBuilder(returnPath)(_.returnPath)
      _ <- optionalBuilder(sourceArn)(_.sourceArn)
      _ <- optionalBuilder(returnPathArn)(_.returnPathArn)
      b <- optionalBuilder(configurationSetName)(_.configurationSetName)
    } yield b

  def toSes: SesSendEmailRequest =
    setupOpts
      .runS(
        SesSendEmailRequest
          .builder()
      )
      .value
      .source(from)
      .destination(to.toSes)
      .message(message.toSes)
      .replyToAddresses(replyTo.asJava)
      .tags(messageTags.map(p => SesMessageTag.builder().name(p._1).value(p._2).build()).toList.asJava)
      .build()

}
