package tr.edu.ku.comp302.ui;

import javax.swing.*;
import java.awt.*;

public class TimeView extends JPanel {
    private JLabel timeLabel;
    private JFrame parentFrame; 

    public TimeView(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new FlowLayout(FlowLayout.LEFT));
        setBackground(new Color(66, 40, 53));

        timeLabel = new JLabel("Kalan Süre: ");
        timeLabel.setForeground(Color.WHITE);
        this.add(timeLabel);
    }

    public void setTime(int time) {
        timeLabel.setText("Kalan Süre: " + time);
        
        if (parentFrame != null) {
            parentFrame.revalidate();
            parentFrame.repaint();
        }
    }
}
