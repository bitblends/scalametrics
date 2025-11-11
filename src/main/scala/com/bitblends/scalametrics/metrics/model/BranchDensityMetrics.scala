package com.bitblends.scalametrics.metrics.model

/**
  * Represents metrics related to branch density for a given code symbol. This case class provides detailed insights
  * into the number and types of branching constructs, as well as their density and complexity.
  *
  * @param loc
  *   The total number of lines of code (LOC) associated with the code symbol being analyzed.
  * @param branches
  *   The total number of branching points identified within the code symbol. This includes if conditions, loops, case
  *   matches, and catch blocks.
  * @param ifCount
  *   The number of `if` statements identified within the code symbol.
  * @param caseCount
  *   The count of `case` patterns used in the code symbol.
  * @param loopCount
  *   The number of looping constructs (e.g., `for`, `while`, `do-while`) present in the code.
  * @param catchCaseCount
  *   The count of `catch` blocks used for exception handling within the code symbol.
  * @param boolOpsCount
  *   The number of boolean operations (e.g., `&&`, `||`) found in the code, which contribute to branching logic.
  * @param densityPer100
  *   The calculated branch density normalized per 100 lines of code. This metric provides a measure of branching
  *   intensity relative to the size of the code.
  * @param boolOpsPer100
  *   The number of boolean operations normalized per 100 lines of code. This metric reflects the logical complexity in
  *   relation to the overall code volume.
  */
case class BranchDensityMetrics(
    loc: Int = 0,
    branches: Int = 0,
    ifCount: Int = 0,
    caseCount: Int = 0,
    loopCount: Int = 0,
    catchCaseCount: Int = 0,
    boolOpsCount: Int = 0,
    densityPer100: Double = 0.0,
    boolOpsPer100: Double = 0.0
)
