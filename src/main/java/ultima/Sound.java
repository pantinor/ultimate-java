package ultima;

public enum Sound {
		
	MAGIC ("magic.ogg", false, 0.3f), 
	ATTACK ("attack.ogg", false, 0.3f), 
	MOONGATE ("moongate_flash.ogg", false, 0.3f), 
	BLOCKED ("blocked.ogg", false, 0.3f),
	FLEE ("flee.ogg", false, 0.3f),
	ERROR ("error.ogg", false, 0.3f),
	
	PC_ATTACK ("pc_attack.ogg", false, 0.3f),
	PC_STRUCK ("pc_struck.ogg", false, 0.3f),
	NPC_ATTACK ("npc_attack.ogg", false, 0.3f),
	NPC_STRUCK ("npc_struck.ogg", false, 0.3f),
	NPC_MAGIC ("enemy_magic_proj.ogg", false, 0.3f),
	NPC_MAGIC_STRUCK ("enemy_magic_proj_hit.ogg", false, 0.3f),
	
	EVADE ("evade.ogg", false, 0.3f),
	POISON_EFFECT ("Poison.ogg", false, 0.3f),
	POISON_DAMAGE ("poison_damage.ogg", false, 0.3f),
	SLEEP ("Hypnosis.ogg", false, 0.3f),
	BOOM ("boom.ogg", false, 0.3f),
	HEALING ("HealingMini.ogg", false, 0.3f),
	POSITIVE_EFFECT ("PositiveEffect.ogg", false, 0.3f),
	NEGATIVE_EFFECT ("NegativeEffect.ogg", false, 0.3f),
	STEAL_ESSENCE ("StealEssence.ogg", false, 0.3f),
	TREMOR ("Armageddon.ogg", false, 0.3f),
	ACID ("AcidicRain.ogg", false, 0.3f),
	LIGHTNING ("LightningBolt.ogg", false, 0.3f),
	FIREBALL ("Fireball.ogg", false, 0.3f),

	//music
	OUTSIDE ("Wanderer.mp3", false, 0.1f), 
	TOWNS ("Towns.mp3", false, 0.1f), 
	SHRINES ("Shrines.mp3", false, 0.1f), 
	SHOPPING ("Shopping.mp3", false, 0.1f), 
	RULEBRIT ("Rule_Britannia.mp3", false, 0.1f), 
	FANFARE ("Fanfare_Of_Lord_British.mp3", false, 0.1f), 
	DUNGEON ("Dungeon.mp3", false, 0.1f), 
	COMBAT ("Combat.mp3", false, 0.1f), 
	CASTLES ("Castles.mp3", false, 0.1f);
	
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
