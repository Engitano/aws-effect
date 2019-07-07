package com.engitano.awseffect.sqs

import cats.{Applicative, Eval, Traverse}
import cats.syntax.functor._

case class WithReceipt[A](wrapped: A, receiptHandle: String)

trait WithReceiptInstances {
  implicit def catsDataTraverseForWithReceipt = new Traverse[WithReceipt] {
    override def traverse[G[_], A, B](fa: WithReceipt[A])(f: A => G[B])(implicit evidence$1: Applicative[G]): G[WithReceipt[B]] =
      f(fa.wrapped).map(b => WithReceipt(b, fa.receiptHandle))

    override def foldLeft[A, B](fa: WithReceipt[A], b: B)(f: (B, A) => B): B =
      f(b, fa.wrapped)

    override def foldRight[A, B](fa: WithReceipt[A], lb: Eval[B])(f: (A, Eval[B]) => Eval[B]): Eval[B] =
      f(fa.wrapped, lb)
  }
}
