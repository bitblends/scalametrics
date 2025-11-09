/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.analyzer.model

/**
  * Represents a summary of return type declarations within a codebase, including their overall and public visibility
  * statistics.
  *
  * @param totalDefs
  *   The total number of definitions analyzed in the codebase.
  * @param totalPublicDefs
  *   The total number of public definitions analyzed in the codebase.
  * @param explicitDefs
  *   The total number of definitions with explicitly specified return types.
  * @param explicitPublicDefs
  *   The total number of public definitions with explicitly specified return types.
  */
final case class ReturnTypeSummary(
    totalDefs: Int,
    totalPublicDefs: Int,
    explicitDefs: Int,
    explicitPublicDefs: Int
) {

  /**
    * Calculates the percentage of definitions that have explicitly specified return types.
    *
    * The percentage is computed as the ratio of `explicitDefs` to `totalDefs` multiplied by 100. If `totalDefs` is 0,
    * the method returns 100.0 to indicate that all (zero) definitions are considered explicitly specified.
    *
    * @return
    *   The percentage of definitions with explicitly specified return types as a Double.
    */
  def pctExplicit: Double =
    if (totalDefs == 0) 100.0 else (explicitDefs.toDouble / totalDefs) * 100.0

  /**
    * Calculates the percentage of public definitions that have explicitly specified return types.
    *
    * The percentage is computed as the ratio of `explicitPublicDefs` to `totalPublicDefs` multiplied by 100. If
    * `totalPublicDefs` is 0, the method returns 100.0 to indicate that all (zero) public definitions are considered
    * explicitly specified.
    *
    * @return
    *   The percentage of public definitions with explicitly specified return types as a Double.
    */
  def pctExplicitPublic: Double =
    if (totalPublicDefs == 0) 100.0 else (explicitPublicDefs.toDouble / totalPublicDefs) * 100.0
}
