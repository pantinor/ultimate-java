package generator;

import java.awt.image.BufferedImage;
import java.util.Date;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import util.Utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

public class Dungeon {

    private int xsize = 0;
    private int ysize = 0;

    private int objects = 0;
    private int chanceRoom = 75;

    private int[] dungeon_map = {};

    private long oldseed = 0;

    final public static int tileUnused = 0;
    final public static int tileDirtWall = 1; // not in use
    final public static int tileDirtFloor = 2;
    final public static int tileStoneWall = 3;
    final public static int tileCorridor = 4;
    final public static int tileDoor = 5;
    final public static int tileUpStairs = 6;
    final public static int tileDownStairs = 7;
    final public static int tileChest = 8;

    public static void main(String[] args) {

        Dungeon d = new Dungeon();
        d.createDungeon(32, 32, 30);

        System.out.println(d.showDungeon());

        JOptionPane.showMessageDialog(null, null, "Map", JOptionPane.YES_NO_OPTION, d.generateDungeonImage());

    }

    void createDungeon(int inx, int iny, int inobj) {

        if (inobj < 1) {
            objects = 10;
        } else {
            objects = inobj;
        }

        if (inx < 3) {
            xsize = 3;
        } else {
            xsize = inx;
        }

        if (iny < 3) {
            ysize = 3;
        } else {
            ysize = iny;
        }

        dungeon_map = new int[xsize * ysize];

        // start with making the "standard stuff" on the map
        for (int y = 0; y < ysize; y++) {
            for (int x = 0; x < xsize; x++) {
                // ie, making the borders of unwalkable walls
                if (y == 0) {
                    setCell(x, y, tileStoneWall);
                } else if (y == ysize - 1) {
                    setCell(x, y, tileStoneWall);
                } else if (x == 0) {
                    setCell(x, y, tileStoneWall);
                } else if (x == xsize - 1) {
                    setCell(x, y, tileStoneWall);
                } // and fill the rest with dirt
                else {
                    setCell(x, y, tileUnused);
                }
            }
        }

        /**
         * *****************************************************************************
         * And now the code of the random-map-generation-algorithm begins!
		 ******************************************************************************
         */
        // start with making a room in the middle, which we can start building upon
        makeRoom(xsize / 2, ysize / 2, 8, 6, getRand(0, 3));

        // keep count of the number of "objects" we've made
        int currentFeatures = 1; // +1 for the first room we just made

        // then we start the main loop
        for (int countingTries = 0; countingTries < 1000; countingTries++) {

            // check if we've reached our quota
            if (currentFeatures == objects) {
                break;
            }

            // start with a random wall
            int newx = 0;
            int xmod = 0;
            int newy = 0;
            int ymod = 0;
            int validTile = -1;

            for (int testing = 0; testing < 1000; testing++) {
                newx = getRand(1, xsize - 1);
                newy = getRand(1, ysize - 1);
                validTile = -1;

                if (getCell(newx, newy) == tileDirtWall || getCell(newx, newy) == tileCorridor) {
                    // check if we can reach the place
                    if (getCell(newx, newy + 1) == tileDirtFloor || getCell(newx, newy + 1) == tileCorridor) {
                        validTile = 0; //
                        xmod = 0;
                        ymod = -1;
                    } else if (getCell(newx - 1, newy) == tileDirtFloor || getCell(newx - 1, newy) == tileCorridor) {
                        validTile = 1; //
                        xmod = +1;
                        ymod = 0;
                    } else if (getCell(newx, newy - 1) == tileDirtFloor || getCell(newx, newy - 1) == tileCorridor) {
                        validTile = 2; //
                        xmod = 0;
                        ymod = +1;
                    } else if (getCell(newx + 1, newy) == tileDirtFloor || getCell(newx + 1, newy) == tileCorridor) {
                        validTile = 3; //
                        xmod = -1;
                        ymod = 0;
                    }

					// check that we haven't got another door nearby, so we
                    // won't get alot of openings besides each other
                    if (validTile > -1) {
                        if (getCell(newx, newy + 1) == tileDoor) // north
                        {
                            validTile = -1;
                        } else if (getCell(newx - 1, newy) == tileDoor)// east
                        {
                            validTile = -1;
                        } else if (getCell(newx, newy - 1) == tileDoor)// south
                        {
                            validTile = -1;
                        } else if (getCell(newx + 1, newy) == tileDoor)// west
                        {
                            validTile = -1;
                        }
                    }

					// if we can, jump out of the loop and continue with the
                    // rest
                    if (validTile > -1) {
                        break;
                    }
                }
            }

            if (validTile > -1) {

				// choose what to build now at our newly found place, and at
                // what direction
                int feature = getRand(0, 100);
                if (feature <= chanceRoom) { // a new room
                    if (makeRoom((newx + xmod), (newy + ymod), 8, 6, validTile)) {
                        currentFeatures++; // add to our quota

                        // then we mark the wall opening with a door
                        setCell(newx, newy, tileDoor);

                        // clean up infront of the door so we can reach it
                        setCell((newx + xmod), (newy + ymod), tileDirtFloor);
                    }
                } else if (feature >= chanceRoom) { // new corridor
                    if (makeCorridor((newx + xmod), (newy + ymod), 6, validTile)) {
                        // same thing here, add to the quota and a door
                        currentFeatures++;
                        setCell(newx, newy, tileDoor);
                    }
                }
            }
        }

        // sprinkle out the bonusstuff (stairs, chests etc.) over the map
        int newx = 0;
        int newy = 0;
        int ways = 0; // from how many directions we can reach the random spot
        // from
        int state = 0; // the state the loop is in, start with the stairs

        while (state != 10) {
            for (int testing = 0; testing < 1000; testing++) {

                newx = getRand(1, xsize - 1);
                newy = getRand(1, ysize - 2);

                ways = 4; // the lower the better

                // check if we can reach the spot
                if (getCell(newx, newy + 1) == tileDirtFloor || getCell(newx, newy + 1) == tileCorridor) {
                    // north
                    if (getCell(newx, newy + 1) != tileDoor) {
                        ways--;
                    }
                }

                if (getCell(newx - 1, newy) == tileDirtFloor || getCell(newx - 1, newy) == tileCorridor) {
                    // east
                    if (getCell(newx - 1, newy) != tileDoor) {
                        ways--;
                    }
                }

                if (getCell(newx, newy - 1) == tileDirtFloor || getCell(newx, newy - 1) == tileCorridor) {
                    // south
                    if (getCell(newx, newy - 1) != tileDoor) {
                        ways--;
                    }
                }

                if (getCell(newx + 1, newy) == tileDirtFloor || getCell(newx + 1, newy) == tileCorridor) {
                    // west
                    if (getCell(newx + 1, newy) != tileDoor) {
                        ways--;
                    }
                }

                if (state == 0) {
                    if (ways == 0) {
                        // we're in state 0, let's place a "upstairs" thing
                        setCell(newx, newy, tileUpStairs);
                        state = 1;
                        break;
                    }
                } else if (state == 1) {
                    if (ways == 0) {
                        // state 1, place a "downstairs"
                        setCell(newx, newy, tileDownStairs);
                        state = 10;
                        break;
                    }
                }
            }
        }

    }

