package com.engitano.awseffect.marshalling

import cats.{Applicative, Contravariant, Functor, Monad}
import cats.arrow.Profunctor
import cats.syntax.applicative._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.option._

case class MarshallingException(reason: String, cause: Option[Throwable] = None) extends Exception(reason) {
  cause.foreach(this.initCause)
}

object Marshaller {
  def apply[F[_], From, To](implicit m: Marshaller[F, From, To]) = m

  case class ConstPartiallyApplied[F[_], A](dummy: Boolean = false) extends AnyVal {
    def apply[B](b: B)(implicit F: Applicative[F]): Marshaller[F, A, B] = new Marshaller[F, A, B] {
      override def marshall(t: A): F[B] = b.pure
    }
  }
  def pure[F[_], A] = ConstPartiallyApplied[F, A]()

  implicit def optionMarshaller[F[_]: Functor, A, B](implicit marshaller: Marshaller[F, A, B]): Marshaller[F, A, Option[B]] =
    marshaller.map(_.some)

  implicit def idMarshaller[F[_]: Applicative, A]: Marshaller[F, A, A] = new Marshaller[F, A, A] {
    override def marshall(t: A): F[A] = Applicative[F].pure(t)
  }
}

trait Marshaller[F[_], -From, To] {
  self =>
  def marshall(t: From): F[To]

  def map[B](f: To => B)(implicit F: Functor[F]): Marshaller[F, From, B] =
    new Marshaller[F, From, B] {
      override def marshall(t: From): F[B] = self.marshall(t).map(f)
    }

  def contramap[A](f: A => From): Marshaller[F, A, To] =
    new Marshaller[F, A, To] {
      override def marshall(t: A): F[To] = self.marshall(f(t))
    }
}

trait MarshallInstances {

  implicit def catsDataContravariatFromMarshaller[F[_], T]: Contravariant[Marshaller[F, ?, T]] =
    new Contravariant[Marshaller[F, ?, T]] {
      override def contramap[A, B](fa: Marshaller[F, A, T])(
        f: B => A
      ): Marshaller[F, B, T] = fa.contramap(f)
    }

  implicit def catsArrowProfunctorForMarshaller[F[_]: Functor]: Profunctor[Marshaller[F, ?, ?]] = new Profunctor[Marshaller[F, ?, ?]] {
    override def dimap[A, B, C, D](fab: Marshaller[F, A, B])(f: C => A)(g: B => D): Marshaller[F, C, D] =
      fab.map(g).contramap(f)
  }

  implicit def catsDataFlatMapForMarshaller[F[_]: Monad, T] =
    new Monad[Marshaller[F, T, ?]]  {
      override def pure[A](x: A): Marshaller[F, T, A] = Marshaller.pure[F, T](x)


      override def flatMap[A, B](fa: Marshaller[F, T, A])(f: A => Marshaller[F, T, B]): Marshaller[F, T, B] = new Marshaller[F, T, B] {
        override def marshall(t: T): F[B] = fa.marshall(t).flatMap(a => f(a).marshall(t))
      }

      override def tailRecM[A, B](a: A)(f: A => Marshaller[F, T, Either[A, B]]): Marshaller[F, T, B] = flatMap(f(a)) {
        case Left(aa) => tailRecM(aa)(f)
        case Right(b) => pure(b)
      }
    }
}