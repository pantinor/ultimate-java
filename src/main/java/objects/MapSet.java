package objects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "maps")
public class MapSet {
	
	private List<BaseMap> maps = null;
	private Map<Integer, BaseMap> mapMap = new HashMap<Integer, BaseMap>();

	@XmlElement(name = "map")
	public List<BaseMap> getMaps() {
		return maps;
	}

	public void setMaps(List<BaseMap> maps) {
		this.maps = maps;
	}
	
	public void setMapTable() {
		for (BaseMap m : maps) {
			mapMap.put(m.getId(), m);
		}
	}
	
	public BaseMap getMapById(int id) {
		return mapMap.get(id);
	}
	

	
	

}