    public void setCell(int x, int y, int celltype) {
        dungeon_map[x + xsize * y] = celltype;
    }

    public int getCell(int x, int y) {
        return dungeon_map[x + xsize * y];
    }

    private int getRand(int min, int max) {

        Date now = new Date();
        long seed = now.getTime() + oldseed;
        oldseed = seed;

        Random randomizer = new Random(seed);
        int n = max - min + 1;
        int i = randomizer.nextInt(n);
        if (i < 0) {
            i = -i;
        }

        return min + i;
    }

    private boolean makeCorridor(int x, int y, int lenght, int direction) {

        // define the dimensions of the corridor (er.. only the width and height..)
        int len = getRand(2, lenght);
        int floor = tileCorridor;
        int dir = 0;
        if (direction > 0 && direction < 4) {
            dir = direction;
        }

        int xtemp = 0;
        int ytemp = 0;

        // reject corridors that are out of bounds
        if (x < 0 || x > xsize) {
            return false;
        }
        if (y < 0 || y > ysize) {
            return false;
        }

        switch (dir) {

            case 0: // north
                xtemp = x;

                // make sure it's not out of the boundaries
                for (ytemp = y; ytemp > (y - len); ytemp--) {
                    if (ytemp < 0 || ytemp > ysize) {
                        return false; // oh boho, it was!
                    }
                    if (getCell(xtemp, ytemp) != tileUnused) {
                        return false;
                    }
                }

                // if we're still here, let's start building
                for (ytemp = y; ytemp > (y - len); ytemp--) {
                    setCell(xtemp, ytemp, floor);
                }
                break;

            case 1: // east
                ytemp = y;

                for (xtemp = x; xtemp < (x + len); xtemp++) {
                    if (xtemp < 0 || xtemp > xsize) {
                        return false;
                    }
                    if (getCell(xtemp, ytemp) != tileUnused) {
                        return false;
                    }
                }

                for (xtemp = x; xtemp < (x + len); xtemp++) {
                    setCell(xtemp, ytemp, floor);
                }
                break;

            case 2: // south
                xtemp = x;

                for (ytemp = y; ytemp < (y + len); ytemp++) {
                    if (ytemp < 0 || ytemp > ysize) {
                        return false;
                    }
                    if (getCell(xtemp, ytemp) != tileUnused) {
                        return false;
                    }
                }

                for (ytemp = y; ytemp < (y + len); ytemp++) {
                    setCell(xtemp, ytemp, floor);
                }
                break;

            case 3: // west
                ytemp = y;

                for (xtemp = x; xtemp > (x - len); xtemp--) {
                    if (xtemp < 0 || xtemp > xsize) {
                        return false;
                    }
                    if (getCell(xtemp, ytemp) != tileUnused) {
                        return false;
                    }
                }

                for (xtemp = x; xtemp > (x - len); xtemp--) {
                    setCell(xtemp, ytemp, floor);
                }
                break;
        }

        return true;
    }

