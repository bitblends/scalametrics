/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.stats.model

/**
  * Represents an aggregated summary of metrics and statistics for a software project, capturing various aspects such as
  * code complexity, symbol documentation, inline and implicit usage, pattern matching, branch density, and overall
  * package-level summaries.
  *
  * This case class is intended for analyzing the structural and qualitative characteristics of the codebase, providing
  * insights into code quality, maintainability, and areas that may require attention.
  *
  * @param totalFiles
  *   The total number of files in the project.
  * @param totalLoc
  *   The total number of lines of code across all files.
  * @param totalFunctions
  *   The total number of functions in the project.
  * @param totalPublicFunctions
  *   The total number of public functions.
  * @param totalPrivateFunctions
  *   The total number of private functions.
  * @param averageFileSizeBytes
  *   The average file size in bytes.
  * @param totalFileSizeBytes
  *   The total file size across all files in bytes.
  * @param totalSymbols
  *   The total number of symbols in the project.
  * @param totalPublicSymbols
  *   The total number of public symbols.
  * @param totalPrivateSymbols
  *   The total number of private symbols.
  * @param totalDeprecatedSymbols
  *   The total number of deprecated symbols.
  * @param totalDefsValsVars
  *   The total number of defs, vals, and vars.
  * @param totalPublicDefsValsVars
  *   The total number of public defs, vals, and vars.
  * @param totalNestedSymbols
  *   The total number of nested symbols.
  * @param documentedPublicSymbols
  *   The total number of documented public symbols.
  * @param avgCyclomaticComplexity
  *   The average cyclomatic complexity across the codebase.
  * @param maxCyclomaticComplexity
  *   The maximum cyclomatic complexity observed in the codebase.
  * @param avgNestingDepth
  *   The average nesting depth of code blocks.
  * @param maxNestingDepth
  *   The maximum nesting depth observed in the codebase.
  * @param scalaDocCoverage
  *   The percentage of documented public symbols.
  * @param deprecatedSymbolsDensity
  *   The percentage of deprecated symbols across all symbols.
  * @param inlineAndImplicitStats
  *   A comprehensive view of inline and implicit characteristics.
  * @param patternMatchingStats
  *   Statistics related to pattern matching usage.
  * @param branchDensityStats
  *   Statistics related to branch density and Boolean operations.
  * @param totalPackages
  *   The total number of packages in the project.
  * @param packagesWithHighComplexity
  *   Number of packages with average complexity exceeding a threshold.
  * @param packagesWithLowDocumentation
  *   Number of packages with documentation coverage below a threshold.
  */
