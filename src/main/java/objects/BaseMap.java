package objects;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.StringUtils;

import ultima.Constants;
import ultima.Ultima4;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

@XmlRootElement(name = "map")
public class BaseMap implements Constants {

	private int id;
	private String fname;
	private String type;
	private int width;
	private int height;
	private int levels;
	private int chunkwidth;
	private int chunkheight;
	private int offset;
	private boolean showavatar;
	private boolean nolineofsight;
	private boolean firstperson;
	private boolean contextual;
	private String music;
	private String borderbehavior;
	private String tileset;
	private String tilemap;
	
	private List<Portal> portals;
	private List<Label> labels;
	private City city;
	private Dungeon dungeon;
	private Shrine shrine;
	private List<Moongate> moongates;
	private Tile[] tiles;
	private float[][] shadownMap;
	private long wanderFlag = 0;
	

	public Moongate getMoongate(int phase) {
		if (moongates == null) return null;
		for (Moongate m : moongates) {
			if (m.getPhase() == phase) 
				return m;
		}
		return null;
	}
	
	public Portal getPortal(int id) {
		if (portals == null) return null;
		for (Portal p : portals) {
			if (p.getDestmapid() == id) 
				return p;
		}
		return null;
	}
	
	public Portal getPortal(float x, float y) {
		if (portals == null) return null;
		for (Portal p : portals) {
			if (p.getX() == x && p.getY() == y) 
				return p;
		}
		return null;
	}
	
	@XmlAttribute
	public int getId() {
		return id;
	}
	@XmlAttribute
	public String getFname() {
		return fname;
	}
	@XmlAttribute
	public String getType() {
		return type;
	}
	@XmlAttribute
	public int getWidth() {
		return width;
	}
	@XmlAttribute
	public int getHeight() {
		return height;
	}
	@XmlAttribute
	public int getLevels() {
		return levels;
	}
	@XmlAttribute
	public int getOffset() {
		return offset;
	}
	@XmlAttribute
	public boolean getShowavatar() {
		return showavatar;
	}
	@XmlAttribute
	public boolean getNolineofsight() {
		return nolineofsight;
	}
	@XmlAttribute
	public boolean getFirstperson() {
		return firstperson;
	}
	@XmlAttribute
	public boolean getContextual() {
		return contextual;
	}
	@XmlAttribute
	public String getMusic() {
		return music;
	}
	@XmlAttribute
	public String getTileset() {
		return tileset;
	}
	@XmlAttribute
	public String getTilemap() {
		return tilemap;
	}
	@XmlAttribute
	public int getChunkwidth() {
		return chunkwidth;
	}
	@XmlAttribute
	public int getChunkheight() {
		return chunkheight;
	}
	@XmlAttribute
	public String getBorderbehavior() {
		return borderbehavior;
	}
	@XmlElement(name = "portal")
	public List<Portal> getPortals() {
		return portals;
	}
	@XmlElement(name = "label")
	public List<Label> getLabels() {
		return labels;
	}
	@XmlElement
	public City getCity() {
		return city;
	}


	@XmlElement
	public Shrine getShrine() {
		return shrine;
	}
	@XmlElement(name="moongate")
	public List<Moongate> getMoongates() {
		return moongates;
	}

	@XmlElement
	public Dungeon getDungeon() {
		return dungeon;
	}

