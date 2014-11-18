package objects;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "tileRules")
public class TileRules {
	
	private List<Rule> rules = null;

	@XmlElement(name = "rule")
	public List<Rule> getRules() {
		return rules;
	}

	public void setRules(List<Rule> rules) {
		this.rules = rules;
	}
	
	public Rule getRule(String name) {
		if (name == null) {
			return null;
		}
		for (Rule r : rules) {
			if (r.getName().equals(name)) {
				return r;
			}
		}
		return null;
	}
	

}
