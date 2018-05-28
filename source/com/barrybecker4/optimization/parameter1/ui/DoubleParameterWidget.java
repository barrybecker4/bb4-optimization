// Copyright by Barry G. Becker, 2000-2011. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.optimization.parameter1.ui;

import com.barrybecker4.optimization.parameter1.ParameterChangeListener;
import com.barrybecker4.optimization.parameter1.types.Parameter;
import com.barrybecker4.ui.sliders.LabeledSlider;
import com.barrybecker4.ui.sliders.SliderChangeListener;

import java.awt.*;

/**
 *
 * @author Barry Becker
 */
public class DoubleParameterWidget extends ParameterWidget implements SliderChangeListener {

    private LabeledSlider slider;

    public DoubleParameterWidget(Parameter param, ParameterChangeListener listener) {
        super(param, listener);
    }

    /**
     * Create a ui widget appropriate for the parameter type.
     */
    @Override
    protected void addChildren() {

         slider =
                 new LabeledSlider(parameter.getName(), parameter.getValue(),
                                               parameter.getMinValue(), parameter.getMaxValue());
         if (parameter.isIntegerOnly()) {
             slider.setShowAsInteger(true);
         }
         slider.addChangeListener(this);
         add(slider, BorderLayout.CENTER);
    }

    /**
     * @param slider the slider that changed.
     */
    public void sliderChanged(LabeledSlider slider) {
         parameter.setValue(slider.getValue());
         doNotification();
    }

    @Override
    public void refreshInternal() {
        slider.setValue(parameter.getValue());
    }
}
