package ui;

import domain.controllers.BuildModeController;
import domain.controllers.KeyHandler;
import domain.controllers.TilesController;
import domain.models.Player;

import javax.swing.*;


import java.awt.*;


public class BuildModeView extends BaseView implements Runnable {
    private BuildModeController controller;
    private JPanel mainPanel;
    private GamePanel gamePanel; // Custom panel for drawing
    TilesController tilesController = new TilesController(gamePanel);

    KeyHandler keyH = new KeyHandler();
    Player player;
    Thread gameThread;


    public BuildModeView() {
        super("Build Mode");
        mainPanel = new JPanel(new BorderLayout());
        

        gamePanel = new GamePanel();
        mainPanel.add(gamePanel, BorderLayout.CENTER);
        setViewContent(mainPanel);

        controller = new BuildModeController(10, 10, 6); // Example criteria

        

        player = new Player(gamePanel, keyH);
        gamePanel.addKeyListener(keyH);
        gamePanel.setFocusable(true);
        gamePanel.requestFocusInWindow();


        
    }


    @Override
    public Container getContentPane() {
        return mainPanel;
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime(); // Önceki zaman
        double drawInterval = 1000000000.0 / 60.0; // 60 FPS için her frame süresi (nanoseconds)
        double delta = 0;
        long currentTime;
        long timer =0;
        int drawCount = 0;

    
        while (gameThread != null) {
            currentTime = System.nanoTime();

            delta += (currentTime - lastTime) / drawInterval; // Delta hesabı
            timer += (currentTime - lastTime);
            lastTime = currentTime;
           
    
            if (delta >= 1) {
                gamePanel.update();       // Oyun mantığını güncelle
                gamePanel.repaint(); // Oyunu yeniden çiz
                delta--;
                drawCount++;
            }
            
            if(timer >= 1000000000) {
                System.out.println("FPS:"+drawCount);
                drawCount =0;
                timer = 0;

            }
    
           
        }
    }
    

    

    public class GamePanel extends JPanel {
        

        public void update() {
            player.update();
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g; 

            tilesController.draw(g2);
            player.draw(g2);

            g2.dispose();
        }}

}

