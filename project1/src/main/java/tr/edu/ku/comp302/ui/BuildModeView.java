package tr.edu.ku.comp302.ui;

import tr.edu.ku.comp302.domain.controllers.BuildModeController;
import tr.edu.ku.comp302.domain.controllers.NavigationController;
import tr.edu.ku.comp302.domain.controllers.TilesController;
import tr.edu.ku.comp302.domain.models.BuildObject;
import tr.edu.ku.comp302.domain.models.HallType;
import tr.edu.ku.comp302.config.GameConfig;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

/**
 * BuildModeView provides the UI for building the 4 halls.
 */
public class BuildModeView extends JPanel {

    private NavigationController controller;
    private BuildModeController buildModeController;
    private TilesController tilesController;

    // Bazı tile resimleri
    private BufferedImage floorImage;
    private BufferedImage wallTop, wallBottom, wallLeft, wallRight;

    // Seçili obje
    private String selectedObjectType = null;

    // Envanterdeki obje resimleri (key=objName)
    private Map<String, BufferedImage> objectImages;

    private final int gridRows = GameConfig.NUM_HALL_ROWS;
    private final int gridCols = GameConfig.NUM_HALL_COLS;
    private final int tileSize = GameConfig.TILE_SIZE;

    // UI panelleri
    private JPanel inventoryPanel;
    private JPanel canvasPanel;
    private JPanel controlPanel;

    public BuildModeView(NavigationController controller) {
        this.controller = controller;
        this.buildModeController = new BuildModeController();
        this.tilesController = new TilesController();
        this.tilesController.loadTiles();
        this.setBackground(new Color(66, 40, 53));

        setLayout(new BorderLayout());

        loadTileImages();
        loadObjectImages();

        canvasPanel = createCanvasPanel();
        canvasPanel.setBackground(new Color(66, 40, 53));
        inventoryPanel = createInventoryPanel();
        controlPanel = createControlPanel();

        add(canvasPanel, BorderLayout.CENTER);
        add(inventoryPanel, BorderLayout.EAST);
        add(controlPanel, BorderLayout.SOUTH);
    }

