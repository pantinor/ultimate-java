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
import ultima.Constants.ScreenType;
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

public class SecondaryInputProcessor extends InputAdapter {
	
	private BaseScreen screen;
	private Stage stage;
	private int initialKeyCode;
	private BaseMap bm;
	private int currentX;
	private int currentY;
	//used for flaming oil only
	private Direction rangeInputModeDirection = null;
	
	public SecondaryInputProcessor(BaseScreen screen, Stage stage) {
		this.screen = screen;
		this.stage = stage;
	}
	
	public void setinitialKeyCode(int k, BaseMap bm, int x, int y) {
		this.initialKeyCode = k;
		this.bm = bm;
		this.currentX = x;
		this.currentY = y;
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
				
			} else if (initialKeyCode == Keys.S) {
				
				screen.log("Search > " + dir.toString());
				
			} else if (initialKeyCode == Keys.A) {
				
				screen.log("Attack > " + dir.toString());
				
				GameScreen gameScreen = (GameScreen)screen;
				
				for (Creature c : bm.getCreatures()) {
					if (c.currentX == x && c.currentY == y) {
						Tile t = bm.getTile(x, y);
						gameScreen.attackAt(t.getCombatMap(), c);
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

			}

		}
		
		screen.finishTurn(currentX, currentY);
		return false;
	}
	
	public void animateAttack(final CombatScreen scr, PartyMember attacker, Direction dir, int x, int y, int range) {
	    
		Vector target = scr.attack(attacker, dir, x, y, range);

		ProjectileActor p = scr.new ProjectileActor(Color.RED, x, y);
		
		Vector3 v = scr.getMapPixelCoords(target.x, target.y);
		
		p.addAction(sequence(moveTo(v.x, v.y, .3f),fadeOut(.2f), new Action() {
			public boolean act(float delta) {
				Creature active = scr.party.getMembers().get(scr.party.getActivePlayer()).combatCr;
				scr.finishTurn(active.currentX,active.currentY);
				return true;
			}
		}, removeActor(p)));
		
    	stage.addActor(p);
	}
	

}
