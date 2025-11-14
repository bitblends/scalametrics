/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.stats.model

import com.bitblends.scalametrics.metrics.model.Metadata

/**
  * Represents statistics and metrics associated with a member or symbol in a Scala codebase.
  *
  * This case class provides detailed insights into various attributes of a member, such as complexity, nesting depth,
  * presence of Scaladoc, and a variety of specific metrics related to inline and implicit constructs, pattern matching,
  * and branch density.
  *
  * @param metadata
  *   Metadata describing the member, including its name, type, access modifier, location, and other properties.
  * @param complexity
  *   The cyclomatic complexity of the member, representing its logical complexity based on the control flow structure.
  * @param nestingDepth
  *   The maximum depth of nested constructs within the member, providing an indication of structural complexity.
  * @param hasScaladoc
  *   Indicates whether the member includes Scaladoc documentation.
  * @param inlineAndImplicitStats
  *   A set of metrics related to the inline and implicit characteristics of the member, such as the presence of the
  *   `inline` modifier, implicit conversions, and abstractness.
  * @param patternMatchingStats
  *   Metrics that assess the complexity and usage of pattern matching constructs within the member, including the
  *   number of cases, guards, and wildcards.
  * @param branchDensityStats
  *   Metrics that provide insights into the branching intensity and density for the member, including counts of
  *   branches, loops, and conditional statements.
  */
case class MemberStats(
    metadata: Metadata,
    complexity: Int = 0,
    nestingDepth: Int = 0,
    hasScaladoc: Boolean = false,
    inlineAndImplicitStats: InlineAndImplicitStats,
    patternMatchingStats: PatternMatchingStats,
    branchDensityStats: BranchDensityStats
) extends SymbolStatsBase
    with Serializer {

  /**
    * * Combines two MemberStats instances by summing their complexity and merging their metrics.
    *
    * @param that
    *   Another MemberStats instance to combine with this one.
    * @return
    *   A new MemberStats instance representing the combined statistics.
    */
  def +(that: MemberStats): MemberStats = {
    MemberStats(
      metadata = this.metadata, // assuming metadata remains the same
      complexity = this.complexity + that.complexity,
      nestingDepth = math.max(this.nestingDepth, that.nestingDepth),
      hasScaladoc = this.hasScaladoc || that.hasScaladoc,
      inlineAndImplicitStats = this.inlineAndImplicitStats + that.inlineAndImplicitStats,
      patternMatchingStats = this.patternMatchingStats + that.patternMatchingStats,
      branchDensityStats = this.branchDensityStats + that.branchDensityStats
    )
  }

  /**
    * Provides a formatted string representation of the MemberStats instance, detailing its various metrics.
    *
    * @return
    *   A formatted string summarizing the member's statistics.
    */
  override def formattedString: String = {
    s"""MemberStats:
       |----------------------------------------------------------
       |  Name: ${metadata.name}
       |  Cyclomatic Complexity: $complexity
       |  Nesting Depth: $nestingDepth
       |  Has Scaladoc: $hasScaladoc
       |  Inline and Implicit Stats: ${inlineAndImplicitStats.formattedString}
       |  Pattern Matching Stats: ${patternMatchingStats.formattedString}
       |  Branch Density Stats: ${branchDensityStats.formattedString}
     """.stripMargin
  }
}
