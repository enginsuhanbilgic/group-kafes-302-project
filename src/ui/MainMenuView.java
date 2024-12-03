package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class MainMenuView extends BaseView {
    private JPanel panel;
    private BufferedImage backgroundImage;

    public MainMenuView() {
        super("Kafes Game - Main Menu");

        // Load the background image
        loadBackgroundImage();

        // Create the panel for this view
        panel = new BackgroundPanel();

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
        panel.setLayout(null);

        // Title label
        JLabel titleLabel = createOutlinedLabel("Welcome to Kafes Game", 48);
        titleLabel.setBounds(350, 50, 780, 60);
        panel.add(titleLabel);

        // Buttons
        JButton startButton = createStyledButton("Start Game");
        JButton helpButton = createStyledButton("Help");
        JButton exitButton = createStyledButton("Exit");

        startButton.setBounds(490, 300, 300, 50);
        helpButton.setBounds(490, 400, 300, 50);
        exitButton.setBounds(490, 500, 300, 50);

        startButton.addActionListener(e -> JOptionPane.showMessageDialog(frame, "Starting the game..."));
        helpButton.addActionListener(e -> new HelpScreenView().showFrame());
        exitButton.addActionListener(e -> System.exit(0));

        panel.add(startButton);
        panel.add(helpButton);
        panel.add(exitButton);
    }

    private JLabel createOutlinedLabel(String text, int fontSize) {
        return new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw black outline
                g2d.setFont(new Font("Arial", Font.BOLD, fontSize));
                g2d.setColor(Color.BLACK);
                g2d.drawString(text, 5 - 1, 40 - 1);
                g2d.drawString(text, 5 - 1, 40 + 1);
                g2d.drawString(text, 5 + 1, 40 - 1);
                g2d.drawString(text, 5 + 1, 40 + 1);

                // Draw white text
                g2d.setColor(Color.WHITE);
                g2d.drawString(text, 5, 40);
            }
        };
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

    // Custom button UI for hover effects
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
