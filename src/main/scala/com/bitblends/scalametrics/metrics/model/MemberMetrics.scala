/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.metrics.model

/**
  * Represents metrics for a specific Scala symbol (e.g., class, object, method, trait, etc.). The `MemberMetrics` class
  * captures a wide range of details about the symbol, including its structure, complexity, pattern matching
  * characteristics, and branch density metrics.
  *
  * @param fileId
  *   Unique identifier for the file where the symbol is defined.
  * @param name
  *   Name of the symbol.
  * @param memberType
  *   Type of the symbol, such as "val", "var", "type", "class", "object", or "trait".
  * @param signature
  *   Full signature of the symbol, including method parameters or type information.
  * @param accessModifier
  *   Access level of the symbol, such as "public", "private", or "protected".
  * @param linesOfCode
  *   Number of lines of code the symbol occupies.
  * @param hasScaladoc
  *   Indicates whether the symbol is documented with Scaladoc comments.
  * @param isDeprecated
  *   Indicates whether the symbol is marked as deprecated.
  * @param cComplexity
  *   Cyclomatic complexity of the symbol, representing the number of independent code paths.
  * @param nestingDepth
  *   Maximum nesting depth within the symbol's body.
  * @param hasInlineModifier
  *   Indicates if the symbol has an `inline` keyword, used for inlining in Scala 3.
  * @param isGivenInstance
  *   Indicates whether the symbol represents a given instance in Scala 3.
  * @param isGivenConversion
  *   Indicates whether the symbol represents a given conversion in Scala 3.
  * @param isImplicit
  *   Indicates whether the symbol is declared as implicit.
  * @param isAbstract
  *   Indicates whether the symbol is abstract.
  * @param hasExplicitReturnType
  *   Indicates whether the symbol has an explicitly declared return type.
  * @param inferredReturnType
  *   Inferred return type of the symbol, if not explicitly declared.
  * @param pmMatches
  *   Number of pattern matching expressions within the symbol.
  * @param pmCases
  *   Total number of case clauses in all pattern matches.
  * @param pmGuards
  *   Number of guard conditions in pattern matches.
  * @param pmWildcards
  *   Number of wildcard patterns (_) in all pattern matches.
  * @param pmMaxNesting
  *   Maximum nesting level in pattern matching expressions.
  * @param pmNestedMatches
  *   Number of nested pattern matching expressions inside other matches.
  * @param pmAvgCasesPerMatch
  *   Average number of case clauses per pattern match.
  * @param bdBranches
  *   Total number of branches, including if, case, loop, and catch statements.
  * @param bdIfCount
  *   Number of if statements in the symbol.
  * @param bdCaseCount
  *   Number of case statements in the symbol.
  * @param bdLoopCount
  *   Number of loops (while or for) in the symbol.
  * @param bdCatchCaseCount
  *   Number of catch clauses in the symbol.
  * @param bdBoolOpsCount
  *   Number of boolean operations (&&, ||, etc.) within the symbol.
  * @param bdDensityPer100
  *   Branch density per 100 lines of code, measuring the ratio of branching statements to lines of code.
  * @param bdBoolOpsPer100
  *   Boolean operations density per 100 lines of code, measuring the ratio of boolean operations to lines of code.
  */
case class MemberMetrics(
    fileId: String,
    name: String,
    memberType: String, // "val", "var", "type", "class", "object", "trait",...
    signature: String,
    accessModifier: String,
    linesOfCode: Int,
    hasScaladoc: Boolean,
    isDeprecated: Boolean,
    // Cyclomatic complexity
    cComplexity: Int,
    // Nesting depth
    nestingDepth: Int,
    // inline
    hasInlineModifier: Boolean,
    // given
    isGivenInstance: Boolean, // Scala 3 'given'
    isGivenConversion: Boolean,
    // implicit
    isImplicit: Boolean,
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
