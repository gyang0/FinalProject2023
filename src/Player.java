import java.awt.*;
import java.util.ArrayList;

/**
 * Player class, for the player obviously, the blue guy that jumps around on the screen.
 * The Player is a type of Block.
 *
 * @author Gene Yang
 * @version May 24, 2023
 */
public class Player extends Block {
    // Collision detection range of the player (in blocks)
    private final int COLLISION_RANGE = 3;

    // For movements
    private boolean moveUp;
    private boolean moveRight;
    private boolean moveLeft;
    private boolean inLiquid;

    // Health
    private double health;

    // Jumping mechanism
    private int speed = 3;
    private double verticalVel = 0.0; // Vertical velocity (for jumping)
    private double verticalAccel = 0.5; // Vertical acceleration (for jumping)
    private int jumpAmt = 2;
    private boolean canJump = true;

    // Reload for mining gun
    private int reload = 0;

    // Spawn coordinates
    private int spawnX;
    private int spawnY;


    /**
     * Initializes player image and health
     * @param x The x-coordinate of the player
     * @param y The y-coordinate of the player
     * @param w The width of the player
     */
    public Player(int x, int y, int w){
        super(x, y, w, "src/imgs/player.png");

        // Set spawn point
        this.spawnX = x;
        this.spawnY = y;

        this.health = 100.0;
        this.inLiquid = false;
    }

    /** Getters **/
    public int getX(){ return this.x; }
    public int getY(){ return this.y; }
    public int getReload(){ return this.reload; }

    /** Setters **/
    public void setX(int x){this.x = x; }
    public void setY(int y){ this.y = y; }
    public void setReload(int reload){ this.reload = reload; }
    public void setMoveUp(boolean toggle){ moveUp = toggle; }
    public void setMoveRight(boolean toggle){ moveRight = toggle; }
    public void setMoveLeft(boolean toggle){ moveLeft = toggle; }

    /**
     * Check for collisions
     * Uses AABB (Axis-Aligned Bounding Box) collisions.
     * @param hitbox The hitbox that we're currently checking
     * @return True if the player collided with this block, false otherwise.
     */
    public boolean collided(Block hitbox){
        return this.x + this.w > hitbox.x &&
               this.x < hitbox.x + hitbox.w &&
               this.y + this.w > hitbox.y &&
               this.y < hitbox.y + hitbox.w;
    }

    /**
     * Handles the player's x-collisions and sets the appropriate x-coordinate.
     * @param hitboxes The 2D map of blocks
     */
    public void xCollide(Block[][] hitboxes){
        // Width of the map blocks
        int blockWidth = hitboxes[0][0].w;

        for(int i = this.x/blockWidth - 3; i <= this.x/blockWidth + 3; i++){
            for(int j = this.y/blockWidth - 3; j <= this.y/blockWidth + 3; j++){
                if(i < 0 || i >= hitboxes.length || j < 0 || j >= hitboxes[0].length)
                    continue;

                if(collided(hitboxes[i][j]) && hitboxes[i][j] instanceof SolidBlock){
                    // Either the player is touching the left side of the block
                    if (this.x < hitboxes[i][j].x) {
                        this.x = hitboxes[i][j].x - this.w;
                    }

                    // Or the player is touching the right side.
                    else {
                        this.x = hitboxes[i][j].x + hitboxes[i][j].w;
                    }
                }
            }
        }
    }

    /**
     * Handles the player's y-collisions and sets the appropriate y-coordinate.
     * @param hitboxes The 2D map of blocks
     */
    public void yCollide(Block[][] hitboxes){
        // Width of the map blocks
        int blockWidth = hitboxes[0][0].w;

        // Set the jumping option to false for now
        this.canJump = false;

        for(int i = this.x/blockWidth - COLLISION_RANGE; i <= this.x/blockWidth + COLLISION_RANGE; i++){
            for(int j = this.y/blockWidth - COLLISION_RANGE; j <= this.y/blockWidth + COLLISION_RANGE; j++){
                if(i < 0 || i >= hitboxes.length || j < 0 || j >= hitboxes[0].length)
                    continue;

                if(collided(hitboxes[i][j]) && hitboxes[i][j] instanceof SolidBlock){
                    this.verticalVel = 0;

                    // Either the player is touching the upper side of the block
                    if(this.y < hitboxes[i][j].y){
                        this.y = hitboxes[i][j].y - this.w;

                        // Can jump from the top side of a block.
                        this.canJump = true;
                    }

                    // Or the player is touching the bottom side.
                    else {
                        this.y = hitboxes[i][j].y + hitboxes[i][j].w;
                    }
                }
            }
        }
    }

