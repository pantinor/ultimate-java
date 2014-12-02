import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import java.awt.Color;

import objects.Tile;
import objects.TileSet;

public class PackImagesUtil {

	public static File dir = new File("D:\\xu4-source\\u4\\graphics\\png");

	public static void main2(String[] argv) throws Exception {

		File[] tileFiles = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().startsWith("tile_");
			}
		});
		
		Map<String,BufferedImage> imgMap = new HashMap<String,BufferedImage>();
		for (File file : tileFiles) {
			String name = file.getName().replace("tile_", "").replace(".png", "");
			BufferedImage input = ImageIO.read(file);
			imgMap.put(name, input);
		}

		File file2 = new File("target/classes/xml/tileset-base.xml");
		JAXBContext jaxbContext = JAXBContext.newInstance(TileSet.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		TileSet ts = (TileSet) jaxbUnmarshaller.unmarshal(file2);
		ts.setMaps();

		MaxRectsPacker mrp = new MaxRectsPacker();
		ArrayList<MaxRectsPacker.Rect> packedRects = new ArrayList<MaxRectsPacker.Rect>();
		
		
		for (Tile t : ts.getTiles()) {

			BufferedImage img = imgMap.get(t.getName());
			int height = img.getHeight();
			int frames = height / 16;

			for (int j = 0; j < frames; j++) {
				BufferedImage fr = img.getSubimage(0, j*16, 16, 16);
				MaxRectsPacker.Rect rect = new MaxRectsPacker.Rect(fr,0,0,16,16);
				rect.name = t.getName();
				rect.index = j;
				packedRects.add(rect);
			}
		}
		
		System.out.println("Writing: number of sprites: " +packedRects.size());
		
		ArrayList<MaxRectsPacker.Page> pages = mrp.pack(packedRects);
		mrp.writeImages(new File("."), pages, "tiles");
		mrp.writePackFile(new File("."), pages, "tile-atlas.txt");
		
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
	
	public static void main5(String[] argv) throws Exception {
		
		String inputFileName = "C:\\Users\\Paul\\Desktop\\nethack.gif";
		String outputFileName = "C:\\Users\\Paul\\Desktop\\roguelike-sprites.png";

		Color[] rgbs = {
				new Color(32,64,64),
				new Color(24,48,48),
				new Color(0,32,32),
				};
	
		ImageTransparency.convert(inputFileName, outputFileName, rgbs);		
		
		MaxRectsPacker mrp = new MaxRectsPacker();
		ArrayList<MaxRectsPacker.Rect> packedRects = new ArrayList<MaxRectsPacker.Rect>();
		int rows = 30;
		int cols = 30;
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
		
		mrp.writePackFileWithRects(new File("."), "roguelike-sprites-atlas.txt",packedRects, "roguelike-sprites.png");
		
	    System.out.println("done");
	
	}
	
	public static void main(String[] argv) throws Exception {
		MaxRectsPacker mrp = new MaxRectsPacker();
		ArrayList<MaxRectsPacker.Rect> packedRects = new ArrayList<MaxRectsPacker.Rect>();
		
		int tileWidth = 48;
		int tileHeight = 31;
		
		int[] dx = {0, 48+8, (48+8)*2, 176, 176+48, 176+48*2}; 
		int[] dy = {0, 31+1, 31*2 +2, 31*3+3, 31*4+4, 31*5+5}; 
		
		int rows = 6;
		int cols = 6;
		for (int i=0;i<rows;i++) {
			for (int j=0;j<cols;j++) {
				int xp = dx[i];
				int yp = dy[j];
				MaxRectsPacker.Rect rect = new MaxRectsPacker.Rect(xp,yp,tileWidth,tileHeight);
				rect.name = "beast";
				rect.index = 0;
				packedRects.add(rect);
			}
		}
		
		
		mrp.writePackFileWithRects(new File("."), "beasties-atlas.txt",packedRects, "beasties.png");
		
	    System.out.println("done");
	
	}

	
}
