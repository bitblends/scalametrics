import java.time.LocalDateTime

lazy val root = (project in file("."))
  .enablePlugins(BuildInfoPlugin, GitVersioning, GitBranchPrompt)
  .settings(
    scalaVersion := "2.13.17",
    crossScalaVersions := Seq("2.12.20", "2.13.17", "3.3.6"),
    name := "scalametrics",
    description := "ScalaMetrics: Comprehensive code metrics and analysis library for Scala",
    organization := "com.bitblends",
    organizationName := "BitBlends",
    organizationHomepage := Some(url("https://bitblends.com")),
    startYear := Some(2025),
    licenses += ("MIT", url("https://opensource.org/licenses/MIT")),
    homepage := Some(url("https://github.com/bitblends/scalametrix")),
    headerLicense := Some(
      HeaderLicense.Custom(
        s"""|SPDX-FileCopyrightText: ${
                                        startYear.value.getOrElse(LocalDateTime.now().getYear)
                                      } Benjamin Saff and contributors
            |SPDX-License-Identifier: MIT
            |""".stripMargin
      )
    ),
    developers := Authors.authors,
    Compile / compile / scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 12)) =>
          Seq.empty
        case Some((2, 13)) =>
          Seq(
            "-opt:l:method",
            "-opt:l:inline"
          )
        case _ =>
          Seq.empty
      }
    },
    Compile / packageDoc / publishArtifact := true,
    Compile / packageSrc / publishArtifact := true,
    Compile / publishArtifact := true,
    Test / parallelExecution := false,
    Test / fork := true,
    Test / packageBin / publishArtifact := false,
    Test / packageDoc / publishArtifact := false,
    Test / packageSrc / publishArtifact := false,
    pomIncludeRepository := { _ => false },
    crossVersion := CrossVersion.binary,
    apiURL := Some(url("https://bitblends.github.io/scalametrics/api-reference/2.13/index.html")),
    buildInfoPackage := "com.bitblends.scalametrics",
    buildInfoOptions += BuildInfoOption.BuildTime,
    buildInfoKeys := Seq[BuildInfoKey](
      name,
      version,
      BuildInfoKey.action("versionInfo")(
        s"ScalaMetrics ${version.value} - Comprehensive code metrics and analysis library for Scala"
      ),
      scalaVersion,
      BuildInfoKey.action("gitDate")(git.formattedDateVersion.value),
      BuildInfoKey.action("gitSHA")(git.formattedShaVersion.value),
      BuildInfoKey.action("headCommit")(git.gitHeadCommit.value),
      BuildInfoKey.action("currentTags")(git.gitCurrentTags.value),
      BuildInfoKey.action("currentBranch")(git.gitCurrentBranch.value)
    ),
    coverageEnabled := true,
    pomIncludeRepository := { _ => false },
    javacOptions ++= Seq("--release", "17"),
    scalacOptions ++= Seq(
      "-deprecation",
      "-encoding",
      "UTF-8",
      "-feature",
      "-unchecked"
    ),
    scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 12)) =>
        Seq(
          "-Xlint",
          "-Ywarn-unused",
          "-Ywarn-dead-code",
          "-Ywarn-numeric-widen",
          "-Ywarn-value-discard"
        )
      case Some((2, 13)) =>
        Seq(
          "-Xlint",
          "-Xsource:3",
          "-Ywarn-dead-code",
          "-Ywarn-numeric-widen",
          "-Ywarn-value-discard"
        )
      case Some((3, _)) =>
        Seq(
          "-Ykind-projector:underscores",
          "-explain",
          "-Wunused:all"
        )
      case _ => Nil
    }),
    libraryDependencies ++= Seq(
      "org.scalameta" %% "scalameta" % "4.14.1",
      "org.scala-lang.modules" %% "scala-collection-compat" % "2.14.0",
      "org.scala-lang.modules" %% "scala-java8-compat" % "1.0.2",
      "org.scalatest" %% "scalatest" % "3.2.19" % Test
    )
  )
