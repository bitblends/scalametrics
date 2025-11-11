/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.stats.model

/**
  * Represents the aggregated statistical data for a source file in a software project.
  *
  * This class encapsulates the metadata, calculated metrics, and detailed statistics for the file, combining high-level
  * rollup metrics and granular information for its members and methods. It is designed to provide a comprehensive view
  * of a file's structure and characteristics in the context of the overall project.
  *
  * @param metadata
  *   Metadata and header information about the file, including its name, size, and associated package.
  * @param fileRollup
  *   Aggregated metrics summarizing various attributes of the file, such as lines of code, function statistics, symbol
  *   statistics, and pattern matching usage, among others.
  * @param memberStats
  *   Statistical data for individual members within the file, providing information on their characteristics, such as
  *   size, visibility, and complexity.
  * @param methodStats
  *   Detailed statistics for methods defined in the file, including metrics on performance, complexity, and patterns
  *   used within the code.
  */
case class FileStats(
    metadata: FileStatsMetadata,
    fileRollup: FileRollup,
    memberStats: Vector[MemberStats],
    methodStats: Vector[MethodStats]
)
