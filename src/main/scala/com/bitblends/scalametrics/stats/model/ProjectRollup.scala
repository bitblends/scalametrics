/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.stats.model

/**
  * Represents a holistic roll-up of a project's various metrics and statistics. This includes insights about
  * structural, complexity, documentation, and usage aspects across files, packages, functions, and symbols.
  *
  * The class provides detailed metrics related to:
  *   - Core project structure (files, lines of code, file size, packages).
  *   - Function metrics (counts of public and private functions).
  *   - Symbol metrics (counts of public, private, nested, and deprecated symbols).
  *   - Documentation coverage, including Scaladoc coverage percentages.
  *   - Complexity metrics, such as cyclomatic complexity and nesting depth.
  *   - Inline, implicit, and explicitness metrics for return types.
  *   - Pattern matching specifics, including matches, guards, and nesting.
  *   - Branch density statistics comprising conditional, loop, and catch usages.
  *
  * Primarily, it serves to provide reporting mechanisms and analytics for codebases, enabling better insights into
  * quality, maintainability, and adherence to best practices.
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
