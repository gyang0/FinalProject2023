import java.awt.*;

/**
 * Individual bullets fired by the player's mining gun.
 * @author Gene Yang
 * @version May 24, 2023
 */
public class Bullet {
    // Position
    private int x;
    private int y;

    // The angle it was fired at, relative to the player.
    private double theta;

    private final int WIDTH = 5;
    private final int RADIUS = 5;
    private final int SPEED = 6;
    private final int EXPLOSION_RANGE = 3;
    private final int EXPLOSION_RADIUS_SQUARED = 6;

    /** Constructor **/
    public Bullet(int x, int y, double theta){
        this.x = x;
        this.y = y;
        this.theta = theta;
    }

    public void paint(Graphics g){
        g.setColor(Color.RED);
        g.fillRoundRect(this.x, this.y, this.WIDTH, this.WIDTH, this.RADIUS, this.RADIUS);
    }

    public void update(){
        this.x += this.SPEED * Math.cos(this.theta);
        this.y += this.SPEED * Math.sin(this.theta);
    }

    /** Getters **/
    public int getX(){
        return this.x;
    }
    public int getY(){
        return this.y;
    }

    /**
     * Checks if the bullet is out of range.
     * @param screenWidth The width of the screen
     * @param screenHeight The height of the screen
     * @return True if the bullet is out of range, false otherwise.
     */
    public boolean outOfRange(int screenWidth, int screenHeight){
        return this.x < 0 || this.x > screenWidth || this.y < 0 || this.y > screenHeight;
    }

    /**
     *
     * Literally, makes a "boom."
     * @param map The game map, a 2D array of Block objects
     * @param decor The decor map, a 2D array of decor Blocks.
     * @param curRow The row of the position the bullet hit.
     * @param curCol The column of the position the bullet hit.
     */
    public void makeBoom(Block[][] map, TransparentBlock[][] decor, int curRow, int curCol){
        // Width of the map blocks
        int blockWidth = map[0][0].w;

        for (int r = curRow - this.EXPLOSION_RANGE; r <= curRow + this.EXPLOSION_RANGE; r++) {
            for (int c = curCol - this.EXPLOSION_RANGE; c <= curCol + this.EXPLOSION_RANGE; c++) {
                if (r < 0 || r >= map.length || c < 0 || c >= map[0].length)
                    continue;

                if(map[r][c] instanceof LiquidBlock || map[r][c] instanceof ImmovableBlock)
                    continue;

                if (Math.pow(curRow - r, 2) + Math.pow(curCol - c, 2) < this.EXPLOSION_RADIUS_SQUARED) {
                    map[r][c] = new CaveBackground(r * blockWidth, c * blockWidth, blockWidth);
                    decor[r][c] = null;
                }
            }
        }
    }
}
