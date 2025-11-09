/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.utils

import java.io.File
import scala.io.Source
import scala.meta.Dialect
import scala.meta.dialects.{
  Scala210,
  Scala211,
  Scala212,
  Scala212Source3,
  Scala213,
  Scala213Source3,
  Scala3,
  Scala30,
  Scala31,
  Scala32,
  Scala33,
  Scala36,
  Scala37,
  Scala38
}
import scala.util.{Try, Using}

/**
  * DialectConfig provides utilities for managing and mapping Scala dialects based on Scalafmt configuration, source
  * directories, or project settings.
  */
object DialectConfig {

  /**
    * A mapping of string identifiers to Scalameta dialects.
    *
    * This map provides a convenient way to translate common representations of Scala versions (e.g., "2.13",
    * "scala211", "dotty") into the corresponding Scalameta dialects (`Dialect`).
    *
    * Common use cases include:
    *   - Translating version strings in build tools (e.g., SBT) to dialects
    *   - Resolving aliases such as "dotty" to `Scala3`
    *   - Supporting fallbacks for unsupported versions of Scala (e.g., "3.4" and "3.5" mapped to `Scala3`)
    *
    * Notes:
    *   - Additional versions such as "Scala34" and "Scala35" may be supported in future Scalameta releases.
    *   - Aliases follow typical usage patterns in tools like Scalafmt.
    */
  val dialectMap: Map[String, Dialect] = Map(
    "scala210" -> Scala210,
    "scala211" -> Scala211,
    "scala212" -> Scala212,
    "scala212source3" -> Scala212Source3,
    "scala213" -> Scala213,
    "scala213source3" -> Scala213Source3,
    "scala3" -> Scala38, // Default to latest Scala 3 dialect
    "scala30" -> Scala30,
    "scala31" -> Scala31,
    "scala32" -> Scala32,
    "scala33" -> Scala33,
    // Note: Scala34 and Scala35 may not be available in older Scalameta versions
    // Uncomment these lines if using Scalameta 4.15.0 or higher
    // "scala34" -> Scala34,
    // "scala35" -> Scala35,
    "scala36" -> Scala36,
    "scala37" -> Scala37,
    "scala38" -> Scala38,
    // Aliases commonly used in Scalafmt
    "Scala210" -> Scala210,
    "Scala211" -> Scala211,
    "Scala212" -> Scala212,
    "Scala212Source3" -> Scala212Source3,
    "Scala213" -> Scala213,
    "Scala213Source3" -> Scala213Source3,
    "Scala3" -> Scala38, // Default to latest Scala 3 dialect
    "Scala30" -> Scala30,
    "Scala31" -> Scala31,
    "Scala32" -> Scala32,
    "Scala33" -> Scala33,
    "Scala36" -> Scala36,
    "Scala37" -> Scala37,
    "Scala38" -> Scala38,
    // "Scala34" -> Scala34,
    // "Scala35" -> Scala35,
    // Additional common aliases
    "2.10" -> Scala210,
    "2.11" -> Scala211,
    "2.12" -> Scala212,
    "2.13" -> Scala213,
    "3" -> Scala3,
    "3.0" -> Scala30,
    "3.1" -> Scala31,
    "3.2" -> Scala32,
    "3.3" -> Scala33,
    "3.4" -> Scala3, // Fall back to Scala3 for unsupported versions
    "3.5" -> Scala3, // Fall back to Scala3 for unsupported versions
    "dotty" -> Scala3,
    "Dotty" -> Scala3
  )

