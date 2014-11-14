package objects;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "weapon")
public class Weapon {

	private String name;
	private String abbr;
	private int range;
	private int absolute_range;
	private int damage;
	private boolean attackthroughobjects;
	private boolean choosedistance;
	private boolean dontshowtravel;
	private String hittile;
	private String leavetile;
	private boolean lose;
	private boolean losewhenranged;
	private boolean magic;
	private String misstile;
	private boolean returns;
	private List<Constraint> constraints;
	
	@XmlAttribute
	public String getName() {
		return name;
	}
	@XmlAttribute
	public String getAbbr() {
		return abbr;
	}
	@XmlAttribute
	public int getRange() {
		return range;
	}
	@XmlAttribute
	public int getAbsolute_range() {
		return absolute_range;
	}
	@XmlAttribute
	public int getDamage() {
		return damage;
	}
	@XmlAttribute
	public boolean getAttackthroughobjects() {
		return attackthroughobjects;
	}
	@XmlAttribute
	public boolean getChoosedistance() {
		return choosedistance;
	}
	@XmlAttribute
	public boolean getDontshowtravel() {
		return dontshowtravel;
	}
	@XmlAttribute
	public String getHittile() {
		return hittile;
	}
	@XmlAttribute
	public String getLeavetile() {
		return leavetile;
	}
	@XmlAttribute
	public boolean getLose() {
		return lose;
	}
	@XmlAttribute
	public boolean getLosewhenranged() {
		return losewhenranged;
	}
	@XmlAttribute
	public boolean getMagic() {
		return magic;
	}
	@XmlAttribute
	public String getMisstile() {
		return misstile;
	}
	@XmlAttribute
	public boolean getReturns() {
		return returns;
	}
	@XmlElement(name="constraint")
	public List<Constraint> getConstraints() {
		return constraints;
	}
	
	
	public void setName(String name) {
		this.name = name;
	}
	public void setAbbr(String abbr) {
		this.abbr = abbr;
	}
	public void setRange(int range) {
		this.range = range;
	}
	public void setAbsolute_range(int absolute_range) {
		this.absolute_range = absolute_range;
	}
	public void setDamage(int damage) {
		this.damage = damage;
	}
	public void setAttackthroughobjects(boolean attackthroughobjects) {
		this.attackthroughobjects = attackthroughobjects;
	}
	public void setChoosedistance(boolean choosedistance) {
		this.choosedistance = choosedistance;
	}
	public void setDontshowtravel(boolean dontshowtravel) {
		this.dontshowtravel = dontshowtravel;
	}
	public void setHittile(String hittile) {
		this.hittile = hittile;
	}
	public void setLeavetile(String leavetile) {
		this.leavetile = leavetile;
	}
	public void setLose(boolean lose) {
		this.lose = lose;
	}
	public void setLosewhenranged(boolean losewhenranged) {
		this.losewhenranged = losewhenranged;
	}
	public void setMagic(boolean magic) {
		this.magic = magic;
	}
	public void setMisstile(String misstile) {
		this.misstile = misstile;
	}
	public void setReturns(boolean returns) {
		this.returns = returns;
	}

	public void setConstraints(List<Constraint> constraints) {
		this.constraints = constraints;
	}
	
	@Override
	public String toString() {
		return String
				.format("Weapon [name=%s, abbr=%s, range=%s, absolute_range=%s, damage=%s, attackthroughobjects=%s, choosedistance=%s, dontshowtravel=%s, hittile=%s, leavetile=%s, lose=%s, losewhenranged=%s, magic=%s, misstile=%s, returns=%s, constraints=%s]",
						name, abbr, range, absolute_range, damage, attackthroughobjects, choosedistance, dontshowtravel, hittile, leavetile, lose, losewhenranged, magic, misstile, returns, constraints);
	}

	

	
	
	

}
