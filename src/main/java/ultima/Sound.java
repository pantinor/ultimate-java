package ultima;

public enum Sound {
		
	BACKGROUND1 ("sound/combat1.ogg", false, 0.1f), 
	BACKGROUND2 ("sound/combat2.ogg", false, 0.1f), 
	BACKGROUND3 ("sound/combat3.ogg", false, 0.1f), 
	
	POSITIVE_EFFECT ("sound/PositiveEffect.ogg", false, 0.3f),
	NEGATIVE_EFFECT ("sound/NegativeEffect.ogg", false, 0.3f),

	MAGIC ("sound/magic.ogg", false, 0.3f), 
	ATTACK ("sound/attack.ogg", false, 0.3f), 
	MOONGATE ("sound/moongate_flash.ogg", false, 0.3f), 
	SUMMONED ("sound/summon1.ogg", false, 0.3f);
	
	String file;
	boolean looping;
	float volume;
	
	private Sound(String name, boolean looping, float volume) {
		this.file = name;
		this.looping = looping;
		this.volume = volume;
	}
	
	public String getFile() {
		return this.file;
	}
	
	public boolean getLooping() {
		return this.looping;
	}
	
	public float getVolume() {
		return this.volume;
	}
	
}
