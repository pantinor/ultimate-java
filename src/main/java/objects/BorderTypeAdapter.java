package objects;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import ultima.Constants.MapBorderBehavior;

public class BorderTypeAdapter extends XmlAdapter<String, MapBorderBehavior> {

    public String marshal(MapBorderBehavior t) {
        return t.toString();
    }

    public MapBorderBehavior unmarshal(String val) {
        return MapBorderBehavior.valueOf(val);
    }
}