    private boolean makeRoom(int x, int y, int xlength, int ylength, int direction) {

		// define the dimensions of the room, it should be at least 4x4 tiles
        // (2x2 for walking on, the rest is walls)
        int xlen = getRand(4, xlength);
        int ylen = getRand(4, ylength);

        // the tile type it's going to be filled with
        int floor = tileDirtFloor; // jordgolv..
        int wall = tileDirtWall; // jordv????gg

        // choose the way it's pointing at
        int dir = 0;
        if (direction > 0 && direction < 4) {
            dir = direction;
        }

        switch (dir) {

            case 0: // north

                // Check if there's enough space left for it
                for (int ytemp = y; ytemp > (y - ylen); ytemp--) {
                    if (ytemp < 0 || ytemp > ysize) {
                        return false;
                    }
                    for (int xtemp = (x - xlen / 2); xtemp < (x + (xlen + 1) / 2); xtemp++) {
                        if (xtemp < 0 || xtemp > xsize) {
                            return false;
                        }
                        if (getCell(xtemp, ytemp) != tileUnused) {
                            return false; // no space left...
                        }
                    }
                }

                // we're still here, build
                for (int ytemp = y; ytemp > (y - ylen); ytemp--) {
                    for (int xtemp = (x - xlen / 2); xtemp < (x + (xlen + 1) / 2); xtemp++) {
                        // start with the walls
                        if (xtemp == (x - xlen / 2)) {
                            setCell(xtemp, ytemp, wall);
                        } else if (xtemp == (x + (xlen - 1) / 2)) {
                            setCell(xtemp, ytemp, wall);
                        } else if (ytemp == y) {
                            setCell(xtemp, ytemp, wall);
                        } else if (ytemp == (y - ylen + 1)) {
                            setCell(xtemp, ytemp, wall);
                        } // and then fill with the floor
                        else {
                            setCell(xtemp, ytemp, floor);
                        }
                    }
                }

                break;

            case 1: // east

                for (int ytemp = (y - ylen / 2); ytemp < (y + (ylen + 1) / 2); ytemp++) {
                    if (ytemp < 0 || ytemp > ysize) {
                        return false;
                    }
                    for (int xtemp = x; xtemp < (x + xlen); xtemp++) {
                        if (xtemp < 0 || xtemp > xsize) {
                            return false;
                        }
                        if (getCell(xtemp, ytemp) != tileUnused) {
                            return false;
                        }
                    }
                }

                for (int ytemp = (y - ylen / 2); ytemp < (y + (ylen + 1) / 2); ytemp++) {
                    for (int xtemp = x; xtemp < (x + xlen); xtemp++) {
                        if (xtemp == x) {
                            setCell(xtemp, ytemp, wall);
                        } else if (xtemp == (x + xlen - 1)) {
                            setCell(xtemp, ytemp, wall);
                        } else if (ytemp == (y - ylen / 2)) {
                            setCell(xtemp, ytemp, wall);
                        } else if (ytemp == (y + (ylen - 1) / 2)) {
                            setCell(xtemp, ytemp, wall);
                        } else {
                            setCell(xtemp, ytemp, floor);
                        }
                    }
                }

                break;

            case 2: // south

                for (int ytemp = y; ytemp < (y + ylen); ytemp++) {
                    if (ytemp < 0 || ytemp > ysize) {
                        return false;
                    }
                    for (int xtemp = (x - xlen / 2); xtemp < (x + (xlen + 1) / 2); xtemp++) {
                        if (xtemp < 0 || xtemp > xsize) {
                            return false;
                        }
                        if (getCell(xtemp, ytemp) != tileUnused) {
                            return false;
                        }
                    }
                }

                for (int ytemp = y; ytemp < (y + ylen); ytemp++) {
                    for (int xtemp = (x - xlen / 2); xtemp < (x + (xlen + 1) / 2); xtemp++) {
                        if (xtemp == (x - xlen / 2)) {
                            setCell(xtemp, ytemp, wall);
                        } else if (xtemp == (x + (xlen - 1) / 2)) {
                            setCell(xtemp, ytemp, wall);
                        } else if (ytemp == y) {
                            setCell(xtemp, ytemp, wall);
                        } else if (ytemp == (y + ylen - 1)) {
                            setCell(xtemp, ytemp, wall);
                        } else {
                            setCell(xtemp, ytemp, floor);
                        }
                    }
                }

                break;

            case 3: // west

                for (int ytemp = (y - ylen / 2); ytemp < (y + (ylen + 1) / 2); ytemp++) {
                    if (ytemp < 0 || ytemp > ysize) {
                        return false;
                    }
                    for (int xtemp = x; xtemp > (x - xlen); xtemp--) {
                        if (xtemp < 0 || xtemp > xsize) {
                            return false;
                        }
                        if (getCell(xtemp, ytemp) != tileUnused) {
                            return false;
                        }
                    }
                }

                for (int ytemp = (y - ylen / 2); ytemp < (y + (ylen + 1) / 2); ytemp++) {
                    for (int xtemp = x; xtemp > (x - xlen); xtemp--) {
                        if (xtemp == x) {
                            setCell(xtemp, ytemp, wall);
                        } else if (xtemp == (x - xlen + 1)) {
                            setCell(xtemp, ytemp, wall);
                        } else if (ytemp == (y - ylen / 2)) {
                            setCell(xtemp, ytemp, wall);
                        } else if (ytemp == (y + (ylen - 1) / 2)) {
                            setCell(xtemp, ytemp, wall);
                        } else {
                            setCell(xtemp, ytemp, floor);
                        }
                    }
                }

                break;
        }

        return true;
    }

