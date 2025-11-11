package com.bitblends.scalametrics.stats.model

/**
  * Represents statistical data related to branch density and boolean operations in a given codebase.
  *
  * @param branches
  *   Total number of branches in the analyzed code.
  * @param ifCount
  *   Count of `if` statements present in the code.
  * @param caseCount
  *   Number of `case` expressions present in the code.
  * @param loopCount
  *   Count of loop constructs such as `for`, `while`, or similar in the code.
  * @param catchCaseCount
  *   Total count of `catch-case` blocks in the code.
  * @param boolOpsCount
  *   Number of boolean operations such as `&&`, `||`, etc., in the code.
  * @param densityPer100
  *   Measure of branch density, normalized per 100 lines of code.
  * @param boolOpsPer100
  *   Measure of boolean operations, normalized per 100 lines of code.
  */
case class BranchDensityStats(
    branches: Int,
    ifCount: Int,
    caseCount: Int,
    loopCount: Int,
    catchCaseCount: Int,
    boolOpsCount: Int,
    densityPer100: Double,
    boolOpsPer100: Double
) extends StatsBase