case class ProjectRollup(
    // Core metrics
    totalFiles: Int,
    totalLoc: Int,
    totalFunctions: Int,
    totalPublicFunctions: Int,
    totalPrivateFunctions: Int,
    averageFileSizeBytes: Long,
    totalFileSizeBytes: Long,
    // Symbol metrics
    totalSymbols: Int,
    totalPublicSymbols: Int,
    totalPrivateSymbols: Int,
    totalDeprecatedSymbols: Int,
    totalDefsValsVars: Int,
    totalPublicDefsValsVars: Int,
    totalNestedSymbols: Int,
    documentedPublicSymbols: Int,
    // Complexity metrics (averages and maximums)
    avgCyclomaticComplexity: Double,
    maxCyclomaticComplexity: Int,
    // Nesting depth metrics
    avgNestingDepth: Double,
    maxNestingDepth: Int,
    // Document coverage metrics
    scalaDocCoverage: Double, // Percentage of documented public symbols
    deprecatedSymbolsDensity: Double, // Percentage of deprecated symbols
    // Inline, implicits, explicitness metrics
    inlineAndImplicitStats: InlineAndImplicitStats,
    // Pattern matching metrics
    patternMatchingStats: PatternMatchingStats,
    // Branch density stats
    branchDensityStats: BranchDensityStats,
    // Package-level summary
    totalPackages: Int,
    packagesWithHighComplexity: Int, // Packages with avg complexity > threshold
    packagesWithLowDocumentation: Int // Packages with doc coverage < threshold
) {

  /**
    * Generates a detailed, human-readable summary of various project-level metrics and statistics, including
    * information about files, functions, symbols, documentation coverage, return type explicitness, inline and implicit
    * usage, pattern matching, branch density, and complexity metrics.
    *
    * The summary presents an aggregated view of these metrics, formatted for easy interpretation, and provides insights
    * into the overall structural and qualitative aspects of the project.
    *
    * @return
    *   A formatted string containing the complete summary of project metrics and statistics.
    */
  def prettify: String = {
    f"""OVERALL STATISTICS:
       |--------------------------------------------------------------------------------
       |Total Files: $totalFiles
       |Total Lines of Code: $totalLoc
       |Total File Size: ${totalFileSizeBytes / (1024 * 1024)}MB
       |Average File Size: ${averageFileSizeBytes / 1024}KB
       |Total Packages: $totalPackages
       |
       |FUNCTION METRICS:
       |--------------------------------------------------------------------------------
       |Total Functions: $totalFunctions
       |  Public Functions: $totalPublicFunctions (${
                                                      if (totalFunctions > 0)
                                                        "%.1f".format(100.0 * totalPublicFunctions / totalFunctions)
                                                      else "0.0"
                                                    }%%)
       |  Private Functions: $totalPrivateFunctions (${
                                                        if (totalFunctions > 0)
                                                          "%.1f".format(100.0 * totalPrivateFunctions / totalFunctions)
                                                        else "0.0"
                                                      }%%)
       |
       |SYMBOL METRICS:
       |--------------------------------------------------------------------------------
       |Total Symbols: $totalSymbols
       |  Public Symbols: $totalPublicSymbols
       |  Private Symbols: $totalPrivateSymbols
       |  Nested Symbols: $totalNestedSymbols
       |
       |DOCUMENTATION & DEPRECATION:
       |--------------------------------------------------------------------------------
       |Scaladoc Coverage: ${
                              "%.2f".format(scalaDocCoverage)
                            }%% ($documentedPublicSymbols/$totalPublicSymbols public symbols)
       |Deprecated Symbols: $totalDeprecatedSymbols (${"%.2f".format(deprecatedSymbolsDensity)}%% of all symbols)
       |Packages with Low Documentation: $packagesWithLowDocumentation
       |
       |RETURN TYPE EXPLICITNESS:
       |--------------------------------------------------------------------------------
       |All Defs/Vals/Vars: ${inlineAndImplicitStats.explicitDefsValsVars}/$totalDefsValsVars (${
                                                                                                  "%.2f".format(
                                                                                                    inlineAndImplicitStats.returnTypeExplicitness
                                                                                                  )
                                                                                                }%%)
       |Public Only: ${inlineAndImplicitStats.explicitPublicDefsValsVars}/$totalPublicDefsValsVars (${
                                                                                                       "%.2f".format(
                                                                                                         inlineAndImplicitStats.publicReturnTypeExplicitness
                                                                                                       )
                                                                                                     }%%)
       |
       |INLINE USAGE:
       |--------------------------------------------------------------------------------
       |Inline Methods: ${inlineAndImplicitStats.inlineMethods}
       |Inline Vals: ${inlineAndImplicitStats.inlineVals}
       |Inline Vars: ${inlineAndImplicitStats.inlineVars}
       |Inline Parameters: ${inlineAndImplicitStats.inlineParams}
       |
       |IMPLICIT/GIVEN USAGE:
       |--------------------------------------------------------------------------------
       |Implicit Vals: ${inlineAndImplicitStats.implicitVals}
       |Implicit Vars: ${inlineAndImplicitStats.implicitVars}
       |Implicit Conversions: ${inlineAndImplicitStats.implicitConversions}
       |Given Instances: ${inlineAndImplicitStats.givenInstances}
       |Given Conversions: ${inlineAndImplicitStats.givenConversions}
       |
       |PATTERN MATCHING METRICS:
       |--------------------------------------------------------------------------------
       |Total Matches: ${patternMatchingStats.matches}
       |Total Cases: ${patternMatchingStats.cases}
       |Guards Used: ${patternMatchingStats.guards}
       |Wildcards: ${patternMatchingStats.wildcards}
       |Max Nesting Level: ${patternMatchingStats.maxNesting}
       |Nested Matches: ${patternMatchingStats.nestedMatches}
       |
       |BRANCH DENSITY METRICS:
       |--------------------------------------------------------------------------------
       |Total Branches: ${branchDensityStats.branches}
       |  If Statements: ${branchDensityStats.ifCount}
       |  Case Statements: ${branchDensityStats.caseCount}
       |  Loops: ${branchDensityStats.loopCount}
       |  Catch Cases: ${branchDensityStats.catchCaseCount}
       |Boolean Operations: ${branchDensityStats.boolOpsCount}
       |Branch Density: ${"%.2f".format(branchDensityStats.densityPer100)} per 100 LOC
       |Boolean Op Density: ${"%.2f".format(branchDensityStats.boolOpsPer100)} per 100 LOC
       |
       |COMPLEXITY METRICS:
       |--------------------------------------------------------------------------------
       |Cyclomatic Complexity: Avg ${"%.2f".format(avgCyclomaticComplexity)}, Max $maxCyclomaticComplexity
       |Nesting Depth: Avg ${"%.2f".format(avgNestingDepth)}, Max $maxNestingDepth
       |Packages with High Complexity: $packagesWithHighComplexity
       |===============================================================================
       |""".stripMargin
  }

}
