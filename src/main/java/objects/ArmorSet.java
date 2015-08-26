package objects;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import ultima.Constants.ArmorType;

@XmlRootElement(name = "armors")
public class ArmorSet {

    private List<Armor> armors;

    @XmlElement(name = "armor")
    public List<Armor> getArmors() {
        return armors;
    }

    public void setArmors(List<Armor> armors) {
        this.armors = armors;
    }

    public void init() {
        for (Armor a : this.armors) {
            ArmorType t = a.getType();
            t.setArmor(a);
        }
    }

}
