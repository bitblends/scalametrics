/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.stats.model

/**
  * Represents a project with its associated metadata, rollup information, and a collection of packages.
  *
  * @param metadata
  *   Contains metadata and descriptive information about the project such as name, version, Scala version, and other
  *   optional details.
  * @param projectRollup
  *   Aggregated or summary information related to the project.
  * @param packages
  *   A collection of packages associated with the project.
  */
case class ProjectStats(
    metadata: ProjectMetadata,
    projectRollup: ProjectRollup,
    packages: Vector[Package]
)
