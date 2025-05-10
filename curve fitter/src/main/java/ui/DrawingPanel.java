package main.java.ui;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import main.java.algorithm.CurveAlgorithm;
import main.java.algorithm.Bezier;
import main.java.algorithm.NURBS;
import main.java.model.ControlPoint;
import main.java.model.SlopeHandle;

public class DrawingPanel extends JPanel {
    private static final int POINT_SIZE = 14;
    private static final int HANDLE_SIZE = 10;
    private static final int SNAP_DISTANCE = 30;
    private static final double HANDLE_RADIUS = 60.0;

    private final java.util.List<ControlPoint> controlPoints = new ArrayList<>();
    private final CurveAlgorithm nurbs = new NURBS(3);
    private final CurveAlgorithm bezier = new Bezier();
    private CurveAlgorithm currentAlgorithm = nurbs;
    private ControlPoint selectedPoint;
    private SlopeHandle activeSlopeHandle;

    public DrawingPanel() {
        setBackground(new Color(250, 250, 250));
        setupInteraction();
        setFocusable(true);
    }

    private void drawControlPointNumbers(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        for(int i=0; i<controlPoints.size(); i++) {
            Point p = controlPoints.get(i).getPosition();
            g2d.drawString(Integer.toString(i+1), p.x + 15, p.y - 15);
        }
    }

    public void setCurveType(String type) {
        currentAlgorithm = "NURBS".equals(type) ? nurbs : bezier;
        repaint();
    }

