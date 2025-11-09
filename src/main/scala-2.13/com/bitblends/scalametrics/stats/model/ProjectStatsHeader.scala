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
) {

  /**
    * Converts the properties of the case class implementing the `Product` trait into a `Map[String, String]`, where the
    * keys correspond to field names and the values are string representations of the field values. Handles `Option`
    * values by converting `Some` to the contained value and `None` to an empty string. Collections such as `Seq` are
    * joined into a comma-separated string.
    *
    * @return
    *   a map where keys are the names of the fields and values are the string representations of the corresponding
    *   field values.
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
}
