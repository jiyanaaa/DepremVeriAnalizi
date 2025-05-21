package com.DepremVeriAnalizi.ui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CustomComponents {

    public static JPanel createLabeledComboBox(String label, JComboBox<?> comboBox) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.add(new JLabel(label));
        panel.add(comboBox);
        return panel;
    }

    public static JPanel createLabeledSlider(String label, JSlider slider) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JLabel titleLabel = new JLabel(label);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);

        slider.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(slider);

        return panel;
    }

    public static JButton createStyledButton(String text, String iconPath) {
        JButton button = new JButton(text);
        if (iconPath != null) {
            button.setIcon(new ImageIcon(iconPath));
        }
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        return button;
    }

    public static JPanel createResultPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Analiz Sonuçları"));
        panel.setPreferredSize(new Dimension(300, 200));
        return panel;
    }

    public static void addResultRow(JPanel panel, String label, String value) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row.add(new JLabel(label + ":"));
        row.add(new JLabel(value));
        panel.add(row);
    }

    public static JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        return panel;
    }

    public static void showError(String title, String message) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public static void showInfo(String title, String message) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean showConfirm(String title, String message) {
        return JOptionPane.showConfirmDialog(null, message, title,
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public static String showInput(String title, String message) {
        return JOptionPane.showInputDialog(null, message, title, JOptionPane.QUESTION_MESSAGE);
    }

    public static JProgressBar createProgressBar() {
        JProgressBar progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(200, 20));
        return progressBar;
    }

    public static JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        return statusBar;
    }

    public static void setWaitCursor(Component component, boolean waiting) {
        component.setCursor(waiting ?
                Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) :
                Cursor.getDefaultCursor());
    }
}