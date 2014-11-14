package graphics;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "context")
public class Context {
	
	private String type;
	private int frame;
	private String dir;
	private List<Transform> transforms;
	@XmlAttribute
	public String getType() {
		return type;
	}
	@XmlAttribute
	public int getFrame() {
		return frame;
	}
	@XmlAttribute
	public String getDir() {
		return dir;
	}
	public void setType(String type) {
		this.type = type;
	}
	public void setFrame(int frame) {
		this.frame = frame;
	}
	public void setDir(String dir) {
		this.dir = dir;
	}
	@XmlElement(name="transform")
	public List<Transform> getTransforms() {
		return transforms;
	}
	public void setTransforms(List<Transform> transforms) {
		this.transforms = transforms;
	}
	
	

}
