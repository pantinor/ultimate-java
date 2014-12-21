package ultima;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.removeActor;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import objects.BaseMap;
import objects.City;
import objects.Creature;
import objects.Party.PartyMember;
import objects.Person;
import objects.Tile;
import ultima.CombatScreen.ProjectileActor;
import ultima.Constants.Direction;
import ultima.Constants.DungeonTile;
import ultima.Constants.Maps;
import ultima.Constants.ScreenType;
import ultima.Constants.Stone;
import ultima.Constants.TileAttrib;
import ultima.Constants.TileRule;
import ultima.Constants.Vector;
import ultima.Constants.WeaponType;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;

import dungeon.DungeonScreen;

public class SecondaryInputProcessor extends InputAdapter {
	
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
				
				screen.log("Talk > " + dir.toString());
	
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
						dialog = new ConversationDialog(p, this.screen, this.screen.skin).show(stage);
					} else {
						screen.log("Funny, no response! ");
					}
				}
				
			} else if (initialKeyCode == Keys.O) {
				
				screen.log("Open > " + dir.toString());
				if (bm.openDoor(x, y)) {
					screen.log("Opened!");
				} else {
					screen.log("Can't!");
				}
	
			} else if (initialKeyCode == Keys.L) {
				
				screen.log("Look > " + dir.toString());
				
			} else if (initialKeyCode == Keys.G) {
				
				if (keycode >= Keys.NUM_1 && keycode <= Keys.NUM_8) {
					GameScreen gameScreen = (GameScreen)screen;
					gameScreen.getChest(keycode - 7 - 1, x, y);
				}
				
			} else if (initialKeyCode == Keys.A) {
				
				screen.log("Attack > " + dir.toString());
				
				GameScreen gameScreen = (GameScreen)screen;
				
				for (Creature c : bm.getCreatures()) {
					if (c.currentX == x && c.currentY == y) {
						Tile ct = bm.getTile(x, y);
						Maps cm = ct.getCombatMap();
						
						TileRule ptr = bm.getTile(currentX, currentY).getRule();
						if (c.getSwims() && !ptr.has(TileAttrib.unwalkable)) {
							cm = Maps.SHORE_CON;
						} else if (c.getSails() && !ptr.has(TileAttrib.unwalkable)) {
							cm = Maps.SHORSHIP_CON;
						}
						
						gameScreen.attackAt(cm, c);
						return false;
					}
				}


			}
			
			if(dialog == null) {
				Gdx.input.setInputProcessor(new InputMultiplexer(screen, stage));
			}

		
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
					    animateAttack(combatScreen, attacker, dir, x, y, keycode - 7);

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
				    
				    int range = wt.getWeapon().getRange();
				    animateAttack(combatScreen, attacker, dir, x, y, range);
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
					if (keycode >= Keys.NUM_0 && keycode <= Keys.NUM_9) {
						dngScreen.dungeonDrinkFountain(dngTile, keycode - 7);
					}
					break;
				case ORB:
					if (keycode >= Keys.NUM_0 && keycode <= Keys.NUM_9) {
						dngScreen.dungeonTouchOrb(keycode - 7);
					}
					break;
				default:
					break;
				}
				
			}
			
			Gdx.input.setInputProcessor(new InputMultiplexer(screen, stage));

		}
		
		screen.finishTurn(currentX, currentY);
		return false;
	}
	
	public void animateAttack(final CombatScreen scr, PartyMember attacker, Direction dir, int x, int y, int range) {
	    
		final Vector target = scr.attack(attacker, dir, x, y, range);

		final ProjectileActor p = scr.new ProjectileActor(Color.RED, x, y, target.res);
		
		Vector3 v = scr.getMapPixelCoords(target.x, target.y);
		
		p.addAction(sequence(moveTo(v.x, v.y, .3f), new Action() {
			public boolean act(float delta) {
				
				switch(p.res) {
				case HIT:
					p.resultTexture = CombatScreen.hitTile;
					break;
				case MISS:
					p.resultTexture = CombatScreen.missTile;
					break;
				}
				
				scr.finishPlayerTurn();
				return true;
			}
		}, fadeOut(.2f), removeActor(p)));
		
    	stage.addActor(p);
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
	

}
