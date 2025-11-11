package com.bitblends.scalametrics.metrics.model

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
case class ParameterMetrics(
    totalParams: Int = 0,
    paramLists: Int = 0,
    implicitParamLists: Int = 0,
    usingParamLists: Int = 0,
    implicitParams: Int = 0,
    usingParams: Int = 0,
    defaultedParams: Int = 0,
    byNameParams: Int = 0,
    varargParams: Int = 0
) extends MetricBase
