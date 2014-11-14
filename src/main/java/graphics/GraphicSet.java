package graphics;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "graphics")
public class GraphicSet {
	private List<ImageSet> imagesets;
	private List<Layout> layouts;
	private List<TileAnimSet> tileAnimSets;

	@XmlElement(name="imageset")
	public List<ImageSet> getImagesets() {
		return imagesets;
	}

	public void setImagesets(List<ImageSet> imagesets) {
		this.imagesets = imagesets;
	}
	
	@XmlElement(name="layout")
	public List<Layout> getLayouts() {
		return layouts;
	}

	public void setLayouts(List<Layout> layouts) {
		this.layouts = layouts;
	}
	@XmlElement(name="tileanimset")
	public List<TileAnimSet> getTileAnimSets() {
		return tileAnimSets;
	}

	public void setTileAnimSets(List<TileAnimSet> tileAnimSets) {
		this.tileAnimSets = tileAnimSets;
	}
	

}
