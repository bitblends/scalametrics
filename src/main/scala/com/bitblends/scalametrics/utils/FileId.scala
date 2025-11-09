/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.utils

import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.security.MessageDigest

/**
  * Utility object for generating deterministic file identifiers and handling path normalization.
  */
object FileId {

  /**
    * Generates a deterministic identifier for a file within a project. The identifier is a compact, SHA-1-based hash
    * created using the project ID and the normalized path of the file.
    *
    * <code>sha1(projectId + ":" + normalizedPath)</code>
    *
    * @param file
    *   the file for which the identifier is generated
    * @param repoRootOpt
    *   an optional path to the repository root used to normalize the file path; if not provided, the file's absolute
    *   path is used
    * @param projectId
    *   the unique identifier of the project
    * @return
    *   a deterministic 16-character hexadecimal string that uniquely identifies the file
    */
  def idFor(file: java.io.File, projectId: Option[String] = None, repoRootOpt: Option[Path] = None): String = {
    val input = s"""${projectId.map(p => s"$p:")}${normalizedPath(file, repoRootOpt)}"""
    val md = MessageDigest.getInstance("SHA-1")
    val digest = md.digest(input.getBytes(StandardCharsets.UTF_8))
    // take first 16 hex chars (8 bytes) for compact id
    hex(digest).take(16)
  }

  /**
    * Converts an array of bytes into a hexadecimal string.
    *
    * @param bytes
    *   the array of bytes to be converted into a hexadecimal string
    * @return
    *   a string representing the hexadecimal format of the input byte array
    */
  private def hex(bytes: Array[Byte]): String =
    bytes.map("%02x".format(_)).mkString

  /**
    * Normalizes the file path to a consistent format. If a repository root path is provided, the method attempts to
    * produce a relative path to the file based on the root; otherwise, it returns the absolute normalized path of the
    * file. Backslashes are replaced with forward slashes for compatibility.
    *
    * @param file
    *   the file whose path is to be normalized
    * @param repoRootOpt
    *   an optional path to the repository root to compute a relative path; if not provided or if the roots differ, the
    *   absolute normalized file path is returned (should be the project root)
    * @return
    *   a normalized string representation of the file path, using forward slashes as path separators
    */
  def normalizedPath(file: java.io.File, repoRootOpt: Option[Path] = None): String = {
    val p = file.toPath.toAbsolutePath.normalize
    repoRootOpt match {
      case Some(root) =>
        try {
          root.toAbsolutePath.normalize.relativize(p).toString.replace('\\', '/')
        } catch {
          case _: IllegalArgumentException => // can't relativize (different roots)
            p.toString.replace('\\', '/')
        }
      case None =>
        p.toString.replace('\\', '/')
    }
  }
}
