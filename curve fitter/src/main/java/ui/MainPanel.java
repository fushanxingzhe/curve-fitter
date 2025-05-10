package main.java.ui;

import javax.swing.*;
import java.awt.*;

public class MainPanel extends JPanel {
    private final DrawingPanel drawingPanel = new DrawingPanel();

    public MainPanel() {
        setLayout(new BorderLayout());
        add(drawingPanel, BorderLayout.CENTER);
        JComboBox<String> comboBox = new JComboBox<>(new String[]{"NURBS", "贝塞尔"});
        comboBox.addActionListener(e -> drawingPanel.setCurveType((String) comboBox.getSelectedItem()));
        add(comboBox, BorderLayout.NORTH);
    }
}
