package com.github

import scalaz.Disjunction

package object dnvriend {
  type DTry[A] = Disjunction[Throwable, A]

  implicit def toDisjunctionOps[A](f: => A): DisjunctionOps[A] = new DisjunctionOps[A](f)

  class DisjunctionOps[A](f: => A) {
    def safe: DTry[A] = Disjunction.fromTryCatchNonFatal(f)
  }
}
