package tr.edu.ku.comp302.domain.controllers;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {
    public boolean up, down, left, right;
    public boolean b, p, r;
    public boolean escPressed = false;
    public boolean hPressed;

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
        if (move == KeyEvent.VK_P) {
            p = true;
        }
        if (move == KeyEvent.VK_B) {
            b = true;
        }
        if (move == KeyEvent.VK_R) {
            r = true;
        }
        if (move == KeyEvent.VK_ESCAPE) { // Avoid repeated toggling
            escPressed = !escPressed;
            //System.out.println(escPressed);
        }
        if (move == KeyEvent.VK_H) {
            hPressed = true;
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
        if (move == KeyEvent.VK_H) {
            hPressed = false;
        }
    }

    public boolean isEscPressed(){
        return escPressed;
    }
    public boolean isRPressed(){
        return r;
    }
    public boolean isBPressed(){
        return b;
    }
    public boolean isPPressed(){
        return p;
    }


    public void resetKeys() {
        up = down = left = right = false;
    }


}
