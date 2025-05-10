package main.java.model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class ControlPoint {
    private Point position = new Point();
    private List<SlopeHandle> slopeHandles = new ArrayList<>();
    private double slopeAngle;
    private double weight = 1.0;

    public ControlPoint(Point p) {
        position.setLocation(p);
    }

    public Point getPosition() {
        return position;
    }

    public List<SlopeHandle> getSlopeHandles() {
        return slopeHandles;
    }

    public double getSlopeAngle() {
        return slopeAngle;
    }

    public void setSlopeAngle(double slopeAngle) {
        this.slopeAngle = slopeAngle;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}
