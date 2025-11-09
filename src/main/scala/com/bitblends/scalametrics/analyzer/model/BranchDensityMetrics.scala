/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.analyzer.model

/**
  * Represents metrics related to branch density for a given code entity.
  *
  * @param loc
  *   The total number of lines in the analyzed body (physical Lines of Code).
  * @param branches
  *   The total count of branch tokens found in the analyzed code.
  * @param ifCount
  *   The number of `if` statements found in the code.
  * @param caseCount
  *   The number of `case` branches found within match expressions.
  * @param loopCount
  *   The total number of loops found, including `while`, `do`, and all `for` loops (`for` and `for-yield`).
  * @param catchCaseCount
  *   The number of `catch` clauses present in the code.
  * @param boolOpsCount
  *   The number of boolean operations (`&&` and `||`) present in the code.
  */
final case class BranchDensityMetrics(
    loc: Int, // lines in the analyzed body (physical LOC)
    branches: Int, // total branch tokens counted
    ifCount: Int,
    caseCount: Int,
    loopCount: Int, // while + do + for/for-yield
    catchCaseCount: Int,
    boolOpsCount: Int // && and ||
) {

  /**
    * Computes the branch density, defined as the number of branch tokens (e.g., if, case, while, do, for, etc.) per 100
    * lines of code (LOC). If the number of lines of code is zero, it returns 0.0 to prevent division by zero.
    *
    * @return
    *   The branch density per 100 lines of code as a Double.
    */
  def densityPer100: Double = if (loc == 0) 0.0 else 100.0 * branches.toDouble / loc

  /**
    * Computes the number of boolean operations (e.g., &&, ||) per 100 lines of code. Returns 0.0 if the number of lines
    * of code is zero to avoid division by zero.
    *
    * @return
    *   The number of boolean operations per 100 lines of code as a Double.
    */
  def boolOpsPer100: Double = if (loc == 0) 0.0 else 100.0 * boolOpsCount.toDouble / loc
}
