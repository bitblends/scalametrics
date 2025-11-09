/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.metrics.model

/**
  * Represents the metrics and metadata of a method in a Scala source file. This includes information about the method’s
  * structure, parameters, complexity, explicitness, pattern matching, and branch density.
  *
  * @param fileId
  *   The unique identifier of the file containing the method.
  * @param name
  *   The name of the method.
  * @param signature
  *   The full signature of the method, including parameter types and return type.
  * @param accessModifier
  *   The access modifier of the method (e.g., public, private, or protected).
  * @param linesOfCode
  *   The number of lines of code in the method.
  * @param isNested
  *   Indicates whether the method is nested within another structure (e.g., another method or class).
  * @param hasScaladoc
  *   Indicates whether the method has associated Scaladoc documentation.
  * @param isDeprecated
  *   Indicates whether the method is marked as deprecated.
  * @param parentMember
  *   The name of the parent member or structure, if the method is nested; otherwise, None.
  * @param cComplexity
  *   The cyclomatic complexity of the method, representing the number of independent paths through the method's code.
  * @param nestingDepth
  *   The nesting depth of the method, measuring the maximum level of nested structures within the method.
  * @param totalParams
  *   The total number of parameters in the method.
  * @param paramLists
  *   The number of parameter lists in the method.
  * @param implicitParamLists
  *   The number of implicit parameter lists in the method.
  * @param usingParamLists
  *   The number of using parameter lists in the method.
  * @param implicitParams
  *   The total number of implicit parameters in the method.
  * @param usingParams
  *   The total number of using parameters in the method.
  * @param defaultedParams
  *   The number of parameters in the method that have default values.
  * @param byNameParams
  *   The number of by-name parameters in the method.
  * @param varargParams
  *   The number of vararg parameters in the method.
  * @param hasInlineModifier
  *   Indicates whether the method uses the inline modifier.
  * @param inlineParamCount
  *   The number of parameters in the method that are declared inline.
  * @param isImplicitConversion
  *   Indicates whether the method is an implicit conversion.
  * @param isAbstract
  *   Indicates whether the method is abstract.
  * @param hasExplicitReturnType
  *   Indicates whether the method has an explicitly declared return type.
  * @param inferredReturnType
  *   The inferred return type of the method, if an explicit return type is not provided.
  * @param pmMatches
  *   The number of pattern match expressions in the method.
  * @param pmCases
  *   The total number of cases within pattern match expressions in the method.
  * @param pmGuards
  *   The number of guard conditions in the pattern match expressions of the method.
  * @param pmWildcards
  *   The number of wildcards in the pattern match expressions of the method.
  * @param pmMaxNesting
  *   The maximum nesting level of pattern match expressions in the method.
  * @param pmNestedMatches
  *   The total number of nested pattern match expressions in the method.
  * @param pmAvgCasesPerMatch
  *   The average number of cases per pattern match expression in the method.
  * @param bdBranches
  *   The total number of branches (if-else, cases, loops, etc.) in the method.
  * @param bdIfCount
  *   The number of if-else branches in the method.
  * @param bdCaseCount
  *   The number of case branches in the method.
  * @param bdLoopCount
  *   The number of loop constructs (e.g., while, for) in the method.
  * @param bdCatchCaseCount
  *   The number of catch cases in the method.
  * @param bdBoolOpsCount
  *   The number of boolean operations (e.g., &&, ||) in the method.
  * @param bdDensityPer100
  *   The branch density per 100 lines of code in the method.
  * @param bdBoolOpsPer100
  *   The number of boolean operations per 100 lines of code in the method.
  */
case class MethodMetrics(
    fileId: String,
    name: String,
    signature: String,
    accessModifier: String,
    linesOfCode: Int,
    isNested: Boolean,
    hasScaladoc: Boolean,
    isDeprecated: Boolean,
    parentMember: Option[String] = None,
    // Cyclomatic complexity
    cComplexity: Int,
    // Nesting depth
    nestingDepth: Int,
    // Parameter metrics
    totalParams: Int,
    paramLists: Int,
    implicitParamLists: Int,
    usingParamLists: Int,
    implicitParams: Int,
    usingParams: Int,
    defaultedParams: Int,
    byNameParams: Int,
    varargParams: Int,
    // inline
    hasInlineModifier: Boolean,
    inlineParamCount: Int,
    isImplicitConversion: Boolean,
    // explicitness
    isAbstract: Boolean,
    hasExplicitReturnType: Boolean,
    inferredReturnType: Option[String] = None,
    // Pattern matching metrics
    pmMatches: Int,
    pmCases: Int,
    pmGuards: Int,
    pmWildcards: Int,
    pmMaxNesting: Int,
    pmNestedMatches: Int,
    pmAvgCasesPerMatch: Double,
    // Branch density metrics
    bdBranches: Int,
    bdIfCount: Int,
    bdCaseCount: Int,
    bdLoopCount: Int,
    bdCatchCaseCount: Int,
    bdBoolOpsCount: Int,
    bdDensityPer100: Double,
    bdBoolOpsPer100: Double
) extends Symbol
