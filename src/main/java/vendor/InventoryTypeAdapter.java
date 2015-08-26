package vendor;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import ultima.Constants.InventoryType;

public class InventoryTypeAdapter extends XmlAdapter<String, InventoryType> {

    public String marshal(InventoryType t) {
        return t.toString();
    }

    public InventoryType unmarshal(String val) {
        return InventoryType.valueOf(val);
    }
}
