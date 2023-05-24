import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;

// Note 1: The game can get slightly laggy at times, but usually it should run fine.
// Note 2: The 'imgs' folder should be in the 'src' folder. All other classes should be in 'src' too.

/* Credits
 * The block-block collision algorithm is AABB (Axis-Aligned Bounding Block), from
 *     https://developer.mozilla.org/en-US/docs/Games/Techniques/2D_collision_detection
 *
 * The image stuff
 *     First I made a crude pixel art editor to design the blocks: https://github.com/gyang0/PixelArtTool
 *     After designing each image and screenshotting, I added each image into the 'imgs' folder.
 *     The ImageIcon/Image stuff was referenced from my chess program: https://github.com/gyang0/Hackberry/blob/main/src/Piece.java
 *          I don't remember the exact links, but I think I learned the ImageIcon stuff used in the chess program here:
 *              https://zetcode.com/java/imageicon/
 *              https://stackoverflow.com/questions/13011705/how-to-add-an-imageicon-to-a-jframe
 *
 *      For some blocks, I made the PNG background transparent using this tool:
 *              https://onlinepngtools.com/create-transparent-png
 *
 * The storyline was partly inspired by the atomic bomb project and those occasional nuclear reactor explosions in the news.
 * Cutscenes were made using https://sketch.io/sketchpad/
 *      Source of the embarrassing "Drag to move" that somehow made it into the last cutscene, but oh well.
 * */

/** Log
 * Apr 11 - Added basic class structure for blocks.
 * Apr 12 - Simplified class structure for the time being and tried to figure out Swing.
 * Apr 14 - Went to bed depressed after key inputs didn't work.
 * Apr 15 - Rewrote key inputs with reference from Wordle, I had forgotten to call setFocusable(). Went to bed happy.
 * Apr 17 - Finished collisions.
 * Apr 19 - Created level map, added lava & water classes. Screen now moves with the player, and player slows down in liquid.
 * Apr 22 - Added gravity and jumping for player. Player now moves differently in liquid.
 *
 * Apr 24 - Added basic land generation with random floodfill (stone with lava pockets).
 * Apr 26 - Lighting effects based on distance from player, constrained player x and vertical velocity.
 * Apr 27 - Added a sky block as a transparent block.
 * Apr 29 - Replaced Lava block with Acid. Got a decent-looking cave by adding random Stone clusters in Air blocks.
 * May 3 - Took a break for APs, removed considerable lag by doing player collisions with blocks in range only.
 * May 5 - Bullets created on mouse click, removes the block they collide with. Tweaked key events so that arrow keys work too.
 * May 6 - Liquid blocks flow downwards (referenced from FallingSand, but my approach is cruder).
 * May 8 - Added basic enemies that move around randomly.
 * May 10 - Decided to rework the class structure. The Swing nightmare has started once more.
 * May 12 - Started preliminary work on block graphics, tweaked terraform method to include fewer liquid blocks.
 * May 14 - Removed horrible block graphics with still horrible but less gaudy graphics. Added player & enemy graphics
 *          Started work on player health.
 * May 16 - Added decoration blocks and opacity calculation depending on the block's distance from the player.
 *          Cleaned up the structure by forcing related classes to use the same (or nearly the same) methods.
 *          Added a cooldown period for the mining gun.
 *          Hastily patched up the enemy movement bug.
 * May 18 - Touched up shading and added block outlines (outlines make everything look better)
 * May 20 - Added health regen to make it easier, freaked out once I saw that the deadline was close.
 *          Added basic cutscene functionality with half-baked story.
 * May 24 - Final touches (that is, going on a commenting binge in which I tried to comment every method in order to
 *                         make up for not commenting while actually coding)
 */


/**
 * Main class, handles Swing and stuff.
 * @author Gene Yang
 * @version May 24, 2023
 */
public class Game {
    // Constants
    private final int SCREEN_WIDTH = 700;
    private final int SCREEN_HEIGHT = 600;
    private final int BLOCK_WIDTH = 40;
    private final int MAP_WIDTH = 75; // In blocks
    private final int MAP_HEIGHT = 500; // In blocks
    private final int ENEMY_COUNT = 50; // Number of enemies
    private final String GAME_FONT = "Helvetica Neue-bold-20";

    // Number of block rows and columns to display
    private int numBlockRows;
    private int numBlockColumns;

