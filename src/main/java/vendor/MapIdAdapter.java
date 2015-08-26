package vendor;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import ultima.Constants.Maps;

public class MapIdAdapter extends XmlAdapter<String, Maps> {

    public String marshal(Maps t) {
        return t.toString();
    }

    public Maps unmarshal(String val) {
        return Maps.valueOf(val);
    }
}
