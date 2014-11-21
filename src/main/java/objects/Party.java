package objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Random;

import objects.SaveGame.SaveGamePlayerRecord;
import ultima.Constants;
import util.Utils;


public class Party extends Observable implements Constants {
	
	private SaveGame saveGame;
	private List<PartyMember> members = new ArrayList<PartyMember>();
	private int activePlayer = 0;
	private Tile transport;
	private int torchduration;
	
	public Party(SaveGame sg) {
		this.saveGame = sg;
		
		for (int i = 0;i<saveGame.members;i++) {
			members.add(new PartyMember(this, saveGame.players[i]));
		}
		
		if (members.size() == 0) {
			SaveGame.SaveGamePlayerRecord rec = sg.new SaveGamePlayerRecord();
			rec.name = "avatar";
			rec.hp=200;
			members.add(new PartyMember(this, rec));
		}
		
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
			player.hpMax = getMaxLevel() * 100;
			player.hp = player.hpMax;
			
			Random rand = new Random();
	    
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

		public Party getParty() {
			return party;
		}

		public SaveGamePlayerRecord getPlayer() {
			return player;
		}


	}
	
	public void notifyChange() {
		setChanged();
		notifyObservers(this);
	}

}
