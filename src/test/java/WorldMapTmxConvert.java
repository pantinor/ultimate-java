import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import objects.BaseMap;
import objects.Label;
import objects.MapSet;
import objects.Moongate;
import objects.Portal;
import objects.Tile;
import objects.TileSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import ultima.Constants;
import ultima.Utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Region;

public class WorldMapTmxConvert {

	private String tilesetName;
	private String imageSource;
	private int mapWidth;
	private int mapHeight;
	private int tileWidth;
	private int tileHeight;
	
	private String dataLayer;
	private String portalLayer;
	private String moongateLayer;

	private List<Portal> portalObjects;
	private List<Moongate> moongateObjects;
	private List<Label> labelObjects;

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
		ms.setMapTable();

		int TILE_SIZE = 16;

		BaseMap map = ms.getMapById(0);
		Utils.setMapTiles(map, ts);
		Tile[] tiles = map.getTiles();


		// load the atlas and determine the tile indexes per tilemap position
		FileHandle f = new FileHandle("target/classes/tilemaps/tile-atlas.txt");
		TextureAtlasData atlas = new TextureAtlasData(f, f.parent(), false);
		Tile[] mapTileIds = new Tile[atlas.getRegions().size + 1];
		for (Region r : atlas.getRegions()) {
			int x = r.left / r.width;
			int y = r.top / r.height;
			int i = y * TILE_SIZE + x + 1;
			mapTileIds[i] = ts.getTileByName(r.name);
		}

		// map layer
		StringBuffer data = new StringBuffer();
		int count = 1;
		int total = 1;
		for (int i = 0; i < tiles.length; i++) {
			Tile t = tiles[i];
			data.append(findTileId(mapTileIds, t.getName()) + ",");
			count++;
			total++;
			if (count > map.getWidth()) {
				data.append("\n");
				count = 1;
			}
			if (total > map.getWidth() * map.getHeight()) {
				break;
			}
		}

		String dl = data.toString();
		dl = dl.substring(0, dl.length() - 2);

		// portal layer
		List<Portal> portals = map.getPortals();
		StringBuffer portalBuffer = new StringBuffer();
		
		//set map tile id per dest map type
		for (Portal p : portals) {
			BaseMap destMap = ms.getMapById(p.getDestmapid());
			p.setName(Constants.Maps.convert(p.getDestmapid()).toString());
			String ttype = destMap.getCity()==null?destMap.getType():destMap.getCity().getType();
			p.setMapTileId(findTileId(mapTileIds, ttype));
		}
		
		if (portals != null) {
			for (int y = 0; y < map.getHeight(); y++) {
				for (int x = 0; x < map.getWidth(); x++) {
					Portal p = findPortalAtCoords(portals, x, y);
					if (p == null) {
						portalBuffer.append("0,");
					} else {
						portalBuffer.append(p.getMapTileId() + ",");
					}
				}
				portalBuffer.append("\n");
			}
		}

		String pl = portalBuffer.toString();
		pl = pl.substring(0, pl.length() - 2);
		
		
		// moongate layer
		List<Moongate> moongates = map.getMoongates();
		StringBuffer moongateBuffer = new StringBuffer();
		
		//set map tile id per dest map type
		for (Moongate m : moongates) {
			m.setMapTileId(findTileId(mapTileIds, "moongate"));
		}
		
		if (moongates != null) {
			for (int y = 0; y < map.getHeight(); y++) {
				for (int x = 0; x < map.getWidth(); x++) {
					Moongate p = findMoongateAtCoords(moongates, x, y);
					if (p == null) {
						moongateBuffer.append("0,");
					} else {
						moongateBuffer.append(p.getMapTileId() + ",");
					}
				}
				moongateBuffer.append("\n");
			}
		}

		String ml = moongateBuffer.toString();
		ml = ml.substring(0, ml.length() - 2);
		
		

		WorldMapTmxConvert c = new WorldMapTmxConvert(map.getFname(), "tiles.png", 
				map.getWidth(), map.getHeight(), 
				TILE_SIZE, TILE_SIZE, 
				dl, pl, ml, 
				portals, moongates, map.getLabels());
		
