package vendor;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import ultima.Constants.InventoryType;
import ultima.Constants.Maps;

@XmlRootElement(name = "vendor")
public class Vendor {

    private Maps mapId;
    private String name;
    private String owner;
    private List<Item> inventoryItems;
    private InventoryType vendorType;
    private int personId;
    private VendorClass vendorClass;

    private String genericField1;

    @XmlAttribute(name = "name")
    public String getName() {
        return name;
    }

    @XmlAttribute(name = "owner")
    public String getOwner() {
        return owner;
    }

    @XmlElement(name = "item")
    public List<Item> getInventoryItems() {
        return inventoryItems;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setInventoryItems(List<Item> inventoryItems) {
        this.inventoryItems = inventoryItems;
    }

    @XmlAttribute(name = "type")
    @XmlJavaTypeAdapter(InventoryTypeAdapter.class)
    public InventoryType getVendorType() {
        return vendorType;
    }

    public void setVendorType(InventoryType vendorType) {
        this.vendorType = vendorType;
    }

    @XmlAttribute(name = "id")
    @XmlJavaTypeAdapter(MapIdAdapter.class)
    public Maps getMapId() {
        return mapId;
    }

    public void setMapId(Maps mapId) {
        this.mapId = mapId;
    }

    @XmlAttribute(name = "personId")
    public int getPersonId() {
        return personId;
    }

    public void setPersonId(int personId) {
        this.personId = personId;
    }

    @XmlAttribute(name = "genericField1")
    public String getGenericField1() {
        return genericField1;
    }

    public void setGenericField1(String genericField1) {
        this.genericField1 = genericField1;
    }

    @Override
    public String toString() {
        return String.format("Vendor [mapId=%s, name=%s, owner=%s, personId=%s]", mapId, name, owner, personId);
    }

    public VendorClass getVendorClass() {
        return vendorClass;
    }

    public void setVendorClass(VendorClass vendorClass) {
        this.vendorClass = vendorClass;
    }

    public Item getItem(String choice) {
        for (Item i : inventoryItems) {
            if (i.getChoice().equals(choice)) {
                return i;
            }
        }
        return null;
    }

    public Item getTavernInfo(String choice) {
        for (Item i : inventoryItems) {
            if (i.getType() == InventoryType.TAVERNINFO && i.getChoice().equals(choice)) {
                return i;
            }
        }
        return null;
    }

}
