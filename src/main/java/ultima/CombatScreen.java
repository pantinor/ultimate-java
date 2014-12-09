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

import objects.BaseMap;
import objects.Creature;
import objects.CreatureSet;
import objects.Party;
import objects.Party.PartyMember;
import objects.Tile;
import ultima.Constants.WeaponType;
import util.Utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
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
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class CombatScreen extends BaseScreen {
	
	public Ultima4 mainGame;
	
	public static int AREA_CREATURES = 16;
	public static int AREA_PLAYERS  =  8;
	
	private CreatureType[] crSlots = new CreatureType[AREA_CREATURES];
	
	private List<Creature> players = new ArrayList<Creature>();
	private CursorActor cursor;

	private Maps contextMap;
	private BaseMap combatMap;
	private CreatureType crType;
	private CreatureSet creatureSet;
	
	public Party party;
	private Stage stage;
	
	private OrthogonalTiledMapRenderer renderer;
	private SpriteBatch batch;
	private SecondaryInputProcessor sip;

	public CombatScreen(Screen returnScreen, Party party, Maps contextMap, BaseMap combatMap, TiledMap tmap, CreatureType cr, CreatureSet cs, TextureAtlas a1, TextureAtlas a2) {
		
		scType = ScreenType.COMBAT;

		this.returnScreen = returnScreen;
		this.contextMap = contextMap;
		this.combatMap = combatMap;
		this.crType = cr;
		this.party = party;
		this.creatureSet = cs;
		
		renderer = new OrthogonalTiledMapRenderer(tmap, 2f);
		
		MapProperties prop = tmap.getProperties();
		mapPixelHeight = prop.get("height", Integer.class) * tilePixelWidth;
		
		mapCamera = new OrthographicCamera();
		mapCamera.setToOrtho(false);
		stage = new Stage();
		stage.setViewport(new ScreenViewport(mapCamera));
		skin = new Skin(Gdx.files.classpath("skin/uiskin.json"));

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
			int index = Integer.parseInt((String)obj.getProperties().get("index"));
			int startX = Integer.parseInt((String)obj.getProperties().get("startX"));
			int startY = Integer.parseInt((String)obj.getProperties().get("startY"));
			
			if (crSlots[index] == null) continue;
			
			Creature c = creatureSet.getInstance(crSlots[index], a2, a1);
			
			c.currentX = startX;
			c.currentY = startY;
			c.currentPos = getMapPixelCoords(startX, startY);

			combatMap.addCreature(c);
		}
		
		MapLayer pLayer = tmap.getLayers().get("Player Positions");
		iter = pLayer.getObjects().iterator();
		while(iter.hasNext()) {
			MapObject obj = iter.next();
			int index = Integer.parseInt((String)obj.getProperties().get("index"));
			int startX = Integer.parseInt((String)obj.getProperties().get("startX"));
			int startY = Integer.parseInt((String)obj.getProperties().get("startY"));
			
			if (index + 1 > party.getSaveGame().members) continue;
			
			Creature c = creatureSet.getInstance(CreatureType.get(party.getMember(index).getPlayer().klass.toString().toLowerCase()), a2, a1);
			c.currentX = startX;
			c.currentY = startY;
			c.currentPos = getMapPixelCoords(startX, startY);

			players.add(c);
			
			if (index == 0) cursor.setPos(c.currentPos);
		}
		
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
		
		stage.act();
		stage.draw();
		
		renderer.getBatch().begin();
		for (Creature cr : combatMap.getCreatures()) {
			if (cr.currentPos == null  ) {
				continue;
			}
			renderer.getBatch().draw(cr.getAnim().getKeyFrame(time, true), cr.currentPos.x, cr.currentPos.y, tilePixelWidth, tilePixelHeight);
		}
		
		for (Creature cr : players) {
			if (cr.currentPos == null  ) {
				continue;
			}
			renderer.getBatch().draw(cr.getAnim().getKeyFrame(time, true), cr.currentPos.x, cr.currentPos.y, tilePixelWidth, tilePixelHeight);
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

		y = 18 * 5;
		for (String s : logs) {
			font.draw(batch, s, 5, y);
			y = y - 18;
		}

		if (showZstats > 0) {
			party.getSaveGame().renderZstats(showZstats, font, batch, Ultima4.SCREEN_HEIGHT);
		}
		batch.end();

		
	}
	

	@Override
	public boolean keyUp (int keycode) {
		
		Creature active = players.get(party.getActivePlayer());
		
		if (keycode == Keys.UP) {
			if (!preMove(active.currentX,active.currentY, Direction.NORTH)) return false;
			active.currentY--;
			active.currentPos = getMapPixelCoords(active.currentX,active.currentY);
			
		} else if (keycode == Keys.DOWN) {
			if (!preMove(active.currentX,active.currentY, Direction.SOUTH)) return false;
			active.currentY++;
			active.currentPos = getMapPixelCoords(active.currentX,active.currentY);
			
		} else if (keycode == Keys.RIGHT) {
			if (!preMove(active.currentX,active.currentY, Direction.EAST)) return false;
			active.currentX++;
			active.currentPos = getMapPixelCoords(active.currentX,active.currentY);
			
		} else if (keycode == Keys.LEFT) {
			if (!preMove(active.currentX,active.currentY, Direction.WEST)) return false;
			active.currentX--;
			active.currentPos = getMapPixelCoords(active.currentX,active.currentY);
			
		} else if (keycode == Keys.A) {
			log("Attack: ");
			Gdx.input.setInputProcessor(sip);
			sip.setinitialKeyCode(keycode, combatMap, active.currentX, active.currentY);
			return false;				
		}
		
		finishTurn(active.currentX,active.currentY);

		return false;

	}
	
	private boolean preMove(int x, int y, Direction dir) {
				
		Vector3 next = null;
		if (dir == Direction.NORTH) next = new Vector3(x,y-1,0);
		if (dir == Direction.SOUTH) next = new Vector3(x,y+1,0);
		if (dir == Direction.EAST) next = new Vector3(x+1,y,0);
		if (dir == Direction.WEST) next = new Vector3(x-1,y,0);
				
		if (next.x > combatMap.getWidth()-1 || next.x < 0 || next.y > combatMap.getHeight()-1 || next.y < 0) {
			mainGame.setScreen(returnScreen);
			return false;
		}
		
		int mask = combatMap.getValidMovesMask(x, y);
		if (!Direction.isDirInMask(dir, mask)) {
			Sounds.play(Sound.BLOCKED);
			return false;
		}
		
		return true;
	}
	
	public void finishTurn(int currentX, int currentY) {
		
		party.endTurn(MapType.combat);

		combatMap.moveObjects(this, currentX, currentY);
		
		Creature nextActivePlayer = players.get(party.nextActivePlayer()); 
		cursor.setPos(nextActivePlayer.currentPos);
		
	}
	

	public Vector attack(PartyMember attacker, Direction dir, int x, int y, int range) {
	    
		Sounds.play(Sound.PC_ATTACK);
		
	    WeaponType wt = attacker.getPlayer().weapon;
		boolean weaponCanAttackThroughObjects = wt.getWeapon().getAttackthroughobjects();
	    
	    List<Vector> path = getDirectionalActionPath(dir, x, y, 1, range, weaponCanAttackThroughObjects);
	    
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
	private List<Vector> getDirectionalActionPath(Direction dir, int x, int y, int minDistance, int maxDistance, boolean weaponCanAttackThroughObjects) {
		
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

			boolean blocked = combatMap.isTileBlockedForRangedAttack(nx, ny);
			Tile tile = combatMap.getTile(nx, ny);
			boolean canAttackOverSolid = (tile != null && tile.getRule() != null && tile.getRule() == TileRule.solid_attackover && weaponCanAttackThroughObjects);

			if (!blocked || canAttackOverSolid) {
				path.add(new Vector(nx, ny));
			} else {
				path.add(new Vector(nx, ny));
				break;
			}
			
			switch (dir) {
			case NORTH:	ny--;break;
			case SOUTH:	ny++;break;
			case EAST:	nx++;break;
			case WEST:	nx--;break;
			}

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
	    
	    if ((combatMap.getId() == Maps.ABYSS.getId() && !wt.getWeapon().getMagic()) || !attackHit(creature)) {
	        log("Missed!\n");
	    } else {
	        Sounds.play(Sound.NPC_STRUCK);
	        dealDamage(attacker, creature);
	    }

	    return true;
	}
	
	private boolean attackHit(Creature defender) {
	    int attackValue = rand.nextInt(0x100);
	    int defenseValue = 128;
	    return attackValue > defenseValue;
	}
	
	private boolean dealDamage(PartyMember attacker, Creature cr) {
	    int xp = cr.getExp();
	    if (!damageCreature(cr, attacker.getDamage(), true)) {
	    	attacker.awardXP(xp);
	        return false;
	    }
	    return true;
	}
	
	/**
	 * Applies damage to the creature.
	 * Returns true if the creature still exists after the damage has been applied
	 * or false, if the creature was destroyed
	 *
	 * If byplayer is false (when a monster is killed by walking through
	 * fire or poison, or as a result of jinx) we don't report experience
	 * on death
	 */
	private boolean damageCreature(Creature cr, int damage, boolean byplayer) {
	    
		/* deal the damage */
		if (cr.getTile() != CreatureType.lord_british) {
	        cr.setHP(Utils.adjustValueMin(cr.getHP(), -damage, 0));
	    }

	    switch (cr.getStatus()) {

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
	
	
	
	private Texture getCursorTexture() {
		Pixmap pixmap = new Pixmap(tilePixelHeight,tilePixelHeight, Format.RGBA8888);
		pixmap.setColor(0.9f, 0.9f, 0.9f, 0.7f);
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

	public List<Creature> getPlayers() {
		return players;
	}

	

}
