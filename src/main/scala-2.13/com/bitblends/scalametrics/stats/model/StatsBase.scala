/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.stats.model

/**
  * Defines a base trait for statistical models. This serves as a foundation or marker trait for components within the
  * statistics package and can be extended by specific statistical entities.
  */
trait StatsBase extends Product {

  /**
    * Converts the current instance of a class that extends `StatsBase` into a `Map[String, Any]` representation. Each
    * field of the product is represented as a key-value pair in the resulting map.
    *
    * @return
    *   A map representation of the current instance where keys are field names and values are their corresponding
    *   normalized values.
    */
  def toMap: Map[String, Any] = StatsBase.productToMap(this)
}

/**
  * Companion object for the `StatsBase` trait. This object provides utility methods for data transformation,
  * manipulation, and serialization, specifically designed to handle maps, product types, and other data types commonly
  * used in statistical models.
  */
object StatsBase {

  /**
    * Converts a nested map into a flattened representation, where nested keys are combined into a single key with dot
    * notation.
    *
    * @param map
    *   The input map to be flattened. Keys are Strings and values can be of any type, including nested maps.
    * @param prefix
    *   A string prefix applied to the keys of the flattened map. Defaults to an empty string.
    * @return
    *   A flattened map with dot-separated keys and corresponding values from the input map.
    */
  def flatten(map: Map[String, Any], prefix: String = ""): Map[String, Any] =
    map.flatMap {
      case (k, v: Map[_, _]) =>
        flatten(v.asInstanceOf[Map[String, Any]], if (prefix.isEmpty) k else s"$prefix.$k")
      case (k, v) =>
        Map((if (prefix.isEmpty) k else s"$prefix.$k") -> v)
    }

  /**
    * Converts the given input into its JSON string representation. The input can be of various types such as null,
    * Maps, Sequences, Strings, Booleans, Numbers, or any other object. It handles special character escaping and
    * ensures proper formatting for JSON compliance.
    *
    * @param v
    *   The input value to be converted to a JSON string. It can be of any type, including maps, lists, strings,
    *   numbers, booleans, or null.
    * @return
    *   A JSON-formatted string representation of the input.
    */
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
    * Escapes special characters in a string for safe inclusion in JSON formatting. This includes replacing control
    * characters and other special characters with their escaped representations (e.g., `"` becomes `\"`, `\n` becomes
    * `\\n`).
    *
    * @param s
    *   The input string to be escaped.
    * @return
    *   A new string with special characters replaced by their escaped equivalents.
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
    * Converts a given product (case classes, tuples, etc.) into a map where each field name is a key and its
    * corresponding value is the map value.
    *
    * @param p
    *   The input product whose elements need to be converted into a map. The product can be any case class, tuple, or
    *   other instance implementing the Product trait.
    * @return
    *   A map where the keys are the product's field names and the values are their respective normalized values.
    */
  private def productToMap(p: Product): Map[String, Any] =
    p.productElementNames.zip(p.productIterator.toSeq).map { case (n, v) => n -> norm(v) }.toMap

  /**
    * Normalizes the input value by recursively transforming it based on its type. Specific transformations are applied
    * to instances of `StatsBase`, `Option`, sequences, maps, and products, converting them into more generic
    * representations such as maps, lists, or strings.
    *
    * @param v
    *   The input value to be normalized. It can be of any type, including instances of `StatsBase`, `Option`,
    *   sequences, maps, or products.
    * @return
    *   The normalized representation of the input, which could be a map, list, string, or other suitable type.
    */
  private def norm(v: Any): Any = v match {
    case s: StatsBase => s.toMap
    case o: Option[_] => o.map(norm).orNull
    case seq: Seq[_]  => seq.map(norm)
    case m: Map[_, _] =>
      m.asInstanceOf[Map[Any, Any]].map { case (k, v2) => k.toString -> norm(v2) }
    case p: Product if !p.isInstanceOf[StatsBase] =>
      // If you want to auto-convert any Product: enable next line, else leave as toString
      // productToMap(p)
      p.toString
    case other => other
  }

}
