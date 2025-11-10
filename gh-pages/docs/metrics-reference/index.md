---
title: Metrics Reference
description: Comprehensive reference for all code metrics provided by ScalaMetrics, including definitions, calculations, and usage guidelines.
keywords: [ code metrics, metrics reference, ScalaMetrics, static analysis, code quality, software metrics, raw metrics, aggregated metrics, aggregated stats, stats, Scala ]
layout: doc
toc: true
---

# Metrics Reference

ScalaMetrics offers a wide range of code metrics to help you analyze and improve the quality of your Scala codebase.
There are two types of metrics that ScalaMetrics is able to generate: [Raw Project Metrics][raw-project-metrics]
and [Aggregated Project Stats][aggregated-project-stats]. Raw metrics provide detailed measurements at the project,
file, method, and member levels without any aggregation or roll-ups. Aggregated stats provide summarized statistics at
the project, package, and file levels, including roll-ups for methods and members.

Depending on your analysis needs, you can choose to work with either raw metrics for detailed insights or aggregated
stats for a high-level overview. The aggregated stats is generally recommended for most use cases as it provides a
more concise view of the codebase while still capturing important details through roll-ups. While raw metrics are useful
for custom analyses and reporting based on specific requirements. This section provides a detailed reference for each
metric.

<!-- @formatter:off -->
!!! tip
    If you are just getting started, consider using the aggregated stats as described in the [Getting Started Guide][aggregated-project-statistics].
<!-- @formatter:on -->

## Raw Project Metrics

When collecting raw metrics, ScalaMetrics provides detailed statistics at the project, file, method and member level.
Remember that raw metrics do not include any aggregation or roll-ups; they represent the direct measurements extracted
from the source code.
Recall the steps in [Getting Started][raw-metrics], you can generate raw metrics using the`generateProjectMetrics`
method which returns a `ProjectMetrics` case class. This case class contains all project metrics, that includes file,
method and member-level metrics.

### ProjectMetrics Case Class

``` scala title="ProjectMetrics.scala"
case class ProjectMetrics(
    projectInfo: ProjectInfo,
    fileMetrics: Vector[FileMetricsResult]
)
```

<!-- @formatter:off -->
!!! tip
    `ProjectMetrics` is the foundation case class for raw metrics analysis in ScalaMetrics. It encapsulates the overall 
    project information. This is what you need for raw metrics analysis and custom reporting and aggregation for your
    whole project.
<!-- @formatter:on -->

### FileMetricsResult Case Class

The `FileMetricsResult` case class encapsulates the metrics for a single scala source file, along with its associated
method and member metrics.

``` scala title="FileMetricsResult.scala"
case class FileMetricsResult(
    fileMetrics: FileMetrics,
    methodMetrics: Vector[MethodMetrics] = Vector.empty,
    memberMetrics: Vector[MemberMetrics] = Vector.empty
)
```

### FileMetrics Case Class

The `FileMetrics` case class contains various metrics related to the file itself, while the `MethodMetrics` and
`MemberMetrics` case classes provide detailed metrics for methods and members defined within that file.

``` scala title="FileMetrics.scala" 
case class FileMetrics(
    projectId: Option[String],
    fileId: String,
    file: File,
    packageName: String,
    linesOfCode: Int,
    fileSizeBytes: Long
)
```

<style>
/* Disable word wrap for the first column of all tables */
.md-typeset table td:nth-of-type(1),
.md-typeset table th:nth-of-type(1) {
  white-space: nowrap;
}

/* Disable word wrap for the second column of all tables */
.md-typeset table td:nth-of-type(2),
.md-typeset table th:nth-of-type(2) {
  white-space: nowrap;
}
</style>

### FileMetrics Parameters

The table below summarizes the parameters of the `FileMetrics` case class and what each one represents.

