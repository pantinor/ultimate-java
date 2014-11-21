package ultima;

import objects.ArmorSet;
import objects.BaseMap;
import objects.CreatureSet;
import objects.MapSet;
import objects.Moongate;
import objects.Party;
import objects.Portal;
import objects.SaveGame;
import objects.Tile;
import objects.TileRules;
import objects.TileSet;
import objects.WeaponSet;
import util.Utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
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
import com.badlogic.gdx.utils.Array;

import dungeon.DungeonViewer;

public class Ultima4 extends SimpleGame implements Constants {
	
	Context context;
	
	public static TileSet baseTileSet;
	public static TileRules tileRules;
	public static WeaponSet weapons;
	public static ArmorSet armors;
	public static CreatureSet creatures;
	
	MapSet maps;
	TextureAtlas atlas;
	TextureAtlas moonAtlas;
	Animation player;
	float time = 0;
	TiledMap map;
	//OrthogonalTiledMapRenderer renderer;
	UltimaMapRenderer renderer;
	Batch mapBatch, batch2;
	
	BitmapFont font;

	public static int SCREEN_WIDTH = 800;
	public static int SCREEN_HEIGHT = 600;
	public static int tilePixelWidth = 32;
	public static int tilePixelHeight = 32;
	int mapPixelHeight;

	boolean changeMapPosition = true;
	
	Vector3 newMapPixelCoords;
	Vector2 currentMousePos;

	Array<AtlasRegion> moongateTextures = new Array<AtlasRegion>();
	int phase = 0, trammelphase = 0, trammelSubphase = 0, feluccaphase = 0;

