package graphics;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "tileanim")
public class TileAnim {
	
	private String name;
	private int random;
	
	private List<Context> contexts;
	private List<Transform> transforms;
	@XmlElement(name="transform")
	public List<Transform> getTransforms() {
		return transforms;
	}
	public void setTransforms(List<Transform> transforms) {
		this.transforms = transforms;
	}
	@XmlElement(name="context")
	public List<Context> getContexts() {
		return contexts;
	}
	public void setContexts(List<Context> contexts) {
		this.contexts = contexts;
	}
	@XmlAttribute
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@XmlAttribute
	public int getRandom() {
		return random;
	}
	public void setRandom(int random) {
		this.random = random;
	}
	
	
	

}
