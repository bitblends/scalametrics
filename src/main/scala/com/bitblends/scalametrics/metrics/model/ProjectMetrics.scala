/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.metrics.model

/**
  * Represents aggregated metrics for a project, combining project-level information and detailed metrics for individual
  * files.
  *
  * @param projectInfo
  *   Metadata and configuration details about the project, including name, version, Scala version, and other relevant
  *   information.
  * @param fileMetrics
  *   A collection of analysis results for each file in the project, encompassing metrics at the file level and detailed
  *   insights about methods and members within the file.
  */
case class ProjectMetrics(
    projectInfo: ProjectInfo,
    fileMetrics: Vector[FileMetrics]
)
