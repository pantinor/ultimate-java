package objects;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import ultima.Constants;

@XmlRootElement(name = "portal")
public class Portal implements Constants {

	private String name;
	private int x;
	private int y;
	private int destmapid;
	private int startlevel;
	private String action;
	private String condition;
	private boolean savelocation;
	private String message;
	private String transport;
	private RetroActiveDest retroActiveDest;
	private int mapTileId;
	
	@XmlAttribute
	public int getX() {
		return x;
	}
	@XmlAttribute
	public int getY() {
		return y;
	}
	@XmlAttribute
	public int getDestmapid() {
		return destmapid;
	}

	@XmlAttribute
	public int getStartlevel() {
		return startlevel;
	}
	@XmlAttribute
	public String getAction() {
		return action;
	}
	@XmlAttribute
	public String getCondition() {
		return condition;
	}
	@XmlAttribute
	public boolean isSavelocation() {
		return savelocation;
	}
	@XmlAttribute
	public String getMessage() {
		return message;
	}
	@XmlAttribute
	public String getTransport() {
		return transport;
	}
	@XmlElement(name="retroActiveDest")
	public RetroActiveDest getRetroActiveDest() {
		return retroActiveDest;
	}
	
	

	public void setX(int x) {
		this.x = x;
	}
	public void setY(int y) {
		this.y = y;
	}
	public void setDestmapid(int destmapid) {
		this.destmapid = destmapid;
	}

	public void setStartlevel(int startlevel) {
		this.startlevel = startlevel;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public void setCondition(String condition) {
		this.condition = condition;
	}
	public void setSavelocation(boolean savelocation) {
		this.savelocation = savelocation;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public void setTransport(String transport) {
		this.transport = transport;
	}
	public void setRetroActiveDest(RetroActiveDest retroActiveDest) {
		this.retroActiveDest = retroActiveDest;
	}
	@Override
	public String toString() {
		
		String template = "<object name=\"%s\" type=\"portal\" x=\"%s\" y=\"%s\" width=\"16\" height=\"16\">\n"+
				"<properties>\n"+
				"<property name=\"condition\" value=\"%s\"/>\n"+
				"<property name=\"x\" value=\"%s\"/>\n"+
				"<property name=\"y\" value=\"%s\"/>\n"+
				"<property name=\"destination\" value=\"%s\"/>\n"+
				"<property name=\"message\" value=\"%s\"/>\n"+
				"</properties>\n"+
				"</object>\n";

			return String.format(template, name, x*16, y*16, condition==null?"":condition,x, y, destmapid, message==null?"":message);
	}
	public int getMapTileId() {
		return mapTileId;
	}
	public void setMapTileId(int mapTileId) {
		this.mapTileId = mapTileId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	


	

}