| Parameter       | Type             | Description                                                                                                                                                             |
|-----------------|------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `projectId`     | `Option[String]` | An optional identifier for the project to which the file belongs. It will be `None` when the metrics are generated for a single file or multiple files with no project. |
| `fileId`        | `String`         | The unique identifier for the file within the project. Automatically generated.                                                                                         |
| `file`          | `java.io.File`   | The file instance representing the file on the filesystem.                                                                                                              |
| `packageName`   | `String`         | The package name where the file resides within the project. If no package declaration is found the value will be `<default>`                                            |
| `linesOfCode`   | `Int`            | The total number of lines of code present in the file. Ignores blank lines with tabs or whitespaces and line-breaks.                                                    |
| `fileSizeBytes` | `Long`           | The size of the file in bytes.                                                                                                                                          |

### MemberMetrics Parameters

The table below summarizes the parameters of the `MemberMetrics` case class and what each one represents.

| Parameter               | Type             | Description                                                                                                   |
|-------------------------|------------------|---------------------------------------------------------------------------------------------------------------|
| `fileId`                | `String`         | Unique identifier for the file where the symbol is defined.                                                   |
| `name`                  | `String`         | Name of the symbol.                                                                                           |
| `memberType`            | `String`         | Type of the symbol, such as `val`, `var`, `type`, `class`, `object`, or `trait`.                              |
| `signature`             | `String`         | Full signature of the symbol, including method parameters or type information.                                |
| `accessModifier`        | `String`         | Access level of the symbol, such as `public`, `private`, or `protected`.                                      |
| `linesOfCode`           | `Int`            | Number of lines of code the symbol occupies. Ignores blank lines with tabs or whitespaces and line-breaks.    |
| `hasScaladoc`           | `Boolean`        | Indicates whether the symbol is documented with Scaladoc comments.                                            |
| `isDeprecated`          | `Boolean`        | Indicates whether the symbol is marked as deprecated.                                                         |
| `cComplexity`           | `Int`            | Cyclomatic complexity of the symbol, representing the number of independent code paths.                       |
| `nestingDepth`          | `Int`            | Maximum nesting depth within the symbol's body.                                                               |
| `hasInlineModifier`     | `Boolean`        | Indicates if the symbol has an `inline` keyword, used for inlining in Scala 3.                                |
| `isGivenInstance`       | `Boolean`        | Indicates whether the symbol represents a `given` instance in Scala 3.                                        |
| `isGivenConversion`     | `Boolean`        | Indicates whether the symbol represents a `given` conversion in Scala 3.                                      |
| `isImplicit`            | `Boolean`        | Indicates whether the symbol is declared as implicit.                                                         |
| `isAbstract`            | `Boolean`        | Indicates whether the symbol is abstract.                                                                     |
| `hasExplicitReturnType` | `Boolean`        | Indicates whether the symbol has an explicitly declared return type.                                          |
| `inferredReturnType`    | `Option[String]` | Inferred return type of the symbol, if not explicitly declared.                                               |
| `pmMatches`             | `Int`            | Number of pattern matching expressions within the symbol.                                                     |
| `pmCases`               | `Int`            | Total number of case clauses in all pattern matches.                                                          |
| `pmGuards`              | `Int`            | Number of guard conditions in pattern matches.                                                                |
| `pmWildcards`           | `Int`            | Number of wildcard patterns (`_`) in all pattern matches.                                                     |
| `pmMaxNesting`          | `Int`            | Maximum nesting level in pattern matching expressions.                                                        |
| `pmNestedMatches`       | `Int`            | Number of nested pattern matching expressions inside other matches.                                           |
| `pmAvgCasesPerMatch`    | `Double`         | Average number of case clauses per pattern match.                                                             |
| `bdBranches`            | `Int`            | Total number of branches, including if, case, loop, and catch statements.                                     |
| `bdIfCount`             | `Int`            | Number of if statements in the symbol.                                                                        |
| `bdCaseCount`           | `Int`            | Number of case statements in the symbol.                                                                      |
| `bdLoopCount`           | `Int`            | Number of loops (`while` or `for`) in the symbol.                                                             |
| `bdCatchCaseCount`      | `Int`            | Number of catch clauses in the symbol.                                                                        |
| `bdBoolOpsCount`        | `Int`            | Number of boolean operations (`&&`, <code>\|\|</code>, ect.)                                                  |
| `bdDensityPer100`       | `Double`         | Branch density per 100 lines of code, measuring the ratio of branching statements to lines of code.           |
| `bdBoolOpsPer100`       | `Double`         | Boolean operations density per 100 lines of code, measuring the ratio of boolean operations to lines of code. |