    private void setupInteraction() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                if (SwingUtilities.isRightMouseButton(e)) {
                    deleteControlPoint(e.getPoint());
                } else {
                    handlePress(e.getPoint());
                }
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                activeSlopeHandle = null;
            }
        });
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (selectedPoint != null) {
                    int keyCode = e.getKeyCode();
                    if (keyCode == KeyEvent.VK_UP) {
                        selectedPoint.setWeight(selectedPoint.getWeight() + 0.1);
                    } else if (keyCode == KeyEvent.VK_DOWN) {
                        selectedPoint.setWeight(Math.max(0.1, selectedPoint.getWeight() - 0.1));
                    }
                    else if (keyCode == KeyEvent.VK_DELETE) {
                        clearAllPoints();
                    }
                    repaint();
                }
                else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    clearAllPoints();
                    repaint();
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                handleDrag(e.getPoint());
                repaint();
            }
        });
    }

    private void deleteControlPoint(Point p) {
        Iterator<ControlPoint> iterator = controlPoints.iterator();
        while (iterator.hasNext()) {
            ControlPoint cp = iterator.next();
            if (p.distance(cp.getPosition()) < SNAP_DISTANCE) {
                iterator.remove();
                if (selectedPoint == cp) selectedPoint = null;
                repaint();
                return;
            }
        }
    }

    private void clearAllPoints() {
        controlPoints.clear();
        selectedPoint = null;
        activeSlopeHandle = null;
    }

    private void handlePress(Point p) {
        if (trySelectSlopeHandle(p)) return;
        selectOrCreateControlPoint(p);
    }

    private boolean trySelectSlopeHandle(Point p) {
        if (selectedPoint == null) return false;
        for (SlopeHandle handle : selectedPoint.getSlopeHandles()) {
            if (p.distance(handle.getPosition()) < HANDLE_SIZE) {
                activeSlopeHandle = handle;
                return true;
            }
        }
        return false;
    }

    private void selectOrCreateControlPoint(Point p) {
        for (ControlPoint cp : controlPoints) {
            if (p.distance(cp.getPosition()) < SNAP_DISTANCE) {
                selectedPoint = cp;
                initSlopeHandles();
                return;
            }
        }
        createNewControlPoint(p);
    }

    private void createNewControlPoint(Point p) {
        ControlPoint newPoint = new ControlPoint(p);
        controlPoints.add(newPoint);
        selectedPoint = newPoint;
        initSlopeHandles();
    }

    private void handleDrag(Point p) {
        if (activeSlopeHandle != null) {
            updateSlopeHandles(p);
        } else if (selectedPoint != null) {
            p.x = Math.max(20, Math.min(getWidth()-20, p.x));
            p.y = Math.max(20, Math.min(getHeight()-20, p.y));
            selectedPoint.getPosition().setLocation(p);
            updateSlopeHandlePositions();
        }
    }

    private void updateSlopeHandles(Point p) {
        Point center = selectedPoint.getPosition();
        double dx = p.x - center.x;
        double dy = p.y - center.y;
        double distance = Math.sqrt(dx*dx + dy*dy);

        activeSlopeHandle.setPosition(p);
        SlopeHandle opposite = getOppositeHandle();
        opposite.setPosition(new Point((int)(center.x - dx), (int)(center.y - dy)));

        selectedPoint.setWeight(Math.max(0.1, distance / HANDLE_RADIUS));
        selectedPoint.setSlopeAngle(Math.atan2(dy, dx));
    }

    private void initSlopeHandles() {
        if (selectedPoint.getSlopeHandles().isEmpty()) {
            Point base = selectedPoint.getPosition();
            selectedPoint.getSlopeHandles().add(new SlopeHandle(
                    new Point((int)(base.x + HANDLE_RADIUS), base.y)));
            selectedPoint.getSlopeHandles().add(new SlopeHandle(
                    new Point((int)(base.x - HANDLE_RADIUS), base.y)));
        }
    }

    private void updateSlopeHandlePositions() {
        Point center = selectedPoint.getPosition();
        double angle = getCurrentAngle();

        selectedPoint.getSlopeHandles().get(0).setPosition(new Point(
                (int)(center.x + HANDLE_RADIUS * Math.cos(angle)),
                (int)(center.y + HANDLE_RADIUS * Math.sin(angle))
        ));
        selectedPoint.getSlopeHandles().get(1).setPosition(new Point(
                (int)(center.x - HANDLE_RADIUS * Math.cos(angle)),
                (int)(center.y - HANDLE_RADIUS * Math.sin(angle))
        ));
    }

    private double getCurrentAngle() {
        if (selectedPoint.getSlopeHandles().isEmpty()) return 0;
        Point handlePos = selectedPoint.getSlopeHandles().get(0).getPosition();
        return Math.atan2(handlePos.y - selectedPoint.getPosition().y,
                handlePos.x - selectedPoint.getPosition().x);
    }

    private SlopeHandle getOppositeHandle() {
        return selectedPoint.getSlopeHandles().get(
                selectedPoint.getSlopeHandles().indexOf(activeSlopeHandle) == 0 ? 1 : 0);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        drawControlPointNumbers(g2d);
        drawConnectionLines(g2d);
        drawControlPoints(g2d);
        drawSlopeHandles(g2d);
        drawCurrentCurve(g2d);
    }

    private void drawConnectionLines(Graphics2D g2d) {
        g2d.setStroke(new BasicStroke(2f));
        g2d.setColor(new Color(200, 200, 200, 150));
        for (int i = 1; i < controlPoints.size(); i++) {
            Point p1 = controlPoints.get(i-1).getPosition();
            Point p2 = controlPoints.get(i).getPosition();
            g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
    }

    private void drawControlPoints(Graphics2D g2d) {
        for (ControlPoint cp : controlPoints) {
            Point p = cp.getPosition();
            boolean isSelected = cp == selectedPoint;
            g2d.setColor(new Color(0, 0, 0, 30));
            g2d.fillOval(p.x-POINT_SIZE/2+3, p.y-POINT_SIZE/2+3, POINT_SIZE, POINT_SIZE);
            GradientPaint gradient = new GradientPaint(
                    p.x-POINT_SIZE/2, p.y-POINT_SIZE/2,
                    isSelected ? new Color(255, 90, 90) : new Color(80, 140, 220),
                    p.x+POINT_SIZE/2, p.y+POINT_SIZE/2,
                    isSelected ? new Color(220, 70, 70) : new Color(60, 120, 200)
            );
            g2d.setPaint(gradient);
            g2d.fillOval(p.x-POINT_SIZE/2, p.y-POINT_SIZE/2, POINT_SIZE, POINT_SIZE);
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.setColor(new Color(255, 255, 255, 150));
            g2d.drawOval(p.x-POINT_SIZE/2, p.y-POINT_SIZE/2, POINT_SIZE, POINT_SIZE);
            if (isSelected) {
                g2d.setColor(Color.BLACK);
                String text = String.format("权重: %.1f", cp.getWeight());
                g2d.drawString(text, p.x + POINT_SIZE, p.y + POINT_SIZE/2);
            }
            if (isSelected) {
                String coordText = String.format("(%d,%d)", p.x, p.y);
                g2d.drawString(coordText, p.x + POINT_SIZE, p.y - POINT_SIZE/2);
            }
        }
    }

    private void drawSlopeHandles(Graphics2D g2d) {
        if (selectedPoint == null) return;
        Point center = selectedPoint.getPosition();

        g2d.setStroke(new BasicStroke(1.5f));
        g2d.setColor(new Color(150, 200, 150, 150));

        for (SlopeHandle handle : selectedPoint.getSlopeHandles()) {
            Point h = handle.getPosition();
            g2d.drawLine(center.x, center.y, h.x, h.y);
            g2d.setColor(new Color(100, 180, 100));
            g2d.fillOval(h.x-HANDLE_SIZE/2, h.y-HANDLE_SIZE/2, HANDLE_SIZE, HANDLE_SIZE);
        }
    }

    private void drawCurrentCurve(Graphics2D g2d) {
        if (controlPoints.size() >= currentAlgorithm.minPoints()) {
            currentAlgorithm.fit(controlPoints);
            drawCurve(g2d, currentAlgorithm);
        }
    }

    private void drawCurve(Graphics2D g2d, CurveAlgorithm algorithm) {
        ArrayList<Point> validPoints = new ArrayList<>();
        for (int t = 0; t <= 200; t++) {
            Point p = algorithm.predict(t / 200.0);
            if (isValidPoint(p)) {
                validPoints.add(p);
            }
        }
        if (validPoints.size() >= 2) {
            g2d.setColor(algorithm.color());
            g2d.setStroke(new BasicStroke(3.5f));

            Point prev = validPoints.get(0);
            for (int i = 1; i < validPoints.size(); i++) {
                Point current = validPoints.get(i);
                if (shouldDrawSegment(prev, current)) {
                    g2d.drawLine(prev.x, prev.y, current.x, current.y);
                }
                prev = current;
            }
        }
    }

    private boolean isValidPoint(Point p) {
        return p != null &&
                p.x >= -50 && p.x <= getWidth()+50 &&
                p.y >= -50 && p.y <= getHeight()+50;
    }

    private boolean shouldDrawSegment(Point p1, Point p2) {
        int dx = Math.abs(p1.x - p2.x);
        int dy = Math.abs(p1.y - p2.y);
        return dx < getWidth()*0.2 && dy < getHeight()*0.2;
    }
}