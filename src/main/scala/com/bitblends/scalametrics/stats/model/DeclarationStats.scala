/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.stats.model

/**
  * Represents statistics related to declarations within a file, including member and method statistics.
  *
  * @param memberStats
  *   A collection of statistics for various members (e.g., values, variables, types, classes, objects, traits) defined
  *   within a file.
  * @param methodStats
  *   A collection of statistics for methods defined within a file, including detailed metrics such as cyclomatic
  *   complexity, parameter usage, and branch density.
  */
case class DeclarationStats(
    memberStats: Vector[MemberStats],
    methodStats: Vector[MethodStats]
)
