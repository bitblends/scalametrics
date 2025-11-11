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
  */
case class PatternMatchingStats(
    matches: Int,
    cases: Int,
    guards: Int,
    wildcards: Int,
    maxNesting: Int,
    nestedMatches: Int
) extends StatsBase
