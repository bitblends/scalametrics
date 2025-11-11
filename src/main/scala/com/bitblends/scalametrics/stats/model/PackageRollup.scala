/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.stats.model

/**
  * Represents a summary of statistical metrics for a package, including function counts, inline usage, implicit
  * metrics, given definitions, pattern matching metrics, and branch density metrics.
  *
  * @param name
  *   The name of the package.
  * @param totalFunctions
  *   The total number of functions defined in the package.
  * @param publicFunctions
  *   The number of public functions in the package.
  * @param privateFunctions
  *   The number of private functions in the package.
  * @param inlineMethods
  *   The number of inline methods defined in the package.
  * @param inlineVals
  *   The number of inline values defined in the package.
  * @param inlineVars
  *   The number of inline variables defined in the package.
  * @param inlineParams
  *   The number of inline parameters used in the package.
  * @param implicitVals
  *   The number of implicit values defined in the package.
  * @param implicitVars
  *   The number of implicit variables defined in the package.
  * @param implicitConversions
  *   The number of implicit conversions defined in the package.
  * @param givenInstances
  *   The number of `given` instances defined in the package.
  * @param givenConversions
  *   The number of `given` conversions defined in the package.
  * @param pmMatches
  *   The total number of pattern matches in the package.
  * @param pmCases
  *   The total number of pattern matching cases in the package.
  * @param pmGuards
  *   The total number of guards in pattern matches in the package.
  * @param pmWildcards
  *   The total number of wildcards used in pattern matches in the package.
  * @param pmMaxNesting
  *   The maximum nesting level of pattern matches in the package.
  * @param pmNestedMatches
  *   The total number of nested pattern matches in the package.
  * @param bdBranches
  *   The total number of branch points in the package.
  * @param bdIfCount
  *   The number of `if` statements in the package.
  * @param bdCaseCount
  *   The number of `case` statements in the package.
  * @param bdLoopCount
  *   The number of loops (e.g., `for`, `while`) in the package.
  * @param bdCatchCaseCount
  *   The number of `catch` cases in the package.
  * @param bdBoolOpsCount
  *   The number of boolean operators (e.g., `&&`, `||`) in the package.
  * @param bdDensityPer100
  *   The branch density per 100 lines of code in the package.
  * @param bdBoolOpsPer100
  *   The number of boolean operations per 100 lines of code in the package.
  */
case class PackageRollup(
    name: String,
    totalFunctions: Int,
    publicFunctions: Int,
    privateFunctions: Int,

    // Inline and implicit/given metrics
    inlineMethods: Int,
    inlineVals: Int,
    inlineVars: Int,
    inlineParams: Int,
    // implicit metrics
    implicitVals: Int,
    implicitVars: Int,
    implicitConversions: Int,
    // given
    givenInstances: Int,
    givenConversions: Int,

    // Pattern matching metrics
    pmMatches: Int,
    pmCases: Int,
    pmGuards: Int,
    pmWildcards: Int,
    pmMaxNesting: Int,
    pmNestedMatches: Int,

    // Branch density metrics
    bdBranches: Int,
    bdIfCount: Int,
    bdCaseCount: Int,
    bdLoopCount: Int,
    bdCatchCaseCount: Int,
    bdBoolOpsCount: Int,
    bdDensityPer100: Double,
    bdBoolOpsPer100: Double
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
       |  Inline Methods: $inlineMethods
       |  Inline Vals: $inlineVals
       |  Inline Vars: $inlineVars
       |  Inline Params: $inlineParams
       |
       |Implicit Usage:
       |  Implicit Vals: $implicitVals
       |  Implicit Vars: $implicitVars
       |  Implicit Conversions: $implicitConversions
       |
       |Given Usage:
       |  Given Instances: $givenInstances
       |  Given Conversions: $givenConversions
       |
       |Pattern Matching Metrics:
       |  Matches: $pmMatches
       |  Cases: $pmCases
       |  Guards: $pmGuards
       |  Wildcards: $pmWildcards
       |  Max Nesting Level: $pmMaxNesting
       |  Nested Matches: $pmNestedMatches
       |
       |Branch Density Metrics:
       |  Total Branches: $bdBranches
       |  If Statements: $bdIfCount
       |  Case Statements: $bdCaseCount
       |  Loops: $bdLoopCount
       |  Catch Cases: $bdCatchCaseCount
       |  Boolean Operations: $bdBoolOpsCount
       |  Branch Density per 100 LOC: ${"%.2f".format(bdDensityPer100)}
       |  Boolean Ops per 100 LOC: ${"%.2f".format(bdBoolOpsPer100)}
       |""".stripMargin
  }

}
