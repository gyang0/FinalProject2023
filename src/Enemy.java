import java.awt.*;

/**
 * An enemy block that chases the player if it enters a certain range
 * @author Gene Yang
 * @version May 24, 2023
 */
public class Enemy extends Block {
    // The enemy will chase the player if the player is within this much of it.
    private final int CHASE_DIST = 300;

    // Warning: enemy exhbits odd movements if speed is too low. It has to do with the Math.atan2() method.
    // My best guess is that the integer division result is being caught as 0 or infinity on the special cases.
    private double speed = 2;

    /** Constructor **/
    public Enemy(int x, int y, int w){
        super(x, y, w, "src/imgs/enemy.png");

        this.updateRate = 5;
    }

    /**
     * Draws the enemy graphics
     * @param g The Graphics Object
     * @param screenWidth The width of the screen
     * @param screenHeight The height of the screen
     * @param playerPos The array of the player's position, in [x, y]
     */
    @Override
    public void paint(Graphics g, int screenWidth, int screenHeight, int[] playerPos) {
        g.drawImage(img, this.x - playerPos[0] + screenWidth/2, this.y - playerPos[1] + screenHeight/2, this.w - 1, this.w - 1, null);

        // Darkness effect - black overlay
        g.setColor(new Color(0, 0, 0, calcOpacity(playerPos)));
        g.fillRect(this.x - playerPos[0] + screenWidth/2, this.y - playerPos[1] + screenHeight/2, this.w, this.w);
    }

    /**
     * Updates the angle from the player to the enemy and moves accordingly.
     * @param map The game map, a 2D array of Block objects
     * @param playerPos The position of the player, in [x, y]
     */
    @Override
    public void update(Block[][] map, int[] playerPos){
        // Chase a player within range.
        if(Math.pow(playerPos[0] - this.x, 2) + Math.pow(playerPos[1] - this.y, 2) < Math.pow(this.CHASE_DIST, 2)) {
            double theta = Math.atan2(playerPos[1] - this.y, playerPos[0] - this.x);

            // cos for x, sin for y, oh how trig works on the sly.
            this.x += (int) (Math.cos(theta) * this.speed);
            this.y += (int) (Math.sin(theta) * this.speed);
        }
    }
}