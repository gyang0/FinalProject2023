/**
 * A Barrier block pads the edges of the map and can't be broken.
 * @author Gene Yang
 * @version May 24, 2023
 */
public class Barrier extends ImmovableBlock {
    public Barrier(int x, int y, int w){
        super(x, y, w, "src/imgs/barrier.png");
    }
}
