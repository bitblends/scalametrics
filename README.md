<h1>
  <img src="gh-pages/docs/images/logo.png" alt="Scala Logo" width="32" />
  ScalaMetrics
</h1>

> The most comprehensive code metrics and code analysis library for Scala

<div style="display: flex; gap: 5pt; align-items: baseline; justify-content: left;">

[![Release](https://img.shields.io/github/v/release/bitblends/scalametrics?sort=semver&style=flat&color=darkgreen&labelColor=2f363d&logo=github&logoColor=white)](https://github.com/bitblends/scalametrics/releases/latest)
[![Maven Central](https://img.shields.io/maven-central/v/com.bitblends/scalametrics_2.13?style=flat&color=darkgreen&labelColor=2f363d&logo=Sonatype&logoColor=white)](https://central.sonatype.com/artifact/com.bitblends/scalametrics_2.13)
[![CI](https://img.shields.io/github/actions/workflow/status/bitblends/scalametrics/ci.yml?branch=main&style=flat&color=green&labelColor=2f363d)](https://github.com/bitblends/scalametrics/actions/workflows/ci.yml?query=branch%3Amain)
[![Scala versions](https://img.shields.io/badge/Scala-2.12%20%7C%202.13%20%7C%203-ff4757?style=flat&color=red&labelColor=2f363d&logo=scala&logoColor=white)](https://www.scala-lang.org)
[![License](https://img.shields.io/badge/License-MIT-3?style=flat&color=yellow&labelColor=2f363d&logoColor=white)](LICENSE)

</div>
<p>
ScalaMetrics is a powerful static analysis library for Scala projects. It provides comprehensive analysis at multiple granularity levels: project, package, file, method, and member.
</p>


## Features

- **Multi-Level Analysis**: Extract metrics at project, package, file, method, and member levels
- **Comprehensive Metrics**:
  - Cyclomatic complexity
  - Nesting depth
  - Expression branch density
  - Pattern matching
  - Lines of code
  - Documentation coverage
  - Parameter and arity (implicit, using, default, varargs)
  - Return type explicitness
  - Inline and implicit usage
- **Raw and Aggregated Metrics**: Provides both raw and aggregated metrics for detailed insights
- **Multiple Dialect Support**: Supports Scala 2.12, 2.13, and 3.3
- **Immutable Design**: Functional pipeline architecture with immutable data flow
- **ScalaMeta-Powered**: Leverages ScalaMeta for accurate AST parsing and traversal
- **Automatic Dialect Detection**: Automatically detects Scala dialects for accurate parsing using a combination of
   heuristics and statistical methods

## Installation

Add ScalaMetrics to your `build.sbt`:

```scala
libraryDependencies += "com.bitblends" %% "scalametrics" % "1.1.0"
```

## Documentation
Learn more by visiting [ScalaMetrics Documentation](https://bitblends.github.io/scalametrics/) site.

## Quick Start

ScalaMetrics offers a wide range of code metrics to help you analyze and improve the quality of your Scala codebase.
There are two types of metrics that ScalaMetrics is able to generate: `Aggregated Project Statistics` and `Raw Metrics`.
The aggregated stats is generally recommended for most use cases as it provides a more concise view of the codebase
while still capturing important details through roll-ups.

After calling either `generateProjectStats` or `generateProjectMetrics`, you will receive a case class containing
the metrics. You can then process these metrics based on your needs.

### Raw Metrics

#### Full project analysis (raw metrics)

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

`ProjectStats` case class contains aggregated statistics for the entire project, including file-level, package-level,
and method and member-level statistics (with roll-ups).

``` scala scala
case class ProjectStats(
    metadata: ProjectMetadata,
    projectRollup: ProjectRollup,
    packages: Vector[Package]
)

// ... ProjectRollup, ProjectMetadata (see documentation)

case class Package(packageRollup: PackageRollup, fileStats: Vector[FileStats])

// ... PackageRollup (see documentation)

case class FileStats(
    metadata: FileStatsMetadata,
    fileRollup: FileRollup,
    memberStats: Vector[MemberStats],
    methodStats: Vector[MethodStats]
)
```

## Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## License

Licensed under the MIT License. See [LICENSE](LICENSE) file for details.

## Acknowledgments

Built with:
- [ScalaMeta](https://scalameta.org/) - Scala metaprogramming library
