/**
 * A block that's transparent and can't collide with the player.
 * Acts as the base class for most decoration blocks.
 *
 * @author Gene Yang
 * @version May 24, 2023
 */
public class TransparentBlock extends Block {
    public TransparentBlock(int x, int y, int w, String imgSrc){
        super(x, y, w, imgSrc);
    }

    @Override
    public void update(Block[][] map, int[] playerPos) {}
}
