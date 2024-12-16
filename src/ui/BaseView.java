package ui;

import javax.swing.*;
import java.awt.*;

public abstract class BaseView {
    protected static JFrame frame;

    public BaseView(String title) {
        // Create the JFrame once and reuse it
        if (frame == null) {
            frame = new JFrame(title);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1280, 960);
            frame.setResizable(false);
            frame.setLocationRelativeTo(null); // Center the frame
            frame.setLayout(null); // Allow for custom positioning
        } else {
            frame.setTitle(title);
        }
    }

    // Method to set the current view's content in the frame
    protected void setViewContent(Container content) {
        frame.setContentPane(content);
        frame.revalidate(); // Revalidate the frame to apply changes
        frame.repaint();    // Repaint to ensure proper rendering
    }

    // Abstract method for providing the content pane of each view
    public abstract Container getContentPane();

    // Method to show the frame
    public void showFrame() {
        frame.setVisible(true);
    }
}