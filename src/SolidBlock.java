/**
 * A block that's solid and doesn't move, but one that can still be broken by the mining gun.
 * @author Gene Yang
 * @version May 24, 2023
 */
public class SolidBlock extends Block {
    public SolidBlock(int x, int y, int w, String imgSrc){
        super(x, y, w, imgSrc);
    }

    // Does nothing on update
    @Override
    public void update(Block[][] map, int[] playerPos){}
}
