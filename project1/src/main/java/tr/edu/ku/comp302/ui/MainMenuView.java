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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import tr.edu.ku.comp302.domain.controllers.NavigationController;
import tr.edu.ku.comp302.domain.controllers.SaveLoadController;
import tr.edu.ku.comp302.domain.models.GameState;

/*
 * MainMenuView extends JPanel since all of the views are a part of Card Layout.
 * It uses MigLayout which is outside of the standard Swing Library.
 */
public class MainMenuView extends JPanel {
    private BufferedImage backgroundImage;

    private static final String SAVES_DIRECTORY = "saves";
    
    public MainMenuView(NavigationController controller) {
        loadBackgroundImage();

        // Set MigLayout as the layout manager
        setLayout(new MigLayout(
                "fill, insets 10",
                "[center]",
                "[]push[]20[]15[]15[]15[]15[]push"
        ));

        // Buttons
        JButton startButton = createStyledButton("Start Game"); // Starts a blank game session, can be deleted in future.
        JButton loadGameButton = createStyledButton("Load Game");
        JButton buildModeButton = createStyledButton("Build Mode");
        JButton helpButton = createStyledButton("Help");
        JButton exitButton = createStyledButton("Exit");

        // Button Actions
        startButton.addActionListener(e -> {
            controller.resetPlayer();
            controller.startNewPlayMode();
        });

        loadGameButton.addActionListener(e -> {
            List<String> savedGames = getSavedDesigns();

            if (savedGames.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "No saved games found.", 
                    "Information", 
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            } else {
                String[] saves = savedGames.toArray(new String[0]);
                String selectedDesign = (String) JOptionPane.showInputDialog(this,
                    "Select a game to load:",
                    "Load Game",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    saves,
                    saves[0]);

                if (selectedDesign != null) {
                    try {
                        // 1) Load the GameState
                        GameState loadedState = SaveLoadController.loadGame(selectedDesign);

                        if (loadedState == null) {
                            JOptionPane.showMessageDialog(this,
                                    "Failed to load the game file. It may be corrupted or missing.",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        // 2) Let NavigationController create a new PlayMode with that state
                        JOptionPane.showMessageDialog(this, 
                            "Game loaded successfully!", 
                            "Success", 
                            JOptionPane.INFORMATION_MESSAGE);
                        controller.startLoadedGame(loadedState);
                        
                    } catch (Exception err) {
                        err.printStackTrace(); // print full stack trace to console
                        JOptionPane.showMessageDialog(this,
                                "Error loading game: " + err.toString(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }

                }
            }
        });

        //  Yeni buton eklendi
        buildModeButton.addActionListener(e -> {
            controller.showBuildMode();
        });

        helpButton.addActionListener(e -> controller.showHelpMenu(evt -> controller.showMainMenu()));
        exitButton.addActionListener(e -> System.exit(0));

        add(startButton,    "pos 300px 36%, wrap"); 
        add(loadGameButton, "pos 300px 43%, wrap");
        add(buildModeButton,"pos 300px 50%, wrap"); 
        add(helpButton,     "pos 300px 57%, wrap");
        add(exitButton,     "pos 300px 64%");      

        setOpaque(false);
    }

    private List<String> getSavedDesigns() {
        try {
            return Files.list(Paths.get(SAVES_DIRECTORY))
                    .filter(path -> path.toString().endsWith(".ser"))
                    .map(path -> path.getFileName().toString().replace(".ser", ""))
                    .sorted()
                    .toList();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    } 

    private void loadBackgroundImage() {
        try {
            // Load the image from the resources folder
            InputStream imageStream = getClass().getClassLoader().getResourceAsStream("assets/mainMenu.png");
            if (imageStream != null) {
                backgroundImage = ImageIO.read(imageStream);
            } else {
                System.err.println("Image not found: assets/mainMenu.jpg");
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