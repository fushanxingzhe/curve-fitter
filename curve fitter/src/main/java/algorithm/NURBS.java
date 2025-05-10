package main.java.algorithm;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import main.java.model.ControlPoint;

public class NURBS implements CurveAlgorithm {
    private int degree;
    private List<ControlPoint> controlPoints = new ArrayList<>();
    private double[] weights;
    private final Color color = new Color(220, 80, 80);

    public NURBS(int degree) {
        this.degree = Math.max(1, Math.min(degree, 3));
    }

    @Override
    public void fit(List<ControlPoint> points) {
        this.controlPoints = new ArrayList<>(points);
        this.weights = new double[points.size()];
        if (!points.isEmpty()) {
            points.get(0).setWeight(1.0);
            points.get(points.size()-1).setWeight(1.0);
        }

        for (int i = 0; i < points.size(); i++) {
            weights[i] = points.get(i).getWeight();
        }
    }

    @Override
    public Point predict(double t) {
        if (controlPoints.size() < minPoints()) return null;
        t = Math.max(0.0, Math.min(1.0, t));
        if (t == 0.0) return controlPoints.get(0).getPosition();
        if (t == 1.0) return controlPoints.get(controlPoints.size()-1).getPosition();

        double[] knots = generateKnots();
        int n = controlPoints.size() - 1;

        double x = 0.0, y = 0.0, denominator = 0.0;
        for (int i = 0; i <= n; i++) {
            double basis = basisFunction(degree, i, knots, t);
            basis *= weights[i];
            x += controlPoints.get(i).getPosition().x * basis;
            y += controlPoints.get(i).getPosition().y * basis;
            denominator += basis;
        }

        return denominator < 1e-6 ? null : new Point((int)(x/denominator), (int)(y/denominator));
    }

    private double[] generateKnots() {
        int numCtrlPoints = controlPoints.size();
        int numKnots = numCtrlPoints + degree + 1;
        double[] knots = new double[numKnots];
        Arrays.fill(knots, 0, degree + 1, 0.0);
        Arrays.fill(knots, numKnots - degree - 1, numKnots, 1.0);
        if (numCtrlPoints > degree + 1) {
            int internalCount = numCtrlPoints - degree - 1;
            double step = 1.0 / (internalCount + 1);
            for (int i = degree + 1; i < numKnots - degree - 1; i++) {
                knots[i] = (i - degree) * step;
            }
        }
        return knots;
    }

    private double basisFunction(int p, int i, double[] knots, double t) {
        if (p == 0) {
            boolean lower = t >= knots[i] - 1e-6;
            boolean upper = t <= knots[i+1] + 1e-6;
            return (lower && upper) ? 1.0 : 0.0;
        }

        double left = 0.0, right = 0.0;
        final double denominator1 = knots[i+p] - knots[i];
        if (denominator1 > 1e-6) {
            left = ((t - knots[i]) / denominator1) * basisFunction(p-1, i, knots, t);
        }

        final double denominator2 = knots[i+p+1] - knots[i+1];
        if (denominator2 > 1e-6) {
            right = ((knots[i+p+1] - t) / denominator2) * basisFunction(p-1, i+1, knots, t);
        }

        return left + right;
    }

    public void setDegree(int degree) {
        this.degree = Math.max(1, Math.min(degree, 5));
    }

    @Override public Color color() { return color; }
    @Override public int minPoints() { return degree + 1; }
}