    // Sprites
    private Player player;
    private ArrayList<Bullet> bullets;
    private ArrayList<Enemy> enemies;

    // Swing
    private JFrame window;
    private Scene graphicsPanel;

    // The constructor is used to set up the Scene and draw all graphics.
    public Game(){
        window = new JFrame("Test");
        window.setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLayout(new BorderLayout());

        graphicsPanel = new Scene();
        window.add(graphicsPanel, BorderLayout.CENTER);

        window.setVisible(true);
    }

    /**
     * Main method
     * @param args
     */
    public static void main(String[] args){
        Game game = new Game();
    }

    /**
     * A class that handles all graphics in the game and uses listeners.
     * @author Gene Yang
     * @version May 24, 2023
     */
    private class Scene extends JPanel implements MouseListener, KeyListener {
        // The height bounds for the mysterious lab at the end.
        private int MIN_LAB_HEIGHT = 10;
        private int MAX_LAB_HEIGHT = 15;

        // Map setup
        private Block[][] map;
        private TransparentBlock[][] decor;
        private boolean alreadySet[][]; // Whether the block of a map position has been set
        private boolean decorSet[][]; // Whether the decor of a map position has been set

        // Data associated with different block types
        // Java has made me overly descriptive for variable names
        private ArrayList<String> blockTypes;
        private HashMap<String, Double> blockVarietyProb;
        private HashMap<String, Integer> blockTerraformMaxDepth;

        // Data associated with different decor block types
        private ArrayList<String> decorBlockTypes;
        private HashMap<String, Double> decorBlockVarietyProb;

        // List of cutscenes
        private ArrayList<String> cutscenes;
        private boolean startCutScene;
        private int curCutScene;
        private int depthPerCutScene;
        private ArrayList<Integer> cutSceneDone = new ArrayList<Integer>();

        // Coordinates for the skip/continue button at the cutscenes
        private final int[] skipBtn = new int[]{320, 500, 100, 30}; // [x, y, w, h]

        /**
         * Initializes the data for blocks, decor blocks, and cutscenes.
         */
        public void initInfo() {
            blockTypes = new ArrayList<String>();
            blockVarietyProb = new HashMap<String, Double>();
            blockTerraformMaxDepth = new HashMap<String, Integer>();

            // Add the block types
            // Coding aesthetics
            blockTypes.add("stone");
            blockVarietyProb.put("stone", 1.5);
            blockTerraformMaxDepth.put("stone", 5);
            blockTypes.add("dirt");
            blockVarietyProb.put("dirt", 1.0);
            blockTerraformMaxDepth.put("dirt", 4);
            blockTypes.add("acid");
            blockVarietyProb.put("acid", 0.2);
            blockTerraformMaxDepth.put("acid", 3);
            blockTypes.add("water");
            blockVarietyProb.put("water", 0.2);
            blockTerraformMaxDepth.put("water", 3);

            // Decor blocks
            decorBlockTypes = new ArrayList<String>();
            decorBlockVarietyProb = new HashMap<String, Double>();

            decorBlockTypes.add("stalagmite");
            decorBlockVarietyProb.put("stalagmite", 50.0);
            decorBlockTypes.add("stalactite");
            decorBlockVarietyProb.put("stalactite", 50.0);
            decorBlockTypes.add("bat");
            decorBlockVarietyProb.put("bat", 10.0);
            decorBlockTypes.add("flower");
            decorBlockVarietyProb.put("flower", 40.0);
            decorBlockTypes.add("vine");
            decorBlockVarietyProb.put("vine", 80.0);

            // Cutscenes
            cutscenes = new ArrayList<String>();
            cutscenes.add("cutscene0.png"); // First cutscene is the instructions page
            cutscenes.add("cutscene1.png");
            cutscenes.add("cutscene2.png");
            cutscenes.add("cutscene3.png");
            cutscenes.add("cutscene4.png");
            cutscenes.add("cutscene5.png");

            curCutScene = 0;
            startCutScene = false;
            depthPerCutScene = (MAP_HEIGHT - MAX_LAB_HEIGHT)/cutscenes.size();
        }

