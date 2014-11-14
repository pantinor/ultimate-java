package graphics;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "imageset")
public class ImageSet {
	private List<Image> images;
	private String setExtends;
	private String name;
	
	@XmlElement(name="image")
	public List<Image> getImages() {
		return images;
	}

	public void setImages(List<Image> images) {
		this.images = images;
	}
	
	@XmlAttribute(name="extends")
	public String getSetExtends() {
		return setExtends;
	}
	public void setSetExtends(String setExtends) {
		this.setExtends = setExtends;
	}
	
	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
