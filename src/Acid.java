/**
 * Acid, which flows like a LiquidBlock and can harm the player.
 * @author Gene Yang
 * @version May 24, 2023
 */
public class Acid extends LiquidBlock {
    public Acid(int x, int y, int w){
        super(x, y, w, "src/imgs/acid.png");
        this.updateRate = 50;
    }

    @Override
    public void update(Block[][] map, int[] playerPos){
        // Don't update unless the number of ticks is equal to the update rate
        this.numTicks++;
        if(this.numTicks != this.updateRate)
            return;

        int curX = this.x/this.w;
        int curY = this.y/this.w;

        // Flow down
        if(curY + 1 < map[0].length && map[curX][curY + 1] instanceof CaveBackground){
            map[curX][curY] = new CaveBackground(curX * this.w, curY * this.w, this.w);
            map[curX][curY + 1] = new Acid(curX * this.w, (curY + 1) * this.w, this.w);
        } else {
            int choice = (int)(Math.random() * 2);
            if(choice == 0 && curX + 1 < map.length && map[curX + 1][curY] instanceof CaveBackground){
                map[curX][curY] = new CaveBackground(curX * this.w, curY * this.w, this.w);
                map[curX + 1][curY] = new Acid((curX + 1) * this.w, curY * this.w, this.w);
            }
            else if(curX - 1 >= 0 && map[curX - 1][curY] instanceof CaveBackground){
                map[curX][curY] = new CaveBackground(curX * this.w, curY * this.w, this.w);
                map[curX - 1][curY] = new Acid((curX - 1) * this.w, curY * this.w, this.w);
            }
        }

        // Reset number of ticks
        this.numTicks = 0;
    }
}
