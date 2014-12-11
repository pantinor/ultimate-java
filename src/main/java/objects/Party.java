package objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import objects.SaveGame.SaveGamePlayerRecord;

import org.apache.commons.lang.StringUtils;

import ultima.Constants;
import util.Utils;


public class Party implements Constants {
	
	private SaveGame saveGame;
	private List<PartyMember> members = new ArrayList<PartyMember>();
	private int activePlayer = 0;
	private Tile transport;
	private int torchduration;
	private Random rand = new Random();
	
	public Party(SaveGame sg) {
		this.saveGame = sg;
		
		for (int i = 0;i<saveGame.members;i++) {
			members.add(new PartyMember(this, saveGame.players[i]));
		}
			
		
	}
	
	public void addMember(SaveGame.SaveGamePlayerRecord rec) throws Exception {
		if (rec == null) throw new Exception("Cannot add null record to party members!");
		members.add(new PartyMember(this, rec));
	}
	
	public List<PartyMember> getMembers() {
		return members;
	}
	
	public PartyMember getMember(int index) { 
		return members.get(index);
	}
	
	public SaveGame getSaveGame() {
		return saveGame;
	}

	public void setSaveGame(SaveGame saveGame) {
		this.saveGame = saveGame;
	}
	
	public int getActivePlayer() {
		return activePlayer;
	}

	public Tile getTransport() {
		return transport;
	}

	public int getTorchduration() {
		return torchduration;
	}

	public void setMembers(List<PartyMember> members) {
		this.members = members;
	}
	
	public PartyMember getActivePartyMember() {
		return members.get(activePlayer);
	}
	
	public int getAbleCombatPlayers() {
		int n = 0;
		for (int i=0;i<members.size();i++) {
			if (!members.get(i).isDisabled()) {
				n++;
			}
		}
		return n;
	}
	
	/**
	 * Gets the next active index without changing the active index
	 */
	public int getNextActiveIndex() {
		boolean allbad = true;
		for (int i=0;i<members.size();i++) {
			if (!members.get(i).isDisabled()) {
				allbad = false;
			}
		}
		if (allbad) return -1;
		
		int tmp = activePlayer;
		boolean flag = true;
		while (flag) {
			tmp ++;
			if (tmp >= members.size()) {
				tmp = 0;
			} 
			if (!members.get(tmp).isDisabled()) {
				flag = false;
			}
		}
		return tmp;
	}

	/**
	 * Gets and sets the next axctive player
	 */
	public PartyMember getAndSetNextActivePlayer() {
		
		boolean allbad = true;
		for (int i=0;i<members.size();i++) {
			if (!members.get(i).isDisabled()) {
				allbad = false;
			}
		}
		if (allbad) return null;
		
		PartyMember p = null;
		boolean flag = true;
		while (flag) {
			this.activePlayer ++;
			if (activePlayer >= members.size()) {
				activePlayer = 0;
			} 
			if (!members.get(activePlayer).isDisabled()) {
				p = members.get(activePlayer);
				flag = false;
			}
		}
		return p;
	}
	
	public void reset() {
		for (PartyMember pm : members) {
			pm.fled = false;
		}
		activePlayer  = 0;
	}

	public void setTransport(Tile transport) {
		this.transport = transport;
	}

	public void setTorchduration(int torchduration) {
		this.torchduration = torchduration;
	}
	
	public void adjustFood(int v) {
	    saveGame.food = Utils.adjustValue(saveGame.food, v, 999900, 0);
	}
	public void adjustGold(int v) {
	    saveGame.gold = Utils.adjustValue(saveGame.gold, v, 9999, 0);
	}

	public class PartyMember {
		
		private SaveGamePlayerRecord player;
		private Party party;
		
		public boolean fled;
		public Creature combatCr;

		
		public PartyMember(Party py, SaveGamePlayerRecord p) {
			this.party = py;
			this.player = p;
		}
		
		public CreatureStatus getDamagedState() {
			if (player.hp <= 0) {
				return CreatureStatus.DEAD;
			} else if (player.hp < 24) {
				return CreatureStatus.FLEEING;
			} else {
				return CreatureStatus.BARELYWOUNDED;
			}
		}
		
