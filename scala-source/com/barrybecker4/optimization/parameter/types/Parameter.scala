// Copyright by Barry G. Becker, 2000-2018. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.optimization.parameter.types

import com.barrybecker4.optimization.parameter.Direction
import com.barrybecker4.optimization.parameter.ParameterChangeListener
import com.barrybecker4.optimization.parameter.redistribution.RedistributionFunction
import com.barrybecker4.optimization.parameter.ui.ParameterWidget
import scala.util.Random


/**
  * Interface for a general parameter of some type.
  * @author Barry Becker
  */
trait Parameter {

  /** @return the name of the parameter. */
  def getName: String

  /** Increments the parameter a little bit in the specified direction.
    * If we are already at the max end of the range, then we increment in a negative direction.
    * @param direction either forward, or backward.
    * @return the size of the increment taken
    */
  def incrementByEps(direction: Direction): Double

  /** Modify the value of this parameter by a little bit.
    * The amount that it changes by depends on the size of r, which is the
    * number of standard deviations of the gaussian probability distribution to use.
    * @param r the size of the (1 std deviation) gaussian neighborhood to select a random nbr from
    *          (relative to each parameter range).
    */
  def tweakValue(r: Double, rand: Random): Unit

  /** Randomizes the value within its range.
    * If no redistribution function, then the distribution is uniform.
    */
  def randomizeValue(rand: Random): Unit

  /** @return a value whose type matches the type of the parameter.
    *         (e.g. String for StringParameter Integer for IntegerParameter)
    */
  def getNaturalValue: Any

  /** @return the double value of this parameter. The natural value needs to be converted to some double.
    *    e.g a BooleanParameter is returned as a 0 (false) or 1 (true).
    */
  def getValue: Double

  /** This optional function redistributes the normally uniform
    * parameter distribution into something potentially completely different
    * like a gaussian, or one where specific values have higher probability than others.
    * @param func the redistribution function to use
    */
  def setRedistributionFunction(func: RedistributionFunction): Unit

  /** Set the value of the parameter.
    * If there is a redistribution function, then
    * set the value in the inverse redistribution space - at least
    * until I implement the inverse redistribution function.
    * @param value value to set.
    */
  def setValue(value: Double): Unit

  /** @return the minimum value of the parameters range.*/
  def getMinValue: Double

  /** @return the maximum value of the parameters range. */
  def getMaxValue: Double

  /** @return the parameters range.*/
  def getRange: Double

  /** @return true if this parameter should be treated as an integer and not a double.*/
  def isIntegerOnly: Boolean

  /** @return class type of the underlying parameter value (e.g. float.class or int.class)*/
  def getType: Class[_]

  /** All parameters can produce copies of themselves.
    * @return new copy of the parameter
    */
  def copy: Parameter

  /** A UI control of some type for changing the parameter.
    * @param listener parameter change listener.
    * @return new parameter widget
    */
  def createWidget(listener: ParameterChangeListener): ParameterWidget
}