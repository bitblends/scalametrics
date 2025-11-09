/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.stats.model

/**
  * Represents statistical data for a package, including aggregated package-level metrics and metrics for individual
  * files within the package.
  *
  * @param packageRollup
  *   An aggregated summary of statistical metrics for the package, encapsulated in a `PackageRollup` instance.
  * @param fileStats
  *   A collection of file-level statistics within the package, represented as a `Vector` of `FileStats` instances.
  */
case class Package(packageRollup: PackageRollup, fileStats: Vector[FileStats])
