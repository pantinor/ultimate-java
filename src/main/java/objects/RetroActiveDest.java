package objects;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "retroActiveDest")
public class RetroActiveDest {
	
	private int x;
	private int y;
	private int mapid;
	
	@XmlAttribute
	public int getX() {
		return x;
	}
	@XmlAttribute
	public int getY() {
		return y;
	}
	@XmlAttribute
	public int getMapid() {
		return mapid;
	}
	public void setX(int x) {
		this.x = x;
	}
	public void setY(int y) {
		this.y = y;
	}
	public void setMapid(int mapid) {
		this.mapid = mapid;
	}
	
	@Override
	public String toString() {
		return String.format("RetroActiveDest [x=%s, y=%s, mapid=%s]", x, y, mapid);
	}
	
	

}
