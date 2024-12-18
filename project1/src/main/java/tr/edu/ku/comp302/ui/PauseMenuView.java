package tr.edu.ku.comp302.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class PauseMenuView extends JDialog {

    public PauseMenuView(JFrame parentFrame, ActionListener onResume, ActionListener onHelp, ActionListener onReturn) {
        super(parentFrame, "Pause Menu", true); // Modal dialog
        this.setSize(300, 200);
        this.setLayout(new GridLayout(4, 1));
        this.setLocationRelativeTo(parentFrame);

        JLabel titleLabel = new JLabel("Paused", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        this.add(titleLabel);

        JButton resumeButton = new JButton("Resume");
        resumeButton.addActionListener(e -> {
            onResume.actionPerformed(e);
            this.dispose(); // Close dialog
        });
        this.add(resumeButton);

        JButton helpButton = new JButton("Help Menu");
        helpButton.addActionListener(e -> {
            onHelp.actionPerformed(e);
            this.dispose(); // Close dialog
        });
        this.add(helpButton);

        JButton returnButton = new JButton("Return to Main Menu");
        returnButton.addActionListener(e -> {
            onReturn.actionPerformed(e);
            this.dispose(); // Close dialog
        });
        this.add(returnButton);

        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
}
