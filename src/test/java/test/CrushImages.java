package test;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

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
		BufferedImage input = ImageIO.read(new File("target\\classes\\graphics\\beasties.png"));
		
		int tileWidth = 48;
		int tileHeight = 31;
		
		int[] dx = {0, 48+8, (48+8)*2, 176, 176+48, 176+48*2}; 
		int[] dy = {0, 31+1, 31*2 +2, 31*3+3, 31*4+4, 31*5+5}; 

		BufferedImage output = new BufferedImage(tileWidth * 6, tileHeight * 6, BufferedImage.TYPE_INT_ARGB);

		for (int x = 0; x < dx.length; x++) {
			for (int y = 0; y < dy.length; y++) {
				int xp = dx[x];
				int yp = dy[y];

				BufferedImage tile = input.getSubimage(xp, yp, tileWidth, tileHeight);
				output.getGraphics().drawImage(tile, x * tileWidth, y * tileHeight, null);
			}
		}
		
		ImageIO.write(output, "PNG", new File("beasties.png"));
	}
	
	
	
}
