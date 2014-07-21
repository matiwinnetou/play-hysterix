import sbtrelease._
import ReleaseStateTransformations._
import ReleasePlugin._
import ReleaseKeys._

import com.typesafe.sbt.SbtPgp.PgpKeys.publishSigned
import xerial.sbt.Sonatype.SonatypeKeys.sonatypeReleaseAll

releaseSettings

sonatypeSettings

releaseProcess := Seq(
  checkSnapshotDependencies,
  inquireVersions,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  publishSignedArtifacts,
  sonatypeRelease,
  setNextVersion,
  commitNextVersion,
  pushChanges
)

lazy val publishSignedArtifacts = ReleaseStep(
  action = publishSignedArtifactsAction,
  enableCrossBuild = true)

lazy val publishSignedArtifactsAction = { st: State =>
  val extracted = Project.extract(st)
  val ref = extracted.get(thisProjectRef)
  extracted.runAggregated(publishSigned in Global in ref, st)
}

lazy val sonatypeRelease = releaseTask(sonatypeReleaseAll in ThisProject)
