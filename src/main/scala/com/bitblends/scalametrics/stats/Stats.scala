/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.stats

import com.bitblends.scalametrics.metrics.model._
import com.bitblends.scalametrics.stats.model._
import com.bitblends.scalametrics.utils.Id

/**
  * Represents a statistical analysis and aggregation utility for various levels of code metrics.
  *
  * The `Stats` class provides methods to compute, aggregate, and organize metrics at the member, method, file, package,
  * and project levels. These methods assist in analyzing structural and behavioral characteristics of code components
  * such as complexity, nesting, pattern matching, symbol visibility, and declarations.
  */
object Stats {

  /**
    * Analyzes project metrics and generates a project representation with detailed metrics.
    *
    * The method processes file-level and package-level metrics, calculates various statistics such as code coverage,
    * symbol visibility, inline usage, implicit definitions, and pattern matching metrics. Based on these calculations,
    * it composes an instance of the `Project` class representing the metrics of the analyzed project.
    *
    * @param projectMetrics
    *   a `ProjectMetrics` instance containing metrics data for files and packages within a project.
    * @param projectBaseDir
    *   optional base directory for computing relative file paths. If not provided, uses absolute paths.
    * @return
    *   a `Project` instance containing aggregated and detailed metrics derived from the input.
    */
  def getProjectStats(projectMetrics: ProjectMetrics, projectBaseDir: Option[java.io.File] = None): ProjectStats = {

    // Get all files and create file stats for each file
    val fileStatsList: Vector[FileStats] = projectMetrics.fileMetrics.map(fm => fileStats(fm, projectBaseDir))

    // Package stats
    val packageStats: Vector[PackageStats] = this.packageStats(fileStatsList)

    // Project metadata
    val metadata = projectMetadata(projectMetrics.projectInfo)

    // Rollup packages
    val rollup = packageStats.map(_.rollup).foldLeft(Rollup())(_ + _)

    ProjectStats(
      metadata = metadata,
      rollup = rollup,
      packageStats = packageStats
    )

  }

  /**
    * Computes statistics for each package by aggregating file statistics. Groups the given file statistics by package
    * name, calculates a rollup for each package, and returns a vector of package-level statistics.
    *
    * @param fileStats
    *   Vector of FileStats containing metadata and statistical data for files.
    * @return
    *   Vector of PackageStats, where each entry represents aggregated statistics for a single package.
    */
  private def packageStats(fileStats: Vector[FileStats]): Vector[PackageStats] = {
    fileStats
      .groupBy(_.metadata.packageName)
      .map { case (packageName: String, fileStatsInPackage: Vector[FileStats]) =>
        val packageRollup = fileStatsInPackage.map(_.rollup).foldLeft(Rollup())(_ + _)
        PackageStats(PackageMetadata(Id.of(packageName), packageName), packageRollup, fileStatsInPackage)
      }
      .toVector
  }

  /**
    * Calculates the average nesting depth based on the provided method and member statistics.
    *
    * @param methodStats
    *   a vector of MethodStats containing nesting depth information for methods
    * @param memberStats
    *   a vector of MemberStats containing nesting depth information for members
    * @return
    *   the average nesting depth as a double, or 0.0 if no nesting depth data is available
    */
  private def avgNestingDepth(methodStats: Vector[MethodStats], memberStats: Vector[MemberStats]): Double = {
    val nestings = methodStats.map(_.nestingDepth) ++ memberStats.map(_.nestingDepth)
    if (nestings.nonEmpty)
      nestings.sum.toDouble / nestings.size
    else 0.0
  }

  /**
    * Computes the maximum nesting depth from the given method and member statistics.
    *
    * @param methodStats
    *   a vector of MethodStats which contains nesting depth information for methods
    * @param memberStats
    *   a vector of MemberStats which contains nesting depth information for members
    * @return
    *   the maximum nesting depth found among the provided method and member statistics, or 0 if none are provided
    */
  private def maxNestingDepth(methodStats: Vector[MethodStats], memberStats: Vector[MemberStats]): Int = {
    val nestings = methodStats.map(_.nestingDepth) ++ memberStats.map(_.nestingDepth)
    if (nestings.nonEmpty)
      nestings.max
    else 0
  }

