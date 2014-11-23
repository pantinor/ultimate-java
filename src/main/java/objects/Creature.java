package objects;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import ultima.Constants;
import util.Utils;

@XmlRootElement(name = "creature")
public class Creature implements Constants {

	private boolean ambushes;
	private int basehp;
	private boolean camouflage;
	private String camouflageTile;
	private boolean canMoveOntoAvatar;
	private boolean canMoveOntoCreatures;
	private boolean cantattack;
	private String casts;
	private CreatureStatus damageStatus = CreatureStatus.FINE;
	private boolean divides;
	private int encounterSize;
	private int exp;
	private boolean flies;
	private boolean forceOfNature;
	private boolean good;
	private int hp;
	private int id;
	private boolean incorporeal;
	private int leader;
	private boolean leavestile;
	private String movement;
	private String name;
	private boolean nochest;
	private boolean poisons;
	private boolean ranged;
	private String rangedhittile;
	private String rangedmisstile;
	private String resists;
	private boolean sails;
	private boolean spawnsOnDeath;
	private String spawntile;
	private List<StatusType> status = new ArrayList<StatusType>();
	private String steals;
	private boolean swims;
	private boolean teleports;
	private String tile;
	
	private boolean undead;
	private boolean wontattack;
	private String worldrangedtile;
	
	public Creature() {
		addStatus(StatusType.STAT_GOOD);
	}
	
