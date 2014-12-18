package test;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.badlogic.gdx.tools.texturepacker.MaxRectsPacker;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings;

public class AtlasWriter extends MaxRectsPacker {
		
	public AtlasWriter(Settings settings) {
		super(settings);
	}

	public void writePackFileWithRects(File outputDir, String packFileName, List<Rect> rects, String imageName) throws IOException {
		File packFile = new File(outputDir, packFileName);
		if (packFile.exists()) packFile.delete();

		FileWriter writer = new FileWriter(packFile, true);
		
		writer.write(imageName + "\n");
		writer.write("format: RGBA8888\n");
		writer.write("filter: Nearest,Nearest\n");
		writer.write("repeat: none\n");

		for (Rect rect : rects) {
			String rectName = rect.name;
			writer.write(rectName + "\n");
			writer.write("  rotate: false\n");
			writer.write("  xy: " + rect.x + ", " + rect.y + "\n");
			writer.write("  size: " + rect.width + ", " + rect.height + "\n");
			writer.write("  orig: " + rect.width + ", " + rect.height + "\n");
			writer.write("  offset: 0, 0\n");
			writer.write("  index: " + rect.index + "\n");
		}
		
		writer.close();
	}
	
	public static class Rect {
		public String name;
		public int x, y;
		public int width, height;
		public int index = 0;
		public Rect(int x, int y, int width, int height) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}
	}

}