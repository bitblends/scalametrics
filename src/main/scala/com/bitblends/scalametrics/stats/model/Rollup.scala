/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.stats.model

/**
  * Represents an aggregated summary of various statistical metrics for a software project, package, or file.
  *
  * This case class encapsulates a wide range of metrics, including counts, averages, and percentages related to code
  * complexity, documentation coverage, and other relevant statistics. It also includes nested statistics for core
  * metrics, inline and implicit usage, pattern matching, branch density, and parameter statistics.
  *
  * @param totalCount
  *   The total number of items (e.g., files or packages) included in the rollup.
  * @param averageFileSizeBytes
  *   The average size of files in bytes.
  * @param returnTypeExplicitness
  *   The percentage of definitions with explicit return types.
  * @param publicReturnTypeExplicitness
  *   The percentage of public definitions with explicit return types.
  * @param itemsWithHighComplexity
  *   The count of items with average complexity above a defined threshold.
  * @param itemsWithLowDocumentation
  *   The count of items with documentation coverage below a defined threshold.
  * @param itemsWithHighNesting
  *   The count of items with average nesting depth above a defined threshold.
  * @param itemsWithHighBranchDensity
  *   The count of items with average branch density above a defined threshold.
  * @param itemsWithHighPatternMatching
  *   The count of items with average pattern matching statistics above a defined threshold.
  * @param itemsWithHighParameterCount
  *   The count of items with average parameter count above a defined threshold.
  * @param avgCyclomaticComplexity
  *   The average cyclomatic complexity across all items.
  * @param maxCyclomaticComplexity
  *   The maximum cyclomatic complexity observed among all items.
  * @param avgNestingDepth
  *   The average nesting depth across all items.
  * @param maxNestingDepth
  *   The maximum nesting depth observed among all items.
  * @param scalaDocCoveragePercentage
  *   The overall ScalaDoc coverage percentage.
  * @param deprecatedSymbolsDensityPercentage
  *   The density percentage of deprecated symbols.
  * @param coreStats
  *   Core statistical metrics encapsulated in a `CoreStats` instance.
  * @param inlineAndImplicitStats
  *   Statistics related to inline and implicit usage encapsulated in an `InlineAndImplicitStats` instance.
  * @param patternMatchingStats
  *   Statistics related to pattern matching encapsulated in a `PatternMatchingStats` instance.
  * @param branchDensityStats
  *   Statistics related to branch density encapsulated in a `BranchDensityStats` instance.
  * @param parameterStats
  *   Statistics related to parameter usage encapsulated in a `ParameterStats` instance.
  */