		public int getDamage() {
		    int maxDamage = player.weapon.getWeapon().getDamage();
		    maxDamage += player.str;
		    if (maxDamage > 255)
		        maxDamage = 255;
		    return rand.nextInt(maxDamage);
		}
			
		public void awardXP(int value) {
			int exp = Utils.adjustValueMax(player.xp, value, 9999);
			player.xp = exp;
		}
		
		public boolean heal(HealType type) {
			switch (type) {

			case NONE:
				return true;

			case CURE:
				if (player.status != StatusType.POISONED) {
					return false;
				}
				player.status = StatusType.GOOD;
				break;

			case FULLHEAL:
				if (player.status == StatusType.DEAD || player.hp == player.hpMax) {
					return false;
				}
				player.hp = player.hpMax;
				break;

			case RESURRECT:
				if (player.status != StatusType.DEAD) {
					return false;
				}
				player.status = StatusType.GOOD;
				break;

			case HEAL:
				if (player.status == StatusType.DEAD || player.hp == player.hpMax) {
					return false;
				}
				player.hp += 75 + (rand.nextInt(0x100) % 0x19);
				break;

			case CAMPHEAL:
				if (player.status == StatusType.DEAD || player.hp == player.hpMax) {
					return false;
				}
				player.hp += 99 + (rand.nextInt(0x100) & 0x77);
				break;

			case INNHEAL:
				if (player.status == StatusType.DEAD || player.hp == player.hpMax) {
					return false;
				}
				player.hp += 100 + (rand.nextInt(50) * 2);
				break;

			default:
				return false;
			}

			if (player.hp > player.hpMax) {
				player.hp = player.hpMax;
			}

			return true;
		}

		public Party getParty() {
			return party;
		}

		public SaveGamePlayerRecord getPlayer() {
			return player;
		}
		
		public Creature nearestOpponent(int dist, boolean ranged) {
			return null;
		}
		
		public boolean isDead() {
		    return player.status == StatusType.DEAD;
		}

		public boolean isDisabled() {
		    return ((player.status == StatusType.GOOD || player.status == StatusType.POISONED) && !fled) ? false : true;
		}

		/**
		 * Lose the equipped weapon for the player (flaming oil, ranged daggers, etc.)
		 * Returns the number of weapons left of that type, including the one in
		 * the players hand
		 */
		public WeaponType loseWeapon() {
		    int weapon = player.weapon.ordinal();
		    if (saveGame.weapons[weapon] > 0) {
		    	--saveGame.weapons[weapon];
		    	int w = saveGame.weapons[weapon] + 1;
		        return WeaponType.get(w);
		    } else {
		        player.weapon = WeaponType.HANDS;
		        return WeaponType.HANDS;
		    }
		}

		public void putToSleep() {    
		    if (!isDead()) {
		        //soundPlay(SOUND_SLEEP, false);
				player.status = StatusType.SLEEPING;    
		        //setTile(Tileset::findTileByName("corpse")->getId());
		    }
		}

		public void wakeUp() {
			player.status = StatusType.GOOD;    
		}
		
		public boolean applyDamage(int damage, boolean combatRelatedDamage) {
		    int newHp = player.hp;

		    if (isDead())
		        return false;

		    newHp -= damage;

		    if (newHp < 0) {
				player.status = StatusType.DEAD;    
		        newHp = 0;
		    }
		    
		    player.hp = newHp;

		    if (combatRelatedDamage && isDead()) {
		        //Coords p = getCoords();                    
		        //Map *map = getMap();
		        //map->annotations->add(p, Tileset::findTileByName("corpse")->getId())->setTTL(party->size() * 2);

		        /* remove yourself from the map */
		        //remove();        
		        return false;
		    }

		    return true;
		}
		
		public int getAttackBonus() {
		    if (player.dex >= 40) return 255;
		    return player.dex;
		}

		public int getDefense() {
		    return player.armor.getArmor().getDefense();
		}

	}
	
	public boolean isJoinedInParty(String name) {
		for (PartyMember pm : members) {
			if (pm.getPlayer().name.equals(name)) return true;
		}
		return false;
	}
	
	
	/**
	 * Determine of character name is joinable and return the virtue for that character.
	 */
	public Virtue getVirtueForJoinable(String name) {
		if (StringUtils.isEmpty(name)) {
			return null;
		}
		for (int i = 1; i < 8; i++) {
			if (name.equals(saveGame.players[i].name)) {
				return saveGame.players[i].klass.getVirtue();
			}
		}
		return null;
	}
	
