/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.stats.model

/**
  * Represents statistical data for a package, including aggregated package-level metrics and metrics for individual
  * files within the package.
  *
  * @param rollup
  *   An aggregated summary of statistical metrics for the package, encapsulated in a `PackageRollup` instance.
  * @param fileStats
  *   A collection of file-level statistics within the package, represented as a `Vector` of `FileStats` instances.
  */
case class PackageStats(metadata: PackageMetadata, rollup: Rollup, fileStats: Vector[FileStats]) extends Serializer {

  /**
    * Generates a formatted string representation of the package statistics, including metadata, rollup metrics, and
    * detailed file statistics.
    *
    * @return
    *   A multiline string containing the complete statistics for the package.
    */
  override def formattedString: String = {
    val fileStatsStr = fileStats.map(_.formattedString).mkString("\n")
    s"""Package Stats:
       |----------------------------------------------------------
       |Metadata:
       |${metadata.formattedString}
       |Rollup:
       |${rollup.formattedString}
       |File Stats:
       |$fileStatsStr
       |""".stripMargin
  }
}
