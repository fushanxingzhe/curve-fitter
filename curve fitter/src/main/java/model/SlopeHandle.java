package main.java.model;

import java.awt.Point;

public class SlopeHandle {
    private Point position = new Point();

    public SlopeHandle(Point p) {
        position.setLocation(p);
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }
}
