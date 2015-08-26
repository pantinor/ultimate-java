package util;

/**
 * Performs FOV by pushing values outwards from the source location. It will
 * spread around edges like smoke or water. This may not be the desired behavior
 * for a strict sight area, but may be appropriate for a sound map.
 *
 * This algorithm does perform bounds checking.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class SpreadFOV implements FOVSolver {

    private float[][] lightMap;
    private float[][] map;
    private float radius, decay;
    private int startx, starty, width, height;
    private RadiusStrategy rStrat;
    private boolean wrap;

    public SpreadFOV(int width, int height, boolean wrap) {
        this.width = width;
        this.height = height;
        this.lightMap = new float[width][height];
        this.wrap = wrap;
    }

    public float[][] getLightMap() {
        return lightMap;
    }

    @Override
    public float[][] calculateFOV(float[][] map, int startx, int starty, float radius) {
        return calculateFOV(map, startx, starty, 1, 1 / radius, BasicRadiusStrategy.CIRCLE);
    }

    @Override
    public float[][] calculateFOV(float[][] map, int startx, int starty, float force, float decay, RadiusStrategy rStrat) {

        this.map = map;
        this.decay = decay;
        this.startx = startx;
        this.starty = starty;
        this.rStrat = rStrat;
        this.radius = force / decay; //assume worst case of no resistance in tiles

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                lightMap[x][y] = 0;
            }
        }

        lightMap[startx][starty] = force; //make the starting space full power

        lightSurroundings(startx, starty);

        return lightMap;
    }

    private void lightSurroundings(int sx, int sy) {
        if (lightMap[sx][sy] <= 0) {
            return;//no light to spread
        }

        for (int x = sx - 1; x <= sx + 1; x++) {
            for (int y = sy - 1; y <= sy + 1; y++) {

                int col = x;
                int row = y;
                float dx = Math.abs(startx - x);
                float dy = Math.abs(starty - y);

                if (wrap) {
                    if (col < 0) {
                        col = width + col;
                    } else if (col >= width) {
                        col = col - width;
                    }
                    if (row < 0) {
                        row = height + row;
                    } else if (row >= height) {
                        row = row - height;
                    }
                }

                //ensure in bounds
                if (col < 0 || col >= width || row < 0 || row >= height) {
                    continue;
                }

                float distance = rStrat.radius(dx, dy);
                if (distance <= radius) {
                    float surroundingLight = getNearLight(x, y);
                    if (lightMap[col][row] < surroundingLight) {
                        lightMap[col][row] = surroundingLight;
                        lightSurroundings(col, row);
                    }
                }
            }
        }
    }

    /**
     * Find the light let through by the nearest square.
     *
     * @param x
     * @param y
     * @return
     */
    private float getNearLight(int x, int y) {
        int x2 = x - (int) Math.signum(x - startx);
        int y2 = y - (int) Math.signum(y - starty);

        //clamp x2 and y2 to bound within map
        x2 = Math.max(0, x2);
        x2 = Math.min(width - 1, x2);
        y2 = Math.max(0, y2);
        y2 = Math.min(height - 1, y2);

        //find largest emmitted light in direction of source
        float light = Math.max(Math.max(lightMap[x2][y] - map[x2][y], lightMap[x][y2] - map[x][y2]), lightMap[x2][y2] - map[x2][y2]);

        float distance = rStrat.radius(x, y, x2, y2);
        light -= decay * distance;
        return light;
    }

}
