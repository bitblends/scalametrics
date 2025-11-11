/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.stats.model

/**
  * Represents the statistical summary of a package, including metrics related to functions, inline and implicit usage,
  * pattern matching, and branch density. This case class aggregates multiple metrics into a unified structure to
  * facilitate analysis of a package's overall complexity, density, and patterns of usage.
  *
  * @param name
  *   The name of the package being analyzed.
  * @param totalFunctions
  *   The total number of functions (both public and private) within the package.
  * @param publicFunctions
  *   The number of public functions in the package.
  * @param privateFunctions
  *   The number of private functions in the package.
  * @param inlineAndImplicitStats
  *   A collection of metrics detailing the usage of inline methods, implicit variables, and given instances.
  * @param patternMatchingStats
  *   A collection of statistics covering pattern matching constructs in the package, including matches, cases, guards,
  *   and nesting levels.
  * @param branchDensityStats
  *   Statistics regarding branch density, including conditional statements, loop counts, and boolean operations.
  */
case class PackageRollup(
    // core package metrics
    name: String,
    totalFunctions: Int,
    publicFunctions: Int,
    privateFunctions: Int,
    // Inline and implicit/given metrics
    inlineAndImplicitStats: InlineAndImplicitStats,
    // Pattern matching metrics
    patternMatchingStats: PatternMatchingStats,
    // Branch density metrics
    branchDensityStats: BranchDensityStats
) extends StatsBase {

  /**
    * Returns a formatted string containing various statistical metrics and summaries related to a package. The output
    * includes details about total functions, inline usage, implicit usage, given instances, pattern matching metrics,
    * and branch density metrics.
    *
    * @return
    *   A formatted string representing the package statistics and metrics, organized in a human-readable structure.
    */
  def prettify: String = {
    f"""Package: $name
       |Total Functions: $totalFunctions
       |  Public Functions: $publicFunctions
       |  Private Functions: $privateFunctions
       |
       |Inline Usage:
       |  Inline Methods: ${inlineAndImplicitStats.inlineMethods}
       |  Inline Vals: ${inlineAndImplicitStats.inlineVals}
       |  Inline Vars: ${inlineAndImplicitStats.inlineVars}
       |  Inline Params: ${inlineAndImplicitStats.inlineParams}
       |
       |Implicit/Given Usage:
       |  Implicit Vals: ${inlineAndImplicitStats.implicitVals}
       |  Implicit Vars: ${inlineAndImplicitStats.implicitVars}
       |  Implicit Conversions: ${inlineAndImplicitStats.implicitConversions}
       |
       |Given Usage:
       |
       |  Given Instances: ${inlineAndImplicitStats.givenInstances}
       |  Given Conversions: ${inlineAndImplicitStats.givenConversions}
       |
       |Pattern Matching Metrics:
       |  Matches: ${patternMatchingStats.matches}
       |  Cases: ${patternMatchingStats.cases}
       |  Guards: ${patternMatchingStats.guards}
       |  Wildcards: ${patternMatchingStats.wildcards}
       |  Max Nesting Level: ${patternMatchingStats.maxNesting}
       |  Nested Matches: ${patternMatchingStats.nestedMatches}
       |
       |Branch Density Metrics:
       |  Branches: ${branchDensityStats.branches}
       |  Ifs: ${branchDensityStats.ifCount}
       |  Cases: ${branchDensityStats.caseCount}
       |  Loops: ${branchDensityStats.loopCount}
       |  Catch Cases: ${branchDensityStats.catchCaseCount}
       |  Boolean Operations: ${branchDensityStats.boolOpsCount}
       |  Density per 100 LOC: ${"%.2f".format(branchDensityStats.densityPer100)}
       |  Boolean Ops per 100 LOC: ${"%.2f".format(branchDensityStats.boolOpsPer100)}
       |""".stripMargin
  }

}