  /**
    * Calculates the percentage of public symbols that are documented.
    *
    * @param totalPublicSymbols
    *   the total number of public symbols in the codebase
    * @param documentedPublicSymbols
    *   the number of documented public symbols in the codebase
    * @return
    *   the percentage of documented public symbols as a double, or 0.0 if there are no public symbols
    */
  private def scalaDocCoverage(totalPublicSymbols: Int, documentedPublicSymbols: Int) = {
    if (totalPublicSymbols > 0)
      100.0 * documentedPublicSymbols.toDouble / totalPublicSymbols
    else 0.0
  }

  /**
    * Calculates the density of deprecated symbols as a percentage of the total symbols.
    *
    * @param totalSymbols
    *   the total number of symbols
    * @param totalDeprecatedSymbols
    *   the total number of deprecated symbols
    * @return
    *   the percentage of deprecated symbols among the total symbols, or 0.0 if totalSymbols is 0
    */
  private def deprecatedSymbolsDensity(totalSymbols: Int, totalDeprecatedSymbols: Int) = {
    if (totalSymbols > 0)
      100.0 * totalDeprecatedSymbols.toDouble / totalSymbols
    else 0.0
  }

  /**
    * Computes the maximum cyclomatic complexity from the provided method statistics and member statistics.
    *
    * @param methodStats
    *   a vector of MethodStats, each containing information about the cyclomatic complexity of methods
    * @param memberStats
    *   a vector of MemberStats, each containing information about the cyclomatic complexity of class members
    * @return
    *   the maximum cyclomatic complexity from the method and member statistics; returns 0 if no complexities are
    *   provided
    */
  private def maxCyclomaticComplexity(methodStats: Vector[MethodStats], memberStats: Vector[MemberStats]): Int = {
    val complexities = methodStats.map(_.complexity) ++ memberStats.map(_.complexity)
    if (complexities.nonEmpty)
      complexities.max
    else 0
  }

  /**
    * Calculates the average cyclomatic complexity from a collection of method and member statistics.
    *
    * @param methodStats
    *   A vector containing statistics of methods including their cyclomatic complexity.
    * @param memberStats
    *   A vector containing statistics of members including their cyclomatic complexity.
    * @return
    *   The average cyclomatic complexity as a Double. If no data is provided, returns 0.0.
    */
  private def avgCyclomaticComplexity(methodStats: Vector[MethodStats], memberStats: Vector[MemberStats]): Double = {
    val complexities = methodStats.map(_.complexity) ++ memberStats.map(_.complexity)
    if (complexities.nonEmpty)
      complexities.sum.toDouble / complexities.size
    else 0.0
  }

  /**
    * Determines the number of files with an average method complexity greater than the specified threshold.
    *
    * @param fileStats
    *   A vector of FileStats representing the statistics of various files.
    * @param threshold
    *   A double value representing the complexity threshold. Default is 10.0.
    * @return
    *   The count of files where the average method complexity exceeds the threshold.
    */
  private def filesWithHighComplexity(fileStats: Vector[FileStats], threshold: Double = 10.0): Int = {
    fileStats.count { fs =>
      val fileMethods: Vector[MethodStats] = fs.methodStats
      if (fileMethods.nonEmpty) {
        val avgComplexity = fileMethods.map(_.complexity).sum.toDouble / fileMethods.size
        avgComplexity > threshold
      } else false
    }
  }

  /**
    * Counts the number of files with low documentation coverage based on a given threshold.
    *
    * @param fileStats
    *   a vector of file statistics containing documentation and symbol counts
    * @param threshold
    *   the documentation coverage percentage threshold (default is 50.0)
    * @return
    *   the number of files with documentation coverage below the specified threshold
    */
  private def filesWithLowDocumentation(fileStats: Vector[FileStats], threshold: Double = 50.0): Int = {
    fileStats.count { fs =>
      val filePublicSymbols = fs.rollup.coreStats.totalPublicSymbols
      val fileDocumentedSymbols = fs.rollup.coreStats.totalDocumentedPublicSymbols
      if (filePublicSymbols > 0) {
        val coverage = 100.0 * fileDocumentedSymbols.toDouble / filePublicSymbols
        coverage < threshold
      } else false
    }
  }

