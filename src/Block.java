import javax.swing.*;
import java.awt.*;

/**
 * The base class of all Blocks.
 * Different subclasses are created depending on a specific trait, but they all use the methods here.
 *
 * @author Gene Yang
 * @version May 24, 2023
 */
public abstract class Block {
    // Block images
    public ImageIcon imgIcon;
    public Image img;

    // For the basic shape
    public int x;
    public int y;
    public int w;

    // Number of ticks so far and the rate at which to update the block
    public int numTicks;
    public int updateRate;

    /**
     * Constructor
     */
    public Block(int x, int y, int w, String imgSrc){
        this.x = x;
        this.y = y;
        this.w = w;

        this.numTicks = 0;
        this.updateRate = 0;

        // Images
        imgIcon = new ImageIcon(imgSrc);
        img = imgIcon.getImage();
    }

    /**
     * Calculates the opacity for the darkness overlay
     * @param playerPos The player's coordinates in [x, y]
     * @return The opacity value for the darkness overlay, to be used in RGB colors.
     */
    public int calcOpacity(int[] playerPos){
        return Math.min(250, (int)(( Math.abs(playerPos[0] - this.x) + Math.abs(playerPos[1] - this.y) )*0.6) );
    }

    /**
     * Paints the block
     * @param g The Graphics Object
     * @param screenWidth The width of the screen
     * @param screenHeight The height of the screen
     * @param playerPos The array of the player's position, in [x, y]
     */
    public void paint(Graphics g, int screenWidth, int screenHeight, int[] playerPos) {
        // Black outline
        g.setColor(Color.BLACK);
        g.fillRect(this.x - playerPos[0] + screenWidth / 2, this.y - playerPos[1] + screenHeight / 2, this.w, this.w);

        // Making it slightly smaller than BLOCK_WIDTH to avoid glitchiness
        g.drawImage(img, this.x - playerPos[0] + screenWidth / 2, this.y - playerPos[1] + screenHeight / 2, this.w - 1, this.w - 1, null);

        // Darkness effect - black overlay
        g.setColor(new Color(0, 0, 0, calcOpacity(playerPos)));
        g.fillRect(this.x - playerPos[0] + screenWidth / 2, this.y - playerPos[1] + screenHeight / 2, this.w, this.w);
    }

    // Used to update the block's position and set the darkness overlay.
    public void update(Block[][] map, int[] playerPos){}
}
