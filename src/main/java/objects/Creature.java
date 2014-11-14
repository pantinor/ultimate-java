package objects;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "creature")
public class Creature {

	private int id;
	private int encounterSize;
	private String name;
	private String tile;
	private int basehp;
	private int exp;
	private int leader;
	private boolean ambushes;
	private boolean camouflage;
	private String camouflageTile;
	private boolean canMoveOntoAvatar;
	private boolean canMoveOntoCreatures;
	private boolean cantattack;
	private String casts;
	private boolean divides;
	private boolean spawnsOnDeath;
	private boolean flies;
	private boolean good;
	private boolean incorporeal;
	private boolean leavestile;
	private String movement;
	private boolean nochest;
	private boolean poisons;
	private boolean ranged;
	private String rangedhittile;
	private String rangedmisstile;
	private String resists;
	private boolean sails;
	private String spawntile;
	private String steals;
	private boolean swims;
	private boolean teleports;
	private boolean undead;
	private boolean wontattack;
	private String worldrangedtile;
	private boolean forceOfNature;
	
	@XmlAttribute
	public int getId() {
		return id;
	}
	@XmlAttribute
	public int getEncounterSize() {
		return encounterSize;
	}
	@XmlAttribute
	public String getName() {
		return name;
	}
	@XmlAttribute
	public String getTile() {
		return tile;
	}
	@XmlAttribute
	public int getBasehp() {
		return basehp;
	}
	@XmlAttribute
	public int getExp() {
		return exp;
	}
	@XmlAttribute
	public int getLeader() {
		return leader;
	}
	@XmlAttribute
	public boolean getAmbushes() {
		return ambushes;
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
	@XmlAttribute
	public boolean getDivides() {
		return divides;
	}
	@XmlAttribute
	public boolean getSpawnsOnDeath() {
		return spawnsOnDeath;
	}
	@XmlAttribute
	public boolean getFlies() {
		return flies;
	}
	@XmlAttribute
	public boolean getGood() {
		return good;
	}
	@XmlAttribute
	public boolean getIncorporeal() {
		return incorporeal;
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
	@XmlAttribute
	public boolean getForceOfNature() {
		return forceOfNature;
	}
	
	
	
	
	public void setId(int id) {
		this.id = id;
	}
	public void setEncounterSize(int encounterSize) {
		this.encounterSize = encounterSize;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setTile(String tile) {
		this.tile = tile;
	}
	public void setBasehp(int basehp) {
		this.basehp = basehp;
	}
	public void setExp(int exp) {
		this.exp = exp;
	}
	public void setLeader(int leader) {
		this.leader = leader;
	}
	public void setAmbushes(boolean ambushes) {
		this.ambushes = ambushes;
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
	public void setDivides(boolean divides) {
		this.divides = divides;
	}
	public void setSpawnsOnDeath(boolean spawnsOnDeath) {
		this.spawnsOnDeath = spawnsOnDeath;
	}
	public void setFlies(boolean flies) {
		this.flies = flies;
	}
	public void setGood(boolean good) {
		this.good = good;
	}
	public void setIncorporeal(boolean incorporeal) {
		this.incorporeal = incorporeal;
	}
	public void setLeavestile(boolean leavestile) {
		this.leavestile = leavestile;
	}
	public void setMovement(String movement) {
		this.movement = movement;
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
	public void setUndead(boolean undead) {
		this.undead = undead;
	}
	public void setWontattack(boolean wontattack) {
		this.wontattack = wontattack;
	}
	public void setWorldrangedtile(String worldrangedtile) {
		this.worldrangedtile = worldrangedtile;
	}
	public void setForceOfNature(boolean forceOfNature) {
		this.forceOfNature = forceOfNature;
	}
	
	@Override
	public String toString() {
		return String
				.format("Creature [id=%s, encounterSize=%s, name=%s, tile=%s, basehp=%s, exp=%s, leader=%s, ambushes=%s, camouflage=%s, camouflageTile=%s, canMoveOntoAvatar=%s, canMoveOntoCreatures=%s, cantattack=%s, casts=%s, divides=%s, spawnsOnDeath=%s, flies=%s, good=%s, incorporeal=%s, leavestile=%s, movement=%s, nochest=%s, poisons=%s, ranged=%s, rangedhittile=%s, rangedmisstile=%s, resists=%s, sails=%s, spawntile=%s, steals=%s, swims=%s, teleports=%s, undead=%s, wontattack=%s, worldrangedtile=%s, forceOfNature=%s]",
						id, encounterSize, name, tile, basehp, exp, leader, ambushes, camouflage, camouflageTile, canMoveOntoAvatar, canMoveOntoCreatures, cantattack, casts, divides, spawnsOnDeath, flies, good, incorporeal, leavestile, movement,
						nochest, poisons, ranged, rangedhittile, rangedmisstile, resists, sails, spawntile, steals, swims, teleports, undead, wontattack, worldrangedtile, forceOfNature);
	}
	
	

}
