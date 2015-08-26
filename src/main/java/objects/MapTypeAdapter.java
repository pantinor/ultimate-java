package objects;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import ultima.Constants.MapType;

public class MapTypeAdapter extends XmlAdapter<String, MapType> {

    public String marshal(MapType t) {
        return t.toString();
    }

    public MapType unmarshal(String val) {
        return MapType.valueOf(val);
    }
}
