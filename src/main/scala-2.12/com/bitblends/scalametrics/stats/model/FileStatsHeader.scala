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
  * @param linesOfCode
  *   Number of lines of code present within the file.
  * @param fileSizeBytes
  *   Size of the file in bytes.
  */
case class FileStatsHeader(
    projectId: String,
    fileId: String,
    fileName: String,
    filePath: String,
    packageName: String,
    linesOfCode: Int,
    fileSizeBytes: Long
) {

  /**
    * Converts the properties of the case class into a Map where the keys are the field names and the values are their
    * corresponding string representations.
    *
    * @return
    *   A Map containing the field names as keys and their corresponding values as string representations.
    */
  def toMap: Map[String, String] = {
    val fieldNames = List(
      "projectId",
      "fileId",
      "fileName",
      "filePath",
      "packageName",
      "linesOfCode",
      "fileSizeBytes"
    )
    fieldNames
      .zip(this.productIterator.toList)
      .map { case (name, value) =>
        name -> (value match {
          case Some(v)     => v.toString
          case None        => ""
          case seq: Seq[_] => seq.mkString(",")
          case v           => v.toString
        })
      }
      .toMap
  }
}
