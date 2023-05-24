/**
 * Immovable blocks can't be destroyed by the player.
 * Technically this class does nothing except serve as the base class, but it makes for neater code.
 *
 * @author Gene Yang
 * @version May 24, 2023
 */
public class ImmovableBlock extends SolidBlock {
    public ImmovableBlock(int x, int y, int w, String imgSrc){
        super(x, y, w, imgSrc);
    }
}
