package test;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings;

public class PackImagesUtil {

	public static File dir = new File("D:\\xu4-source\\u4\\graphics\\png");

	public static void main(String[] argv) throws Exception {

		File[] files = new File[11];
		
		files[0] = new File("target\\classes\\graphics\\abacus.png");
		files[1] = new File("target\\classes\\graphics\\inside.png");
		files[2] = new File("target\\classes\\graphics\\outside.png");
		files[3] = new File("target\\classes\\graphics\\wagon.png");
		files[4] = new File("target\\classes\\graphics\\honcom.png");
		files[5] = new File("target\\classes\\graphics\\portal.png");
		files[6] = new File("target\\classes\\graphics\\spirhum.png");
		files[7] = new File("target\\classes\\graphics\\gypsy.png");
		files[8] = new File("target\\classes\\graphics\\sachonor.png");
		files[9] = new File("target\\classes\\graphics\\tree.png");
		files[10] = new File("target\\classes\\graphics\\valjus.png");

		
		Map<String,BufferedImage> imgMap = new HashMap<String,BufferedImage>();
		
		for (File file : files) {
			String name = file.getName();
			BufferedImage input = ImageIO.read(file);
			BufferedImage fr = input.getSubimage(0, 0, 320, 152);
			imgMap.put(name, fr);
		}

		Settings settings = new Settings();
		settings.maxWidth = 500;
		settings.maxHeight = 2000;
		settings.paddingX = 5;
		settings.paddingY = 5;
		settings.fast = false;
		MaxRectsPacker mrp = new MaxRectsPacker(settings);
		ArrayList<MaxRectsPacker.Rect> packedRects = new ArrayList<MaxRectsPacker.Rect>();
		
		
		for (String name : imgMap.keySet()) {
			BufferedImage img = imgMap.get(name);
			MaxRectsPacker.Rect rect = new MaxRectsPacker.Rect(img,0,0,320,152);
			rect.name = name;
			rect.index = 0;
			packedRects.add(rect);
		}
		
		BufferedImage tit = ImageIO.read(new File("target\\classes\\graphics\\title.png"));
		MaxRectsPacker.Rect rect = new MaxRectsPacker.Rect(tit,0,0,tit.getWidth(),tit.getHeight());
		rect.name = "title";
		rect.index = 0;
		packedRects.add(rect);
		
		BufferedImage abacus = ImageIO.read(new File("target\\classes\\graphics\\abacus.png"));
		BufferedImage wb = abacus.getSubimage(8, 187, 8, 12);
		BufferedImage bb = abacus.getSubimage(24, 187, 8, 12);
		
		MaxRectsPacker.Rect rect2 = new MaxRectsPacker.Rect(wb,0,0,8,12);
		rect2.name = "white-bead";
		rect2.index = 0;
		packedRects.add(rect2);
		
		MaxRectsPacker.Rect rect3 = new MaxRectsPacker.Rect(bb,0,0,8,12);
		rect3.name = "black-bead";
		rect3.index = 0;
		packedRects.add(rect3);
		
		
		System.out.println("Writing: number of sprites: " +packedRects.size());
		
		ArrayList<MaxRectsPacker.Page> pages = mrp.pack(packedRects);
		mrp.writeImages(new File("."), pages, "initial-screens");
		mrp.writePackFile(new File("."), pages, "initial-atlas.txt");
		
	    System.out.println("done");
		


	}
	
	public static void main3(String[] argv) throws Exception {
		MaxRectsPacker mrp = new MaxRectsPacker();
		ArrayList<MaxRectsPacker.Rect> packedRects = new ArrayList<MaxRectsPacker.Rect>();
		int rows = 2;
		int cols = 4;
		int w = 50;
		for (int i=0;i<rows;i++) {
			for (int j=0;j<cols;j++) {
				MaxRectsPacker.Rect rect = new MaxRectsPacker.Rect(i*w,j*w,w,w);
				rect.name = "phase";
				rect.index = 0;
				packedRects.add(rect);
			}
		}
		
		System.out.println("Writing: number of sprites: " +packedRects.size());
		
		mrp.writePackFileWithRects(new File("."), "moon-atlas.txt",packedRects, "moonPhases.png");
		
	    System.out.println("done");
	
	}
	
	public static void main4(String[] argv) throws Exception {
		
		String inputFileName = "C:\\Users\\Paul\\Desktop\\ultima_v_5_warriors_of_destiny_tileset.png";
		String outputFileName = "C:\\Users\\Paul\\Desktop\\ultima_5_tileset.png";

	
		//ImageTransparency.convert(inputFileName, outputFileName);		
		
		MaxRectsPacker mrp = new MaxRectsPacker();
		ArrayList<MaxRectsPacker.Rect> packedRects = new ArrayList<MaxRectsPacker.Rect>();
		int rows = 16;
		int cols = 32;
		int w = 32;
		for (int i=0;i<rows;i++) {
			for (int j=0;j<cols;j++) {
				MaxRectsPacker.Rect rect = new MaxRectsPacker.Rect(i*w,j*w,w,w);
				rect.name = "phase";
				rect.index = 0;
				packedRects.add(rect);
			}
		}
		
		System.out.println("Writing: number of sprites: " +packedRects.size());
		
		mrp.writePackFileWithRects(new File("."), "ultima5-atlas.txt",packedRects, "ultima_5_tileset.png");
		
	    System.out.println("done");
	
	}
	

	


	
}
