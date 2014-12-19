package objects;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import ultima.Constants.Direction;

public class DirectionTypeAdapter extends XmlAdapter<String, Direction> {

	public String marshal(Direction t) {
		return t.toString();
	}

	public Direction unmarshal(String val) {
		return Direction.valueOf(val);
	}
}