  /**
    * Determines the appropriate dialect for a file based on its source directory
    *
    * Order of precedence:
    *   1. If source dir has full version (e.g., "scala-2.13", "scala-3.3"): Use exact dialect, no further checks needed
    *   2. If source dir is "scala-2" without minor: Check .scalafmt.conf, build.sbt, then default
    *   3. If source dir is "scala-3" without minor: Check .scalafmt.conf (if not "scala3"), then build.sbt/cross
    *      versions
    *   4. If source dir is "scala": Check .scalafmt.conf first, then fall back to build.sbt scalaVersion
    *
    * @param file
    *   The source file being analyzed
    * @param projectBaseDir
    *   The base directory of the project
    * @param scalaVersion
    *   The Scala version from SBT configuration
    * @param crossVersions
    *   Cross Scala versions from SBT configuration
    * @return
    *   The appropriate Scalameta Dialect
    */
  def getDialectForFile(
      file: File,
      projectBaseDir: File,
      scalaVersion: String,
      crossVersions: Seq[String] = Seq.empty
  ): Dialect = {

    /**
      * Finds the Scala source directory for a given file by traversing up its parent directories.
      * @param f
      *   A File object representing the source file.
      * @return
      *   An Option containing a tuple of (type, directory name) if a Scala source directory is found, None otherwise.
      */
    def findScalaSourceDir(f: File): Option[(String, String)] = {
      Iterator
        .iterate(Option(f.getParentFile))(_.flatMap(p => Option(p.getParentFile)))
        .takeWhile(_.isDefined)
        .flatten
        .map(_.getName)
        .collectFirst {
          case name if name.matches("scala-2\\.\\d+(\\.\\d+)?") => ("scala-2-full", name)
          case name if name.matches("scala-3\\.\\d+(\\.\\d+)?") => ("scala-3-full", name)
          case name @ "scala-2"                                 => ("scala-2", name)
          case name @ "scala-3"                                 => ("scala-3", name)
          case name @ "scala"                                   => ("scala", name)
          case _                                                => ("", "") // ignore other dirs
        }
    }

    // Read .scalafmt.conf if it exists
    val dialectFromScalafmt: Option[Dialect] = getDialectFromScalafmt(projectBaseDir)

    // Find which source directory contains this file
    findScalaSourceDir(file) match {

      // Case 1: Directory has full version (e.g., "scala-2.13", "scala-3.3") - highest priority
      case Some(("scala-2-full", dirName)) =>
        // Extract version and use it directly - no need to check .scalafmt.conf or build.sbt
        val versionPattern = """scala-(2\.(\d+)(?:\.(\d+))?)""".r
        dirName match {
          case versionPattern(fullVersion, _, _) => dialectFromScalaVersion(fullVersion)
          // Shouldn't happen due to regex check, but fallback
          case _ => Scala213
        }

      case Some(("scala-3-full", dirName)) =>
        // Extract version and use it directly - no need to check .scalafmt.conf or build.sbt
        val versionPattern = """scala-(3\.(\d+)(?:\.(\d+))?)""".r
        dirName match {
          case versionPattern(fullVersion, _, _) => dialectFromScalaVersion(fullVersion)
          // Shouldn't happen due to regex check, but fallback
          case _ => Scala3
        }

      // Case 2: Directory is "scala-2" without minor version
      case Some(("scala-2", _)) =>
        // Check .scalafmt.conf for Scala 2.x dialect
        dialectFromScalafmt
          .filter { d =>
            val dStr = d.toString
            dStr.contains("Scala2") && !dStr.contains("Source3")
          }
          .getOrElse {
            // Check if main scala version is 2.x
            if (scalaVersion.startsWith("2.")) {
              dialectFromScalaVersion(scalaVersion)
            } else {
              // Check cross versions for any Scala 2.x version
              crossVersions
                .find(_.startsWith("2."))
                .map(dialectFromScalaVersion)
                .getOrElse(Scala213) // Default Scala 2 dialect
            }
          }

      // Case 3: Directory is "scala-3" without minor version
      case Some(("scala-3", _)) =>
        // Check .scalafmt.conf, but only use it if it's NOT generic "scala3"
        val scalafmtDialect: Option[Dialect] = dialectFromScalafmt.flatMap { d =>
          if (d.toString.equalsIgnoreCase("scala3")) {
            None // Ignore generic scala3, need more specific version
          } else if (d.toString.contains("Scala3")) {
            Some(d) // Use specific Scala 3.x dialect from scalafmt
          } else {
            None
          }
        }

        scalafmtDialect.getOrElse {
          // Check if main scala version is 3.x
          if (scalaVersion.startsWith("3.")) {
            dialectFromScalaVersion(scalaVersion)
          } else {
            // Check cross versions for any Scala 3.x version
            crossVersions
              .find(_.startsWith("3."))
              .map(dialectFromScalaVersion)
              .getOrElse(Scala33) // Default to Scala 3.3 as a reasonable default
          }
        }

      // Case 4: Directory is just "scala" or no scala directory found
      case Some(("scala", _)) | Some((_, _)) | None =>
        // Standard scala directory - check .scalafmt.conf first
        dialectFromScalafmt.getOrElse {
          // Fall back to build.sbt scalaVersion
          dialectFromScalaVersion(scalaVersion)
        }

    }
  }