### MethodMetrics Parameters

The table below summarizes the parameters of the `MethodMetrics` case class and what each one represents. MethodMetrics
parameters capture similar information to MemberMetrics but are specifically tailored for methods, including additional
details about parameters and method-specific characteristics.

| Parameter               | Type             | Description                                                                                                      |
|-------------------------|------------------|------------------------------------------------------------------------------------------------------------------|
| `fileId`                | `String`         | The unique identifier of the file containing the method.                                                         |
| `name`                  | `String`         | The name of the method.                                                                                          |
| `signature`             | `String`         | The full signature of the method, including parameter types and return type.                                     |
| `accessModifier`        | `String`         | The access modifier of the method (e.g., `public`, `private`, or `protected`).                                   |
| `linesOfCode`           | `Int`            | The number of lines of code in the method.                                                                       |
| `isNested`              | `Boolean`        | Indicates whether the method is nested within another structure (e.g., another method or class).                 |
| `hasScaladoc`           | `Boolean`        | Indicates whether the method has associated Scaladoc documentation.                                              |
| `isDeprecated`          | `Boolean`        | Indicates whether the method is marked as deprecated.                                                            |
| `parentMember`          | `Option[String]` | The name of the parent member or structure, if the method is nested; otherwise, None.                            |
| `cComplexity`           | `Int`            | The cyclomatic complexity of the method, representing the number of independent paths through the method's code. |
| `nestingDepth`          | `Int`            | The nesting depth of the method, measuring the maximum level of nested structures within the method.             |
| `totalParams`           | `Int`            | The total number of parameters in the method.                                                                    |
| `paramLists`            | `Int`            | The number of parameter lists in the method.                                                                     |
| `implicitParamLists`    | `Int`            | The number of implicit parameter lists in the method.                                                            |
| `usingParamLists`       | `Int`            | The number of using parameter lists in the method.                                                               |
| `implicitParams`        | `Int`            | The total number of implicit parameters in the method.                                                           |
| `usingParams`           | `Int`            | The total number of using parameters in the method.                                                              |
| `defaultedParams`       | `Int`            | The number of parameters in the method that have default values.                                                 |
| `byNameParams`          | `Int`            | The number of by-name parameters in the method.                                                                  |
| `varargParams`          | `Int`            | The number of vararg parameters in the method.                                                                   |
| `hasInlineModifier`     | `Boolean`        | Indicates whether the method uses the inline modifier.                                                           |
| `inlineParamCount`      | `Int`            | The number of parameters in the method that are declared inline.                                                 |
| `isImplicitConversion`  | `Boolean`        | Indicates whether the method is an implicit conversion.                                                          |
| `isAbstract`            | `Boolean`        | Indicates whether the method is abstract.                                                                        |
| `hasExplicitReturnType` | `Boolean`        | Indicates whether the method has an explicitly declared return type.                                             |
| `inferredReturnType`    | `Option[String]` | The inferred return type of the method, if an explicit return type is not provided.                              |
| `pmMatches`             | `Int`            | The number of pattern match expressions in the method.                                                           |
| `pmCases`               | `Int`            | The total number of cases within pattern match expressions in the method.                                        |
| `pmGuards`              | `Int`            | The number of guard conditions in the pattern match expressions of the method.                                   |
| `pmWildcards`           | `Int`            | The number of wildcards in the pattern match expressions of the method.                                          |
| `pmMaxNesting`          | `Int`            | The maximum nesting level of pattern match expressions in the method.                                            |
| `pmNestedMatches`       | `Int`            | The total number of nested pattern match expressions in the method.                                              |
| `pmAvgCasesPerMatch`    | `Double`         | The average number of cases per pattern match expression in the method.                                          |
| `bdBranches`            | `Int`            | The total number of branches (if-else, cases, loops, etc.) in the method.                                        |
| `bdIfCount`             | `Int`            | The number of if-else branches in the method.                                                                    |
| `bdCaseCount`           | `Int`            | The number of case branches in the method.                                                                       |
| `bdLoopCount`           | `Int`            | The number of loop constructs (e.g., `while`, `for`) in the method.                                              |
| `bdCatchCaseCount`      | `Int`            | The number of catch cases in the method.                                                                         |
| `bdBoolOpsCount`        | `Int`            | The number of boolean operations (`&&`, <code>\|\|</code>, ect.)                                                 |
| `bdDensityPer100`       | `Double`         | The branch density per 100 lines of code in the method.                                                          |
| `bdBoolOpsPer100`       | `Double`         | The number of boolean operations per 100 lines of code in the method.                                            |

