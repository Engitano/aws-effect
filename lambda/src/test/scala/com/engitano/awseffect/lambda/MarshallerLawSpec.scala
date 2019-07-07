package com.engitano.awseffect.lambda

import cats.Eq
import cats.laws.discipline._
import cats.laws.discipline.eq._
import org.scalacheck.{Arbitrary, Cogen}
import cats.tests.CatsSuite
import com.engitano.awseffect.marshalling.Marshaller
import org.scalacheck.ScalacheckShapeless._

class MarshallerLawSpec extends CatsSuite {

  import com.engitano.awseffect.instances.marshalling._

  implicit def ec = ExhaustiveCheck.instance(Stream((1 to 50).toList :_*))

  implicit def ecs = ExhaustiveCheck.instance(Stream((1 to 50).map(_.toString).toList :_*))

  implicit def eqMarshaller[A, B](implicit ev: Eq[A => Option[B]]): cats.kernel.Eq[Marshaller[Option, A, B]] =
    Eq.by[Marshaller[Option, A, B], A => Option[B]](_.marshall)

  implicit def catsLawsArbitraryForMarshaller[F[_], A: Arbitrary: Cogen, B](implicit F: Arbitrary[F[B]]): Arbitrary[Marshaller[F, A, B]] =
    Arbitrary(Arbitrary.arbitrary[A => F[B]].map(a => new Marshaller[F, A, B] {
      override def marshall(t: A): F[B] = a(t)
    }))

  checkAll("Marshaller.FunctorLaws", MonadTests[Marshaller[Option, Int, ?]].functor[Int, Int, String])
  checkAll("Marshaller.FlatMapLaws", MonadTests[Marshaller[Option, Int, ?]].flatMap[Int, Int,String])
  checkAll("Marshaller.ApplicativeLaws", MonadTests[Marshaller[Option, Int, ?]].applicative[Int, Int,String])
  checkAll("Marshaller.ContravariantLaws", ContravariantTests[Marshaller[Option, ?, Int]].contravariant[Int, Int,String])
  checkAll("Marshaller.ProfunctorLaws", ProfunctorTests[Marshaller[Option, ?, ?]].profunctor[Int, Int,String,Int, Int,String])
}
