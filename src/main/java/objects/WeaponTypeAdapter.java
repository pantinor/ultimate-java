package objects;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import ultima.Constants.WeaponType;

public class WeaponTypeAdapter extends XmlAdapter<String, WeaponType> {

    public String marshal(WeaponType t) {
        return t.toString();
    }

    public WeaponType unmarshal(String val) {
        return WeaponType.valueOf(val);
    }
}