	public void setId(int id) {
		this.id = id;
	}
	public void setFname(String fname) {
		this.fname = fname;
	}
	public void setType(String type) {
		this.type = type;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public void setLevels(int levels) {
		this.levels = levels;
	}
	public void setChunkWidth(int chunk_width) {
		this.chunkwidth = chunk_width;
	}
	public void setChunkHeight(int chunk_height) {
		this.chunkheight = chunk_height;
	}
	public void setOffset(int offset) {
		this.offset = offset;
	}
	public void setShowavatar(boolean showavatar) {
		this.showavatar = showavatar;
	}
	public void setNolineofsight(boolean nolineofsight) {
		this.nolineofsight = nolineofsight;
	}
	public void setFirstperson(boolean firstperson) {
		this.firstperson = firstperson;
	}
	public void setContextual(boolean contextual) {
		this.contextual = contextual;
	}
	public void setMusic(String music) {
		this.music = music;
	}
	public void setBorderBehavior(String borderBehavior) {
		this.borderbehavior = borderBehavior;
	}
	public void setTileset(String tileset) {
		this.tileset = tileset;
	}
	public void setTilemap(String tilemap) {
		this.tilemap = tilemap;
	}
	public void setChunkwidth(int chunkwidth) {
		this.chunkwidth = chunkwidth;
	}
	public void setChunkheight(int chunkheight) {
		this.chunkheight = chunkheight;
	}
	public void setBorderbehavior(String borderbehavior) {
		this.borderbehavior = borderbehavior;
	}
	public void setPortals(List<Portal> portals) {
		this.portals = portals;
	}
	public void setLabels(List<Label> labels) {
		this.labels = labels;
	}
	public void setCity(City city) {
		this.city = city;
	}

	public void setShrine(Shrine shrine) {
		this.shrine = shrine;
	}
	public void setMoongates(List<Moongate> moongate) {
		this.moongates = moongate;
	}


	public void setDungeon(Dungeon dungeon) {
		this.dungeon = dungeon;
	}
	@Override
	public String toString() {
		return String.format("BaseMap [id=%s, fname=%s, portals=%s]", id, fname, portals);
	}

	public Tile[] getTiles() {
		return tiles;
	}

	public void setTiles(Tile[] tiles) {
		this.tiles = tiles;
	}

	public Tile getTile(int x, int y) {
		if (x + (y * width) >= tiles.length) return null;
		return tiles[x + (y * width)];
	}
	
	public Tile getTile(Vector3 v) {
		if ((int)v.x + ((int)v.y * width) >= tiles.length) return null;
		return tiles[(int)v.x + ((int)v.y * width)];
	}
	
	public float[][] getShadownMap() {
		return shadownMap;
	}

	public void setShadownMap(float[][] shadownMap) {
		this.shadownMap = shadownMap;
	}


	public void setSprites(Ultima4 mainGame, TextureAtlas atlas1, TextureAtlas atlas2) {
		if (city != null) {
			
			for(Person p : city.getPeople()) {
				if (p == null) continue;
				String tname = p.getTile().getName();
				
				Array<AtlasRegion> tr = atlas1.findRegions(tname);
				if (tr == null || tr.size == 0) {
					tr = atlas2.findRegions(tname);
				}
				
				//give some randomness to the animations
				//tr.shuffle();
				
				//random rate between 1 and 4
				int frameRate = ThreadLocalRandom.current().nextInt(1,4);
				p.setAnim(new Animation(frameRate, tr));
				
				Vector3 pixelPos = mainGame.getMapPixelCoords(p.getStart_x(), p.getStart_y());
				p.setCurrentPos(pixelPos);
				p.setX(p.getStart_x());
				p.setY(p.getStart_y());
			}
			
		}
	}
	

	public void moveObjects(Ultima4 mainGame) {
		
		if (city != null) {
			
			wanderFlag++;
			
			for(Person p : city.getPeople()) {
				if (p == null) continue;
				if (p.getMovement() == ObjectMovementBehavior.WANDER) {
					Direction dir = Direction.getRandomValidDirection(getValidMovesMask(p.getX(), p.getY()));
					if (dir == null) continue; 
					if (wanderFlag % 2 == 0) continue; 
					if (p.isTalking()) continue; 

					Vector3 pos = null;
					if (dir == Direction.NORTH) pos = new Vector3(p.getX(), p.getY()-1, 0);
					if (dir == Direction.SOUTH) pos = new Vector3(p.getX(), p.getY()+1, 0);
					if (dir == Direction.EAST) pos = new Vector3(p.getX()+1, p.getY(), 0);
					if (dir == Direction.WEST) pos = new Vector3(p.getX()-1, p.getY(), 0);
					Vector3 pixelPos = mainGame.getMapPixelCoords((int)pos.x, (int)pos.y);
					p.setCurrentPos(pixelPos);
					p.setX((int)pos.x);
					p.setY((int)pos.y);
				}
			}
			
		}
		
	}
	
	public int getValidMovesMask(int x, int y) {
		
		int mask = 0;
		
		Tile north = getTile(x,y-1);
		Tile south = getTile(x,y+1);
		Tile east = getTile(x+1,y);
		Tile west = getTile(x-1,y);
		
		if (north != null) {
			Rule northRule = Ultima4.tileRules.getRule(north.getRule());
			if (northRule == null || !StringUtils.equals(northRule.getCantwalkon(),"all")) {
				mask = Direction.addToMask(Direction.NORTH, mask);
			}
		}
		if (south != null) {
			Rule southRule = Ultima4.tileRules.getRule(south.getRule());
			if (southRule == null || !StringUtils.equals(southRule.getCantwalkon(),"all")) {
				mask = Direction.addToMask(Direction.SOUTH, mask);
			}
		}
		if (east != null) {
			Rule eastRule = Ultima4.tileRules.getRule(east.getRule());
			if (eastRule == null || !StringUtils.equals(eastRule.getCantwalkon(),"all")) {
				mask = Direction.addToMask(Direction.EAST, mask);
			}
		}
		if (west != null) {
			Rule westRule = Ultima4.tileRules.getRule(west.getRule());
			if (westRule == null || !StringUtils.equals(westRule.getCantwalkon(),"all")) {
				mask = Direction.addToMask(Direction.WEST, mask);
			}
		}


		return mask;
		
	}
	

	
	


}
