package ultima;

import java.util.List;
import java.util.Random;

import objects.ArmorSet;
import objects.BaseMap;
import objects.Creature;
import objects.CreatureSet;
import objects.MapSet;
import objects.Moongate;
import objects.Party;
import objects.Party.PartyMember;
import objects.Portal;
import objects.SaveGame;
import objects.Tile;
import objects.TileSet;
import objects.WeaponSet;
import util.FixedSizeArrayList;
import util.Utils;
import vendor.VendorClassSet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
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
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import dungeon.DungeonViewer;

public class GameScreen implements Screen, InputProcessor, Constants {
	public Ultima4 mainGame;

	public OrthographicCamera mapCamera;
	public Stage stage;
	public Skin skin;
	
	public static Context context;
	
	public static TileSet baseTileSet;
	public static WeaponSet weapons;
	public static ArmorSet armors;
	public static CreatureSet creatures;
	public static VendorClassSet vendorClassSet;
	
	MapSet maps;
	TextureAtlas atlas;
	TextureAtlas u5atlas;
	TextureAtlas moonAtlas;
	Animation player;
	float time = 0;
	TiledMap map;
	UltimaMapRenderer renderer;
	Batch mapBatch, batch2;
	
	BitmapFont font;

	public static int tilePixelWidth = 32;
	public static int tilePixelHeight = 32;
	int mapPixelHeight;

	boolean changeMapPosition = true;
	
	Vector3 newMapPixelCoords;
	Vector2 currentMousePos;

	Array<AtlasRegion> moongateTextures = new Array<AtlasRegion>();
	int phase = 0, trammelphase = 0, trammelSubphase = 0, feluccaphase = 0;

	public DungeonViewer dungeonViewer;
	public List<String> logs = new FixedSizeArrayList<String>(5);
	public int showZstats = 0;
	
	public SecondaryInputProcessor sip;
	Random rand = new Random();
	
