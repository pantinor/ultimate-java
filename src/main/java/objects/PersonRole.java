package objects;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import ultima.Constants.InventoryType;
import vendor.InventoryTypeAdapter;

@XmlRootElement(name = "personrole")
public class PersonRole {
	
	private String role;
	private InventoryType inventoryType;
	private int id;
	

	@XmlAttribute(name="id")
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@XmlAttribute(name="type")
	@XmlJavaTypeAdapter(InventoryTypeAdapter.class)
	public InventoryType getInventoryType() {
		return inventoryType;
	}

	public void setInventoryType(InventoryType inventoryType) {
		this.inventoryType = inventoryType;
	}
	@XmlAttribute(name="role")
	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	@Override
	public String toString() {
		return String.format("%s, inventoryType=%s, id=%s", role, inventoryType, id);
	}



	
	
	

}