        /**
         * Floodfill to create a unique map every time.
         * @param row The row of the current position
         * @param column The column of the current position
         * @param depth The current depth of recursion
         * @param maxDepth Maximum depth of recursion
         * @param type The type of block to replace the current position with
         */
        public void floodfill(int row, int column, int depth, int maxDepth, String type) {
            // If we recursed too deep or if the 10% chance is met, return.
            if (depth > maxDepth) return;
            if ((int) (Math.random() * 100) < 10) return;

            // Avoid the edges
            if (column < 1 || column >= MAP_HEIGHT - 1 || row < 1 || row >= MAP_WIDTH - 1) return;

            switch (type) {
                case "acid":
                    map[row][column] = new Acid(row * BLOCK_WIDTH, column * BLOCK_WIDTH, BLOCK_WIDTH);
                    break;
                case "stone":
                    map[row][column] = new Stone(row * BLOCK_WIDTH, column * BLOCK_WIDTH, BLOCK_WIDTH);
                    break;
                case "water":
                    map[row][column] = new Water(row * BLOCK_WIDTH, column * BLOCK_WIDTH, BLOCK_WIDTH);
                    break;
                case "dirt":
                    map[row][column] = new Dirt(row * BLOCK_WIDTH, column * BLOCK_WIDTH, BLOCK_WIDTH);
                    break;
                case "cave":
                    map[row][column] = new CaveBackground(row * BLOCK_WIDTH, column * BLOCK_WIDTH, BLOCK_WIDTH);
                    break;
            }

            // The block at map[row][column] has now been set.
            alreadySet[row][column] = true;

            // Recursion
            floodfill(row - 1, column, depth + 1, maxDepth, type);
            floodfill(row + 1, column, depth + 1, maxDepth, type);
            floodfill(row, column - 1, depth + 1, maxDepth, type);
            floodfill(row, column + 1, depth + 1, maxDepth, type);
        }

        /**
         * Sets the decor of a certain block.
         * @param row The row of the position to set
         * @param col The column of the position to set
         * @param type The type of decor block as a String
         */
        public void setDecor(int row, int col, String type) {
            // A decor block can only be placed in position not occupied by any other block.
            if (!(map[row][col] instanceof CaveBackground))
                return;

            switch (type) {
                // Most of the decor are things that grow on the underside of blocks.
                case "stalactite":
                    if (col > 0 && map[row][col - 1] instanceof Stone) {
                        decor[row][col] = new Stalactite(row * BLOCK_WIDTH, col * BLOCK_WIDTH, BLOCK_WIDTH);
                    }
                    break;
                case "bat":
                    if (col > 0 && map[row][col - 1] instanceof SolidBlock) {
                        decor[row][col] = new Bat(row * BLOCK_WIDTH, col * BLOCK_WIDTH, BLOCK_WIDTH);
                    }
                    break;
                case "flower":
                    if (col > 0 && map[row][col - 1] instanceof Dirt) {
                        decor[row][col] = new Flower(row * BLOCK_WIDTH, col * BLOCK_WIDTH, BLOCK_WIDTH);
                    }
                    break;
                case "vine":
                    if (col > 0 && (map[row][col - 1] instanceof SolidBlock || map[row][col - 1] instanceof Vine)) {
                        decor[row][col] = new Vine(row * BLOCK_WIDTH, col * BLOCK_WIDTH, BLOCK_WIDTH);
                    }
                    break;

                // Only the stalagmite grows on the top of blocks (I think?)
                case "stalagmite":
                    if (col < MAP_HEIGHT - 1 && map[row][col + 1] instanceof Stone) {
                        decor[row][col] = new Stalagmite(row * BLOCK_WIDTH, col * BLOCK_WIDTH, BLOCK_WIDTH);
                    }
                    break;
            }
        }

