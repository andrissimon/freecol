package net.sf.freecol.client.gui.panel;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Insets;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;

import javax.swing.UIManager;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.event.MouseInputListener;
import javax.swing.border.BevelBorder;

import net.sf.freecol.client.FreeColClient;

import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Tile;


/**
 * This component draws a small version of the map. It allows us
 * to see a larger part of the map and to relocate the viewport by
 * clicking on it.
 */
public final class MiniMap extends JPanel implements MouseInputListener {
    public static final String COPYRIGHT = "Copyright (C) 2003-2005 The FreeCol Team";
    public static final String LICENSE = "http://www.gnu.org/licenses/gpl.html";
    public static final String REVISION = "$Revision$";

    private static final Logger logger = Logger.getLogger(MiniMap.class.getName());
    public static final int MINIMAP_ZOOMOUT = 13;
    public static final int MINIMAP_ZOOMIN = 14;
    
    private static final int MAP_WIDTH = 256;
    private static final int MAP_HEIGHT = 128;

    private int mapX;
    private int mapY;

    private FreeColClient freeColClient;
    private final Map map;
    private final ImageProvider imageProvider;
    private JComponent container;
    private final JButton          miniMapZoomOutButton;
    private final JButton          miniMapZoomInButton;

    private int tileSize; //tileSize is the size (in pixels) that each tile will take up on the mini map

    /* The top left tile on the mini map represents the tile
    * (xOffset, yOffset) in the world map */
    private int xOffset, yOffset;




