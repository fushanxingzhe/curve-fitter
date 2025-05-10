package main.java.algorithm;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import main.java.model.ControlPoint;

public class Bezier implements CurveAlgorithm {
    private List<ControlPoint> controlPoints = new ArrayList<>();
    private final Color color = new Color(80, 140, 220);

    @Override
    public void fit(List<ControlPoint> points) {
        this.controlPoints = new ArrayList<>(points);
    }

    @Override
    public Point predict(double t) {
        int n = controlPoints.size() - 1;
        if (n < 1) return new Point(0, 0);

        double x = 0.0, y = 0.0;
        for (int i = 0; i <= n; i++) {
            double blend = bernstein(n, i, t);
            x += controlPoints.get(i).getPosition().x * blend;
            y += controlPoints.get(i).getPosition().y * blend;
        }
        return new Point((int)x, (int)y);
    }

    private double bernstein(int n, int i, double t) {
        return combination(n, i) * Math.pow(t, i) * Math.pow(1-t, n-i);
    }

    private long combination(int n, int k) {
        if (k < 0 || k > n) return 0;
        long result = 1;
        for (int i = 1; i <= k; i++) {
            result = result * (n - k + i) / i;
        }
        return result;
    }

    @Override public Color color() { return color; }
    @Override public int minPoints() { return 2; }
}