package com.barrybecker4.optimization.viewer.projectors;

import com.barrybecker4.common.math.Range;
import com.barrybecker4.optimization.parameter.ParameterArray;

import javax.vecmath.Point2d;

/**
 * @author Barry Becker
 */
public interface IProjector {

    Point2d project(ParameterArray params);

    Range getXRange(ParameterArray params);

    Range getYRange(ParameterArray params);
}
