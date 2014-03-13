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

public class ConsolidatedValues {

	private List<Double> samples = new ArrayList<>();

	public void addSample(double sample) {
		samples.add(sample);
	}

	public int getSampleCount() {
		return samples.size();
	}

	public double getAverage() {
		double v = 0;

		for (double d : samples) {
			v += d;
		}

		return v / samples.size();
	}
}
