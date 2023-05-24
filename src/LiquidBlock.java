/**
 * A block that flows, simulating liquid.
 * @author Gene Yang
 * @version May 24, 2023
 */
public class LiquidBlock extends Block {
    public LiquidBlock(int x, int y, int w, String imgSrc){
        super(x, y, w, imgSrc);
    }

    @Override
    public void update(Block[][] map, int[] playerPos) {}
}
