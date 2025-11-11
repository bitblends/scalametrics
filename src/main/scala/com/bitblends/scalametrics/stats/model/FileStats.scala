/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.stats.model

/**
  * Represents statistical information about a file, including metadata, rollup metrics, and declaration-based
  * statistics.
  *
  * @param header
  *   Metadata associated with the file, such as its name, size, and lines of code.
  * @param fileRollup
  *   Aggregated metrics or summarized statistics for the file.
  * @param declarationStats
  *   Statistics related to declarations present within the file.
  */
case class FileStats(
    header: FileStatsHeader,
    fileRollup: FileRollup,
    declarationStats: DeclarationStats
)
