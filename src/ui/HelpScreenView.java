package ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class HelpScreenView extends BaseView {
    private JPanel panel;
    private BufferedImage backgroundImage;

    public HelpScreenView() {
        super("Kafes Game - Help");

        // Load the background image
        loadBackgroundImage();

        // Create the panel for this view
        panel = new BackgroundPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // Use BoxLayout for vertical alignment

        // Add components to the panel
        createComponents();

        // Set this panel as the frame's content pane
        setViewContent(panel);
    }

    private void loadBackgroundImage() {
        try {
            backgroundImage = ImageIO.read(new File("src/assets/mainMenuBackground.jpg"));
        } catch (IOException e) {
            backgroundImage = null; // Handle image load failure gracefully
        }
    }

    private void createComponents() {
        // Spacer for centering the content vertically
        panel.add(Box.createVerticalGlue());

        // Help text with line breaks and proper alignment
        JLabel helpLabel = createOutlinedLabel("<html><center>This is how you play Kafes Game.<br>Good luck!</center></html>", 24);
        helpLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Center horizontally
        panel.add(helpLabel);

        // Spacer between label and button
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Back button
        JButton backButton = createStyledButton("Back");
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT); // Center horizontally
        backButton.addActionListener(e -> new MainMenuView().showFrame());
        panel.add(backButton);

        // Spacer for centering the content vertically
        panel.add(Box.createVerticalGlue());
    }

    private JLabel createOutlinedLabel(String text, int fontSize) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, fontSize));
        label.setForeground(Color.WHITE);
        label.setOpaque(false); // Make the background transparent
        label.setHorizontalAlignment(SwingConstants.CENTER); // Ensure proper horizontal centering
        label.setVerticalAlignment(SwingConstants.CENTER); // Ensure proper vertical centering
    
        // Wrap text in <html> to enable HTML rendering
        label.setText("<html><center>" + text + "</center></html>");
        return label;
    }
    

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 18));
        button.setForeground(Color.WHITE);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        button.setUI(new FaintBackgroundButtonUI());
        return button;
    }

    @Override
    public Container getContentPane() {
        return panel;
    }

    // Custom panel for rendering the background image
    private class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
            }
        }
    }

    private class FaintBackgroundButtonUI extends javax.swing.plaf.basic.BasicButtonUI {
        @Override
        public void paint(Graphics g, JComponent c) {
            Graphics2D g2d = (Graphics2D) g.create();
            AbstractButton button = (AbstractButton) c;

            if (button.getModel().isRollover()) {
                g2d.setColor(new Color(255, 255, 255, 100));
            } else {
                g2d.setColor(new Color(255, 255, 255, 40));
            }
            g2d.fillRoundRect(0, 0, button.getWidth(), button.getHeight(), 10, 10);

            super.paint(g, c);
            g2d.dispose();
        }
    }
}
