package com.example.ioelsensorapp;

import java.nio.ByteBuffer;

public class PeakDetection {

    private static final int DEFAULT_LAG = 32;
    private static final int DEFAULT_THRESHOLD = 2;
    private static final double DEFAULT_INFLUENCE = 0.5;
    private static final double DEFAULT_EPSILON = 0.01;

    int index= 0;
    int lag= DEFAULT_LAG;
    int threshold= DEFAULT_THRESHOLD;
    double influence= DEFAULT_INFLUENCE;
    double EPSILON= DEFAULT_EPSILON;
    int peak= 0;

    double[] data;
    double[] avg;
    double[] std;

    public PeakDetection() {
        index = 0;
        lag = DEFAULT_LAG;
        threshold = DEFAULT_THRESHOLD;
        influence = DEFAULT_INFLUENCE;
        EPSILON = DEFAULT_EPSILON;
        peak = 0;
    }

    public void begin() {
        data = new double[DEFAULT_LAG];
        avg = new double[DEFAULT_LAG];
        std = new double[DEFAULT_LAG];
        for (int i = 0; i < lag; i++) {
            data[i] = 0.0;
            avg[i] = 0.0;
            std[i] = 0.0;
        }
    }

    public void begin(int lag_i, int threshold_i, double influence_d) {
        lag = lag_i;
        threshold = threshold_i;
        influence = influence_d;
        data = new double[lag];
        avg = new double[lag];
        std = new double[lag];
        for (int i = 0; i < lag; i++) {
            data[i] = 0.0;
            avg[i] = 0.0;
            std[i] = 0.0;
        }
    }

    public void add(double newSample) {
        peak = 0;
        int i = index % lag;
        int j = (index +1) % lag;
        double deviation = newSample - avg[i];
        if (deviation > threshold *std[i]) {
            data[j] = influence * newSample + (1.0 - influence) * data[i];
            peak = 1;
        } else if (deviation < -threshold *std[i]) {
            data[j] = influence * newSample + (1.0 - influence) * data[i];
            peak = -1;
        } else {
            data[j] = newSample;
        }
        avg[j] = getAvg(j, lag);
        std[j] = getStd(j, lag);
        index++;
        if (index >= 16383) //2^14
        {
            index = lag + j;
        }
    }

    public double getFilt()
    {
        int i = index % lag;
        return avg[i];
    }

    public double getPeak()
    {
        return peak;
    }

    public double getAvg(int start, int len)
    {
        double x = 0.0;
        for (int i = 0; i < len; ++i)
        {
            x += data[(start + i) % lag];
        }
        return x / len;
    }

    public double getPoint(int start, int len)
    {
        double xi = 0.0;
        for (int i = 0; i < len; ++i)
        {
            xi += data[(start + i) % lag] * data[(start + i) % lag];
        }
        return xi / len;
    }

    public double getStd(int start, int len)
    {
        double x1 = getAvg(start, len);
        double x2 = getPoint(start, len);
        double powx1 = x1 * x1;
        double std = x2 - powx1;
        if (std > -EPSILON && std < EPSILON)
        {
            return 0.0;
        }
        else
        {
            return Math.sqrt(x2 - powx1);
        }
    }

}