---

## Aggregated Project Stats

When collecting aggregated metrics, ScalaMetrics provides summarized statistics at the project, package, and file
levels. Aggregated metrics include roll-ups for methods and members, providing a high-level overview of the codebase.
Remember the steps in [Getting Started][aggregated-project-statistics], you can generate aggregated metrics using the
`generateProjectStats` method which returns a `ProjectStats` case class. This case class contains all aggregated project
statistics, including package and file-level roll-ups.

### ProjectStats Case Class

This case class contains the aggregated statistics for the entire project, including its `ProjectRollup` and a
collection of `Package` instances representing the statistics for each package in the project.
The `header` field contains metadata about the project statistics generation from `ProjectInfo` case class.
The `projectRollup` field contains the overall aggregated statistics for the entire project.

``` scala title="ProjectStats.scala"
case class ProjectStats(
    header: ProjectStatsHeader,
    projectRollup: ProjectRollup,
    packages: Vector[Package]
)
```

<!-- @formatter:off -->
!!! tip
    `ProjectStats` is the foundation case class for aggregated metrics analysis in ScalaMetrics. It encapsulates the overall 
    project statistics along with package and file-level roll-ups. This is what you need for **aggregated metrics** analysis and 
    reporting for your whole project.

<!-- @formatter:on -->

### Package Case Class

This case class encapsulates the aggregated statistics for a specific package within the project, including its
`PackageRollup` which is the aggregated statistics for all packages in the package, and a collection of `FileStats`
instances representing the statistics for each file in the package.

``` scala title="Package.scala"
case class Package(packageRollup: PackageRollup, fileStats: Vector[FileStats])
```

### FileStats Case Class

This case class encapsulates the aggregated statistics for a specific file within a package. It includes a
`FileStatsHeader` containing metadata about the file statistics generation, a `FileRollup` which is the aggregated
statistics for the file (using all declarations inside the file such as the members and methods), and a
`DeclarationStats` instance encapsulating the aggregated statistics for all declarations (methods and members) in the
file.

``` scala title="FileStats.scala"
case class FileStats(
    header: FileStatsHeader,
    fileRollup: FileRollup,
    declarationStats: DeclarationStats
)
```

### DeclarationStats Case Class

`DeclarationStats` case class encapsulates the aggregated statistics for all declarations within a file, including both
members and methods. It contains two fields: `memberStats`, which is a vector of `MemberStats` instances representing
the aggregated statistics for each member in the file, and `methodStats`, which is a vector of `MethodStats` instances
representing the aggregated statistics for each method in the file.

``` scala title="DeclarationStats.scala"
case class DeclarationStats(
    memberStats: Vector[MemberStats],
    methodStats: Vector[MethodStats]
)
```

### ProjectStatsHeader Parameters

This case class contains metadata about the project statistics generation, derived from the `ProjectInfo` case class.
Most of the information is optional to accommodate projects that may not provide all details.

