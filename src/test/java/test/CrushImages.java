package test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

public class CrushImages {

    private static void crush(String in, String out, int inWidth, int inHeight, int outWidth, int outHeight) throws IOException {
        BufferedImage input = ImageIO.read(new File(in));
        int tilesWidth = Math.round(input.getWidth() / inWidth);
        int tilesHeight = Math.round(input.getHeight() / inHeight);

        BufferedImage output = new BufferedImage(tilesWidth * outWidth, tilesHeight * outHeight, BufferedImage.TYPE_INT_ARGB);
        int xoffset = (inWidth - outWidth) / 2;
        int yoffset = (inHeight - outHeight) / 2;
        for (int x = 0; x < tilesWidth; x++) {
            for (int y = 0; y < tilesHeight; y++) {
                int xp = (x * inWidth) + xoffset;
                int yp = (y * inHeight) + yoffset;

                BufferedImage tile = input.getSubimage(xp, yp, outWidth, outHeight);
                output.getGraphics().drawImage(tile, x * outWidth, y * outHeight, null);
            }
        }
        System.out.println("Writing: " + out);
        ImageIO.write(output, "PNG", new File(out));
    }

    public static void main(String[] argv) throws IOException {
        //crush("moonSheet.png", "moonPhases.png", 79, 79, 50, 50);
        makeBeastieAtlas();
    }

    private static void makeBeastieAtlas() throws IOException {
        BufferedImage inv = ImageIO.read(new File("C:\\Users\\Paul\\Documents\\water\\44f625cd-ff72-424b-9407-63af2a87f6d9_scaled.jpg"));
        BufferedImage skills = ImageIO.read(new File("C:\\Users\\Paul\\Documents\\water\\00c04382-a5d4-4268-b3ec-92119fd9861d_scaled.jpg"));
        BufferedImage weaparmor = ImageIO.read(new File("C:\\Users\\Paul\\Documents\\water\\14a0b639-12b7-4d91-b6e9-fe38153d3e0c_scaled.jpg"));
        BufferedImage weapons = ImageIO.read(new File("C:\\Users\\Paul\\Documents\\water\\2232e149-1a4e-4871-8cb9-38a9493ff61b_scaled.jpg"));
        BufferedImage food = ImageIO.read(new File("C:\\Users\\Paul\\Documents\\water\\cb5d5ca6-3fa9-45e0-a9ba-f5c027d5c94a_scaled.jpg"));
        BufferedImage potions = ImageIO.read(new File("C:\\Users\\Paul\\Documents\\water\\6a9729a6-0375-406e-9f4c-006d4efa3d23_scaled.jpg"));
        BufferedImage herbs = ImageIO.read(new File("C:\\Users\\Paul\\Documents\\water\\0fa58e7c-aec4-4bd8-94fb-b623da4b5766_scaled.jpg"));
        BufferedImage armor = ImageIO.read(new File("C:\\Users\\Paul\\Documents\\water\\dd7187e6-6dc4-45ff-8ecc-1386afaa6b7b_scaled.jpg"));

        BufferedImage[] images = new BufferedImage[]{skills,weapons,weaparmor,armor,inv,potions,herbs,food};

        int tileWidth = 44;
        int tileHeight = 44;

        int[] xoff1 = new int[]{14, 72, 131, 189, 248, 306, 364, 424, 481, 539, 597, 656};
        int[] yoff1 = new int[]{12, 71, 129, 187, 246, 304, 362, 421};

        int[] xoff2 = new int[]{4, 51, 98, 145, 192, 239, 286, 333, 379, 426, 473, 520, 566};
        int[] yoff2 = new int[]{5, 52, 100, 147, 195, 242, 291, 337, 385, 432};

        int[][][] offsets = new int[][][]{
            {xoff1, yoff1},
            {xoff1, yoff1},
            {xoff1, yoff1},
            {xoff2, yoff2},
            {xoff1, yoff1},
            {xoff1, yoff1},
            {xoff1, yoff1},
            {xoff1, yoff1},};

        List<BufferedImage> items = new ArrayList<>();

        for (int i = 0; i < images.length; i++) {
            BufferedImage im = images[i];
            System.out.println("Starting " + i);
            for (int y = 0; y < offsets[i][1].length; y++) {
                for (int x = 0; x < offsets[i][0].length; x++) {
                    BufferedImage tile = im.getSubimage(offsets[i][0][x], offsets[i][1][y], tileWidth, tileHeight);
                    items.add(tile);
                }
            }
        }

        int y = 0;
        try {
            Iterator<BufferedImage> iter = items.iterator();
            while (iter.hasNext()) {
                y += tileHeight;
                for (int x = 0; x < 12; x++) {
                    iter.next();
                }
            }
        } catch (Exception e) {
        }

        BufferedImage output = new BufferedImage(tileWidth * 12, y, BufferedImage.TYPE_INT_ARGB);

        try {
            y = 0;
            Iterator<BufferedImage> iter = items.iterator();
            while (iter.hasNext()) {
                for (int x = 0; x < 12; x++) {
                    output.getGraphics().drawImage(iter.next(), x * tileWidth, y, null);
                }
                y += tileHeight;
            }
        } catch (Exception e) {
        }

        ImageIO.write(output, "PNG", new File("inventory.png"));
    }

}
