/*******************************************************************************
 * Copyright (c) 2014, Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Julien Vermillard - initial API and implementation
 *******************************************************************************/
package org.eclipse.tutorials.mqtt;

import java.util.ArrayList;
import java.util.List;

/**
 * Store a list of sample and produce the average when needed.
 */
public class ConsolidatedValues {

    private List<Double> samples = new ArrayList<>();

    /**
     * Add a sample to the list of value to be part of the consolidated value.
     * 
     * @param sample the value to add
     */
    public void addSample(double sample) {
        samples.add(sample);
    }

    /**
     * Get the number of samples for this consolidated value
     */
    public int getSampleCount() {
        return samples.size();
    }

    /**
     * Compute the average value of the list of added samples.
     */
    public double getAverage() {
        double v = 0;

        for (double d : samples) {
            v += d;
        }

        return v / samples.size();
    }
}
