/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.metrics

import com.bitblends.scalametrics.analyzer.model.ImplicitInlineGivenMetrics

import scala.annotation.tailrec
import scala.collection.immutable
import scala.meta._

/**
  * Analyzes inline usage and implicit/given usage in Scala source code.
  *
  * Detects:
  *   - Inline usage (Scala 3's inline modifier and Scala 2.13's @inline annotation)
  *   - Implicit conversions and given instances (Scala 2 and Scala 3)
  *   - Using/implicit parameter lists
  *
  * Scala 2:
  *   - inline: @inline annotation on methods
  *   - implicit: implicit defs, vals, vars, and parameter lists
  *   - implicit conversions: implicit def with single param and non-Unit return
  *
  * Scala 3:
  *   - inline: inline modifier on defs, vals, vars, and parameters
  *   - given: given instances and given conversions (Conversion[A,B])
  *   - using: using parameter lists
  */
object InlineAndImplicits {

  /**
    * Computes metrics related to the use of the `implicit` modifier in a variable definition.
    *
    * @param v
    *   The variable definition (`Defn.Var`) being analyzed.
    * @return
    *   An `ImplicitInlineGivenMetrics` instance containing information about whether the variable definition includes
    *   the `implicit` modifier.
    */
  def forVar(v: Defn.Var): ImplicitInlineGivenMetrics = {
    val hasImplicitMod: Boolean = v.mods.exists {
      case Mod.Implicit() => true
      case _              => false
    }
    ImplicitInlineGivenMetrics(hasImplicitMod = hasImplicitMod)
  }

  /**
    * Computes metrics related to the use of the `implicit` modifier in a value definition.
    *
    * @param mods
    *   A sequence of modifiers (`Seq[Mod]`) associated with the value definition being analyzed.
    * @return
    *   An `ImplicitInlineGivenMetrics` instance containing information about whether the value definition includes the
    *   `implicit` modifier.
    */
  def forVal(mods: Seq[Mod]): ImplicitInlineGivenMetrics = {
    val hasImplicitMod: Boolean = mods.exists {
      case Mod.Implicit() => true
      case _              => false
    }
    ImplicitInlineGivenMetrics(hasImplicitMod = hasImplicitMod)
  }

  /**
    * Analyzes a Scala method declaration to compute metrics related to implicit conversions, inline methods, and the
    * usage of modifiers like `implicit` and `inline`.
    *
    * @param defn
    *   The function definition being analyzed.
    * @return
    *   An `ImplicitInlineGivenMetrics` instance containing the following information:
    *   - Whether the function looks like an implicit conversion.
    *   - Whether the function has an `implicit` modifier.
    *   - Whether the function is marked as an inline method.
    */
  def forDef(defn: Defn.Def): ImplicitInlineGivenMetrics = {

    // Check for inline modifier (Scala 3)
    val hasInlineMod: Boolean = defn.mods.exists {
      case Mod.Inline() => true
      case _            => false
    }

    // Scala 2 @inline annotation (optional)
    val hasInlineAnnotation: Boolean = defn.mods.exists {
      case Mod.Annot(Init(Type.Name("inline"), _, _)) => true
      case _                                          => false
    }

    // METRIC
    val isInlineMethod = hasInlineMod || hasInlineAnnotation

    val inlineParamCount: Int =
      defn.paramClauses.foldLeft(0) { (acc, pc) =>
        acc + pc.count(p =>
          p.mods.exists {
            case Mod.Inline() => true
            case _            => false
          }
        )
      }

    // METRIC Check for implicit modifier (Scala 2)
    val hasImplicitMod: Boolean = defn.mods.exists {
      case Mod.Implicit() => true
      case _              => false
    }

    // METRIC
    val hasImplicitConversion = looksLikeImplicitConversion(defn)

    ImplicitInlineGivenMetrics(
      hasImplicitConversion,
      hasImplicitMod,
      isInlineMethod
    )
  }

  /**
    * Heuristic (for Scala 2 implicit conversion) to determine if a function definition appears to represent an implicit
    * conversion.
    *
    * <code>implicit def f(a: A): B</code>
    *
    * The following conditions must be satisfied for a method to "look like" an implicit conversion:
    *   - The method has an `implicit` modifier.
    *   - The first parameter clause contains exactly one parameter, and that parameter is not implicit.
    *   - The method's declared return type is not `Unit`. If the return type is absent (inferred), it is assumed
    *     non-`Unit`.
    *
    * @param d
    *   The function definition (`Defn.Def`) to be analyzed.
    * @return
    *   True if the function definition satisfies the heuristic for an implicit conversion, false otherwise.
    */
  private def looksLikeImplicitConversion(d: Defn.Def): Boolean = {
    val hasImplicitMod = d.mods.exists {
      case Mod.Implicit() => true
      case _              => false
    }

    if (!hasImplicitMod) return false

    val pss = d.paramClauses
    if (pss.isEmpty) return false
    val first = pss.head

    // must have exactly one (non-implicit) parameter in the first clause
    val singleParam = first.size == 1 && !first.head.mods.exists {
      case Mod.Implicit() => true
      case _              => false
    }

    val nonUnitRet = d.decltpe.forall(t => !isUnitType(t)) // if absent (inferred), assume non-Unit
    singleParam && nonUnitRet
  }

