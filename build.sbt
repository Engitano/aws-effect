resolvers += Resolver.sonatypeRepo("releases")
val kindProjector = "org.typelevel" % "kind-projector" % "0.11.0" cross CrossVersion.full

val majorVersion = SettingKey[String]("major version")
val minorVersion = SettingKey[String]("minor version")
val patchVersion = SettingKey[Option[String]]("patch version")

Global / majorVersion := "0"
Global / minorVersion := "1"
Global / patchVersion := Some("0")

val writeVersion = taskKey[Unit]("Writes the version to version.txt")
writeVersion := {
  IO.write(baseDirectory.value / "version.txt", (root / version).value)
}

test in publish := {}

lazy val root = (project in file("."))
  .aggregate(`aws-effect-common`, `aws-effect-sqs`, `aws-effect-sns`, `aws-effect-ses`, `aws-effect-lambda`, `aws-effect-lambda-http4s`)
  .settings(Common())
  .settings(
    version := s"${majorVersion.value}.${minorVersion.value}${patchVersion.value.fold("")(p => s".$p")}",
    skip in publish := true
  )

lazy val `aws-effect-common` = (project in file("common"))
  .settings(Common())
  .settings(bintrayPackageLabels ++= Seq("aws"))
  .settings(
    version := s"${majorVersion.value}.${minorVersion.value}${patchVersion.value.fold("")(p => s".$p")}",
    libraryDependencies ++= Seq(
      Dependencies.catsEffect,
      Dependencies.circe,
      Dependencies.circeAuto,
      Dependencies.catsTestkit         % Test,
      Dependencies.scalacheckShapeless % Test
    )
  )
  .settings(addCompilerPlugin(kindProjector))

lazy val `aws-effect-sqs` = (project in file("sqs"))
  .settings(Common())
  .settings(bintrayPackageLabels ++= Seq("aws", "sqs"))
  .settings(
    version := s"${majorVersion.value}.${minorVersion.value}${patchVersion.value.fold("")(p => s".$p")}",
    libraryDependencies ++= Seq(
      Dependencies.catsEffect,
      Dependencies.fs2,
      Dependencies.sqs
    )
  )
  .settings(addCompilerPlugin(kindProjector))
  .dependsOn(`aws-effect-common`)

lazy val `aws-effect-sns` = (project in file("sns"))
  .settings(Common())
  .settings(bintrayPackageLabels ++= Seq("aws", "sns"))
  .settings(
    version := s"${majorVersion.value}.${minorVersion.value}${patchVersion.value.fold("")(p => s".$p")}",
    libraryDependencies ++= Seq(
      Dependencies.catsEffect,
      Dependencies.fs2,
      Dependencies.sns
    )
  )
  .settings(addCompilerPlugin(kindProjector))
  .dependsOn(`aws-effect-common`)

lazy val `aws-effect-ses` = (project in file("ses"))
  .settings(Common())
  .settings(bintrayPackageLabels ++= Seq("aws", "ses"))
  .settings(
    version := s"${majorVersion.value}.${minorVersion.value}${patchVersion.value.fold("")(p => s".$p")}",
    libraryDependencies ++= Seq(
      Dependencies.catsEffect,
      Dependencies.ses,
      Dependencies.scalatest % Test
    )
  )
  .settings(addCompilerPlugin(kindProjector))
  .dependsOn(`aws-effect-common`)

lazy val `aws-effect-lambda` = (project in file("lambda"))
  .settings(Common())
  .settings(bintrayPackageLabels ++= Seq("aws", "lambda"))
  .settings(
    version := s"${majorVersion.value}.${minorVersion.value}${patchVersion.value.fold("")(p => s".$p")}",
    libraryDependencies ++= Seq(
      Dependencies.catsEffect,
      Dependencies.circe,
      Dependencies.circeAuto,
      Dependencies.circeFs2,
      Dependencies.fs2io,
      Dependencies.lambda,
      Dependencies.log4cats,
      Dependencies.slf4j,
      Dependencies.scalatest % Test,
      Dependencies.scalamock % Test
    )
  )
  .settings(addCompilerPlugin(kindProjector))
  .dependsOn(`aws-effect-common`)

lazy val `aws-effect-lambda-http4s` = (project in file("lambda-http4s"))
  .settings(Common())
  .settings(bintrayPackageLabels ++= Seq("aws", "lambda", "http4s"))
  .settings(
    version := s"${majorVersion.value}.${minorVersion.value}${patchVersion.value.fold("")(p => s".$p")}",
    libraryDependencies ++= Seq(
      Dependencies.http4sCore,
      Dependencies.circe       % Test,
      Dependencies.circeAuto   % Test,
      Dependencies.http4sDsl   % Test,
      Dependencies.http4sCirce % Test,
      Dependencies.scalatest   % Test,
      Dependencies.scalamock   % Test
    )
  )
  .settings(addCompilerPlugin(kindProjector))
  .dependsOn(`aws-effect-lambda`)
