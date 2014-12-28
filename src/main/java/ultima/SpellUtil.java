package ultima;

import objects.Creature;
import objects.Drawable;
import objects.Party;
import objects.Party.PartyMember;
import objects.Tile;
import ultima.ConversationDialog.LBAction;
import ultima.DungeonScreen.DungeonTileModelInstance;
import util.Utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Music.OnCompletionListener;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;

public class SpellUtil implements Constants {

	public static boolean spellCast(final BaseScreen screen, final Context context, final Spell spell, 
			final PartyMember caster, final PartyMember subject, final Direction dir, final int phase) {
	    
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
		
		if (caster.getPlayer().mp < spell.getMp()) {
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

	    if (context.getAura().getType() == AuraType.NEGATE) {
			Ultima4.hud.add("Spell is negated!");
	        return false;
	    }

	    caster.adjustMagic(spell.getMp());
	    
	    OnCompletionListener ocl = new OnCompletionListener() {
			@Override
			public void onCompletion(Music music) {
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
					spellJinx(context);
					break;
				case LIGHT:
					spellLight();
					break;
				case NEGATE:
					spellNegate(context);
					break;
				case OPEN:
					spellOpen();
					break;
				case PROTECTION:
					spellProtect(context);
					break;
				case QUICKNESS:
					spellQuick(context);
					break;
				case SLEEP:
					spellSleep(screen, caster);
					break;
				case TREMOR:
					spellTremor(screen, caster);
					break;
				case UNDEAD:
					spellUndead(screen, caster);
					break;
				case VIEW:
					spellView(screen, caster);
					break;
				case XIT:
					spellXit(screen, caster);
					break;
				case YUP:
					spellYup(screen, caster);
					break;
				case ZDOWN:
					spellZdown(screen, caster);
					break;
				default:
					break;
					
				}
				
			    music.setOnCompletionListener(null);

			}
	    };
	

	    Sounds.play(Sound.MAGIC, ocl);


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
			combatScreen.replaceTile("dungeon_floor", x, y);
			
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

	public static void spellJinx(Context context) {
		context.getAura().set(AuraType.JINX, 10);
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

	public static void spellNegate(Context context) {
		context.getAura().set(AuraType.NEGATE, 10);
	}

	public static void spellOpen() {
	}

	public static void spellProtect(Context context) {
		context.getAura().set(AuraType.PROTECTION, 10);
	}

	public static void spellRez(PartyMember subject) {
		subject.heal(HealType.RESURRECT);
	}

	public static void spellQuick(Context context) {
		context.getAura().set(AuraType.QUICKNESS, 10);
	}

	public static void spellSleep(BaseScreen screen, PartyMember caster) {
	}

	public static void spellTremor(BaseScreen screen, PartyMember caster) {
		
		if (screen.scType == ScreenType.COMBAT) {
			
			CombatScreen combatScreen = (CombatScreen)screen;
			
			SequenceAction seq = Actions.action(SequenceAction.class);

		    for (Creature cr : combatScreen.combatMap.getCreatures()) {
		    	
		        /* creatures with over 192 hp are unaffected */
				if (cr.getHP() > 192) {
					seq.addAction(Actions.run(new PlaySoundAction(Sound.EVADE)));
					continue;
				} else {
					
					if (Utils.rand.nextInt(2) == 0) {
						/* Deal maximum damage to creature */
						Utils.dealDamage(caster, cr, 0xFF);
						
						Tile tile = GameScreen.baseTileSet.getTileByName("hit_flash");
						Drawable d = new Drawable(cr.currentX, cr.currentY, tile, GameScreen.standardAtlas);
				    	d.setX(cr.currentPos.x);
				    	d.setY(cr.currentPos.y);
				    	d.addAction(Actions.sequence(Actions.delay(.4f), Actions.fadeOut(.2f), Actions.removeActor()));
						
						seq.addAction(Actions.run(new AddActorAction(combatScreen.getStage(),d)));
						seq.addAction(Actions.run(new PlaySoundAction(Sound.NPC_STRUCK)));
						
					} else if (Utils.rand.nextInt(2) == 0) {
						/* Deal enough damage to creature to make it flee */
						if (cr.getHP() > 23) {
							Utils.dealDamage(caster, cr, cr.getHP() - 23);
							
							Tile tile = GameScreen.baseTileSet.getTileByName("hit_flash");
							Drawable d = new Drawable(cr.currentX, cr.currentY, tile, GameScreen.standardAtlas);
					    	d.setX(cr.currentPos.x);
					    	d.setY(cr.currentPos.y);
					    	d.addAction(Actions.sequence(Actions.delay(.4f), Actions.fadeOut(.2f), Actions.removeActor()));
							
							seq.addAction(Actions.run(new AddActorAction(combatScreen.getStage(),d)));
							seq.addAction(Actions.run(new PlaySoundAction(Sound.NPC_STRUCK)));
							
						}
					} else {
						seq.addAction(Actions.run(new PlaySoundAction(Sound.EVADE)));
					}
				}
				
				if (cr.getDamageStatus() == CreatureStatus.DEAD) {
					seq.addAction(Actions.run(combatScreen.new RemoveCreatureAction(cr)));
				}
		    	
		    }
		    
		    combatScreen.getStage().addAction(seq);

		    
		}
		
	}

	public static void spellUndead(BaseScreen screen, PartyMember caster) {
		
		if (screen.scType == ScreenType.COMBAT) {
			
			SequenceAction seq = Actions.action(SequenceAction.class);
			
			CombatScreen combatScreen = (CombatScreen)screen;
		    for (Creature cr : combatScreen.combatMap.getCreatures()) {
		    	if (cr.getUndead() && Utils.rand.nextInt(2) == 0) {
		    		
					Tile tile = GameScreen.baseTileSet.getTileByName("hit_flash");
					Drawable d = new Drawable(cr.currentX, cr.currentY, tile, GameScreen.standardAtlas);
			    	d.setX(cr.currentPos.x);
			    	d.setY(cr.currentPos.y);
			    	
			    	d.addAction(Actions.sequence(Actions.delay(.4f), Actions.fadeOut(.2f), Actions.removeActor()));
			    	
					seq.addAction(Actions.run(new AddActorAction(combatScreen.getStage(),d)));
					seq.addAction(Actions.run(new PlaySoundAction(Sound.NPC_STRUCK)));
		    		
		    		Utils.dealDamage(caster, cr, 23);
		    	}
		    	
				if (cr.getDamageStatus() == CreatureStatus.DEAD) {
					seq.addAction(Actions.run(combatScreen.new RemoveCreatureAction(cr)));
				}
		    }
		    combatScreen.getStage().addAction(seq);

		}
	}

	public static void spellView(BaseScreen screen, PartyMember caster) {
		if (screen.scType == ScreenType.MAIN) {
			GameScreen gameScreen = (GameScreen)screen;
			Gdx.input.setInputProcessor(gameScreen.new PeerGemInputAdapter());
		}
	}

	public static void spellWinds(Direction fromdir) {
		GameScreen.context.setWindDirection(fromdir);
	}

	public static void spellXit(BaseScreen screen, PartyMember caster) {
	}

	public static void spellYup(BaseScreen screen, PartyMember caster) {
	}

	public static void spellZdown(BaseScreen screen, PartyMember caster) {
	}


}
