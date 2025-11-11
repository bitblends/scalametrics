/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.stats.model

/**
  * Represents aggregated metrics and statistics for a file in a codebase.
  *
  * The class includes high-level statistics such as lines of code (LOC), function counts, symbol information, file
  * size, inline and implicit/given usage, pattern matching details, and branch density metrics.
  *
  * These metrics are intended to provide insights into various aspects of the file, including its structure,
  * complexity, coding patterns, and adherence to best practices.
  *
  * @param loc
  *   The number of lines of code in the file.
  * @param totalFunctions
  *   The total number of functions defined in the file.
  * @param totalPublicFunctions
  *   The total number of public functions in the file.
  * @param totalPrivateFunctions
  *   The total number of private functions in the file.
  * @param fileSizeBytes
  *   The size of the file in bytes.
  * @param totalSymbols
  *   The total number of symbols defined in the file.
  * @param totalPublicSymbols
  *   The total number of public symbols in the file.
  * @param totalPrivateSymbols
  *   The total number of private symbols in the file.
  * @param totalNestedSymbols
  *   The total number of nested symbols defined in the file.
  * @param documentedPublicSymbols
  *   The number of public symbols in the file that are documented.
  * @param totalDeprecatedSymbols
  *   The total number of deprecated symbols in the file.
  * @param totalDefsValsVars
  *   The total number of defs, vals, and vars in the file.
  * @param totalPublicDefsValsVars
  *   The total number of public defs, vals, and vars in the file.
  * @param inlineAndImplicitStats
  *   Metrics related to inline and implicit/given usage within the file.
  * @param patternMatchingStats
  *   Statistics for pattern matching constructs within the file.
  * @param branchDensityStats
  *   Metrics for branch density and boolean operations within the file.
  */
case class FileRollup(
    // core metrics
    loc: Int,
    totalFunctions: Int,
    totalPublicFunctions: Int,
    totalPrivateFunctions: Int,
    fileSizeBytes: Long,
    totalSymbols: Int,
    totalPublicSymbols: Int,
    totalPrivateSymbols: Int,
    totalNestedSymbols: Int,
    documentedPublicSymbols: Int,
    totalDeprecatedSymbols: Int,
    totalDefsValsVars: Int,
    totalPublicDefsValsVars: Int,
    // Inline and implicit/given usage metrics
    inlineAndImplicitStats: InlineAndImplicitStats,
    // Pattern matching metrics (aggregated from methods and members)
    patternMatchingStats: PatternMatchingStats,
    // Branch density metrics (aggregated from methods and members
    branchDensityStats: BranchDensityStats
) {

  /**
    * Generates a formatted string summarizing various metrics and statistics for a file, such as lines of code (LOC),
    * number of functions, symbol information, usage of return types, inline definitions, implicit and given instances,
    * pattern matching details, and branch density metrics.
    *
    * Provides detailed insights ranging from high-level file statistics to specific code metrics, including return type
    * explicitness, inline usage, implicit/given usage, and density metrics for branches and boolean operations.
    *
    * @return
    *   A human-readable, structured string containing metrics and statistics for the file.
    */
  def prettify: String = {
    f"""Lines of Code: $loc
       |Total Functions: $totalFunctions
       |  Public Functions: $totalPublicFunctions
       |  Private Functions: $totalPrivateFunctions
       |File Size (bytes): $fileSizeBytes
       |Total Symbols: $totalSymbols
       |  Public Symbols: $totalPublicSymbols
       |  Private Symbols: $totalPrivateSymbols
       |  Nested Symbols: $totalNestedSymbols
       |Documented Public Symbols: $documentedPublicSymbols
       |Deprecated Symbols: $totalDeprecatedSymbols
       |
       |Return Type Explicitness:
       |  Total Defs/Vals/Vars: $totalDefsValsVars
       |    Explicit: ${inlineAndImplicitStats.explicitDefsValsVars}
       |    Percentage: ${"%.2f".format(inlineAndImplicitStats.returnTypeExplicitness)}%%
       |  Public Defs/Vals/Vars: $totalPublicDefsValsVars
       |    Explicit: ${inlineAndImplicitStats.explicitPublicDefsValsVars}
       |    Percentage: ${"%.2f".format(inlineAndImplicitStats.publicReturnTypeExplicitness)}%%
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
