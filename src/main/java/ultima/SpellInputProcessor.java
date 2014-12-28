package ultima;

import objects.Party.PartyMember;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class SpellInputProcessor extends InputAdapter implements Constants {
	
	private BaseScreen screen;
	private Stage stage;
	private int player = -1;
	private Spell spell;
	private int currentX;
	private int currentY;
	private PartyMember caster;
	
	public SpellInputProcessor(BaseScreen screen, Stage stage, int x, int y, PartyMember pm) {
		this.screen = screen;
		this.stage = stage;
		this.currentX = x;
		this.currentY = y;
		this.caster = pm;
	}
	
	@Override
	public boolean keyUp (int keycode) {
		
		if (caster == null && player == -1 && keycode >= Keys.NUM_1 && keycode <= Keys.NUM_8) {
			screen.logAppend("" + (keycode - 7));
			player = keycode - 7;
			screen.log("Spell (A-Z): ");
		} else if (caster != null || player != -1) {
			
			if (caster == null) {
				caster = GameScreen.context.getParty().getMember(player - 1);
			}
			
			if (keycode >= Keys.A && keycode <= Keys.Z) {
			
				spell = Spell.get(keycode - 29);
				screen.log("" + spell.getDesc() + "!");
				
				switch(spell) {
				case AWAKEN:
				case CURE:
				case HEAL:
				case RESURRECT:
					screen.log("on who (1-8): ");
					Gdx.input.setInputProcessor(new SubjectInputAdapter());
					break;
					
				case DISPEL:
				case ICEBALL:
				case KILL:
				case MAGICMISSILE:
				case FIREBALL:
				case ENERGY:
				case WINDS:
				case BLINK:
					screen.log("Direction: ");
					Gdx.input.setInputProcessor(new DirectionInputAdapter());
					break;
					
				case GATE:
					screen.log("Phase (1-8): ");
					Gdx.input.setInputProcessor(new PhaseInputAdapter());
					break;
					
				default:
					SpellUtil.spellCast(screen, GameScreen.context, spell, caster, null, null, 0);
					Gdx.input.setInputProcessor(new InputMultiplexer(screen, stage));
					break;
					
				}
			
			} else {
				screen.log("Not a spell!");
				Gdx.input.setInputProcessor(new InputMultiplexer(screen, stage));
			}
			
		} else {
			screen.log("Not a player!");
			Gdx.input.setInputProcessor(new InputMultiplexer(screen, stage));
		}
		
		return false;
	}
	
	class SubjectInputAdapter extends InputAdapter {

		@Override
		public boolean keyUp(int keycode) {
			if (keycode >= Keys.NUM_1 && keycode <= Keys.NUM_8) {
				screen.logAppend("" + (keycode - 7));
				PartyMember subject = GameScreen.context.getParty().getMember(keycode - 7 - 1);
				SpellUtil.spellCast(screen, GameScreen.context, spell, caster, subject, null, 0);
			} else {
				screen.log("Not a player!");
			}
			Gdx.input.setInputProcessor(new InputMultiplexer(screen, stage));
			return false;
		}
	}
	
	class DirectionInputAdapter extends InputAdapter {

		@Override
		public boolean keyUp(int keycode) {
			Direction dir = Direction.NORTH;
			if (keycode == Keys.UP) {
				dir = Direction.NORTH;
			} else if (keycode == Keys.DOWN) {
				dir = Direction.SOUTH;
			} else if (keycode == Keys.LEFT) {
				dir = Direction.WEST;
			} else if (keycode == Keys.RIGHT) {
				dir = Direction.EAST;
			} else {
				screen.log("what?");
				Gdx.input.setInputProcessor(new InputMultiplexer(screen, stage));
				return false;
			}
			
			screen.logAppend(dir.toString());
			
			SpellUtil.spellCast(screen, GameScreen.context, spell, caster, null, dir, 0);
			Gdx.input.setInputProcessor(new InputMultiplexer(screen, stage));
			return false;
		}
	}
	
	class PhaseInputAdapter extends InputAdapter {

		@Override
		public boolean keyUp(int keycode) {
			if (keycode >= Keys.NUM_1 && keycode <= Keys.NUM_8) {
				SpellUtil.spellCast(screen, GameScreen.context, spell, caster, null, null, keycode - 7);
			} else {
				screen.log("what?");
			}
			
			Gdx.input.setInputProcessor(new InputMultiplexer(screen, stage));
			return false;
		}
	}

}
