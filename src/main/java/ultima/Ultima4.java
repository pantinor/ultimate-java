package ultima;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import objects.ArmorSet;
import objects.CreatureSet;
import objects.MapSet;
import objects.Moongate;
import objects.TileRules;
import objects.TileSet;
import objects.WeaponSet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
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
	TileSet dungeonTileSet;
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
	int tilePixelWidth;
	int tilePixelHeight;
	int mapPixelWidth;
	int mapPixelHeight;
	boolean changeMapPosition = true;
	
	Vector3 currentMapPixelCoords;
	Vector2 currentMousePos;

	Array<AtlasRegion> moongateTextures = new Array<AtlasRegion>();
	int phase = 0, trammelphase = 0, trammelSubphase = 0, feluccaphase = 0;

	DungeonViewer dungeonViewer;

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
			
			
			baseTileSet = (TileSet) loadXml("tileset-base.xml", TileSet.class);			
			dungeonTileSet = (TileSet) loadXml("tileset-dungeon.xml", TileSet.class);
			tileRules = (TileRules) loadXml("tileRules.xml", TileRules.class);
			maps = (MapSet) loadXml("maps.xml", MapSet.class);
			maps.setMapTable();
			weapons = (WeaponSet) loadXml("weapons.xml", WeaponSet.class);
			armors = (ArmorSet) loadXml("armors.xml", ArmorSet.class);
			creatures = (CreatureSet) loadXml("creatures.xml", CreatureSet.class);
			
			atlas = new TextureAtlas(Gdx.files.classpath("tilemaps/tile-atlas.txt"));
			player = new Animation(0.25f, atlas.findRegions("avatar"));
			//textures for the moongates
			moongateTextures = atlas.findRegions("moongate");
			//textures for the phases of  the moon
			moonAtlas = new TextureAtlas(Gdx.files.classpath("graphics/moon-atlas.txt"));


			map = new TmxMapLoader().load("tilemaps/map_0.tmx");
			renderer = new OrthogonalTiledMapRenderer(map, 2f);
			mapBatch = renderer.getSpriteBatch();
			
			font = new BitmapFont();
			font.setColor(Color.WHITE);
			
			
			batch2 = new SpriteBatch();

			MapProperties prop = map.getProperties();
			tilePixelWidth = prop.get("tilewidth", Integer.class) * 2;
			tilePixelHeight = prop.get("tileheight", Integer.class) * 2;
			mapPixelWidth = prop.get("width", Integer.class) * tilePixelWidth;
			mapPixelHeight = prop.get("height", Integer.class) * tilePixelWidth;
			
			currentMapPixelCoords = getMapPixelCoords(86,108);
			
			context = new Context();
			context.setCurrentMap(maps.getMapById(0));
			context.setCurrentTiledMap(map);
			
			new Thread(new GameTimer()).start();
					
		} catch(Exception e) {
			e.printStackTrace();
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
			font.draw(batch2, "current map coords: " + getCurrentMapCoords(), 10, 40);
			font.draw(batch2, "current mouse pos: " + currentMousePos, 10, 20);
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
		
		if (keycode == Keys.UP) {
			mapCamera.position.y = mapCamera.position.y + tilePixelHeight;
		} else if (keycode == Keys.RIGHT) {
			mapCamera.position.x = mapCamera.position.x + tilePixelWidth;
		} else if (keycode == Keys.LEFT) {
			mapCamera.position.x = mapCamera.position.x - tilePixelWidth;
		} else if (keycode == Keys.DOWN) {
			mapCamera.position.y = mapCamera.position.y - tilePixelHeight;
		}
		mapCamera.update();
		
		currentMapPixelCoords = mapCamera.unproject(new Vector3(SCREEN_WIDTH/2, SCREEN_HEIGHT/2, 0));
		
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
		return false;

	}
	
	@Override
	public void resize(int width, int height) {
		mapCamera.viewportWidth = width;
		mapCamera.viewportHeight = height;
	}
	
	public int yDownPixel(float y) {
		return mapPixelHeight - Math.round(y) - tilePixelHeight;
	}
	
	//translate map tile coords to world pixel coords
	public Vector3 getMapPixelCoords(int x, int y) {
		
		Vector3 v = new Vector3(
				x * tilePixelWidth, 
				yDownPixel((y) * tilePixelHeight), 
				0);
		
		return v;
	}
	
	public Vector3 getCurrentMapCoords() {
		
		Vector3 v = mapCamera.unproject(new Vector3(SCREEN_WIDTH/2, SCREEN_HEIGHT/2, 0));
		
		return new Vector3(
				Math.round((v.x) / tilePixelWidth), 
				Math.round(yDownPixel(v.y) / tilePixelHeight),
				0);
	}
	
	public Object loadXml(String fname, Class<?> clazz) throws Exception {
		InputStream is = Ultima4.class.getResourceAsStream("/xml/"+fname);
		JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		return jaxbUnmarshaller.unmarshal(is);
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
