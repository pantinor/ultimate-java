package objects;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "dungeon")
public class Dungeon {
	private String name;
	private int rooms;
	
	@XmlAttribute
	public String getName() {
		return name;
	}
	@XmlAttribute
	public int getRooms() {
		return rooms;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setRooms(int rooms) {
		this.rooms = rooms;
	}
	
	@Override
	public String toString() {
		return String.format("Dungeon [name=%s, rooms=%s]", name, rooms);
	}
	
	

}