  /**
    * Computes the number of files with an average nesting depth above a given threshold.
    *
    * @param fileStats
    *   Vector containing statistics for files, including method and member stats.
    * @param threshold
    *   The threshold value for average nesting depth to consider a file as highly nested. Default is 3.0.
    * @return
    *   The count of files with an average nesting depth exceeding the specified threshold.
    */
  private def filesWithHighNesting(fileStats: Vector[FileStats], threshold: Double = 3.0): Int = {
    fileStats.count { fs =>
      val fileMethods: Vector[MethodStats] = fs.methodStats
      val fileMembers: Vector[MemberStats] = fs.memberStats
      val allNestingDepths = fileMethods.map(_.nestingDepth) ++ fileMembers.map(_.nestingDepth)
      if (allNestingDepths.nonEmpty) {
        val avgNesting = allNestingDepths.sum.toDouble / allNestingDepths.size
        avgNesting > threshold
      } else false
    }
  }

  /**
    * Counts the number of files whose average branch density exceeds the specified threshold.
    *
    * @param fileStats
    *   Vector of `FileStats` representing files to analyze.
    * @param threshold
    *   The branch density threshold to evaluate against. Defaults to 5.0.
    * @return
    *   The count of files with an average branch density greater than the threshold.
    */
  private def filesWithHighBranchDensity(fileStats: Vector[FileStats], threshold: Double = 5.0): Int = {
    fileStats.count { fs =>
      val fileMethods: Vector[MethodStats] = fs.methodStats
      val fileMembers: Vector[MemberStats] = fs.memberStats
      val allBranchDensities =
        fileMethods.map(_.branchDensityStats.branches) ++ fileMembers.map(_.branchDensityStats.branches)
      if (allBranchDensities.nonEmpty) {
        val avgBranchDensity = allBranchDensities.sum.toDouble / allBranchDensities.size
        avgBranchDensity > threshold
      } else false
    }
  }

  /**
    * Counts the number of files that have an average pattern matching count greater than a specified threshold.
    *
    * @param fileStats
    *   A vector of `FileStats`, where each `FileStats` object contains statistics related to a file.
    * @param threshold
    *   A double value representing the average pattern matching threshold above which files are considered. Defaults to
    *   5.0 if not provided.
    * @return
    *   The count of files where the average pattern matching exceeds the specified threshold.
    */
  private def filesWithHighPatternMatching(fileStats: Vector[FileStats], threshold: Double = 5.0): Int = {
    fileStats.count { fs =>
      val fileMethods: Vector[MethodStats] = fs.methodStats
      val fileMembers: Vector[MemberStats] = fs.memberStats
      val allPatternMatches =
        fileMethods.map(_.patternMatchingStats.matches) ++ fileMembers.map(_.patternMatchingStats.matches)
      if (allPatternMatches.nonEmpty) {
        val avgPatternMatches = allPatternMatches.sum.toDouble / allPatternMatches.size
        avgPatternMatches > threshold
      } else false
    }
  }

  /**
    * Counts the number of files where the average parameter count of methods exceeds a specified threshold.
    *
    * @param fileStats
    *   A vector of `FileStats` objects containing method-level statistics.
    * @param threshold
    *   A threshold value for the average parameter count. Default is 5.0.
    * @return
    *   The count of files where the average parameter count of methods exceeds the threshold.
    */
  private def filesWithHighParameterCount(fileStats: Vector[FileStats], threshold: Double = 5.0): Int = {
    fileStats.count { fs =>
      val fileMethods: Vector[MethodStats] = fs.methodStats
      val allParamCounts = fileMethods.map(_.parameterStats.totalParams)
      if (allParamCounts.nonEmpty) {
        val avgParamCount = allParamCounts.sum.toDouble / allParamCounts.size
        avgParamCount > threshold
      } else false
    }
  }

