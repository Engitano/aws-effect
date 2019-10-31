package com.engitano.awseffect

import com.amazonaws.services.lambda.runtime.Context
import java.io.OutputStream
import java.io.InputStream

package object lambda {
    import cats.effect.Blocker
    type LambdaHandler[F[_]] = (InputStream, OutputStream, Context) => F[Unit]
}
