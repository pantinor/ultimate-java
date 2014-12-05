package objects;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import ultima.Constants.CreatureType;

public class CreatureTypeAdapter extends XmlAdapter<String, CreatureType> {

	public String marshal(CreatureType t) {
		return t.toString();
	}

	public CreatureType unmarshal(String val) {
		return CreatureType.valueOf(val);
	}
}