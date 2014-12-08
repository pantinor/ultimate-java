package objects;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import ultima.Constants.Maps;

@XmlRootElement(name = "tileset")
public class TileSet {
	
	private List<Tile> tiles = null;
	private Map<String, Tile> nameMap = new HashMap<String, Tile>();
	private Map<Integer, Tile> indexMap = new HashMap<Integer, Tile>();


	@XmlElement(name = "tile")
	public List<Tile> getTiles() {
		return tiles;
	}

	public void setTiles(List<Tile> tiles) {
		this.tiles = tiles;
	}
	
	public void setMaps() {
		for (Tile t : tiles) {
			nameMap.put(t.getName(), t);
			indexMap.put(t.getIndex(), t);
			
			if (t.getName().equals("horse")) t.setCombatMap(Maps.GRASS_CON);
			else if (t.getName().equals("swamp")) t.setCombatMap(Maps.MARSH_CON);
			else if (t.getName().equals("grass")) t.setCombatMap(Maps.GRASS_CON);
			else if (t.getName().equals("brush")) t.setCombatMap(Maps.BRUSH_CON);
			else if (t.getName().equals("forest")) t.setCombatMap(Maps.FOREST_CON);
			else if (t.getName().equals("hills")) t.setCombatMap(Maps.HILL_CON);
			else if (t.getName().equals("dungeon")) t.setCombatMap(Maps.DUNGEON_CON);
			else if (t.getName().equals("city")) t.setCombatMap(Maps.GRASS_CON);
			else if (t.getName().equals("castle")) t.setCombatMap(Maps.GRASS_CON);
			else if (t.getName().equals("town")) t.setCombatMap(Maps.GRASS_CON);
			else if (t.getName().equals("lcb_entrance")) t.setCombatMap(Maps.GRASS_CON);
			else if (t.getName().equals("bridge")) t.setCombatMap(Maps.BRIDGE_CON);
			else if (t.getName().equals("balloon")) t.setCombatMap(Maps.GRASS_CON);
			else if (t.getName().contains("bridge_piece")) t.setCombatMap(Maps.BRIDGE_CON);
			else if (t.getName().equals("shrine")) t.setCombatMap(Maps.GRASS_CON);
			else if (t.getName().equals("chest")) t.setCombatMap(Maps.GRASS_CON);
			else if (t.getName().equals("brick_floor")) t.setCombatMap(Maps.BRICK_CON);
			else if (t.getName().equals("moongate")) t.setCombatMap(Maps.GRASS_CON);
			else if (t.getName().equals("moongate_opening")) t.setCombatMap(Maps.GRASS_CON);
			else if (t.getName().equals("dungeon_floor")) t.setCombatMap(Maps.DUNGEON_CON);
		}
	}
	
	public Tile getTileByName(String name) {
		return nameMap.get(name);
	}
	
	public Tile getTileByIndex(int index) {
		return indexMap.get(index);
	}

}
