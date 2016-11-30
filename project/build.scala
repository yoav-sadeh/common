import sbt._
import Keys._

object MacroBuild extends Build {
  name := "common"

  organization := "com.hamlazot"

  version := "0.1.0-SNAPSHOT"

  lazy val main = Project("common", file(".")) dependsOn(macroSub) settings {

    libraryDependencies ++= Seq(
      "org.json4s" %% "json4s-jackson" % "3.3.0",
      "org.json4s" %% "json4s-core" % "3.3.0",
      "org.json4s" %% "json4s-ext" % "3.3.0",
      "org.specs2" %% "specs2-core" % "3.7" % "test"
    )
  }

  lazy val macroSub = Project("macro", file("macro")) settings(
    libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-compiler" % _)
  )
}