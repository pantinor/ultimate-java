package graphics;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "color")
public class Color {
	private int red;
	private int green;
	private int blue;
	@XmlAttribute
	public int getRed() {
		return red;
	}
	@XmlAttribute
	public int getGreen() {
		return green;
	}
	@XmlAttribute
	public int getBlue() {
		return blue;
	}
	public void setRed(int red) {
		this.red = red;
	}
	public void setGreen(int green) {
		this.green = green;
	}
	public void setBlue(int blue) {
		this.blue = blue;
	}
	
	


}
