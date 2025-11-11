/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.metrics.model

/**
  * Represents metrics associated with a member in the source code, extending the `Symbol` trait and encapsulating
  * various analytical details such as cyclomatic complexity, nesting depth, documentation presence, and structural
  * characteristics of inline, implicit, pattern matching, and branching density aspects.
  *
  * This case class is primarily used for analyzing and assessing code quality and complexity at the member level.
  *
  * @param metadata
  *   Metadata providing structural and property-related information about the member, such as its name, type,
  *   accessibility, and other descriptive attributes of the symbol.
  * @param cComplexity
  *   The cyclomatic complexity associated with the member, measuring the number of independent paths through its code.
  * @param nestingDepth
  *   The depth of nested constructs within the member's code, indicating structural complexity and maintainability
  *   factors.
  * @param hasScaladoc
  *   Indicates whether the member has accompanying Scaladoc documentation.
  * @param inlineAndImplicitMetrics
  *   Metrics related to inline and implicit properties of the member, including inline modifiers, implicit conversions,
  *   and explicitness of type definitions.
  * @param patternMatchingMetrics
  *   Metrics associated with pattern matching constructs in the member's code, capturing details on matches, cases,
  *   nesting, guards, and related complexities.
  * @param branchDensityMetrics
  *   Metrics related to branch density and logical complexity within the member's code, including details on branch
  *   types, densities, and boolean operations.
  */
case class MemberMetrics(
    metadata: Metadata,
    cComplexity: Int,
    nestingDepth: Int,
    hasScaladoc: Boolean,
    inlineAndImplicitMetrics: InlineAndImplicitMetrics,
    patternMatchingMetrics: PatternMatchingMetrics,
    branchDensityMetrics: BranchDensityMetrics
) extends Symbol
