/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.utils

import java.util.logging.{Level, Logger}

trait ScalaMetricsLogger {

  private lazy val logger = {
    val name = this.getClass.getName
    Logger.getLogger(if (name.endsWith("$")) name.dropRight(1) else name)
  }

  def debug(msg: => String): Unit =
    if (logger.isLoggable(Level.FINE)) logger.fine(msg)

  def info(msg: => String): Unit =
    if (logger.isLoggable(Level.INFO)) logger.info(msg)

  def warn(msg: => String): Unit =
    if (logger.isLoggable(Level.WARNING)) logger.warning(msg)

  def error(msg: => String, t: Throwable = null): Unit = {
    if (logger.isLoggable(Level.SEVERE)) {
      if (t eq null) logger.severe(msg)
      else {
        logger.severe(msg)
        logger.log(Level.SEVERE, msg, t)
      }
    }
  }
}
