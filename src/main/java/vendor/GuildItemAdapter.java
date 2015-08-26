package vendor;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import ultima.Constants.GuildItemType;

public class GuildItemAdapter extends XmlAdapter<String, GuildItemType> {

    public String marshal(GuildItemType t) {
        return t.toString();
    }

    public GuildItemType unmarshal(String val) {
        return GuildItemType.valueOf(val);
    }
}
