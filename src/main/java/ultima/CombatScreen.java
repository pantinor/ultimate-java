package ultima;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.forever;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.removeActor;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import objects.BaseMap;
import objects.Creature;
import objects.CreatureSet;
import objects.Party;
import objects.Party.PartyMember;
import objects.Tile;
import util.Utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class CombatScreen extends BaseScreen {
	
	private Ultima4 mainGame;
	
	public static int AREA_CREATURES = 16;
	public static int AREA_PLAYERS  =  8;
	
	private CreatureType[] crSlots = new CreatureType[AREA_CREATURES];
	
	private CursorActor cursor;

	private Maps contextMap;
	private BaseMap combatMap;
	private CreatureType crType;
	private CreatureSet creatureSet;
	
	public Context context;
	public Party party;
	private Stage stage;
	
	private OrthogonalTiledMapRenderer renderer;
	private SpriteBatch batch;
	private SecondaryInputProcessor sip;
	
	private Random rand = new Random();
	
	public CombatScreen(Ultima4 mainGame, BaseScreen returnScreen, Context context, Maps contextMap, BaseMap combatMap, TiledMap tmap, CreatureType cr, CreatureSet cs, TextureAtlas a1, TextureAtlas a2) {
		
		scType = ScreenType.COMBAT;

		this.mainGame = mainGame;
		this.returnScreen = returnScreen;
		this.contextMap = contextMap;
		this.combatMap = combatMap;
		
		this.crType = cr;
		
		this.context = context;
		this.party = context.getParty();
		this.creatureSet = cs;
		
		renderer = new OrthogonalTiledMapRenderer(tmap, 2f);
		
		MapProperties prop = tmap.getProperties();
		mapPixelHeight = prop.get("height", Integer.class) * tilePixelWidth;
		
		mapCamera = new OrthographicCamera();
		mapCamera.setToOrtho(false);
		stage = new Stage();
		stage.setViewport(new ScreenViewport(mapCamera));
		skin = new Skin(Gdx.files.internal("assets/skin/uiskin.json"));

		cursor = new CursorActor();
		stage.addActor(cursor);
		cursor.addAction(forever(sequence(fadeOut(1), fadeIn(1))));

		batch = new SpriteBatch();
		
		font = new BitmapFont();
		font.setColor(Color.WHITE);		

		sip = new SecondaryInputProcessor(this, stage);

		
	    fillCreatureTable(crType);
	    
		MapLayer mLayer = tmap.getLayers().get("Monster Positions");
		Iterator<MapObject> iter = mLayer.getObjects().iterator();
		while(iter.hasNext()) {
			MapObject obj = iter.next();
			int index = (Integer)obj.getProperties().get("index");
			int startX = (Integer)obj.getProperties().get("startX");
			int startY = (Integer)obj.getProperties().get("startY");
			
			if (crSlots[index] == null) continue;
			
			Creature c = creatureSet.getInstance(crSlots[index], a1, a2);
			
			c.currentX = startX;
			c.currentY = startY;
			c.currentPos = getMapPixelCoords(startX, startY);

			combatMap.addCreature(c);
		}
		
		MapLayer pLayer = tmap.getLayers().get("Player Positions");
		iter = pLayer.getObjects().iterator();
		while(iter.hasNext()) {
			MapObject obj = iter.next();
			int index = (Integer)obj.getProperties().get("index");
			int startX = (Integer)obj.getProperties().get("startX");
			int startY = (Integer)obj.getProperties().get("startY");
			
			if (index + 1 > party.getSaveGame().members) continue;
			
			Creature c = creatureSet.getInstance(CreatureType.get(party.getMember(index).getPlayer().klass.toString().toLowerCase()), a1, a2);
			c.currentX = startX;
			c.currentY = startY;
			c.currentPos = getMapPixelCoords(startX, startY);

			party.getMember(index).combatCr = c;
			
			if (index == 0) cursor.setPos(c.currentPos);
		}
		
		combatMap.setCombatPlayers(party.getMembers());
		
		newMapPixelCoords = getMapPixelCoords(5, 5);
		changeMapPosition = true;
	}
	
	
	@Override
	public void show() {
		Gdx.input.setInputProcessor(this);
	}
	
	private void fillCreatureTable(CreatureType ct) {
		   
		if (ct == null) return;
		
		int numCreatures = getNumberOfCreatures(ct);

		CreatureType baseType = ct;
		if (baseType == CreatureType.pirate_ship) {
			baseType = CreatureType.rogue;
		}

		for (int i = 0; i < numCreatures; i++) {
			CreatureType current = baseType;

			/* find a free spot in the creature table */
			int j = 0;
			do {
				j = rand.nextInt(AREA_CREATURES);
			} while (crSlots[j] != null);

			/* see if creature is a leader or leader's leader */
			if (CreatureType.get(baseType.getCreature().getLeader()) != baseType.getCreature().getTile() && i != (numCreatures - 1)) { 
				if (rand.nextInt(32) == 0) { // leader's leader
					CreatureType t1 = CreatureType.get(baseType.getCreature().getLeader());
					CreatureType t2 = CreatureType.get(t1.getCreature().getLeader());
					current = t2;
				}
				else if (rand.nextInt(8) == 0) { // leader
					current = CreatureType.get(baseType.getCreature().getLeader());
				}
			}

			/* place this creature in the creature table */
			crSlots[j] = current;
		}
		
	}
	
	private int getNumberOfCreatures(CreatureType ct) {
		int ncreatures = 0;

		if (contextMap == Maps.WORLD || contextMap.getMap().getType() == MapType.dungeon) {

			ncreatures = rand.nextInt(8) + 1;

			if (ncreatures == 1) {
				if (ct != null && ct.getCreature().getEncounterSize() > 0) {
					ncreatures = rand.nextInt(ct.getCreature().getEncounterSize()) + ct.getCreature().getEncounterSize() + 1;
				} else {
					ncreatures = 8;
				}
			}

			while (ncreatures > 2 * party.getSaveGame().members) {
				ncreatures = rand.nextInt(16) + 1;
			}

		} else {
			if (ct != null && ct.getCreature().getTile() == CreatureType.guard) {
				ncreatures = party.getSaveGame().members * 2;
			} else {
				ncreatures = 1;
			}
		}
    
		return ncreatures;
	}
	

	
	@Override
	public void render(float delta) {
		
		time += delta;

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		if (changeMapPosition) {
			mapCamera.position.set(newMapPixelCoords);
			changeMapPosition = false;
		}

		mapCamera.update();
		renderer.setView(mapCamera);
		renderer.render();
				
		renderer.getBatch().begin();
		for (Creature cr : combatMap.getCreatures()) {
			if (cr.currentPos == null  ) {
				continue;
			}
			renderer.getBatch().draw(cr.getAnim().getKeyFrame(time, true), cr.currentPos.x, cr.currentPos.y, tilePixelWidth, tilePixelHeight);
		}
		
		for(PartyMember p : party.getMembers()) {
			if (p.combatCr == null || p.combatCr.currentPos == null || p.fled) {
				continue;
			}
			renderer.getBatch().draw(p.combatCr.getAnim().getKeyFrame(time, true), p.combatCr.currentPos.x, p.combatCr.currentPos.y, tilePixelWidth, tilePixelHeight);
		}
		
		renderer.getBatch().end();


		batch.begin();

		int y = 5;
		for (int i = party.getMembers().size() - 1; i >= 0; i--) {
			PartyMember pm = party.getMember(i);
			String s = (i + 1) + " - " + pm.getPlayer().name + "   " + pm.getPlayer().hp + "" + pm.getPlayer().status.getValue();
			y = y + 18;
			font.draw(batch, s, Ultima4.SCREEN_WIDTH - 125, y);
		}

		logs.render(batch);

		if (showZstats > 0) {
			party.getSaveGame().renderZstats(showZstats, font, batch, Ultima4.SCREEN_HEIGHT);
		}
		batch.end();
		
		stage.act();
		stage.draw();

		
	}
	

	@Override
	public boolean keyUp (int keycode) {
				
		Creature active = party.getActivePartyMember().combatCr;
		
		if (keycode == Keys.UP) {
			if (!preMove(active, Direction.NORTH)) return false;
			active.currentY--;
			active.currentPos = getMapPixelCoords(active.currentX,active.currentY);
			
		} else if (keycode == Keys.DOWN) {
			if (!preMove(active, Direction.SOUTH)) return false;
			active.currentY++;
			active.currentPos = getMapPixelCoords(active.currentX,active.currentY);
			
		} else if (keycode == Keys.RIGHT) {
			if (!preMove(active, Direction.EAST)) return false;
			active.currentX++;
			active.currentPos = getMapPixelCoords(active.currentX,active.currentY);
			
		} else if (keycode == Keys.LEFT) {
			if (!preMove(active, Direction.WEST)) return false;
			active.currentX--;
			active.currentPos = getMapPixelCoords(active.currentX,active.currentY);
			
		} else if (keycode == Keys.A) {
			log("Attack: ");
			Gdx.input.setInputProcessor(sip);
			sip.setinitialKeyCode(keycode, combatMap, active.currentX, active.currentY);
			return false;				
		} else if (keycode == Keys.Z) {
			showZstats = showZstats + 1;
			if (showZstats >= STATS_PLAYER1 && showZstats <= STATS_PLAYER8) {
				if (showZstats > party.getMembers().size()) showZstats = STATS_WEAPONS;
			}
			if (showZstats > STATS_SPELLS) showZstats = STATS_NONE;
		}
		
		finishPlayerTurn();

		return false;

	}
	
	private boolean preMove(Creature active, Direction dir) {
		
		int x = active.currentX;
		int y = active.currentY;
		
		Vector next = null;
		if (dir == Direction.NORTH) next = new Vector(x,y-1);
		if (dir == Direction.SOUTH) next = new Vector(x,y+1);
		if (dir == Direction.EAST) next = new Vector(x+1,y);
		if (dir == Direction.WEST) next = new Vector(x-1,y);
				
		if (next.x > combatMap.getWidth()-1 || next.x < 0 || next.y > combatMap.getHeight()-1 || next.y < 0) {
		    
			/* active player left/fled combat */
			PartyMember ap = party.getActivePartyMember();
			ap.fled = true;
			Sounds.play(Sound.FLEE);
			
			if (party.getAbleCombatPlayers() == 0) {
				end();
				return false;
			} else {
				int ni = party.getNextActiveIndex();
				Creature nextActivePlayer = party.getMember(ni).combatCr; 
				cursor.setPos(nextActivePlayer.currentPos);
			}
			
		} else {
		
			int mask = combatMap.getValidMovesMask(x, y);
			if (!Direction.isDirInMask(dir, mask)) {
				Sounds.play(Sound.BLOCKED);
				return false;
			}
		}
		
		return true;
	}
	
	public void finishPlayerTurn() {
		
		boolean roundIsDone = party.isRoundDone();
		
		PartyMember next = party.getAndSetNextActivePlayer();
		if (next != null) {
			Creature nextActivePlayer = next.combatCr;
			cursor.setPos(nextActivePlayer.currentPos);
		}
		
		if (roundIsDone) {
			finishTurn(0,0);
		}
	}
	
	@Override
	public void finishTurn(int currentX, int currentY) {
		
		party.endTurn(MapType.combat);
		
		//accept no input starting now
		Gdx.input.setInputProcessor(null);
		
		SequenceAction seq = Actions.action(SequenceAction.class);
		for (Creature cr : combatMap.getCreatures()) {
			seq.addAction(new CreatureActionsAction(cr));
			seq.addAction(Actions.delay(.04f));
		}
		seq.addAction(new FinishCreatureAction());
		stage.addAction(seq);
				
	}
	
	class CreatureActionsAction extends Action {
		private Creature cr;
		public CreatureActionsAction(Creature cr) {
			super();
			this.cr = cr;
		}
		public boolean act(float delta) {
			if (!creatureAction(cr)) {
				//remove creature from map
				combatMap.getCreatures().remove(cr);
			}
			return true;
		}
	}
	
	class FinishCreatureAction extends Action {
		public boolean act(float delta) {
			//enable input again
			Gdx.input.setInputProcessor(CombatScreen.this);
			return true;
		}
	}
	
	public void end() {
		
		boolean isWon = combatMap.getCreatures().size() == 0;
		returnScreen.endCombat(isWon);
		
		combatMap.setCombatPlayers(null);
		party.reset();
		
		mainGame.setScreen(returnScreen);
	}
	

	public Vector attack(PartyMember attacker, Direction dir, int x, int y, int range) {
	    
		Sounds.play(Sound.PC_ATTACK);
		
	    WeaponType wt = attacker.getPlayer().weapon;
		boolean weaponCanAttackThroughObjects = wt.getWeapon().getAttackthroughobjects();
	    
	    List<Vector> path = getDirectionalActionPath(Direction.getMask(dir), x, y, 1, range, weaponCanAttackThroughObjects, true);
	    
	    Vector target = null;
	    int distance = 1;
	    for (Vector v : path) {
            target = v;
	        if (attackAt(v, attacker, dir, range, distance)) {
	            break;
	        }
	        distance++;
	    }
    	
    	return target;
	}
	
	
	/**
	 * Gets the path of coordinates for an action.  Each tile in the
	 * direction specified by dirmask, between the minimum and maximum
	 * distances given, is included in the path, until blockedPredicate
	 * fails.  If a tile is blocked, that tile is included in the path
	 * only if includeBlocked is true.
	 */
	private List<Vector> getDirectionalActionPath(int dirmask, int x, int y, int minDistance, int maxDistance, 
			boolean weaponCanAttackThroughObjects, boolean checkForCreatures) {
		
	    List<Vector> path = new ArrayList<Vector>();

	    /*
	     * try every tile in the given direction, up to the given range.
	     * Stop when the the range is exceeded, or the action is blocked.
	     */
	    int nx = x;
	    int ny = y;

		for (int distance = minDistance; distance <= maxDistance; distance++) {
			
			/* make sure our action isn't taking us off the map */
			if (nx > combatMap.getWidth() - 1 || nx < 0 || ny > combatMap.getHeight() - 1 || ny < 0) {
				break;
			}

			boolean blocked = combatMap.isTileBlockedForRangedAttack(nx, ny, checkForCreatures);
			Tile tile = combatMap.getTile(nx, ny);
			boolean canAttackOverSolid = (tile != null && tile.getRule() != null 
					&& tile.getRule() == TileRule.solid_attackover && weaponCanAttackThroughObjects);

			if (!blocked || canAttackOverSolid) {
				path.add(new Vector(nx, ny));
			} else {
				path.add(new Vector(nx, ny));
				break;
			}
			
			if (Direction.isDirInMask(Direction.NORTH, dirmask)) ny--;
			if (Direction.isDirInMask(Direction.SOUTH, dirmask)) ny++;
			if (Direction.isDirInMask(Direction.EAST, dirmask)) nx++;
			if (Direction.isDirInMask(Direction.WEST, dirmask)) nx--;
			

		}

	    return path;
	}
	
	private boolean attackAt(Vector target, PartyMember attacker, Direction dir, int range, int distance) {

	    Creature creature = null;
	    for (Creature c : combatMap.getCreatures()) {
	    	if (c.currentX == target.x && c.currentY == target.y) {
	    		creature = c;
	    		break;
	    	}
	    }
	    
	    WeaponType wt = attacker.getPlayer().weapon;
	    boolean wrongRange = (wt.getWeapon().getAbsolute_range() > 0 && (distance != range));

	    if (creature == null || wrongRange) {
	        if (!wt.getWeapon().getDontshowtravel()) {
	        }
	        return false;
	    }
	    
	    if ((combatMap.getId() == Maps.ABYSS.getId() && !wt.getWeapon().getMagic()) || !attackHit(attacker, creature)) {
	        log("Missed!\n");
	    } else {
	        Sounds.play(Sound.NPC_STRUCK);
	        dealDamage(attacker, creature);
	    }

	    return true;
	}
	
	private boolean attackAt(Vector target, Creature attacker) {

	    PartyMember defender = null;
	    for (PartyMember p : party.getMembers()) {
	    	if (p.combatCr.currentX == target.x && p.combatCr.currentY == target.y) {
	    		defender = p;
	    		break;
	    	}
	    }
	    
	    if (defender == null) return false;
	    	    
	    if (attackHit(attacker, defender)) {
	    	
			ProjectileActor p = new ProjectileActor(Color.YELLOW, attacker.currentX, attacker.currentY);
			Vector3 v = getMapPixelCoords(defender.combatCr.currentX, defender.combatCr.currentY);
			p.addAction(sequence(moveTo(v.x, v.y, .3f),fadeOut(.2f), new Action() {
				public boolean act(float delta) {
			        Sounds.play(Sound.PC_STRUCK);
					return true;
				}
			}, removeActor(p)));
			
	    	stage.addActor(p);
	    	
	        dealDamage(attacker, defender);
		    return true;

	    }

	    return false;
	}
	
	private boolean attackHit(Creature attacker, PartyMember defender) {
	    int attackValue = rand.nextInt(0x100) + attacker.getAttackBonus();
	    int defenseValue = defender.getDefense();
	    return attackValue > defenseValue;
	}
	
	private boolean attackHit(PartyMember attacker, Creature defender) {
	    int attackValue = rand.nextInt(0x100) + attacker.getAttackBonus();
	    int defenseValue = defender.getDefense();
	    return attackValue > defenseValue;
	}
	
	private boolean dealDamage(PartyMember attacker, Creature defender) {
	    int xp = defender.getExp();
	    if (!damageCreature(defender, attacker.getDamage(), true)) {
	    	attacker.awardXP(xp);
	        return false;
	    }
	    return true;
	}
	
	private boolean dealDamage(Creature attacker, PartyMember defender) {
		int damage = attacker.getDamage();
	    return defender.applyDamage(damage, true);
	}
	
	private boolean damageCreature(Creature cr, int damage, boolean byplayer) {
	    
		if (cr.getTile() != CreatureType.lord_british) {
	        cr.setHP(Utils.adjustValueMin(cr.getHP(), -damage, 0));
	    }

	    switch (cr.getDamageStatus()) {

	    case DEAD:        
			if (byplayer) {
				log(String.format("%s Killed! Exp. %d", cr.getName(), cr.getExp()));
			} else {
				log(String.format("%s Killed!", cr.getName()));
			}

	        combatMap.removeCreature(cr);
	        return false;        
	    case FLEEING:
			log(String.format("%s Fleeing!", cr.getName()));
	        break;

	    case CRITICAL:
			log(String.format("%s Critical!", cr.getName()));
	        break;

	    case HEAVILYWOUNDED:
			log(String.format("%s Heavily Wounded!", cr.getName()));
	        break;

	    case LIGHTLYWOUNDED:
			log(String.format("%s Lightly Wounded!", cr.getName()));
	        break;

	    case BARELYWOUNDED:
			log(String.format("%s Barely Wounded!", cr.getName()));
	        break;
		case FINE:
			break;
		default:
			break;
	    }

	    return true;
	}
	
	/**
	 * Return false if to remove from map.
	 */
	private boolean creatureAction(Creature creature) {

	    if (creature.getStatus() == StatusType.SLEEPING && rand.nextInt(8) == 0) {
	        creature.setStatus(StatusType.GOOD); 
	    }

	    if (creature.getStatus() == StatusType.SLEEPING) {
	        return true;
	    }

	    if (creature.negates()) {
	        context.setAura(AuraType.NEGATE, 2);
	    }
	    
	    CombatAction action = null;

	    if (creature.getTeleports() && rand.nextInt(8) == 0) {
	        action = CombatAction.TELEPORT;
	    } else if (creature.getRanged() && rand.nextInt(4) == 0 && (!creature.rangedAttackIs("magic_flash") || context.getAura().getType() != AuraType.NEGATE)) {
	        action = CombatAction.RANGED;
	    } else if (creature.castsSleep() && context.getAura().getType() != AuraType.NEGATE && rand.nextInt(4) == 0) {
	        action = CombatAction.CAST_SLEEP;
	    } else if (creature.getDamageStatus() == CreatureStatus.FLEEING) {
	        action = CombatAction.FLEE;
	    } else {
	        action = CombatAction.ATTACK; 
	    }
	            
	    /* 
	     * now find out who to do it to
	     */
		DistanceWrapper dist = new DistanceWrapper(0);
	    PartyMember target = nearestPartyMember(creature.currentX, creature.currentY, dist, action == CombatAction.RANGED);    
	    if (target == null) {
	        return true;
	    }

	    if (action == CombatAction.ATTACK && dist.getVal() > 1) {
	        action = CombatAction.ADVANCE;
	    }

	    /* let's see if the creature blends into the background, or if he appears... */
	    if (creature.getCamouflage() && !hideOrShow(creature)) {
	        return true;
	    }
	    
	    switch(action) {
	    case ATTACK: {
	        Sounds.play(Sound.NPC_ATTACK);

	        if (attackHit(creature, target)) {
	            Sounds.play(Sound.PC_STRUCK);
	            //GameController::flashTile(target->getCoords(), "hit_flash", 4);

	            if (!dealDamage(creature, target)) {
	                target = null;
	            }

	            if (target != null) {
	                if (creature.stealsFood() && rand.nextInt(4) == 0) {
	                	Sounds.play(Sound.EVADE);
	                    party.adjustGold(-(rand.nextInt(0x3f)));
	                }
	            
	                if (creature.stealsGold()) {
	                	Sounds.play(Sound.EVADE);
	                    party.adjustFood(-2500);
	                }
	            }
	        } else {
	        	//GameController::flashTile(target->getCoords(), "miss_flash", 1);
	        }
	        break;
	    }
	    case CAST_SLEEP: {            
	        log("Sleep!");
            Sounds.play(Sound.SLEEP);
            for (PartyMember p : party.getMembers()) {
                if (rand.nextInt(2) == 0 && !p.isDisabled()) {
                    p.putToSleep();
                }
            }
	        break;
	    }

	    case TELEPORT: {
//	        Coords new_c;
//	        bool valid = false;
//	        bool firstTry = true;                    
//	        
//	        while (!valid) {
//	            Map *map = getMap();
//	            new_c = Coords(rand.nextInt(map->width), rand.nextInt(map->height), c->location->coords.z);
//	                
//	            const Tile *tile = map->tileTypeAt(new_c, WITH_OBJECTS);
//	            
//	            if (tile->isCreatureWalkable()) {
//	                /* If the tile would slow me down, try again! */
//	                if (firstTry && tile->getSpeed() != FAST)
//	                    firstTry = false;
//	                /* OK, good enough! */
//	                else
//	                    valid = true;
//	            }
//	        }
//	        
//	        /* Teleport! */
//	        setCoords(new_c);
//	        break;
	    }

	    case RANGED: {

	        // figure out which direction to fire the weapon
		    int dirmask = combatMap.getRelativeDirection(target.combatCr.currentX, target.combatCr.currentY, creature.currentX, creature.currentY);

	        Sounds.play(Sound.NPC_ATTACK);
	        
		    List<Vector> path = getDirectionalActionPath(dirmask, creature.currentX, creature.currentY, 1, 11, false, false);
		    for (Vector v : path) {
		        if (attackAt(v, creature)) {
		            break;
		        }
		    }

	        break;
	    }

	    case FLEE:
	    case ADVANCE: {
	    	moveCreature(action, creature, target.combatCr.currentX, target.combatCr.currentY);
	    	
    		//is map OOB
    		if (creature.currentX > combatMap.getWidth()-1 || creature.currentX < 0 || 
    				creature.currentY > combatMap.getHeight()-1 || creature.currentY < 0) {
    			log(String.format("%s Flees!", creature.getName()));
    			Sounds.play(Sound.EVADE);
    			if (creature.getGood()) {
    				party.adjustKarma(KarmaAction.SPARED_GOOD);
    			}
    			return false;
    		}   		
	    	
	        break;
	    }
	    }
	    
	    return true;
	}
	
	public PartyMember nearestPartyMember(int fromX, int fromY, DistanceWrapper dist, boolean ranged) {
		PartyMember opponent = null;
		int d = 0;
		int leastDist = 0xFFFF;
		boolean jinx = (context.getAura().getType() == AuraType.JINX);

		for (int i = 0; i < party.getMembers().size(); i++) {
			
			PartyMember pm = party.getMember(i);
			if (pm.fled) continue;
			
			if (!jinx) {
				if (ranged) {
					d = combatMap.distance(fromX, fromY, pm.combatCr.currentX, pm.combatCr.currentY);
				} else {
					d = combatMap.movementDistance(fromX, fromY, pm.combatCr.currentX, pm.combatCr.currentY);
				}

				/* skip target 50% of time if same distance */
				if (d < leastDist || (d == leastDist && rand.nextInt(2) == 0)) {
					opponent = pm;
					leastDist = d;
				}
			}
		}

		if (opponent != null) {
			dist.setVal(leastDist);
		}

		return opponent;
	}
	
	class DistanceWrapper {
		private int val;
		public DistanceWrapper(int val) {
			this.val = val;
		}

		public int getVal() {
			return val;
		}
		public void setVal(int val) {
			this.val = val;
		}
	}

	/**
	 * Hides or shows a camouflaged creature, depending on its distance from
	 * the nearest opponent
	 */
	public boolean hideOrShow(Creature cr) {
	    /* find the nearest opponent */
	    DistanceWrapper dist = new DistanceWrapper(0);
	    
	    /* ok, now we've got the nearest party member.  Now, see if they're close enough */
		if (nearestPartyMember(cr.currentX, cr.currentY, dist, false) != null) {
			if ((dist.getVal() < 5) && !cr.getVisible())
				cr.setVisible(true); /* show yourself */
			else if (dist.getVal() >= 5)
				cr.setVisible(false); /* hide and take no action! */
		}

	    return cr.getVisible();
	}
	
	
	/**
	 * Moves an object in combat according to its chosen combat action
	 */
	public boolean moveCreature(CombatAction action, Creature cr, int targetX, int targetY) {
		
		int nx = cr.currentX;
		int ny = cr.currentY;
		
        int mask = combatMap.getValidMovesMask(nx, ny, cr, -1, -1);
        Direction dir;

	    if (action == CombatAction.FLEE) {
	        dir = combatMap.getPath(targetX, targetY, mask, false, nx, ny);
	        if (dir == null && (nx == 0 || ny == 0)) {
	        	//force a map exit
	        	cr.currentX = -1;
	        	cr.currentY = -1;
	        	return true;
	        }
	    } else {
	        dir = combatMap.getPath(targetX, targetY, mask, true, nx, ny);
	    }
	    
		Vector3 pixelPos = null;
	    
		if (dir == null) {
			return false;
		} else {
			if (dir == Direction.NORTH) ny--;
			if (dir == Direction.SOUTH) ny++;
			if (dir == Direction.EAST) nx++;
			if (dir == Direction.WEST) nx--;
		}
				
	    boolean slowed = false;
		SlowedType slowedType = SlowedType.SLOWED_BY_TILE;
	    if (cr.getSwims() || cr.getSails()) {
	        slowedType = SlowedType.SLOWED_BY_WIND;
	    } else if (cr.getFlies()) {
	        slowedType = SlowedType.SLOWED_BY_NOTHING;
	    }

	    switch(slowedType) {
	    case SLOWED_BY_TILE:
	    	Tile t = combatMap.getTile(nx, ny);
	        if (t != null) slowed = context.slowedByTile(t);
	        break;
	    case SLOWED_BY_WIND:
	        slowed = context.slowedByWind(dir);
	        break;
	    case SLOWED_BY_NOTHING:
	    default:
	        break;
	    }

	    if (!slowed) {        
			pixelPos = getMapPixelCoords(nx, ny);
			cr.currentPos = pixelPos;
			cr.currentX = nx;
			cr.currentY = ny;
	        return true;
	    }

	    return false;
	}
	
	
	private Texture getCursorTexture() {
		Pixmap pixmap = new Pixmap(tilePixelHeight,tilePixelHeight, Format.RGBA8888);
		pixmap.setColor(Color.YELLOW);
		int w = 4;
		pixmap.fillRectangle(0, 0, w, tilePixelHeight);
		pixmap.fillRectangle(tilePixelHeight - w, 0, w, tilePixelHeight);
		pixmap.fillRectangle(w, 0, tilePixelHeight-2*w, w);
		pixmap.fillRectangle(w, tilePixelHeight - w, tilePixelHeight-2*w, w);
		return new Texture(pixmap);
	}
	
	class CursorActor extends Actor {
		Texture texture;
		Sprite sprite;
		CursorActor() {
			texture = getCursorTexture();
			sprite = new Sprite(texture);
		}
		void setPos(Vector3 v) {
			setX(v.x);
			setY(v.y);
		}
		
		@Override
		public void draw(Batch batch, float parentAlpha) {
			
			Color color = getColor();
			batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
			
			batch.draw(sprite, getX(), getY());
		}
		
	}
	
	class ProjectileActor extends Actor {
		Texture texture;
		Sprite sprite;
		ProjectileActor(Color color, int x, int y) {
			Pixmap pixmap = new Pixmap(tilePixelHeight,tilePixelHeight, Format.RGBA8888);
			pixmap.setColor(color);
			pixmap.fillCircle(16,16,3);
			texture = new Texture(pixmap);
			sprite = new Sprite(texture);
			
			Vector3 v = getMapPixelCoords(x, y);
			this.setX(v.x);
			this.setY(v.y);
		}
		
		@Override
		public void draw(Batch batch, float parentAlpha) {
			
			Color color = getColor();
			batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
			
			batch.draw(sprite, getX(), getY());
		}
		
	}
	

}
