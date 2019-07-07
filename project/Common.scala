import bintray.BintrayPlugin.autoImport.{bintrayOrganization, bintrayPackageLabels}
import sbt._
import sbt.Keys.{licenses, _}

object Common {

  val scala213               = "2.13.0"
  val scala212               = "2.12.8"
  val supportedScalaVersions = List(scala213, scala212)

  def apply() = Seq(
    scalaVersion := scala213,
    organization := "com.engitano",
    organizationName := "Engitano",
    crossScalaVersions := supportedScalaVersions,
    startYear := Some(2019),
    bintrayOrganization := Some("engitano"),
    licenses += ("MIT", new URL("http://opensource.org/licenses/MIT")),
    addCompilerPlugin("com.olegpy"   %% "better-monadic-for" % "0.3.0"),
    libraryDependencies ++= Seq("org.scala-lang.modules" %% "scala-collection-compat" % "2.0.0")
  )
}
