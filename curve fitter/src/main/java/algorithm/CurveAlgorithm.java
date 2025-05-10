package main.java.algorithm;

import java.awt.*;
import java.util.List;
import main.java.model.ControlPoint;

public interface CurveAlgorithm {
    void fit(List<ControlPoint> points);
    Point predict(double t);
    Color color();
    int minPoints();
}