    String showDungeon() {
        // used to print the map on the screen
        String dungeonMap = "";
        for (int y = 0; y < ysize; y++) {
            for (int x = 0; x < xsize; x++) {
                switch (getCell(x, y)) {
                    case tileUnused:
                        dungeonMap += " ";
                        break;
                    case tileDirtWall:
                        dungeonMap += "+";
                        break;
                    case tileDirtFloor:
                        dungeonMap += ".";
                        break;
                    case tileStoneWall:
                        dungeonMap += "O";
                        break;
                    case tileCorridor:
                        dungeonMap += "#";
                        break;
                    case tileDoor:
                        dungeonMap += "D";
                        break;
                    case tileUpStairs:
                        dungeonMap += "<";
                        break;
                    case tileDownStairs:
                        dungeonMap += ">";
                        break;
                    case tileChest:
                        dungeonMap += "*";
                        break;
                }
            }
            dungeonMap += "\n";
        }
        return dungeonMap;
    }

    public ImageIcon generateDungeonImage() {
        int dim = 10;
        BufferedImage img = new BufferedImage(xsize * dim, ysize * dim, BufferedImage.TYPE_INT_RGB);
        int[] pixels = new int[(xsize * dim) * (ysize * dim)];

        for (int y = 0; y < ysize; y++) {
            for (int x = 0; x < xsize; x++) {
                fill(pixels, x, y, dim);
            }
        }

        img.setRGB(0, 0, xsize * dim, ysize * dim, pixels, 0, xsize * dim);

        return new ImageIcon(img);
    }

