package ultima;

import java.util.Iterator;
import java.util.Random;

import objects.ArmorSet;
import objects.BaseMap;
import objects.Creature;
import objects.CreatureSet;
import objects.Drawable;
import objects.MapSet;
import objects.Moongate;
import objects.Party;
import objects.Party.PartyMember;
import objects.Portal;
import objects.SaveGame;
import objects.Tile;
import objects.TileSet;
import objects.WeaponSet;
import util.LogDisplay;
import util.UltimaMapRenderer;
import util.UltimaTiledMapLoader;
import util.Utils;
import vendor.VendorClassSet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import dungeon.DungeonScreen;

public class GameScreen extends BaseScreen {
	
	public static Context context;
	public static TileSet baseTileSet;
	public static WeaponSet weapons;
	public static ArmorSet armors;
	public static CreatureSet creatures;
	public static VendorClassSet vendorClassSet;
	public static TextureAtlas standardAtlas;
	public static TextureAtlas enhancedAtlas;
	
	MapSet maps;
	TextureAtlas moonAtlas;
	Animation avatar;
	
	TiledMap map;
	UltimaMapRenderer renderer;
	Batch mapBatch, batch;
	
	Array<AtlasRegion> moongateTextures = new Array<AtlasRegion>();
	int phase = 0, trammelphase = 0, trammelSubphase = 0, feluccaphase = 0;

	Stage mapObjectsStage;
	
	public SecondaryInputProcessor sip;
	Random rand = new Random();
	
	GameTimer gameTimer;
	boolean loadDungeonFromSavedGame = false;
	
