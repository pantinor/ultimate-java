package vendor;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "script")
public class Script {
	private String id;
	private Intro intro;
	private String noun;
	
	@XmlElement(name = "intro")
	public Intro getIntro() {
		return intro;
	}
	public void setId(String id) {
		this.id = id;
	}
	public void setIntro(Intro intro) {
		this.intro = intro;
	}
	@XmlAttribute(name="id")
	public String getId() {
		return id;
	}
	@XmlAttribute(name="noun")
	public String getNoun() {
		return noun;
	}
	public void setNoun(String noun) {
		this.noun = noun;
	}


	

	

	

	


}
