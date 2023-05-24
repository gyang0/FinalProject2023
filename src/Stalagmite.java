import java.awt.*;

/**
 * A decoration block designed to resemble a stalagmite (I'm pretty sure that's the thing that grows from the ground).
 * @author Gene Yang
 * @version May 24, 2023
 */
public class Stalagmite extends TransparentBlock {
    public Stalagmite(int x, int y, int w){
        super(x, y, w, "src/imgs/stalagmite.png");
    }

    @Override
    public void paint(Graphics g, int screenWidth, int screenHeight, int[] playerPos) {
        // No black background for transparent images

        // Making it slightly smaller than BLOCK_WIDTH to avoid glitchiness
        g.drawImage(img, this.x - playerPos[0] + screenWidth/2, this.y - playerPos[1] + screenHeight/2, this.w - 1, this.w - 1, null);

        // Darkness effect - black overlay
        g.setColor(new Color(0, 0, 0, calcOpacity(playerPos)));
        g.fillRect(this.x - playerPos[0] + screenWidth/2, this.y - playerPos[1] + screenHeight/2, this.w, this.w);
    }
}