| Parameter               | Type             | Description                                                                            |
|-------------------------|------------------|----------------------------------------------------------------------------------------|
| `name`                  | `String`         | The name of the project.                                                               |
| `version`               | `String`         | The version of the project.                                                            |
| `scalaVersion`          | `String`         | The Scala version used in the project.                                                 |
| `description`           | `Option[String]` | An optional description or summary of the project.                                     |
| `crossScalaVersions`    | `Seq[String]`    | A sequence of Scala versions for cross-compilation.                                    |
| `organization`          | `Option[String]` | An optional identifier for the organization overseeing the project.                    |
| `organizationName`      | `Option[String]` | An optional name of the organization overseeing the project.                           |
| `organizationHomepage`  | `Option[String]` | An optional homepage URL of the organization.                                          |
| `homepage`              | `Option[String]` | An optional homepage URL for the project.                                              |
| `licenses`              | `Option[String]` | Optional license information, typically SPDX identifier(s).                            |
| `startYear`             | `Option[String]` | An optional start year indicating when the project was initiated.                      |
| `isSnapshot`            | `Option[String]` | An optional string indicating if the project version is a snapshot.                    |
| `apiURL`                | `Option[String]` | An optional URL to the project's API documentation.                                    |
| `scmInfo`               | `Option[String]` | An optional string containing source control management information.                   |
| `developers`            | `Option[String]` | An optional string detailing information about the developers involved in the project. |
| `versionScheme`         | `Option[String]` | An optional versioning scheme used by the project (e.g., "semantic").                  |
| `projectInfoNameFormal` | `Option[String]` | An optional, formal name for the project, if applicable.                               |

### ProjectRollup Parameters

The table below summarizes the parameters of the `ProjectRollup` case class and what each one represents.

| Parameter                      | Type     | Description                                                             |
|--------------------------------|----------|-------------------------------------------------------------------------|
| `totalFiles`                   | `Int`    | Total number of files in the project.                                   |
| `totalLoc`                     | `Int`    | Total lines of code across all files in the project.                    |
| `totalFunctions`               | `Int`    | Total number of functions/methods in the project.                       |
| `totalPublicFunctions`         | `Int`    | Total number of public functions/methods.                               |
| `totalPrivateFunctions`        | `Int`    | Total number of private functions/methods.                              |
| `averageFileSizeBytes`         | `Long`   | Average size of files in bytes.                                         |
| `totalFileSizeBytes`           | `Long`   | Total size of all files in bytes.                                       |
| `totalSymbols`                 | `Int`    | Total number of symbols in the project.                                 |
| `totalPublicSymbols`           | `Int`    | Total number of public symbols in the project.                          |
| `totalPrivateSymbols`          | `Int`    | Total number of private symbols in the project.                         |
| `totalNestedSymbols`           | `Int`    | Total number of nested symbols.                                         |
| `documentedPublicSymbols`      | `Int`    | Number of documented public symbols.                                    |
| `totalDeprecatedSymbols`       | `Int`    | Total number of deprecated symbols in the project.                      |
| `scalaDocCoverage`             | `Double` | Percentage of public symbols that are documented.                       |
| `deprecatedSymbolsDensity`     | `Double` | Percentage of symbols marked as deprecated.                             |
| `totalDefsValsVars`            | `Int`    | Total number of defs, vals, and vars in the project.                    |
| `totalPublicDefsValsVars`      | `Int`    | Total number of public defs, vals, and vars.                            |
| `explicitDefsValsVars`         | `Int`    | Total number of defs, vals, and vars with explicit return types.        |
| `explicitPublicDefsValsVars`   | `Int`    | Total number of public defs, vals, and vars with explicit return types. |
| `returnTypeExplicitness`       | `Double` | Percentage of defs, vals, and vars with explicit return types.          |
| `publicReturnTypeExplicitness` | `Double` | Percentage of public defs, vals, and vars with explicit return types.   |
| `inlineMethods`                | `Int`    | Total number of inline methods.                                         |
| `inlineVals`                   | `Int`    | Total number of inline vals.                                            |
| `inlineVars`                   | `Int`    | Total number of inline vars.                                            |
| `inlineParams`                 | `Int`    | Total number of inline parameters.                                      |
| `implicitVals`                 | `Int`    | Total number of implicit vals.                                          |
| `implicitVars`                 | `Int`    | Total number of implicit vars.                                          |
| `implicitConversions`          | `Int`    | Total number of implicit conversions.                                   |
| `givenInstances`               | `Int`    | Total number of given instances.                                        |
| `givenConversions`             | `Int`    | Total number of given conversions.                                      |
| `pmMatches`                    | `Int`    | Total number of pattern match expressions.                              |
| `pmCases`                      | `Int`    | Total number of case branches in pattern matches.                       |
| `pmGuards`                     | `Int`    | Total number of guards used in pattern matches.                         |
| `pmWildcards`                  | `Int`    | Total number of wildcard usages in pattern matches.                     |
| `pmMaxNesting`                 | `Int`    | Maximum nesting depth in pattern matches.                               |
| `pmNestedMatches`              | `Int`    | Total number of nested pattern matches.                                 |
| `bdBranches`                   | `Int`    | Total number of branches.                                               |
| `bdIfCount`                    | `Int`    | Total number of if statements.                                          |
| `bdCaseCount`                  | `Int`    | Total number of case branches.                                          |
| `bdLoopCount`                  | `Int`    | Total number of loop structures.                                        |
| `bdCatchCaseCount`             | `Int`    | Total number of catch case branches.                                    |
| `bdBoolOpsCount`               | `Int`    | Total number of boolean operations.                                     |
| `bdDensityPer100`              | `Double` | Branch density per 100 lines of code.                                   |
| `bdBoolOpsPer100`              | `Double` | Boolean operation density per 100 lines of code.                        |
| `avgCyclomaticComplexity`      | `Double` | Average cyclomatic complexity across all functions.                     |
| `maxCyclomaticComplexity`      | `Int`    | Maximum cyclomatic complexity of any function.                          |
| `avgNestingDepth`              | `Double` | Average nesting depth across all functions.                             |
| `maxNestingDepth`              | `Int`    | Maximum nesting depth in any function.                                  |
| `totalPackages`                | `Int`    | Total number of packages in the project.                                |
| `packagesWithHighComplexity`   | `Int`    | Number of packages with high average complexity.                        |
| `packagesWithLowDocumentation` | `Int`    | Number of packages with documentation coverage below a threshold.       |