    private void loadTileImages() {
        try {
            floorImage = ImageIO.read(getClass().getResourceAsStream("/assets/floor_plain.png"));
            wallTop = ImageIO.read(getClass().getResourceAsStream("/assets/wall_center.png"));
            wallBottom = ImageIO.read(getClass().getResourceAsStream("/assets/wall_center.png"));
            wallLeft = ImageIO.read(getClass().getResourceAsStream("/assets/Wall_outer_w.png"));
            wallRight = ImageIO.read(getClass().getResourceAsStream("/assets/Wall_outer_e.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadObjectImages() {
        objectImages = new HashMap<>();
        try {
            BufferedImage boxImg = ImageIO.read(getClass().getResourceAsStream("/assets/box.png"));
            BufferedImage chestImg = ImageIO.read(getClass().getResourceAsStream("/assets/chest_closed.png"));
            BufferedImage columnImg = ImageIO.read(getClass().getResourceAsStream("/assets/column_wall.png"));
            BufferedImage skullImg = ImageIO.read(getClass().getResourceAsStream("/assets/skull.png"));

            objectImages.put("box", boxImg);
            objectImages.put("chest_closed", chestImg);
            objectImages.put("column_wall", columnImg);
            objectImages.put("skull", skullImg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JPanel createCanvasPanel() {
        JPanel panel = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(gridCols * tileSize, gridRows * tileSize);
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawHall(g);
            }
        };
        panel.setBackground(Color.BLACK);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = e.getX() / tileSize;
                int y = e.getY() / tileSize;

                // Sadece kafes içine yerleştirme kontrolü
                if (x -1 < GameConfig.KAFES_STARTING_X || x >= GameConfig.KAFES_STARTING_X + gridCols - 1
                        || y -1< GameConfig.KAFES_STARTING_Y || y >= GameConfig.KAFES_STARTING_Y + gridRows -1) {
                    JOptionPane.showMessageDialog(panel, "You can only place objects inside the grid!", "Invalid Placement", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (selectedObjectType != null) {
                    buildModeController.placeObject(x, y, selectedObjectType);
                    repaint();
                }
            }
        });
        return panel;
    }

    private JPanel createInventoryPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(200, 0));

        panel.add(new JLabel("Inventory:"));
        panel.add(Box.createVerticalStrut(10));

        for (String objName : objectImages.keySet()) {
            BufferedImage img = objectImages.get(objName);
            JButton btn = new JButton(new ImageIcon(img));
            btn.setText(objName);
            btn.setHorizontalTextPosition(SwingConstants.CENTER);
            btn.setVerticalTextPosition(SwingConstants.BOTTOM);

            btn.addActionListener(e -> {
                selectedObjectType = objName;
            });

            panel.add(btn);
            panel.add(Box.createVerticalStrut(10));
        }

        JButton noneBtn = new JButton("De-select");
        noneBtn.addActionListener(e -> selectedObjectType = null);
        panel.add(noneBtn);

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JButton nextHallBtn = new JButton("Next Hall");
        nextHallBtn.addActionListener(e -> onNextHall());
        panel.add(nextHallBtn);

        JButton completeBtn = new JButton("Complete & Start Game");
        completeBtn.addActionListener(e -> onCompleteAndStartGame());
        panel.add(completeBtn);

        JButton backMenuBtn = new JButton("Back to Main Menu");
        backMenuBtn.addActionListener(e -> controller.showMainMenu());
        panel.add(backMenuBtn);

        return panel;
    }

    private void drawHall(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        tilesController.draw(g2);

        HallType currentHall = buildModeController.getCurrentHall();
        for (BuildObject obj : getObjectsForHall(currentHall)) {
            BufferedImage img = objectImages.get(obj.getObjectType());
            if (img != null) {
                g.drawImage(img, obj.getX() * tileSize, obj.getY() * tileSize, tileSize, tileSize, null);
            } else {
                g.setColor(Color.RED);
                g.fillRect(obj.getX() * tileSize, obj.getY() * tileSize, tileSize, tileSize);
            }
        }
    }

    // reflection hack yerine BuildModeController'a getter eklemeyi seçebilirsiniz.
    private List<BuildObject> getObjectsForHall(HallType hallType) {
        try {
            java.lang.reflect.Field f = buildModeController.getClass()
                    .getDeclaredField("hallObjectsMap");
            f.setAccessible(true);
            Map<HallType, List<BuildObject>> map =
                    (Map<HallType, List<BuildObject>>) f.get(buildModeController);
            return map.get(hallType);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ArrayList<>();
    }

    private void onNextHall() {
        if (!buildModeController.isCurrentHallValid()) {
            JOptionPane.showMessageDialog(this,
                    "You must place enough objects in this hall before proceeding!",
                    "Not Enough Objects",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        boolean moved = buildModeController.goToNextHall();
        if (!moved) {
            JOptionPane.showMessageDialog(this, "All halls completed. You can now start the game.");
        } else {
            JOptionPane.showMessageDialog(this, "Moved to next hall: " + buildModeController.getCurrentHall());
        }
        repaint();
    }

    /**
     * Son hall da dahil tüm hall'ları tamamladıktan sonra,
     * JSON kaydet ve doğrudan Play Mode'a geç.
     */
    private void onCompleteAndStartGame() {
        // Son hall da min objeler konuldu mu kontrol edelim
       // boolean moved = buildModeController.goToNextHall();
        //üif (moved) {
        if (!buildModeController.isCurrentHallValid()) {
            JOptionPane.showMessageDialog(this,
                    "You must place enough objects in the last hall before starting the game!",
                    "Not Enough Objects",
                    JOptionPane.WARNING_MESSAGE);
            return;
            
        }

        // Tüm hall'ların verisini JSON'a çevirelim
        String json = buildModeController.exportToJson();

        // Dosyaya kaydetme örneği ("world1.json" ismiyle)
        String fileName = "world1.json";
        try (PrintWriter out = new PrintWriter(fileName, StandardCharsets.UTF_8)) {
            out.println(json);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving JSON to file!");
            return;
        }


        String jsonData = "";
        try {
            jsonData = new String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Ardından NavigationController ile direkt PlayMode'a geçiyoruz
        controller.startNewPlayModeFromJson(jsonData);

    }
}