  /**
    * Generates a header containing key metadata about a project from the given project information.
    *
    * This method extracts information such as the project's name, version, Scala version, description, organization
    * details, licenses, and other metadata to create an instance of `ProjectStatsHeader`.
    *
    * @param projectInfo
    *   The input object containing various details about the project, including its name, version, Scala version,
    *   organization, licenses, description, developers, and more.
    * @return
    *   A `ProjectStatsHeader` object that encapsulates the project's metadata in a structured format.
    */
  private def projectMetadata(projectInfo: ProjectInfo): ProjectMetadata = {
    ProjectMetadata(
      name = projectInfo.name,
      version = projectInfo.version,
      scalaVersion = projectInfo.scalaVersion,
      description = projectInfo.description,
      crossScalaVersions = projectInfo.crossScalaVersions,
      organization = projectInfo.organization,
      organizationName = projectInfo.organizationName,
      organizationHomepage = projectInfo.organizationHomepage,
      homepage = projectInfo.homepage,
      licenses = projectInfo.licenses,
      startYear = projectInfo.startYear,
      isSnapshot = projectInfo.isSnapshot,
      apiURL = projectInfo.apiURL,
      scmInfo = projectInfo.scmInfo,
      developers = projectInfo.developers,
      versionScheme = projectInfo.versionScheme,
      projectInfoNameFormal = projectInfo.projectInfoNameFormal
    )
  }

  /**
    * Computes file-level statistics by aggregating header, rollup, and declaration statistics.
    *
    * This method combines header details, rollup computations, and declaration-level member and method statistics from
    * the provided file metrics result to create a comprehensive view of the file's overall metrics.
    *
    * @param fileMetrics
    *   The file metrics result object containing aggregated file, method, and member-level metrics required to generate
    *   the file statistics.
    * @param projectBaseDir
    *   Optional base directory for computing relative file paths.
    * @return
    *   A `FileStats` instance encapsulating the file's computed header data, aggregated rollup metrics, and structured
    *   declaration statistics for methods and members.
    */
  private def fileStats(fileMetrics: FileMetrics, projectBaseDir: Option[java.io.File] = None): FileStats = {
    val members = fileMetrics.memberMetrics.map(memberStats)
    val methods = fileMetrics.methodMetrics.map(methodStats)
    val rollup = fileStatsRollup(fileMetrics)
    val metadata = fileStatsMetadata(fileMetrics, projectBaseDir)
    FileStats(
      metadata = metadata,
      rollup = rollup,
      memberStats = members,
      methodStats = methods
    )
  }

