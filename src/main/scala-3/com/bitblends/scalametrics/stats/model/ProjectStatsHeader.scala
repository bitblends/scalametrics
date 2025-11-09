/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.stats.model

/**
  * Represents metadata and configuration details for a project.
  *
  * @param name
  *   The name of the project.
  * @param version
  *   The version of the project.
  * @param scalaVersion
  *   The Scala version used in the project.
  * @param description
  *   An optional description or summary of the project.
  * @param crossScalaVersions
  *   A sequence of Scala versions for cross-compilation.
  * @param organization
  *   An optional identifier for the organization overseeing the project.
  * @param organizationName
  *   An optional name of the organization overseeing the project.
  * @param organizationHomepage
  *   An optional homepage URL of the organization.
  * @param homepage
  *   An optional homepage URL for the project.
  * @param licenses
  *   An optional license information for the project, typically in the form of SPDX identifier(s).
  * @param startYear
  *   An optional start year indicating when the project was initiated.
  * @param isSnapshot
  *   An optional string indicating if the project version is a snapshot.
  * @param apiURL
  *   An optional URL to the project's API documentation.
  * @param scmInfo
  *   An optional string containing source control management information.
  * @param developers
  *   An optional string detailing information about the developers involved in the project.
  * @param versionScheme
  *   An optional versioning scheme used by the project (e.g., "semantic").
  * @param projectInfoNameFormal
  *   An optional, formal name for the project, if applicable.
  */
case class ProjectStatsHeader(
    name: String,
    version: String,
    scalaVersion: String,
    description: Option[String] = None,
    crossScalaVersions: Seq[String] = Seq.empty,
    organization: Option[String] = None,
    organizationName: Option[String] = None,
    organizationHomepage: Option[String] = None,
    homepage: Option[String] = None,
    licenses: Option[String] = None,
    startYear: Option[String] = None,
    isSnapshot: Option[String] = None,
    apiURL: Option[String] = None,
    scmInfo: Option[String] = None,
    developers: Option[String] = None,
    versionScheme: Option[String] = None,
    projectInfoNameFormal: Option[String] = None
):

  /**
    * Converts the case class fields and their values into a map where field names are keys and their string
    * representations are the corresponding values. For optional or collection fields, the method handles their
    * conversion accordingly:
    *   - `Option` values are converted to their inner value as a string, or an empty string if `None`.
    *   - `Seq` values are joined into a comma-separated string.
    *   - Other types are converted using their `toString` representation.
    *
    * @return
    *   A Map where keys are the field names as strings and values are their string representations.
    */
  def toMap: Map[String, String] = {
    this.productElementNames
      .zip(this.productIterator)
      .map { case (name, value) =>
        name -> (value match {
          case Some(v)     => v.toString
          case None        => ""
          case seq: Seq[_] => seq.mkString(", ")
          case v           => v.toString
        })
      }
      .toMap
  }