### PackageRollup Parameters

The table below summarizes the parameters of the `PackageRollup` case class and what each one represents.

| Parameter             | Type      | Description                                                            |
|-----------------------|-----------|------------------------------------------------------------------------|
| `name`                | `String`  | The name of the package.                                               |
| `totalFunctions`      | `Int`     | The total number of functions defined in the package.                  |
| `publicFunctions`     | `Int`     | The number of public functions in the package.                         |
| `privateFunctions`    | `Int`     | The number of private functions in the package.                        |
| `inlineMethods`       | `Int`     | The number of inline methods defined in the package.                   |
| `inlineVals`          | `Int`     | The number of inline values defined in the package.                    |
| `inlineVars`          | `Int`     | The number of inline variables defined in the package.                 |
| `inlineParams`        | `Int`     | The number of inline parameters used in the package.                   |
| `implicitVals`        | `Int`     | The number of implicit values defined in the package.                  |
| `implicitVars`        | `Int`     | The number of implicit variables defined in the package.               |
| `implicitConversions` | `Int`     | The number of implicit conversions defined in the package.             |
| `givenInstances`      | `Int`     | The number of `given` instances defined in the package.                |
| `givenConversions`    | `Int`     | The number of `given` conversions defined in the package.              |
| `pmMatches`           | `Int`     | The total number of pattern matches in the package.                    |
| `pmCases`             | `Int`     | The total number of pattern matching cases in the package.             |
| `pmGuards`            | `Int`     | The total number of guards in pattern matches in the package.          |
| `pmWildcards`         | `Int`     | The total number of wildcards used in pattern matches in the package.  |
| `pmMaxNesting`        | `Int`     | The maximum nesting level of pattern matches in the package.           |
| `pmNestedMatches`     | `Int`     | The total number of nested pattern matches in the package.             |
| `bdBranches`          | `Int`     | The total number of branch points in the package.                      |
| `bdIfCount`           | `Int`     | The number of `if` statements in the package.                          |
| `bdCaseCount`         | `Int`     | The number of `case` statements in the package.                        |
| `bdLoopCount`         | `Int`     | The number of loops (e.g., `for`, `while`) in the package.             |
| `bdCatchCaseCount`    | `Int`     | The number of `catch` cases in the package.                            |
| `bdBoolOpsCount`      | `Int`     | The number of boolean operators (`&&`, <code>\|\|</code>, ect.)        |
| `bdDensityPer100`     | `Double`  | The branch density per 100 lines of code in the package.               |
| `bdBoolOpsPer100`     | `Double`  | The number of boolean operations per 100 lines of code in the package. |

### FileStatsHeader Parameters

