package tr.edu.ku.comp302.domain.controllers;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {
    public boolean up, down, left, right;

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used but must be implemented
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int move = e.getKeyCode();

        if (move == KeyEvent.VK_UP) {
            up = true;
        }
        if (move == KeyEvent.VK_DOWN) {
            down = true;
        }
        if (move == KeyEvent.VK_LEFT) {
            left = true;
        }
        if (move == KeyEvent.VK_RIGHT) {
            right = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int move = e.getKeyCode();

        if (move == KeyEvent.VK_UP) {
            up = false;
        }
        if (move == KeyEvent.VK_DOWN) {
            down = false;
        }
        if (move == KeyEvent.VK_LEFT) {
            left = false;
        }
        if (move == KeyEvent.VK_RIGHT) {
            right = false;
        }
    }
}
