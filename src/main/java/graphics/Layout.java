package graphics;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "layout")
public class Layout {
	private String name;
	private String type;
	private TileShape tileShape;
	private Viewport viewPort;
	
	@XmlElement(name="tileshape")
	public TileShape getTileShape() {
		return tileShape;
	}
	@XmlElement(name="viewport")
	public Viewport getViewPort() {
		return viewPort;
	}
	public void setTileShape(TileShape tileShape) {
		this.tileShape = tileShape;
	}
	public void setViewPort(Viewport viewPort) {
		this.viewPort = viewPort;
	}
	@XmlAttribute
	public String getName() {
		return name;
	}
	@XmlAttribute
	public String getType() {
		return type;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	

}
