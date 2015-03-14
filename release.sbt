import sbtrelease._
import ReleaseStateTransformations._
import ReleasePlugin._
import ReleaseKeys._

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
  sonatypeReleaseReleaseStep,
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
  extracted.runAggregated(PgpKeys.publishSigned in Global in ref, st)
}

lazy val sonatypeReleaseReleaseStep = releaseTask(sonatypeReleaseAll in ThisProject)
