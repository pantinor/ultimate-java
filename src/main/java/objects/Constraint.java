package objects;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "constraint")
public class Constraint {
	
	private boolean canuse;
	private String charClass;
	
	@XmlAttribute
	public boolean getCanuse() {
		return canuse;
	}
	@XmlAttribute(name="class")
	public String getCharClass() {
		return charClass;
	}
	public void setCanuse(boolean canuse) {
		this.canuse = canuse;
	}
	public void setCharClass(String clazz) {
		this.charClass = clazz;
	}
	
	@Override
	public String toString() {
		return String.format("Constraint [canuse=%s, charClass=%s]", canuse, charClass);
	}
	
	

}
