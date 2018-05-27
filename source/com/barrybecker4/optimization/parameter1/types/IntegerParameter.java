// Copyright by Barry G. Becker, 2000-2011. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.optimization.parameter1.types;

import com.barrybecker4.common.math.MathUtil;
import com.barrybecker4.optimization.parameter1.Direction;
import com.barrybecker4.optimization.parameter1.ParameterChangeListener;
import com.barrybecker4.optimization.parameter1.redistribution.DiscreteRedistribution;
import com.barrybecker4.optimization.parameter1.ui.DoubleParameterWidget;
import com.barrybecker4.optimization.parameter1.ui.ParameterWidget;

import scala.util.Random;

/**
 *  represents an integer parameter in an algorithm
 *
 *  @author Barry Becker
 */
public class IntegerParameter extends AbstractParameter {

    public IntegerParameter(int val, int minVal, int maxVal, String paramName ) {
        super((double)val, (double)minVal, (double)maxVal, paramName, true);
    }

    public static IntegerParameter createDiscreteParameter(
                                                            int val, int minVal, int maxVal, String paramName,
                                                            int[] discreteSpecialValues, double[] specialValueProbabilities) {
       IntegerParameter param = new IntegerParameter(val, minVal, maxVal, paramName);
       int numValues = (maxVal - minVal + 1);
        param.setRedistributionFunction(
                new DiscreteRedistribution(numValues, discreteSpecialValues, specialValueProbabilities));
        return param;
    }

    public Parameter copy() {
        IntegerParameter p =  new IntegerParameter( (int)Math.round(getValue()), (int)getMinValue(), (int)getMaxValue(), getName() );
        p.setRedistributionFunction(redistributionFunction);
        return p;
    }

    @Override
    public void randomizeValue(Random rand) {
        value = getMinValue() + rand.nextDouble() * (getRange() + 1.0);
    }

    @Override
    public void tweakValue(double r, Random rand)  {
        if (isOrdered()) {
            super.tweakValue(r, rand);
        }
        else {
            double rr = rand.nextDouble();
            if (rr < r) {
                // if not ordered, then just randomize with probability r
                randomizeValue(rand) ;
            }
        }
   }

    protected boolean isOrdered() {
        return true;
    }

    /**
     * increments the parameter based on the number of steps to get from one end of the range to the other.
     * If we are already at the max end of the range, then we can only move in the other direction if at all.
     * @param direction 1 for forward, -1 for backward.
     * @return the size of the increment taken
     */
    public double incrementByEps(Direction direction ) {

        double increment = direction.getMultiplier();
        value = getValue() + increment;
        return increment;
    }

    @Override
    public void setValue(double value) {

        this.value = value;
        // if there is a redistribution function, we need to apply its inverse.
        if (redistributionFunction != null) {
            double v = (value - minValue) / (getRange() + 1.0);
            this.value =
                    minValue + (getRange() + 1.0) * redistributionFunction.getInverseFunctionValue(v);
        }
    }

    @Override
    public double getValue() {
        double value = this.value;
        if (redistributionFunction != null) {
            double v = (this.value - minValue) / (getRange() + 1.0);
            double rv = redistributionFunction.getValue(v);
            value = rv * (getRange() + (1.0 - MathUtil.EPS())) + minValue;
        }
        return Math.round(value);
    }

    @Override
    public boolean isIntegerOnly() {
        return true;
    }

    public Object getNaturalValue() {
        return Math.round(this.getValue());
    }

    @Override
    public Class getType() {
        return int.class;
    }

    public ParameterWidget createWidget(ParameterChangeListener listener) {
        return new DoubleParameterWidget(this, listener);
    }
}