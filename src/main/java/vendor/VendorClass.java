package vendor;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import ultima.Constants.InventoryType;
import ultima.Sound;

@XmlRootElement(name="vendorClass")
public class VendorClass {
	
	private InventoryType type;
	private List<Item> itemCatalog;
	private List<Vendor> vendors;
	private Sound backgroundMusic;
	
	@XmlAttribute(name="type")
	@XmlJavaTypeAdapter(InventoryTypeAdapter.class)
	public InventoryType getType() {
		return type;
	}
	@XmlElement(name="item")
	public List<Item> getItemCatalog() {
		return itemCatalog;
	}
	@XmlElement(name="vendor")
	public List<Vendor> getVendors() {
		return vendors;
	}
	@XmlAttribute(name="music")
	@XmlJavaTypeAdapter(SoundAdapter.class)
	public Sound getBackgroundMusic() {
		return backgroundMusic;
	}
	public void setType(InventoryType type) {
		this.type = type;
	}
	public void setItemCatalog(List<Item> itemCatalog) {
		this.itemCatalog = itemCatalog;
	}
	public void setVendors(List<Vendor> vendors) {
		this.vendors = vendors;
	}
	public void setBackgroundMusic(Sound backgroundMusic) {
		this.backgroundMusic = backgroundMusic;
	}
	@Override
	public String toString() {
		return String.format("VendorClass [type=%s, itemCatalog=%s, vendors=%s, backgroundMusic=%s]", type, itemCatalog, vendors, backgroundMusic);
	}
	
	public Item getItemForChoice(String choice) {
		if (itemCatalog == null) return null;
		for (Item i : itemCatalog) {
			if (i.getChoice().equals(choice))
				return i;
		}
		return null;
	}
	

}
