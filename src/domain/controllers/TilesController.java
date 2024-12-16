package domain.controllers;

import java.awt.Graphics2D;
import java.io.IOException;

import javax.imageio.ImageIO;

import domain.models.Tiles;
import ui.BuildModeView.GamePanel;

public class TilesController {

    GamePanel gp;
    domain.models.Tiles[] tile;

    public TilesController(GamePanel gp) {
        this.gp = gp;
        tile = new Tiles[20];
        getTilesImage();

    }

    public void getTilesImage() {
        try {
            tile[0] = new Tiles();
            tile[0].image = ImageIO.read(getClass().getResourceAsStream("/assets/floor_plain.png"));
            tile[1] = new Tiles();
            tile[1].image = ImageIO.read(getClass().getResourceAsStream("/assets/Wall_outer_e.png"));
            tile[2] = new Tiles();
            tile[2].image = ImageIO.read(getClass().getResourceAsStream("/assets/Wall_outer_w.png"));
            tile[3] = new Tiles();
            tile[3].image = ImageIO.read(getClass().getResourceAsStream("/assets/wall_top_center.png"));



 ;
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

    }

    public void draw(Graphics2D g2) {
        g2.drawImage(tile[0].image,  48,  48, 16, 16, null);

        for (int y = 0; y < 1; y++) {
            for (int x = 0; x < 1; x++) {
                g2.drawImage(tile[0].image, x * 48, y * 48, 48, 48, null);
            }
        }

        for (int x = 0; x < 15; x++) {
            g2.drawImage(tile[1].image, 100, x*16 + 60, 16, 16, null);
        }

        for (int x = 0; x < 21; x++) {
            g2.drawImage(tile[3].image, 103  + x*16, 44 , 16, 16, null);
        }


        for (int x = 0; x < 15; x++) {
            g2.drawImage(tile[1].image, 100, 392 + x*16 , 16, 16, null);
        }

        for (int x = 0; x < 15; x++) {
            g2.drawImage(tile[2].image, 436, 60 + x*16 , 16, 16, null);
        }
        for (int x = 0; x < 15; x++) {
            g2.drawImage(tile[2].image, 436, 392 + x*16 , 16, 16, null);
        }


        

    }




}