	public GameScreen(Ultima4 mainGame) {
		
		scType = ScreenType.MAIN;
		
		this.mainGame = mainGame;
			
		skin = new Skin(Gdx.files.internal("assets/skin/uiskin.json"));
		
		try {
			standardAtlas = new TextureAtlas(Gdx.files.internal("assets/tilemaps/tiles-vga-atlas.txt"));
			enhancedAtlas = new TextureAtlas(Gdx.files.internal("assets/tilemaps/monsters-u4.atlas"));
			
			baseTileSet = (TileSet) Utils.loadXml("tileset-base.xml", TileSet.class);	
			baseTileSet.setMaps();
						
			maps = (MapSet) Utils.loadXml("maps.xml", MapSet.class);
			maps.init(baseTileSet);
			
			vendorClassSet = (VendorClassSet) Utils.loadXml("vendor.xml", VendorClassSet.class);
			vendorClassSet.init();
			
			weapons = (WeaponSet) Utils.loadXml("weapons.xml", WeaponSet.class);
			armors = (ArmorSet) Utils.loadXml("armors.xml", ArmorSet.class);
			creatures = (CreatureSet) Utils.loadXml("creatures.xml", CreatureSet.class);
			creatures.init();

			avatar = new Animation(0.25f, enhancedAtlas.findRegions("avatar"));
					
			//textures for the moongates
			moongateTextures = standardAtlas.findRegions("moongate");
			//textures for the phases of  the moon
			moonAtlas = new TextureAtlas(Gdx.files.internal("assets/graphics/moon-atlas.txt"));

			//font = new BitmapFont(Gdx.files.classpath("fonts/Calisto_18.fnt"));
			font = new BitmapFont();

			font.setColor(Color.WHITE);		
			batch = new SpriteBatch();
			batch.enableBlending();
			
			if (logs == null) logs = new LogDisplay(font);
			
			mapCamera = new OrthographicCamera();
			mapCamera.setToOrtho(false);
			
			stage = new Stage(new ScreenViewport());
			mapObjectsStage = new Stage(new ScreenViewport(mapCamera));
				        
			sip = new SecondaryInputProcessor(this, stage);

			
			context = new Context();

			SaveGame sg = new SaveGame();
			sg.read(PARTY_SAV_BASE_FILENAME);
			
			Party party = new Party(sg);
			context.setParty(party);
			
			phase = sg.trammelphase * 3;
		
			//loadNextMap(Maps.WORLD, 233, 234, 0,0,0,false);
			loadNextMap(Maps.WORLD, sg.x, sg.y, 0,0,0,false);
			
			if (Maps.get(sg.location) != Maps.WORLD) {
				loadDungeonFromSavedGame = true;
			}

			gameTimer = new GameTimer();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void show() {
		Gdx.input.setInputProcessor(new InputMultiplexer(this, stage));
		gameTimer.run = true;
		new Thread(gameTimer).start();
		
		if (loadDungeonFromSavedGame) {
			SaveGame sg = context.getParty().getSaveGame();
			loadNextMap(Maps.get(sg.location), sg.x, sg.y, sg.dngx, sg.dngy, sg.dnglevel, true);
			loadDungeonFromSavedGame = false;
		}
	}
	
	@Override
	public void hide() {
		gameTimer.run = false;
	}
		
	public void loadNextMap(Maps m, int x, int y, int dngx, int dngy, int dngLevel, boolean restoreSG) {
		
		log("Entering " + m.getLabel() + "!");
		
		if (m.getMap().getType() == MapType.dungeon) {
			
			DungeonScreen sc = new DungeonScreen(mainGame, stage, this, m);
			
			if (restoreSG) {
				sc.restoreSaveGameLocation(dngx, dngy, dngLevel);
			}
			
			mainGame.setScreen(sc);
			
		} else if (m.getMap().getType().equals("shrine")) {
			
		} else {
			
			BaseMap bm = m.getMap();
			context.setCurrentMap(bm);
					
			map = new UltimaTiledMapLoader(m, standardAtlas, m.getMap().getWidth(), m.getMap().getHeight(), 16, 16).load();
			context.setCurrentTiledMap(map);

			if (renderer != null) renderer.dispose();
			renderer = new UltimaMapRenderer(this, bm, map, 2f);

			mapBatch = renderer.getBatch();
	
			MapProperties prop = map.getProperties();
			mapPixelHeight = prop.get("height", Integer.class) * tilePixelWidth;
			
			bm.initObjects(this, enhancedAtlas, standardAtlas);
			
			newMapPixelCoords = getMapPixelCoords(x, y);
			changeMapPosition = true;
		}
						
	}
	
	public void attackAt(Maps combat, Creature cr) {
		
		Maps contextMap = Maps.get(context.getCurrentMap().getId());
		BaseMap combatMap = combat.getMap();
		
		map = new UltimaTiledMapLoader(combat, standardAtlas, combat.getMap().getWidth(), combat.getMap().getHeight(), 16, 16).load();
		
		context.setCurrentTiledMap(map);
		
		CombatScreen sc = new CombatScreen(mainGame, this, context, contextMap, combatMap, map, cr.getTile(), creatures, enhancedAtlas, standardAtlas);
		mainGame.setScreen(sc);
		
		currentEncounter = cr;

	}
	
	public void endCombat(boolean isWon) {
		
		mainGame.setScreen(this);
		
		if (currentEncounter != null) {
					
			Tile tile = context.getCurrentMap().getTile(currentEncounter.currentX, currentEncounter.currentY);
			
			if (isWon) {
				
				log("Victory!");
				
				if (!currentEncounter.getGood()) {
                    context.getParty().adjustKarma(KarmaAction.KILLED_EVIL);
				}
				
				TileRule r = tile.getRule();
				
			    /* add a chest, if the creature leaves one */
			    if (!currentEncounter.getNochest() && (r == null || !r.has(TileAttrib.unwalkable))) {
			    	Drawable chest = new Drawable(currentEncounter.currentX, currentEncounter.currentY, "chest", standardAtlas);
			    	chest.setX(currentEncounter.currentPos.x);
			    	chest.setY(currentEncounter.currentPos.y);
			    	mapObjectsStage.addActor(chest);
			    }
			    /* add a ship if you just defeated a pirate ship */
			    else if (currentEncounter.getTile() == CreatureType.pirate_ship) {
			    	Drawable ship = new Drawable(currentEncounter.currentX, currentEncounter.currentY, "ship", standardAtlas);
			    	ship.setX(currentEncounter.currentPos.x);
			    	ship.setY(currentEncounter.currentPos.y);
			    	mapObjectsStage.addActor(ship);
			    }
			} else {

				if (context.getParty().didAnyoneFlee()) {
	                log("Battle is lost!");

	                /* minus points for fleeing from evil creatures */
	                if (!currentEncounter.getGood()) {
	                	//lose karma points here
	                    context.getParty().adjustKarma(KarmaAction.FLED_EVIL);
	                } else {
	                	//get extra karma points
	                    context.getParty().adjustKarma(KarmaAction.FLED_GOOD);
	                }
	            } else if (!context.getParty().isAnyoneAlive()) {
	            	//death scene
	            	mainGame.setScreen(new DeathScreen(mainGame, this, context.getParty()));
	            	loadNextMap(Maps.CASTLE_OF_LORD_BRITISH_2, REVIVE_CASTLE_X, REVIVE_CASTLE_Y, 0, 0, 0, false);
	            }
			}
			
			context.getCurrentMap().removeCreature(currentEncounter);
			currentEncounter = null;

		}
	}

	@Override
	public void render(float delta) {
		
		time += delta;

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		if (renderer == null) return;
		
		if (changeMapPosition) {
			mapCamera.position.set(newMapPixelCoords);
			changeMapPosition = false;
		}

		mapCamera.update();
		renderer.setView(mapCamera);
		renderer.render();

		mapBatch.begin();


		if (context.getCurrentMap().getMoongates() != null) {
			for (Moongate g : context.getCurrentMap().getMoongates()) {
				TextureRegion t = g.getCurrentTexture();
				if (t != null) {
					Vector3 v = getMapPixelCoords(g.getX(), g.getY());
					mapBatch.draw(t, v.x, v.y, tilePixelWidth, tilePixelHeight);
				}
			}
		}

		mapBatch.end();

		batch.begin();

		// Vector3 v = getCurrentMapCoords();
		// font.draw(batch2, "map coords: " + v, 10, 40);
		// font.draw(batch2, "fps: " + Gdx.graphics.getFramesPerSecond(), 0, 20);
		//font.draw(batch, "mouse: " + currentMousePos, 10, 70);

		font.draw(batch, "Food: " + context.getParty().getSaveGame().food / 100 + "    Gold: " + context.getParty().getSaveGame().gold, 5, Ultima4.SCREEN_HEIGHT - 5);

		int y = 5;
		for (int i = context.getParty().getMembers().size() - 1; i >= 0; i--) {
			PartyMember pm = context.getParty().getMember(i);
			String s = (i + 1) + " - " + pm.getPlayer().name + "   " + pm.getPlayer().hp + "" + pm.getPlayer().status.getValue();
			y = y + 18;
			
			font.setColor(i == context.getParty().getActivePlayer()? new Color(.35f, .93f, 0.91f, 1) : Color.WHITE);
			if (pm.getPlayer().status == StatusType.POISONED) font.setColor(Color.GREEN);
			if (pm.getPlayer().status == StatusType.SLEEPING) font.setColor(Color.YELLOW);
			if (pm.getPlayer().status == StatusType.DEAD) font.setColor(Color.GRAY);
			
			font.draw(batch, s, Ultima4.SCREEN_WIDTH - 125, y);
		}
		
		font.setColor(Color.WHITE);
		logs.render(batch);

		if (showZstats > 0) {
			context.getParty().getSaveGame().renderZstats(showZstats, font, batch, Ultima4.SCREEN_HEIGHT);
		}

		if (context.getCurrentMap().getId() == Maps.WORLD.getId()) {
			batch.draw(moonAtlas.findRegion("phase_" + trammelphase), 375, Ultima4.SCREEN_HEIGHT - 25, 25, 25);
			batch.draw(moonAtlas.findRegion("phase_" + feluccaphase), 400, Ultima4.SCREEN_HEIGHT - 25, 25, 25);
		}

		batch.end();

		stage.act();
		stage.draw();
		
		mapObjectsStage.act();
		mapObjectsStage.draw();
		
		mapBatch.begin();
		mapBatch.draw(avatar.getKeyFrame(time, true), mapCamera.position.x, mapCamera.position.y, tilePixelWidth, tilePixelHeight);
		mapBatch.end();

		
	}


	@Override
	public boolean keyUp (int keycode) {
		
		context.setLastCommandTime(System.currentTimeMillis());
		
		Vector3 v = getCurrentMapCoords();
		Tile ct = context.getCurrentMap().getTile(v);
		
		if (keycode == Keys.UP) {
			
			if (!preMove(v, Direction.NORTH)) return false;
			mapCamera.position.y = mapCamera.position.y + tilePixelHeight;
			postMove(Direction.NORTH, (int)v.x,(int)v.y-1);
			
		} else if (keycode == Keys.RIGHT) {
			
			if (!preMove(v, Direction.EAST)) return false;
			mapCamera.position.x = mapCamera.position.x + tilePixelWidth;
			postMove(Direction.EAST, (int)v.x+1,(int)v.y);
			
		} else if (keycode == Keys.LEFT) {
			
			if (!preMove(v, Direction.WEST)) return false;
			mapCamera.position.x = mapCamera.position.x - tilePixelWidth;
			postMove(Direction.WEST, (int)v.x-1,(int)v.y);
			
		} else if (keycode == Keys.DOWN) {
			
			if (!preMove(v, Direction.SOUTH)) return false;
			mapCamera.position.y = mapCamera.position.y - tilePixelHeight;
			postMove(Direction.SOUTH, (int)v.x,(int)v.y+1);
			
		} else if (keycode == Keys.K || keycode == Keys.D) {
			if (ct.climbable()) {
				Portal p = context.getCurrentMap().getPortal(v.x, v.y);
				loadNextMap(Maps.get(p.getDestmapid()), p.getStartx(), p.getStarty(), 0, 0, 0, false);
				log(p.getMessage());
			}
		} else if (keycode == Keys.E) {
			//if (ct.enterable()) {
				Portal p = context.getCurrentMap().getPortal(v.x, v.y);
				if (p != null) {
					loadNextMap(Maps.get(p.getDestmapid()), p.getStartx(), p.getStarty(), 0, 0, 0, false);
				}
			//}
		} else if (keycode == Keys.Q) {
			if (context.getCurrentMap().getId() == Maps.WORLD.getId()) {
				context.saveGame(v.x,v.y,0,Maps.WORLD);
				log("Saved Game.");
			} else {
				log("Cannot save inside!");
			}
		} else if (keycode == Keys.S) {

			BaseMap bm = context.getCurrentMap();
			ItemMapLabels l = bm.searchLocation(context.getParty(), (int)v.x, (int)v.y);
			if (l != null) {
				log("You found " + l.getDesc() + ".");
			}
		} else if (keycode == Keys.G) {
			log("Which party member?");
			Gdx.input.setInputProcessor(sip);
			sip.setinitialKeyCode(keycode, context.getCurrentMap(), (int)v.x, (int)v.y);
			return false;
			
		} else if (keycode == Keys.T || keycode == Keys.O || keycode == Keys.L || keycode == Keys.A) {
			Gdx.input.setInputProcessor(sip);
			sip.setinitialKeyCode(keycode, context.getCurrentMap(), (int)v.x, (int)v.y);
			return false;
		} else if (keycode == Keys.Z) {
			showZstats = showZstats + 1;
			if (showZstats >= STATS_PLAYER1 && showZstats <= STATS_PLAYER8) {
				if (showZstats > context.getParty().getMembers().size()) showZstats = STATS_WEAPONS;
			}
			if (showZstats > STATS_SPELLS) showZstats = STATS_NONE;
		} else if (keycode == Keys.SPACE) {
			log("Pass");
		}
		
		finishTurn((int)v.x, (int)v.y);

		return false;

	}
	
	private boolean preMove(Vector3 currentTile, Direction dir) {
		
		BaseMap bm = context.getCurrentMap();
		
		Vector3 nextTile = null;
		if (dir == Direction.NORTH) nextTile = new Vector3(currentTile.x,currentTile.y-1,0);
		if (dir == Direction.SOUTH) nextTile = new Vector3(currentTile.x,currentTile.y+1,0);
		if (dir == Direction.WEST) nextTile = new Vector3(currentTile.x-1,currentTile.y,0);
		if (dir == Direction.EAST) nextTile = new Vector3(currentTile.x+1,currentTile.y,0);
				
		if (bm.getBorderbehavior() == MapBorderBehavior.exit) {
			if (nextTile.x > bm.getWidth()-1 || nextTile.x < 0 || nextTile.y > bm.getHeight()-1 || nextTile.y < 0) {
				Portal p = Maps.WORLD.getMap().getPortal(bm.getId());
				loadNextMap(Maps.WORLD, p.getX(), p.getY(), 0, 0, 0, false);
				return false;
			}
		}
		
		int mask = bm.getValidMovesMask((int)currentTile.x, (int)currentTile.y);
		if (!Direction.isDirInMask(dir, mask)) {
			Sounds.play(Sound.BLOCKED);
			return false;
		}
		
		return true;
	}
	
	private void postMove(Direction dir, int newx, int newy) {
				
		//check if entering moongate
		if (context.getCurrentMap().getId() == Maps.WORLD.getId()) {
			//check for active moongate portal
			for (Moongate g : context.getCurrentMap().getMoongates()) {
				if (g.getCurrentTexture() != null && newx == g.getX() && newy == g.getY()) {
					Sounds.play(Sound.MOONGATE);
					Vector3 d = getDestinationForMoongate(g);
					newMapPixelCoords = getMapPixelCoords((int)d.x,(int)d.y);
					changeMapPosition = true;
				}
			}
		}
		
		log(dir.toString());
	}
	
	public void finishTurn(int currentX, int currentY) {
		
		if (true) {
			
			context.getParty().endTurn(context.getCurrentMap().getType());
			
			creatureCleanup(currentX, currentY);
			
			if (checkRandomCreatures()) {
				spawnCreature(null, currentX, currentY);
			}
			
			context.getCurrentMap().moveObjects(this, currentX, currentY);
			
		}
		
	}
	
	/**
	 * Removes creatures from the current map if they are too far away from the avatar
	 */
	public void creatureCleanup(int currentX, int currentY) {
	    BaseMap bm = context.getCurrentMap();
	    Iterator<Creature> i = bm.getCreatures().iterator();
	    while (i.hasNext()) {
	       Creature cr = i.next();
	        if (Math.abs(currentX - cr.currentX) > MAX_CREATURE_DISTANCE || Math.abs(currentY - cr.currentY) > MAX_CREATURE_DISTANCE) {
	        	i.remove();           
	        }
	    }
	}
	
	public boolean checkRandomCreatures() {
		
	    boolean canSpawnHere = context.getCurrentMap().getId() == Maps.WORLD.getId();
	    //int spawnDivisor = dungeonViewer != null ? (32 - (dungeonViewer.currentLevel << 2)) : 32;
	    int spawnDivisor = 32;
	    int spawnVal = rand.nextInt(spawnDivisor);

	    if (!canSpawnHere || context.getCurrentMap().getCreatures().size() >= MAX_CREATURES_ON_MAP || spawnVal != 0) {
	        return false;
	    }
	    
	    return true;
	}
	
	public boolean spawnCreature(Creature creature, int currentX, int currentY) {

		int dx = 0;
        int dy = 0;
        int tmp = 0;
            	
		boolean ok = false;
		int tries = 0;
		int MAX_TRIES = 10;

		while (!ok && (tries < MAX_TRIES)) {
			dx = 15;
			dy = rand.nextInt(15);

			if (rand.nextInt(2) > 0) {
				dx = -dx;
			}
			if (rand.nextInt(2) > 0) {
				dy = -dy;
			}
			if (rand.nextInt(2) > 0) {
				tmp = dx;
				dx = dy;
				dy = tmp;
			}

			dx = currentX + dx;
			dy = currentY + dy;

			/* make sure we can spawn the creature there */
			if (creature != null) {
				Tile tile = context.getCurrentMap().getTile(dx, dy);
				TileRule rule = tile.getRule();
				if ((creature.getSails() && rule.has(TileAttrib.sailable)) || 
						(creature.getSwims() && rule.has(TileAttrib.swimmable)) || 
						(creature.getFlies() && !rule.has(TileAttrib.unflyable))) {
					ok = true;
				} else {
					tries++;
				}
			} else {
				ok = true;
			}
		}

		if (!ok) {
			return false;
		}
	     
	    
	    if (creature != null) {

	    } else {
	    	Tile tile = context.getCurrentMap().getTile(dx, dy);
	        creature = getRandomCreatureForTile(tile);
	    }
	    
	    if (creature != null) {
	    	creature.currentX = dx;
	    	creature.currentY = dy;
	    	context.getCurrentMap().addCreature(creature);
	    } else {
	    	return false;
	    }
	        
	    return true;
	}
	
	public Creature getRandomCreatureForTile(Tile tile) {

		int era = 0;
		int randId = 0;

		if (tile == null || tile.getRule() == null) {
			System.err.println("randomForTile: Tile or rule is null");
			return null;
		}

		if (tile.getRule().has(TileAttrib.creatureunwalkable)) {
			return null;
		}

		if (tile.getRule().has(TileAttrib.sailable)) {
			randId = CreatureType.pirate_ship.getValue();
			randId += rand.nextInt(6);
			Creature cr = creatures.getInstance(CreatureType.get(randId), enhancedAtlas, standardAtlas);
			return cr;
		} else if (tile.getRule().has(TileAttrib.swimmable)) {
			randId = CreatureType.nixie.getValue();
			randId += rand.nextInt(4);
			Creature cr = creatures.getInstance(CreatureType.get(randId), enhancedAtlas, standardAtlas);
			return cr;
		}

		if (context.getParty().getSaveGame().moves > 30000) {
			era = 15;
		} else if (context.getParty().getSaveGame().moves > 20000) {
			era = 7;
		} else {
			era = 3;
		}

		randId = CreatureType.orc.getValue();
		randId += era & rand.nextInt(16) & rand.nextInt(16);
		Creature cr = creatures.getInstance(CreatureType.get(randId), enhancedAtlas, standardAtlas);
		
		return cr;
	}
		
	
	public void updateMoons(boolean showmoongates) {
		
		// world map only
		if (context.getCurrentMap().getId() == 0) {
			
			context.setMoonPhase(context.getMoonPhase() + 1);
			if (context.getMoonPhase() >= MOON_PHASES * MOON_SECONDS_PER_PHASE * 4) {
				context.setMoonPhase(0);
			}

			phase = (context.getMoonPhase() / (4 * MOON_SECONDS_PER_PHASE));
			feluccaphase = phase % 8;
			trammelphase = phase / 3;
			if (trammelphase > 7) {
				trammelphase = 7;
			}
			trammelSubphase = context.getMoonPhase() % (MOON_SECONDS_PER_PHASE * 4 * 3);

			
			for (Moongate g : context.getCurrentMap().getMoongates()) {
				g.setCurrentTexture(null);
			}

			if (showmoongates) {
				Moongate gate = context.getCurrentMap().getMoongate(trammelphase);
				AtlasRegion texture = null;
				if (trammelSubphase == 0) {
					texture = moongateTextures.get(0);
				} else if (trammelSubphase == 1) {
					texture = moongateTextures.get(1);
				} else if (trammelSubphase == 2) {
					texture = moongateTextures.get(2);
				} else if (trammelSubphase == 3) {
					texture = moongateTextures.get(3);
				} else if ((trammelSubphase > 3) && (trammelSubphase < (MOON_SECONDS_PER_PHASE * 4 * 3) - 3)) {
					texture = moongateTextures.get(3);
				} else if (trammelSubphase == (MOON_SECONDS_PER_PHASE * 4 * 3) - 3) {
					texture = moongateTextures.get(2);
				} else if (trammelSubphase == (MOON_SECONDS_PER_PHASE * 4 * 3) - 2) {
					texture = moongateTextures.get(1);
				} else if (trammelSubphase == (MOON_SECONDS_PER_PHASE * 4 * 3) - 1) {
					texture = moongateTextures.get(0);
				}
				gate.setCurrentTexture(texture);
			}
			
		}
	}
	
	public Vector3 getDestinationForMoongate(Moongate m) {
		Vector3 dest = new Vector3(m.getX(), m.getY(), 0);
		String destGate = null;
		
		if (feluccaphase == m.getDm1()) destGate = m.getD1();
		if (feluccaphase == m.getDm2()) destGate = m.getD2();
		if (feluccaphase == m.getDm3()) destGate = m.getD3();

		for(Moongate dm : context.getCurrentMap().getMoongates()) {
			if (dm.getName().equals(destGate)) {
				dest = new Vector3(dm.getX(), dm.getY(), 0);
			}
		}
		
		return dest;
	}
	
	class GameTimer implements Runnable {
		boolean run = true;
		public void run() {
			while (run) {
				try {
					Thread.sleep(250);
					updateMoons(true);		
					
					if (System.currentTimeMillis() - context.getLastCommandTime() > 20*1000) {
						keyUp(Keys.SPACE);
					}
					
				} catch (InterruptedException e) {
				}
			}
		}
		
	}
	
	
	
	public void getChest(int index, int x, int y) {

		if (context.getParty().isFlying()) {
			log("Not in a ballon!");
			return;
		}

		Drawable chest = null;
		for (Actor a : mapObjectsStage.getActors()) {
			if (a instanceof Drawable) {
				Drawable d = (Drawable)a;
				if (d.getCx() == x && d.getCy() == y) {
					chest = (Drawable)a;
				}
			}
		}

		if (chest != null) {
			PartyMember pm = context.getParty().getMember(index);
			chest.remove();
			getChestTrapHandler(pm);
			log(String.format("The Chest Holds: %d Gold", context.getParty().getChestGold()));
			if (context.getCurrentMap().getType() == MapType.city) {
				context.getParty().adjustKarma(KarmaAction.STOLE_CHEST);
			}
		} else {
			log("Not Here!");
		}
	}

	private boolean getChestTrapHandler(PartyMember pm) {

		TileEffect trapType;
		int randNum = rand.nextInt(4);
		boolean passTest = (rand.nextInt(2) == 0);

		/* Chest is trapped! 50/50 chance */
		if (passTest) {
			/* Figure out which trap the chest has */
			switch (randNum) {
			case 0:
				trapType = TileEffect.FIRE;
				break; /* acid trap (56% chance - 9/16) */
			case 1:
				trapType = TileEffect.SLEEP;
				break; /* sleep trap (19% chance - 3/16) */
			case 2:
				trapType = TileEffect.POISON;
				break; /* poison trap (19% chance - 3/16) */
			case 3:
				trapType = TileEffect.LAVA;
				break; /* bomb trap (6% chance - 1/16) */
			default:
				trapType = TileEffect.FIRE;
				break;
			}

			if (trapType == TileEffect.FIRE) {
				log("Acid Trap!");
				Sounds.play(Sound.PC_STRUCK);
			} else if (trapType == TileEffect.POISON) {
				log("Poison Trap!");
				Sounds.play(Sound.POISON_EFFECT);
			} else if (trapType == TileEffect.SLEEP) {
				log("Sleep Trap!");
				Sounds.play(Sound.PC_STRUCK);
			} else if (trapType == TileEffect.LAVA) {
				log("Bomb Trap!");
				Sounds.play(Sound.PC_STRUCK);
			}
			
			// player is null when using the Open spell (immune to traps)
			// if the chest was opened by a PC, see if the trap was
			// evaded by testing the PC's dex
			if (pm.getPlayer().dex + 25 < rand.nextInt(100)) {
				if (trapType == TileEffect.LAVA) {/* bomb trap */
					context.getParty().applyEffect(trapType);
				} else {
					pm.applyEffect(trapType);
				}
			} else {
				log("Evaded!");
			}

			return true;
		}

		return false;
	}




}
