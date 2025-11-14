/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.stats.model

/**
  * Represents metadata and header information about a file within a project. This class captures essential attributes
  * of a file, such as its identification, name, associated package, size, and the number of lines of code.
  *
  * @param projectId
  *   Unique identifier of the project to which the file belongs.
  * @param fileId
  *   Unique identifier for the file within the project.
  * @param fileName
  *   Name of the file, including extension.
  * @param filePath
  *   Relative path from project root to the file.
  * @param packageName
  *   Fully qualified name of the package containing the file.
  */
case class FileStatsMetadata(
    projectId: String,
    fileId: String,
    fileName: String,
    filePath: String,
    packageName: String,
    linesOfCode: Int,
    fileSizeBytes: Long
) extends Serializer {

  /**
    * Generates a formatted string representation of the file's metadata.
    *
    * @return
    *   A multiline string containing the file's metadata details.
    */
  override def formattedString: String = {
    s"""File Stats Metadata:
       |----------------------------------------------------------
       |  Project ID: $projectId
       |  File ID: $fileId
       |  File Name: $fileName
       |  File Path: $filePath
       |  Package Name: $packageName
       |  Lines of Code: $linesOfCode
       |  File Size (bytes): $fileSizeBytes
       |""".stripMargin
  }
}
