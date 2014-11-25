package vendor;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import ultima.Constants.ArmorType;
import ultima.Constants.GuildItemType;
import ultima.Constants.HealType;
import ultima.Constants.InventoryType;
import ultima.Constants.Reagent;
import ultima.Constants.WeaponType;

@XmlRootElement(name="item")
public class Item {
	
	private String choice;
	private InventoryType type;
	private String name;
	private int price;
	private int quantity;
	private String description;
	private boolean hidden;
	
	private Reagent reagentType;
	private HealType healType;
	private GuildItemType guildItemType;
	private WeaponType weaponType;
	private ArmorType armorType;

	private int roomX;
	private int roomY;
	
	
	@XmlAttribute(name="choice")
	public String getChoice() {
		return choice;
	}
	@XmlAttribute(name="name")
	public String getName() {
		return name;
	}
	@XmlAttribute(name="price")
	public int getPrice() {
		return price;
	}
	public void setChoice(String choice) {
		this.choice = choice;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setPrice(int price) {
		this.price = price;
	}
	@XmlAttribute(name="type")
	@XmlJavaTypeAdapter(InventoryTypeAdapter.class)
	public InventoryType getType() {
		return type;
	}
	public void setType(InventoryType type) {
		this.type = type;
	}
	@XmlAttribute(name="description")
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	@XmlAttribute(name="reagentType")
	@XmlJavaTypeAdapter(ReagentTypeAdapter.class)
	public Reagent getReagentType() {
		return reagentType;
	}
	public void setReagentType(Reagent reagentType) {
		this.reagentType = reagentType;
	}
	@XmlAttribute(name="healType")
	@XmlJavaTypeAdapter(HealTypeAdapter.class)
	public HealType getHealType() {
		return healType;
	}
	public void setHealType(HealType healType) {
		this.healType = healType;
	}
	@XmlAttribute(name="x")
	public int getRoomX() {
		return roomX;
	}
	@XmlAttribute(name="y")
	public int getRoomY() {
		return roomY;
	}
	public void setRoomX(int roomX) {
		this.roomX = roomX;
	}
	public void setRoomY(int roomY) {
		this.roomY = roomY;
	}
	@XmlAttribute(name="guildItemType")
	@XmlJavaTypeAdapter(GuildItemAdapter.class)
	public GuildItemType getGuildItemType() {
		return guildItemType;
	}
	public void setGuildItemType(GuildItemType guildItemType) {
		this.guildItemType = guildItemType;
	}
	@XmlAttribute(name="quant")
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	@XmlAttribute(name="hidden")
	public boolean getHidden() {
		return hidden;
	}
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
	
	
	@XmlAttribute(name="weaponType")
	@XmlJavaTypeAdapter(WeaponAdapter.class)
	public WeaponType getWeaponType() {
		return weaponType;
	}
	@XmlAttribute(name="armorType")
	@XmlJavaTypeAdapter(ArmorAdapter.class)
	public ArmorType getArmorType() {
		return armorType;
	}
	public void setWeaponType(WeaponType weaponType) {
		this.weaponType = weaponType;
	}
	public void setArmorType(ArmorType armorType) {
		this.armorType = armorType;
	}
	@Override
	public String toString() {
		return String.format("Item [type=%s, name=%s]", type, name);
	}
	
	

}
