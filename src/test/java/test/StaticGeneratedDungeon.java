package test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import util.Utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

public class StaticGeneratedDungeon {

	public static final int DIM = 35;
	private Key[] dungeon_map = new Key[DIM * DIM];

	public enum Key {
		NULL("|","water"),
		U(" ","brick_wall"),
		F("-","brick_floor"),
		
		DST("a","secret_door"),
		DSB("b","secret_door"),
		DSL("c","secret_door"),
		DSR("d","secret_door"),
		
		DPT("e","locked_door"),
		DPB("f","locked_door"),
		DPL("g","locked_door"),
		DPR("h","locked_door"),
		
		DL("i","door"),
		DR("j","door"),
		DT("k","door"),
		DB("l","door"),
		
		SU("m","up_ladder"),
		SD("n","down_ladder");

		
		private String id;
		private String name;
		private Key(String id, String name) {
			this.id = id;
			this.name = name;
		}
		public String getId() {
			return id;
		}
		public String getName() {
			return name;
		}
		public static Key get(char id) {
			for (Key m : Key.values()) {
				if (m.getId().charAt(0) == id)
					return m;
			}
			return null;
		}
	}

	public static void main(String[] args) {
		StaticGeneratedDungeon sd = new StaticGeneratedDungeon("The Forsaken Delve of Sorrows 01 (tsv).txt");
		for (int y = 0; y < DIM; y++) {
			for (int x = 0; x < DIM; x++) {
				Key val = sd.getCell(x, y);
				if (val == null) {
					System.out.println(""+x+" "+y);
				}
			}
		}
	}

	public StaticGeneratedDungeon(String file) {

		try {
			@SuppressWarnings("unchecked")
			List<String> lines = FileUtils.readLines(new File(file));
			List<String> newLines = new ArrayList<String>();
			for (String line : lines) {
				for (Key k : Key.values()) {
					line = line.replace(k.toString() + "\t", k.getId());
				}
				line = line.replace("\t", " ");
				newLines.add(line + " ");
			}
			
			int x = 0;
			int y = 0;
			for (String line : newLines) {
				for (char c : line.toCharArray()) {
					setCell(x,y,c);
					x++;
				}
				x = 0;
				y++;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setCell(int x, int y, char c) {
		dungeon_map[x + DIM * y] = Key.get(c);
	}

	public Key getCell(int x, int y) {
		return dungeon_map[x + DIM * y];
	}
	
	public Texture peerGem(int cx, int cy) throws Exception {

		Texture t = null;

		int dim = 10;
		BufferedImage img = new BufferedImage(DIM * dim, DIM * dim, BufferedImage.TYPE_INT_RGB);
		int[] pixels = new int[(DIM * dim) * (DIM * dim)];

		for (int y = 0; y < DIM; y++) {
			for (int x = 0; x < DIM; x++) {
				if (x == cx && y == cy) {
					for (int j = y * dim; j < (y + 1) * dim; j++) {
						for (int k = x * dim; k < (x + 1) * dim; k++) {
							int i = k + j * DIM * dim;
							pixels[i] = Color.rgb565(255,0,0);
						}
					}
				} else {
					fill(pixels, x, y, dim);
				}
			}
		}

		img.setRGB(0, 0, DIM * dim, DIM * dim, pixels, 0, DIM * dim);

		Pixmap p = Utils.createPixmap(img.getWidth(), img.getHeight(), img, 0, 0);

		t = new Texture(p);
		p.dispose();

		return t;

	}
	
	public void fill(int[] pixels, int x, int y, int dim) {
		for (int j = y * dim; j < (y + 1) * dim; j++) {
			for (int k = x * dim; k < (x + 1) * dim; k++) {
				int i = k + j * DIM * dim;
				switch (getCell(x, y)) {
				case DB:
				case DT:
				case DL:
				case DR:
					pixels[i] = Color.YELLOW.toIntBits();
					break;
				case DPB:
				case DPL:
				case DPR:
				case DPT:
					pixels[i] = Color.GREEN.toIntBits();
					break;	
				case DSB:
				case DSL:
				case DSR:
				case DST:
					pixels[i] = Color.DARK_GRAY.toIntBits();
					break;
				case F:
					pixels[i] = Color.BLUE.toIntBits();
					break;
				case U:
					pixels[i] = Color.GRAY.toIntBits();
					break;
				case SU:
					pixels[i] = Color.ORANGE.toIntBits();
					break;
				case SD:
					pixels[i] = Color.MAGENTA.toIntBits();
					break;
				}
			}
		}
	}

}
