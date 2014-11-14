package objects;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "personrole")
public class PersonRole {
	
	private String role;
	private int id;
	
	@XmlAttribute
	public String getRole() {
		return role;
	}
	@XmlAttribute
	public int getId() {
		return id;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	@Override
	public String toString() {
		return String.format("PersonRole [role=%s, id=%s]", role, id);
	}
	
	

}
