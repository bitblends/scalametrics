/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.analyzer.model

/**
  * Represents an owner entity in the analyzer model.
  *
  * The `Owner` trait provides a unified way to describe the owner of a code element being analyzed, such as a class,
  * method, or package. Each owner has a label and an optional name associated with it.
  *
  * Subclasses or implementations of this trait can represent specific kinds of ownership relationships in the source
  * code.
  *
  * @define label
  *   A string label that identifies the owner type.
  * @define named
  *   An optional name that provides additional identity or context for the owner.
  */
sealed trait Owner {

  /**
    * Retrieves the label associated with the owner.
    *
    * This label typically identifies the type or category of the owner within the analysis context.
    *
    * @return
    *   the label of the owner as a string.
    */
  def label: String;

  /**
    * Retrieves the optional name associated with the owner.
    *
    * This method returns an `Option[String]`, where `Some(name)` indicates the name of the owner, and `None` represents
    * the absence of a name. This optional name provides additional identity or context for the owner.
    *
    * @return
    *   an `Option[String]` representing the name of the owner, or `None` if not present.
    */
  def named: Option[String]
}

/**
  * Represents a package owner in the analyzer model.
  *
  * This case class extends the `Owner` trait to specifically model ownership at the package level. Packages are
  * identified by a name and have a predefined label to distinguish them from other owner types.
  *
  * @param n
  *   The name of the package being represented as the owner.
  */
final case class PkgOwner(n: String) extends Owner {

  /**
    * The label that categorizes this owner type as representing a package, specifically defined with the value "pkg".
    *
    * This constant overrides the `label` property from the parent `Owner` trait to provide a specific classification
    * for `PkgOwner` instances.
    */
  override val label: String = "pkg"

  /**
    * Represents the optional named value for this owner.
    *
    * This overrides the `named` property in the parent `Owner` trait, returning the name of the package as an
    * `Option[String]`.
    */
  override val named: Option[String] = Some(n)
}

/**
  * Represents an object owner within the analyzer model.
  *
  * This case class models ownership for entities represented as objects in the analyzed code. It extends the `Owner`
  * trait and provides a predefined label for objects, along with an optional name identifier for the specific object
  * being referred to.
  *
  * @param n
  *   The name of the object owner.
  */
final case class ObjOwner(n: String) extends Owner {

  /**
    * Overrides the `label` field to provide a predefined label representing object ownership.
    *
    * This value is specific to the `ObjOwner` case class and indicates that the owner represents an object in the
    * analyzed code.
    */
  override val label: String = "object"

  /**
    * Overrides the `named` field from the `Owner` trait to provide an optional name representation.
    *
    * This implementation derives the name from the object owner's identifier (`n`) and wraps it in a `Some`. If no name
    * is provided, the field could represent `None` in other contexts, indicating the absence of a name.
    */
  override val named: Option[String] = Some(n)
}

/**
  * Represents an owner entity defined as a "class".
  *
  * This case class extends the `Owner` trait and specifically models the ownership of a class entity within the source
  * code. It provides a fixed label "class" to denote the owner type and allows setting an optional name (`n`) to
  * identify the class being represented.
  *
  * @param n
  *   The name of the class being represented as an owner entity.
  * @define label
  *   A fixed string "class" that identifies this owner as a class entity.
  * @define named
  *   The optional name of the class being represented.
  */
final case class ClsOwner(n: String) extends Owner {

  /**
    * A fixed label identifying the type of owner as a "class".
    *
    * This value overrides the `label` field in the `Owner` trait to designate this owner as representing a class entity
    * within the source code.
    */
  override val label: String = "class"

  /**
    * The optional name of the class being represented.
    *
    * This value overrides the `named` field in the `Owner` trait, providing an `Option` that wraps the name of the
    * class entity. It ensures that the name is available and accessible as a specific instance of `Option[String]`.
    */
  override val named: Option[String] = Some(n)
}

/**
  * Represents an owner entity of type "trait."
  *
  * This case class extends the `Owner` trait and provides information about an owner of a type "trait" in the analysis
  * model.
  *
  * @param n
  *   The name of the trait owner, which provides additional identity or context.
  *
  * $label The label "trait" denotes the type of this owner entity.
  *
  * $named This value contains the optional name of the trait owner, which is derived from the provided parameter `n`
  * wrapped in `Some`.
  */
final case class TrtOwner(n: String) extends Owner {

  /**
    * A constant value that represents the type label for this owner entity.
    *
    * This overridden `label` provides a fixed designation, "trait," to denote the type of the owner entity within the
    * analysis model. It is part of the `Owner` trait implementation, distinguishing entities associated with trait
    * ownership.
    */
  override val label: String = "trait"

  /**
    * Provides the optional name for the owner entity.
    *
    * This overridden value represents the name of the owner entity, wrapped in `Some`, using the parameter `n` from the
    * defining class. It implements the `named` field in the `Owner` trait.
    */
  override val named: Option[String] = Some(n)
}