	public GameScreen(Ultima4 mainGame) {
		this.mainGame = mainGame;
		
		stage = new Stage(new ScreenViewport());
		skin = new Skin(Gdx.files.classpath("skin/uiskin.json"));
		
		try {
			atlas = new TextureAtlas(Gdx.files.classpath("tilemaps/tile-atlas.txt"));
			u5atlas = new TextureAtlas(Gdx.files.classpath("tilemaps/ultima5-atlas.txt"));
			
			baseTileSet = (TileSet) Utils.loadXml("tileset-base.xml", TileSet.class);	
			baseTileSet.setMaps();
						
			maps = (MapSet) Utils.loadXml("maps.xml", MapSet.class);
			maps.init(baseTileSet);
			
			vendorClassSet = (VendorClassSet) Utils.loadXml("vendor.xml", VendorClassSet.class);
			vendorClassSet.init();
			
			weapons = (WeaponSet) Utils.loadXml("weapons.xml", WeaponSet.class);
			armors = (ArmorSet) Utils.loadXml("armors.xml", ArmorSet.class);
			creatures = (CreatureSet) Utils.loadXml("creatures.xml", CreatureSet.class);
			creatures.init(this, u5atlas, atlas);

			player = new Animation(0.25f, atlas.findRegions("avatar"));
			
			//textures for the moongates
			moongateTextures = atlas.findRegions("moongate");
			//textures for the phases of  the moon
			moonAtlas = new TextureAtlas(Gdx.files.classpath("graphics/moon-atlas.txt"));

			//font = new BitmapFont(Gdx.files.classpath("fonts/Calisto_18.fnt"));
			font = new BitmapFont();

			font.setColor(Color.WHITE);		
			batch2 = new SpriteBatch();
			batch2.enableBlending();
			
			mapCamera = new OrthographicCamera();
			
			sip = new SecondaryInputProcessor(this, stage);

			initGame();
			
			Gdx.input.setInputProcessor(new InputMultiplexer(this, stage));
			
			new Thread(new GameTimer()).start();
					
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void initGame() {
		
		context = new Context();

		SaveGame sg = new SaveGame();
		try {
			sg.read(PARTY_SAV_BASE_FILENAME);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Party party = new Party(sg);
		context.setParty(party);
		
		phase = sg.trammelphase * 3;
		
		//TODO look for whether SAVE is in dungeon or on surface here
		loadNextMap(Maps.WORLD.getId(), sg.x, sg.y);
		
	}
	
	public void loadNextMap(int id, int startx, int starty) {
		
		BaseMap bm = maps.getMapById(id);
		context.setCurrentMap(bm);
		
		log("Entering " + Maps.convert(id).getLabel() + "!");
		
		if (bm.getType() == MapType.dungeon) {
			
			dungeonViewer = new DungeonViewer(stage, this, "/data/"+bm.getFname());
			dungeonViewer.create();
			
		} else if (bm.getType().equals("shrine")) {
			
		} else {
					
			map = new TmxMapLoader().load("tilemaps/map_"+id+".tmx");
			context.setCurrentTiledMap(map);
			
			//set the other layers on the maps to invisible
			for (MapLayer l : map.getLayers()) l.setVisible(false);
			map.getLayers().get("Map Layer").setVisible(true);

			if (renderer != null) renderer.dispose();
			//renderer = new OrthogonalTiledMapRenderer(map, 2f);
			renderer = new UltimaMapRenderer(this, bm, map, 2f);

			//if (mapBatch != null) mapBatch.dispose();
			mapBatch = renderer.getBatch();
	
			MapProperties prop = map.getProperties();
			mapPixelHeight = prop.get("height", Integer.class) * tilePixelWidth;
			
			bm.initObjects(this, u5atlas, atlas);
			
			newMapPixelCoords = getMapPixelCoords(startx, starty);
			changeMapPosition = true;
		}
						
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(new InputMultiplexer(this, stage));
	}

	@Override
	public void render(float delta) {
		
		time += delta;

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		if (dungeonViewer != null) {
			
			dungeonViewer.render();
			
		} else {
		
			if (changeMapPosition) {
				mapCamera.position.set(newMapPixelCoords);
				changeMapPosition = false;
			}
			
			mapCamera.update();
			renderer.setView(mapCamera);
			renderer.render();
	
			mapBatch.begin();
			
			mapBatch.draw(player.getKeyFrame(time, true), mapCamera.position.x, mapCamera.position.y, tilePixelWidth, tilePixelHeight);
			
			if (context.getCurrentMap().getMoongates() != null) {
				for (Moongate g : context.getCurrentMap().getMoongates()) {
					TextureRegion t = g.getCurrentTexture();
					if (t != null) {
						Vector3 v = getMapPixelCoords(g.getX(),g.getY());
						mapBatch.draw(t, v.x, v.y, tilePixelWidth, tilePixelHeight);
					}
				}
			}
			
			mapBatch.end();

	
			batch2.begin();
			
			//Vector3 v = getCurrentMapCoords();
			//font.draw(batch2, "map coords: " + v, 10, 40);
			//font.draw(batch2, "fps: " + Gdx.graphics.getFramesPerSecond(), 0, 20);
			font.draw(batch2, "Food: " +context.getParty().getSaveGame().food/100 + "    Gold: " +context.getParty().getSaveGame().gold , 5, Ultima4.SCREEN_HEIGHT - 5);

			int y=5;
			for (int i=context.getParty().getMembers().size()-1;i>=0;i--) {
				PartyMember pm = context.getParty().getMember(i);
				String s = (i+1) + " - " +pm.getPlayer().name + "   " + pm.getPlayer().hp + "" + pm.getPlayer().status.getValue();
				y=y+18;
				font.draw(batch2, s , Ultima4.SCREEN_WIDTH-125, y);
			}
			
			y=18*5;
			for (String s : logs) {
				font.draw(batch2, s, 5, y);
				y=y-18;
			}
			
			if (showZstats > 0) {
				context.getParty().getSaveGame().renderZstats(showZstats, font, batch2, Ultima4.SCREEN_HEIGHT);
			}

			if (context.getCurrentMap().getId() == Maps.WORLD.getId()) {
				batch2.draw(moonAtlas.findRegion("phase_" + trammelphase),375, Ultima4.SCREEN_HEIGHT-25,25,25);
				batch2.draw(moonAtlas.findRegion("phase_" + feluccaphase),400, Ultima4.SCREEN_HEIGHT-25,25,25);
			}
			
			batch2.end();
			
			stage.act();
			stage.draw();
		
		}
		
	}

	@Override
	public void resize(int width, int height) {
		mapCamera.viewportWidth = width;
		mapCamera.viewportHeight = height;
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hide() {
		Gdx.input.setInputProcessor(null);
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean keyDown(int keycode) {
		// TODO Auto-generated method stub
		return false;
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
				loadNextMap(p.getDestmapid(), p.getStartx(), p.getStarty());
				log(p.getMessage());
			}
		} else if (keycode == Keys.E) {
			if (ct.enterable()) {
				Portal p = context.getCurrentMap().getPortal(v.x, v.y);
				loadNextMap(p.getDestmapid(), p.getStartx(), p.getStarty());
			}
		} else if (keycode == Keys.Q) {
			if (context.getCurrentMap().getId() == Maps.WORLD.getId()) {
				context.saveGame(this, v);
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
			
		} else if (keycode == Keys.T || keycode == Keys.O || keycode == Keys.L) {
			Gdx.input.setInputProcessor(sip);
			sip.setinitialKeyCode(keycode, context.getCurrentMap(), (int)v.x, (int)v.y);
			return false;
		} else if (keycode == Keys.Z) {
			showZstats = showZstats + 1;
			if (showZstats > 6) showZstats = 0;
		}
		
		finishTurn((int)v.x, (int)v.y);

		return false;

	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved (int screenX, int screenY) {
		currentMousePos = new Vector2(screenX, screenY);
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
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
				Portal p = maps.getMapById(Maps.WORLD.getId()).getPortal(bm.getId());
				loadNextMap(Maps.WORLD.getId(), p.getX(), p.getY());
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
			
			context.getParty().endTurn();
			
			if (checkRandomCreatures()) {
				spawnCreature(null, currentX, currentY);
			}
			
			context.getCurrentMap().moveObjects(this, currentX, currentY);
			
		}
		
	}
	
	public boolean checkRandomCreatures() {
		
	    boolean canSpawnHere = context.getCurrentMap().getId() == Maps.WORLD.getId() || dungeonViewer != null;
	    int spawnDivisor = dungeonViewer != null ? (32 - (dungeonViewer.currentLevel << 2)) : 32;
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
        
	    if (dungeonViewer != null) {

	    } else {
	    	
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
	    } 
	    
	    if (creature != null) {

	    } else if (dungeonViewer != null) {
	        //creature = creatureMgr->randomForDungeon(c->location->coords.z);
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
			randId += rand.nextInt(7);
			Creature cr = creatures.getInstance(CreatureType.get(randId), u5atlas, atlas);
			return cr;
		} else if (tile.getRule().has(TileAttrib.swimmable)) {
			randId = CreatureType.nixie.getValue();
			randId += rand.nextInt(5);
			Creature cr = creatures.getInstance(CreatureType.get(randId), u5atlas, atlas);
			return cr;
		}

		if (context.getParty().getSaveGame().moves > 30000) {
			era = 0x0f;
		} else if (context.getParty().getSaveGame().moves > 20000) {
			era = 0x07;
		} else {
			era = 0x03;
		}

		randId = CreatureType.orc.getValue();
		randId += era & rand.nextInt(0x10) & rand.nextInt(0x10);
		Creature cr = creatures.getInstance(CreatureType.get(randId), u5atlas, atlas);
		
		return cr;
	}
	
	public void resurfaceFromDungeon() {
		dungeonViewer = null;
		Gdx.input.setInputProcessor(new InputMultiplexer(this, stage));
		context.setCurrentMap(maps.getMapById(Maps.WORLD.getId()));
	}
	
	
	public int yDownPixel(float y) {
		return mapPixelHeight - Math.round(y) - tilePixelHeight;
	}
	
	/**
	 * translate map tile coords to world pixel coords
	 */
	public Vector3 getMapPixelCoords(int x, int y) {
		
		Vector3 v = new Vector3(
				x * tilePixelWidth, 
				yDownPixel((y) * tilePixelHeight), 
				0);
		
		return v;
	}
	
	/**
	 * get the map coords at the camera center
	 */
	public Vector3 getCurrentMapCoords() {
		
		Vector3 v = mapCamera.unproject(new Vector3(Ultima4.SCREEN_WIDTH/2, Ultima4.SCREEN_HEIGHT/2, 0));
		
		return new Vector3(
				Math.round((v.x) / tilePixelWidth), 
				Math.round(yDownPixel(v.y) / tilePixelHeight),
				0);
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

		public void run() {
			while (true) {
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
    
	public void log(String s) {
		logs.add(s);
	}

}
