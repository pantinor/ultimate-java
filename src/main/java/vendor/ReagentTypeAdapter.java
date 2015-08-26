package vendor;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import ultima.Constants.Reagent;

public class ReagentTypeAdapter extends XmlAdapter<String, Reagent> {

    public String marshal(Reagent t) {
        return t.toString();
    }

    public Reagent unmarshal(String val) {
        return Reagent.valueOf(val);
    }
}
