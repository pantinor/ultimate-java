package test;
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


public class UltMapTmxConvert {

	private String tilesetName;
	private String imageSource;
	private int mapWidth;
	private int mapHeight;
	private int tileWidth;
	private int tileHeight;
	private String data;
	private String people;
	private Person[] persons;
	
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
		
		//BaseMap map = ms.getMapById(8);
		for (BaseMap map : ms.getMaps()) {
			
			if (!map.getFname().endsWith("ult")) {
				continue;
			}

			InputStream is = TestMain.class.getResourceAsStream("/data/"+map.getFname());
			byte[] bytes = IOUtils.toByteArray(is);	
			
			Tile[] tiles = new Tile[map.getWidth() * map.getHeight()];
			int pos = 0;
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
			
			//people layer
			Person[] people = null;
			people = getPeopleLayer(map, bytes, mapTileIds, ts);
			

						
			StringBuffer peopleBuffer = new StringBuffer();
			if (people != null) {
				for (int y=0;y<map.getHeight();y++) {
					for (int x=0;x<map.getWidth();x++) {
						Person p = findPersonAtCoords(people, x, y);
						if (p == null) {
							peopleBuffer.append("0,");
						} else {
							peopleBuffer.append(p.getTileMapId() + ",");
						}
					}
					peopleBuffer.append("\n");
				}
			}

			String p = peopleBuffer.toString();
			if (p == null || p.length() < 1) {
				count = 1;
				//make empty
				for (int i=0;i<map.getWidth() * map.getHeight();i++) {
					peopleBuffer.append("0,");
					count++;
					if (count > map.getWidth()) {
						peopleBuffer.append("\n");
						count = 1;
					}
				}
				p = peopleBuffer.toString();
			}
			p = p.substring(0,p.length()-2);

			
			UltMapTmxConvert c = new UltMapTmxConvert(map.getFname(),"tiles.png",map.getWidth(),map.getHeight(),
					TILE_SIZE,TILE_SIZE,TILE_SIZE^2,TILE_SIZE^2,d,p, people);
			FileUtils.writeStringToFile(new File("src/main/resources/tilemaps/map_"+map.getId()+".tmx"), c.toString());
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
	
	private static Person findPersonAtCoords(Person[] people, int x, int y) {
		for (Person p : people) {
			if (p != null && (p.getStart_x() == x && p.getStart_y() == y))
				return p;
		}
		return null;
	}
	
	
	private UltMapTmxConvert(String tilesetName, String imageSource, int mapWidth, int mapHeight, 
			int tileWidth, int tileHeight, int tilesetWidth, int tilesetHeight, String data, String people, Person[] persons) {
		this.tilesetName = tilesetName;
		this.imageSource = imageSource;
		this.mapWidth = mapWidth;
		this.mapHeight = mapHeight;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
		this.data = data;
		this.people = people;
		this.persons = persons;
	}


	@Override
	public String toString() {
		
		StringBuffer personsString = new StringBuffer();
		if (persons != null) {
			for (Person p : persons) {
				if (p==null) continue;
				personsString.append(p.toString());
			}
		}
		
		
		String template = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<map version=\"1.0\" orientation=\"orthogonal\" width=\"%s\" height=\"%s\" tilewidth=\"%s\" tileheight=\"%s\" backgroundcolor=\"#000000\">\n" +
						"<tileset firstgid=\"1\" name=\"%s\" tilewidth=\"%s\" tileheight=\"%s\">\n" + 
						"<image source=\"%s\" width=\"256\" height=\"256\"/>\n</tileset>\n" + 
		
						"<layer name=\"Map Layer\" width=\"%s\" height=\"%s\">\n" + 
						"<data encoding=\"csv\">\n%s\n</data>\n</layer>\n" + 
						"<layer name=\"People Layer\" width=\"%s\" height=\"%s\">\n" + 
						"<data encoding=\"csv\">\n%s\n</data>\n</layer>\n" + 
						"<objectgroup name=\"Person Properties\" width=\"%s\" height=\"%s\">\n%s\n</objectgroup>\n" +
						
						"</map>";
		
		return String.format(template,  mapWidth, mapHeight, tileWidth, tileHeight, 
				tilesetName, tileWidth, tileHeight, 
				imageSource, 
				mapWidth, mapHeight, data,
				mapWidth, mapHeight, people, 
				mapWidth, mapHeight, personsString.toString());		
		
	}
	
	
	private static Person[] getPeopleLayer(BaseMap map, byte[] bytes, Tile[] mapTileIds, TileSet ts) {
		//people layer
		int MAX_PEOPLE = 32;
		Person[] people = new Person[MAX_PEOPLE];
		int startOffset = map.getWidth() * map.getHeight();
		int end = startOffset + MAX_PEOPLE;
		int count = 0;
		for (int i=startOffset;i<end;i++) {
			int index = bytes[i] & 0xff; // convert a byte to an unsigned int value
			if (index == 0) {
				count ++;
				continue;
			}
			Tile t = ts.getTileByIndex(index);
			if (t == null) {
				System.out.println(map.getFname() + " Person Tile index cannot be found: skipping - " + index);
				count++;
				continue;
			}
			int tileId = findTileId(mapTileIds,t.getName());
			Person p = new Person();
			p.setId(count);
			p.setTile(t);
			p.setTileMapId(tileId);
			people[count] = p;
			count++;
		}
		
		
		
		startOffset = map.getWidth() * map.getHeight() + MAX_PEOPLE * 1;
		end = startOffset + MAX_PEOPLE;
		count = 0;
		for (int i=startOffset;i<end;i++) {
			int start_x = bytes[i] & 0xff; // convert a byte to an unsigned int value
			Person p = people[count];
			if (p == null) {
				count++;
				continue;
			}
			p.setStart_x(start_x);
			count++;
		}
		
		startOffset = map.getWidth() * map.getHeight() + MAX_PEOPLE * 2;
		end = startOffset + MAX_PEOPLE;
		count = 0;
		for (int i=startOffset;i<end;i++) {
			int start_y = bytes[i] & 0xff; // convert a byte to an unsigned int value
			Person p = people[count];
			if (p == null) {
				count++;
				continue;
			}
			p.setStart_y(start_y);
			count++;
		}
		
		startOffset = map.getWidth() * map.getHeight() + MAX_PEOPLE * 6;
		end = startOffset + MAX_PEOPLE;
		count = 0;
		for (int i = startOffset; i < end; i++) {
			byte m = bytes[i];
			Person p = people[count];
			if (p == null) {
				count++;
				continue;
			}
			if (m == 0)
				p.setMovement(ObjectMovementBehavior.FIXED);
			else if (m == 1)
				p.setMovement(ObjectMovementBehavior.WANDER);
			else if (m == 0x80)
				p.setMovement(ObjectMovementBehavior.FOLLOW_AVATAR);
			else if (m == 0xFF)
				p.setMovement(ObjectMovementBehavior.ATTACK_AVATAR);

			count++;
		}
		
		startOffset = map.getWidth() * map.getHeight() + MAX_PEOPLE * 7;
		end = startOffset + MAX_PEOPLE;
		count = 0;
		for (int i=startOffset;i<end;i++) {
			int id = bytes[i] & 0xff; // convert a byte to an unsigned int value
			Person p = people[count];
			if (p == null) {
				count++;
				continue;
			}
			p.setDialogId(id);
			count++;
		}
		
		return people;
	}
	
	
	
		

}
