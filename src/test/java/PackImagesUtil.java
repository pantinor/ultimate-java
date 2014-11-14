import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

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
	
	public static void main(String[] argv) throws Exception {
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

	
}
