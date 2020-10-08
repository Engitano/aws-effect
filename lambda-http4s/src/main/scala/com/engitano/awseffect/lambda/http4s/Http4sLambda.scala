package com.engitano.awseffect.lambda.http4s

import com.engitano.awseffect.lambda.catsio.IOLambda
import cats.effect.{Blocker, IO}
import org.http4s.HttpApp
import io.chrisdavenport.vault.Key

trait Http4sLambda extends IOLambda {

    case class Http4sApp(service: HttpApp[IO], vaultKey: Key[LambdaRequestParams])

    def lambdaHandler(blocker: Blocker): IO[Http4sApp]

    override def handler(blocker: Blocker): IO[com.engitano.awseffect.lambda.LambdaHandler[IO]] = 
        lambdaHandler(blocker).map(h => Http4sHandler(blocker)(h.service, h.vaultKey))
}