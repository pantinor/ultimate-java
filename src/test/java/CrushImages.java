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
		crush("moonSheet.png", "moonPhases.png", 79, 79, 50, 50);
	}
}
