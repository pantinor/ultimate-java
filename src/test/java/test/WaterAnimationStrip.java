/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class WaterAnimationStrip {

    public static void main(String[] args) {
        String inputFileName = "tmx/ultima-ega.png";
        String outputFileName = "tmx/animation_strip5.png";

        int startX = 192;
        int startY = 64;
        int tileSize = 16;
        int frameCount = 8;
        int shiftPerFrame = 2; // shift downward by 2 pixels each frame

        try {
            BufferedImage source = ImageIO.read(new File(inputFileName));
            if (source == null) {
                System.err.println("Could not read input image: " + inputFileName);
                return;
            }

            // Validate bounds
            if (startX < 0 || startY < 0
                    || startX + tileSize > source.getWidth()
                    || startY + tileSize > source.getHeight()) {
                System.err.println("Requested 16x16 tile is outside the bounds of the source image.");
                return;
            }

            // Extract the original 16x16 tile
            BufferedImage tile = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < tileSize; y++) {
                for (int x = 0; x < tileSize; x++) {
                    int argb = source.getRGB(startX + x, startY + y);
                    tile.setRGB(x, y, argb);
                }
            }

            // Output strip: 8 frames in a row => 128 wide, 16 high
            BufferedImage output = new BufferedImage(tileSize * frameCount, tileSize, BufferedImage.TYPE_INT_ARGB);

            // Create 8 shifted frames
            for (int frame = 0; frame < frameCount; frame++) {
                int frameOffsetX = frame * tileSize;
                int shift = (frame * shiftPerFrame) % tileSize;

                for (int y = 0; y < tileSize; y++) {
                    for (int x = 0; x < tileSize; x++) {
                        // Shift downward by 'shift' pixels, wrapping around
                        int srcY = (y - shift + tileSize) % tileSize;
                        int argb = tile.getRGB(x, srcY);
                        output.setRGB(frameOffsetX + x, y, argb);
                    }
                }
            }

            ImageIO.write(output, "png", new File(outputFileName));
            System.out.println("Saved: " + outputFileName);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
