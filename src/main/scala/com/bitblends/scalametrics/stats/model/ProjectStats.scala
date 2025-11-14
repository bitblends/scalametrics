/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.stats.model

/**
  * Represents a project with its associated metadata, rollup information, and a collection of packages.
  *
  * @param metadata
  *   Contains metadata and descriptive information about the project such as name, version, Scala version, and other
  *   optional details.
  * @param rollup
  *   Aggregated or summary information of packages.
  * @param packageStats
  *   A collection of packages associated with the project.
  */
case class ProjectStats(
    metadata: ProjectMetadata,
    rollup: Rollup,
    packageStats: Vector[PackageStats]
) extends Serializer {

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
  override def formattedString: String = {
    f"""OVERALL STATISTICS:
       |--------------------------------------------------------------------------------
       |Total Files: ${rollup.totalCount}
       |Total Lines of Code: ${rollup.coreStats.totalLoc}
       |Total File Size: ${rollup.coreStats.totalFileSizeBytes / (1024 * 1024)}MB
       |Average File Size: ${rollup.averageFileSizeBytes / 1024}KB
       |Total Packages: ${packageStats.length}
       |
       |FUNCTION METRICS:
       |--------------------------------------------------------------------------------
       |Total Functions: ${rollup.coreStats.totalFunctions}
       |  Public Functions: ${rollup.coreStats.totalPublicFunctions} (${
                                                                         if (rollup.coreStats.totalFunctions > 0)
                                                                           "%.1f".format(
                                                                             100.0 * rollup.coreStats.totalPublicFunctions / rollup.coreStats.totalFunctions
                                                                           )
                                                                         else "0.0"
                                                                       }%%)
       |  Private Functions: ${rollup.coreStats.totalPrivateFunctions} (${
                                                                           if (rollup.coreStats.totalFunctions > 0)
                                                                             "%.1f".format(
                                                                               100.0 * rollup.coreStats.totalPrivateFunctions / rollup.coreStats.totalFunctions
                                                                             )
                                                                           else "0.0"
                                                                         }%%)
       |
       |SYMBOL METRICS:
       |--------------------------------------------------------------------------------
       |Total Symbols: ${rollup.coreStats.totalSymbols}
       |  Public Symbols: ${rollup.coreStats.totalPublicSymbols}
       |  Private Symbols: ${rollup.coreStats.totalPrivateSymbols}
       |  Nested Symbols: ${rollup.coreStats.totalNestedSymbols}
       |
       |DOCUMENTATION & DEPRECATION:
       |--------------------------------------------------------------------------------
       |Scaladoc Coverage: ${"%.2f".format(rollup.scalaDocCoveragePercentage)}%% (${
                                                                                     rollup.coreStats.totalDocumentedPublicSymbols
                                                                                   }/${
                                                                                        rollup.coreStats.totalPublicSymbols
                                                                                      } public symbols)
       |Deprecated Symbols: ${rollup.coreStats.totalDeprecatedSymbols} (${
                                                                           "%.2f".format(
                                                                             rollup.deprecatedSymbolsDensityPercentage
                                                                           )
                                                                         }%% of all symbols)
       |Packages with Low Documentation: ${rollup.itemsWithLowDocumentation}
       |
       |RETURN TYPE EXPLICITNESS:
       |--------------------------------------------------------------------------------
       |All Defs/Vals/Vars: ${rollup.inlineAndImplicitStats.explicitDefsValsVars}/${
                                                                                     rollup.coreStats.totalDefsValsVars
                                                                                   } (${
                                                                                         "%.2f".format(
                                                                                           rollup.returnTypeExplicitness
                                                                                         )
                                                                                       }%%)
       |Public Only: ${rollup.inlineAndImplicitStats.explicitPublicDefsValsVars}/${
                                                                                    rollup.coreStats.totalPublicDefsValsVars
                                                                                  } (${
                                                                                        "%.2f".format(
                                                                                          rollup.publicReturnTypeExplicitness
                                                                                        )
                                                                                      }%%)
       |
       |INLINE USAGE:
       |--------------------------------------------------------------------------------
       |Inline Methods: ${rollup.inlineAndImplicitStats.inlineMethods}
       |Inline Vals: ${rollup.inlineAndImplicitStats.inlineVals}
       |Inline Vars: ${rollup.inlineAndImplicitStats.inlineVars}
       |Inline Parameters: ${rollup.inlineAndImplicitStats.inlineParams}
       |
       |IMPLICIT/GIVEN USAGE:
       |--------------------------------------------------------------------------------
       |Implicit Vals: ${rollup.inlineAndImplicitStats.implicitVals}
       |Implicit Vars: ${rollup.inlineAndImplicitStats.implicitVars}
       |Implicit Conversions: ${rollup.inlineAndImplicitStats.implicitConversions}
       |Given Instances: ${rollup.inlineAndImplicitStats.givenInstances}
       |Given Conversions: ${rollup.inlineAndImplicitStats.givenConversions}
       |
       |PATTERN MATCHING METRICS:
       |--------------------------------------------------------------------------------
       |Total Matches: ${rollup.patternMatchingStats.matches}
       |Total Cases: ${rollup.patternMatchingStats.cases}
       |Guards Used: ${rollup.patternMatchingStats.guards}
       |Wildcards: ${rollup.patternMatchingStats.wildcards}
       |Max Nesting Level: ${rollup.patternMatchingStats.maxNesting}
       |Nested Matches: ${rollup.patternMatchingStats.nestedMatches}
       |
       |BRANCH DENSITY METRICS:
       |--------------------------------------------------------------------------------
       |Total Branches: ${rollup.branchDensityStats.branches}
       |  If Statements: ${rollup.branchDensityStats.ifCount}
       |  Case Statements: ${rollup.branchDensityStats.caseCount}
       |  Loops: ${rollup.branchDensityStats.loopCount}
       |  Catch Cases: ${rollup.branchDensityStats.catchCaseCount}
       |Boolean Operations: ${rollup.branchDensityStats.boolOpsCount}
       |Branch Density: ${"%.2f".format(rollup.branchDensityStats.densityPer100(rollup.coreStats.totalLoc))} per 100 LOC
       |Boolean Op Density: ${
                               "%.2f".format(rollup.branchDensityStats.boolOpsPer100(rollup.coreStats.totalLoc))
                             } per 100 LOC
       |
       |COMPLEXITY METRICS:
       |--------------------------------------------------------------------------------
       |Cyclomatic Complexity: Avg ${"%.2f".format(rollup.avgCyclomaticComplexity)}, Max ${
                                                                                            rollup.maxCyclomaticComplexity
                                                                                          }
       |Nesting Depth: Avg ${"%.2f".format(rollup.avgNestingDepth)}, Max ${rollup.maxNestingDepth}
       |Packages with High Complexity: ${rollup.itemsWithHighComplexity}
       |===============================================================================
       |""".stripMargin
  }
}
