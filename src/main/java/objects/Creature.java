package objects;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.Vector3;

import ultima.Constants;

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
	private String steals;
	private boolean swims;
	private boolean teleports;
	
	private boolean undead;
	private boolean wontattack;
	private String worldrangedtile;
	
	private CreatureType tile;
	private CreatureStatus status = CreatureStatus.FINE;
	private Animation anim;
	public int currentX;
	public int currentY;
	public Vector3 currentPos; //in pixels

	public Creature() {
		
	}
	
	public Creature(Creature clone) {
		this.ambushes = clone.ambushes;
		this.basehp = clone.basehp;
		this.camouflage = clone.camouflage;
		this.camouflageTile = clone.camouflageTile;
		this.canMoveOntoAvatar = clone.canMoveOntoAvatar;
		this.canMoveOntoCreatures = clone.canMoveOntoCreatures;
		this.cantattack = clone.cantattack;
		this.casts = clone.casts;
		this.divides = clone.divides;
		this.encounterSize = clone.encounterSize;
		this.exp = clone.exp;
		this.flies = clone.flies;
		this.forceOfNature = clone.forceOfNature;
		this.good = clone.good;
		this.hp = clone.hp;
		this.id = clone.id;
		this.incorporeal = clone.incorporeal;
		this.leader = clone.leader;
		this.leavestile = clone.leavestile;
		this.movement = clone.movement;
		this.name = clone.name;
		this.nochest = clone.nochest;
		this.poisons = clone.poisons;
		this.ranged = clone.ranged;
		this.rangedhittile = clone.rangedhittile;
		this.rangedmisstile = clone.rangedmisstile;
		this.resists = clone.resists;
		this.sails = clone.sails;
		this.spawnsOnDeath = clone.spawnsOnDeath;
		this.spawntile = clone.spawntile;
		this.steals = clone.steals;
		this.swims = clone.swims;
		this.teleports = clone.teleports;
		this.undead = clone.undead;
		this.wontattack = clone.wontattack;
		this.worldrangedtile = clone.worldrangedtile;
		this.tile = clone.tile;
		this.status = clone.status;
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
	@XmlAttribute(name="tile")
	@XmlJavaTypeAdapter(CreatureTypeAdapter.class)
	public CreatureType getTile() {
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

	public void setTile(CreatureType tile) {
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
	public CreatureStatus getStatus() {
		return status;
	}
	public void setStatus(CreatureStatus status) {
		this.status = status;
	}

	public Animation getAnim() {
		return anim;
	}

	public void setAnim(Animation anim) {
		this.anim = anim;
	}

	@Override
	public String toString() {
		return String.format("Creature [id=%s, name=%s, tile=%s]", id, name, tile);
	}


}
