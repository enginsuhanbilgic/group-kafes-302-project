package tr.edu.ku.comp302.domain.controllers;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Tracks the last left-click.
 */
public class MouseHandler extends MouseAdapter {

    private Point lastClick = null;

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            lastClick = e.getPoint(); 
        }
    }

    /**
     * Returns the last left-click position, then resets it to null.
     */
    public synchronized Point getLastClickAndConsume() {
        Point temp = lastClick;
        lastClick = null;
        return temp;
    }
}
