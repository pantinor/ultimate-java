package graphics;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "subimage")
public class SubImage {
	
	private String name;
	private int x;
	private int y;
	private int width;
	private int height;
	
	@XmlAttribute
	public String getName() {
		return name;
	}
	@XmlAttribute
	public int getX() {
		return x;
	}
	@XmlAttribute
	public int getY() {
		return y;
	}
	@XmlAttribute
	public int getWidth() {
		return width;
	}
	@XmlAttribute
	public int getHeight() {
		return height;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setX(int x) {
		this.x = x;
	}
	public void setY(int y) {
		this.y = y;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	
	

}
