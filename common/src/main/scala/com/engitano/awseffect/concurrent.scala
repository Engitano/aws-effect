package com.engitano.awseffect

import java.util.concurrent.CompletionStage

import cats.effect.Async

object concurrent {

  implicit def toPimpedFuture[A](cs: CompletionStage[A]) = new PimpedFutures[A](cs)

  class PimpedFutures[A](cs: CompletionStage[A]) {
    def to[F[_]: Async] = Async[F].async[A] { cb =>
      cs whenComplete { (a: A, t) =>
        Option(t).fold(cb(Right(a)))(e => cb(Left(e)))
      }
    }
  }
}
