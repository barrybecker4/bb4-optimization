// Copyright by Barry G. Becker, 2013-2014. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.optimization.optimizee.optimizees.problems

import com.barrybecker4.optimization.parameter.NumericParameterArray
import com.barrybecker4.optimization.parameter.types.{DoubleParameter, Parameter}

import scala.util.Random

/**
  * Constants related to the analytics functions
  * @author Barry Becker
  */
object ParabolaFunctionConsts {

  val P1 = 1.0
  val P2 = 2.0

  private val EXACT_SOLUTION_PARAMS: Array[Parameter] = Array(
    new DoubleParameter(P1, 0.0, 3.0, "p1"),
    new DoubleParameter(P2, 0.0, 3.0, "p2")
  )

  val EXACT_SOLUTION = new NumericParameterArray(EXACT_SOLUTION_PARAMS, new Random(1))

  // define the initialGuess in some bounded region of the 2-dimensional search space.
  private val VALUES = Array(6.81, 7.93) // initialGuess

  private val MIN_VALS = Array(-10.0, -10.0)
  private val MAX_VALS = Array(10.0, 10.0)
  private val PARAM_NAMES = Array("p1", "p2")

  val INITIAL_GUESS = new NumericParameterArray(VALUES, MIN_VALS, MAX_VALS, PARAM_NAMES, new Random(1))

  EXACT_SOLUTION.setFitness(0.0)
}