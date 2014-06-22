import sbt.Keys._
import scala.Some

organization := "pl.matisoft"

name := "play-hysterix"

version := "0.2.4-SNAPSHOT"

scalaVersion := "2.10.4"

sonatypeSettings
  
releaseSettings
  
publishMavenStyle := true

val commonSettings = Seq(
    javacOptions ++= Seq("-target", "1.8", "-source", "1.8"),
    parallelExecution := true
)

lazy val main = (project in file(".")).settings(commonSettings:_*).enablePlugins(PlayJava)

libraryDependencies += "com.yammer.metrics" % "metrics-core" % "3.0.0-BETA1"

libraryDependencies += "org.mockito" % "mockito-all" % "1.9.5" % Test

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
      <url>git@github.com:mati1979/play-hysterix.git</url>
      <connection>scm:git:git@github.com:mati1979/play-hysterix.git</connection>
    </scm>
  <url>https://github.com/mati1979/play-hysterix</url>
  <developers>
      <developer>
          <id>matiwinnetou</id>
          <name>Mateusz Szczap</name>
          <url>https://github.com/mati1979</url>
        </developer>
    </developers>)
