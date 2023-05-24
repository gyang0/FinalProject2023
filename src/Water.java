/**
 * A block that resembles water (it's blue and flows, what else is there?)
 * @author Gene Yang
 * @version May 24, 2023
 */
public class Water extends LiquidBlock {
    public Water(int x, int y, int w){
        super(x, y, w, "src/imgs/water.png");
        this.updateRate = 25;
    }

    @Override
    public void update(Block[][] map, int[] playerPos){
        // Doesn't do anything until the number of ticks matches the update rate.
        this.numTicks++;
        if(this.numTicks != this.updateRate)
            return;

        int curX = this.x/this.w;
        int curY = this.y/this.w;

        // Flow down
        if(curY + 1 < map[0].length && map[curX][curY + 1] instanceof CaveBackground){
            map[curX][curY] = new CaveBackground(curX * this.w, curY * this.w, this.w);
            map[curX][curY + 1] = new Water(curX * this.w, (curY + 1) * this.w, this.w);
        } else {
            int choice = (int)(Math.random() * 2);
            if(choice == 0 && curX + 1 < map.length && map[curX + 1][curY] instanceof CaveBackground){
                map[curX][curY] = new CaveBackground(curX * this.w, curY * this.w, this.w);
                map[curX + 1][curY] = new Water((curX + 1) * this.w, curY * this.w, this.w);
            }
            else if(curX - 1 >= 0 && map[curX - 1][curY] instanceof CaveBackground){
                map[curX][curY] = new CaveBackground(curX * this.w, curY * this.w, this.w);
                map[curX - 1][curY] = new Water((curX - 1) * this.w, curY * this.w, this.w);
            }
        }

        // Reset number of ticks
        this.numTicks = 0;
    }
}