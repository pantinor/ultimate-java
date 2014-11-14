package objects;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "shrine")
public class Shrine {
	private String mantra;
	private String virtue;
	@XmlAttribute
	public String getMantra() {
		return mantra;
	}
	@XmlAttribute
	public String getVirtue() {
		return virtue;
	}
	public void setMantra(String mantra) {
		this.mantra = mantra;
	}
	public void setVirtue(String virtue) {
		this.virtue = virtue;
	}
	@Override
	public String toString() {
		return String.format("Shrine [mantra=%s, virtue=%s]", mantra, virtue);
	}
	
	

}
