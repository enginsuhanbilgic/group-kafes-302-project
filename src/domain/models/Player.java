package domain.models;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import domain.controllers.KeyHandler;
import javax.imageio.ImageIO;

import ui.BuildModeView.GamePanel;

public class Player {
    public int x = 100 ,y = 100 ;
    public int speed = 4;
    GamePanel gp;
    KeyHandler keyH = new KeyHandler();
    BufferedImage playerImage = loadImage("/assets/player.png");
    

    public Player(GamePanel gp, KeyHandler keyH ){
        this.gp = gp;
        this.keyH = keyH;
        
    }

    public BufferedImage loadImage(String path) {
    BufferedImage image = null;
    try {
        image = ImageIO.read(getClass().getResourceAsStream(path));
    } catch (IOException e) {
        System.err.println("Error loading image from path: " + path);
        e.printStackTrace();
    }
    return image;
}
    

    public void update() {

        if (keyH.up == true) {
            System.out.println("SADSA");
            y -= speed;
        } 
        else if (keyH.down == true) {
            y += speed;
        } 
        else if (keyH.left == true) {
            x -= speed;
        } 
        else if (keyH.right == true) {
            x += speed;
        }

    }

    public void draw(Graphics2D g2) {
        g2.setColor(Color.WHITE);

     //   g2.fillRect(x, y, 48, 48);
        g2.drawImage(playerImage, x,y,48,48,null);

    }


   
    }
    