	public DungeonViewer dungeonViewer;
	public DialogWindow hud;

	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Ultima4";
		cfg.width = SCREEN_WIDTH;
		cfg.height = SCREEN_HEIGHT;
		new LwjglApplication(new Ultima4(), cfg);

	}

	@Override
	public void init() {
		
		try {
			
			
			baseTileSet = (TileSet) Utils.loadXml("tileset-base.xml", TileSet.class);	
			baseTileSet.setMaps();
			
			tileRules = (TileRules) Utils.loadXml("tileRules.xml", TileRules.class);
			
			maps = (MapSet) Utils.loadXml("maps.xml", MapSet.class);
			maps.init(baseTileSet);
			
			weapons = (WeaponSet) Utils.loadXml("weapons.xml", WeaponSet.class);
			armors = (ArmorSet) Utils.loadXml("armors.xml", ArmorSet.class);
			creatures = (CreatureSet) Utils.loadXml("creatures.xml", CreatureSet.class);
			
			atlas = new TextureAtlas(Gdx.files.classpath("tilemaps/tile-atlas.txt"));
			player = new Animation(0.25f, atlas.findRegions("avatar"));
			
			//textures for the moongates
			moongateTextures = atlas.findRegions("moongate");
			//textures for the phases of  the moon
			moonAtlas = new TextureAtlas(Gdx.files.classpath("graphics/moon-atlas.txt"));

			font = new BitmapFont();
			font.setColor(Color.WHITE);		
			batch2 = new SpriteBatch();
			
			mapCamera = new OrthographicCamera();
			
			initGame();
			
			hud = new DialogWindow(stage, this, skin);
			stage.addActor(hud);
			
			new Thread(new GameTimer(this)).start();
					
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
		
		loadNextMap(Maps.WORLD.getId(), sg.x, sg.y);
		
	}
	
	public void loadNextMap(int id, int startx, int starty) {
		
		BaseMap bm = maps.getMapById(id);
		context.setCurrentMap(bm);
		
		if (bm.getType().equals("dungeon")) {
			
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
			renderer = new UltimaMapRenderer(bm, map, 2f);

			//if (mapBatch != null) mapBatch.dispose();
			mapBatch = renderer.getBatch();
	
			MapProperties prop = map.getProperties();
			mapPixelHeight = prop.get("height", Integer.class) * tilePixelWidth;
			
			bm.setSprites(this, atlas);
			
			newMapPixelCoords = getMapPixelCoords(startx, starty);
			changeMapPosition = true;
		}
						
	}

	@Override
	public void draw(float delta) {
		
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
			Vector3 v = getCurrentMapCoords();
			font.draw(batch2, "map coords: " + v, 10, 40);
			font.draw(batch2, "fps: " + Gdx.graphics.getFramesPerSecond(), 0, 20);
			if (context.getCurrentMap().getId() == Maps.WORLD.getId()) {
				batch2.draw(moonAtlas.findRegion("phase_" + trammelphase),375,SCREEN_HEIGHT-25,25,25);
				batch2.draw(moonAtlas.findRegion("phase_" + feluccaphase),400,SCREEN_HEIGHT-25,25,25);
			}
			batch2.end();
			
			stage.act();
			stage.draw();
		
		}
		

		
	}
	
	@Override
	public boolean mouseMoved (int screenX, int screenY) {
		currentMousePos = new Vector2(screenX, screenY);
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
			postMove((int)v.x,(int)v.y-1);
		} else if (keycode == Keys.RIGHT) {
			if (!preMove(v, Direction.EAST)) return false;
			mapCamera.position.x = mapCamera.position.x + tilePixelWidth;
			postMove((int)v.x+1,(int)v.y);
		} else if (keycode == Keys.LEFT) {
			if (!preMove(v, Direction.WEST)) return false;
			mapCamera.position.x = mapCamera.position.x - tilePixelWidth;
			postMove((int)v.x-1,(int)v.y);
		} else if (keycode == Keys.DOWN) {
			if (!preMove(v, Direction.SOUTH)) return false;
			mapCamera.position.y = mapCamera.position.y - tilePixelHeight;
			postMove((int)v.x,(int)v.y+1);
		} else if (keycode == Keys.E) {
			if (ct.enterable()) {
				Portal p = context.getCurrentMap().getPortal(v.x, v.y);
				loadNextMap(p.getDestmapid(), p.getStartx(), p.getStarty());
			}
		} else if (keycode == Keys.Q) {
			if (context.getCurrentMap().getId() == Maps.WORLD.getId()) {
				context.saveGame(this, v);
			}
		}
		
		finishTurn();

		return false;

	}
	
	private boolean preMove(Vector3 currentTile, Direction dir) {
		
		BaseMap bm = context.getCurrentMap();
		
		Vector3 nextTile = null;
		if (dir == Direction.NORTH) nextTile = new Vector3(currentTile.x,currentTile.y-1,0);
		if (dir == Direction.SOUTH) nextTile = new Vector3(currentTile.x,currentTile.y+1,0);
		if (dir == Direction.WEST) nextTile = new Vector3(currentTile.x-1,currentTile.y,0);
		if (dir == Direction.EAST) nextTile = new Vector3(currentTile.x+1,currentTile.y,0);
				
		if (bm.getBorderbehavior().equals("exit")) {
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
	
	private void postMove(int newx, int newy) {
				
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
	}
	
	public void finishTurn() {
		
		
		if (true) { //TODO is not party flying
			context.getCurrentMap().moveObjects(this);
		}
		
		context.incrementMoves();
	}
	
	public void resurfaceFromDungeon() {
		dungeonViewer = null;
		Gdx.input.setInputProcessor(new InputMultiplexer(this, stage));
		context.setCurrentMap(maps.getMapById(Maps.WORLD.getId()));
	}
	
	
	@Override
	public void resize(int width, int height) {
		mapCamera.viewportWidth = width;
		mapCamera.viewportHeight = height;
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
		
		Vector3 v = mapCamera.unproject(new Vector3(SCREEN_WIDTH/2, SCREEN_HEIGHT/2, 0));
		
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
		private Ultima4 mainGame;
		
		private GameTimer(Ultima4 mainGame) {
			this.mainGame = mainGame;
		}

		public void run() {
			while (true) {
				try {
					Thread.sleep(250);
					updateMoons(true);		
					
					if (System.currentTimeMillis() - mainGame.context.getLastCommandTime() > 20*1000) {
						mainGame.keyUp(Keys.SPACE);
					}
					
				} catch (InterruptedException e) {
				}
			}
		}
		
	}
    


}
