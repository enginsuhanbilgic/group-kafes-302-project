package tr.edu.ku.comp302.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import tr.edu.ku.comp302.domain.controllers.NavigationController;

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
                "fill, insets 10",
                "[center]",
                "[]push[]20[]15[]15[]15[]push" // GÜNCELLENDİ: satır aralığı artırdık
        ));

       

        // Buttons
        JButton startButton = createStyledButton("Start Game");
        JButton buildModeButton = createStyledButton("Build Mode");  // GÜNCELLENDİ
        JButton helpButton = createStyledButton("Help");
        JButton exitButton = createStyledButton("Exit");

        // Button Actions
        startButton.addActionListener(e -> {
            controller.resetPlayer();
            controller.startNewPlayMode();
        });

        //  Yeni buton eklendi
        buildModeButton.addActionListener(e -> {
            controller.showBuildMode();
        });

        helpButton.addActionListener(e -> controller.showHelpMenu(evt -> controller.showMainMenu()));
        exitButton.addActionListener(e -> System.exit(0));

        add(startButton,    "pos 300px 43%, wrap"); 
        add(buildModeButton,"pos 300px 50%, wrap"); 
        add(helpButton,     "pos 300px 57%, wrap");
        add(exitButton,     "pos 300px 64%");      

        setOpaque(false);
    }

    private void loadBackgroundImage() {
        try {
            // Load the image from the resources folder
            InputStream imageStream = getClass().getClassLoader().getResourceAsStream("assets/mainMenu.png");
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
        button.setPreferredSize(new Dimension(500, 40));
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
