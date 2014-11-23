package objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Random;

import objects.SaveGame.SaveGamePlayerRecord;

import org.apache.commons.lang.StringUtils;

import ultima.Constants;
import util.Utils;


public class Party extends Observable implements Constants {
	
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
		
//		SaveGame.SaveGamePlayerRecord rec = sg.new SaveGamePlayerRecord();
//		rec.name = "avatar";
//		rec.hp=200;
//		members.add(new PartyMember(this, rec));
		
		
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
		private List<StatusType> status = new ArrayList<StatusType>();
		private Party party;
		
		public PartyMember(Party py, SaveGamePlayerRecord p) {
			this.party = py;
			this.player = p;
			addStatus(p.status);
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
			case CLASS_MAGE: // mage: 200% of int
				max_mp = player.intel * 2;
				break;

			case CLASS_DRUID: // druid: 150% of int
				max_mp = player.intel * 3 / 2;
				break;

			case CLASS_BARD: // bard, paladin, ranger: 100% of int
			case CLASS_PALADIN:
			case CLASS_RANGER:
				max_mp = player.intel;
				break;

			case CLASS_TINKER: // tinker: 50% of int
				max_mp = player.intel / 2;
				break;

			case CLASS_FIGHTER: // fighter, shepherd: no mp at all
			case CLASS_SHEPHERD:
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
			notifyChange();
		}
		
		public void advanceLevel() {
			if (getLevel() == getMaxLevel())
				return;
			
			player.status = StatusType.STAT_GOOD;
			this.status.clear();
			addStatus(StatusType.STAT_GOOD);
			
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
	    
			notifyChange();

		}
		
		public boolean heal(HealType type) {
			switch (type) {

			case HT_NONE:
				return true;

			case HT_CURE:
				if (!status.contains(StatusType.STAT_POISONED)) {
					return false;
				}
				removeStatus(StatusType.STAT_POISONED);
				break;

			case HT_FULLHEAL:
				if (status.contains(StatusType.STAT_DEAD) || player.hp == player.hpMax) {
					return false;
				}
				player.hp = player.hpMax;
				break;

			case HT_RESURRECT:
				if (!status.contains(StatusType.STAT_DEAD)) {
					return false;
				}
				removeStatus(StatusType.STAT_DEAD);
				addStatus(StatusType.STAT_GOOD);
				break;

			case HT_HEAL:
				if (status.contains(StatusType.STAT_DEAD) || player.hp == player.hpMax) {
					return false;
				}
				player.hp += 75 + (rand.nextInt(0x100) % 0x19);
				break;

			case HT_CAMPHEAL:
				if (status.contains(StatusType.STAT_DEAD) || player.hp == player.hpMax) {
					return false;
				}
				player.hp += 99 + (rand.nextInt(0x100) & 0x77);
				break;

			case HT_INNHEAL:
				if (status.contains(StatusType.STAT_DEAD) || player.hp == player.hpMax) {
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

			notifyChange();

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

		public void removeStatus(StatusType status) {
			this.status.remove(status);
		}
		public void addStatus(StatusType s) {
			if (!this.status.contains(s)) {
				this.status.add(s);
			}
		}

	}
	
	public boolean isJoinedInParty(String name) {
		for (PartyMember pm : members) {
			if (pm.getPlayer().name.equals(name)) return true;
		}
		return false;
	}
	
	/**
	 * For the UI update on the party text area
	 */
	public void notifyChange() {
		setChanged();
		notifyObservers(this);
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
				
				notifyChange();
				
				return CannotJoinError.JOIN_SUCCEEDED;
			}
		}
    
		return CannotJoinError.JOIN_NOT_EXPERIENCED;
	}

}
