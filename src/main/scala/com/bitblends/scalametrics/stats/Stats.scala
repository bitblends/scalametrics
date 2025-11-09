/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.stats

import com.bitblends.scalametrics.metrics.model._
import com.bitblends.scalametrics.stats.model._

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

    val fileStatsList: Vector[FileStats] = projectMetrics.fileMetrics.map(fm => fileStats(fm, projectBaseDir))
    val packageStatsList: Vector[PackageRollup] = fileStatsList
      .groupBy(_.header.packageName)
      .map { case (packageName: String, fileStatsInPackage: Vector[FileStats]) =>
        val packageLoc: Int = fileStatsInPackage.map(_.header.linesOfCode).sum
        packageStats(packageName, packageLoc, fileStatsInPackage.map(_.fileRollup))
      }
      .toVector

    val totalPackages = packageStatsList.size
    val fileRollupList = fileStatsList.map(_.fileRollup)
    val methodStatList = fileStatsList.flatMap(_.declarationStats.methodStats)
    val memberStatList = fileStatsList.flatMap(_.declarationStats.memberStats)

    // Core metrics
    val totalFiles = fileStatsList.size
    val totalLoc = fileStatsList.map(_.header.linesOfCode).foldLeft(0)(_ + _)
    val totalFileSizeBytes = fileStatsList.map(_.header.fileSizeBytes).foldLeft(0L)(_ + _)
    val averageFileSizeBytes = if (totalFiles > 0) totalFileSizeBytes / totalFiles else 0L

    // Function metrics
    val totalFunctions = methodStatList.size
    val totalPublicFunctions = methodStatList.count(_.accessModifier == "public")
    val totalPrivateFunctions = methodStatList.count(_.accessModifier == "private")

    // Symbol metrics
    val allSymbols = methodStatList ++ memberStatList
    val totalSymbols = allSymbols.size
    val totalPublicSymbols = allSymbols.count(_.accessModifier == "public")
    val totalPrivateSymbols = allSymbols.count(_.accessModifier == "private")
    val totalNestedSymbols = allSymbols.count(_.accessModifier == "local")
    val documentedPublicSymbols = allSymbols.count(s => s.accessModifier == "public" && s.hasScaladoc)
    val totalDeprecatedSymbols = allSymbols.count(_.isDeprecated)

    // Coverage metrics
    val scalaDocCoverage =
      if (totalPublicSymbols > 0)
        100.0 * documentedPublicSymbols.toDouble / totalPublicSymbols
      else 0.0
    val deprecatedSymbolsDensity =
      if (totalSymbols > 0)
        100.0 * totalDeprecatedSymbols.toDouble / totalSymbols
      else 0.0

    // Return type explicitness
    val totalDefsValsVars = fileRollupList.map(_.totalDefsValsVars).sum
    val totalPublicDefsValsVars = fileRollupList.map(_.totalPublicDefsValsVars).sum
    val explicitDefsValsVars = fileRollupList.map(_.explicitDefsValsVars).sum
    val explicitPublicDefsValsVars = fileRollupList.map(_.explicitPublicDefsValsVars).sum
    val returnTypeExplicitness =
      if (totalDefsValsVars > 0)
        100.0 * explicitDefsValsVars.toDouble / totalDefsValsVars
      else 0.0
    val publicReturnTypeExplicitness =
      if (totalPublicDefsValsVars > 0)
        100.0 * explicitPublicDefsValsVars.toDouble / totalPublicDefsValsVars
      else 0.0

    // Inline metrics
    val inlineMethods = fileRollupList.map(_.inlineMethods).sum
    val inlineVals = fileRollupList.map(_.inlineVals).sum
    val inlineVars = fileRollupList.map(_.inlineVars).sum
    val inlineParams = fileRollupList.map(_.inlineParams).sum

    // Implicit metrics
    val implicitVals = fileRollupList.map(_.implicitVals).sum
    val implicitVars = fileRollupList.map(_.implicitVars).sum
    val implicitConversions = fileRollupList.map(_.implicitConversions).sum

    // Given metrics
    val givenInstances = fileRollupList.map(_.givenInstances).sum
    val givenConversions = fileRollupList.map(_.givenConversions).sum

    // Pattern matching metrics
    val pmMatches = fileRollupList.map(_.pmMatches).sum
    val pmCases = fileRollupList.map(_.pmCases).sum
    val pmGuards = fileRollupList.map(_.pmGuards).sum
    val pmWildcards = fileRollupList.map(_.pmWildcards).sum
    val pmMaxNesting = if (fileRollupList.nonEmpty) fileRollupList.map(_.pmMaxNesting).max else 0
    val pmNestedMatches = fileRollupList.map(_.pmNestedMatches).sum

    // Branch density metrics
    val bdBranches = fileRollupList.map(_.bdBranches).sum
    val bdIfCount = fileRollupList.map(_.bdIfCount).sum
    val bdCaseCount = fileRollupList.map(_.bdCaseCount).sum
    val bdLoopCount = fileRollupList.map(_.bdLoopCount).sum
    val bdCatchCaseCount = fileRollupList.map(_.bdCatchCaseCount).sum
    val bdBoolOpsCount = fileRollupList.map(_.bdBoolOpsCount).sum
    val bdDensityPer100 = if (totalLoc == 0) 0.0 else 100.0 * bdBranches.toDouble / totalLoc
    val bdBoolOpsPer100 = if (totalLoc == 0) 0.0 else 100.0 * bdBoolOpsCount.toDouble / totalLoc

    // Complexity metrics
    val methodComplexities = methodStatList.map(_.cComplexity)
    val avgCyclomaticComplexity =
      if (methodComplexities.nonEmpty)
        methodComplexities.sum.toDouble / methodComplexities.size
      else 0.0
    val maxCyclomaticComplexity = if (methodComplexities.nonEmpty) methodComplexities.max else 0

    val methodNestingDepths = methodStatList.map(_.nestingDepth)
    val avgNestingDepth =
      if (methodNestingDepths.nonEmpty)
        methodNestingDepths.sum.toDouble / methodNestingDepths.size
      else 0.0
    val maxNestingDepth = if (methodNestingDepths.nonEmpty) methodNestingDepths.max else 0

    // Count packages with high complexity (threshold: avg complexity > 10)
    val packagesWithHighComplexity = packageStatsList.count { pkg =>
      val pkgMethods: Vector[MethodStats] = fileStatsList
        .filter(_.header.packageName == pkg.name)
        .flatMap(_.declarationStats.methodStats)
      if (pkgMethods.nonEmpty) {
        val avgComplexity = pkgMethods.map(_.cComplexity).sum.toDouble / pkgMethods.size
        avgComplexity > 10.0
      } else false
    }

    // Count packages with low documentation (threshold: < 50% coverage)
    val packagesWithLowDocumentation = packageStatsList.count { pkg =>
      val pkgPublicSymbols = fileStatsList
        .filter(_.header.packageName == pkg.name)
        .flatMap(f => f.declarationStats.methodStats ++ f.declarationStats.memberStats)
        .count(_.accessModifier == "public")
      val pkgDocumentedSymbols = fileStatsList
        .filter(_.header.packageName == pkg.name)
        .flatMap(f => f.declarationStats.methodStats ++ f.declarationStats.memberStats)
        .count(s => s.accessModifier == "public" && s.hasScaladoc)
      if (pkgPublicSymbols > 0) {
        val coverage = 100.0 * pkgDocumentedSymbols.toDouble / pkgPublicSymbols
        coverage < 50.0
      } else false
    }

    val projectStats = ProjectRollup(
      // Core metrics
      totalFiles = totalFiles,
      totalLoc = totalLoc,
      totalFunctions = totalFunctions,
      totalPublicFunctions = totalPublicFunctions,
      totalPrivateFunctions = totalPrivateFunctions,
      averageFileSizeBytes = averageFileSizeBytes,
      totalFileSizeBytes = totalFileSizeBytes,
      // Symbol metrics
      totalSymbols = totalSymbols,
      totalPublicSymbols = totalPublicSymbols,
      totalPrivateSymbols = totalPrivateSymbols,
      totalNestedSymbols = totalNestedSymbols,
      documentedPublicSymbols = documentedPublicSymbols,
      totalDeprecatedSymbols = totalDeprecatedSymbols,
      // Coverage metrics
      scalaDocCoverage = scalaDocCoverage,
      deprecatedSymbolsDensity = deprecatedSymbolsDensity,
      // Return type explicitness
      totalDefsValsVars = totalDefsValsVars,
      totalPublicDefsValsVars = totalPublicDefsValsVars,
      explicitDefsValsVars = explicitDefsValsVars,
      explicitPublicDefsValsVars = explicitPublicDefsValsVars,
      returnTypeExplicitness = returnTypeExplicitness,
      publicReturnTypeExplicitness = publicReturnTypeExplicitness,
      // Inline metrics
      inlineMethods = inlineMethods,
      inlineVals = inlineVals,
      inlineVars = inlineVars,
      inlineParams = inlineParams,
      // Implicit metrics
      implicitVals = implicitVals,
      implicitVars = implicitVars,
      implicitConversions = implicitConversions,
      // Given metrics
      givenInstances = givenInstances,
      givenConversions = givenConversions,
      // Pattern matching metrics
      pmMatches = pmMatches,
      pmCases = pmCases,
      pmGuards = pmGuards,
      pmWildcards = pmWildcards,
      pmMaxNesting = pmMaxNesting,
      pmNestedMatches = pmNestedMatches,
      // Branch density metrics
      bdBranches = bdBranches,
      bdIfCount = bdIfCount,
      bdCaseCount = bdCaseCount,
      bdLoopCount = bdLoopCount,
      bdCatchCaseCount = bdCatchCaseCount,
      bdBoolOpsCount = bdBoolOpsCount,
      bdDensityPer100 = bdDensityPer100,
      bdBoolOpsPer100 = bdBoolOpsPer100,
      // Complexity metrics
      avgCyclomaticComplexity = avgCyclomaticComplexity,
      maxCyclomaticComplexity = maxCyclomaticComplexity,
      avgNestingDepth = avgNestingDepth,
      maxNestingDepth = maxNestingDepth,
      // Package-level summary
      totalPackages = totalPackages,
      packagesWithHighComplexity = packagesWithHighComplexity,
      packagesWithLowDocumentation = packagesWithLowDocumentation
    )

    ProjectStats(
      header = projectMetadata(projectMetrics.projectInfo),
      projectRollup = projectStats,
      packages = packageStatsList.map(pkgStat =>
        Package(
          packageRollup = pkgStat,
          fileStats = fileStatsList
        )
      )
    )

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
  private def projectMetadata(projectInfo: ProjectInfo): ProjectStatsHeader = {
    ProjectStatsHeader(
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
    * @param fileMetricsResult
    *   The file metrics result object containing aggregated file, method, and member-level metrics required to generate
    *   the file statistics.
    * @param projectBaseDir
    *   Optional base directory for computing relative file paths.
    * @return
    *   A `FileStats` instance encapsulating the file's computed header data, aggregated rollup metrics, and structured
    *   declaration statistics for methods and members.
    */
  def fileStats(fileMetricsResult: FileMetricsResult, projectBaseDir: Option[java.io.File] = None): FileStats = {
    FileStats(
      header = fileStatsHeader(fileMetricsResult, projectBaseDir),
      fileRollup = fileRollup(fileMetricsResult),
      DeclarationStats(
        fileMetricsResult.memberMetrics.map(memberStats),
        fileMetricsResult.methodMetrics.map(methodStats)
      )
    )
  }

  /**
    * Computes and converts the given member-level metrics into a structured format.
    *
    * This method takes metrics of a code member (such as a field, method, or class) and returns a `MemberStats` object,
    * which organizes and aggregates the input metrics for further analysis or reporting.
    *
    * @param memberMetrics
    *   The metrics of the member to analyze, including properties such as file ID, name, type, access modifiers, lines
    *   of code, Scaladoc presence, deprecation status, complexity, nesting depth, explicitness, implicitness, and
    *   pattern matching metrics.
    * @return
    *   A `MemberStats` instance containing aggregated statistics for the given member, such as cyclomatic complexity,
    *   nesting depth, inline and implicit information, pattern matching specifics, and branch density metrics.
    */
  def memberStats(memberMetrics: MemberMetrics): MemberStats = {
    MemberStats(
      fileId = memberMetrics.fileId,
      name = memberMetrics.name,
      memberType = memberMetrics.memberType,
      signature = memberMetrics.signature,
      accessModifier = memberMetrics.accessModifier,
      linesOfCode = memberMetrics.linesOfCode,
      hasScaladoc = memberMetrics.hasScaladoc,
      isDeprecated = memberMetrics.isDeprecated,
      // Cyclomatic complexity
      cComplexity = memberMetrics.cComplexity,
      // Nesting depth
      nestingDepth = memberMetrics.nestingDepth,
      // inline
      hasInlineModifier = memberMetrics.hasInlineModifier,
      // given
      isGivenInstance = memberMetrics.isGivenInstance,
      isGivenConversion = memberMetrics.isGivenConversion,
      // implicit
      isImplicit = memberMetrics.isImplicit,
      // explicitness
      isAbstract = memberMetrics.isAbstract,
      hasExplicitReturnType = memberMetrics.hasExplicitReturnType,
      inferredReturnType = memberMetrics.inferredReturnType,
      // Pattern matching metrics
      pmMatches = memberMetrics.pmMatches,
      pmCases = memberMetrics.pmCases,
      pmGuards = memberMetrics.pmGuards,
      pmWildcards = memberMetrics.pmWildcards,
      pmMaxNesting = memberMetrics.pmMaxNesting,
      pmNestedMatches = memberMetrics.pmNestedMatches,
      pmAvgCasesPerMatch = memberMetrics.pmAvgCasesPerMatch,
      // Branch density metrics
      bdBranches = memberMetrics.bdBranches,
      bdIfCount = memberMetrics.bdIfCount,
      bdCaseCount = memberMetrics.bdCaseCount,
      bdLoopCount = memberMetrics.bdLoopCount,
      bdCatchCaseCount = memberMetrics.bdCatchCaseCount,
      bdBoolOpsCount = memberMetrics.bdBoolOpsCount,
      bdDensityPer100 = memberMetrics.bdDensityPer100,
      bdBoolOpsPer100 = memberMetrics.bdBoolOpsPer100
    )
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
  def methodStats(methodMetrics: MethodMetrics): MethodStats = {
    MethodStats(
      fileId = methodMetrics.fileId,
      name = methodMetrics.name,
      signature = methodMetrics.signature,
      accessModifier = methodMetrics.accessModifier,
      linesOfCode = methodMetrics.linesOfCode,
      isNested = methodMetrics.isNested,
      hasScaladoc = methodMetrics.hasScaladoc,
      isDeprecated = methodMetrics.isDeprecated,
      parentMember = methodMetrics.parentMember,
      // Cyclomatic complexity
      cComplexity = methodMetrics.cComplexity,
      // Nesting depth
      nestingDepth = methodMetrics.nestingDepth,
      // Parameter metrics
      totalParams = methodMetrics.totalParams,
      paramLists = methodMetrics.paramLists,
      implicitParamLists = methodMetrics.implicitParamLists,
      usingParamLists = methodMetrics.usingParamLists,
      implicitParams = methodMetrics.implicitParams,
      usingParams = methodMetrics.usingParams,
      defaultedParams = methodMetrics.defaultedParams,
      byNameParams = methodMetrics.byNameParams,
      varargParams = methodMetrics.varargParams,
      // inline
      hasInlineModifier = methodMetrics.hasInlineModifier,
      inlineParamCount = methodMetrics.inlineParamCount,
      // implicit
      isImplicitConversion = methodMetrics.isImplicitConversion,
      // explicitness
      isAbstract = methodMetrics.isAbstract,
      hasExplicitReturnType = methodMetrics.hasExplicitReturnType,
      inferredReturnType = methodMetrics.inferredReturnType,
      // Pattern matching metrics
      pmMatches = methodMetrics.pmMatches,
      pmCases = methodMetrics.pmCases,
      pmGuards = methodMetrics.pmGuards,
      pmWildcards = methodMetrics.pmWildcards,
      pmMaxNesting = methodMetrics.pmMaxNesting,
      pmNestedMatches = methodMetrics.pmNestedMatches,
      pmAvgCasesPerMatch = methodMetrics.pmAvgCasesPerMatch,
      // Branch density metrics
      bdBranches = methodMetrics.bdBranches,
      bdIfCount = methodMetrics.bdIfCount,
      bdCaseCount = methodMetrics.bdCaseCount,
      bdLoopCount = methodMetrics.bdLoopCount,
      bdCatchCaseCount = methodMetrics.bdCatchCaseCount,
      bdBoolOpsCount = methodMetrics.bdBoolOpsCount,
      bdDensityPer100 = methodMetrics.bdDensityPer100,
      bdBoolOpsPer100 = methodMetrics.bdBoolOpsPer100
    )
  }

  /**
    * Generates file-level statistics by aggregating metrics from method and member metrics.
    *
    * @param fileMetricsResult
    *   The metrics result object containing file-level, method-level, and member-level metrics to compute the
    *   aggregated statistics.
    * @return
    *   A `FileStats` instance containing aggregated and computed statistics for the file, including lines of code,
    *   public/private symbol counts, return type explicitness, inline/implicit metrics, and pattern matching
    *   statistics.
    */
  def fileRollup(fileMetricsResult: FileMetricsResult): FileRollup = {
    // rollup member and method stats
    val totalDefsValsVars = fileMetricsResult.memberMetrics.count(m =>
      m.memberType == "val" || m.memberType == "var"
    ) + fileMetricsResult.methodMetrics.size
    val totalPublicDefsValsVars = fileMetricsResult.memberMetrics.count(m =>
      m.memberType == "val" || m.memberType == "var" && m.accessModifier == "public"
    ) +
      fileMetricsResult.methodMetrics.count(_.accessModifier == "public")
    val explicitDefsValsVars = fileMetricsResult.methodMetrics.count(
      _.hasExplicitReturnType
    ) + fileMetricsResult.memberMetrics.count(_.hasExplicitReturnType)
    val explicitPublicDefsValsVars =
      fileMetricsResult.methodMetrics.count(m => m.hasExplicitReturnType && m.accessModifier == "public") + // defs
        fileMetricsResult.memberMetrics.count(m =>
          (m.memberType == "val" || m.memberType == "var") && m.hasExplicitReturnType && m.accessModifier == "public"
        ) // vals/vars

    FileRollup(
      // core metrics
      loc = fileMetricsResult.fileMetrics.linesOfCode,
      totalFunctions = fileMetricsResult.methodMetrics.size,
      totalPublicFunctions = fileMetricsResult.methodMetrics.count(x => x.accessModifier == "public"),
      totalPrivateFunctions = fileMetricsResult.methodMetrics.count(x => x.accessModifier == "private"),
      fileSizeBytes = fileMetricsResult.fileMetrics.fileSizeBytes,
      totalSymbols = fileMetricsResult.memberMetrics.size + fileMetricsResult.methodMetrics.size,
      totalPublicSymbols =
        (fileMetricsResult.memberMetrics ++ fileMetricsResult.methodMetrics).count(_.accessModifier == "public"),
      totalPrivateSymbols =
        (fileMetricsResult.memberMetrics ++ fileMetricsResult.methodMetrics).count(_.accessModifier == "private"),
      totalNestedSymbols =
        (fileMetricsResult.memberMetrics ++ fileMetricsResult.methodMetrics).count(_.accessModifier == "local"),
      documentedPublicSymbols = (fileMetricsResult.memberMetrics ++ fileMetricsResult.methodMetrics).count(m =>
        m.accessModifier == "public" && m.hasScaladoc
      ),
      totalDeprecatedSymbols =
        (fileMetricsResult.memberMetrics ++ fileMetricsResult.methodMetrics).count(_.isDeprecated),
      // return type explicitness
      totalDefsValsVars = fileMetricsResult.memberMetrics.count(m =>
        m.memberType == "val" || m.memberType == "var"
      ) + fileMetricsResult.methodMetrics.size,
      totalPublicDefsValsVars = fileMetricsResult.memberMetrics.count(m =>
        m.memberType == "val" || m.memberType == "var" && m.accessModifier == "public"
      ) +
        fileMetricsResult.methodMetrics.count(_.accessModifier == "public"),
      explicitDefsValsVars = fileMetricsResult.methodMetrics.count(
        _.hasExplicitReturnType
      ) + fileMetricsResult.memberMetrics.count(_.hasExplicitReturnType),
      explicitPublicDefsValsVars =
        fileMetricsResult.methodMetrics.count(m => m.hasExplicitReturnType && m.accessModifier == "public") + // defs
          fileMetricsResult.memberMetrics.count(m =>
            (m.memberType == "val" || m.memberType == "var") && m.hasExplicitReturnType && m.accessModifier == "public" // vals/vars
          ),
      returnTypeExplicitness =
        if (totalDefsValsVars > 0) (explicitDefsValsVars.toDouble / totalDefsValsVars.toDouble) * 100.0 else 0.0,
      publicReturnTypeExplicitness =
        if (totalPublicDefsValsVars > 0)
          (explicitPublicDefsValsVars.toDouble / totalPublicDefsValsVars.toDouble) * 100.0
        else 0.0,
      // inline
      inlineMethods = fileMetricsResult.methodMetrics.count(_.hasInlineModifier),
      inlineVals = fileMetricsResult.memberMetrics.count(m => m.hasInlineModifier && m.memberType == "val"),
      inlineVars = fileMetricsResult.memberMetrics.count(m => m.hasInlineModifier && m.memberType == "var"),
      inlineParams = fileMetricsResult.methodMetrics.map(_.inlineParamCount).sum,
      // implicit
      implicitVals = fileMetricsResult.memberMetrics.count(m => m.memberType == "val" && m.isImplicit),
      implicitVars = fileMetricsResult.memberMetrics.count(m => m.memberType == "var" && m.isImplicit),
      implicitConversions = fileMetricsResult.methodMetrics.count(_.isImplicitConversion),
      givenInstances = fileMetricsResult.memberMetrics.count(_.isGivenInstance),
      givenConversions = fileMetricsResult.memberMetrics.count(_.isGivenConversion),
      // Pattern Matching
      pmMatches =
        fileMetricsResult.methodMetrics.map(_.pmMatches).sum + fileMetricsResult.memberMetrics.map(_.pmMatches).sum,
      pmCases = fileMetricsResult.methodMetrics.map(_.pmCases).sum + fileMetricsResult.memberMetrics.map(_.pmCases).sum,
      pmGuards =
        fileMetricsResult.methodMetrics.map(_.pmGuards).sum + fileMetricsResult.memberMetrics.map(_.pmGuards).sum,
      pmWildcards = fileMetricsResult.methodMetrics.map(_.pmWildcards).sum +
        fileMetricsResult.memberMetrics.map(_.pmWildcards).sum,
      pmMaxNesting = fileMetricsResult.methodMetrics.map(_.pmMaxNesting).sum +
        fileMetricsResult.memberMetrics.map(_.pmMaxNesting).sum,
      pmNestedMatches = fileMetricsResult.methodMetrics.map(_.pmNestedMatches).sum +
        fileMetricsResult.memberMetrics.map(_.pmNestedMatches).sum,
      // Branch Density
      bdBranches = fileMetricsResult.methodMetrics.map(_.bdBranches).sum +
        fileMetricsResult.memberMetrics.map(_.bdBranches).sum,
      bdIfCount = fileMetricsResult.methodMetrics.map(_.bdIfCount).sum +
        fileMetricsResult.memberMetrics.map(_.bdIfCount).sum,
      bdCaseCount = fileMetricsResult.methodMetrics.map(_.bdCaseCount).sum +
        fileMetricsResult.memberMetrics.map(_.bdCaseCount).sum,
      bdLoopCount = fileMetricsResult.methodMetrics.map(_.bdLoopCount).sum +
        fileMetricsResult.memberMetrics.map(_.bdLoopCount).sum,
      bdCatchCaseCount = fileMetricsResult.methodMetrics.map(_.bdCatchCaseCount).sum +
        fileMetricsResult.memberMetrics.map(_.bdCatchCaseCount).sum,
      bdBoolOpsCount = fileMetricsResult.methodMetrics.map(_.bdBoolOpsCount).sum +
        fileMetricsResult.memberMetrics.map(_.bdBoolOpsCount).sum,
      // Calculate density metrics based on file's total lines of code
      bdDensityPer100 = {
        val totalBranches = fileMetricsResult.methodMetrics.map(_.bdBranches).sum +
          fileMetricsResult.memberMetrics.map(_.bdBranches).sum
        val loc = fileMetricsResult.fileMetrics.linesOfCode
        if (loc == 0) 0.0 else 100.0 * totalBranches.toDouble / loc
      },
      bdBoolOpsPer100 = {
        val totalBoolOps = fileMetricsResult.methodMetrics.map(_.bdBoolOpsCount).sum +
          fileMetricsResult.memberMetrics.map(_.bdBoolOpsCount).sum
        val loc = fileMetricsResult.fileMetrics.linesOfCode
        if (loc == 0) 0.0 else 100.0 * totalBoolOps.toDouble / loc
      }
    )
  }

  /**
    * Generates a header containing key file metrics from the provided `FileMetricsResult`.
    *
    * This method extracts the essential file-level metadata such as project ID, file ID, file name, package name, lines
    * of code, and file size from a `FileMetricsResult` and wraps it into a `FileStatsHeader` object.
    *
    * @param fileMetricsResult
    *   The file metrics result containing metadata and metrics for the file, including project ID, file ID, file name,
    *   package name, lines of code, and file size in bytes.
    * @param projectBaseDir
    *   Optional base directory for computing relative file paths. If not provided, uses absolute path.
    * @return
    *   A `FileStatsHeader` object that captures the header-level metrics for the file and its associated project.
    */
  def fileStatsHeader(
      fileMetricsResult: FileMetricsResult,
      projectBaseDir: Option[java.io.File] = None
  ): FileStatsHeader = {
    val file = fileMetricsResult.fileMetrics.file

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

    FileStatsHeader(
      projectId = fileMetricsResult.fileMetrics.projectId.getOrElse("N/A"),
      fileId = fileMetricsResult.fileMetrics.fileId,
      fileName = file.getName,
      filePath = relativePath,
      packageName = fileMetricsResult.fileMetrics.packageName,
      linesOfCode = fileMetricsResult.fileMetrics.linesOfCode,
      fileSizeBytes = fileMetricsResult.fileMetrics.fileSizeBytes
    )
  }

  /**
    * Computes package-level statistics by aggregating data from multiple file-level rollups.
    *
    * This method collects various metrics from a list of `FileRollup` instances, such as function counts, branch
    * density, implicit and inline declarations, pattern matching constructs, and more, to summarize the overall
    * statistics for a package.
    *
    * @param packageName
    *   The name of the package for which statistics are being computed.
    * @param packageLoc
    *   The total lines of code (LOC) within the package, used to normalize certain metrics (e.g., branch density).
    * @param fileStatsList
    *   A vector of `FileRollup` instances containing aggregated statistics for files within the package.
    * @return
    *   A `PackageStats` object representing the aggregated statistics for the package, including function counts,
    *   inline and implicit metric details, branch density statistics, and pattern matching metrics.
    */
  def packageStats(packageName: String, packageLoc: Int, fileStatsList: Vector[FileRollup]): PackageRollup = {
    // Aggregate branch density metrics
    val totalBranches = fileStatsList.map(_.bdBranches).sum
    val totalBoolOps = fileStatsList.map(_.bdBoolOpsCount).sum

    // Aggregate FileStats into PackageStats
    PackageRollup(
      name = packageName,
      totalFunctions = fileStatsList.map(_.totalFunctions).sum,
      publicFunctions = fileStatsList.map(_.totalPublicFunctions).sum,
      privateFunctions = fileStatsList.map(_.totalPrivateFunctions).sum,
      // Inline metrics
      inlineMethods = fileStatsList.map(_.inlineMethods).sum,
      inlineVals = fileStatsList.map(_.inlineVals).sum,
      inlineVars = fileStatsList.map(_.inlineVars).sum,
      inlineParams = fileStatsList.map(_.inlineParams).sum,
      // Implicit metrics (Note: PackageStats doesn't have implicitDefs field)
      implicitVals = fileStatsList.map(_.implicitVals).sum,
      implicitVars = fileStatsList.map(_.implicitVars).sum,
      implicitConversions = fileStatsList.map(_.implicitConversions).sum,
      // Given metrics
      givenInstances = fileStatsList.map(_.givenInstances).sum,
      givenConversions = fileStatsList.map(_.givenConversions).sum,
      // Pattern matching metrics
      pmMatches = fileStatsList.map(_.pmMatches).sum,
      pmCases = fileStatsList.map(_.pmCases).sum,
      pmGuards = fileStatsList.map(_.pmGuards).sum,
      pmWildcards = fileStatsList.map(_.pmWildcards).sum,
      pmMaxNesting = if (fileStatsList.nonEmpty) fileStatsList.map(_.pmMaxNesting).max else 0,
      pmNestedMatches = fileStatsList.map(_.pmNestedMatches).sum,
      // Branch density metrics
      bdBranches = totalBranches,
      bdIfCount = fileStatsList.map(_.bdIfCount).sum,
      bdCaseCount = fileStatsList.map(_.bdCaseCount).sum,
      bdLoopCount = fileStatsList.map(_.bdLoopCount).sum,
      bdCatchCaseCount = fileStatsList.map(_.bdCatchCaseCount).sum,
      bdBoolOpsCount = totalBoolOps,
      bdDensityPer100 = if (packageLoc == 0) 0.0 else 100.0 * totalBranches.toDouble / packageLoc,
      bdBoolOpsPer100 = if (packageLoc == 0) 0.0 else 100.0 * totalBoolOps.toDouble / packageLoc
    )
  }

}
