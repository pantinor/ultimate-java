package ultima;

public enum Sound {
		
	MAGIC ("sound/magic.ogg", false, 0.3f), 
	ATTACK ("sound/attack.ogg", false, 0.3f), 
	MOONGATE ("sound/moongate_flash.ogg", false, 0.3f), 
	BLOCKED ("sound/blocked.ogg", false, 0.3f),
	FLEE ("sound/flee.ogg", false, 0.3f),

	PC_ATTACK ("sound/pc_attack.ogg", false, 0.3f),
	PC_STRUCK ("sound/pc_struck.ogg", false, 0.3f),
	NPC_ATTACK ("sound/npc_attack.ogg", false, 0.3f),
	NPC_STRUCK ("sound/npc_struck.ogg", false, 0.3f),
	EVADE ("sound/evade.ogg", false, 0.3f),
	ERROR ("sound/error.ogg", false, 0.3f),
	POISON_EFFECT ("sound/poison_effect.ogg", false, 0.3f),
	POISON_DAMAGE ("sound/poison_damage.ogg", false, 0.3f),
	SLEEP ("sound/reaper_sleeper.ogg", false, 0.3f),


	//music
	OUTSIDE ("sound/Wanderer.mp3", false, 0.1f), 
	TOWNS ("sound/Towns.mp3", false, 0.1f), 
	SHRINES ("sound/Shrines.mp3", false, 0.1f), 
	SHOPPING ("sound/Shopping.mp3", false, 0.1f), 
	RULEBRIT ("sound/Rule_Britannia.mp3", false, 0.1f), 
	FANFARE ("sound/Fanfare_Of_Lord_British.mp3", false, 0.1f), 
	DUNGEON ("sound/Dungeon.mp3", false, 0.1f), 
	COMBAT ("sound/Combat.mp3", false, 0.1f), 
	CASTLES ("sound/Castles.mp3", false, 0.1f);
	
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