        /**
         * Draws the mysterious lab which hopefully looks like a lab.
         */
        public void drawLab() {
            int curHeight = 10;

            for (int i = 1; i < MAP_WIDTH - 1; i++) {
                // Roughly 1/3 possibility of changing the height.
                if (Math.random() * 100 < 33)
                    curHeight = (int) (Math.random() * (MAX_LAB_HEIGHT - MIN_LAB_HEIGHT)) + MIN_LAB_HEIGHT;

                for (int j = 0; j < curHeight; j++) {
                    // Two types of blocks used for the lab
                    if (Math.random() * 100 < 75)
                        map[i][MAP_HEIGHT - 2 - j] = new LabBlock1(i * BLOCK_WIDTH, (MAP_HEIGHT - 2 - j) * BLOCK_WIDTH, BLOCK_WIDTH);
                    else
                        map[i][MAP_HEIGHT - 2 - j] = new LabBlock2(i * BLOCK_WIDTH, (MAP_HEIGHT - 2 - j) * BLOCK_WIDTH, BLOCK_WIDTH);

                    // Add a striped block in a half-hearted attempt to make it look more artificial.
                    if (j == curHeight - 1)
                        map[i][MAP_HEIGHT - 2 - j] = new LabBlock3(i * BLOCK_WIDTH, (MAP_HEIGHT - 2 - j) * BLOCK_WIDTH, BLOCK_WIDTH);


                    // Remove the decor blocks on top.
                    decor[i][MAP_HEIGHT - 2 - j] = null;
                }

                // 4 spaces of padding.
                for (int j = 0; j < 4; j++)
                    map[i][MAP_HEIGHT - 2 - curHeight - j] = new CaveBackground(i * BLOCK_WIDTH, (MAP_HEIGHT - 2 - curHeight - j) * BLOCK_WIDTH, BLOCK_WIDTH);
            }
        }

        /**
         * Where the actual fun takes place.
         * Uses the floodfill methods to terraform the map, adding random patches here and there.
         */
        public void terraform() {
            // Start by adding a background
            for (int r = 0; r < MAP_WIDTH; r++) {
                for (int c = 0; c < MAP_HEIGHT; c++) {
                    // Flipped coordinates for correct display
                    map[r][c] = new CaveBackground(r * BLOCK_WIDTH, c * BLOCK_WIDTH, BLOCK_WIDTH);
                }
            }

            // Make several chunks of stone and dirt.
            for (String type : blockTypes) {
                for (int i = 0; i < MAP_HEIGHT - MAX_LAB_HEIGHT; i++) {
                    for (int j = 0; j < MAP_WIDTH; j++) {
                        if (alreadySet[j][i])
                            continue;

                        if (Math.random() * 100 < blockVarietyProb.get(type)) {
                            floodfill(j, i, 0, blockTerraformMaxDepth.get(type), type);
                        }
                    }
                }

                // Reset for other block types
                alreadySet = new boolean[MAP_WIDTH][MAP_HEIGHT];
            }

            // Add decorations
            for (String type : decorBlockTypes) {
                for (int i = 1; i < MAP_HEIGHT - 1; i++) {
                    for (int j = 1; j < MAP_WIDTH - MAX_LAB_HEIGHT; j++) {
                        if (decorSet[j][i] || map[j][i] instanceof ImmovableBlock)
                            continue;

                        if (Math.random() * 100 < decorBlockVarietyProb.get(type)) {
                            setDecor(j, i, type);
                            decorSet[j][i] = true;
                        }
                    }
                }
            }

            // Add barriers
            for (int i = 0; i < MAP_WIDTH; i++)
                map[i][MAP_HEIGHT - 1] = new Barrier(i * BLOCK_WIDTH, (MAP_HEIGHT - 1) * BLOCK_WIDTH, BLOCK_WIDTH);
            for (int i = 0; i < MAP_HEIGHT; i++) {
                map[0][i] = new Barrier(0, i * BLOCK_WIDTH, BLOCK_WIDTH);
                map[MAP_WIDTH - 1][i] = new Barrier((MAP_WIDTH - 1) * BLOCK_WIDTH, i * BLOCK_WIDTH, BLOCK_WIDTH);
            }

            // Add enemies
            for (int i = 0; i < ENEMY_COUNT; i++) {
                // Enemies spawn in the lower half of the cave
                int x = (int) (Math.random() * (MAP_WIDTH * BLOCK_WIDTH));
                int y = (int) (Math.random() * (MAP_HEIGHT * BLOCK_WIDTH) / 2) + (MAP_HEIGHT * BLOCK_WIDTH) / 2;
                enemies.add(new Enemy(x, y, BLOCK_WIDTH));
            }

            // The mysterious laboratory at the bottom
            this.drawLab();
        }

