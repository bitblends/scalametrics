/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.stats.model

import com.bitblends.scalametrics.metrics.model.{
  BranchDensityMetrics,
  InlineAndImplicitMetrics,
  Metadata,
  PatternMatchingMetrics
}

/**
  * Represents the statistical analysis of a specific member in a Scala codebase. This case class consolidates a variety
  * of metrics, such as code complexity, documentation presence, nesting depth, and other specific statistics related to
  * inline constructs, pattern matching, and branching structures.
  *
  * @param metadata
  *   Contains metadata associated with the member, such as its name, type, location, and other descriptive information.
  *
  * @param cComplexity
  *   Cyclomatic complexity of the code member, which measures the number of linearly independent paths through the
  *   program.
  *
  * @param hasScaladoc
  *   Indicates whether the member is documented with Scaladoc.
  *
  * @param nestingDepth
  *   Represents the depth of nested blocks or structures within the member.
  *
  * @param inlineAndImplicitStats
  *   Statistics related to inline modifiers, implicit constructs, and given instances or conversions.
  *
  * @param patternMatchingMetrics
  *   General statistics on pattern matching usage, including the number of match expressions, cases, and nested
  *   matches.
  *
  * @param branchDensityMetrics
  *   Metrics related to branch density within the member, such as conditional branches, loops, boolean operations, and
  *   branching complexity.
  *
  * This class extends `MemberBase`, inheriting its characteristics and potential behavior.
  */
case class MemberStats(
    metadata: Metadata,
    cComplexity: Int,
    hasScaladoc: Boolean,
    nestingDepth: Int,
    inlineAndImplicitStats: InlineAndImplicitMetrics,
    patternMatchingMetrics: PatternMatchingMetrics,
    branchDensityMetrics: BranchDensityMetrics
) extends MemberBase
    with StatsBase