  /**
    * Aggregates statistics related to the provided file metrics into a rollup structure.
    *
    * @param fileMetrics
    *   An instance of FileMetrics containing metadata and metrics about the file, its members, and methods.
    * @return
    *   Rollup An aggregated statistics object encapsulating various metrics derived from the file metrics.
    */
  private def fileStatsRollup(fileMetrics: FileMetrics): Rollup = {
    val members = fileMetrics.memberMetrics.map(memberStats)
    val methods = fileMetrics.methodMetrics.map(methodStats)
    val loc = fileMetrics.metadata.linesOfCode
    val fileSize = fileMetrics.metadata.fileSizeBytes
    val combined = methods ++ members

    val coreStats = CoreStats(
      totalLoc = loc,
      totalFunctions = methods.size,
      totalPublicFunctions = methods.count(_.metadata.accessModifier == "public"),
      totalPrivateFunctions = methods.count(_.metadata.accessModifier == "private"),
      totalFileSizeBytes = fileSize,
      totalSymbols = combined.size,
      totalPublicSymbols = combined.count(_.metadata.accessModifier == "public"),
      totalPrivateSymbols = combined.count(_.metadata.accessModifier == "private"),
      totalNestedSymbols = combined.count(_.metadata.accessModifier == "local"),
      totalDocumentedPublicSymbols = combined.count(s => s.metadata.accessModifier == "public" && s.hasScaladoc),
      totalDeprecatedSymbols = combined.count(_.metadata.isDeprecated),
      totalDefsValsVars = aggregatedTotalDefsValsVars(methods, members),
      totalPublicDefsValsVars = aggregatedPublicDefsValsVars(methods, members)
    )

    Rollup(
      totalCount = 1,
      averageFileSizeBytes = fileSize,
      returnTypeExplicitness =
        if (coreStats.totalDefsValsVars > 0)
          (combined.map(_.inlineAndImplicitStats.explicitDefsValsVars).sum.toDouble /
            coreStats.totalDefsValsVars.toDouble) * 100.0
        else 0.0,
      publicReturnTypeExplicitness =
        if (coreStats.totalPublicDefsValsVars > 0)
          (combined.map(_.inlineAndImplicitStats.explicitPublicDefsValsVars).sum.toDouble /
            coreStats.totalPublicDefsValsVars.toDouble) * 100.0
        else 0.0,
      itemsWithLowDocumentation = filesWithLowDocumentation(Vector.empty),
      itemsWithHighComplexity = filesWithHighComplexity(Vector.empty),
      itemsWithHighNesting = filesWithHighNesting(Vector.empty),
      itemsWithHighBranchDensity = filesWithHighBranchDensity(Vector.empty),
      itemsWithHighPatternMatching = filesWithHighPatternMatching(Vector.empty),
      itemsWithHighParameterCount = filesWithHighParameterCount(Vector.empty),
      avgNestingDepth = avgNestingDepth(methods, members),
      maxNestingDepth = maxNestingDepth(methods, members),
      avgCyclomaticComplexity = avgCyclomaticComplexity(methods, members),
      maxCyclomaticComplexity = maxCyclomaticComplexity(methods, members),
      scalaDocCoveragePercentage = scalaDocCoverage(
        combined.count(_.metadata.accessModifier == "public"),
        combined.count(s => s.metadata.accessModifier == "public" && s.hasScaladoc)
      ),
      deprecatedSymbolsDensityPercentage = deprecatedSymbolsDensity(
        combined.size,
        combined.count(_.metadata.isDeprecated)
      ),
      coreStats = coreStats,
      inlineAndImplicitStats = combined.map(_.inlineAndImplicitStats).foldLeft(InlineAndImplicitStats())(_ + _),
      patternMatchingStats = combined.map(_.patternMatchingStats).foldLeft(PatternMatchingStats())(_ + _),
      branchDensityStats = combined.map(_.branchDensityStats).foldLeft(BranchDensityStats())(_ + _),
      parameterStats = methods.map(_.parameterStats).foldLeft(ParameterStats())(_ + _)
    )

  }

  /**
    * Aggregates the count of public `def`s, `val`s, and `var`s from the provided method and member statistics.
    *
    * @param methods
    *   A vector containing statistics of methods.
    * @param members
    *   A vector containing statistics of members.
    * @return
    *   The total count of public `def`s, `val`s, and `var`s.
    */
  private def aggregatedPublicDefsValsVars(methods: Vector[MethodStats], members: Vector[MemberStats]): Int = {
    val publicMemberValsVars = members.count(m =>
      (m.metadata.declarationType == "val" || m.metadata.declarationType == "var") && m.metadata.accessModifier == "public"
    )
    publicMemberValsVars + methods.count(_.metadata.accessModifier == "public")
  }

  /**
    * Aggregates the total count of definitions, `val`, and `var` declarations in the provided method and member
    * statistics.
    *
    * @param methods
    *   A vector of `MethodStats` representing method statistics.
    * @param members
    *   A vector of `MemberStats` representing member statistics.
    * @return
    *   The total count of definitions, `val`, and `var` declarations.
    */
  private def aggregatedTotalDefsValsVars(methods: Vector[MethodStats], members: Vector[MemberStats]): Int = {
    val memberValsVars = members.count(m => m.metadata.declarationType == "val" || m.metadata.declarationType == "var")
    memberValsVars + methods.size
  }

