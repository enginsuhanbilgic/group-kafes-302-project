package tr.edu.ku.comp302.ui;

import net.miginfocom.swing.MigLayout;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.function.Function;

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
            InputStream imageStream = getClass().getClassLoader().getResourceAsStream("assets/mainMenu.png");
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
        // 1. Load icon URLs for all items
        URL archerIconUrl      = getClass().getClassLoader().getResource("assets/npc_archer.png");
        URL fighterIconUrl     = getClass().getClassLoader().getResource("assets/npc_fighter.png");
        URL wizardIconUrl      = getClass().getClassLoader().getResource("assets/npc_wizard.png");
        URL extraTimeIconUrl   = getClass().getClassLoader().getResource("assets/enchantment_extratime.png");
        URL revealIconUrl      = getClass().getClassLoader().getResource("assets/enchantment_reveal.png");
        URL cloakIconUrl       = getClass().getClassLoader().getResource("assets/enchantment_cloak.png");
        URL gemIconUrl         = getClass().getClassLoader().getResource("assets/enchantment_gem.png");
        URL extraLifeIconUrl   = getClass().getClassLoader().getResource("assets/enchantment_heart.png");

        // Convert URLs to strings for <img src="..."/>
        String archerIconPath    = (archerIconUrl    != null) ? archerIconUrl.toExternalForm()    : null;
        String fighterIconPath   = (fighterIconUrl   != null) ? fighterIconUrl.toExternalForm()   : null;
        String wizardIconPath    = (wizardIconUrl    != null) ? wizardIconUrl.toExternalForm()    : null;
        String extraTimeIconPath = (extraTimeIconUrl != null) ? extraTimeIconUrl.toExternalForm() : null;
        String revealIconPath    = (revealIconUrl    != null) ? revealIconUrl.toExternalForm()    : null;
        String cloakIconPath     = (cloakIconUrl     != null) ? cloakIconUrl.toExternalForm()     : null;
        String gemIconPath       = (gemIconUrl       != null) ? gemIconUrl.toExternalForm()       : null;
        String extraLifeIconPath = (extraLifeIconUrl != null) ? extraLifeIconUrl.toExternalForm() : null;

        // Helper method to embed an icon or show "(icon missing)"
        // width/height can be adjusted or removed if your icons are already suitably sized
        Function<String, String> imgOrFallback = path ->
                (path != null)
                        ? "<img src='" + path + "' width='24' height='24' style='vertical-align:middle;'/>"
                        : "(icon missing)";

        // 2. Build the help text, embedding the icon references in the HTML
        String helpText =
                "<html>"
                        + "  <body style=\"font-family: 'Open Sans', Arial, sans-serif; font-size: 18px; line-height: 1.5; color: white; margin: 0;\">"
                        + "    <div style='background-color: rgba(0,0,0,0.6); padding: 20px; margin: 20px;'>"
                        + "      <h1 style='text-align: center; margin-top: 0;'>How to Play</h1>"

                        + "      <h2 style='font-size: 22px; text-decoration: underline;'>Movement</h2>"
                        + "      <p>Use the arrow keys (←, →, ↑, ↓) to move your hero around the hall. You cannot pass through walls. "
                        + "         Your main goal is to find hidden runes in each hall to unlock the door.</p>"
                        + "      <p>Press 'H' key to toggle damage range indicators for monsters.</p>"

                        + "      <h2 style='font-size: 22px; text-decoration: underline;'>Runes &amp; Doors</h2>"
                        + "      <p>Each hall has a hidden rune. Click on objects (only those you are adjacent to) to check if the rune is underneath. "
                        + "         Once you find the rune, the exit door opens and you can proceed to the next hall.</p>"

                        + "      <h2 style='font-size: 22px; text-decoration: underline;'>Monsters</h2>"
                        + "      <ul style='list-style-type: disc; margin-left: 40px;'>"
                        + "        <li>"
                        + "          <strong>Archer Monster:</strong> "
                        +            imgOrFallback.apply(archerIconPath)
                        + "          Appears randomly; if you get within 4 squares without a Cloak of Protection, you lose a life."
                        + "        </li>"
                        + "        <li>"
                        + "          <strong>Fighter Monster:</strong> "
                        +            imgOrFallback.apply(fighterIconPath)
                        + "          Roams randomly. If it’s next to you, it will stab you. You can distract it by throwing a Luring Gem."
                        + "        </li>"
                        + "        <li>"
                        + "          <strong>Wizard Monster:</strong> "
                        +            imgOrFallback.apply(wizardIconPath)
                        + "          Teleports the rune every 5 seconds. It does not attack but keeps you guessing!"
                        + "        </li>"
                        + "      </ul>"

                        + "      <h2 style='font-size: 22px; text-decoration: underline;'>Enchantments</h2>"
                        + "      <p>These power-ups appear randomly and disappear if not collected quickly. Some apply immediately, others go into your bag for later use.</p>"
                        + "      <ul style='list-style-type: disc; margin-left: 40px;'>"
                        + "        <li>"
                        + "          <strong>Extra Time:</strong> "
                        +            imgOrFallback.apply(extraTimeIconPath)
                        + "          +5 seconds to your timer (applies immediately)."
                        + "        </li>"
                        + "        <li>"
                        + "          <strong>Reveal (R key):</strong> "
                        +            imgOrFallback.apply(revealIconPath)
                        + "          Highlights a 4x4 area containing the rune for 10 seconds."
                        + "        </li>"
                        + "        <li>"
                        + "          <strong>Cloak of Protection (P key):</strong> "
                        +            imgOrFallback.apply(cloakIconPath)
                        + "          Renders you invisible to Archer Monsters for 20 seconds."
                        + "        </li>"
                        + "        <li>"
                        + "          <strong>Luring Gem (B + direction):</strong> "
                        +            imgOrFallback.apply(gemIconPath)
                        + "          Throws a gem to lure Fighter Monsters away."
                        + "        </li>"
                        + "        <li>"
                        + "          <strong>Extra Life:</strong> "
                        +            imgOrFallback.apply(extraLifeIconPath)
                        + "          Instantly increases your life count by 1."
                        + "        </li>"
                        + "      </ul>"

                        + "      <h2 style='font-size: 22px; text-decoration: underline;'>Timer &amp; Lives</h2>"
                        + "      <p>You start each hall with a time limit based on the number of objects. "
                        + "         If time runs out or you lose all your lives, the game ends. "
                        + "         Collect enchantments to extend time or gain extra lives.</p>"

                        + "      <p style='text-align: center; margin-bottom: 0;'><strong>Good luck finding all the runes and escaping the dungeon!</strong></p>"
                        + "    </div>"
                        + "  </body>"
                        + "</html>";

        // 3. Create the JLabel with the HTML text
        JLabel helpLabel = new JLabel(helpText);
        helpLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        helpLabel.setForeground(Color.WHITE);
        helpLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Add the label to the panel
        add(helpLabel, "wrap, align center, gapbottom 20");

        // 4. Create and add the Back button
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