	/* combat methods */
	public void act() {
	}
	public void applyDamage(int damage) {
		Utils.adjustValueMin(this.hp, damage, 0);
	}
	public void dealDamage(Creature m, int damage) {
		m.applyDamage(damage);
	}
	@XmlAttribute
	public boolean getAmbushes() {
		return ambushes;
	}
	@XmlAttribute
	public int getBasehp() {
		return basehp;
	}
	@XmlAttribute
	public boolean getCamouflage() {
		return camouflage;
	}
	@XmlAttribute
	public String getCamouflageTile() {
		return camouflageTile;
	}
	@XmlAttribute
	public boolean getCanMoveOntoAvatar() {
		return canMoveOntoAvatar;
	}
	@XmlAttribute
	public boolean getCanMoveOntoCreatures() {
		return canMoveOntoCreatures;
	}
	@XmlAttribute
	public boolean getCantattack() {
		return cantattack;
	}
	@XmlAttribute
	public String getCasts() {
		return casts;
	}
	public CreatureStatus getDamageStatus() {
		return damageStatus;
	}
	@XmlAttribute
	public boolean getDivides() {
		return divides;
	}
	@XmlAttribute
	public int getEncounterSize() {
		return encounterSize;
	}
	@XmlAttribute
	public int getExp() {
		return exp;
	}
	@XmlAttribute
	public boolean getFlies() {
		return flies;
	}
	@XmlAttribute
	public boolean getForceOfNature() {
		return forceOfNature;
	}
	@XmlAttribute
	public boolean getGood() {
		return good;
	}
	@XmlAttribute
	public int getId() {
		return id;
	}
	@XmlAttribute
	public boolean getIncorporeal() {
		return incorporeal;
	}
	@XmlAttribute
	public int getLeader() {
		return leader;
	}
	@XmlAttribute
	public boolean getLeavestile() {
		return leavestile;
	}
	@XmlAttribute
	public String getMovement() {
		return movement;
	}
	@XmlAttribute
	public String getName() {
		return name;
	}
	@XmlAttribute
	public boolean getNochest() {
		return nochest;
	}
	@XmlAttribute
	public boolean getPoisons() {
		return poisons;
	}
	@XmlAttribute
	public boolean getRanged() {
		return ranged;
	}
	@XmlAttribute
	public String getRangedhittile() {
		return rangedhittile;
	}
	@XmlAttribute
	public String getRangedmisstile() {
		return rangedmisstile;
	}
	@XmlAttribute
	public String getResists() {
		return resists;
	}
	@XmlAttribute
	public boolean getSails() {
		return sails;
	}
	@XmlAttribute
	public boolean getSpawnsOnDeath() {
		return spawnsOnDeath;
	}
	@XmlAttribute
	public String getSpawntile() {
		return spawntile;
	}
	@XmlAttribute
	public String getSteals() {
		return steals;
	}
	@XmlAttribute
	public boolean getSwims() {
		return swims;
	}
	
	
	
	
	@XmlAttribute
	public boolean getTeleports() {
		return teleports;
	}
	@XmlAttribute
	public String getTile() {
		return tile;
	}
	@XmlAttribute
	public boolean getUndead() {
		return undead;
	}
	@XmlAttribute
	public boolean getWontattack() {
		return wontattack;
	}
	@XmlAttribute
	public String getWorldrangedtile() {
		return worldrangedtile;
	}
	public boolean isAsleep() {
		return status.contains(StatusType.STAT_SLEEPING);
	}
	public Creature nearestOpponent(int dist, boolean ranged) {
		return null;
	}
	public void putToSleep() {
		if (!status.contains(StatusType.STAT_DEAD)) {
			status.add(StatusType.STAT_SLEEPING);
		}
	}
	public void removeStatus(StatusType status) {
		this.status.remove(status);
	}
	public void addStatus(StatusType status) {
		this.status.add(status);
	}
	public void setAmbushes(boolean ambushes) {
		this.ambushes = ambushes;
	}
	public void setBasehp(int basehp) {
		this.basehp = basehp;
	}
	public void setCamouflage(boolean camouflage) {
		this.camouflage = camouflage;
	}
	public void setCamouflageTile(String camouflageTile) {
		this.camouflageTile = camouflageTile;
	}
	public void setCanMoveOntoAvatar(boolean canMoveOntoAvatar) {
		this.canMoveOntoAvatar = canMoveOntoAvatar;
	}
	public void setCanMoveOntoCreatures(boolean canMoveOntoCreatures) {
		this.canMoveOntoCreatures = canMoveOntoCreatures;
	}
	public void setCantattack(boolean cantattack) {
		this.cantattack = cantattack;
	}
	public void setCasts(String casts) {
		this.casts = casts;
	}
	public void setDamageStatus(CreatureStatus damageStatus) {
		this.damageStatus = damageStatus;
	}
	public void setDivides(boolean divides) {
		this.divides = divides;
	}
	public void setEncounterSize(int encounterSize) {
		this.encounterSize = encounterSize;
	}
	public void setExp(int exp) {
		this.exp = exp;
	}
	public void setFlies(boolean flies) {
		this.flies = flies;
	}
	public void setForceOfNature(boolean forceOfNature) {
		this.forceOfNature = forceOfNature;
	}
	public void setGood(boolean good) {
		this.good = good;
	}
	public void setId(int id) {
		this.id = id;
	}
	public void setIncorporeal(boolean incorporeal) {
		this.incorporeal = incorporeal;
	}
	public void setLeader(int leader) {
		this.leader = leader;
	}
	public void setLeavestile(boolean leavestile) {
		this.leavestile = leavestile;
	}
	public void setMovement(String movement) {
		this.movement = movement;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setNochest(boolean nochest) {
		this.nochest = nochest;
	}
	public void setPoisons(boolean poisons) {
		this.poisons = poisons;
	}
	public void setRanged(boolean ranged) {
		this.ranged = ranged;
	}
	public void setRangedhittile(String rangedhittile) {
		this.rangedhittile = rangedhittile;
	}
	public void setRangedmisstile(String rangedmisstile) {
		this.rangedmisstile = rangedmisstile;
	}
	public void setResists(String resists) {
		this.resists = resists;
	}
	public void setSails(boolean sails) {
		this.sails = sails;
	}
	public void setSpawnsOnDeath(boolean spawnsOnDeath) {
		this.spawnsOnDeath = spawnsOnDeath;
	}
	
	public void setSpawntile(String spawntile) {
		this.spawntile = spawntile;
	}

	public void setSteals(String steals) {
		this.steals = steals;
	}
	
	public void setSwims(boolean swims) {
		this.swims = swims;
	}
	
	public void setTeleports(boolean teleports) {
		this.teleports = teleports;
	}

	public void setTile(String tile) {
		this.tile = tile;
	}

	public void setUndead(boolean undead) {
		this.undead = undead;
	}

	public void setWontattack(boolean wontattack) {
		this.wontattack = wontattack;
	}

	public void setWorldrangedtile(String worldrangedtile) {
		this.worldrangedtile = worldrangedtile;
	}

	public void wakeUp() {
		this.status.remove(StatusType.STAT_SLEEPING);
	}


}
