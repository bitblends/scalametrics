---
title: Getting Started
description: Learn how to install and use ScalaMetrics for comprehensive code metrics and analysis in your Scala projects.
keywords: [ScalaMetrics, getting started, installation, code metrics, Scala, static analysis]
layout: doc
hide: [title]
---

# Getting Started

## Installation

Add `scalametrics` to your `build.sbt`:

``` scala
libraryDependencies += "com.bitblends" %% "scalametrics" % "{{ project_version }}"
```

The library is cross-compiled for Scala 2.12.20, 2.13.17, and 3.3.6.

## Quick Start

ScalaMetrics offers a wide range of code metrics to help you analyze and improve the quality of your Scala codebase.
There are two types of metrics that ScalaMetrics is able to generate: `Aggregated Project Statistics` and `Raw Metrics`.
The aggregated stats is generally recommended for most use cases as it provides a more concise view of the codebase
while still capturing important details through roll-ups.

### Aggregated Project Statistics

For a complete project analysis you can provide a minimal `ProjectInfo` case class, source files, and the base directory
of the project to receive a `ProjectStats` case class.
This case class contains aggregated statistics for the entire project, including file-level, package-level, and method
and member-level statistics (with roll-ups).

``` scala
import java.io.File
import com.bitblends.scalametrics.metrics.model.{ProjectInfo, ProjectStats}
import com.bitblends.scalametrics.utils.Id
import com.bitblends.scalametrics.ScalaMetrics

// Minimal ProjectInfo setup
val projectInfo = ProjectInfo(
  projectId = Id.of("MyScalaProject"), // or any unique identifier of your choice
  name = "MyScalaProject",
  version = "1.0.0",
  scalaVersion = "2.13.17"
)

// Specify one or more source directories
val sourceDirectories = Seq(
  new File("src/main/scala"),
  new File("src/main/scala-3")
)

// Gather all Scala source files
val sourceFiles = sourceDirectories.flatMap { dir => (dir ** "*.scala").get }

// Generate ProjectStats
val projectStats: ProjectStats = ScalaMetrics.generateProjectStats(
  files = sourceFiles,
  projectBaseDir = new File("/home/projects", "MyScalaProject"), // root directory of the project, example: /home/projects/MyScalaProject
  projectInfo = projectInfo
)
```

For detailed documentation on all available metrics and their definitions, such as `MethodMetrics`, `MemberMetrics`,
`MemberStats`, etc. please refer to the [API Reference][api-reference].

### Raw Metrics

If you are only interested in the raw metrics with no roll-ups, you can use `generateProjectMetrics` which returns a
`ProjectMetrics` case class containing file-level metrics only. You can then process these raw metrics based on your
specific needs.

#### Full project analysis

``` scala
import java.io.File
import com.bitblends.scalametrics.metrics.model.{ProjectInfo, ProjectMetrics}
import com.bitblends.scalametrics.utils.Id
import com.bitblends.scalametrics.ScalaMetrics

// Minimal ProjectInfo setup
val projectInfo = ProjectInfo(
  projectId = Id.of("MyScalaProject"), // or any unique identifier of your choice
  name = "MyScalaProject",
  version = "1.0.0",
  scalaVersion = "2.13.17"
)

// Specify one or more source directories
val sourceDirectories = Seq(
  new File("src/main/scala"),
  new File("src/main/scala-3")
)

// Gather all Scala source files
val sourceFiles = sourceDirectories.flatMap { dir => (dir ** "*.scala").get }

// Generate metrics
val projectMetrics: ProjectMetrics = ScalaMetrics.generateProjectMetrics(
  files = sourceFiles,
  projectBaseDir = new File("/home/projects", "MyScalaProject"), // root directory of the project, example: /home/projects/MyScalaProject
  projectInfo = projectInfo
)
```

#### Analyze a single file (with automatic dialect detection)

You can analyze a single Scala file to get its metrics using `generateFileMetrics`. The dialect will be automatically
detected based on the file content.

``` scala
import java.io.File
import com.bitblends.scalametrics.ScalaMetrics
import com.bitblends.scalametrics.metrics.model.FileMetrics

val file = new File("src/main/scala/example/MyClass.scala")
val result: Option[FileMetrics] = ScalaMetrics.generateFileMetrics(file)
```

#### Analyze a single file with a dialect

You can also specify a dialect explicitly if you want to override the automatic detection.

``` scala
import java.io.File
import com.bitblends.scalametrics.ScalaMetrics
import com.bitblends.scalametrics.metrics.model.FileMetrics
import scala.meta.Dialect

val file = new File("src/main/scala/example/MyClass.scala")
val result: Option[FileMetrics] = ScalaMetrics.generateFileMetrics(file)

// You can provide a dialect if you want to override the automatic detection
import org.scalameta.dialects.{Scala213, Scala212, Scala3}

val dialect: Dialect = Scala213 // or Scala212, Scala3
val result: Option[FileMetrics] = ScalaMetrics.generateFileMetrics(file, dialect = Some(dialect))
```

---

`ProjectMetrics` case class contains raw metrics for the entire project, including file-level, member-level, and
method-level metrics (no package or roll-ups). `FileMetrics` case class contains the metrics for a single file
along with its methods and members.

``` scala
case class ProjectMetrics(
    projectInfo: ProjectInfo,
    fileMetrics: Vector[FileMetrics]
)

case class FileMetrics(
    metadata: FileMetadata,
    methodMetrics: Vector[MethodMetrics] = Vector.empty,
    memberMetrics: Vector[MemberMetrics] = Vector.empty
)
```

