/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.stats.model

/**
  * Represents core statistical metrics for analyzing codebases and their components.
  *
  * This case class provides various metrics, including lines of code, function counts, symbol statistics, and size
  * information. It supports operations such as combining metrics with another `CoreStats` instance and can generate a
  * formatted string representation for easier interpretation of the data.
  *
  * @param totalLoc
  *   The total number of lines of code.
  * @param totalFunctions
  *   The total number of functions in the codebase.
  * @param totalPublicFunctions
  *   The total number of public functions in the codebase.
  * @param totalPrivateFunctions
  *   The total number of private functions in the codebase.
  * @param totalFileSizeBytes
  *   The total file size in bytes of the codebase.
  * @param totalSymbols
  *   The total number of symbols in the codebase.
  * @param totalPublicSymbols
  *   The total number of public symbols in the codebase.
  * @param totalPrivateSymbols
  *   The total number of private symbols in the codebase.
  * @param totalNestedSymbols
  *   The total number of nested symbols in the codebase.
  * @param totalDocumentedPublicSymbols
  *   The total number of documented public symbols in the codebase.
  * @param totalDeprecatedSymbols
  *   The total number of deprecated symbols in the codebase.
  * @param totalDefsValsVars
  *   The total number of definitions, vals, and vars in the codebase.
  * @param totalPublicDefsValsVars
  *   The total number of public definitions, vals, and vars in the codebase.
  */
case class CoreStats(
    totalLoc: Int = 0,
    totalFunctions: Int = 0,
    totalPublicFunctions: Int = 0,
    totalPrivateFunctions: Int = 0,
    totalFileSizeBytes: Long = 0L,
    totalSymbols: Int = 0,
    totalPublicSymbols: Int = 0,
    totalPrivateSymbols: Int = 0,
    totalNestedSymbols: Int = 0,
    totalDocumentedPublicSymbols: Int = 0,
    totalDeprecatedSymbols: Int = 0,
    totalDefsValsVars: Int = 0,
    totalPublicDefsValsVars: Int = 0
) extends Serializer {

  /**
    * Combines the metrics of the current `CoreStats` instance with another `CoreStats` instance.
    *
    * This method performs an element-wise addition of all properties between the two `CoreStats` instances and returns
    * a new `CoreStats` instance containing the aggregated metrics.
    *
    * @param that
    *   Another `CoreStats` instance to combine with the current instance.
    * @return
    *   A new `CoreStats` instance representing the sum of the metrics from both instances.
    */
  def +(that: CoreStats): CoreStats =
    CoreStats(
      totalLoc = this.totalLoc + that.totalLoc,
      totalFunctions = this.totalFunctions + that.totalFunctions,
      totalPublicFunctions = this.totalPublicFunctions + that.totalPublicFunctions,
      totalPrivateFunctions = this.totalPrivateFunctions + that.totalPrivateFunctions,
      totalFileSizeBytes = this.totalFileSizeBytes + that.totalFileSizeBytes,
      totalSymbols = this.totalSymbols + that.totalSymbols,
      totalPublicSymbols = this.totalPublicSymbols + that.totalPublicSymbols,
      totalPrivateSymbols = this.totalPrivateSymbols + that.totalPrivateSymbols,
      totalNestedSymbols = this.totalNestedSymbols + that.totalNestedSymbols,
      totalDocumentedPublicSymbols = this.totalDocumentedPublicSymbols + that.totalDocumentedPublicSymbols,
      totalDeprecatedSymbols = this.totalDeprecatedSymbols + that.totalDeprecatedSymbols,
      totalDefsValsVars = this.totalDefsValsVars + that.totalDefsValsVars,
      totalPublicDefsValsVars = this.totalPublicDefsValsVars + that.totalPublicDefsValsVars
    )

  /**
    * Provides a formatted string representation of the current `CoreStats` instance.
    *
    * This method organizes the core statistical data into a human-readable format. The resulting output displays key
    * metrics such as total lines of code, number of functions, symbol statistics, and other related details, making it
    * easier to review and analyze the metrics in a structured way.
    *
    * @return
    *   A string containing the formatted summary of the `CoreStats` metrics.
    */
  override def formattedString: String = {
    s"""CoreStats:
       |----------------------------------------------------------
       |  Total Lines of Code: $totalLoc
       |  Total Functions: $totalFunctions
       |  Total Public Functions: $totalPublicFunctions
       |  Total Private Functions: $totalPrivateFunctions
       |  Total File Size (Bytes): $totalFileSizeBytes
       |  Total Symbols: $totalSymbols
       |  Total Public Symbols: $totalPublicSymbols
       |  Total Private Symbols: $totalPrivateSymbols
       |  Total Nested Symbols: $totalNestedSymbols
       |  Total Documented Public Symbols: $totalDocumentedPublicSymbols
       |  Total Deprecated Symbols: $totalDeprecatedSymbols
       |  Total Defs, Vals, Vars: $totalDefsValsVars
       |  Total Public Defs, Vals, Vars: $totalPublicDefsValsVars
     """.stripMargin
  }
}