  /**
    * Computes and aggregates method-level statistics based on the provided method-level metrics.
    *
    * @param methodMetrics
    *   The metrics of the method to analyze, including information such as name, signature, lines of code, access
    *   modifiers, parameters, complexity, pattern matching, and branch density metrics.
    * @return
    *   A `MethodStats` instance containing computed statistics for the given method, such as cyclomatic complexity,
    *   nesting depth, parameter metrics, pattern matching details, branch density metrics, and additional metadata.
    */
  private def methodStats(methodMetrics: MethodMetrics): MethodStats = {
    MethodStats(
      metadata = methodMetrics.metadata,
      complexity = methodMetrics.complexity,
      hasScaladoc = methodMetrics.hasScaladoc,
      nestingDepth = methodMetrics.nestingDepth,
      parameterStats = parameterStatsFor(methodMetrics),
      inlineAndImplicitStats = inlineAndImplicitStatsFor(methodMetrics),
      patternMatchingStats = patternMatchingStatsFor(methodMetrics),
      branchDensityStats = branchDensityStatsFor(methodMetrics)
    )
  }

  /**
    * Generates statistical data about a member based on the provided member metrics.
    *
    * @param memberMetrics
    *   An object containing metrics data for a specific member, including metadata, complexity, nesting depth, and
    *   other characteristics.
    * @return
    *   A MemberStats object containing calculated statistics, such as complexity, nesting depth, Scaladoc presence, and
    *   other detailed metrics.
    */
  private def memberStats(memberMetrics: MemberMetrics): MemberStats = MemberStats(
    metadata = memberMetrics.metadata,
    complexity = memberMetrics.complexity,
    nestingDepth = memberMetrics.nestingDepth,
    hasScaladoc = memberMetrics.hasScaladoc,
    inlineAndImplicitStats = inlineAndImplicitStatsFor(memberMetrics),
    patternMatchingStats = patternMatchingStatsFor(memberMetrics),
    branchDensityStats = branchDensityStatsFor(memberMetrics)
  )

  /**
    * Computes parameter statistics for a given set of method metrics.
    *
    * @param methodMetrics
    *   The metrics of the method for which parameter statistics are to be calculated.
    * @return
    *   A ParameterStats instance containing statistics about the parameters of the given method.
    */
  private def parameterStatsFor(methodMetrics: MethodMetrics): ParameterStats = {
    ParameterStats(
      totalParams = methodMetrics.parameterMetrics.totalParams,
      paramLists = methodMetrics.parameterMetrics.paramLists,
      implicitParamLists = methodMetrics.parameterMetrics.implicitParamLists,
      usingParamLists = methodMetrics.parameterMetrics.usingParamLists,
      implicitParams = methodMetrics.parameterMetrics.implicitParams,
      usingParams = methodMetrics.parameterMetrics.usingParams,
      defaultedParams = methodMetrics.parameterMetrics.defaultedParams,
      byNameParams = methodMetrics.parameterMetrics.byNameParams,
      varargParams = methodMetrics.parameterMetrics.varargParams
    )
  }

  /**
    * Computes branch density statistics for a given member's metrics.
    *
    * @param symbol
    *   The metrics of the member for which branch density statistics are computed.
    * @return
    *   A BranchDensityStats instance containing detailed branch density metrics.
    */
  private def branchDensityStatsFor(symbol: Symbol): BranchDensityStats = BranchDensityStats(
    branches = symbol.branchDensityMetrics.branches,
    ifCount = symbol.branchDensityMetrics.ifCount,
    caseCount = symbol.branchDensityMetrics.caseCount,
    loopCount = symbol.branchDensityMetrics.loopCount,
    catchCaseCount = symbol.branchDensityMetrics.catchCaseCount,
    boolOpsCount = symbol.branchDensityMetrics.boolOpsCount,
    densityPer100 = symbol.branchDensityMetrics.densityPer100,
    boolOpsPer100 = symbol.branchDensityMetrics.boolOpsPer100
  )

  /**
    * Generates pattern matching statistics for a given symbol.
    *
    * @param symbol
    *   The symbol for which pattern matching statistics are to be generated.
    * @return
    *   A PatternMatchingStats object containing detailed metrics on pattern matching for the given symbol, such as
    *   matches, cases, guards, wildcards, max nesting, nested matches, and average cases per match.
    */
  private def patternMatchingStatsFor(symbol: Symbol): PatternMatchingStats = PatternMatchingStats(
    matches = symbol.patternMatchingMetrics.matches,
    cases = symbol.patternMatchingMetrics.cases,
    guards = symbol.patternMatchingMetrics.guards,
    wildcards = symbol.patternMatchingMetrics.wildcards,
    maxNesting = symbol.patternMatchingMetrics.maxNesting,
    nestedMatches = symbol.patternMatchingMetrics.nestedMatches,
    avgCasesPerMatch = symbol.patternMatchingMetrics.avgCasesPerMatch
  )