    /**
     * Preliminary collisions to check if the player is in liquid (and particularly, if the player is in Acid)
     * @param hitboxes The 2D array of map blocks
     */
    public void checkCollisions(Block[][] hitboxes){
        // Width of the map blocks
        int blockWidth = hitboxes[0][0].w;

        for(int i = this.x/blockWidth - COLLISION_RANGE; i <= this.x/blockWidth + COLLISION_RANGE; i++){
            for(int j = this.y/blockWidth - COLLISION_RANGE; j <= this.y/blockWidth + COLLISION_RANGE; j++){
                if(i < 0 || i >= hitboxes.length || j < 0 || j >= hitboxes[0].length)
                    continue;

                if(collided(hitboxes[i][j])){
                    if(hitboxes[i][j] instanceof LiquidBlock)
                        this.inLiquid = true;

                    if(hitboxes[i][j] instanceof Acid)
                        this.health -= 0.2;
                }
            }
        }
    }

    /**
     * Checks if the player collided with enemies.
     * @param enemies The list of enemies present
     */
    public void checkEnemyCollisions(ArrayList<Enemy> enemies){
        for(Enemy e : enemies){
            if(collided(e)){
                this.health -= 0.2;
            }
        }
    }

    /**
     * Resets player coordinates to spawn after death.
     */
    public void handleDeaths(){
        if(this.health <= 0){
            this.x = this.spawnX;
            this.y = this.spawnY;
        }
    }

    /**
     * Handles all movements of the player, along with things like health regen and reload.
     * @param hitboxes The list of hitboxes present, just the entire map.
     */
    public void move(Block[][] hitboxes){
        this.inLiquid = false;

        // Preliminary collisions to see if the player is in water.
        this.checkCollisions(hitboxes);

        // Different considerations in water
        if(this.inLiquid){
            this.speed = 2;
            this.jumpAmt = -1;
            this.verticalAccel = 0.07;
            this.canJump = true;

            this.verticalVel = Math.min(this.verticalVel, 3);
        } else {
            this.speed = 3;
            this.verticalAccel = 0.12;
            this.jumpAmt = -6;

            this.verticalVel = Math.min(this.verticalVel, 4);
        }

        // X-axis movements
        if(moveLeft) this.x -= this.speed;
        if(moveRight) this.x += this.speed;
        this.xCollide(hitboxes);

        if(moveUp && this.canJump) this.verticalVel = this.jumpAmt;

        // Simulate gravity while doing y-axis movements
        this.y += this.verticalVel;
        this.verticalVel += verticalAccel;

        this.yCollide(hitboxes);

        // For when the player's health drops to 0
        this.handleDeaths();

        // Health regen
        if(this.health < 100.0)
            this.health += 0.05;

        // Reload time decrement
        if (this.reload > 0)
            this.reload--;
    }

    /**
     * Draws the player
     * @param g Graphics object
     * @param playerPos The position the player as an array, [x, y]
     */
    @Override
    public void paint(Graphics g, int screenWidth, int screenHeight, int[] playerPos){
        g.drawImage(img, screenWidth/2, screenHeight/2, this.w, this.w, null);

        // Health bar, green rectangle over a red base.
        g.setColor(Color.RED);
        g.fillRect(screenWidth/2, screenHeight/2 - 15, 30, 5);

        g.setColor(Color.GREEN);
        g.fillRect(screenWidth/2, screenHeight/2 - 15, (int)(30 * this.health/100), 5);
    }
}

