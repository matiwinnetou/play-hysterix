organization := "pl.matisoft"

name := "play-hysterix"

version := "0.1.play23"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
)

sonatypeSettings

releaseSettings

publishMavenStyle := true

lazy val main = (project in file(".")).enablePlugins(PlayJava)

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
