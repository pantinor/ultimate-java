package objects;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "rule")
public class Rule {

	private String name;
	private boolean balloon;
	private boolean canattackover;
	private boolean canlandballoon;
	private String cantwalkoff;
	private String cantwalkon;
	private boolean chest;
	private boolean dispel;
	private boolean door;
	private String effect;
	private boolean horse;
	private boolean lockeddoor;
	private boolean creatureunwalkable;
	private boolean replacement;
	private boolean onWaterOnlyReplacement;
	private boolean sailable;
	private boolean ship;
	private String speed;
	private boolean swimable;
	private boolean talkover;
	private boolean unflyable;
	private boolean livingthing;
	
	@XmlAttribute
	public String getName() {
		return name;
	}
	@XmlAttribute
	public boolean getBalloon() {
		return balloon;
	}
	@XmlAttribute
	public boolean getCanattackover() {
		return canattackover;
	}
	@XmlAttribute
	public boolean getCanlandballoon() {
		return canlandballoon;
	}
	@XmlAttribute
	public String getCantwalkoff() {
		return cantwalkoff;
	}
	@XmlAttribute
	public String getCantwalkon() {
		return cantwalkon;
	}
	@XmlAttribute
	public boolean getChest() {
		return chest;
	}
	@XmlAttribute
	public boolean getDispel() {
		return dispel;
	}
	@XmlAttribute
	public boolean getDoor() {
		return door;
	}
	@XmlAttribute
	public String getEffect() {
		return effect;
	}
	@XmlAttribute
	public boolean getHorse() {
		return horse;
	}
	@XmlAttribute
	public boolean getLockeddoor() {
		return lockeddoor;
	}
	@XmlAttribute
	public boolean getCreatureunwalkable() {
		return creatureunwalkable;
	}
	@XmlAttribute
	public boolean getReplacement() {
		return replacement;
	}
	@XmlAttribute
	public boolean getOnWaterOnlyReplacement() {
		return onWaterOnlyReplacement;
	}
	@XmlAttribute
	public boolean getSailable() {
		return sailable;
	}
	@XmlAttribute
	public boolean getShip() {
		return ship;
	}
	@XmlAttribute
	public String getSpeed() {
		return speed;
	}
	@XmlAttribute
	public boolean getSwimable() {
		return swimable;
	}
	@XmlAttribute
	public boolean getTalkover() {
		return talkover;
	}
	@XmlAttribute
	public boolean getUnflyable() {
		return unflyable;
	}
	@XmlAttribute
	public boolean getLivingthing() {
		return livingthing;
	}
	
	
	
	public void setName(String name) {
		this.name = name;
	}
	public void setBalloon(boolean balloon) {
		this.balloon = balloon;
	}
	public void setCanattackover(boolean canattackover) {
		this.canattackover = canattackover;
	}
	public void setCanlandballoon(boolean canlandballoon) {
		this.canlandballoon = canlandballoon;
	}
	public void setCantwalkoff(String cantwalkoff) {
		this.cantwalkoff = cantwalkoff;
	}
	public void setCantwalkon(String cantwalkon) {
		this.cantwalkon = cantwalkon;
	}
	public void setChest(boolean chest) {
		this.chest = chest;
	}
	public void setDispel(boolean dispel) {
		this.dispel = dispel;
	}
	public void setDoor(boolean door) {
		this.door = door;
	}
	public void setEffect(String effect) {
		this.effect = effect;
	}
	public void setHorse(boolean horse) {
		this.horse = horse;
	}
	public void setLockeddoor(boolean lockeddoor) {
		this.lockeddoor = lockeddoor;
	}
	public void setCreatureunwalkable(boolean creatureunwalkable) {
		this.creatureunwalkable = creatureunwalkable;
	}
	public void setReplacement(boolean replacement) {
		this.replacement = replacement;
	}
	public void setOnWaterOnlyReplacement(boolean onWaterOnlyReplacement) {
		this.onWaterOnlyReplacement = onWaterOnlyReplacement;
	}
	public void setSailable(boolean sailable) {
		this.sailable = sailable;
	}
	public void setShip(boolean ship) {
		this.ship = ship;
	}
	public void setSpeed(String speed) {
		this.speed = speed;
	}
	public void setSwimable(boolean swimable) {
		this.swimable = swimable;
	}
	public void setTalkover(boolean talkover) {
		this.talkover = talkover;
	}
	public void setUnflyable(boolean unflyable) {
		this.unflyable = unflyable;
	}
	public void setLivingthing(boolean livingthing) {
		this.livingthing = livingthing;
	}
	
	@Override
	public String toString() {
		return String.format("Rule [name=%s]", name);
	}
	
	
	
	

}
