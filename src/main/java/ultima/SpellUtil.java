package ultima;

import objects.Party;
import objects.Party.PartyMember;
import objects.Tile;
import ultima.DungeonScreen.DungeonTileModelInstance;
import util.Utils;

import com.badlogic.gdx.math.Vector3;

public class SpellUtil implements Constants {

	public static boolean spellCast(BaseScreen screen, Context context, Spell spell, PartyMember caster, PartyMember subject, Direction dir, int phase) {
	    
		if (caster == null || spell == null || screen == null) {
			return false;
		}
		
		switch(spell) {
		case AWAKEN:
		case CURE:
		case HEAL:
		case RESURRECT:
			if (subject == null) {
				Ultima4.hud.add("Thou must indicate a target to cast the spell!");
				return false;
			}
			break;
			
		case DISPEL:
		case ICEBALL:
		case KILL:
		case MAGICMISSILE:
		case FIREBALL:
		case ENERGY:
		case WINDS:
		case BLINK:
			if (dir == null) {
				Ultima4.hud.add("Thou must indicate a direction to cast the spell!");
				return false;
			}
			break;
			
		default:
			break;
			
		}
		
		if (caster.getPlayer().mp < spell.getMask()) {
			Ultima4.hud.add("Thou dost not have enough magic points!");
			return false;
		}
		
		Party party = context.getParty();
		
		if (party.getSaveGame().mixtures[spell.ordinal()] < 1) {
			Ultima4.hud.add("Thou dost not have the mixture prepared!");
			return false;
		}
		
	    // subtract the mixture for even trying to cast the spell
		party.getSaveGame().mixtures[spell.ordinal()]--;

	    if (context.getAura() != null && context.getAura().getType() == AuraType.NEGATE) {
			Ultima4.hud.add("Spell is negated!");
	        return false;
	    }

	    caster.adjustMagic(spell.getMp());

	    Sounds.play(Sound.MAGIC);
	    
		switch(spell) {
		
		case AWAKEN:
			spellAwaken(subject);
			break;
		case CURE:
			spellCure(subject);
			break;
		case HEAL:
			spellHeal(subject);
			break;
		case RESURRECT:
			spellRez(subject);
			break;
			
		case DISPEL:
			spellDispel(screen, context, caster, dir);
			break;
		case ICEBALL:
			spellIceball(screen, caster, dir);
			break;
		case KILL:
			spellKill(screen, caster, dir);
			break;
		case MAGICMISSILE:
			spellMMissle(screen, caster, dir);
			break;
		case FIREBALL:
			spellFireball(screen, caster, dir);
			break;
		case ENERGY:
			spellEnergyField(dir);
			break;
		case WINDS:
			spellWinds(dir);
			break;
		case BLINK:
			spellBlink(dir);
			break;
			
		case GATE:
			spellGate(phase);
			break;
		case JINX:
			spellJinx();
			break;
		case LIGHT:
			spellLight();
			break;
		case NEGATE:
			spellNegate();
			break;
		case OPEN:
			spellOpen();
			break;
		case PROTECTION:
			spellProtect();
			break;
		case QUICKNESS:
			spellQuick();
			break;
		case SLEEP:
			spellSleep();
			break;
		case TREMOR:
			spellTremor();
			break;
		case UNDEAD:
			spellUndead();
			break;
		case VIEW:
			spellView();
			break;
		case XIT:
			spellXit();
			break;
		case YUP:
			spellYup();
			break;
		case ZDOWN:
			spellZdown();
			break;
		default:
			break;
			
		}


	    return true;
	}
	
	private static void spellMagicAttack(CombatScreen screen, PartyMember caster, Spell spell, Direction dir, int minDamage, int maxDamage) {
		
		int x = caster.combatCr.currentX;
		int y = caster.combatCr.currentY;
		if (dir == Direction.NORTH) y--;
		if (dir == Direction.SOUTH) y++;
		if (dir == Direction.EAST) x++;
		if (dir == Direction.WEST) x--;
		
		Utils.animateMagicAttack(screen.getStage(), screen, caster, dir, x, y, spell, minDamage, maxDamage);
		
	}
	
	public static void spellAwaken(PartyMember subject) {
		subject.wakeUp();
	}

	public static void spellBlink(Direction dir) {
	}