/**
  * Represents a specific implementation of the `Owner` trait for a `def` entity.
  *
  * This case class defines an owner for a Scala `def` declaration, associating it with a specific name and a predefined
  * label indicating its type.
  *
  * @param n
  *   The name associated with the `def` entity for which this owner is defined.
  * @define label
  *   A fixed label "def" representing the type of the owner.
  * @define named
  *   An optional name providing additional context for the owner. For `DefOwner`, this is always set to the name passed
  *   during construction.
  */
final case class DefOwner(n: String) extends Owner {

  /**
    * Represents the fixed label for the `DefOwner` entity, specifically indicating that the owner type corresponds to a
    * Scala `def` declaration.
    *
    * The label is a predefined, immutable string value that serves as a marker for the type of entity being
    * represented.
    */
  override val label: String = "def"

  /**
    * Provides an optional name for the `DefOwner` instance, derived from the associated `def` entity name.
    *
    * This is always set to the name passed during the construction of the `DefOwner` instance.
    */
  override val named: Option[String] = Some(n)
}

/**
  * Represents an owner entity for a block in the code analysis model.
  *
  * BlkOwner is a concrete implementation of the `Owner` trait, specifically representing ownership related to a block
  * of code. A block is uniquely identified by an integer ID.
  *
  * The label for this owner type is always "block". Additionally, the `named` property provides a more descriptive
  * representation of the block in the format "&lt;block#id&gt;", where `id` is the numeric identifier of the block.
  *
  * @param id
  *   A unique identifier for the block.
  */
final case class BlkOwner(id: Int) extends Owner {

  /**
    * Defines the label for the `BlkOwner` class, identifying this specific owner type as "block".
    *
    * The `label` property serves as a fixed marker for instances of `BlkOwner`, always set to the string "block". It
    * represents the ownership type associated with a block of code within the analysis model.
    */
  override val label: String = "block"

  /**
    * Provides a descriptive label for the block instance in the format "&lt;block#id&gt;" where `id` is a unique
    * identifier.
    *
    * The `named` property represents an optional string used as a human-readable identifier for the block owner. It is
    * constructed dynamically using the block's unique numeric ID.
    */
  override val named: Option[String] = Some(s"<block#$id>")
}

/**
  * Represents a lambda owner in the analyzer model.
  *
  * This case class extends the `Owner` trait and provides specific characteristics for lambdas. A lambda owner is
  * identified by an ID and has a predefined label and name format.
  *
  * @param id
  *   The identifier for the lambda owner, which helps to uniquely distinguish this lambda.
  * @define label
  *   The label is set to "lambda" for all instances of this class.
  * @define named
  *   The name is derived by appending the ID to a predefined format, resulting in a name such as "&lt;lambda#1&gt;".
  */
final case class LamOwner(id: Int) extends Owner {

  /**
    * Specifies the label for the `LamOwner` class.
    *
    * This value is overridden in the `LamOwner` class to always return "lambda", signifying that the owner represents a
    * lambda construct.
    */
  override val label: String = "lambda"

  /**
    * Overrides the `named` attribute to provide a unique name for the lambda owner.
    *
    * The name is constructed using a predefined format that includes the lambda's ID, such as "&lt;lambda#1&gt;". This
    * is useful for identifying specific lambda owners within the analyzer model.
    */
  override val named: Option[String] = Some(s"<lambda#$id>")
}

/**
  * A specific implementation of the `Owner` trait representing a "template" owner type.
  *
  * The `TplOwner` case class is used to model ownership for code elements represented by templates. It assigns a fixed
  * label "template" and does not include an additional name (`named` is `None`).
  *
  * @define label
  *   A string label that identifies the owner type ("template").
  * @define named
  *   An optional name providing additional identity or context for the owner (`None` for this case class).
  */
final case class TplOwner() extends Owner {

  /**
    * The label used to identify this owner type.
    *
    * This value is overridden to provide a fixed label specific to the "template" owner type.
    */
  override val label: String = "template"

  /**
    * An optional name providing additional identity or context for the owner.
    *
    * This value is overridden to always be `None` for this specific owner type, indicating the absence of additional
    * naming context in this implementation.
    */
  override val named: Option[String] = None
}

/**
  * Represents a member owner in the analyzer model.
  *
  * The `MemberOwner` case class describes an owner entity corresponding to code members such as functions, methods,
  * classes, etc. It extends the `Owner` trait, specifying a `kind` of the member (e.g., "function", "method", "class")
  * and its `name`. The `label` is a predefined identifier with the prefix "member:", and the `named` field optionally
  * holds the provided name of the member.
  *
  * @param kind
  *   The kind or type of the code member (e.g., "function", "method", "class").
  * @param name
  *   The name of the code member being analyzed.
  */
final case class MemberOwner(kind: String, name: String) extends Owner {

  /**
    * Provides a predefined label for the member owner within the analyzer model.
    *
    * This label is constructed by prefixing the kind of the member (such as "function", "method", or "class") with
    * "member:". It serves as a unique identifier associated with the specific type of member.
    */
  override val label: String = s"member:$kind"

  /**
    * Represents an optional named property for the member owner.
    *
    * This field holds an optional string value derived from the `name` of the member owner. It provides a means to
    * reference or identify the member owner by an explicitly provided name, if available.
    */
  override val named: Option[String] = Some(name)
}
