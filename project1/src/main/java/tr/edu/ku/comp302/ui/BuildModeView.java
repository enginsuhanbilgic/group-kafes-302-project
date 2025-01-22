package tr.edu.ku.comp302.ui;

import tr.edu.ku.comp302.domain.controllers.BuildModeController;
import tr.edu.ku.comp302.domain.controllers.NavigationController;
import tr.edu.ku.comp302.domain.controllers.ResourceManager;
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

    /*// Bazı tile resimleri
    private BufferedImage floorImage;
    private BufferedImage wallTop, wallBottom, wallLeft, wallRight;*/

    // Seçili obje
    private String selectedObjectType = null;
    private boolean isRemoveMode = false;

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
        this.tilesController.loadTiles(HallType.DEFAULT);
        this.setBackground(new Color(66, 40, 53));

        setLayout(new BorderLayout());

        //loadTileImages();
        loadObjectImages();

        canvasPanel = createCanvasPanel();
        canvasPanel.setBackground(new Color(66, 40, 53));
        inventoryPanel = createInventoryPanel();
        controlPanel = createControlPanel();

        add(canvasPanel, BorderLayout.CENTER);
        add(inventoryPanel, BorderLayout.EAST);
        add(controlPanel, BorderLayout.SOUTH);
    }

    /*private void loadTileImages() {
        try {
            floorImage = ImageIO.read(getClass().getResourceAsStream("/assets/floor_plain.png"));
            wallTop = ImageIO.read(getClass().getResourceAsStream("/assets/wall_center.png"));
            wallBottom = ImageIO.read(getClass().getResourceAsStream("/assets/wall_center.png"));
            wallLeft = ImageIO.read(getClass().getResourceAsStream("/assets/Wall_outer_w.png"));
            wallRight = ImageIO.read(getClass().getResourceAsStream("/assets/Wall_outer_e.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    private void loadObjectImages() {
        objectImages = new HashMap<>();
        try {
            BufferedImage boxImg = ImageIO.read(getClass().getResourceAsStream("/assets/box.png"));
            BufferedImage chestImg = ImageIO.read(getClass().getResourceAsStream("/assets/chest_closed.png"));
            BufferedImage columnImg = ImageIO.read(getClass().getResourceAsStream("/assets/column_wall.png"));
            BufferedImage skullImg = ImageIO.read(getClass().getResourceAsStream("/assets/skull.png"));
            BufferedImage boxesStackedImg = ImageIO.read(getClass().getResourceAsStream("/assets/boxes_stacked.png"));

            objectImages.put("box", boxImg);
            objectImages.put("chest_closed", chestImg);
            objectImages.put("column_wall", columnImg);
            objectImages.put("skull", skullImg);
            objectImages.put("boxes_stacked", boxesStackedImg);
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
                if (x - 1 < GameConfig.KAFES_STARTING_X || x >= GameConfig.KAFES_STARTING_X + gridCols - 1
                        || y - 2< GameConfig.KAFES_STARTING_Y || y >= GameConfig.KAFES_STARTING_Y + gridRows -2) {
                    JOptionPane.showMessageDialog(panel, "You can only interact with objects inside the grid!", "Invalid Action", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (isRemoveMode) {
                    removeObjectAt(x, y);
                }

                if (selectedObjectType != null){
                    List<BuildObject> objects = buildModeController.getObjectsForHall(buildModeController.getCurrentHall());
                    boolean alreadyExists = false;
                    for (BuildObject obj : objects) {
                        if (obj.getX() == x && obj.getY() == y) {
                            alreadyExists = true;
                            break;
                        }
                    }

                    if (alreadyExists) {
                        JOptionPane.showMessageDialog(panel,
                                "There is already an object at this location!",
                                "Invalid Placement",
                                JOptionPane.WARNING_MESSAGE);
                    } else {
                        // 3) Otherwise, place the new object
                        buildModeController.placeObject(x, y, selectedObjectType);
                        repaint();
                    }
                }

                repaint();
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
                isRemoveMode = false;
            });

            panel.add(btn);
            panel.add(Box.createVerticalStrut(10));
        }

        JButton removeBtn = new JButton("Remove Object");
        removeBtn.addActionListener(e -> {
            isRemoveMode = true;
            selectedObjectType = null;
        });
        panel.add(removeBtn);
        panel.add(Box.createVerticalStrut(10));

        JButton noneBtn = new JButton("De-select");
        noneBtn.addActionListener(e -> {
            selectedObjectType = null;
            isRemoveMode = false;
        });
        panel.add(noneBtn);

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JButton prevHallBtn = new JButton("Previous Hall");
        prevHallBtn.addActionListener(e -> onPreviousHall());
        panel.add(prevHallBtn);

        JButton nextHallBtn = new JButton("Next Hall");
        nextHallBtn.addActionListener(e -> onNextHall());
        panel.add(nextHallBtn);

        JButton saveBtn = new JButton("Save Design");
        saveBtn.addActionListener(e -> onSaveDesign());
        panel.add(saveBtn);

        JButton loadBtn = new JButton("Load Design");
        loadBtn.addActionListener(e -> onLoadDesign());
        panel.add(loadBtn);

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
        for (BuildObject obj : buildModeController.getObjectsForHall(currentHall)) {
            BufferedImage img = objectImages.get(obj.getObjectType());
            String imageName = obj.getObjectType();
            int px = obj.getX() * tileSize;
            int py = obj.getY() * tileSize;
            if (img != null) {
                if(imageName.trim().equals("column_wall") || imageName.trim().equals("boxes_stacked")){
                    g2.drawImage(img, px, py - tileSize/2, tileSize, tileSize + tileSize/2, null);
                }
                else{
                    g2.drawImage(img, px, py, tileSize, tileSize, null);
                }
            } else {
                g.setColor(Color.RED);
                g.fillRect(obj.getX() * tileSize, obj.getY() * tileSize, tileSize, tileSize);
            }
        }

        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.setColor(Color.WHITE);
        g2.drawString(currentHall.toText(), 300, 20);
    }

    private void onPreviousHall() {
        boolean moved = buildModeController.goToPreviousHall();
        if (!moved) {
            JOptionPane.showMessageDialog(this, "This is the first hall, you cannot go back further.");
        } else {
            JOptionPane.showMessageDialog(this, "Moved to previous hall: " + buildModeController.getCurrentHall());
        }
        selectedObjectType = null;
        repaint();
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
        selectedObjectType = null;
        repaint();
    }

    private void onSaveDesign() {
        String designName = JOptionPane.showInputDialog(this, 
            "Enter a name for your design:", 
            "Save Design", 
            JOptionPane.PLAIN_MESSAGE);

        if (designName != null && !designName.trim().isEmpty()) {
            try {
                buildModeController.saveDesign(designName.trim());
                JOptionPane.showMessageDialog(this, 
                    "Design saved successfully!", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error saving design: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onLoadDesign() {
        List<String> savedDesigns = buildModeController.getSavedDesigns();
        
        if (savedDesigns.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No saved designs found.", 
                "Information", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] designs = savedDesigns.toArray(new String[0]);
        String selectedDesign = (String) JOptionPane.showInputDialog(this,
            "Select a design to load:",
            "Load Design",
            JOptionPane.QUESTION_MESSAGE,
            null,
            designs,
            designs[0]);

        if (selectedDesign != null) {
            try {
                buildModeController.loadDesign(selectedDesign);
                selectedObjectType = null;
                JOptionPane.showMessageDialog(this, 
                    "Design loaded successfully!", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                repaint();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error loading design: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
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
        controller.resetPlayer();
        controller.startNewPlayModeFromJson(jsonData, HallType.EARTH);

    }

    private void removeObjectAt(int x, int y) {
        HallType currentHall = buildModeController.getCurrentHall();
        List<BuildObject> objects = buildModeController.getObjectsForHall(currentHall);
        objects.removeIf(obj -> obj.getX() == x && obj.getY() == y);
    }
}