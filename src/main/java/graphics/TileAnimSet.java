package graphics;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "tileanimset")
public class TileAnimSet {
	private String name;
	private List<TileAnim> tileAnims;
	@XmlAttribute
	public String getName() {
		return name;
	}
	@XmlElement(name="tileanim")
	public List<TileAnim> getTileAnims() {
		return tileAnims;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setTileAnims(List<TileAnim> tileAnims) {
		this.tileAnims = tileAnims;
	}
	
	

}
