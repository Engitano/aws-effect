package com.engitano.awseffect

object syntax {
  object builder {

    implicit class PimpedBuilder[A](a: A) {
      def append[B](b: B)(f: A => B => A): A = f(a)(b)

      def appendOpt[B](b: Option[B])(f: A => B => A): A = b.fold(a)(f(a))
    }

  }
}
