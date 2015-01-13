package objects;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import ultima.Constants.ArmorType;
import ultima.Constants.ClassType;

@XmlRootElement(name = "armor")
public class Armor {
	
	private ArmorType type;
	private String name;
	private int defense;
	private List<Constraint> constraints;
	
	@XmlAttribute
	public String getName() {
		return name;
	}
	@XmlAttribute
	public int getDefense() {
		return defense;
	}
	@XmlElement(name="constraint")
	public List<Constraint> getConstraints() {
		return constraints;
	}
	
	
	public void setName(String name) {
		this.name = name;
	}
	public void setDefense(int defense) {
		this.defense = defense;
	}
	public void setConstraints(List<Constraint> constraints) {
		this.constraints = constraints;
	}
	@XmlAttribute(name="type")
	@XmlJavaTypeAdapter(ArmorTypeAdapter.class)
	public ArmorType getType() {
		return type;
	}
	public void setType(ArmorType type) {
		this.type = type;
	}

	
	public boolean canUse(ClassType klazz) {
		if (constraints == null) return true;
		boolean canuse = true;
		for (Constraint c : constraints) {
			boolean b1 = c.getCharClass().equals(klazz.toString().toLowerCase());
			boolean b2 = c.getCharClass().equals("all");
			if (!c.getCanuse() && b1) {
				canuse = false;
			}
			if (!c.getCanuse() && b2) {
				canuse = false;
			}
			if (c.getCanuse() && b1) {
				canuse = true;
			}
		}
		return canuse;
	}
	

}