        /**
         * Handles the collisions & removal of bullets shot from the player's mining gun
         * @param g The Graphics parameter from this.paintComponent.
         */
        public void handleBullets(Graphics g) {
            // Drawing
            for (int i = 0; i < bullets.size(); i++) {
                bullets.get(i).update();
                bullets.get(i).paint(g);

                int curI = (bullets.get(i).getX() + player.x - SCREEN_WIDTH / 2) / BLOCK_WIDTH;
                int curJ = (bullets.get(i).getY() + player.y - SCREEN_HEIGHT / 2) / BLOCK_WIDTH;

                // Remove bullets that are out of range
                if (curI < 0 || curI >= MAP_WIDTH || curJ < 0 || curJ >= MAP_HEIGHT ||
                        bullets.get(i).outOfRange(SCREEN_WIDTH, SCREEN_HEIGHT)) {
                    bullets.remove(i);
                }
            }
            for (int i = 0; i < bullets.size(); i++) {
                int curI = (bullets.get(i).getX() + player.x - SCREEN_WIDTH / 2) / BLOCK_WIDTH;
                int curJ = (bullets.get(i).getY() + player.y - SCREEN_HEIGHT / 2) / BLOCK_WIDTH;

                // Collisions
                if (map[curI][curJ] instanceof SolidBlock) {
                    bullets.get(i).makeBoom(map, decor, curI, curJ);
                    bullets.remove(i);
                }
            }
        }

        /**
         * Pauses for a specified amount of milliseconds, used for animation.
         * @param ms The milliseconds to pause
         */
        public void pause(int ms) {
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * The constructor is used to add appropriate listeners, initialize the block data, and terraform the map.
         */
        public Scene() {
            // Mouse and key Listeners
            setFocusable(true);
            requestFocusInWindow();
            addMouseListener(this);
            addKeyListener(this);

            numBlockColumns = SCREEN_WIDTH / BLOCK_WIDTH;
            numBlockRows = SCREEN_HEIGHT / BLOCK_WIDTH;

            // Sprites
            player = new Player(MAP_WIDTH * BLOCK_WIDTH / 2, -100, 30);
            bullets = new ArrayList<Bullet>();
            enemies = new ArrayList<Enemy>();

            // Fills in the map with stone.
            map = new Block[MAP_WIDTH][MAP_HEIGHT];
            decor = new TransparentBlock[MAP_WIDTH][MAP_HEIGHT];
            alreadySet = new boolean[MAP_WIDTH][MAP_HEIGHT];
            decorSet = new boolean[MAP_WIDTH][MAP_HEIGHT];

            // Add block info and terraform the map
            initInfo();
            terraform();
        }

        /**
         * Draws everything used in the game
         * @param g the <code>Graphics</code> object to protect
         */
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            // If a cutscene hasn't started
            if (!startCutScene) {
                // Center the display on the player's current square, so that no blocks are painted unnecessarily.
                for (int i = player.x / BLOCK_WIDTH - numBlockColumns / 2 - 1; i <= player.x / BLOCK_WIDTH + numBlockColumns / 2 + 1; i++) {
                    for (int j = player.y / BLOCK_WIDTH - numBlockRows / 2 - 1; j <= player.y / BLOCK_WIDTH + numBlockRows / 2 + 1; j++) {

                        if (i < 0 || i >= MAP_WIDTH || j < 0 || j >= MAP_HEIGHT)
                            continue;

                        // Update blocks if needed
                        map[i][j].update(map, new int[]{player.x, player.y});

                        // Shift the display by (player.x, player.y) to give the camera effect.
                        map[i][j].paint(g, SCREEN_WIDTH, SCREEN_HEIGHT, new int[]{player.x, player.y});
                    }
                }

                // Player movements & display
                player.move(map);

                // Constrain player movements
                player.x = (Math.min(player.x, (MAP_WIDTH - 1) * BLOCK_WIDTH));
                player.x = (Math.max(player.x, 0));

                player.checkEnemyCollisions(enemies);
                player.paint(g, SCREEN_WIDTH, SCREEN_HEIGHT, new int[]{player.x, player.y});
                handleBullets(g);

                for (Enemy e : enemies) {
                    e.update(map, new int[]{player.x, player.y});
                    e.paint(g, SCREEN_WIDTH, SCREEN_HEIGHT, new int[]{player.x, player.y});
                }

                // Decorations
                for (int i = player.x / BLOCK_WIDTH - numBlockRows / 2 - 1; i <= player.x / BLOCK_WIDTH + numBlockRows / 2 + 1; i++) {
                    for (int j = player.y / BLOCK_WIDTH - numBlockColumns / 2 - 1; j <= player.y / BLOCK_WIDTH + numBlockColumns / 2 + 1; j++) {
                        if (i < 0 || i >= MAP_WIDTH || j < 0 || j >= MAP_HEIGHT)
                            continue;

                        if (decor[i][j] == null)
                            continue;

                        // Shift the display by (player.x, player.y) to give the camera effect.
                        decor[i][j].paint(g, SCREEN_WIDTH, SCREEN_HEIGHT, new int[]{player.x, player.y});
                    }
                }

                // Progress bar
                g.setColor(Color.WHITE);
                g.fillRect(550, 50, 10, 100);

                g.setColor(Color.GRAY);
                g.fillRect(550, 50 + (player.y / BLOCK_WIDTH) * 100 / MAP_HEIGHT, 10, 10);

                g.setColor(Color.GREEN);
                for (int i = 0; i < cutscenes.size(); i++) {
                    g.fillRect(550, 50 + ((depthPerCutScene * i * BLOCK_WIDTH) / BLOCK_WIDTH) * 100 / MAP_HEIGHT, 10, 5);
                }

            } else {
                // Background
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

                // Cutscene image
                ImageIcon cutSceneIcon = new ImageIcon("src/imgs/" + cutscenes.get(curCutScene));
                Image cutSceneImg = cutSceneIcon.getImage();
                g.drawImage(cutSceneImg, 150, 0, Math.min(SCREEN_WIDTH, SCREEN_HEIGHT) - 150, Math.min(SCREEN_WIDTH, SCREEN_HEIGHT) - 150, null);

                // Skip button
                g.setColor(Color.GRAY);
                g.fillRoundRect(skipBtn[0], skipBtn[1], skipBtn[2], skipBtn[3], 5, 5);

                g.setColor(Color.WHITE);
                g.setFont(Font.decode(GAME_FONT));
                g.drawString("Continue", skipBtn[0] + 10, skipBtn[1] + 20);
            }

            // If the player has reached a cutscene depth and the associated cutscene isn't finished
            if (player.y / BLOCK_WIDTH % depthPerCutScene == 0 && !cutSceneDone.contains(player.y / BLOCK_WIDTH)) {
                // If all cutscenes haven't been finished
                if(curCutScene < cutscenes.size()) {
                    startCutScene = true;
                    cutSceneDone.add(player.y / BLOCK_WIDTH);
                }
            }

            // Puase for 5 milliseconds and redraw.
            pause(5);
            repaint();
        }

