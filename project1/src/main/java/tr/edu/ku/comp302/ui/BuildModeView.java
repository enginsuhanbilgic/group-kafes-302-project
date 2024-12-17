package tr.edu.ku.comp302.ui;

import tr.edu.ku.comp302.domain.controllers.NavigationController;
import javax.swing.*;

/*TO DO
 * Placeholder BuildModeView. 
 */
public class BuildModeView extends JPanel {
    public BuildModeView(NavigationController controller) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JLabel label = new JLabel("Build Mode", SwingConstants.CENTER);
        label.setAlignmentX(CENTER_ALIGNMENT);
        
        JButton backButton = new JButton("Back to Main Menu");
        backButton.setAlignmentX(CENTER_ALIGNMENT);
        backButton.addActionListener(e -> controller.showMainMenu());
        
        add(Box.createVerticalGlue());
        add(label);
        add(Box.createVerticalStrut(20));
        add(backButton);
        add(Box.createVerticalGlue());
    }
}
