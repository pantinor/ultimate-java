package ultima;

import objects.BaseMap;
import objects.City;
import objects.Creature;
import objects.Party.PartyMember;
import objects.Person;
import objects.Tile;
import ultima.Constants.ArmorType;
import util.Utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class SecondaryInputProcessor extends InputAdapter implements Constants {
	
	private BaseScreen screen;
	private Stage stage;
	private int initialKeyCode;
	private BaseMap bm;
	private int currentX;
	private int currentY;
	//used for flaming oil only
	private Direction rangeInputModeDirection = null;
	//only for dungeon screen
	private DungeonTile dngTile;

	private StringBuilder buffer;

	public SecondaryInputProcessor(BaseScreen screen, Stage stage) {
		this.screen = screen;
		this.stage = stage;
	}
	
	public void setinitialKeyCode(int k, BaseMap bm, int x, int y) {
		this.initialKeyCode = k;
		this.bm = bm;
		this.currentX = x;
		this.currentY = y;
		buffer = new StringBuilder();
		
		switch(k) {
		case Keys.T:
			screen.log("TALK> ");
			break;
		case Keys.O:
			screen.log("OPEN> ");
			break;
		case Keys.J:
			screen.log("JIMMY> ");
			break;
		case Keys.L:
			screen.log("LOOK> ");
			break;
		case Keys.A:
			screen.log("ATTACK> ");
			break;
		case Keys.G:
		case Keys.R:
		case Keys.W:
			screen.log("Which party member?");
			break;

		
		}
	}
	
	public void setinitialKeyCode(int k, DungeonTile dngTile, int x, int y) {
		this.initialKeyCode = k;
		this.dngTile = dngTile;
		this.currentX = x;
		this.currentY = y;
		buffer = new StringBuilder();
	}
	
	@Override
	public boolean keyUp (int keycode) {
		Direction dir = Direction.NORTH;
		
		int x=currentX, y=currentY;

		if (keycode == Keys.UP) {
			dir = Direction.NORTH;
			y = y - 1;
		} else if (keycode == Keys.DOWN) {
			dir = Direction.SOUTH;
			y = y + 1;
		} else if (keycode == Keys.LEFT) {
			dir = Direction.WEST;
			x = x - 1;
		} else if (keycode == Keys.RIGHT) {
			dir = Direction.EAST;
			x = x + 1;
		}
		

		if (screen.scType == ScreenType.MAIN) {

			ConversationDialog dialog = null;
			
			if (initialKeyCode == Keys.T) {
				
				screen.logAppend(dir.toString());
	
				Tile tile = bm.getTile(x, y);
				if (tile.getRule() == TileRule.signs) {
					//talking to vendor so get the vendor on other side of sign
					switch (dir) {
					case NORTH: y = y - 1; break;
					case SOUTH:	y = y + 1; break;
					case EAST: x = x + 1; break;
					case WEST: x = x - 1; break;
					}
				}
				
				City city = bm.getCity();
				if (city != null) {
					Person p = city.getPersonAt(x, y);
					if (p != null && (p.getConversation() != null || p.getRole() != null)) {
						Gdx.input.setInputProcessor(stage);
						dialog = new ConversationDialog(p, screen, stage, BaseScreen.skin).show(stage);
					} else {
						screen.log("Funny, no response! ");
					}
				} else {
					screen.log("Funny, no response! ");
				}
				
				return false;

				
			} else if (initialKeyCode == Keys.O) {
				
				screen.logAppend(dir.toString());
				if (bm.openDoor(x, y)) {
					screen.log("Opened!");
				} else {
					screen.log("Can't!");
				}
				
			} else if (initialKeyCode == Keys.J) {
				
				screen.logAppend(dir.toString());
				if (bm.unlockDoor(x, y)) {
					screen.log("Unlocked!");
				} else {
					screen.log("Can't!");
				}
				
			} else if (initialKeyCode == Keys.R) {
				
				if (keycode >= Keys.NUM_1 && keycode <= Keys.NUM_8) {
					Gdx.input.setInputProcessor(new ReadyWearInputAdapter(GameScreen.context.getParty().getMember(keycode -7 - 1), true));
					return false;
				}
				
			} else if (initialKeyCode == Keys.W) {
				
				if (keycode >= Keys.NUM_1 && keycode <= Keys.NUM_8) {
					Gdx.input.setInputProcessor(new ReadyWearInputAdapter(GameScreen.context.getParty().getMember(keycode -7 - 1), false));
					return false;
				}
				
			} else if (initialKeyCode == Keys.G) {
				
				if (keycode >= Keys.NUM_1 && keycode <= Keys.NUM_8) {
					GameScreen gameScreen = (GameScreen)screen;
					gameScreen.getChest(keycode - 7 - 1, x, y);
				}
				
			} else if (initialKeyCode == Keys.A) {
				
				screen.logAppend(dir.toString());
				
				GameScreen gameScreen = (GameScreen)screen;
				
				for (Creature c : bm.getCreatures()) {
					if (c.currentX == x && c.currentY == y) {
						Maps cm = GameScreen.context.getCombatMap(c, bm, x, y, currentX, currentY);
						gameScreen.attackAt(cm, c);
						return false;
					}
				}


			}
			
			if(dialog == null) {
				Gdx.input.setInputProcessor(new InputMultiplexer(screen, stage));
			}

		} else if (screen.scType == ScreenType.SHRINE) {
			
			
			ShrineScreen shrineScreen = (ShrineScreen)screen;
			if (keycode >= Keys.NUM_0 && keycode <= Keys.NUM_3) {
				shrineScreen.meditate(keycode - 7);
			} else {
				screen.log("For how many Cycles (0-3)?");
				return false;
			}
			Gdx.input.setInputProcessor(new InputMultiplexer(screen, stage));
			return false;
			

		} else if (screen.scType == ScreenType.COMBAT) {
			
			CombatScreen combatScreen = (CombatScreen)screen;
			
			if (initialKeyCode == Keys.A) {
				
				screen.log("Attack > " + dir.toString());

			    PartyMember attacker = combatScreen.party.getActivePartyMember();
			    WeaponType wt = attacker.getPlayer().weapon;

				if (rangeInputModeDirection != null) {
					switch (rangeInputModeDirection) {
					case NORTH: y = y - 1; break;
					case SOUTH:	y = y + 1; break;
					case EAST: x = x + 1; break;
					case WEST: x = x - 1; break;
					}	
					if (keycode >= Keys.NUM_0 && keycode <= Keys.NUM_9) {
						Sounds.play(Sound.PC_ATTACK);
					    Utils.animateAttack(stage, combatScreen, attacker, rangeInputModeDirection, x, y, keycode - 7);

					} else {
						screen.log("Invalid range!");
					}
					rangeInputModeDirection = null;

				} else {

				    if (wt.getWeapon().getChoosedistance()) {
				    	rangeInputModeDirection = dir;
						screen.log("Throw Range:");
				    	return false;
				    }
				    
					Sounds.play(Sound.PC_ATTACK);
				    int range = wt.getWeapon().getRange();
				    Utils.animateAttack(stage, combatScreen, attacker, dir, x, y, range);
				}
				
				Gdx.input.setInputProcessor(new InputMultiplexer(screen, stage));
				return false;

			} else if (initialKeyCode == Keys.U) {
				
				if (keycode == Keys.ENTER) {
					if (buffer.length() < 1) return false;
					String useItem = buffer.toString();
					screen.log(useItem);
					if (useItem.startsWith("stone")) {
						screen.log("There are holes for 4 stones.");
						screen.log("What colors?");
						screen.log("1: ");
						buffer = new StringBuilder();
						StoneColorsInputAdapter scia = new StoneColorsInputAdapter(combatScreen);
						Gdx.input.setInputProcessor(scia);
					} else {
						screen.log("Not a usable item!");
						Gdx.input.setInputProcessor(new InputMultiplexer(screen, stage));
						combatScreen.finishPlayerTurn();
					}
				} else if (keycode == Keys.BACKSPACE) {
					if (buffer.length() > 0) {
						buffer.deleteCharAt(buffer.length() - 1);
						screen.logDeleteLastChar();
					}
				} else if (keycode >= 29 && keycode <= 54) {
					buffer.append(Keys.toString(keycode).toLowerCase());
					screen.logAppend(Keys.toString(keycode).toLowerCase());
				}
				
				return false;
				
			}
			
		} else if (screen.scType == ScreenType.DUNGEON) {
			DungeonScreen dngScreen = (DungeonScreen)screen;
			
			if (initialKeyCode == Keys.S) {
				
				switch(dngTile) {
				case FOUNTAIN_PLAIN:
				case FOUNTAIN_HEAL:
				case FOUNTAIN_ACID:
				case FOUNTAIN_CURE:
				case FOUNTAIN_POISON:
					if (keycode >= Keys.NUM_1 && keycode <= Keys.NUM_8) {
						dngScreen.dungeonDrinkFountain(dngTile, keycode - 7 - 1);
					}
					break;
				case ORB:
					if (keycode >= Keys.NUM_1 && keycode <= Keys.NUM_8) {
						dngScreen.dungeonTouchOrb(keycode - 7 - 1);
					}
					break;
				default:
					break;
				}
				
			} else if (initialKeyCode == Keys.G) {
				
				if (keycode >= Keys.NUM_1 && keycode <= Keys.NUM_8) {
					dngScreen.getChest(keycode - 7 - 1, currentX, currentY);
				}
				
			} else if (initialKeyCode == Keys.R) {
				
				if (keycode >= Keys.NUM_1 && keycode <= Keys.NUM_8) {
					Gdx.input.setInputProcessor(new ReadyWearInputAdapter(GameScreen.context.getParty().getMember(keycode -7 - 1), true));
					return false;
				}
				
			} else if (initialKeyCode == Keys.W) {
				
				if (keycode >= Keys.NUM_1 && keycode <= Keys.NUM_8) {
					Gdx.input.setInputProcessor(new ReadyWearInputAdapter(GameScreen.context.getParty().getMember(keycode -7 - 1), false));
					return false;
				}
			}
			
			Gdx.input.setInputProcessor(new InputMultiplexer(screen, stage));

		}
		
		screen.finishTurn(currentX, currentY);
		return false;
	}
	
	class StoneColorsInputAdapter extends InputAdapter {
		public int pos = 1;
		public Stone st1;
		public Stone st2;
		public Stone st3;
		public Stone st4;
		CombatScreen combatScreen;
		

		public StoneColorsInputAdapter(CombatScreen combatScreen) {
			this.combatScreen = combatScreen;
		}

		@Override
		public boolean keyUp(int keycode) {
			if (keycode == Keys.ENTER) {
				if (buffer.length() < 1) return false;
				String color = buffer.toString().toUpperCase();	
				try {
					Stone stone = Stone.valueOf(Stone.class, color);
					
					if ((GameScreen.context.getParty().getSaveGame().stones & stone.getLoc()) == 0) {
						screen.log("None owned!");
						Gdx.input.setInputProcessor(new InputMultiplexer(screen, stage));
						combatScreen.finishPlayerTurn();
						return false;
					}
				
					switch(pos) {
					case 1:st1 = stone;break;
					case 2:st2 = stone;break;
					case 3:st3 = stone;break;
					case 4:st4 = stone;break;
					}
					
					if (pos == 4) {
						Gdx.input.setInputProcessor(new InputMultiplexer(screen, stage));
						combatScreen.useStones(st1, st2, st3, st4);
					} else {
						buffer = new StringBuilder();
						pos++;
						screen.log(pos + ": ");
					}

				} catch (IllegalArgumentException e) {
					screen.log("None owned!");
					Gdx.input.setInputProcessor(new InputMultiplexer(screen, stage));
					combatScreen.finishPlayerTurn();
				}
				
			} else if (keycode == Keys.BACKSPACE) {
				if (buffer.length() > 0) {
					buffer.deleteCharAt(buffer.length() - 1);
					screen.logDeleteLastChar();
				}
			} else if (keycode >= 29 && keycode <= 54) {
				buffer.append(Keys.toString(keycode).toLowerCase());
				screen.logAppend(Keys.toString(keycode).toLowerCase());
			}
			return false;
		}
	}
	
	
	public class ReadyWearInputAdapter extends InputAdapter {
		boolean ready;
		PartyMember pm;
		public ReadyWearInputAdapter(PartyMember pm, boolean ready) {
			this.ready = ready;
			this.pm = pm;
			
			if (ready) {
				for (WeaponType wt : WeaponType.values()) {
					char ch = (char)('a' + wt.ordinal());
					if (wt == WeaponType.HANDS) {
						screen.log(Character.toUpperCase(ch) + " - " + WeaponType.get(ch - 'a'));
						continue;
					}
					if (pm.getParty().getSaveGame().weapons[wt.ordinal()] > 0 ) {
						screen.log(Character.toUpperCase(ch) + " - " + WeaponType.get(ch - 'a'));
					} else if (pm.getPlayer().weapon == wt) {
						screen.log(Character.toUpperCase(ch) + " - " + WeaponType.get(ch - 'a'));
					}
				}
			} else {
				for (ArmorType at : ArmorType.values()) {
					char ch = (char)('a' + at.ordinal());
					if (at == ArmorType.NONE) {
						screen.log(Character.toUpperCase(ch) + " - " + ArmorType.get(ch - 'a'));
						continue;
					}
					if (pm.getParty().getSaveGame().armor[at.ordinal()] > 0 ) {
						screen.log(Character.toUpperCase(ch) + " - " + ArmorType.get(ch - 'a'));
					} else if (pm.getPlayer().armor == at) {
						screen.log(Character.toUpperCase(ch) + " - " + ArmorType.get(ch - 'a'));
					}
				}
			}
		}

		@Override
		public boolean keyUp(int keycode) {
			if (keycode >= Keys.A && keycode <= Keys.P) {
				boolean ret = false;
				if (ready) {
					ret = pm.readyWeapon(keycode - 29);
				} else {
					ret = pm.wearArmor(keycode - 29);
				}
				if (!ret) {
					screen.log("Failed!");
				} else {
					screen.log("Success!");
				}
			}
			Gdx.input.setInputProcessor(new InputMultiplexer(screen, stage));
			screen.finishTurn(currentX, currentY);
			return false;
		}
	}
	

	
}
