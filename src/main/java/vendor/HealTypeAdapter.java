package vendor;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import ultima.Constants.HealType;

public class HealTypeAdapter extends XmlAdapter<String, HealType> {

    public String marshal(HealType t) {
        return t.toString();
    }

    public HealType unmarshal(String val) {
        return HealType.valueOf(val);
    }
}