	/**
	 * See if the character can join the party
	 */
	public CannotJoinError join(String name) {
		int i;
    
		for (i = saveGame.members; i < 8; i++) {
			if (name.equals(saveGame.players[i].name)) {
    
				/* ensure avatar is experienced enough */
				if (saveGame.members + 1 > (saveGame.players[0].hpMax / 100)) {
					return CannotJoinError.JOIN_NOT_EXPERIENCED;
				}
    
				/* ensure character has enough karma */
				if ((saveGame.karma[saveGame.players[i].klass.ordinal()] > 0) && 
					(saveGame.karma[saveGame.players[i].klass.ordinal()] < 40)) {
					return CannotJoinError.JOIN_NOT_VIRTUOUS;
				}
				
				//swap the positions in the saved game file , this is how we know if the NPC is a member of the party or not
				SaveGamePlayerRecord tmp = saveGame.players[saveGame.members];
				saveGame.players[saveGame.members] = saveGame.players[i];
				saveGame.players[i] = tmp;
        
				members.add(new PartyMember(this, saveGame.players[saveGame.members++]));
								
				return CannotJoinError.JOIN_SUCCEEDED;
			}
		}
    
		return CannotJoinError.JOIN_NOT_EXPERIENCED;
	}
	
	public void adjustKarma(KarmaAction action) {

		int timeLimited = 0;
		int[] newKarma = new int[8];
		int[] maxVal = new int[8];

		for (int v = 0; v < 8; v++) {
			newKarma[v] = saveGame.karma[v] == 0 ? 100 : saveGame.karma[v];
			maxVal[v] = saveGame.karma[v] == 0 ? 100 : 99;
		}

		switch (action) {
		case FOUND_ITEM:
			//only increment HONOR for found items
			adjustKarmaMax(newKarma, Virtue.HONOR, 5, maxVal);
			break;
		case STOLE_CHEST:
			adjustKarmaMin(newKarma, Virtue.HONESTY, -1, 1);
			adjustKarmaMin(newKarma, Virtue.JUSTICE, -1, 1);
			adjustKarmaMin(newKarma, Virtue.HONOR, -1, 1);
			break;
		case GAVE_ALL_TO_BEGGAR:
		case GAVE_TO_BEGGAR:
			timeLimited = 1;
			adjustKarmaMax(newKarma, Virtue.COMPASSION, 2, maxVal);
			break;
		case BRAGGED:
			adjustKarmaMin(newKarma, Virtue.HUMILITY, -5, 1);
			break;
		case HUMBLE:
			timeLimited = 1;
			adjustKarmaMax(newKarma, Virtue.HUMILITY, 10, maxVal);
			break;
		case HAWKWIND:
		case MEDITATION:
			timeLimited = 1;
			adjustKarmaMax(newKarma, Virtue.SPIRITUALITY, 3, maxVal);
			break;
		case BAD_MANTRA:
			adjustKarmaMin(newKarma, Virtue.SPIRITUALITY, -3, 1);
			break;
		case ATTACKED_GOOD:
			adjustKarmaMin(newKarma, Virtue.COMPASSION, -5, 1);
			adjustKarmaMin(newKarma, Virtue.JUSTICE, -5, 1);
			adjustKarmaMin(newKarma, Virtue.HONOR, -5, 1);
			break;
		case FLED_EVIL:
			adjustKarmaMin(newKarma, Virtue.VALOR, -2, 1);
			break;
		case HEALTHY_FLED_EVIL:
			adjustKarmaMin(newKarma, Virtue.VALOR, -2, 1);
			adjustKarmaMin(newKarma, Virtue.SACRIFICE, -2, 1);
			break;
		case KILLED_EVIL:
			Random rand = new Random();
			// gain one valor half the time, zero the rest
			adjustKarmaMax(newKarma, Virtue.VALOR, rand.nextInt(1), maxVal);
			break;
		case FLED_GOOD:
			adjustKarmaMax(newKarma, Virtue.COMPASSION, 2, maxVal);
			adjustKarmaMax(newKarma, Virtue.JUSTICE, 2, maxVal);
			break;
		case SPARED_GOOD:
			adjustKarmaMax(newKarma, Virtue.COMPASSION, 1, maxVal);
			adjustKarmaMax(newKarma, Virtue.JUSTICE, 1, maxVal);
			break;
		case DONATED_BLOOD:
			adjustKarmaMax(newKarma, Virtue.SACRIFICE, 5, maxVal);
			break;
		case DIDNT_DONATE_BLOOD:
			adjustKarmaMin(newKarma, Virtue.SACRIFICE, -5, 1);
			break;
		case CHEAT_REAGENTS:
			adjustKarmaMin(newKarma, Virtue.HONESTY, -10, 1);
			adjustKarmaMin(newKarma, Virtue.JUSTICE, -10, 1);
			adjustKarmaMin(newKarma, Virtue.HONOR, -10, 1);
			break;
		case DIDNT_CHEAT_REAGENTS:
			timeLimited = 1;
			adjustKarmaMax(newKarma, Virtue.HONESTY, 2, maxVal);
			adjustKarmaMax(newKarma, Virtue.JUSTICE, 2, maxVal);
			adjustKarmaMax(newKarma, Virtue.HONOR, 2, maxVal);
			break;
		case USED_SKULL:
			for (Virtue virt : Virtue.values()) {
				adjustKarmaMin(newKarma, virt, -5, 1);
			}
			break;
		case DESTROYED_SKULL:
			for (Virtue virt : Virtue.values()) {
				adjustKarmaMax(newKarma, virt, 10, maxVal);
			}
			break;
		}

		/*
		 * check if enough time has passed since last virtue award if action is
		 * time limited -- if not, throw away new values
		 */
		if (timeLimited > 0) {
			if (((saveGame.moves / 16) >= 0x10000) || (((saveGame.moves / 16) & 0xFFFF) != saveGame.lastvirtue)) {
				saveGame.lastvirtue = (saveGame.moves / 16) & 0xFFFF;
			} else {
				return;
			}
		}

		/*
		 * return to u4dos compatibility and handle losing of eighths
		 */
		for (int v = 0; v < 8; v++) {
			if (maxVal[v] == 100) { // already an avatar
				if (newKarma[v] < 100) { // but lost it
					saveGame.karma[v] = newKarma[v];
				} else { // return to u4dos compatibility
					saveGame.karma[v] = 0;
				}
			} else {
				saveGame.karma[v] = newKarma[v];
			}
		}

	}