  /**
    * Analyzes a function declaration to compute metrics related to implicit conversions, inline methods, and the usage
    * of modifiers like `implicit` and `inline`.
    *
    * @param defn
    *   The function declaration (`Decl.Def`) being analyzed.
    * @return
    *   An `ImplicitInlineGivenMetrics` instance containing the following information:
    *   - Whether the function has an `implicit` modifier.
    *   - Whether the function is marked as an inline method.
    */
  def forDecl(defn: Decl.Def): ImplicitInlineGivenMetrics = {

    // Check for inline modifier (Scala 3)
    val hasInlineMod: Boolean = defn.mods.exists {
      case Mod.Inline() => true
      case _            => false
    }

    val hasInlineAnnotation: Boolean = defn.mods.exists {
      case Mod.Annot(Init(Type.Name("inline"), _, _)) => true
      case _                                          => false
    }

    val isInlineMethod = hasInlineMod || hasInlineAnnotation

    // Check for implicit modifier (Scala 2)
    val hasImplicitMod: Boolean = defn.mods.exists {
      case Mod.Implicit() => true
      case _              => false
    }

    val hasImplicitConversion = looksLikeImplicitConversion(defn)

    ImplicitInlineGivenMetrics(
      hasImplicitMod = hasImplicitMod,
      hasImplicitConversion = hasImplicitConversion,
      isInlineMethod = isInlineMethod
    )
  }

  /**
    * Heuristic to determine if a function declaration appears to represent an implicit conversion.
    *
    * The following conditions must be satisfied for a method to "look like" an implicit conversion:
    *   - The method has an `implicit` modifier.
    *   - The first parameter clause contains exactly one parameter, and that parameter is not implicit.
    *   - The method's declared return type is not `Unit`. If the return type is absent (inferred), it is assumed
    *     non-`Unit`.
    *
    * @param d
    *   The function declaration to be analyzed.
    * @return
    *   True if the function declaration satisfies the heuristic for an implicit conversion, false otherwise.
    */
  private def looksLikeImplicitConversion(d: Decl.Def): Boolean = {
    val hasImplicitMod = d.mods.exists {
      case Mod.Implicit() => true
      case _              => false
    }

    if (!hasImplicitMod) return false

    val pss = d.paramClauses
    if (pss.isEmpty) return false
    val first = pss.head

    // must have exactly one (non-implicit) parameter in the first clause
    val singleParam = first.size == 1 && !first.head.mods.exists {
      case Mod.Implicit() => true
      case _              => false
    }

    val nonUnitRet = !isUnitType(d.decltpe) // if absent (inferred), assume non-Unit
    singleParam && nonUnitRet
  }

  /**
    * Determines whether the given type represents a `Unit` type.
    *
    * @param t
    *   The type to be checked.
    * @return
    *   True if the type is `Unit` or selects `Unit` from a package; false otherwise.
    */
  private def isUnitType(t: Type): Boolean = t match {
    case Type.Name("Unit") | Type.Select(_, Type.Name("Unit")) => true
    case _                                                     => false
  }

  /**
    * Extracts the parameter groups of a function definition.
    *
    * @param d
    *   The function definition (`Defn.Def`) from which the parameter groups will be extracted.
    * @return
    *   A list of parameter groups, where each group is represented as a list of `Term.Param`. If the input does not
    *   match the expected structure or no parameters are defined, an empty list is returned.
    */
  private def paramsOf(d: Defn.Def): List[List[Term.Param]] = d match {
    case Defn.Def.Initial(_, _, _, paramss, _, _) =>
      paramss
    case Defn.Def(origin, mods, name, paramClauseGroups, decltpe, body) =>
      paramClauseGroups
    case _ => Nil
  }

  /**
    * Extracts the modifiers of a function definition (`Defn.Def`).
    *
    * @param d
    *   The function definition from which the modifiers will be extracted.
    * @return
    *   A sequence of modifiers (`Mod`) associated with the given function definition. If the input does not match the
    *   expected structure, an empty sequence is returned.
    */
  private def modsOf(d: Defn.Def): immutable.Seq[Mod] = d match {
    case Defn.Def(mods, _, _, _, _, _) =>
      mods
    case _ => Nil
  }

  /**
    * Extracts the last name of a given type: given Conversion[_, _] or def given conversion via decltpe (scala 3)
    *
    * @param t
    *   The type from which the last name will be extracted.
    * @return
    *   A string representing the last name of the given type.
    */
  @tailrec
  private def lastTypeName(t: Type): String = t match {
    case Type.Name(n)                  => n
    case Type.Select(_, Type.Name(n))  => n
    case Type.Project(_, Type.Name(n)) => n
    case Type.Apply(tpe, _)            => lastTypeName(tpe)
    case Type.Annotate(tpe, _)         => lastTypeName(tpe)
    case other                         => other.syntax
  }

  /**
    * Determines whether the given type is a "Conversion" type. (scala 3)
    *
    * @param t
    *   The type to be checked.
    * @return
    *   True if the type represents a "Conversion" or "scala.Conversion", false otherwise.
    */
  private def isConversionType(t: Type): Boolean =
    lastTypeName(t) == "Conversion" || lastTypeName(t) == "scala.Conversion"

}
