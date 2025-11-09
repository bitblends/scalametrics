/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.utils

import java.security.MessageDigest

/**
  * Utility object for generating compact, deterministic identifiers based on the provided string parts. The identifiers
  * are created using a SHA-1 hash computation truncated to 8 bytes, represented as a hexadecimal string.
  */
object Id {
  private val sha1 = MessageDigest.getInstance("SHA-1")

  /**
    * Generates a compact, deterministic identifier based on the provided string parts. Combines the given parts into a
    * single input, computes a SHA-1 hash, and returns the first 8 bytes of the hash as a hexadecimal string.
    *
    * @param parts
    *   the sequence of string components to be combined and hashed
    * @return
    *   a compact 16-character hexadecimal string representing the hash of the combined input
    */
  def of(parts: String*): String = {
    val md = sha1.clone().asInstanceOf[MessageDigest]
    parts.foreach(p => md.update(p.getBytes("UTF-8")))
    md.digest().take(8).map("%02x".format(_)).mkString
  }
}
