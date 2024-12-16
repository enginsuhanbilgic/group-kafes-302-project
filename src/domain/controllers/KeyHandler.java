package domain.controllers;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {

    public boolean up, down, left, right;

    public void keyTyped(KeyEvent e) {
        
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int move = e.getKeyCode();
        
            if (move == KeyEvent.VK_W) {
                up = true;
            }
            if (move == KeyEvent.VK_S) {
                down = true;
            }
            if (move == KeyEvent.VK_A) {
                left = true;
            }
            if (move == KeyEvent.VK_D) {
                right = true; }}
    

    @Override
    public void keyReleased(KeyEvent e) {
        int move = e.getKeyCode();
        
        if (move == KeyEvent.VK_W) {
            up = false;
        }
        if (move == KeyEvent.VK_S) {
            down = false;
        }
        if (move == KeyEvent.VK_A) {
            left = false;
        }
        if (move == KeyEvent.VK_D) {
            right = false; }}
    }