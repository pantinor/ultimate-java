package graphics;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "image")
public class Image {
	
	private String name;
	private String filename;
	private int width;
	private int height;
	private int depth;
	private int prescale;
	private String filetype;
	private int tiles;
	private boolean introOnly;
	private int transparentIndex;
	private boolean xu4Graphic;
	private String fixup;
	private List<SubImage> subimages;
	
	@XmlAttribute
	public String getName() {
		return name;
	}
	@XmlAttribute
	public String getFilename() {
		return filename;
	}
	@XmlAttribute
	public int getWidth() {
		return width;
	}
	@XmlAttribute
	public int getHeight() {
		return height;
	}
	@XmlAttribute
	public int getDepth() {
		return depth;
	}
	@XmlAttribute
	public int getPrescale() {
		return prescale;
	}
	@XmlAttribute
	public String getFiletype() {
		return filetype;
	}
	@XmlAttribute
	public int getTiles() {
		return tiles;
	}
	@XmlAttribute
	public boolean getIntroOnly() {
		return introOnly;
	}
	@XmlAttribute
	public int getTransparentIndex() {
		return transparentIndex;
	}
	@XmlAttribute
	public boolean getXu4Graphic() {
		return xu4Graphic;
	}
	@XmlAttribute
	public String getFixup() {
		return fixup;
	}
	@XmlElement(name="subimage")
	public List<SubImage> getSubimages() {
		return subimages;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public void setDepth(int depth) {
		this.depth = depth;
	}
	public void setPrescale(int prescale) {
		this.prescale = prescale;
	}
	public void setFiletype(String filetype) {
		this.filetype = filetype;
	}
	public void setTiles(int tiles) {
		this.tiles = tiles;
	}
	public void setIntroOnly(boolean introOnly) {
		this.introOnly = introOnly;
	}
	public void setTransparentIndex(int transparentIndex) {
		this.transparentIndex = transparentIndex;
	}
	public void setXu4Graphic(boolean xu4Graphic) {
		this.xu4Graphic = xu4Graphic;
	}
	public void setFixup(String fixup) {
		this.fixup = fixup;
	}
	public void setSubimages(List<SubImage> subimages) {
		this.subimages = subimages;
	}
	
	
	
}
