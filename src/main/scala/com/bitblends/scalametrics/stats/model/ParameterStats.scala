/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.stats.model

/**
  * Represents a collection of metrics related to parameters and parameter lists in a codebase.
  *
  * This case class provides an analysis of parameters in terms of quantity and special characteristics, aiding in
  * understanding the structure and complexity of function or method definitions.
  *
  * @constructor
  *   Creates an instance of ParameterMetrics.
  *
  * @param totalParams
  *   The total number of parameters across all parameter lists.
  * @param paramLists
  *   The total number of parameter lists in a method or function.
  * @param implicitParamLists
  *   The count of implicit parameter lists.
  * @param usingParamLists
  *   The count of using parameter lists.
  * @param implicitParams
  *   The total number of individual implicit parameters.
  * @param usingParams
  *   The total number of parameters utilizing the `using` keyword.
  * @param defaultedParams
  *   The count of parameters that have default values defined.
  * @param byNameParams
  *   The total number of by-name parameters.
  * @param varargParams
  *   The total number of variadic parameters.
  */
case class ParameterStats(
    totalParams: Int = 0,
    paramLists: Int = 0,
    implicitParamLists: Int = 0,
    usingParamLists: Int = 0,
    implicitParams: Int = 0,
    usingParams: Int = 0,
    defaultedParams: Int = 0,
    byNameParams: Int = 0,
    varargParams: Int = 0
) extends Serializer {

  /**
    * Combines this `ParameterStats` instance with another `ParameterStats` instance, aggregating their respective
    * statistical values.
    *
    * @param that
    *   The `ParameterStats` instance to be combined with this instance.
    * @return
    *   A new `ParameterStats` instance with combined statistical values.
    */
  def +(that: ParameterStats): ParameterStats =
    ParameterStats(
      totalParams = this.totalParams + that.totalParams,
      paramLists = this.paramLists + that.paramLists,
      implicitParamLists = this.implicitParamLists + that.implicitParamLists,
      usingParamLists = this.usingParamLists + that.usingParamLists,
      implicitParams = this.implicitParams + that.implicitParams,
      usingParams = this.usingParams + that.usingParams,
      defaultedParams = this.defaultedParams + that.defaultedParams,
      byNameParams = this.byNameParams + that.byNameParams,
      varargParams = this.varargParams + that.varargParams
    )

  /**
    * Provides a formatted string representation of the `ParameterStats` instance, detailing each metric.
    *
    * @return
    *   A formatted string summarizing the parameter statistics.
    */
  override def formattedString: String = {
    s"""ParameterStats:
       |----------------------------------------------------------
       |  Total Params: $totalParams
       |  Param Lists: $paramLists
       |  Implicit Param Lists: $implicitParamLists
       |  Using Param Lists: $usingParamLists
       |  Implicit Params: $implicitParams
       |  Using Params: $usingParams
       |  Defaulted Params: $defaultedParams
       |  By-Name Params: $byNameParams
       |  Vararg Params: $varargParams
     """.stripMargin
  }

}
