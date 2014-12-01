package vendor;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import objects.Party;
import ultima.Constants.InventoryType;
import ultima.Constants.Maps;

@XmlRootElement(name="vendorSet")
public class VendorClassSet {
	
	private List<VendorClass> vendorClasses;

	@XmlElement(name="vendorClass")
	public List<VendorClass> getVendorClasses() {
		return vendorClasses;
	}

	public void setVendorClasses(List<VendorClass> vendorClasses) {
		this.vendorClasses = vendorClasses;
	}
	
	public Vendor getVendor(InventoryType type, Maps id) {
		for (VendorClass vc : vendorClasses) {
			if (vc.getType() == type) {
				for (Vendor v : vc.getVendors()) {
					if (v.getMapId() == id) {
						return v;
					}
				}
			}
		}
		return null;
	}
	
	public void init() {
		for(VendorClass vc : vendorClasses ) {
			for (Vendor v : vc.getVendors()) {
				v.setVendorClass(vc);
			}
		}
	}
	
	public BaseVendor getVendorImpl(InventoryType type, Maps map, Party party) {
		
		BaseVendor v = null;
		
		switch(type) {
		case ARMOR:
			v = new ArmorVendor(getVendor(type, map), party);
			break;
		case FOOD:
			break;
		case GUILDITEM:
			break;
		case HEALER:
			v = new HealerService(getVendor(type, map), party);
			break;
		case HORSE:
			break;
		case INN:
			break;
		case REAGENT:
			break;
		case TAVERN:
			break;
		case TAVERNINFO:
			break;
		case WEAPON:
			v = new WeaponVendor(getVendor(type, map), party);
			break;
		default:
			break;
		
		}
		
		return v;

		
	}

}