The table below summarizes the parameters of the `FileStatsHeader` case class and what each one represents.

| Parameter       | Type     | Description                                                 |
|-----------------|----------|-------------------------------------------------------------|
| `projectId`     | `String` | Unique identifier of the project to which the file belongs. |
| `fileId`        | `String` | Unique identifier for the file within the project.          |
| `fileName`      | `String` | Name of the file, including extension.                      |
| `filePath`      | `String` | Relative path from project root to the file.                |
| `packageName`   | `String` | Fully qualified name of the package containing the file.    |
| `linesOfCode`   | `Int`    | Number of lines of code present within the file.            |
| `fileSizeBytes` | `Long`   | Size of the file in bytes.                                  |

### MemberStats Parameters

MemberStats parameters capture detailed metrics specific to members (such as vals, vars, classes, objects, traits)
within the codebase.

| Parameter               | Type             | Description                                                                                        |
|-------------------------|------------------|----------------------------------------------------------------------------------------------------|
| `fileId`                | `String`         | The identifier of the file containing the member.                                                  |
| `name`                  | `String`         | The name of the member.                                                                            |
| `memberType`            | `String`         | The type of the member, such as `val`, `var`, `type`, `class`, `object`, or `trait`.               |
| `signature`             | `String`         | The fully qualified signature of the member, including parameters and return type when applicable. |
| `accessModifier`        | `String`         | The access modifier (e.g., public, private, protected) of the member.                              |
| `linesOfCode`           | `Int`            | The number of lines of code comprising the member.                                                 |
| `hasScaladoc`           | `Boolean`        | Whether the member has associated Scaladoc comments.                                               |
| `isDeprecated`          | `Boolean`        | Whether the member is marked as deprecated.                                                        |
| `cComplexity`           | `Int`            | Cyclomatic complexity, i.e., the number of independent paths through the member's code.            |
| `nestingDepth`          | `Int`            | The maximum nesting depth of constructs within the member.                                         |
| `hasInlineModifier`     | `Boolean`        | Whether the member includes the `inline` modifier.                                                 |
| `isGivenInstance`       | `Boolean`        | Whether the member is defined as a `given` instance (Scala 3).                                     |
| `isGivenConversion`     | `Boolean`        | Whether the member is a `given` conversion (Scala 3).                                              |
| `isImplicit`            | `Boolean`        | Whether the member uses the `implicit` modifier.                                                   |
| `isAbstract`            | `Boolean`        | Whether the member is abstract.                                                                    |
| `hasExplicitReturnType` | `Boolean`        | Whether the member has an explicitly declared return type.                                         |
| `inferredReturnType`    | `Option[String]` | The inferred return type of the member if no explicit return type is provided.                     |
| `pmMatches`             | `Int`            | Total number of pattern match expressions in the member.                                           |
| `pmCases`               | `Int`            | Total number of case clauses across all matches.                                                   |
| `pmGuards`              | `Int`            | Total number of guard conditions in case clauses.                                                  |
| `pmWildcards`           | `Int`            | Total number of wildcard patterns used in matches.                                                 |
| `pmMaxNesting`          | `Int`            | Maximum nesting depth found within pattern matches.                                                |
| `pmNestedMatches`       | `Int`            | Total number of nested pattern matches.                                                            |
| `pmAvgCasesPerMatch`    | `Double`         | Average number of cases per pattern match.                                                         |
| `bdBranches`            | `Int`            | Total number of branching constructs (if, case, loops, etc.).                                      |
| `bdIfCount`             | `Int`            | Number of `if` constructs.                                                                         |
| `bdCaseCount`           | `Int`            | Number of `case` constructs.                                                                       |
| `bdLoopCount`           | `Int`            | Number of loop constructs.                                                                         |
| `bdCatchCaseCount`      | `Int`            | Number of `catch` case blocks.                                                                     |
| `bdBoolOpsCount`        | `Int`            | Number of boolean operations (`&&`, <code>\|\|</code>, ect.)                                       |
| `bdDensityPer100`       | `Double`         | Branches per 100 lines of code.                                                                    |
| `bdBoolOpsPer100`       | `Double`         | Boolean operations per 100 lines of code.                                                          |