        /* Events */
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyChar() == 'w' || e.getKeyChar() == 'W')
                player.setMoveUp(true);
            if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyChar() == 'a' || e.getKeyChar() == 'A')
                player.setMoveLeft(true);
            if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyChar() == 'd' || e.getKeyChar() == 'D')
                player.setMoveRight(true);
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyChar() == 'w' || e.getKeyChar() == 'W')
                player.setMoveUp(false);
            if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyChar() == 'a' || e.getKeyChar() == 'A')
                player.setMoveLeft(false);
            if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyChar() == 'd' || e.getKeyChar() == 'D')
                player.setMoveRight(false);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (player.getReload() <= 0) {
                bullets.add(new Bullet(SCREEN_WIDTH / 2 + player.w / 2, SCREEN_HEIGHT / 2 + player.w / 2,
                        Math.atan2(e.getY() - (SCREEN_HEIGHT / 2 + player.w / 2),
                                e.getX() - (SCREEN_WIDTH / 2 + player.w / 2))
                        )
                );

                player.setReload(100);
            }

            // Skip button
            // Ugly hard-coded button, but just a one time use anyway.
            if (startCutScene) {
                if (e.getX() > skipBtn[0] && e.getX() < skipBtn[0] + skipBtn[2] && e.getY() > skipBtn[1] && e.getY() < skipBtn[1] + skipBtn[3]) {
                    startCutScene = false;
                    curCutScene++;
                }
            }
        }

        // Unused methods
        @Override public void keyTyped(KeyEvent e) {}
        @Override public void mousePressed(MouseEvent e) {}
        @Override public void mouseReleased(MouseEvent e) {}
        @Override public void mouseEntered(MouseEvent e) {}
        @Override public void mouseExited(MouseEvent e) {}
    }
}