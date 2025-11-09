/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.stats.model

/**
  * Represents a comprehensive rollup of metrics for a project, providing statistics at various levels such as overall
  * structure, function-level data, symbol usage, documentation coverage, complexity, and specific feature usage (e.g.,
  * inline, implicit, given).
  *
  * @param totalFiles
  *   Total number of files in the project.
  * @param totalLoc
  *   Total lines of code across all files in the project.
  * @param totalFunctions
  *   Total number of functions/methods in the project.
  * @param totalPublicFunctions
  *   Total number of public functions/methods.
  * @param totalPrivateFunctions
  *   Total number of private functions/methods.
  * @param averageFileSizeBytes
  *   Average size of files in bytes.
  * @param totalFileSizeBytes
  *   Total size of all files in bytes.
  * @param totalSymbols
  *   Total number of symbols in the project.
  * @param totalPublicSymbols
  *   Total number of public symbols in the project.
  * @param totalPrivateSymbols
  *   Total number of private symbols in the project.
  * @param totalNestedSymbols
  *   Total number of nested symbols.
  * @param documentedPublicSymbols
  *   Number of documented public symbols.
  * @param totalDeprecatedSymbols
  *   Total number of deprecated symbols in the project.
  * @param scalaDocCoverage
  *   Percentage of public symbols that are documented.
  * @param deprecatedSymbolsDensity
  *   Percentage of symbols marked as deprecated.
  * @param totalDefsValsVars
  *   Total number of defs, vals, and vars in the project.
  * @param totalPublicDefsValsVars
  *   Total number of public defs, vals, and vars.
  * @param explicitDefsValsVars
  *   Total number of defs, vals, and vars with explicit return types.
  * @param explicitPublicDefsValsVars
  *   Total number of public defs, vals, and vars with explicit return types.
  * @param returnTypeExplicitness
  *   Percentage of defs, vals, and vars with explicit return types.
  * @param publicReturnTypeExplicitness
  *   Percentage of public defs, vals, and vars with explicit return types.
  * @param inlineMethods
  *   Total number of inline methods.
  * @param inlineVals
  *   Total number of inline vals.
  * @param inlineVars
  *   Total number of inline vars.
  * @param inlineParams
  *   Total number of inline parameters.
  * @param implicitVals
  *   Total number of implicit vals.
  * @param implicitVars
  *   Total number of implicit vars.
  * @param implicitConversions
  *   Total number of implicit conversions.
  * @param givenInstances
  *   Total number of given instances.
  * @param givenConversions
  *   Total number of given conversions.
  * @param pmMatches
  *   Total number of pattern match expressions.
  * @param pmCases
  *   Total number of case branches in pattern matches.
  * @param pmGuards
  *   Total number of guards used in pattern matches.
  * @param pmWildcards
  *   Total number of wildcard usages in pattern matches.
  * @param pmMaxNesting
  *   Maximum nesting depth in pattern matches.
  * @param pmNestedMatches
  *   Total number of nested pattern matches.
  * @param bdBranches
  *   Total number of branches.
  * @param bdIfCount
  *   Total number of if statements.
  * @param bdCaseCount
  *   Total number of case branches.
  * @param bdLoopCount
  *   Total number of loop structures.
  * @param bdCatchCaseCount
  *   Total number of catch case branches.
  * @param bdBoolOpsCount
  *   Total number of boolean operations.
  * @param bdDensityPer100
  *   Branch density per 100 lines of code.
  * @param bdBoolOpsPer100
  *   Boolean operation density per 100 lines of code.
  * @param avgCyclomaticComplexity
  *   Average cyclomatic complexity across all functions.
  * @param maxCyclomaticComplexity
  *   Maximum cyclomatic complexity of any function.
  * @param avgNestingDepth
  *   Average nesting depth across all functions.
  * @param maxNestingDepth
  *   Maximum nesting depth in any function.
  * @param totalPackages
  *   Total number of packages in the project.
  * @param packagesWithHighComplexity
  *   Number of packages with high average complexity.
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
    totalNestedSymbols: Int,
    documentedPublicSymbols: Int,
    totalDeprecatedSymbols: Int,
    // Coverage metrics
    scalaDocCoverage: Double, // Percentage of documented public symbols
    deprecatedSymbolsDensity: Double, // Percentage of deprecated symbols
    // Return type explicitness
    totalDefsValsVars: Int,
    totalPublicDefsValsVars: Int,
    explicitDefsValsVars: Int,
    explicitPublicDefsValsVars: Int,
    returnTypeExplicitness: Double, // Percentage for all defs/vals/vars
    publicReturnTypeExplicitness: Double, // Percentage for public defs/vals/vars only
    // Inline metrics
    inlineMethods: Int,
    inlineVals: Int,
    inlineVars: Int,
    inlineParams: Int,
    // Implicit metrics
    implicitVals: Int,
    implicitVars: Int,
    implicitConversions: Int,
    // Given metrics
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
    bdBoolOpsPer100: Double,
    // Complexity metrics (averages and maximums)
    avgCyclomaticComplexity: Double,
    maxCyclomaticComplexity: Int,
    avgNestingDepth: Double,
    maxNestingDepth: Int,
    // Package-level summary
    totalPackages: Int,
    packagesWithHighComplexity: Int, // Packages with avg complexity > threshold
    packagesWithLowDocumentation: Int // Packages with doc coverage < threshold
):

  /**
    * Generates a formatted report containing various statistical metrics and insights about the project.
    *
    * The report includes:
    *   - Overall project statistics such as total files, lines of code, and file sizes.
    *   - Metrics for functions, symbols, and documentation coverage.
    *   - Information related to deprecations, pattern matching, branching, and inline/given usage.
    *   - Complexity metrics, including cyclomatic complexity and nesting depth.
    *
    * @return
    *   A string containing the detailed, readable summary of project metrics.
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
       |All Defs/Vals/Vars: $explicitDefsValsVars/$totalDefsValsVars (${"%.2f".format(returnTypeExplicitness)}%%)
       |Public Only: $explicitPublicDefsValsVars/$totalPublicDefsValsVars (${
                                                                              "%.2f".format(
                                                                                publicReturnTypeExplicitness
                                                                              )
                                                                            }%%)
       |
       |INLINE USAGE:
       |--------------------------------------------------------------------------------
       |Inline Methods: $inlineMethods
       |Inline Vals: $inlineVals
       |Inline Vars: $inlineVars
       |Inline Parameters: $inlineParams
       |
       |IMPLICIT/GIVEN USAGE:
       |--------------------------------------------------------------------------------
       |Implicit Vals: $implicitVals
       |Implicit Vars: $implicitVars
       |Implicit Conversions: $implicitConversions
       |Given Instances: $givenInstances
       |Given Conversions: $givenConversions
       |
       |PATTERN MATCHING METRICS:
       |--------------------------------------------------------------------------------
       |Total Matches: $pmMatches
       |Total Cases: $pmCases
       |Guards Used: $pmGuards
       |Wildcards: $pmWildcards
       |Max Nesting Level: $pmMaxNesting
       |Nested Matches: $pmNestedMatches
       |
       |BRANCH DENSITY METRICS:
       |--------------------------------------------------------------------------------
       |Total Branches: $bdBranches
       |  If Statements: $bdIfCount
       |  Case Statements: $bdCaseCount
       |  Loops: $bdLoopCount
       |  Catch Cases: $bdCatchCaseCount
       |Boolean Operations: $bdBoolOpsCount
       |Branch Density: ${"%.2f".format(bdDensityPer100)} per 100 LOC
       |Boolean Op Density: ${"%.2f".format(bdBoolOpsPer100)} per 100 LOC
       |
       |COMPLEXITY METRICS:
       |--------------------------------------------------------------------------------
       |Cyclomatic Complexity: Avg ${"%.2f".format(avgCyclomaticComplexity)}, Max $maxCyclomaticComplexity
       |Nesting Depth: Avg ${"%.2f".format(avgNestingDepth)}, Max $maxNestingDepth
       |Packages with High Complexity: $packagesWithHighComplexity
       |===============================================================================
       |""".stripMargin
  }

  /**
    * Converts the current case class instance into a map of field names and their corresponding string representations.
    * For `Option` fields, `Some` values are converted to their string representation, while `None` is converted to an
    * empty string. For `Seq` fields, the elements are joined into a single string separated by commas. Other field
    * types are converted to their string representation by default.
    *
    * @return
    *   A map where the keys are field names and the values are their string representations.
    */
  def toMap: Map[String, String] = {
    this.productElementNames
      .zip(this.productIterator)
      .map { case (name, value) =>
        name -> (value match {
          case Some(v)     => v.toString
          case None        => ""
          case seq: Seq[_] => seq.mkString(",")
          case v           => v.toString
        })
      }
      .toMap
  }
