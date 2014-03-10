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
