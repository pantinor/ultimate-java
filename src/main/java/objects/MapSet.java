package objects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import ultima.Utils;

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
	
	public void init(TileSet ts) {
		for (BaseMap m : maps) {
			
			mapMap.put(m.getId(), m);
			
			String tlkName = m.getCity()==null?null:m.getCity().getTlk_fname();
			if (tlkName != null) {
				
				List<Conversation> conv = Utils.getDialogs(tlkName);
				m.getCity().setConversations(conv);
				
				Person[] people = Utils.getPeople(m.getFname(), ts);
				m.getCity().setPeople(people);
				for (Person p: people) {
					if (p != null) {
						for (Conversation c : conv) {
							if (c.getIndex() == p.getDialogId()) {
								p.setConversation(c);
							}
						}
					}
				}
				
			}

		}
	}
	
	public BaseMap getMapById(int id) {
		return mapMap.get(id);
	}
	

	
	

}
