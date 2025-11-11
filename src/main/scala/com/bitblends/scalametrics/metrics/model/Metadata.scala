package com.bitblends.scalametrics.metrics.model

/**
  * A case class that represents the metadata associated with various kinds of symbols or declarations in a codebase. It
  * provides details such as the symbol's identity, structure, accessibility, and other properties that can assist in
  * analyzing the characteristics and behavior of the symbol.
  *
  * @param fileId
  *   The unique identifier of the file where the symbol is located.
  * @param name
  *   The name of the symbol or declaration.
  * @param signature
  *   The signature of the symbol, representing its type or structure.
  * @param accessModifier
  *   The access level of the symbol (e.g., public, private, protected).
  * @param linesOfCode
  *   The number of lines of code associated with the symbol.
  * @param isDeprecated
  *   Indicates whether the symbol is marked as deprecated.
  * @param isNested
  *   Indicates whether the symbol is nested within another symbol.
  * @param declarationType
  *   Describes the type of declaration (e.g., `val`, `var`, `def`, `type`, `class`, `object`, `trait`).
  * @param parentMember
  *   An optional parameter specifying the parent symbol, if the current symbol is nested.
  */
case class Metadata(
    fileId: String,
    name: String,
    signature: String,
    accessModifier: String,
    linesOfCode: Int,
    isDeprecated: Boolean,
    isNested: Boolean,
    declarationType: String,
    parentMember: Option[String] = None
) extends MetricBase
