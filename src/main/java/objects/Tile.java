package objects;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.badlogic.gdx.graphics.Texture;

@XmlRootElement(name = "tile")
public class Tile {

	private String name;
	private String rule;
	private int frames;
	private String animation;
	private int index;
	private String directions;
	private int rowtoswap;
	private boolean opaque;
	private boolean tiledInDungeon;
	private boolean usesReplacementTileAsBackground;
	private boolean usesWaterReplacementTileAsBackground;
	private Texture image;
	private String altImage;
	
	@XmlAttribute
	public String getName() {
		return name;
	}
	@XmlAttribute
	public String getRule() {
		return rule;
	}
	@XmlAttribute
	public int getFrames() {
		return frames;
	}
	@XmlAttribute
	public String getAnimation() {
		return animation;
	}
	@XmlAttribute
	public int getIndex() {
		return index;
	}
	@XmlAttribute
	public String getDirections() {
		return directions;
	}
	@XmlAttribute
	public int getRowtoswap() {
		return rowtoswap;
	}
	@XmlAttribute
	public boolean isOpaque() {
		return opaque;
	}

	@XmlAttribute
	public boolean getTiledInDungeon() {
		return tiledInDungeon;
	}
	@XmlAttribute
	public boolean getUsesReplacementTileAsBackground() {
		return usesReplacementTileAsBackground;
	}
	@XmlAttribute
	public boolean getUsesWaterReplacementTileAsBackground() {
		return usesWaterReplacementTileAsBackground;
	}
	
	
	public void setName(String name) {
		this.name = name;
	}
	public void setRule(String rule) {
		this.rule = rule;
	}
	public void setFrames(int frames) {
		this.frames = frames;
	}
	public void setAnimation(String animation) {
		this.animation = animation;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public void setDirections(String directions) {
		this.directions = directions;
	}
	public void setRowtoswap(int rowtoswap) {
		this.rowtoswap = rowtoswap;
	}
	public void setOpaque(boolean opaque) {
		this.opaque = opaque;
	}

	public void setTiledInDungeon(boolean tiledInDungeon) {
		this.tiledInDungeon = tiledInDungeon;
	}
	public void setUsesReplacementTileAsBackground(boolean usesReplacementTileAsBackground) {
		this.usesReplacementTileAsBackground = usesReplacementTileAsBackground;
	}
	public void setUsesWaterReplacementTileAsBackground(boolean usesWaterReplacementTileAsBackground) {
		this.usesWaterReplacementTileAsBackground = usesWaterReplacementTileAsBackground;
	}
	
	
	public Texture getImage() {
		return image;
	}
	public void setImage(Texture image) {
		this.image = image;
	}
	@XmlAttribute(name="image")
	public String getAltImage() {
		return altImage;
	}
	public void setAltImage(String altImage) {
		this.altImage = altImage;
	}
	@Override
	public String toString() {
		return String.format("Tile [name=%s, index=%s]", name, index);
	}
	
	public boolean enterable() {
		switch(index) {
		case 9:
		case 10:
		case 11:
		case 12:
		case 14:
		case 29:
		case 30:
		{
			return true;
		}
		default:
			
		}
		
		return false;
	}
	
	public boolean climbable() {
		switch(index) {
		case 27:
		case 28:
		{
			return true;
		}
		default:
			
		}
		
		return false;
	}
	

}
