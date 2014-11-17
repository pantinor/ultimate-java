package ultima;

import objects.ArmorSet;
import objects.BaseMap;
import objects.CreatureSet;
import objects.MapSet;
import objects.Moongate;
import objects.Portal;
import objects.Tile;
import objects.TileRules;
import objects.TileSet;
import objects.WeaponSet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
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
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import dungeon.DungeonViewer;

public class Ultima4 extends SimpleGame implements Constants {
	
	Context context;
	
	TileSet baseTileSet;
	TileRules tileRules;
	MapSet maps;
	WeaponSet weapons;
	ArmorSet armors;
	CreatureSet creatures;
	
	TextureAtlas atlas;
	TextureAtlas moonAtlas;
	Animation player;
	float time = 0;
	TiledMap map;
	OrthogonalTiledMapRenderer renderer;
	Batch mapBatch, batch2;
	
	BitmapFont font;

	public static int SCREEN_WIDTH = 800;
	public static int SCREEN_HEIGHT = 600;
	public static int tilePixelWidth = 32;
	public static int tilePixelHeight = 32;
	int mapPixelHeight;

	boolean changeMapPosition = true;
	
	Vector3 currentMapPixelCoords;
	Vector2 currentMousePos;

	Array<AtlasRegion> moongateTextures = new Array<AtlasRegion>();
	int phase = 0, trammelphase = 0, trammelSubphase = 0, feluccaphase = 0;

	public DungeonViewer dungeonViewer;

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
			maps.setMapTable();
			
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
			
			context = new Context();
			
			mapCamera = new OrthographicCamera();

			loadNextMap(Maps.WORLD.getId(),86,108);
			
			new Thread(new GameTimer()).start();
					
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void loadNextMap(int id, int startx, int starty) {
		
		BaseMap bm = maps.getMapById(id);
		context.setCurrentMap(bm);
		
		if (bm.getType().equals("dungeon")) {
			
			dungeonViewer = new DungeonViewer("/data/"+bm.getFname());
			dungeonViewer.setMainGame(this);
			dungeonViewer.create();
			
		} else if (bm.getType().equals("shrine")) {
			
		} else {
			if (bm.getTiles() == null) {
				try {
					Utils.setMapTiles(context.getCurrentMap(), baseTileSet);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
					
			map = new TmxMapLoader().load("tilemaps/map_"+id+".tmx");
			context.setCurrentTiledMap(map);
	
			if (renderer != null) renderer.dispose();
			renderer = new OrthogonalTiledMapRenderer(map, 2f);
			
			//if (mapBatch != null) mapBatch.dispose();
			mapBatch = renderer.getSpriteBatch();
	
			MapProperties prop = map.getProperties();
			mapPixelHeight = prop.get("height", Integer.class) * tilePixelWidth;
			
			currentMapPixelCoords = getMapPixelCoords(startx, starty);
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
				mapCamera.position.set(currentMapPixelCoords);
				changeMapPosition = false;
			}
			
			mapCamera.update();
			renderer.setView(mapCamera);
			renderer.render();
	
			mapBatch.begin();
			mapBatch.draw(player.getKeyFrame(time, true), mapCamera.position.x, mapCamera.position.y, tilePixelWidth, tilePixelHeight);
			mapBatch.end();
			
			if (context.getCurrentMap().getMoongates() != null) {
				mapBatch.begin();
	
				for (Moongate g : context.getCurrentMap().getMoongates()) {
					TextureRegion t = g.getCurrentTexture();
					if (t != null) {
						Vector3 v = getMapPixelCoords(g.getX(),g.getY());
						mapBatch.draw(t, v.x, v.y, tilePixelWidth, tilePixelHeight);
					}
				}
				mapBatch.end();
			}
	
			batch2.begin();
			Vector3 v = getCurrentMapCoords();
			font.draw(batch2, "map coords: " + v, 10, 40);
			//font.draw(batch2, "current tile: " + context.getCurrentMap().getTile(v), 10, 20);
			batch2.draw(moonAtlas.findRegion("phase_" + trammelphase),375,SCREEN_HEIGHT-25,25,25);
			batch2.draw(moonAtlas.findRegion("phase_" + feluccaphase),400,SCREEN_HEIGHT-25,25,25);
			batch2.end();
		
		}
		

		
	}
	
	@Override
	public boolean mouseMoved (int screenX, int screenY) {
		currentMousePos = new Vector2(screenX, screenY);
		return false;
	}
	
	@Override
	public boolean keyUp (int keycode) {
		
		Vector3 v = getCurrentMapCoords();
		Tile ct = context.getCurrentMap().getTile(v);
		
		if (keycode == Keys.UP) {
			if (!preMove(new Vector3(v.x,v.y-1,0))) return false;
			mapCamera.position.y = mapCamera.position.y + tilePixelHeight;
			postMove();
		} else if (keycode == Keys.RIGHT) {
			if (!preMove(new Vector3(v.x+1,v.y,0))) return false;
			mapCamera.position.x = mapCamera.position.x + tilePixelWidth;
			postMove();
		} else if (keycode == Keys.LEFT) {
			if (!preMove(new Vector3(v.x-1,v.y,0))) return false;
			mapCamera.position.x = mapCamera.position.x - tilePixelWidth;
			postMove();
		} else if (keycode == Keys.DOWN) {
			if (!preMove(new Vector3(v.x,v.y+1,0))) return false;
			mapCamera.position.y = mapCamera.position.y - tilePixelHeight;
			postMove();
		} else if (keycode == Keys.E) {
			if (ct.enterable()) {
				Portal p = context.getCurrentMap().getPortal(v.x, v.y);
				loadNextMap(p.getDestmapid(), p.getStartx(), p.getStarty());
			}
		}

		return false;

	}
	
	private boolean preMove(Vector3 nextTile) {
		
		BaseMap bm = context.getCurrentMap();
		
		if (bm.getBorderbehavior().equals("exit")) {
			if (nextTile.x > bm.getWidth()-1 || nextTile.x < 0 || nextTile.y > bm.getHeight()-1 || nextTile.y < 0) {
				Portal p = maps.getMapById(Maps.WORLD.getId()).getPortal(bm.getId());
				loadNextMap(Maps.WORLD.getId(), p.getX(), p.getY());
				return false;
			}
		}
		
		return true;
	}
	
	private void postMove() {
		
		currentMapPixelCoords = mapCamera.unproject(new Vector3(SCREEN_WIDTH/2, SCREEN_HEIGHT/2, 0));
		
		//check if entering moongate
		if (context.getCurrentMap().getId() == Maps.WORLD.getId()) {
			Vector3 cc = getCurrentMapCoords();
			//check for active moongate portal
			for (Moongate g : context.getCurrentMap().getMoongates()) {
				if (g.getCurrentTexture() != null && cc.x == g.getX() && cc.y == g.getY()) {
					Sounds.play(Sound.MOONGATE);
					Vector3 d = getDestinationForMoongate(g);
					currentMapPixelCoords = getMapPixelCoords((int)d.x,(int)d.y);
					changeMapPosition = true;
				}
			}
		}
	}
	
	public void resurfaceFromDungeon() {
		dungeonViewer = null;
		Gdx.input.setInputProcessor(this);
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
		public void run() {
			while (true) {
				try {
					Thread.sleep(250);
					updateMoons(true);					
				} catch (InterruptedException e) {
				}
			}
		}
		
	}
    


}
