/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.stats.model

/**
  * Represents a detailed statistical rollup of metrics for a single file. This includes core metrics, return type
  * explicitness, usage of inline and implicit/given constructs, pattern matching statistics, and branch density
  * metrics. The `FileRollup` class is primarily used to summarize and analyze code quality and structural complexity at
  * the file level.
  *
  * @param loc
  *   The total number of lines of code within the file.
  * @param totalFunctions
  *   The total number of functions declared in the file.
  * @param totalPublicFunctions
  *   The number of public functions among all functions in the file.
  * @param totalPrivateFunctions
  *   The number of private functions among all functions in the file.
  * @param fileSizeBytes
  *   The size of the file in bytes.
  * @param totalSymbols
  *   The total number of symbols (functions, variables, etc.) in the file.
  * @param totalPublicSymbols
  *   The number of public symbols in the file.
  * @param totalPrivateSymbols
  *   The number of private symbols in the file.
  * @param totalNestedSymbols
  *   The total number of symbols nested inside other declarations.
  * @param documentedPublicSymbols
  *   The count of public symbols that are documented with Scaladoc.
  * @param totalDeprecatedSymbols
  *   The count of symbols marked as deprecated.
  * @param totalDefsValsVars
  *   The total number of definitions, values, and variables in the file.
  * @param totalPublicDefsValsVars
  *   The number of public definitions, values, and variables in the file.
  * @param explicitDefsValsVars
  *   The total number of definitions, values, and variables with an explicitly declared return type.
  * @param explicitPublicDefsValsVars
  *   The number of public definitions, values, and variables with an explicitly declared return type.
  * @param returnTypeExplicitness
  *   The percentage of definitions, values, and variables with explicit return types, expressed as a decimal value.
  * @param publicReturnTypeExplicitness
  *   The percentage of public definitions, values, and variables with explicit return types, expressed as a decimal
  *   value.
  * @param inlineMethods
  *   The count of methods in the file that are marked as inline.
  * @param inlineVals
  *   The count of values in the file that are marked as inline.
  * @param inlineVars
  *   The count of variables in the file that are marked as inline.
  * @param inlineParams
  *   The count of function parameters in the file that are marked as inline.
  * @param implicitVals
  *   The count of values in the file marked as implicit.
  * @param implicitVars
  *   The count of variables in the file marked as implicit.
  * @param implicitConversions
  *   The count of implicit conversion definitions in the file.
  * @param givenInstances
  *   The count of given instances in the file.
  * @param givenConversions
  *   The count of given conversion definitions in the file.
  * @param pmMatches
  *   The total number of pattern matching constructs in the file.
  * @param pmCases
  *   The total number of case clauses in all pattern matches in the file.
  * @param pmGuards
  *   The total number of guard clauses within pattern matches in the file.
  * @param pmWildcards
  *   The count of wildcard match cases (_) throughout the file.
  * @param pmMaxNesting
  *   The maximum nesting depth of pattern matching constructs within the file.
  * @param pmNestedMatches
  *   The count of nested pattern matches in the file.
  * @param bdBranches
  *   The total number of branch points (e.g., if/else, switch cases) in the file.
  * @param bdIfCount
  *   The count of if statements or expressions in the file.
  * @param bdCaseCount
  *   The count of case statements within pattern matches in the file.
  * @param bdLoopCount
  *   The count of loop constructs (e.g., for, while) in the file.
  * @param bdCatchCaseCount
  *   The count of catch case statements within try-catch blocks in the file.
  * @param bdBoolOpsCount
  *   The count of boolean operations (e.g., &&, ||) in the file.
  * @param bdDensityPer100
  *   The density of branch points per 100 lines of code.
  * @param bdBoolOpsPer100
  *   The density of boolean operations per 100 lines of code.
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
    // return type explicitness
    totalDefsValsVars: Int,
    totalPublicDefsValsVars: Int,
    explicitDefsValsVars: Int,
    explicitPublicDefsValsVars: Int,
    returnTypeExplicitness: Double, // Percentage for all defs/vals/vars
    publicReturnTypeExplicitness: Double, // Percentage for public defs/vals/vars only
    // Inline and implicit/given usage metrics
    inlineMethods: Int,
    inlineVals: Int,
    inlineVars: Int,
    inlineParams: Int,
    // implicit
    implicitVals: Int,
    implicitVars: Int,
    implicitConversions: Int,
    // given
    givenInstances: Int,
    givenConversions: Int,
    // Pattern matching metrics (aggregated from methods and members)
    pmMatches: Int,
    pmCases: Int,
    pmGuards: Int,
    pmWildcards: Int,
    pmMaxNesting: Int,
    pmNestedMatches: Int,
    // Branch density metrics (aggregated from methods and members
    bdBranches: Int,
    bdIfCount: Int,
    bdCaseCount: Int,
    bdLoopCount: Int,
    bdCatchCaseCount: Int,
    bdBoolOpsCount: Int,
    bdDensityPer100: Double,
    bdBoolOpsPer100: Double
) {

  /**
    * Generates a well-formatted string representing various statistical metrics and properties of a file.
    *
    * The returned string includes details such as lines of code, function counts, file size, symbol metrics, return
    * type explicitness, inline usage, implicit/given usage, pattern matching metrics, and branch density metrics. These
    * metrics provide a comprehensive overview of the file's structure and complexity.
    *
    * @return
    *   A formatted string summarizing multiple statistical and structural metrics for the file.
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
       |    Explicit: $explicitDefsValsVars
       |    Percentage: ${"%.2f".format(returnTypeExplicitness)}%%
       |  Public Defs/Vals/Vars: $totalPublicDefsValsVars
       |    Explicit: $explicitPublicDefsValsVars
       |    Percentage: ${"%.2f".format(publicReturnTypeExplicitness)}%%
       |
       |Inline Usage:
       |  Inline Methods: $inlineMethods
       |  Inline Vals: $inlineVals
       |  Inline Vars: $inlineVars
       |  Inline Params: $inlineParams
       |
       |Implicit/Given Usage:
       |  Implicit Vals: $implicitVals
       |  Implicit Vars: $implicitVars
       |  Implicit Conversions: $implicitConversions
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
       |  Branches: $bdBranches
       |  Ifs: $bdIfCount
       |  Cases: $bdCaseCount
       |  Loops: $bdLoopCount
       |  Catch Cases: $bdCatchCaseCount
       |  Boolean Operations: $bdBoolOpsCount
       |  Density per 100 LOC: ${"%.2f".format(bdDensityPer100)}
       |  Boolean Ops per 100 LOC: ${"%.2f".format(bdBoolOpsPer100)}
       |""".stripMargin
  }

  def toMap: Map[String, String] = {
    val fieldNames = List(
      // core metrics
      "loc",
      "totalFunctions",
      "totalPublicFunctions",
      "totalPrivateFunctions",
      "fileSizeBytes",
      "totalSymbols",
      "totalPublicSymbols",
      "totalPrivateSymbols",
      "totalNestedSymbols",
      // documented symbols,
      "documentedPublicSymbols",
      "totalDeprecatedSymbols",
      "totalDefsValsVars",
      "totalPublicDefsValsVars",
      "explicitDefsValsVars",
      "explicitPublicDefsValsVars",
      "returnTypeExplicitness",
      "publicReturnTypeExplicitness",
      // Inline and implicit/given metrics,
      "inlineMethods",
      "inlineVals",
      "inlineVars",
      "inlineParams",
      // implicit metrics,
      "implicitVals",
      "implicitVars",
      "implicitConversions",
      // given,
      "givenInstances",
      "givenConversions",
      // Pattern matching metrics,
      "pmMatches",
      "pmCases",
      "pmGuards",
      "pmWildcards",
      "pmMaxNesting",
      "pmNestedMatches",
      // Branch density metrics,
      "bdBranches",
      "bdIfCount",
      "bdCaseCount",
      "bdLoopCount",
      "bdCatchCaseCount",
      "bdBoolOpsCount",
      "bdDensityPer100",
      "bdBoolOpsPer100"
    )
    fieldNames
      .zip(this.productIterator.toList)
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

}