	private void adjustKarmaMax(int[] karma, Virtue v, int value, int[] max) {
		int n = Utils.adjustValueMax(karma[v.ordinal()], value, max[v.ordinal()]);
		karma[v.ordinal()] = n;
	}

	private void adjustKarmaMin(int[] karma, Virtue v, int value, int min) {
		int n = Utils.adjustValueMax(karma[v.ordinal()], value, min);
		karma[v.ordinal()] = n;
	}
	
	public void endTurn(MapType mapType) {

		saveGame.moves++;

		for (int i = 0; i < members.size(); i++) {

			PartyMember member = members.get(i);

			if (mapType != MapType.combat) {

				if (member.player.status != StatusType.DEAD)
					adjustFood(-1);

				switch (member.player.status) {
				case SLEEPING:
					if (rand.nextInt(5) == 0)
						member.wakeUp();
					break;

				case POISONED:
					// soundPlay(SOUND_POISON_DAMAGE, false);
					member.applyDamage(2, false);
					break;

				default:
					break;
				}
			}

			/* regenerate magic points */
			if (!member.isDisabled() && member.player.mp < member.player.getMaxMp()) {
				member.player.mp++;
			}
		}

		// /* The party is starving! */
		// if ((saveGame.food == 0) && ((c->location->context & CTX_NON_COMBAT)
		// == c->location->context)) {
		// setChanged();
		// PartyEvent event(PartyEvent::STARVING, 0);
		// notifyObservers(event);
		// }
		//
		// /* heal ship (25% chance it is healed each turn) */
		// if ((c->location->context == CTX_WORLDMAP) && (saveGame->shiphull <
		// 50) && xu4_random(4) == 0) {
		// healShip(1);
		// }
	}

}