    public void fill(int[] pixels, int x, int y, int dim) {
        for (int j = y * dim; j < (y + 1) * dim; j++) {
            for (int k = x * dim; k < (x + 1) * dim; k++) {
                int i = k + j * xsize * dim;
                switch (getCell(x, y)) {
                    case 0: // Unused
                        pixels[i] = Color.rgb565(54, 47, 45);
                        break;
                    case 1: // Dirt Wall
                        pixels[i] = Color.rgb565(96, 57, 19);
                        break;
                    case 2: // Dirt Floor
                        pixels[i] = Color.rgb565(166, 124, 82);
                        break;
                    case 3: // Stone Wall
                        pixels[i] = Color.rgb565(115, 99, 87);
                        break;
                    case 4: // Corridor
                        pixels[i] = Color.rgb565(123, 46, 0);
                        break;
                    case 5: // Door
                        pixels[i] = Color.rgb565(0, 118, 163);
                        break;
                    case 6: // Stairs Up
                        pixels[i] = Color.rgb565(100, 200, 100);
                        break;
                    case 7: // Stairs Down
                        pixels[i] = Color.rgb565(200, 100, 100);
                        break;
                    case 8: // Treasure Chest
                        pixels[i] = Color.rgb565(255, 247, 153);
                        break;
                }
            }
        }
    }

    public Texture peerGem(int cx, int cy) throws Exception {

        Texture t = null;

        int dim = 10;
        BufferedImage img = new BufferedImage(xsize * dim, ysize * dim, BufferedImage.TYPE_INT_RGB);
        int[] pixels = new int[(xsize * dim) * (ysize * dim)];

        for (int y = 0; y < ysize; y++) {
            for (int x = 0; x < xsize; x++) {
                if (x == cx && y == cy) {
                    for (int j = y * dim; j < (y + 1) * dim; j++) {
                        for (int k = x * dim; k < (x + 1) * dim; k++) {
                            int i = k + j * xsize * dim;
                            pixels[i] = Color.rgb565(255, 0, 0);
                        }
                    }
                } else {
                    fill(pixels, x, y, dim);
                }
            }
        }

        img.setRGB(0, 0, xsize * dim, ysize * dim, pixels, 0, xsize * dim);

        Pixmap p = Utils.createPixmap(img.getWidth(), img.getHeight(), img, 0, 0);

        t = new Texture(p);
        p.dispose();

        return t;

    }

}
