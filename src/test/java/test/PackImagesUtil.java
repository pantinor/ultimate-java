package test;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings;

public class PackImagesUtil {

	public static File dir = new File("D:\\xu4-source\\u4\\graphics\\png");
	
	public static List<File> listFileTree(File dir) {
		List<File> fileTree = new ArrayList<File>();
	    for (File entry : dir.listFiles()) {
	        if (entry.isFile()) fileTree.add(entry);
	        else fileTree.addAll(listFileTree(entry));
	    }
	    return fileTree;
	}
	
	public static void main2(String[] argv) throws Exception {

		List<File> files = listFileTree(new File("C:\\Users\\Paul\\Desktop\\crawl-tiles Oct-5-2010\\dc-mon"));
		Collections.sort(files);
		
		Map<String,BufferedImage> imgMap = new TreeMap<String,BufferedImage>();
		
		for (File file : files) {
			if (file.isDirectory() || !file.getName().endsWith("png")) continue;
			String name = file.getName();
			BufferedImage fr = ImageIO.read(file);
			imgMap.put(name, fr);
		}

		Settings settings = new Settings();
		settings.maxWidth = 768;
		settings.maxHeight = 768;
		settings.paddingX = 0;
		settings.paddingY = 0;
		settings.fast = true;
		settings.pot = false;
		settings.grid = true;

		TexturePacker tp = new TexturePacker(settings);
		
		for (String name : imgMap.keySet()) {
			BufferedImage image = imgMap.get(name);
			tp.addImage(image, name);
		}
		
		
		System.out.println("Writing: number of res: " + imgMap.size());
		
		tp.pack(new File("."), "utumno-mon");
		
	    System.out.println("done");
		


	}
	
	public static void main(String[] argv) throws Exception {

		List<File> files = listFileTree(new File("C:\\Users\\Paul\\Desktop\\crawl-tiles Oct-5-2010\\Monsters"));
		Collections.sort(files);
		
		List<File> files2 = listFileTree(new File("C:\\Users\\Paul\\Desktop\\crawl-tiles Oct-5-2010\\dc-mon"));
		Collections.sort(files2);

		Map<String,BufferedImage> imgMap = new TreeMap<String,BufferedImage>();
		
		for (File file : files) {
			if (file.isDirectory() || !file.getName().endsWith("PNG")) continue;
			String name = file.getName();
			BufferedImage fr = ImageIO.read(file);
			BufferedImage sub = fr.getSubimage(11, 11, 32, 32);
			imgMap.put(name, sub);
		}
		
		for (File file : files2) {
			if (file.isDirectory() || !file.getName().endsWith("png")) continue;
			String name = file.getName();
			BufferedImage fr = ImageIO.read(file);
			imgMap.put(name, fr);
		}
		
		System.out.println("Writing: number of images: " + imgMap.size());


		Settings settings = new Settings();
		settings.maxWidth = 1440;
		settings.maxHeight = 2048;
		settings.paddingX = 0;
		settings.paddingY = 0;
		settings.fast = true;
		settings.pot = false;
		settings.grid = true;

		TexturePacker tp = new TexturePacker(settings);
		for (String name : imgMap.keySet()) {
			BufferedImage image = imgMap.get(name);
			tp.addImage(image, name);
		}
		tp.pack(new File("."), "monsters");
		
		java.awt.Color[] col = {java.awt.Color.MAGENTA};
		ImageTransparency.convert("monsters.png", "monsters-trans.png", col);
		
	    System.out.println("done");
		


	}

	

	


	
}
