/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.stats.model

/**
  * Represents detailed statistical information about a method within a source file.
  *
  * This case class provides a comprehensive analysis of a method, including its metadata, structural details, metrics,
  * and characteristics. It extends `MemberBase` to inherit common member properties while adding method-specific
  * metrics such as parameter details and inline modifiers.
  *
  * @param fileId
  *   The unique identifier of the source file containing the method.
  * @param name
  *   The name of the method.
  * @param signature
  *   The complete signature of the method, including parameter and return type details.
  * @param accessModifier
  *   The access level of the method, such as `public`, `private`, or `protected`.
  * @param linesOfCode
  *   The number of lines of code in the method.
  * @param isNested
  *   Indicates whether the method is nested within another method or class.
  * @param hasScaladoc
  *   Indicates whether the method is documented with Scaladoc.
  * @param isDeprecated
  *   Indicates whether the method is marked as deprecated.
  * @param parentMember
  *   The name of the parent member (if any) within which this method is nested.
  * @param cComplexity
  *   The cyclomatic complexity of the method.
  * @param nestingDepth
  *   The nesting depth of the method code.
  * @param totalParams
  *   The total number of parameters defined in the method signature.
  * @param paramLists
  *   The number of parameter lists in the method signature.
  * @param implicitParamLists
  *   The count of implicit parameter lists in the method signature.
  * @param usingParamLists
  *   The count of `using` parameter lists in the method signature.
  * @param implicitParams
  *   The total number of implicit parameters in the method signature.
  * @param usingParams
  *   The total number of `using` parameters in the method signature.
  * @param defaultedParams
  *   The count of parameters with default values in the method signature.
  * @param byNameParams
  *   The count of by-name parameters in the method signature.
  * @param varargParams
  *   The count of variadic (varargs) parameters in the method signature.
  * @param hasInlineModifier
  *   Indicates if the method is marked with the `inline` modifier.
  * @param inlineParamCount
  *   The number of parameters in the method's inline sections.
  * @param isImplicitConversion
  *   Indicates whether the method is an implicit conversion.
  * @param isAbstract
  *   Indicates whether the method is abstract.
  * @param hasExplicitReturnType
  *   Indicates if the method has an explicitly defined return type.
  * @param inferredReturnType
  *   The inferred return type of the method, if no explicit return type is provided.
  * @param pmMatches
  *   The number of pattern matches within the method.
  * @param pmCases
  *   The total number of pattern match cases within the method.
  * @param pmGuards
  *   The count of guards (`if` conditions) within pattern matches.
  * @param pmWildcards
  *   The number of wildcard patterns used in the method.
  * @param pmMaxNesting
  *   The maximum nesting level within pattern matches in the method.
  * @param pmNestedMatches
  *   The count of nested pattern matches within the method.
  * @param pmAvgCasesPerMatch
  *   The average number of cases per pattern match within the method.
  * @param bdBranches
  *   The count of branch instructions (such as `if`, `else`, and loops) in the method code.
  * @param bdIfCount
  *   The number of `if` conditions in the method code.
  * @param bdCaseCount
  *   The number of `case` statements in the method code.
  * @param bdLoopCount
  *   The count of loop constructs (such as `for`, `while`) in the method code.
  * @param bdCatchCaseCount
  *   The number of `catch` clauses in the method.
  * @param bdBoolOpsCount
  *   The number of boolean operations (such as `&&`, `||`) in the method code.
  * @param bdDensityPer100
  *   The branch density metric calculated per 100 lines of code.
  * @param bdBoolOpsPer100
  *   The count of boolean operations per 100 lines of code.
  */
case class MethodStats(
    fileId: String,
    name: String,
    signature: String,
    accessModifier: String,
    linesOfCode: Int,
    isNested: Boolean,
    hasScaladoc: Boolean,
    isDeprecated: Boolean,
    parentMember: Option[String] = None,
    // Cyclomatic complexity
    cComplexity: Int,
    // Nesting depth
    nestingDepth: Int,
    // Parameter metrics
    totalParams: Int,
    paramLists: Int,
    implicitParamLists: Int,
    usingParamLists: Int,
    implicitParams: Int,
    usingParams: Int,
    defaultedParams: Int,
    byNameParams: Int,
    varargParams: Int,
    // inline
    hasInlineModifier: Boolean,
    inlineParamCount: Int,
    isImplicitConversion: Boolean,
    // explicitness
    isAbstract: Boolean,
    hasExplicitReturnType: Boolean,
    inferredReturnType: Option[String] = None,
    // Pattern matching metrics
    pmMatches: Int,
    pmCases: Int,
    pmGuards: Int,
    pmWildcards: Int,
    pmMaxNesting: Int,
    pmNestedMatches: Int,
    pmAvgCasesPerMatch: Double,
    // Branch density metrics
    bdBranches: Int,
    bdIfCount: Int,
    bdCaseCount: Int,
    bdLoopCount: Int,
    bdCatchCaseCount: Int,
    bdBoolOpsCount: Int,
    bdDensityPer100: Double,
    bdBoolOpsPer100: Double
) extends MemberBase {

  /**
    * Converts the fields of the implementing case class into a Map representation, where the keys are the field names
    * and the values are their corresponding string representations.
    *
    *   - If a field value is an Option, it will use the contained value's string representation if present, otherwise
    *     an empty string.
    *   - If a field value is a sequence, it will be converted into a comma-separated string.
    *   - For other field types, their string representations will be used.
    *
    * @return
    *   A Map where keys are string representations of the field names and values are string representations of the
    *   field values.
    */
  def toMap: Map[String, String] = {
    this.productElementNames
      .zip(this.productIterator)
      .map { case (name, value) =>
        name -> (value match {
          case Some(v)     => v.toString
          case None        => ""
          case seq: Seq[_] => seq.mkString(",")
          case v           => v.toString
        })
      }
      .toMap
  }

}
