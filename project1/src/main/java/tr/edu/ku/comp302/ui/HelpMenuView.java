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

/**
 * A more detailed "Help Menu" panel that shows instructions on how to play the game.
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
        // -- Genişletilmiş, HTML formatlı açıklayıcı yardım metni --
        String helpText = 
            "<html>"
        + "<body style=\"text-align: center; font-family: Arial; color: white;\">"
        + "  <div style='background-color: rgba(0,0,0,0.6); padding: 20px; margin: 20px;'>"
        + "    <h1>How to Play</h1>"
        + "    <p><strong>Movement:</strong> Use the arrow keys (←, →, ↑, ↓) to move your hero around the hall. "
        + "    You cannot pass through walls. Your main goal is to find hidden runes in each hall to unlock the door.</p>"
        + ""
        + "    <p><strong>Runes & Doors:</strong> Each hall has a hidden rune. "
        + "    Click on objects (only those you are adjacent to) to check if the rune is underneath. "
        + "    Once you find the rune, the exit door opens and you can proceed to the next hall.</p>"
        + ""
        + "    <p><strong>Monsters:</strong></p>"
        + "    <ul style=\"text-align: left; margin-left: 40px;\">"
        + "      <li><strong>Archer Monster:</strong> Appears randomly; if you get within 4 squares without a Cloak of Protection, you lose a life.</li>"
        + "      <li><strong>Fighter Monster:</strong> Roams randomly. If it’s next to you, it will stab you. You can distract it by throwing a Luring Gem.</li>"
        + "      <li><strong>Wizard Monster:</strong> Teleports the rune every 5 seconds. It does not attack but keeps you guessing!</li>"
        + "    </ul>"
        + ""
        + "    <p><strong>Enchantments:</strong> These power-ups appear randomly and disappear if not collected quickly. Some apply immediately, others go into your bag for later use.</p>"
        + "    <ul style=\"text-align: left; margin-left: 40px;\">"
        + "      <li><strong>Extra Time:</strong> +5 seconds to your timer (applies immediately).</li>"
        + "      <li><strong>Reveal (R key):</strong> Highlights a 4x4 area containing the rune for 10 seconds.</li>"
        + "      <li><strong>Cloak of Protection (P key):</strong> Renders you invisible to Archer Monsters for 20 seconds.</li>"
        + "      <li><strong>Luring Gem (B + direction):</strong> Throws a gem to lure Fighter Monsters away.</li>"
        + "      <li><strong>Extra Life:</strong> Instantly increases your life count by 1.</li>"
        + "    </ul>"
        + ""
        + "    <p><strong>Timer & Lives:</strong> You start each hall with a time limit based on the number of objects. "
        + "    If time runs out or you lose all your lives, the game ends. Collect enchantments to extend time or gain extra lives.</p>"
        + ""
        + "    <p>Good luck finding all the runes and escaping the dungeon!</p>"
        + "  </div>"
        + "</body>"
        + "</html>";


        JLabel helpLabel = new JLabel(helpText);
        helpLabel.setFont(new Font("Arial", Font.PLAIN, 16));
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
