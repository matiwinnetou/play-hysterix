import sbt.Keys._

organization := "pl.matisoft"

name := "play-hysterix"

scalaVersion := "2.10.6"

crossScalaVersions := Seq("2.10.6", "2.11.7")

publishMavenStyle := true

val commonSettings = Seq(
    javacOptions ++= Seq("-target", "1.8", "-source", "1.8"),
    parallelExecution := true
)

lazy val main = (project in file("."))
                .settings(commonSettings:_*)
                .enablePlugins(PlayJava)

libraryDependencies += "io.dropwizard.metrics" % "metrics-core" % "3.1.2"

libraryDependencies += "org.mockito" % "mockito-all" % "1.10.19" % Test

licenses := Seq("Apache-style" -> url("http://www.apache.org/licenses/LICENSE-2.0.html"))