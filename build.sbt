import sbt.Keys._

import sbtrelease._

import ReleaseStateTransformations._
import ReleaseKeys._

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

publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  }

licenses := Seq("Apache-style" -> url("http://www.apache.org/licenses/LICENSE-2.0.html"))

pomIncludeRepository := { _ => false }

pomExtra := (
    <scm>
      <url>git@github.com:matiwinnetou/play-hysterix.git</url>
      <connection>scm:git:git@github.com:mmatiwinnetou/play-hysterix.git</connection>
    </scm>
  <url>https://github.com/matiwinnetou/play-hysterix</url>
  <developers>
      <developer>
          <id>matiwinnetou</id>
          <name>Mateusz Szczap</name>
          <url>https://github.com/matiwinnetou</url>
        </developer>
  </developers>)
