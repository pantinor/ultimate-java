package vendor;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "intro")
public class Intro {
	
	private VendorMusic music;
	private Zstats zstats;
	private IntroText text;

	@XmlElement(name="ztats")
	public Zstats getZstats() {
		return zstats;
	}
	@XmlElement(name="music")
	public VendorMusic getMusic() {
		return music;
	}
	@XmlElement(name="text")
	public IntroText getText() {
		return text;
	}
	public void setMusic(VendorMusic music) {
		this.music = music;
	}
	public void setZstats(Zstats zstats) {
		this.zstats = zstats;
	}
	public void setText(IntroText text) {
		this.text = text;
	}
	
	

	

}