    /**
     * The constructor that will initialize this component.
     * @param map The map that is displayed on the screen.
     * @param imageProvider The ImageProvider that can provide us with images to display
     * and information about those images (such as the width of a tile image).
     * @param container The component that contains the minimap.
     */
    public MiniMap(FreeColClient freeColClient, Map map, ImageProvider imageProvider, JComponent container) {
        this.freeColClient = freeColClient;
        this.map = map;
        this.imageProvider = imageProvider;
        this.container = container;

        tileSize = 12;

        //setBackground(Color.BLACK);
        addMouseListener(this);
        addMouseMotionListener(this);
        setLayout(null);

        boolean usingSkin;
        Image skin = (Image) UIManager.get("MiniMap.skin");
        if (skin == null) {
            try {
                BevelBorder border = new BevelBorder(BevelBorder.RAISED);
                setBorder(border);
            } catch (Exception e) {}
            setSize(MAP_WIDTH, MAP_HEIGHT);
            setOpaque(true);
            usingSkin = false;
            mapX = 0;
            mapY = 0;
        } else {
            setBorder(null);
            setSize(skin.getWidth(null), skin.getHeight(null));
            setOpaque(false);
            usingSkin = true;
            // The values below should be specified by a skin-configuration-file:
            mapX = 38;
            mapY = 75;
        }

        // Add buttons:
        miniMapZoomOutButton = new JButton("-");
        miniMapZoomOutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                zoomOut();
            }
        });

        miniMapZoomInButton = new JButton("+");
        miniMapZoomInButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                zoomIn();
            }
        });

        miniMapZoomOutButton.setFocusable(false);
        miniMapZoomInButton.setFocusable(false);
        miniMapZoomOutButton.setOpaque(true);
        miniMapZoomInButton.setOpaque(true);
        miniMapZoomOutButton.setSize(50, 20);
        miniMapZoomInButton.setSize(50, 20);
        miniMapZoomOutButton.setActionCommand(String.valueOf(MINIMAP_ZOOMOUT));
        miniMapZoomInButton.setActionCommand(String.valueOf(MINIMAP_ZOOMIN));

        int bh = mapY + MAP_HEIGHT - Math.max(miniMapZoomOutButton.getHeight(), miniMapZoomInButton.getHeight());;
        int bw = mapX;
        if (getBorder() != null) {
            Insets insets = getBorder().getBorderInsets(this);
            bh -= insets.bottom;
            bw += insets.left;
        }

        miniMapZoomOutButton.setLocation(bw, bh);
        miniMapZoomInButton.setLocation(bw + miniMapZoomOutButton.getWidth(), bh);
        add(miniMapZoomInButton);
        add(miniMapZoomOutButton);
    }





    public void setContainer(JComponent container) {
        this.container = container;
    }


    /**
     * Zooms in the mini map
     */
    public void zoomIn() {
        tileSize += 4;
        miniMapZoomOutButton.setEnabled(true);
        repaint();
    }


    /**
     * Zooms out the mini map
     */
    public void zoomOut() {
        if (tileSize > 4) {
            tileSize -= 4;
        }

        if (tileSize <= 4) {
            miniMapZoomOutButton.setEnabled(false);
        }

        repaint();
    }


    /**
     * Paints this component.
     * @param graphics The Graphics context in which to draw this component.
     */
    public void paintComponent(Graphics graphics) {
        Image skin = (Image) UIManager.get("MiniMap.skin");
        if (skin == null) {
            paintMap(graphics, 0, 0, getWidth(), getHeight());
        } else {
            paintMap(graphics, mapX, mapY, MAP_WIDTH, MAP_HEIGHT);
            paintSkin(graphics, skin);
        }
    }


    private void paintSkin(Graphics graphics, Image skin) {
        graphics.drawImage(skin, 0, 0, null);
    }


    private void paintMap(Graphics graphics, int x, int y, int width, int height) {
        Graphics2D g = (Graphics2D) graphics;

        /* Fill the rectangle with solid black */
        g.setColor(Color.BLACK);
        g.fillRect(x, y, width, height);

        if (freeColClient.getGUI() == null || freeColClient.getGUI().getFocus() == null) {
            return;
        }

        /* xSize and ySize represent how many tiles can be represented on the
           mini map at the current zoom level */
        int xSize = width / tileSize;
        int ySize = (height / tileSize) * 4;

        /* If the mini map is too zoomed out, then zoom it in */
        while (xSize > map.getWidth() || ySize > map.getHeight()) {
            tileSize += 4;

            /* Update with the new values */
            xSize = width / tileSize;
            ySize = (height / tileSize) * 4;
        }

        /* Center the mini map correctly based on the map's focus */
        xOffset = freeColClient.getGUI().getFocus().getX() - (xSize / 2);
        yOffset = freeColClient.getGUI().getFocus().getY() - (ySize / 2);

        /* Make sure the mini map won't try to display tiles off the
         * bounds of the world map */
        if (xOffset < 0) {
            xOffset = 0;
        } else if (xOffset + xSize > map.getWidth()) {
            xOffset = map.getWidth() - xSize;
        }
        if (yOffset < 0) {
            yOffset = 0;
        } else if (yOffset + ySize > map.getHeight()) {
            yOffset = map.getHeight() - ySize;
        }

        /* Iterate through all the squares on the mini map and paint the
         * tiles based on terrain */

        /* Points that will be used to draw the diamond for each tile */
        int[] xPoints = new int[4] ;
        int[] yPoints = new int[4] ;

        int xPixels = 0;
        for (int tileX = 0; xPixels <= 256 + tileSize; tileX++, xPixels += tileSize) {
            int yPixels = 0;
            for (int tileY = 0; yPixels <= 128 + tileSize; tileY++, yPixels += tileSize / 4) {
                /* Check the terrain to find out which color to use */
                g.setColor(Color.BLACK); //Default

                Tile tile = map.getTileOrNull(tileX + xOffset, tileY + yOffset);
                Settlement settlement = tile != null ? tile.getSettlement() : null;
                int units = tile != null ? tile.getUnitCount() : 0;

                if (tile == null) {
                    g.setColor(Color.BLACK);
                } else if (tile.getAddition() == Tile.ADD_HILLS) {
                    g.setColor(new Color(0.44f, 0.50f, 0.32f)); // Grayish orange
                } else if (tile.getAddition() == Tile.ADD_MOUNTAINS) {
                    g.setColor(new Color(0.34f, 0.45f, 0.32f)); // Gray
                } else if (!tile.isForested()) {
                    int type = tile.getType();
                    switch (type) {
                        case Tile.PLAINS:
                        case Tile.GRASSLANDS:
                        case Tile.PRAIRIE:
                        case Tile.SAVANNAH:
                            g.setColor(new Color(0.14f, 0.50f, 0.12f)); // Green
                            break;

                        case Tile.MARSH:
                        case Tile.SWAMP:
                            g.setColor(new Color(0.14f, 0.50f, 0.24f)); // Bluish green
                            break;

                        case Tile.DESERT:
                            g.setColor(new Color(0.39f, 0.45f, 0.17f)); // Orangish
                            break;

                        case Tile.TUNDRA:
                            g.setColor(new Color(0.39f, 0.62f, 0.37f)); // Light blue
                            break;

                        case Tile.ARCTIC:
                            g.setColor(Color.WHITE);
                            break;

                        case Tile.OCEAN: //Blue ocean
                            g.setColor(Color.BLUE);
                            break;

                        case Tile.HIGH_SEAS: //Darker blue high seas
                            g.setColor(new Color(0.0f, 0.0f, 0.8f));
                            break;

                        case Tile.UNEXPLORED:
                        default:
                            g.setColor(Color.BLACK);
                            break;
                    }
                } else {
                  // Tile is forested, so display color of the forest
                  g.setColor(new Color(0.14f, 0.45f, 0.12f)); // Darker green
                }

                /* Due to the coordinate system, if the y value of the tile is odd,
                 * it needs to be shifted to the right a half-tile's width
                 */
                if (((tileY + yOffset) % 2) == 0) {
                    xPoints[0] = x + tileX * tileSize - tileSize / 2;
                    xPoints[1] = x + tileX * tileSize;
                    xPoints[2] = x + tileX * tileSize + tileSize / 2;
                    xPoints[3] = xPoints[1];
                } else {
                    xPoints[0] = x + tileX * tileSize;
                    xPoints[1] = x + tileX * tileSize + tileSize / 2;
                    xPoints[2] = x + tileX * tileSize + tileSize;
                    xPoints[3] = xPoints[1];
                }
                yPoints[0] = y + tileY * tileSize / 4;
                yPoints[1] = y + tileY * tileSize / 4 - tileSize / 4;
                yPoints[3] = y + tileY * tileSize / 4 + tileSize / 4;
                yPoints[2] = yPoints[0];

                //Draw it
                g.fillPolygon(xPoints, yPoints, 4);

                if (settlement != null) {
                    xPoints[0] += tileSize / 8;
                    xPoints[2] -= tileSize / 8;
                    yPoints[1] += tileSize / 16;
                    yPoints[3] -= tileSize / 16;
                    g.setColor(settlement.getOwner().getColor());
                    g.fillPolygon(xPoints, yPoints, 4);
                } else if (units > 0) {
                    xPoints[0] += tileSize / 4;
                    xPoints[2] -= tileSize / 4;
                    yPoints[1] += tileSize / 8;
                    yPoints[3] -= tileSize / 8;
                    g.setColor(tile.getFirstUnit().getOwner().getColor());
                    g.fillPolygon(xPoints, yPoints, 4);
                }

            }
        }

        /* Defines where to draw the white rectangle on the mini map.
         * miniRectX/Y are the center of the rectangle.
         * Use miniRectWidth/Height / 2 to get the upper left corner.
         * x/yTiles are the number of tiles that fit on the large map */
        int miniRectX = (freeColClient.getGUI().getFocus().getX() - xOffset) * tileSize;
        int miniRectY = (freeColClient.getGUI().getFocus().getY() - yOffset) * tileSize / 4;
        int miniRectWidth = (container.getWidth() / imageProvider.getTerrainImageWidth(0) + 1) * tileSize;
        int miniRectHeight = (container.getHeight() / imageProvider.getTerrainImageHeight(0) + 1) * tileSize / 2;
        if (miniRectX + miniRectWidth / 2 > width) {
            miniRectX = width - miniRectWidth / 2 - 1;
        } else if (miniRectX - miniRectWidth / 2 < 0) {
            miniRectX = miniRectWidth / 2;
        }
        if (miniRectY + miniRectHeight / 2 > height) {
            miniRectY = height - miniRectHeight / 2 - 1;
        } else if (miniRectY - miniRectHeight / 2 < 0) {
            miniRectY = miniRectHeight / 2;
        }

        miniRectX += x;
        miniRectY += y;

        g.setColor(Color.WHITE);
        g.drawRect(miniRectX - miniRectWidth / 2, miniRectY - miniRectHeight / 2, miniRectWidth, miniRectHeight);
    }


    public void mouseClicked(MouseEvent e) {

    }


    /* Used to keep track of the initial values of xOffset
     * and yOffset for more accurate dragging */
    private int initialX, initialY;

    /* If the user clicks on the mini map, refocus the map
     * to center on the tile that he clicked on */
    public void mousePressed(MouseEvent e) {
        if (!e.getComponent().isEnabled() || !isInMap(e.getX(), e.getY())) {
            return;
        }

        int x = e.getX() - mapX;
        int y = e.getY() - mapY;
        initialX = xOffset;
        initialY = yOffset;
        int tileX = (int) (x / tileSize) + xOffset;
        int tileY = (int) (y / tileSize * 4) + yOffset;
        freeColClient.getGUI().setFocus(tileX, tileY);
    }

    
    private boolean isInMap(int x, int y) {
        return x >= mapX && x < mapX+MAP_WIDTH && y >= mapY && y < mapY+MAP_HEIGHT;
    }


    public void mouseReleased(MouseEvent e) {

    }


    public void mouseEntered(MouseEvent e) {

    }


    public void mouseExited(MouseEvent e) {

    }


    public void mouseDragged(MouseEvent e) {
        /*
          If the user drags the mouse, then continue
          to refocus the screen
        */

        if (!e.getComponent().isEnabled() || !isInMap(e.getX(), e.getY())) {
            return;
        }

        int x = e.getX() - mapX;
        int y = e.getY() - mapY;
        int tileX = (int) (x / tileSize) + initialX;
        int tileY = (int) (y / tileSize * 4) + initialY;
        freeColClient.getGUI().setFocus(tileX, tileY);
    }


    public void mouseMoved(MouseEvent e) {

    }
}