	public static void spellCure(PartyMember subject) {
		subject.heal(HealType.CURE);
	}

	public static void spellDispel(BaseScreen screen, Context context, PartyMember caster, Direction dir) {
		
		if (screen.scType == ScreenType.MAIN) {
			
			GameScreen gameScreen = (GameScreen)screen;
			Vector3 v = gameScreen.getCurrentMapCoords();
			int x = (int)v.x;
			int y = (int)v.y;
			if (dir == Direction.NORTH) y--;
			if (dir == Direction.SOUTH) y++;
			if (dir == Direction.EAST) x++;
			if (dir == Direction.WEST) x--;
			Tile dispellable = context.getCurrentMap().getTile(x,y);
			if (dispellable.getRule() == null || !dispellable.getRule().has(TileAttrib.dispelable)) return;
			gameScreen.replaceTile("grass", x, y);
			
		} else if (screen.scType == ScreenType.COMBAT) {
			
			CombatScreen combatScreen = (CombatScreen)screen;
			int x = caster.combatCr.currentX;
			int y = caster.combatCr.currentY;
			if (dir == Direction.NORTH) y--;
			if (dir == Direction.SOUTH) y++;
			if (dir == Direction.EAST) x++;
			if (dir == Direction.WEST) x--;
			Tile dispellable = combatScreen.combatMap.getTile(x,y);
			if (dispellable.getRule() == null || !dispellable.getRule().has(TileAttrib.dispelable)) return;
			combatScreen.replaceTile("brick_floor", x, y);
			
		} else if (screen.scType == ScreenType.DUNGEON) {
			DungeonScreen dngScreen = (DungeonScreen)screen;
			int x = (Math.round(dngScreen.currentPos.x)-1);
			int y = (Math.round(dngScreen.currentPos.z)-1);
		    DungeonTileModelInstance dispellable = null;
		    for (DungeonTileModelInstance dmi : dngScreen.modelInstances) {
		    	if (dmi.getTile().getValue() >= 160 && dmi.getTile().getValue() <= 163) {
		    		if (dmi.x == x && dmi.y == y) {
		    			dispellable = dmi;
			    		break;
		    		}
		    	}
		    }
		    dngScreen.modelInstances.remove(dispellable);
		    dngScreen.dungeonTiles[dngScreen.currentLevel][x][y] = DungeonTile.NOTHING;
		}

	}

	public static void spellEnergyField(Direction dir) {
	}

	public static void spellFireball(BaseScreen screen, PartyMember caster, Direction dir) {
		if (screen.scType == ScreenType.COMBAT) {
			spellMagicAttack((CombatScreen)screen, caster, Spell.FIREBALL, dir, 24, 128);
		}
	}

	public static void spellGate(int phase) {
	}

	public static void spellHeal(PartyMember subject) {
		subject.heal(HealType.HEAL);
	}

	public static void spellIceball(BaseScreen screen, PartyMember caster, Direction dir) {
		if (screen.scType == ScreenType.COMBAT) {
			spellMagicAttack((CombatScreen)screen, caster, Spell.ICEBALL, dir, 32, 224);
		}
	}

	public static void spellJinx() {
	}

	public static void spellKill(BaseScreen screen, PartyMember caster, Direction dir) {
		if (screen.scType == ScreenType.COMBAT) {
			spellMagicAttack((CombatScreen)screen, caster, Spell.KILL, dir, -1, 232);
		}
	}

	public static void spellLight() {
	}

	public static void spellMMissle(BaseScreen screen, PartyMember caster, Direction dir) {
		if (screen.scType == ScreenType.COMBAT) {
			spellMagicAttack((CombatScreen)screen, caster, Spell.MAGICMISSILE, dir, 64, 16);
		}
	}

	public static void spellNegate() {
	}

	public static void spellOpen() {
	}

	public static void spellProtect() {
	}

	public static void spellRez(PartyMember subject) {
		subject.heal(HealType.RESURRECT);
	}

	public static void spellQuick() {
	}

	public static void spellSleep() {
	}

	public static void spellTremor() {
	}

	public static void spellUndead() {
	}

	public static void spellView() {
	}

	public static void spellWinds(Direction fromdir) {
	}

	public static void spellXit() {
	}

	public static void spellYup() {
	}

	public static void spellZdown() {
	}

}
