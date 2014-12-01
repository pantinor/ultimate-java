package objects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import ultima.Constants.Maps;
import util.Utils;

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
						for (PersonRole pr : m.getCity().getPersonRoles()) {
							if (p.getId() == pr.getId()) p.setRole(pr); 
						}
					}
				}
				
			}
			
			if (m.getId() == Maps.CASTLE_OF_LORD_BRITISH_2.getId()) {
				m.getCity().getPeople()[31].setConversation(new LordBritishConversation());
			}

			
			try {
				Utils.setMapTiles(m, ts);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if (!m.getType().equals("dungeon") && !m.getType().equals("shrine") && !m.getType().equals("combat")) {
				
				//use ydown scheme here same as gdx rendering
				float[][] shadowMap = new float[m.getWidth()][m.getHeight()];
				for (int x=0;x<m.getWidth();x++) {
					for (int y=0;y<m.getHeight();y++) {
						//System.out.println(m.getFname() +" "+(row)+" "+(col));
						shadowMap[x][y] = (m.getTile(x, m.getHeight()-1-y).isOpaque()?1:0);
					}
				}
				m.setShadownMap(shadowMap);
			}


		}
	}
	
	public BaseMap getMapById(int id) {
		return mapMap.get(id);
	}
	

	
	

}