  /**
    * Converts a given Scala version string to its corresponding Scalameta Dialect. Uses the major, minor, and patch
    * components of the version to determine the appropriate dialect.
    *
    * @param scalaVersion
    *   the Scala version string (e.g., "2.12.10", "3.3.1")
    * @return
    *   the corresponding Scalameta Dialect for the provided Scala version
    */
  def dialectFromScalaVersion(scalaVersion: String): Dialect = {
    // Converts a Scala version string to the appropriate dialect
    val versionParts = scalaVersion.split('.')

    if (versionParts.length >= 2) {
      val major = versionParts(0)
      val minor = versionParts(1)
      val patch =
        if (versionParts.length >= 3)
          versionParts(2).takeWhile(_.isDigit)
        else "0"

      (major, minor, patch) match {
        case ("2", "10", _) => Scala210
        case ("2", "11", _) => Scala211
        case ("2", "12", _) => Scala212
        case ("2", "13", _) => Scala213
        case ("3", "0", _)  => Scala30
        case ("3", "1", _)  => Scala31
        case ("3", "2", _)  => Scala32
        case ("3", "3", _)  => Scala33
        case ("3", "4", _)  => Scala3 // Fall back to Scala3 for newer versions
        case ("3", "5", _)  => Scala3 // Fall back to Scala3 for newer versions
        case ("3", _, _)    => Scala3 // Default for Scala 3.x
        case _              => Scala213 // Default fallback
      }
    } else
      Scala213 // Default fallback
  }

  /**
    * Retrieves the appropriate Scalameta dialect configuration based on the project's `.scalafmt.conf` file.
    *
    * This method reads the `runner.dialect` setting from the `.scalafmt.conf` file located in the project's base
    * directory and maps the value to a corresponding Scalameta dialect. If the exact dialect is not found, it attempts
    * fallback transformations to find a match.
    *
    * @param projectBaseDir
    *   The base directory of the project where the `.scalafmt.conf` file is located.
    * @return
    *   An `Option` containing the Scalameta `Dialect` if found, or `None` if no valid dialect is specified or the
    *   configuration file is missing/invalid.
    */
  private def getDialectFromScalafmt(projectBaseDir: File): Option[Dialect] = {
    val scalafmtFile: File = new File(projectBaseDir, ".scalafmt.conf")
    readScalafmtDialect(scalafmtFile).flatMap { dialectStr =>
      dialectMap.get(dialectStr).orElse {
        // If the exact string isn't in our map, try some transformations
        dialectMap.get(dialectStr.toLowerCase).orElse {
          dialectMap.get(dialectStr.replace(".", ""))
        }
      }
    }
  }

  /**
    * Reads the `runner.dialect` value from a provided `.scalafmt.conf` file.
    *
    * The method scans the file for a configuration line defining `runner.dialect`, which should follow the format
    * `runner.dialect = value` or `runner.dialect=value`.
    *
    * @param scalafmtFile
    *   the `.scalafmt.conf` file to be read
    * @return
    *   an optional string containing the value of `runner.dialect` if found, or `None` if the file does not exist, is
    *   not valid, or the value is not present
    */
  def readScalafmtDialect(scalafmtFile: File): Option[String] = {
    //  Reads the runner.dialect value from a .scalafmt.conf file
    if (!scalafmtFile.exists() || !scalafmtFile.isFile) {
      return None
    }

    Try {
      Using.resource(Source.fromFile(scalafmtFile)) { source =>
        val lines = source.getLines().toList

        // Look for runner.dialect = value or runner.dialect=value
        val dialectPattern =
          """^\s*runner\.dialect\s*=\s*["']?([^"'\s]+)["']?\s*(?:#.*)?$""".r

        lines.collectFirst { case dialectPattern(dialect) => dialect }
      }
    }.toOption.flatten
  }

  /**
    * Determines the appropriate dialect for a project (legacy method for backward compatibility)
    *
    * @param projectBaseDir
    *   The base directory of the project being analyzed
    * @param scalaVersion
    *   The Scala version from SBT configuration
    * @return
    *   The appropriate Scalameta Dialect
    */
  def getDialect(projectBaseDir: File, scalaVersion: String): Dialect = {
    // First, check if there's a .scalafmt.conf file
    val dialectFromScalafmt: Option[Dialect] = getDialectFromScalafmt(projectBaseDir)
    dialectFromScalafmt.getOrElse {
      // Fall back to using the Scala version from build.sbt
      dialectFromScalaVersion(scalaVersion)
    }
  }

}