case class Rollup(
    totalCount: Int = 0,
    averageFileSizeBytes: Long = 0L,
    returnTypeExplicitness: Double = 0.0,
    publicReturnTypeExplicitness: Double = 0.0,
    itemsWithHighComplexity: Int = 0, // items with avg complexity > threshold (files or packages)
    itemsWithLowDocumentation: Int = 0, // items with doc coverage < threshold (files or packages)
    itemsWithHighNesting: Int = 0, // items with avg nesting depth > threshold (files or packages)
    itemsWithHighBranchDensity: Int = 0, // items with avg branch density > threshold (files or packages)
    itemsWithHighPatternMatching: Int = 0, // items with avg pattern matching stats > threshold (files or packages)
    itemsWithHighParameterCount: Int = 0, // items with avg parameter count > threshold (files or packages)
    avgCyclomaticComplexity: Double = 0,
    maxCyclomaticComplexity: Int = 0,
    avgNestingDepth: Double = 0,
    maxNestingDepth: Int = 0,
    scalaDocCoveragePercentage: Double = 0,
    deprecatedSymbolsDensityPercentage: Double = 0,
    coreStats: CoreStats = CoreStats(),
    inlineAndImplicitStats: InlineAndImplicitStats = InlineAndImplicitStats(),
    patternMatchingStats: PatternMatchingStats = PatternMatchingStats(),
    branchDensityStats: BranchDensityStats = BranchDensityStats(),
    parameterStats: ParameterStats = ParameterStats()
) extends Serializer {

  /**
    * Combines the metrics and statistics of the current `Rollup` instance with another `Rollup` instance.
    *
    * This method performs an element-wise aggregation of all numeric properties between the two `Rollup` instances and
    * returns a new `Rollup` instance containing the combined results.
    *
    * @param that
    *   Another `Rollup` instance to be combined with the current instance.
    * @return
    *   A new `Rollup` instance representing the aggregated metrics and statistics of the two `Rollup` instances.
    */
  def +(that: Rollup): Rollup = {
    val coreStats = this.coreStats + that.coreStats
    val inlineAndImplicitStats = this.inlineAndImplicitStats + that.inlineAndImplicitStats
    val patternMatchingStats = this.patternMatchingStats + that.patternMatchingStats
    val branchDensityStats = this.branchDensityStats + that.branchDensityStats
    val parameterStats = this.parameterStats + that.parameterStats

    // create a new branchDensityStats by calculating densityPer100 and boolOpsPer100 using combined  coreStats.totalLoc
    val combinedBranchDensityStats = branchDensityStats.copy(
      densityPer100 = branchDensityStats.densityPer100(coreStats.totalLoc),
      boolOpsPer100 = branchDensityStats.boolOpsPer100(coreStats.totalLoc)
    )

    Rollup(
      coreStats = coreStats,
      totalCount = this.totalCount + that.totalCount,
      averageFileSizeBytes = if (this.totalCount + that.totalCount > 0)
        (this.averageFileSizeBytes * this.totalCount + that.averageFileSizeBytes * that.totalCount) / (this.totalCount + that.totalCount)
      else 0L,
      returnTypeExplicitness =
        if (coreStats.totalDefsValsVars > 0)
          (inlineAndImplicitStats.explicitDefsValsVars.toDouble / coreStats.totalDefsValsVars.toDouble) * 100.0
        else
          0.0,
      publicReturnTypeExplicitness =
        if (coreStats.totalPublicDefsValsVars > 0)
          (inlineAndImplicitStats.explicitPublicDefsValsVars.toDouble / coreStats.totalPublicDefsValsVars.toDouble) * 100.0
        else
          0.0,
      itemsWithHighComplexity = this.itemsWithHighComplexity + that.itemsWithHighComplexity,
      itemsWithLowDocumentation = itemsWithLowDocumentation(that),
      itemsWithHighNesting = this.itemsWithHighNesting + that.itemsWithHighNesting,
      itemsWithHighBranchDensity = this.itemsWithHighBranchDensity + that.itemsWithHighBranchDensity,
      itemsWithHighPatternMatching = this.itemsWithHighPatternMatching + that.itemsWithHighPatternMatching,
      itemsWithHighParameterCount = this.itemsWithHighParameterCount + that.itemsWithHighParameterCount,
      avgCyclomaticComplexity =
        if (this.totalCount + that.totalCount > 0)
          (this.avgCyclomaticComplexity * this.totalCount + that.avgCyclomaticComplexity * that.totalCount) / (this.totalCount + that.totalCount)
        else 0.0,
      maxCyclomaticComplexity = math.max(this.maxCyclomaticComplexity, that.maxCyclomaticComplexity),
      avgNestingDepth =
        if (this.totalCount + that.totalCount > 0)
          (this.avgNestingDepth * this.totalCount + that.avgNestingDepth * that.totalCount) / (this.totalCount + that.totalCount)
        else 0.0,
      maxNestingDepth = math.max(this.maxNestingDepth, that.maxNestingDepth),
      scalaDocCoveragePercentage = if (this.totalCount + that.totalCount > 0)
        (this.scalaDocCoveragePercentage * this.totalCount + that.scalaDocCoveragePercentage * that.totalCount) / (this.totalCount + that.totalCount)
      else 0.0,
      deprecatedSymbolsDensityPercentage =
        if (this.totalCount + that.totalCount > 0)
          (this.deprecatedSymbolsDensityPercentage * this.totalCount + that.deprecatedSymbolsDensityPercentage * that.totalCount) / (this.totalCount + that.totalCount)
        else 0.0,
      inlineAndImplicitStats = inlineAndImplicitStats,
      patternMatchingStats = patternMatchingStats,
      branchDensityStats = combinedBranchDensityStats,
      parameterStats = parameterStats
    )
  }

  /**
    * Determines whether the combined documentation coverage of two `Rollup` instances is below a specified threshold.
    *
    * The method computes the aggregate documentation coverage by summing the respective core statistics of the current
    * and provided `Rollup` instances. If the percentage of documented public symbols falls below the given threshold,
    * the method returns 1; otherwise, it returns 0.
    *
    * @param that
    *   Another `Rollup` instance whose core statistics will be combined with the current instance.
    * @param threshold
    *   The minimum acceptable documentation coverage percentage. Defaults to 50.0.
    * @return
    *   An integer indicating whether the combined documentation coverage is below the threshold. Returns 1 if below the
    *   threshold, otherwise 0.
    */
  private def itemsWithLowDocumentation(that: Rollup, threshold: Double = 50.0): Int = {
    val combinedCoreStats = this.coreStats + that.coreStats
    val documentedSymbols = combinedCoreStats.totalDocumentedPublicSymbols
    val totalPublicSymbols = combinedCoreStats.totalPublicSymbols
    val coverage =
      if (totalPublicSymbols > 0) (documentedSymbols.toDouble / totalPublicSymbols.toDouble) * 100.0 else 0.0
    if (coverage < threshold) 1 else 0
  }

  /**
    * Generates a formatted string representation of the current `Rollup` instance.
    *
    * @return
    *   A formatted string representation of the current instance.
    */
  override def formattedString: String = {
    s"""=================================================================================
       |Rollup:
       |=================================================================================
       |  Total Count: $totalCount
       |  Average File Size (Bytes): $averageFileSizeBytes
       |  Return Type Explicitness: ${"%.2f".format(returnTypeExplicitness)}%
       |  Public Return Type Explicitness: ${"%.2f".format(publicReturnTypeExplicitness)}%
       |  Items with High Complexity: $itemsWithHighComplexity
       |  Items with Low Documentation: $itemsWithLowDocumentation
       |  Items with High Nesting: $itemsWithHighNesting
       |  Items with High Branch Density: $itemsWithHighBranchDensity
       |  Items with High Pattern Matching: $itemsWithHighPatternMatching
       |  Items with High Parameter Count: $itemsWithHighParameterCount
       |  Average Cyclomatic Complexity: $avgCyclomaticComplexity
       |  Max Cyclomatic Complexity: $maxCyclomaticComplexity
       |  Average Nesting Depth: $avgNestingDepth
       |  Max Nesting Depth: $maxNestingDepth
       |  Scaladoc Coverage Percentage: $scalaDocCoveragePercentage%
       |  Deprecated Symbols Density Percentage: $deprecatedSymbolsDensityPercentage%
       |
       |${coreStats.formattedString}
       |
       |${inlineAndImplicitStats.formattedString}
       |
       |${patternMatchingStats.formattedString}
       |
       |${branchDensityStats.formattedString}
       |
       |${parameterStats.formattedString}
       |""".stripMargin
  }
}
