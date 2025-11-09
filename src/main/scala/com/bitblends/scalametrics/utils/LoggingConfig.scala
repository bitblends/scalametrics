/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.utils

import java.util.logging.LogManager

object LoggingConfig {

  /**
    * Initialize logging configuration from the logging.properties file. This should be called once at application
    * startup.
    */
  def init(): Unit = {
    val configStream = getClass.getResourceAsStream("/logging.properties")
    if (configStream != null) {
      try {
        LogManager.getLogManager.readConfiguration(configStream)
      } finally {
        configStream.close()
      }
    } else {
      System.err.println("Warning: logging.properties not found in resources")
    }
  }
}
