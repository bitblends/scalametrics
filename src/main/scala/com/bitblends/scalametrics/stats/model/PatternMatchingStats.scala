/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.stats.model

/**
  * Represents detailed statistics related to pattern matching constructs within a codebase.
  *
  * @param matches
  *   The total number of pattern matches encountered.
  * @param cases
  *   The total number of `case` statements analyzed within pattern matching expressions.
  * @param guards
  *   The total count of guard conditions used in the pattern matching cases.
  * @param wildcards
  *   The number of occurrences of wildcard patterns (e.g., `_`) used in pattern matching.
  * @param maxNesting
  *   The maximum level of nesting within pattern matching constructs.
  * @param nestedMatches
  *   The total count of nested pattern matching statements.
  * @param avgCasesPerMatch
  *   The average number of `case` statements per pattern match expression.
  */
case class PatternMatchingStats(
    matches: Int = 0,
    cases: Int = 0,
    guards: Int = 0,
    wildcards: Int = 0,
    maxNesting: Int = 0,
    nestedMatches: Int = 0,
    avgCasesPerMatch: Double = 0.0
) extends Serializer {

  /**
    * Combines the statistics from two `PatternMatchingStats` instances.
    *
    * @param that
    *   Another instance of `PatternMatchingStats` to be added to this instance.
    * @return
    *   A new `PatternMatchingStats` instance that contains the aggregated statistics of both instances.
    */
  def +(that: PatternMatchingStats): PatternMatchingStats = {
    val matchesTotal = this.matches + that.matches
    val casesTotal = this.cases + that.cases

    PatternMatchingStats(
      matches = matchesTotal,
      cases = casesTotal,
      guards = this.guards + that.guards,
      wildcards = this.wildcards + that.wildcards,
      maxNesting = math.max(this.maxNesting, that.maxNesting),
      nestedMatches = this.nestedMatches + that.nestedMatches,
      // recompute weighted average
      avgCasesPerMatch = if (matchesTotal > 0) casesTotal.toDouble / matchesTotal else 0.0
    )
  }

  /**
    * Provides a formatted string representation of the pattern matching statistics.
    *
    * @return
    *   A multi-line string summarizing the pattern matching statistics.
    */
  override def formattedString: String = {
    s"""PatternMatchingStats:
       |----------------------------------------------------------
       |  Total Matches: $matches
       |  Total Cases: $cases
       |  Total Guards: $guards
       |  Total Wildcards: $wildcards
       |  Max Nesting Level: $maxNesting
       |  Total Nested Matches: $nestedMatches
       |  Average Cases per Match: $avgCasesPerMatch
       |""".stripMargin
  }
}
