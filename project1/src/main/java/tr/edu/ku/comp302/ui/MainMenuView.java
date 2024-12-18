package tr.edu.ku.comp302.ui;

import tr.edu.ku.comp302.domain.controllers.NavigationController;

import javax.swing.*;

import net.miginfocom.swing.MigLayout;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

/*
 * MainMenuView extends JPanel since all of the views are a part of Card Layout.
 * It uses MigLayout which is outside of the standard Swing Library.
 */
public class MainMenuView extends JPanel {
    private BufferedImage backgroundImage;
    
    public MainMenuView(NavigationController controller) {
        loadBackgroundImage();

        // Set MigLayout as the layout manager
        setLayout(new MigLayout(
            "fill, insets 10, debug",     // Layout constraints: fill panel, no insets
            "[center]",           // Column constraints: center components horizontally
            "[]push[]20[]15[]15[]push" // Row constraints: vertical spacing and centering
        ));

        // Title Label
        //JLabel titleLabel = new JLabel("Welcome to Kafes Game");
        //titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        JPanel titlePanel = createOutlinedTextPanel("Welcome to Kafes game", 48);
        add(titlePanel, "growx, wrap");

        // Buttons
        JButton startButton = createStyledButton("Start Game");
        JButton helpButton = createStyledButton("Help");
        JButton exitButton = createStyledButton("Exit");

        // Button Actions
        //Start Button currently directs to Play Mode View.
        //This will change to Build Mode after implementing Build Mode.
        startButton.addActionListener(e -> controller.startNewPlayMode());
        helpButton.addActionListener(e -> controller.showHelpMenu(evt -> controller.showMainMenu()));
        exitButton.addActionListener(e -> System.exit(0));

        // Add buttons with spacing
        add(startButton, "align center, wrap");
        add(helpButton, "align center, wrap");
        add(exitButton, "align center");

        setOpaque(false); // Enable custom painting for the background
    }

    private void loadBackgroundImage() {
        try {
            // Load the image from the resources folder
            InputStream imageStream = getClass().getClassLoader().getResourceAsStream("assets/mainMenuBackground.jpg");
            if (imageStream != null) {
                backgroundImage = ImageIO.read(imageStream);
            } else {
                System.err.println("Image not found: assets/mainMenuBackground.jpg");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JPanel createOutlinedTextPanel(String text, int fontSize) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    
                Font font = new Font("Arial", Font.BOLD, fontSize);
                g2d.setFont(font);
    
                // Measure text size
                FontMetrics metrics = g2d.getFontMetrics();
                int textWidth = metrics.stringWidth(text);
                int textHeight = metrics.getHeight();
    
                int x = (getWidth() - textWidth) / 2; // Center horizontally
                int y = (getHeight() - textHeight) / 2 + metrics.getAscent(); // Center vertically
    
                // Draw black outline
                g2d.setColor(Color.BLACK);
                g2d.drawString(text, x - 1, y - 1);
                g2d.drawString(text, x + 1, y - 1);
                g2d.drawString(text, x - 1, y + 1);
                g2d.drawString(text, x + 1, y + 1);
    
                // Draw white text
                g2d.setColor(Color.WHITE);
                g2d.drawString(text, x, y);
            }
    
            @Override
            public Dimension getPreferredSize() {
                // Provide default size for the panel
                return new Dimension(500, 100);
            }
        };
    
        panel.setOpaque(false); // Set the panel to be transparent
        return panel;           // Return the panel
    }
    
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 18));
        button.setForeground(Color.WHITE);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        button.setUI(new FaintBackgroundButtonUI());
        button.setPreferredSize(new Dimension(400, 30));
        return button;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
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
