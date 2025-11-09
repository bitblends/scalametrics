/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.metrics

import scala.meta.{Traverser, Tree}

/**
  * A compatibility trait that extends the `Traverser` functionality to provide a mechanism for traversing child nodes
  * in a tree structure.
  *
  * This trait ensures compatibility and provides a reusable implementation for traversing the children of a `Tree`. It
  * overrides the `traverseChildren` method, which utilizes `super.apply` to delegate to the `Traverser` functionality
  * in Scala 2.13.
  */
trait TraverserCompat extends Traverser {

  /**
    * Traverses the children of the given tree node by delegating to the functionality provided by the `Traverser`.
    *
    * In Scala 2.13, we inherit from Traverser, so traverseChildren calls super.apply
    *
    * @param tree
    *   the tree node whose children are to be traversed
    * @return
    *   Unit, as this method performs side effects during traversal
    */
  protected def traverseChildren(tree: Tree): Unit =
    super.apply(tree)
}