### MethodStats Parameters

MethodStats parameters capture detailed metrics specific to methods within the codebase.

| Parameter               | Type             | Description                                                                            |
|-------------------------|------------------|----------------------------------------------------------------------------------------|
| `fileId`                | `String`         | The unique identifier of the source file containing the method.                        |
| `name`                  | `String`         | The name of the method.                                                                |
| `signature`             | `String`         | The complete signature of the method, including parameter and return type details.     |
| `accessModifier`        | `String`         | The access level of the method, such as `public`, `private`, or `protected`.           |
| `linesOfCode`           | `Int`            | The number of lines of code in the method.                                             |
| `isNested`              | `Boolean`        | Indicates whether the method is nested within another method or class.                 |
| `hasScaladoc`           | `Boolean`        | Indicates whether the method is documented with Scaladoc.                              |
| `isDeprecated`          | `Boolean`        | Indicates whether the method is marked as deprecated.                                  |
| `parentMember`          | `Option[String]` | The name of the parent member (if any) within which this method is nested.             |
| `cComplexity`           | `Int`            | The cyclomatic complexity of the method.                                               |
| `nestingDepth`          | `Int`            | The nesting depth of the method code.                                                  |
| `totalParams`           | `Int`            | The total number of parameters defined in the method signature.                        |
| `paramLists`            | `Int`            | The number of parameter lists in the method signature.                                 |
| `implicitParamLists`    | `Int`            | The count of implicit parameter lists in the method signature.                         |
| `usingParamLists`       | `Int`            | The count of `using` parameter lists in the method signature.                          |
| `implicitParams`        | `Int`            | The total number of implicit parameters in the method signature.                       |
| `usingParams`           | `Int`            | The total number of `using` parameters in the method signature.                        |
| `defaultedParams`       | `Int`            | The count of parameters with default values in the method signature.                   |
| `byNameParams`          | `Int`            | The count of by-name parameters in the method signature.                               |
| `varargParams`          | `Int`            | The count of variadic (varargs) parameters in the method signature.                    |
| `hasInlineModifier`     | `Boolean`        | Indicates if the method is marked with the `inline` modifier.                          |
| `inlineParamCount`      | `Int`            | The number of parameters in the method's inline sections.                              |
| `isImplicitConversion`  | `Boolean`        | Indicates whether the method is an implicit conversion.                                |
| `isAbstract`            | `Boolean`        | Indicates whether the method is abstract.                                              |
| `hasExplicitReturnType` | `Boolean`        | Indicates if the method has an explicitly defined return type.                         |
| `inferredReturnType`    | `Option[String]` | The inferred return type of the method, if no explicit return type is provided.        |
| `pmMatches`             | `Int`            | The number of pattern matches within the method.                                       |
| `pmCases`               | `Int`            | The total number of pattern match cases within the method.                             |
| `pmGuards`              | `Int`            | The count of guards (`if` conditions) within pattern matches.                          |
| `pmWildcards`           | `Int`            | The number of wildcard patterns used in the method.                                    |
| `pmMaxNesting`          | `Int`            | The maximum nesting level within pattern matches in the method.                        |
| `pmNestedMatches`       | `Int`            | The count of nested pattern matches within the method.                                 |
| `pmAvgCasesPerMatch`    | `Double`         | The average number of cases per pattern match within the method.                       |
| `bdBranches`            | `Int`            | The count of branch instructions (such as `if`, `else`, and loops) in the method code. |
| `bdIfCount`             | `Int`            | The number of `if` conditions in the method code.                                      |
| `bdCaseCount`           | `Int`            | The number of `case` statements in the method code.                                    |
| `bdLoopCount`           | `Int`            | The count of loop constructs (such as `for`, `while`) in the method code.              |
| `bdCatchCaseCount`      | `Int`            | The number of `catch` clauses in the method.                                           |
| `bdBoolOpsCount`        | `Int`            | The number of boolean operations (`&&`, <code>\|\|</code>, ect.)                       |
| `bdDensityPer100`       | `Double`         | The branch density metric calculated per 100 lines of code.                            |
| `bdBoolOpsPer100`       | `Double`         | The count of boolean operations per 100 lines of code.                                 |
