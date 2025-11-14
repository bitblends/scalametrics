/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.stats.model
import scala.collection.immutable.ListMap

/**
  * Provides serialization utilities for classes that extend this trait. Enables conversion of an instance into a map or
  * its JSON string representation.
  *
  * This trait must be mixed in with classes that extend `Product`, leveraging the product's fields for serialization.
  */
trait Serializer extends Product {

  /**
    * Converts the current instance of a class that extends `StatsBase` into a `Map[String, Any]` representation. Each
    * field of the product is represented as a key-value pair in the resulting map.
    *
    * @return
    *   A map representation of the current instance where keys are field names and values are their corresponding
    *   normalized values.
    */
  def toMap: Map[String, Any] = Serializer.productToMap(this)

  /**
    * Converts the current instance of a class that extends `StatsBase` into its JSON string representation. This method
    * utilizes the `toMap` method to first convert the instance into a map and then serializes that map into a JSON
    * string.
    *
    * @return
    *   A JSON-formatted string representation of the current instance.
    */
  def toJson: String = Serializer.toJson(toMap)

  /**
    * Ordered field names (top level only, not flattened).
    */
  def fieldNames: Vector[String] =
    Serializer.paramNamesOrdered(this)

  /**
    * Ordered field->value pairs (top level only, not flattened).
    */
  def toListMap: ListMap[String, Any] =
    Serializer.productToMapOrdered(this)

  /**
    * Flattened, ordered (depth-first) dotted keys, e.g. metadata.name
    */
  def toFlatVector(prefix: String = ""): Vector[(String, Any)] =
    Serializer.flattenOrdered(toListMap, prefix)

  /**
    * Convenience: just the headers for CSV (flattened).
    */
  def csvHeaders(prefix: String = ""): Vector[String] =
    toFlatVector(prefix).map(_._1)

  /**
    * Convenience: the values for CSV (stringified, flattened).
    */
  def csvValues(prefix: String = ""): Vector[String] =
    toFlatVector(prefix).map(_._2).map(Serializer.valueToCsv)

  /**
    * Provides a formatted string representation of the current instance. This method can be overridden by subclasses to
    * provide custom formatting.
    *
    * @return
    *   A formatted string representation of the current instance.
    */
  def formattedString: String

}

/**
  * Companion object for the `StatsBase` trait. This object provides utility methods for data transformation,
  * manipulation, and serialization, specifically designed to handle maps, product types, and other data types commonly
  * used in statistical models.
  */
object Serializer {

  def paramNamesOrdered(p: Product): Vector[String] = {
    val names = p.productElementNames.toVector
    if (names.nonEmpty) names
    else (0 until p.productArity).map(i => s"_${i + 1}").toVector
  }

  /**
    * Converts a given `Product` instance into an ordered map representation where the keys are the field names of the
    * instance's primary constructor, and the values are the corresponding field values. Field values are normalized
    * using the `norm` method.
    *
    * @param p
    *   The product instance to be converted into a map. This is typically an instance of a case class or other product
    *   type whose fields are to be mapped.
    * @return
    *   A map where the keys represent the names of fields and the values are the normalized field values, preserving
    *   the order of fields as defined in the primary constructor.
    */
  def productToMapOrdered(p: Product): ListMap[String, Any] = {
    val names = paramNamesOrdered(p)
    val values = p.productIterator.toVector
    ListMap(names.zip(values).map { case (n, v) => n -> norm(v) }: _*)
  }

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
    case s: Serializer => s.toMap
    case o: Option[_]  => o.map(norm).orNull
    case seq: Seq[_]   => seq.map(norm)
    case m: Map[_, _]  =>
      m.asInstanceOf[Map[Any, Any]].map { case (k, v2) => k.toString -> norm(v2) }
    case p: Product if !p.isInstanceOf[Serializer] =>
      // If you want to auto-convert any Product: enable next line, else leave as toString
      productToMap(p)
    // p.toString
    case other => other
  }

  def flattenOrdered(map: ListMap[String, Any], prefix: String = ""): Vector[(String, Any)] = {
    val buf = Vector.newBuilder[(String, Any)]
    map.foreach {
      case (k, v: ListMap[_, _]) =>
        buf ++= flattenOrdered(v.asInstanceOf[ListMap[String, Any]], if (prefix.isEmpty) k else s"$prefix.$k")
      case (k, v: Map[_, _]) =>
        // convert non-ListMap to ListMap to keep a deterministic order
        val lm = ListMap(v.asInstanceOf[Map[String, Any]].toSeq.sortBy(_._1): _*)
        buf ++= flattenOrdered(lm, if (prefix.isEmpty) k else s"$prefix.$k")
      case (k, v) =>
        val key = if (prefix.isEmpty) k else s"$prefix.$k"
        buf += key -> v
    }
    buf.result()
  }

  def valueToCsv(v: Any): String = v match {
    case null       => ""
    case None       => ""
    case Some(x)    => valueToCsv(x)
    case s: String  => escCsv(s)
    case b: Boolean => if (b) "true" else "false"
    case n: Int     => n.toString
    case n: Long    => n.toString
    case n: Double  =>
      if (n.isNaN || n.isInfinity) "" else java.lang.String.format(java.util.Locale.ROOT, "%.6f", n: java.lang.Double)
    case seq: Seq[_]  => escCsv(seq.map(valueToCsv).mkString("[", " ", "]"))
    case m: Map[_, _] =>
      // fallback to JSON for complex nested leftovers
      escCsv(toJson(m))
    case other => escCsv(other.toString)
  }

  // RFC4180-ish escaping for CSV cells
  private def escCsv(s: String): String = {
    val needs = s.indexOf(',') >= 0 || s.indexOf('\n') >= 0 || s.indexOf('\r') >= 0 || s.indexOf('"') >= 0
    val body = s.replace("\"", "\"\"")
    if (needs) "\"" + body + "\"" else body
  }
}
