/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.stats.model

/**
  * Represents metadata information for a package within a project.
  *
  * @param projectId
  *   The unique identifier of the project to which the package belongs.
  * @param name
  *   The name of the package.
  */
case class PackageMetadata(
    projectId: String,
    name: String
) extends Serializer {

  /**
    * Generates a formatted string representation of the package's metadata.
    *
    * @return
    *   A multiline string containing the package's metadata details.
    */
  override def formattedString: String = {
    s"""Package Metadata:
       |----------------------------------------------------------
       |  Project ID: $projectId
       |  Package Name: $name
       |""".stripMargin
  }
}
