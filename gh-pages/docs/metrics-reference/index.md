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
    fileMetrics: Vector[FileMetrics]
)
```

<!-- @formatter:off -->
!!! tip
    `ProjectMetrics` is the foundation case class for raw metrics analysis in ScalaMetrics. It encapsulates the overall 
    project information. This is what you need for raw metrics analysis and custom reporting and aggregation for your
    whole project.
<!-- @formatter:on -->

### FileMetrics Case Class

The `FileMetrics` case class encapsulates the metrics for a single scala source file, along with its associated
method and member metrics.

``` scala title="FileMetrics.scala"
case class FileMetrics(
    metadata: FileMetadata,
    methodMetrics: Vector[MethodMetrics] = Vector.empty,
    memberMetrics: Vector[MemberMetrics] = Vector.empty
)
```

### FileMetadata Case Class

The `FileMetadata` case class contains metadata information about a Scala source file, including its project
association, its unique identifier, and various file-level metrics.

``` scala title="FileMetadata.scala" 
case class FileMetadata(
    projectId: Option[String] = None,
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

### FileMetadata Parameters

The table below summarizes the parameters of the `FileMetadata` case class and what each one represents.

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

| Parameter                  | Type                       | Description                                                                              |
|----------------------------|----------------------------|------------------------------------------------------------------------------------------|
| `metadata`                 | `Metadata`                 | Structural and property-related information about the member (name, type, access, etc.). |
| `complexity`               | `Int`                      | Cyclomatic complexity measuring independent paths through the member's code.             |
| `nestingDepth`             | `Int`                      | Depth of nested constructs inside the member.                                            |
| `hasScaladoc`              | `Boolean`                  | Whether the member has accompanying Scaladoc documentation.                              |
| `inlineAndImplicitMetrics` | `InlineAndImplicitMetrics` | Inline/implicit/explicitness and Scala 3 `given` details.                                |
| `patternMatchingMetrics`   | `PatternMatchingMetrics`   | Counts and structure of pattern-matching constructs.                                     |
| `branchDensityMetrics`     | `BranchDensityMetrics`     | Branch density and boolean-operation metrics.                                            |

### MethodMetrics Parameters

The table below summarizes the parameters of the `MethodMetrics` case class and what each one represents. MethodMetrics
parameters capture similar information to MemberMetrics but are specifically tailored for methods, including additional
details about parameters and method-specific characteristics.

| Parameter                  | Type                       | Description                                                                     |
|----------------------------|----------------------------|---------------------------------------------------------------------------------|
| `metadata`                 | `Metadata`                 | Metadata for the method (name, signature, access, LoC, etc.).                   |
| `complexity`               | `Int`                      | Cyclomatic complexity of the method.                                            |
| `nestingDepth`             | `Int`                      | Maximum depth of nested control-flow constructs in the method.                  |
| `hasScaladoc`              | `Boolean`                  | Whether the method is documented with Scaladoc.                                 |
| `parameterMetrics`         | `ParameterMetrics`         | Parameter counts and characteristics (lists, implicit, by-name, varargs, etc.). |
| `inlineAndImplicitMetrics` | `InlineAndImplicitMetrics` | Inline/implicit/explicitness and return-type details for the method.            |
| `patternMatchingMetrics`   | `PatternMatchingMetrics`   | Pattern-matching counts (matches, cases, guards, nesting, etc.).                |
| `branchDensityMetrics`     | `BranchDensityMetrics`     | Branch density, boolean ops, and normalized densities.                          |

### Metadata Parameters

The `Metadata` case class captures structural and property-related information about a member or method.

| Parameter         | Type             | Description                                                                         |
|-------------------|------------------|-------------------------------------------------------------------------------------|
| `fileId`          | `String`         | The unique identifier of the file where the symbol is located.                      |
| `name`            | `String`         | The name of the symbol or declaration.                                              |
| `signature`       | `String`         | The signature of the symbol, representing its type or structure.                    |
| `accessModifier`  | `String`         | The access level of the symbol (e.g., `public`, `private`, `protected`).            |
| `linesOfCode`     | `Int`            | The number of lines of code associated with the symbol.                             |
| `isDeprecated`    | `Boolean`        | Indicates whether the symbol is marked as deprecated.                               |
| `isNested`        | `Boolean`        | Indicates whether the symbol is nested within another symbol.                       |
| `declarationType` | `String`         | The kind of declaration (e.g.,`val`, `var`, `type`, `class`, `object`, or `trait`.) |
| `parentMember`    | `Option[String]` | The parent symbol name, if the current symbol is nested. (for methods only)         |

### ParameterMetrics Parameters

The `ParameterMetrics` case class captures metrics related to the parameters of methods.
See [Parameters & Arity Analysis][parameters-&-arity-analysis]

| Parameter            | Type  | Description                                                |
|----------------------|-------|------------------------------------------------------------|
| `totalParams`        | `Int` | The total number of parameters across all parameter lists. |
| `paramLists`         | `Int` | The total number of parameter lists.                       |
| `implicitParamLists` | `Int` | The count of implicit parameter lists.                     |
| `usingParamLists`    | `Int` | The count of `using` parameter lists.                      |
| `implicitParams`     | `Int` | The total number of implicit parameters.                   |
| `usingParams`        | `Int` | The total number of `using` parameters.                    |
| `defaultedParams`    | `Int` | The count of parameters that have default values.          |
| `byNameParams`       | `Int` | The total number of by-name parameters.                    |
| `varargParams`       | `Int` | The total number of variadic (varargs) parameters.         |

### InlineAndImplicitMetrics Parameters

The `InlineAndImplicitMetrics` case class captures metrics related to inline definitions, implicit usage, and return
type explicitness for both members and methods. See [Inline & Implicit Analysis][inline-and-implicit-analysis] for more
details.

| Parameter               | Type              | Description                                                         |
|-------------------------|-------------------|---------------------------------------------------------------------|
| `hasInlineModifier`     | `Boolean`         | Indicates whether the symbol has an `inline` modifier.              |
| `inlineParamCount`      | `Option[Int]`     | The count of parameters defined as `inline`, if applicable.         |
| `isImplicitConversion`  | `Boolean`         | Indicates whether the symbol represents an implicit conversion.     |
| `isImplicit`            | `Boolean`         | Indicates whether the symbol itself is declared as implicit.        |
| `isAbstract`            | `Boolean`         | Indicates whether the symbol is abstract.                           |
| `hasExplicitReturnType` | `Boolean`         | Whether an explicit return type is specified for the symbol.        |
| `inferredReturnType`    | `Option[String]`  | The inferred return type of the symbol if not explicitly specified. |
| `isGivenInstance`       | `Option[Boolean]` | Whether the symbol represents a Scala 3 `given` instance.           |
| `isGivenConversion`     | `Option[Boolean]` | Whether the symbol represents a Scala 3 `given` conversion.         |

### PatternMatchingMetrics Parameters

The `PatternMatchingMetrics` case class captures metrics related to pattern matching constructs within methods and
members. See [Pattern-Matching Complexity][pattern-matching-complexity] for more details.

| Parameter          | Type        | Description                                                          |
|--------------------|-------------|----------------------------------------------------------------------|
| `matches`          | `Int`       | The total number of match expressions found.                         |
| `cases`            | `Int`       | The total number of case statements across all match expressions.    |
| `guards`           | `Int`       | The total number of case guards used in match expressions.           |
| `wildcards`        | `Int`       | The total number of wildcard patterns (_) used in match expressions. |
| `maxNesting`       | `Int`       | The maximum nesting depth of match constructs.                       |
| `nestedMatches`    | `Int`       | The total number of nested match expressions.                        |
| `avgCasesPerMatch` | `Double`    | The average number of case statements per match expression.          |
| `matchCases`       | `List[Int]` | A list of the case-count for each match expression.                  |

### BranchDensityMetrics Parameters

The `BranchDensityMetrics` case class captures metrics related to branch density and boolean operations within methods
and members. See [Expression Branch Density Analysis][expression-branch-density-analysis] for more details.

| Parameter        | Type     | Description                                                                  |
|------------------|----------|------------------------------------------------------------------------------|
| `loc`            | `Int`    | The total number of lines of code (LOC) associated with the analyzed symbol. |
| `branches`       | `Int`    | The total number of branching points (ifs, loops, cases, catch blocks).      |
| `ifCount`        | `Int`    | The number of `if` statements identified.                                    |
| `caseCount`      | `Int`    | The number of `case` patterns used.                                          |
| `loopCount`      | `Int`    | The number of loop constructs (e.g., `for`, `while`, `do-while`).            |
| `catchCaseCount` | `Int`    | The number of `catch` blocks used for exception handling.                    |
| `boolOpsCount`   | `Int`    | The number of boolean operations (`&&`, <code>\|\|</code>, ect.)             |
| `densityPer100`  | `Double` | Branch density normalized per 100 lines of code.                             |
| `boolOpsPer100`  | `Double` | Boolean operations normalized per 100 lines of code.                         |

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
    metadata: ProjectMetadata,
    rollup: Rollup,
    packageStats: Vector[PackageStats]
)
```

<!-- @formatter:off -->
!!! tip
    `ProjectStats` is the foundation case class for aggregated metrics analysis in ScalaMetrics. It encapsulates the overall 
    project statistics along with package and file-level roll-ups. This is what you need for **aggregated metrics** analysis and 
    reporting for your whole project.
<!-- @formatter:on -->

### PackageStats Case Class

This case class encapsulates the aggregated statistics for a specific package within the project, including its
`rollup` which is the aggregated statistics for all packages in the package, and a collection of `FileStats`
instances representing the statistics for each file in the package.

``` scala title="PackageStats.scala"
case class PackageStats(metadata: PackageMetadata, 
    rollup: Rollup, 
    fileStats: Vector[FileStats]
)
```

### FileStats Case Class

This case class encapsulates the aggregated statistics for a specific file within a package. It includes a
`FileStatsMetadata` containing metadata about the file statistics generation, a `rollup` which is the aggregated
statistics for the file (using all declarations inside the file such as the members and methods), and a `MemberStats`
and `MethodStats` vectors containing the aggregated statistics for all declarations (methods and members) in the file.

``` scala title="FileStats.scala"
case class FileStats(
    metadata: FileStatsMetadata,
    rollup: Rollup,
    memberStats: Vector[MemberStats],
    methodStats: Vector[MethodStats]
)
```

### FileStatsMetadata Parameters

The table below summarizes the parameters of the `FileStatsMetadata` case class and what each one represents.

| Parameter       | Type     | Description                                                 |
|-----------------|----------|-------------------------------------------------------------|
| `projectId`     | `String` | Unique identifier of the project to which the file belongs. |
| `fileId`        | `String` | Unique identifier for the file within the project.          |
| `fileName`      | `String` | Name of the file, including extension.                      |
| `filePath`      | `String` | Relative path from project root to the file.                |
| `packageName`   | `String` | Fully qualified name of the package containing the file.    |
| `linesOfCode`   | `Int`    | Number of lines of code present within the file.            |
| `fileSizeBytes` | `Long`   | Size of the file in bytes.                                  |

### ProjectMetadata Parameters

This case class contains metadata about the project statistics generation, derived from the `ProjectInfo` case class.
Most of the information is optional to accommodate projects that may not provide all details.

| Parameter               | Type             | Description                                                      |
|-------------------------|------------------|------------------------------------------------------------------|
| `name`                  | `String`         | The name of the project.                                         |
| `version`               | `String`         | The version of the project.                                      |
| `scalaVersion`          | `String`         | The Scala version used in the project.                           |
| `description`           | `Option[String]` | Optional description or summary of the project.                  |
| `crossScalaVersions`    | `Seq[String]`    | Scala versions used for cross-compilation.                       |
| `organization`          | `Option[String]` | Optional identifier for the organization overseeing the project. |
| `organizationName`      | `Option[String]` | Optional name of the organization overseeing the project.        |
| `organizationHomepage`  | `Option[String]` | Optional homepage URL of the organization.                       |
| `homepage`              | `Option[String]` | Optional homepage URL for the project.                           |
| `licenses`              | `Option[String]` | Optional license information (e.g., SPDX identifiers).           |
| `startYear`             | `Option[String]` | Optional start year indicating when the project was initiated.   |
| `isSnapshot`            | `Option[String]` | Optional flag indicating if the version is a snapshot.           |
| `apiURL`                | `Option[String]` | Optional URL to the project's API documentation.                 |
| `scmInfo`               | `Option[String]` | Optional source control management information.                  |
| `developers`            | `Option[String]` | Optional details about the developers involved.                  |
| `versionScheme`         | `Option[String]` | Optional versioning scheme (e.g., "semantic").                   |
| `projectInfoNameFormal` | `Option[String]` | Optional, formal name for the project.                           |

### Rollup Parameters

The table below summarizes the parameters of the `Rollup` case class and what each one represents.

| Parameter                            | Type                     | Description                                                                                           |
|--------------------------------------|--------------------------|-------------------------------------------------------------------------------------------------------|
| `totalCount`                         | `Int`                    | The total number of items (files, packages, etc.) in the rollup.                                      |
| `averageFileSizeBytes`               | `Long`                   | The average file size in bytes.                                                                       |
| `returnTypeExplicitness`             | `Double`                 | The percentage of definitions with explicit return types.                                             |
| `publicReturnTypeExplicitness`       | `Double`                 | The percentage of public definitions with explicit return types.                                      |
| `itemsWithHighComplexity`            | `Int`                    | The count of items with average complexity above a defined threshold.                                 |
| `itemsWithLowDocumentation`          | `Int`                    | The count of items with documentation coverage below a defined threshold.                             |
| `itemsWithHighNesting`               | `Int`                    | The count of items with average nesting depth above a defined threshold.                              |
| `itemsWithHighBranchDensity`         | `Int`                    | The count of items with average branch density above a defined threshold.                             |
| `itemsWithHighPatternMatching`       | `Int`                    | The count of items with average pattern matching statistics above a defined threshold.                |
| `itemsWithHighParameterCount`        | `Int`                    | he count of items with average parameter count above a defined threshold.                             |
| `avgCyclomaticComplexity`            | `Double`                 | The average cyclomatic complexity across all items.                                                   |
| `maxCyclomaticComplexity`            | `Int`                    | The maximum cyclomatic complexity observed among all items.                                           |
| `avgNestingDepth`                    | `Double`                 | The average nesting depth across all items.                                                           |
| `maxNestingDepth`                    | `Int`                    | The maximum nesting depth observed among all items.                                                   |
| `scalaDocCoveragePercentage`         | `Double`                 | The density percentage of deprecated symbols.                                                         |
| `deprecatedSymbolsDensityPercentage` | `Double`                 | The percentage of deprecated symbols across all symbols.                                              |  
| `coreStats`                          | `CoreStats`              | Core statistical metrics encapsulated in a `CoreStats` instance.                                      |
| `inlineAndImplicitStats`             | `InlineAndImplicitStats` | Statistics related to inline and implicit usage encapsulated in an `InlineAndImplicitStats` instance. |
| `patternMatchingStats`               | `PatternMatchingStats`   | Statistics related to pattern matching encapsulated in a `PatternMatchingStats` instance.             |
| `branchDensityStats`                 | `BranchDensityStats`     | Statistics related to branch density encapsulated in a `BranchDensityStats` instance.                 | 
| `parameterStats`                     | `ParameterStats`         | Statistics related to parameter usage encapsulated in a `ParameterStats` instance.                    |


### MemberStats Parameters

MemberStats parameters capture detailed metrics specific to members (such as vals, vars, classes, objects, traits)
within the codebase.

| Parameter                | Type                       | Description                                                                                                                                                               |
|--------------------------|----------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `metadata`               | `Metadata`                 | Descriptive metadata for the member (identity, structure, access, etc.).                                                                                                  |
| `complexity`             | `Int`                      | Cyclomatic complexity of the member.                                                                                                                                      |
| `hasScaladoc`            | `Boolean`                  | Indicates whether the member is documented with Scaladoc.                                                                                                                 |
| `nestingDepth`           | `Int`                      | The maximum depth of nested constructs within the member, providing an indication of structural complexity.                                                               |
| `inlineAndImplicitStats` | `InlineAndImplicitMetrics` | A set of metrics related to the inline and implicit characteristics of the member, such as the presence of the `inline` modifier, implicit conversions, and abstractness. |
| `patternMatchingStats`   | `PatternMatchingStats`     | Metrics that assess the complexity and usage of pattern matching constructs within the member, including the  number of cases, guards, and wildcards                      |
| `branchDensityStats`     | `BranchDensityStats`       | Metrics that provide insights into the branching intensity and density for the member, including counts of branches, loops, and conditional statements.                   |

### MethodStats Parameters

MethodStats parameters capture detailed metrics specific to methods within the codebase.

| Parameter                | Type                       | Description                                                                                                                                                |
|--------------------------|----------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `metadata`               | `Metadata`                 | Metadata for the method declaration (identity, structure, and properties).                                                                                 |
| `complexity`             | `Int`                      | Cyclomatic complexity of the method.                                                                                                                       |
| `hasScaladoc`            | `Boolean`                  | Indicates whether the method has associated Scaladoc.                                                                                                      |
| `nestingDepth`           | `Int`                      | The maximum depth of nested blocks or control structures within the method.                                                                                |
| `paramStats`             | `ParameterStats`           | Detailed statistics related to the method's parameters, such as total parameter count, number of implicit parameters, variadic parameters, and others.     |
| `inlineAndImplicitStats` | `InlineAndImplicitMetrics` | Inline and implicit-related metrics summarizing characteristics like inline modifiers, implicit conversions, and given instances or conversions (Scala 3). |
| `patternMatchingStats`   | `PatternMatchingStats`     | Pattern match-related metrics, capturing the structure, nesting, guard clauses, and wildcard usage within the  method.                                     |
| `branchDensityStats`     | `BranchDensityStats`       | Metrics assessing branch density within the method, providing insight into the control flow and branching within its implementation.                       |
