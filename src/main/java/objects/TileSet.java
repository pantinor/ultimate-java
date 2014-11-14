package objects;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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
		}
	}
	
	public Tile getTileByName(String name) {
		return nameMap.get(name);
	}
	
	public Tile getTileByIndex(int index) {
		return indexMap.get(index);
	}

}
