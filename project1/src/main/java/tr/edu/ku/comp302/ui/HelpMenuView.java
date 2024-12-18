package tr.edu.ku.comp302.ui;

import tr.edu.ku.comp302.domain.controllers.NavigationController;

import net.miginfocom.swing.MigLayout;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/*TO DO
 * Placeholder HelpMenuView
 */
public class HelpMenuView extends JPanel {
    private BufferedImage backgroundImage;

    public HelpMenuView(JFrame parentFrame, ActionListener onBack) {
        // Load the background image
        loadBackgroundImage();

        // Set MigLayout for the HelpMenuView
        setLayout(new MigLayout(
            "fill, insets 0",       // Fill entire space, no padding
            "[center]",             // Center components horizontally
            "push[]20[]push"        // Push components to center vertically
        ));

        // Add components to the panel
        createComponents(onBack);

        setOpaque(false); // Allow background painting
    }

    private void loadBackgroundImage() {
        try {
            // Load the background image from resources
            InputStream imageStream = getClass().getClassLoader().getResourceAsStream("assets/mainMenuBackground.jpg");
            if (imageStream != null) {
                backgroundImage = ImageIO.read(imageStream);
            } else {
                System.err.println("Background image not found!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createComponents(ActionListener onBack) {
        // Help Label
        JLabel helpLabel = new JLabel("<html><center>Welcome to the Help Menu!<br>"
                + "Learn how to play Kafes Game.<br>Good Luck!</center></html>");
        helpLabel.setFont(new Font("Arial", Font.BOLD, 24));
        helpLabel.setForeground(Color.WHITE);
        helpLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(helpLabel, "wrap, align center, gapbottom 20");

        // Back Button
        JButton backButton = createStyledButton("Back");
        backButton.setPreferredSize(new Dimension(400, 30));
        backButton.addActionListener(onBack);
        add(backButton, "align center");
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        
        button.setFont(new Font("Arial", Font.PLAIN, 18));
        button.setForeground(Color.WHITE);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        return button;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}