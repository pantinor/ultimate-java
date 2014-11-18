import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import objects.BaseMap;
import objects.MapSet;
import objects.Tile;
import objects.TileSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import ultima.Constants;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Region;


public class DungeonMapTmxConvert implements Constants {

	private String tilesetName;
	private String imageSource;
	private int mapWidth;
	private int mapHeight;
	private int tileWidth;
	private int tileHeight;
	private String data;
	private List<String> layers;
	
	

	
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
		for (BaseMap map : ms.getMaps()) {
			String tmxmapname = null;
			boolean isAbyss = false;
			if (map.getType().equals("dungeon")) {
				if (map.getFname().equals("abyss.dng")) {
					tmxmapname = "abyss.tmx";
					isAbyss = true;
				} else {
					tmxmapname = "dungeon_" + map.getId() + ".tmx";
				}
			} else {
				continue;
			}

			InputStream is = TestMain.class.getResourceAsStream("/data/"+map.getFname());
			byte[] bytes = IOUtils.toByteArray(is);	
			
			List<DungeonTile[]> dungeonTiles = new ArrayList<DungeonTile[]>();
			int pos = 0 ;
			for (int i = 0;i<8;i++) {
				DungeonTile[] tiles = new DungeonTile[64];
				for (int y = 0; y < map.getHeight(); ++y) {
					for (int x = 0; x < map.getWidth(); ++x) {
						int index = bytes[pos] & 0xff;
						pos ++;
						DungeonTile tile = DungeonTile.getTileByValue(index);
						tiles[x + y * map.getWidth()] = tile;
					}
				}
				dungeonTiles.add(tiles);
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
			
			//map layers
			List<String> layers = new ArrayList<String>();
			for (int j = 0;j<8;j++) {
				DungeonTile[] tiles = dungeonTiles.get(j);
				StringBuffer data = new StringBuffer();
				int count = 1;
				int total = 1;
				for (int i=0;i<tiles.length;i++) {
					DungeonTile t = tiles[i];
					data.append(findTileId(mapTileIds,t.getTileName()) + ",");
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
				layers.add(d);
			}
			
			System.out.println("writing");
			
			DungeonMapTmxConvert c = new DungeonMapTmxConvert(map.getFname(),"tiles.png",map.getWidth(),map.getHeight(),
					TILE_SIZE,TILE_SIZE,TILE_SIZE^2,TILE_SIZE^2, layers);
			
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
	
	
	private DungeonMapTmxConvert(String tilesetName, String imageSource, int mapWidth, int mapHeight, 
			int tileWidth, int tileHeight, int tilesetWidth, int tilesetHeight, List<String> layers) {
		this.tilesetName = tilesetName;
		this.imageSource = imageSource;
		this.mapWidth = mapWidth;
		this.mapHeight = mapHeight;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
		this.data = data;
		this.layers = layers;
	}


	@Override
	public String toString() {
		
//		StringBuffer mpsString = new StringBuffer();
//		if (monsterPositions != null) {
//			for (Position pos : monsterPositions) {
//				if (pos==null) continue;
//				mpsString.append(pos.toString());
//			}
//		}
		
		
		
		String template = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<map version=\"1.0\" orientation=\"orthogonal\" width=\"%s\" height=\"%s\" tilewidth=\"%s\" tileheight=\"%s\" backgroundcolor=\"#000000\">\n" +
						"<tileset firstgid=\"1\" name=\"%s\" tilewidth=\"%s\" tileheight=\"%s\">\n" + 
						"<image source=\"%s\" width=\"256\" height=\"256\"/>\n</tileset>\n" + 
		
						"<layer name=\"Dungeon Layer 1\" width=\"%s\" height=\"%s\">\n<data encoding=\"csv\">\n%s\n</data>\n</layer>\n" + 
						"<layer name=\"Dungeon Layer 2\" width=\"%s\" height=\"%s\">\n<data encoding=\"csv\">\n%s\n</data>\n</layer>\n" + 
						"<layer name=\"Dungeon Layer 3\" width=\"%s\" height=\"%s\">\n<data encoding=\"csv\">\n%s\n</data>\n</layer>\n" + 
						"<layer name=\"Dungeon Layer 4\" width=\"%s\" height=\"%s\">\n<data encoding=\"csv\">\n%s\n</data>\n</layer>\n" + 
						"<layer name=\"Dungeon Layer 5\" width=\"%s\" height=\"%s\">\n<data encoding=\"csv\">\n%s\n</data>\n</layer>\n" + 
						"<layer name=\"Dungeon Layer 6\" width=\"%s\" height=\"%s\">\n<data encoding=\"csv\">\n%s\n</data>\n</layer>\n" + 
						"<layer name=\"Dungeon Layer 7\" width=\"%s\" height=\"%s\">\n<data encoding=\"csv\">\n%s\n</data>\n</layer>\n" + 
						"<layer name=\"Dungeon Layer 8\" width=\"%s\" height=\"%s\">\n<data encoding=\"csv\">\n%s\n</data>\n</layer>\n" + 

						//"<objectgroup name=\"Monster Positions\" width=\"%s\" height=\"%s\">\n%s\n</objectgroup>\n" +

						"</map>";
		
		return String.format(template,  mapWidth, mapHeight, tileWidth, tileHeight, 
				tilesetName, tileWidth, tileHeight, 
				imageSource, 
				
				mapWidth, mapHeight, layers.get(0),
				mapWidth, mapHeight, layers.get(1),
				mapWidth, mapHeight, layers.get(2),
				mapWidth, mapHeight, layers.get(3),
				mapWidth, mapHeight, layers.get(4),
				mapWidth, mapHeight, layers.get(5),
				mapWidth, mapHeight, layers.get(6),
				mapWidth, mapHeight, layers.get(7)

				);		
		
	}	
	
	

		

}
