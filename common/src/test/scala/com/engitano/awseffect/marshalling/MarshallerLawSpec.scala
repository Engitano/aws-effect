package com.engitano.awseffect.marshalling

import cats.{ApplicativeError, Eq}
import cats.laws.discipline._
import cats.laws.discipline.eq._
import cats.laws.discipline.arbitrary._
import cats.instances.either._
import cats.syntax.either._
import org.scalacheck.{Arbitrary, Cogen, Gen}
import cats.tests.CatsSuite
import org.scalacheck.ScalacheckShapeless._

class MarshallerLawSpec extends CatsSuite {

  type OrError[A] = Either[Throwable, A]

  import com.engitano.awseffect.instances.marshalling._

  implicit def ec = ExhaustiveCheck.instance(Stream((1 to 50).toList :_*))

  implicit def et: Eq[Throwable] = Eq.fromUniversalEquals

  implicit def ecs = ExhaustiveCheck.instance(Stream((1 to 50).map(_.toString).toList :_*))

  implicit def eqMarshallerE[A, B](implicit ev: Eq[A => OrError[B]]): cats.kernel.Eq[Marshaller[OrError, A, B]] =
    Eq.by[Marshaller[OrError, A, B], A => OrError[B]](_.marshall)

  implicit def catsLawsArbitraryForThrowable: Arbitrary[Throwable] = Arbitrary(Gen.const(MarshallingException("Empty Marshaller always fails")))

  implicit def catsLawsArbitraryForMarshaller[F[_], A: Arbitrary: Cogen, B](implicit AF: Arbitrary[A => F[B]]): Arbitrary[Marshaller[F, A, B]] =
    Arbitrary(AF.arbitrary.map(a => new Marshaller[F, A, B] {
      override def marshall(t: A): F[B] = a(t)
    }))

  checkAll("Marshaller.MonadLaws", MonadTests[Marshaller[OrError, Int, ?]].monad[Int, Int,String])
  checkAll("Marshaller.AlternativeLaws", AlternativeTests[Marshaller[OrError, Int, ?]].alternative[Int, Int,Int])
  checkAll("Marshaller.ContravariantLaws", ContravariantTests[Marshaller[OrError, ?, Int]].contravariant[Int, Int,String])
  checkAll("Marshaller.ProfunctorLaws", ProfunctorTests[Marshaller[OrError, ?, ?]].profunctor[Int, Int,String,Int, Int,String])
}