		FileUtils.writeStringToFile(new File("src/main/resources/tilemaps/map_" + map.getId() + ".tmx"), c.toString());
	}

	private static int findTileId(Tile[] tiles, String name) {
		for (int i = 1; i < tiles.length; i++) {
			if (tiles[i] == null)
				continue;
			if (StringUtils.equals(tiles[i].getName(), name))
				return i;
		}
		return 0;
	}

	private static Portal findPortalAtCoords(List<Portal> portals, int x, int y) {
		for (Portal p : portals) {
			if (p != null && (p.getX() == x && p.getY() == y))
				return p;
		}
		return null;
	}
	
	private static Moongate findMoongateAtCoords(List<Moongate> moongates, int x, int y) {
		for (Moongate p : moongates) {
			if (p != null && (p.getX() == x && p.getY() == y))
				return p;
		}
		return null;
	}

	private WorldMapTmxConvert(String tilesetName, String imageSource, 
			int mapWidth, int mapHeight, 
			int tileWidth, int tileHeight,  
			String dataLayer, String portalLayer, String moongateLayer, 
			List<Portal> portalObjects, List<Moongate> moongateObjects, List<Label> labelObjects) {
		this.tilesetName = tilesetName;
		this.imageSource = imageSource;
		this.mapWidth = mapWidth;
		this.mapHeight = mapHeight;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
		
		this.dataLayer = dataLayer;
		this.portalLayer = portalLayer;
		this.moongateLayer = moongateLayer;
		
		this.portalObjects = portalObjects;
		this.moongateObjects = moongateObjects;
		this.labelObjects = labelObjects;

		
	}

	@Override
	public String toString() {

		StringBuffer portalString = new StringBuffer();
		if (portalObjects != null) {
			for (Portal p : portalObjects) {
				if (p == null)
					continue;
				portalString.append(p.toString());
			}
		}
		
		StringBuffer moongateString = new StringBuffer();
		if (moongateObjects != null) {
			for (Moongate p : moongateObjects) {
				if (p == null)
					continue;
				moongateString.append(p.toString());
			}
		}
		
		StringBuffer labelString = new StringBuffer();
		if (labelObjects != null) {
			for (Label p : labelObjects) {
				if (p == null)
					continue;
				labelString.append(p.toString());
			}
		}

		String template = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<map version=\"1.0\" orientation=\"orthogonal\" width=\"%s\" height=\"%s\" tilewidth=\"%s\" tileheight=\"%s\" backgroundcolor=\"#000000\">\n" +
				"<tileset firstgid=\"1\" name=\"%s\" tilewidth=\"%s\" tileheight=\"%s\">\n" + 
				"<image source=\"%s\" width=\"%s\" height=\"%s\"/>\n</tileset>\n" +

				"<layer name=\"Map Layer\" width=\"%s\" height=\"%s\">\n" + 
				"<data encoding=\"csv\">\n%s\n</data>\n</layer>\n" +
				
				//"<layer name=\"Portal Layer\" width=\"%s\" height=\"%s\">\n" + 
				//"<data encoding=\"csv\">\n%s\n</data>\n</layer>\n" + 
				
				//"<layer name=\"Moongate Layer\" width=\"%s\" height=\"%s\">\n" + 
				//"<data encoding=\"csv\">\n%s\n</data>\n</layer>\n" + 
				
				//"<objectgroup name=\"Portal Properties\" width=\"%s\" height=\"%s\">\n%s\n</objectgroup>\n" +
				//"<objectgroup name=\"Moongate Properties\" width=\"%s\" height=\"%s\">\n%s\n</objectgroup>\n" +
				//"<objectgroup name=\"Label Properties\" width=\"%s\" height=\"%s\">\n%s\n</objectgroup>\n" + 
				
				"</map>";

		return String.format(template, 
				
				mapWidth, mapHeight, tileWidth, tileHeight, 
				tilesetName, tileWidth, tileHeight, 
				imageSource, mapWidth, mapHeight, 
				
				mapWidth, mapHeight, dataLayer,
				mapWidth, mapHeight, portalLayer,
				mapWidth, mapHeight, moongateLayer,
				
				mapWidth, mapHeight, portalString.toString(),
				mapWidth, mapHeight, moongateString.toString(),
				mapWidth, mapHeight, labelString.toString()
				
				);

	}



}
