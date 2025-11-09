/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.analyzer.model

/**
  * Represents metrics related to the usage of implicit conversions, inline methods, and implicit modifiers in the
  * analyzed code.
  *
  * @param hasImplicitConversion
  *   Indicates whether the analyzed code contains implicit conversions.
  * @param hasImplicitMod
  *   Indicates whether the analyzed code contains elements with the `implicit` modifier.
  * @param isInlineMethod
  *   Indicates whether the analyzed code contains inline methods.
  */
case class ImplicitInlineGivenMetrics(
    hasImplicitConversion: Boolean = false,
    hasImplicitMod: Boolean = false,
    isInlineMethod: Boolean = false
)
