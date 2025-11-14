/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.stats.model

/**
  * Represents statistics related to branch density within a codebase. This includes counts of various branching
  * structures (e.g., if-statements, loops, case expressions) and boolean operations, along with their calculated
  * densities per 100 lines of code.
  *
  * This class enables the analysis and combination of branch density statistics for different parts of a codebase.
  *
  * @param branches
  *   The total number of branching structures (e.g., if-statements, case expressions).
  * @param ifCount
  *   The number of if-statements.
  * @param caseCount
  *   The number of case expressions.
  * @param loopCount
  *   The number of loop structures.
  * @param catchCaseCount
  *   The number of catch-case blocks.
  * @param boolOpsCount
  *   The number of boolean operations (e.g., &&, ||).
  * @param densityPer100
  *   Precomputed overall branch density per 100 lines of code.
  * @param boolOpsPer100
  *   Precomputed density of boolean operations per 100 lines of code.
  */
case class BranchDensityStats(
    branches: Int = 0,
    ifCount: Int = 0,
    caseCount: Int = 0,
    loopCount: Int = 0,
    catchCaseCount: Int = 0,
    boolOpsCount: Int = 0,
    densityPer100: Double = 0.0,
    boolOpsPer100: Double = 0.0
) extends Serializer {

  /**
    * Calculates the branch density per 100 lines of code.
    *
    * @param totalLoc
    *   The total number of lines of code.
    * @return
    *   The branch density per 100 lines of code. Returns 0.0 if totalLoc is 0.
    */
  def densityPer100(totalLoc: Int): Double =
    if (totalLoc == 0) 0.0
    else 100.0 * branches.toDouble / totalLoc.toDouble

  /**
    * Calculates the density of boolean operations per 100 lines of code.
    *
    * @param totalLoc
    *   The total number of lines of code.
    * @return
    *   The density of boolean operations per 100 lines of code. Returns 0.0 if totalLoc is 0.
    */
  def boolOpsPer100(totalLoc: Int): Double =
    if (totalLoc == 0) 0.0
    else 100.0 * boolOpsCount.toDouble / totalLoc.toDouble

  /**
    * Combines the branch density statistics of this instance with those of another instance.
    *
    * @param that
    *   The other `BranchDensityStats` instance to be combined with this one.
    * @return
    *   A new `BranchDensityStats` instance containing the sum of all corresponding metrics.
    */
  def +(that: BranchDensityStats): BranchDensityStats =
    BranchDensityStats(
      branches = this.branches + that.branches,
      ifCount = this.ifCount + that.ifCount,
      caseCount = this.caseCount + that.caseCount,
      loopCount = this.loopCount + that.loopCount,
      catchCaseCount = this.catchCaseCount + that.catchCaseCount,
      boolOpsCount = this.boolOpsCount + that.boolOpsCount
    )

  /**
    * Generates a formatted string representation of the current `BranchDensityStats` instance.
    *
    * @return
    *   A formatted string representation of the current instance.
    */
  override def formattedString: String = {
    s"""BranchDensityStats:
       |----------------------------------------------------------
       |  Total Branches: $branches
       |    If Statements: $ifCount
       |    Case Expressions: $caseCount
       |    Loops: $loopCount
       |    Catch-Case Blocks: $catchCaseCount
       |  Total Boolean Operations: $boolOpsCount
       |  Branch Density: ${"%.2f".format(densityPer100)} per 100 LOC
       |  Boolean Operation Density: ${"%.2f".format(boolOpsPer100)} per 100 LOC
       |""".stripMargin
  }
}
