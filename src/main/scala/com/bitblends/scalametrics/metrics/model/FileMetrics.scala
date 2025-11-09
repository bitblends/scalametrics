/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.metrics.model

import com.bitblends.scalametrics.utils.{FileId, RepoRoot}

import java.io.File

/**
  * Represents metrics associated with a specific file in a project, including information about the file's project,
  * identifier, package, size, and lines of code.
  *
  * @param projectId
  *   An optional identifier for the project to which the file belongs.
  * @param fileId
  *   The unique identifier for the file within the project.
  * @param file
  *   The file object representing the file on the filesystem.
  * @param packageName
  *   The package name where the file resides within the project.
  * @param linesOfCode
  *   The total number of lines of code present in the file.
  * @param fileSizeBytes
  *   The size of the file in bytes.
  */
case class FileMetrics(
    projectId: Option[String],
    fileId: String,
    file: File,
    packageName: String,
    linesOfCode: Int,
    fileSizeBytes: Long
)

/**
  * Companion object for the `FileMetrics` case class, providing factory methods to create instances of `FileMetrics`
  * with varying levels of detail.
  */
object FileMetrics {

  /**
    * Creates an instance of `FileMetrics` with no project-specific identifier.
    *
    * @param file
    *   the file object representing the file on the filesystem
    * @param packageName
    *   the package name where the file resides within the project
    * @param linesOfCode
    *   the total number of lines of code present in the file
    * @param fileSizeBytes
    *   the size of the file in bytes
    * @return
    *   an instance of `FileMetrics` containing information about the specified file
    */
  def apply(
      file: File,
      packageName: String,
      linesOfCode: Int,
      fileSizeBytes: Long
  ): FileMetrics = {
    FileMetrics(
      None,
      fileId = FileId.idFor(file, None, RepoRoot.discover()),
      file,
      packageName,
      linesOfCode,
      fileSizeBytes
    )
  }

  /**
    * Creates an instance of `FileMetrics` with a project-specific identifier.
    *
    * @param projectId
    *   The unique identifier of the project to which the file belongs.
    * @param file
    *   The file object representing the file on the filesystem.
    * @param packageName
    *   The package name where the file resides within the project.
    * @param linesOfCode
    *   The total number of lines of code present in the file.
    * @param fileSizeBytes
    *   The size of the file in bytes.
    * @return
    *   An instance of `FileMetrics` containing detailed information about the specified file, including its project ID.
    */
  def apply(
      projectId: String,
      file: File,
      packageName: String,
      linesOfCode: Int,
      fileSizeBytes: Long
  ): FileMetrics =
    FileMetrics(
      Some(projectId),
      fileId = FileId.idFor(file, Some(projectId), RepoRoot.discover()),
      file,
      packageName,
      linesOfCode,
      fileSizeBytes
    )

}
