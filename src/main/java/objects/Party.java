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

	public void setActivePlayer(int activePlayer) {
		this.activePlayer = activePlayer;
	}

	public void setTransport(Tile transport) {
		this.transport = transport;
	}

	public void setTorchduration(int torchduration) {
		this.torchduration = torchduration;
	}

	public class PartyMember {
		
		private SaveGamePlayerRecord player;
		private Party party;
		
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
		
		public int getMaxMp() {
			int max_mp = -1;

			switch (player.klass) {
			case MAGE: // mage: 200% of int
				max_mp = player.intel * 2;
				break;

			case DRUID: // druid: 150% of int
				max_mp = player.intel * 3 / 2;
				break;

			case BARD: // bard, paladin, ranger: 100% of int
			case PALADIN:
			case RANGER:
				max_mp = player.intel;
				break;

			case TINKER: // tinker: 50% of int
				max_mp = player.intel / 2;
				break;

			case FIGHTER: // fighter, shepherd: no mp at all
			case SHEPHERD:
				max_mp = 0;
				break;

			default:
			}

			/* mp always maxes out at 99 */
			if (max_mp > 99) {
				max_mp = 99;
			}

			return max_mp;
		}
		
		public int getLevel() {
			return player.hpMax / 100;
		}
		
		public int getMaxLevel() {
			int level = 1;
			int next = 100;
	    
			while (player.xp >= next && level < 8) {
				level++;
				next <<= 1;
			}
	    
			return level;
		}
		
		public void adjustMp(int pts) {
			Utils.adjustValueMax(player.mp, pts, getMaxMp());
		}
		
		public boolean advanceLevel() {
			if (getLevel() == getMaxLevel())
				return false;
			
			player.status = StatusType.STAT_GOOD;
			player.hpMax = getMaxLevel() * 100;
			player.hp = player.hpMax;
				    
			/* improve stats by 1-8 each */
			player.str += rand.nextInt(8) + 1;
			player.dex += rand.nextInt(8) + 1;
			player.intel += rand.nextInt(8) + 1;
	    
			if (player.str > 50) {
				player.str = 50;
			}
			if (player.dex > 50) {
				player.dex = 50;
			}
			if (player.intel > 50) {
				player.intel = 50;
			}
			return true;
	    
		}
		
		public boolean heal(HealType type) {
			switch (type) {

			case NONE:
				return true;

			case CURE:
				if (player.status != StatusType.STAT_POISONED) {
					return false;
				}
				player.status = StatusType.STAT_GOOD;
				break;

			case FULLHEAL:
				if (player.status == StatusType.STAT_DEAD || player.hp == player.hpMax) {
					return false;
				}
				player.hp = player.hpMax;
				break;

			case RESURRECT:
				if (player.status != StatusType.STAT_DEAD) {
					return false;
				}
				player.status = StatusType.STAT_GOOD;
				break;

			case HEAL:
				if (player.status == StatusType.STAT_DEAD || player.hp == player.hpMax) {
					return false;
				}
				player.hp += 75 + (rand.nextInt(0x100) % 0x19);
				break;

			case CAMPHEAL:
				if (player.status == StatusType.STAT_DEAD || player.hp == player.hpMax) {
					return false;
				}
				player.hp += 99 + (rand.nextInt(0x100) & 0x77);
				break;

			case INNHEAL:
				if (player.status == StatusType.STAT_DEAD || player.hp == player.hpMax) {
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
		int v = 0;
		int[] newKarma = new int[8];
		int[] maxVal = new int[8];
    
		for (v = 0; v < 8; v++) {
			newKarma[v] = saveGame.karma[v] == 0 ? 100 : saveGame.karma[v];
			maxVal[v] = saveGame.karma[v] == 0 ? 100 : 99;
		}
    
		switch (action) {
		case FOUND_ITEM:
			Utils.adjustValueMax(newKarma[Virtue.HONOR.ordinal()], 5, maxVal[Virtue.HONOR.ordinal()]);
			break;
		case STOLE_CHEST:
			Utils.adjustValueMin(newKarma[Virtue.HONESTY.ordinal()], -1, 1);
			Utils.adjustValueMin(newKarma[Virtue.JUSTICE.ordinal()], -1, 1);
			Utils.adjustValueMin(newKarma[Virtue.HONOR.ordinal()], -1, 1);
			break;
		case GAVE_ALL_TO_BEGGAR:
		case GAVE_TO_BEGGAR:
			timeLimited = 1;
			Utils.adjustValueMax(newKarma[Virtue.COMPASSION.ordinal()], 2, maxVal[Virtue.COMPASSION.ordinal()]);
			break;
		case BRAGGED:
			Utils.adjustValueMin(newKarma[Virtue.HUMILITY.ordinal()], -5, 1);
			break;
		case HUMBLE:
			timeLimited = 1;
			Utils.adjustValueMax(newKarma[Virtue.HUMILITY.ordinal()], 10, maxVal[Virtue.HUMILITY.ordinal()]);
			break;
		case HAWKWIND:
		case MEDITATION:
			timeLimited = 1;
			Utils.adjustValueMax(newKarma[Virtue.SPIRITUALITY.ordinal()], 3, maxVal[Virtue.SPIRITUALITY.ordinal()]);
			break;
		case BAD_MANTRA:
			Utils.adjustValueMin(newKarma[Virtue.SPIRITUALITY.ordinal()], -3, 1);
			break;
		case ATTACKED_GOOD:
			Utils.adjustValueMin(newKarma[Virtue.COMPASSION.ordinal()], -5, 1);
			Utils.adjustValueMin(newKarma[Virtue.JUSTICE.ordinal()], -5, 1);
			Utils.adjustValueMin(newKarma[Virtue.HONOR.ordinal()], -5, 1);
			break;
		case FLED_EVIL:
			Utils.adjustValueMin(newKarma[Virtue.VALOR.ordinal()], -2, 1);
			break;
		case HEALTHY_FLED_EVIL:
			Utils.adjustValueMin(newKarma[Virtue.VALOR.ordinal()], -2, 1);
			Utils.adjustValueMin(newKarma[Virtue.SACRIFICE.ordinal()], -2, 1);
			break;
		case KILLED_EVIL:
			Random rand = new Random();
			// gain one valor half the time, zero the rest
			Utils.adjustValueMax(newKarma[Virtue.VALOR.ordinal()], rand.nextInt(1), maxVal[Virtue.VALOR.ordinal()]); 
			break;
		case FLED_GOOD:
			Utils.adjustValueMax(newKarma[Virtue.COMPASSION.ordinal()], 2, maxVal[Virtue.COMPASSION.ordinal()]);
			Utils.adjustValueMax(newKarma[Virtue.JUSTICE.ordinal()], 2, maxVal[Virtue.JUSTICE.ordinal()]);
			break;
		case SPARED_GOOD:
			Utils.adjustValueMax(newKarma[Virtue.COMPASSION.ordinal()], 1, maxVal[Virtue.COMPASSION.ordinal()]);
			Utils.adjustValueMax(newKarma[Virtue.JUSTICE.ordinal()], 1, maxVal[Virtue.JUSTICE.ordinal()]);
			break;
		case DONATED_BLOOD:
			Utils.adjustValueMax(newKarma[Virtue.SACRIFICE.ordinal()], 5, maxVal[Virtue.SACRIFICE.ordinal()]);
			break;
		case DIDNT_DONATE_BLOOD:
			Utils.adjustValueMin(newKarma[Virtue.SACRIFICE.ordinal()], -5, 1);
			break;
		case CHEAT_REAGENTS:
			Utils.adjustValueMin(newKarma[Virtue.HONESTY.ordinal()], -10, 1);
			Utils.adjustValueMin(newKarma[Virtue.JUSTICE.ordinal()], -10, 1);
			Utils.adjustValueMin(newKarma[Virtue.HONOR.ordinal()], -10, 1);
			break;
		case DIDNT_CHEAT_REAGENTS:
			timeLimited = 1;
			Utils.adjustValueMax(newKarma[Virtue.HONESTY.ordinal()], 2, maxVal[Virtue.HONESTY.ordinal()]);
			Utils.adjustValueMax(newKarma[Virtue.JUSTICE.ordinal()], 2, maxVal[Virtue.JUSTICE.ordinal()]);
			Utils.adjustValueMax(newKarma[Virtue.HONOR.ordinal()], 2, maxVal[Virtue.HONOR.ordinal()]);
			break;
		case USED_SKULL:
			for (v = 0; v < 8; v++) {
				Utils.adjustValueMin(newKarma[v], -5, 1);
			}
			break;
		case DESTROYED_SKULL:
			for (v = 0; v < 8; v++) {
				Utils.adjustValueMax(newKarma[v], 10, maxVal[v]);
			}
			break;
		}
		
		/*
		 * check if enough time has passed since last virtue award if
		 * action is time limited -- if not, throw away new values
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
		for (v = 0; v < 8; v++) {
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


}