  /**
    * Computes and returns statistical metrics related to inline and implicit declarations associated with the given
    * symbol.
    *
    * @param symbol
    *   the symbol representing a program entity for which the inline and implicit statistics are to be calculated
    * @return
    *   an instance of InlineAndImplicitStats containing computed metrics, including counts of explicit definitions,
    *   explicit public definitions, inline methods, inline values, inline variables, given instances, given
    *   conversions, and more
    */
  private def inlineAndImplicitStatsFor(symbol: Symbol): InlineAndImplicitStats = {
    val md = symbol.metadata
    val m = symbol.inlineAndImplicitMetrics
    val decl = md.declarationType
    val isPublic = md.accessModifier == "public"
    val explicitCount = if (m.hasExplicitReturnType) 1 else 0
    val explicitPublicCount = if (isPublic && m.hasExplicitReturnType) 1 else 0
    val inlineMethodCount = if (m.hasInlineModifier && decl == "def") 1 else 0
    val inlineValCount = if (m.hasInlineModifier && decl == "val") 1 else 0
    val inlineVarCount = if (m.hasInlineModifier && decl == "var") 1 else 0
    val implicitValCount = if (m.isImplicit && decl == "val") 1 else 0
    val implicitVarCount = if (m.isImplicit && decl == "var") 1 else 0
    val implicitConvCount = if (m.isImplicitConversion) 1 else 0
    val givenInstCount = m.isGivenInstance.count(_ == true)
    val givenConvCount = m.isGivenConversion.count(_ == true)

    InlineAndImplicitStats(
      explicitDefsValsVars = explicitCount,
      explicitPublicDefsValsVars = explicitPublicCount,
      inlineMethods = inlineMethodCount,
      inlineVals = inlineValCount,
      inlineVars = inlineVarCount,
      inlineParams = m.inlineParamCount.getOrElse(0),
      implicitVals = implicitValCount,
      implicitVars = implicitVarCount,
      implicitConversions = implicitConvCount,
      givenInstances = givenInstCount,
      givenConversions = givenConvCount
    )
  }

  /**
    * Generates a file metadata containing key file metrics from the provided `FileMetrics`.
    *
    * This method extracts the essential file-level metadata such as project ID, file ID, file name, package name, lines
    * of code, and file size from a `FileMetrics` and wraps it into a `FileStatsMetadata` case class.
    *
    * @param fileMetrics
    *   The file metrics result containing metadata and metrics for the file, including project ID, file ID, file name,
    *   package name, lines of code, and file size in bytes.
    * @param projectBaseDir
    *   Optional base directory for computing relative file paths. If not provided, uses absolute path.
    * @return
    *   A `FileStatsHeader` object that captures the header-level metrics for the file and its associated project.
    */
  private def fileStatsMetadata(
      fileMetrics: FileMetrics,
      projectBaseDir: Option[java.io.File] = None
  ): FileStatsMetadata = {
    val file = fileMetrics.metadata.file

    // Compute a relative path if projectBaseDir is provided
    val relativePath = projectBaseDir match {
      case Some(baseDir) =>
        try {
          baseDir.toPath.relativize(file.toPath).toString
        } catch {
          case _: Exception => file.getPath // Fallback to an absolute path if relativization fails
        }
      case None => file.getPath
    }

    FileStatsMetadata(
      projectId = fileMetrics.metadata.projectId.getOrElse("N/A"),
      fileId = fileMetrics.metadata.fileId,
      fileName = file.getName,
      filePath = relativePath,
      packageName = fileMetrics.metadata.packageName,
      linesOfCode = fileMetrics.metadata.linesOfCode,
      fileSizeBytes = fileMetrics.metadata.fileSizeBytes
    )
  }

}
