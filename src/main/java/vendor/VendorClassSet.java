package vendor;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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

}
