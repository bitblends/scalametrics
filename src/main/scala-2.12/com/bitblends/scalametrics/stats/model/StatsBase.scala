/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.stats.model

import scala.reflect.runtime.{universe => ru}

/**
  * Defines a base trait for statistical models. This serves as a foundation or marker trait for components within the
  * statistics package and can be extended by specific statistical entities.
  */
trait StatsBase extends Product {

  /**
    * Converts the current instance into a map representation where the keys are the field names of the instance, and
    * the values are the corresponding field values. The method supports nested conversions by recursively converting
    * inner `StatsBase` instances, Options, and other supported types into appropriate map or normalized
    * representations.
    *
    * @return
    *   A map where the keys represent field names and the values represent field values, recursively normalized and
    *   converted if applicable.
    */
  def toMap: Map[String, Any] = StatsBase.productToMap(this)

}

/**
  * Provides utility methods and functionality for operations related to statistical models. The object includes methods
  * for data transformation, serialization to JSON, and flattening nested data structures.
  */
object StatsBase {

  /**
    * Normalizes an input value to a standard representation. The method processes various types such as `StatsBase`
    * instances, `Option` values, sequences, maps, and other product types. It ensures that nested structures are
    * recursively normalized.
    *
    * @param v
    *   The input value to be normalized. It can be of any type, including `StatsBase`, `Option`, `Seq`, `Map`, or a
    *   `Product`.
    * @return
    *   A normalized representation of the input value. For `StatsBase`, this will be a map. For `Option`, it will
    *   recursively normalize the content if present. For sequences and maps, it recursively processes each element. For
    *   non-supported types, it returns the input as is.
    */
  private def norm(v: Any): Any = v match {
    case s: StatsBase => s.toMap
    case o: Option[_] => o.fold(null: Any)(norm)
    case seq: Seq[_]  => seq.map(norm)
    case m: Map[_, _] =>
      m.asInstanceOf[Map[Any, Any]].map { case (k, v2) => k.toString -> norm(v2) }
    case p: Product if !p.isInstanceOf[StatsBase] =>
      p.toString
    case other => other
  }

  /**
    * Extracts the parameter names of the primary constructor of a given product type.
    *
    * @param p
    *   The product instance whose primary constructor parameter names are to be retrieved.
    * @return
    *   A list of parameter names as strings, corresponding to the primary constructor of the given product type.
    */
  private def paramNames(p: Product): List[String] = {
    val mirror = ru.runtimeMirror(p.getClass.getClassLoader)
    val sym = mirror.classSymbol(p.getClass)
    val ctor = sym.primaryConstructor.asMethod
    ctor.paramLists.flatten.map(_.name.decodedName.toString)
  }

  /**
    * Converts a given `Product` instance into a map representation where the keys are the field names of the instance's
    * primary constructor, and the values are the corresponding field values. Field values are normalized using the
    * `norm` method.
    *
    * @param p
    *   The product instance to be converted into a map. This is typically an instance of a case class or other product
    *   type whose fields are to be mapped.
    * @return
    *   A map where the keys represent the names of fields and the values are the normalized field values.
    */
  private def productToMap(p: Product): Map[String, Any] = {
    val names = paramNames(p)
    val values = p.productIterator.toList
    names.zip(values).map { case (n, v) => n -> norm(v) }.toMap
  }

  def toJson(v: Any): String = v match {
    case null         => "null"
    case m: Map[_, _] =>
      m.asInstanceOf[Map[String, Any]]
        .map { case (k, v2) => "\"" + escape(k) + "\":" + toJson(v2) }
        .mkString("{", ",", "}")
    case seq: Seq[_] => seq.map(toJson).mkString("[", ",", "]")
    case s: String   => "\"" + escape(s) + "\""
    case b: Boolean  => b.toString
    case n: Int      => n.toString
    case n: Long     => n.toString
    case n: Double   =>
      if (n.isNaN || n.isInfinity) "\"" + n.toString + "\"" else n.toString
    case other => "\"" + escape(other.toString) + "\""
  }

  /**
    * Escapes special characters in a given string to make it suitable for inclusion in JSON or other contexts requiring
    * escaped strings. It handles control characters, backslashes, quotes, and other characters requiring specific
    * escaping rules.
    *
    * @param s
    *   The input string to be escaped.
    * @return
    *   A new string with all required characters escaped.
    */
  private def escape(s: String): String =
    s.flatMap {
      case '"'              => "\\\""
      case '\\'             => "\\\\"
      case '\b'             => "\\b"
      case '\f'             => "\\f"
      case '\n'             => "\\n"
      case '\r'             => "\\r"
      case '\t'             => "\\t"
      case c if c.isControl => "\\u%04x".format(c.toInt)
      case c                => c.toString
    }

  /**
    * Flattens a nested map structure into a single-level map, using dot notation in keys for nested paths.
    *
    * @param map
    *   The input map to be flattened. Can include nested maps as values.
    * @param prefix
    *   A string that serves as the prefix for the current level's keys, used for recursion.
    * @return
    *   A flattened map with single-level keys, where nested keys are represented using dot notation.
    */
  def flatten(map: Map[String, Any], prefix: String = ""): Map[String, Any] =
    map.flatMap {
      case (k, v: Map[_, _]) =>
        flatten(v.asInstanceOf[Map[String, Any]], if (prefix.isEmpty) k else s"$prefix.$k")
      case (k, v) =>
        Map((if (prefix.isEmpty) k else s"$prefix.$k") -> v)
    }

}
