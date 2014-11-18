import java.io.File;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import objects.BaseMap;
import objects.MapSet;
import objects.Person;
import objects.Tile;
import objects.TileSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import ultima.Constants.ObjectMovementBehavior;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Region;


public class CombatMapTmxConvert {

	private String tilesetName;
	private String imageSource;
	private int mapWidth;
	private int mapHeight;
	private int tileWidth;
	private int tileHeight;
	private String data;
	private Position[] monsterPositions;
	private Position[] playerPositions;


	
	public static void main(String[] args) throws Exception {
		
		File file2 = new File("target/classes/xml/tileset-base.xml");
		JAXBContext jaxbContext = JAXBContext.newInstance(TileSet.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		TileSet ts = (TileSet) jaxbUnmarshaller.unmarshal(file2);
		ts.setMaps();

		File file3 = new File("target/classes/xml/maps.xml");
		jaxbContext = JAXBContext.newInstance(MapSet.class);
		jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		MapSet ms = (MapSet) jaxbUnmarshaller.unmarshal(file3);
		ms.init(ts);
		
		int TILE_SIZE = 16;
		boolean foundShrine = false;
		for (BaseMap map : ms.getMaps()) {
			String tmxmapname = null;
			boolean isShrine = false;

			if (map.getType().equals("combat") || (map.getType().equals("shrine") && !foundShrine)) {
				if (map.getType().equals("shrine") && !foundShrine) {
					foundShrine = true;
					tmxmapname = "shrine.tmx";
					isShrine = true;
				} else if (map.getType().equals("combat")) {
					tmxmapname = "combat_" + map.getId() + ".tmx";
				}
			} else {
				continue;
			}

			InputStream is = TestMain.class.getResourceAsStream("/data/"+map.getFname());
			byte[] bytes = IOUtils.toByteArray(is);	
			
			Tile[] tiles = new Tile[map.getWidth() * map.getHeight()];
			int pos = isShrine?0:64;
			for (int y = 0; y < map.getHeight(); ++y) {
				for (int x = 0; x < map.getWidth(); ++x) {
					int index = bytes[pos] & 0xff; // convert a byte to an unsigned int value
					pos ++;
					Tile tile = ts.getTileByIndex(index);
					if (tile == null) {
						System.out.println("Tile index cannot be found: " + index + " using index 129 for black space.");
						tile = ts.getTileByIndex(129);
					}
					tiles[x + y * map.getWidth()] = tile;
				}
			}

			       		
			
			//load the atlas and determine the tile indexes per tilemap position
			FileHandle f = new FileHandle("target/classes/tilemaps/tile-atlas.txt");
			TextureAtlasData atlas = new TextureAtlasData(f, f.parent(), false);
			Tile[] mapTileIds = new Tile[atlas.getRegions().size+1];
			for (Region r : atlas.getRegions()) {
				int x = r.left / r.width;
				int y = r.top / r.height;
				int i = y*TILE_SIZE+x+1;
				mapTileIds[i] = ts.getTileByName(r.name);
			}
			
			//map layer
			StringBuffer data = new StringBuffer();
			int count = 1;
			int total = 1;
			for (int i=0;i<tiles.length;i++) {
				Tile t = tiles[i];
				data.append(findTileId(mapTileIds,t.getName()) + ",");
				count++;total++;
				if (count > map.getWidth()) {
					data.append("\n");
					count = 1;
				}
				if (total > map.getWidth() * map.getHeight()) {
					break;
				}
			}
			
			String d = data.toString();
			d = d.substring(0,d.length()-2);
			
			Position[] monPos = new Position[16];
			for (int i=0;i<16;i++) {
				monPos[i] = new Position(i, (int)bytes[i], 0);
			}
			for (int i=0;i<16;i++) {
				monPos[i].startY = (int)bytes[i+16];
			}
			
			Position[] playerPos = new Position[8];
			for (int i=0;i<8;i++) {
				playerPos[i] = new Position(i, (int)bytes[i+32], 0);
			}
			for (int i=0;i<8;i++) {
				playerPos[i].startY = (int)bytes[i+40];
			}
			
			CombatMapTmxConvert c = new CombatMapTmxConvert(map.getFname(),"tiles.png",map.getWidth(),map.getHeight(),
					TILE_SIZE,TILE_SIZE,TILE_SIZE^2,TILE_SIZE^2,d, monPos, playerPos);
			FileUtils.writeStringToFile(new File("src/main/resources/tilemaps/" + tmxmapname), c.toString());
		}

	}
	
	private static int findTileId(Tile[] tiles, String name) {
		for (int i=1;i<tiles.length;i++) {
			if (tiles[i] == null) continue;
			if (StringUtils.equals(tiles[i].getName(),name)) 
				return i;
		}
		return 0;
	}
	
	
	private CombatMapTmxConvert(String tilesetName, String imageSource, int mapWidth, int mapHeight, 
			int tileWidth, int tileHeight, int tilesetWidth, int tilesetHeight, String data, Position[] mps, Position[] pps) {
		this.tilesetName = tilesetName;
		this.imageSource = imageSource;
		this.mapWidth = mapWidth;
		this.mapHeight = mapHeight;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
		this.data = data;
		this.monsterPositions = mps;
		this.playerPositions = pps;
	}


	@Override
	public String toString() {
		
		StringBuffer mpsString = new StringBuffer();
		if (monsterPositions != null) {
			for (Position pos : monsterPositions) {
				if (pos==null) continue;
				mpsString.append(pos.toString());
			}
		}
		
		StringBuffer ppsString = new StringBuffer();
		if (playerPositions != null) {
			for (Position pos : playerPositions) {
				if (pos==null) continue;
				ppsString.append(pos.toString());
			}
		}
		
		
		String template = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<map version=\"1.0\" orientation=\"orthogonal\" width=\"%s\" height=\"%s\" tilewidth=\"%s\" tileheight=\"%s\" backgroundcolor=\"#000000\">\n" +
						"<tileset firstgid=\"1\" name=\"%s\" tilewidth=\"%s\" tileheight=\"%s\">\n" + 
						"<image source=\"%s\" width=\"256\" height=\"256\"/>\n</tileset>\n" + 
		
						"<layer name=\"Map Layer\" width=\"%s\" height=\"%s\">\n" + 
						"<data encoding=\"csv\">\n%s\n</data>\n</layer>\n" + 
						
						"<objectgroup name=\"Monster Positions\" width=\"%s\" height=\"%s\">\n%s\n</objectgroup>\n" +
						"<objectgroup name=\"Player Positions\" width=\"%s\" height=\"%s\">\n%s\n</objectgroup>\n" +

						"</map>";
		
		return String.format(template,  mapWidth, mapHeight, tileWidth, tileHeight, 
				tilesetName, tileWidth, tileHeight, 
				imageSource, 
				mapWidth, mapHeight, data,
				mapWidth, mapHeight, mpsString.toString(),	
				mapWidth, mapHeight, ppsString.toString());		
		
	}	
	
	
	public static class Position {
		int index;
		int startX;
		int startY;

		private Position(int index, int startX, int startY) {
			this.index = index;
			this.startX = startX;
			this.startY = startY;
		}

		@Override
		public String toString() {
			
			String template = "<object name=\"position\" type=\"position\" x=\"%s\" y=\"%s\" width=\"16\" height=\"16\">\n"+
								"<properties>\n"+
								"<property name=\"index\" value=\"%s\"/>\n"+
								"<property name=\"startX\" value=\"%s\"/>\n"+
								"<property name=\"startY\" value=\"%s\"/>\n"+
								"</properties>\n"+
								"</object>\n";
			
			return String.format(template, startX*16, startY*16, index, startX, startY);
		}
		
	}
		

}
