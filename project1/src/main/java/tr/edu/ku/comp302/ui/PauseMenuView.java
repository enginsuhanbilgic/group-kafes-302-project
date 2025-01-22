package tr.edu.ku.comp302.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class PauseMenuView extends JDialog {

    public PauseMenuView(JFrame parentFrame, ActionListener onResume, ActionListener onSave, ActionListener onHelp, ActionListener onReturn) {
        super(parentFrame, "Pause Menu", true); // Modal dialog
        this.setSize(400, 200);
        this.setLayout(new GridLayout(5, 1));
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

        JButton saveButton = new JButton("Save Game and Exit");
        saveButton.addActionListener(e -> {
            onSave.actionPerformed(e);
            this.dispose();
        });
        this.add(saveButton);

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

        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e){
                ActionEvent actionEvent = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Window Closed");
                onResume.actionPerformed(actionEvent);
                PauseMenuView.this.dispose();
            }
        });
    }
}