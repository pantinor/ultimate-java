import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import objects.Tile;
import objects.TileSet;

import org.apache.commons.io.IOUtils;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.assets.AssetManager;
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
import com.badlogic.gdx.maps.Map;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileSets;
import com.badlogic.gdx.maps.tiled.TiledMapTile.BlendMode;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class TestMain extends InputAdapter implements ApplicationListener {
	TextureAtlas atlas;
	Animation player;
	float time = 0;
	private CustomTiledMap map;
	private TiledMap map2;
	private OrthogonalTiledMapRenderer renderer;
	Batch mapBatch, batch2;
	
	static int screenWidth = 1200;
	static int screenHeight = 800;
	int tilePixelWidth;
	int tilePixelHeight;
	int mapPixelWidth;
	int mapPixelHeight;
	int mapViewBoundsWidth = 700;
	int mapViewBoundsHeight = 700;
	boolean initMapPosition = true;
	
	Vector3 currentMapPixelCoords;
	Vector2 currentMousePos;

	BitmapFont font;
	
	OrthographicCamera mapCamera;
	

	public void create () {
		
		atlas = new TextureAtlas(Gdx.files.classpath("tilemaps/tile-atlas.txt"));
		player = new Animation(0.25f, atlas.findRegions("avatar"));
		
		List<CustomTiledMap> maps = new ArrayList<CustomTiledMap>();

		try {
			
			map2 = new TmxMapLoader().load("tilemaps/map_0.tmx");

			
			File file2 = new File("target/classes/xml/tileset-base.xml");
			JAXBContext jaxbContext = JAXBContext.newInstance(TileSet.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			TileSet ts = (TileSet) jaxbUnmarshaller.unmarshal(file2);
			ts.setMaps();
			
			File dir = new File("target/classes/data");
			File[] conFiles = dir.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(".con");
				}
			});
			for (File f : conFiles) {
				InputStream is = TestMain.class.getResourceAsStream("/data/" + f.getName());
				byte[] bytes = IOUtils.toByteArray(is);	
				int pos = 64;
				
				CustomTiledMap combatMap = new CustomTiledMap();
				combatMap.setTilesets(map2.getTileSets());
				
				MapLayers combatLayers = combatMap.getLayers();
				TiledMapTileLayer combatLayer = new TiledMapTileLayer(11, 11, 16, 16);
				for (int y = 0; y < 11; y++) {
					for (int x = 0; x < 11; x++) {
						int index = bytes[pos] & 0xff;pos++;
						Tile tile = ts.getTileByIndex(index);
						AtlasRegion region = atlas.findRegion(tile.getName());
						if (region == null) {
							System.out.println(f.getName() + "Tile index cannot be found: " + tile.getName());
						}
						Cell cell = new Cell();
						cell.setTile(new StaticTiledMapTile(region));
						combatLayer.setCell(x, y, cell);
					}
				}
				combatLayers.add(combatLayer);
				
				maps.add(combatMap);
			}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		map = maps.get(3);
		map2 = new TmxMapLoader().load("tilemaps/map_0.tmx");
		renderer = new OrthogonalTiledMapRenderer(map, 2f);
		mapBatch = renderer.getSpriteBatch();
		
		font = new BitmapFont();
		font.setColor(Color.WHITE);
		batch2 = new SpriteBatch();

		
//		TiledMapTileLayer mgLayer = (TiledMapTileLayer)map.getLayers().get("Moongate Layer");
//		MapLayer mgLayerProperties = map.getLayers().get("Moongate Properties");
//		Iterator<MapObject> objs = mgLayerProperties.getObjects().iterator();
//		
//		while (objs.hasNext()) {
//			MapObject obj = objs.next();
//			MapProperties props = obj.getProperties();
//			int phase = Integer.parseInt(props.get("phase").toString());
//			int x = Integer.parseInt(props.get("x").toString());
//			int y = Integer.parseInt(props.get("y").toString());
//			Cell c = mgLayer.getCell(x, y-1);
//			System.out.println(c);
//		}
		
		mapCamera = new OrthographicCamera();
		

		Gdx.input.setInputProcessor(this);

	}

	public void resume () {
	}

	public void render () {
		time += Gdx.graphics.getDeltaTime();

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		mapCamera.update();

		renderer.setView(mapCamera);
		renderer.render();
		
		mapBatch.begin();
		mapBatch.draw(player.getKeyFrame(time, true), mapCamera.position.x, mapCamera.position.y, 32, 32);
		mapBatch.end();
		
		
		batch2.begin();
		font.draw(batch2, "current map coords: " + getCurrentMapCoords().toString(), 10, 40);
		font.draw(batch2, "current mouse pos: " + currentMousePos, 10, 20);
		batch2.end();
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
				x * tilePixelWidth + 225, 
				yDownPixel((y) * tilePixelHeight), 
				0);
		
		return v;
	}
	
	public Vector3 getCurrentMapCoords() {
		
		Vector3 v = mapCamera.unproject(new Vector3(375, 400, 0));
		
		return new Vector3(
				Math.round((v.x) / 32), 
				Math.round(yDownPixel(v.y) / 32),
				0);
	}

	public void pause () {
	}

	public void dispose () {
	}
	
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "test";
		cfg.width = screenWidth;
		cfg.height = screenHeight;
		new LwjglApplication(new TestMain(), cfg);

	}
	
	
	public class CustomTiledMap extends TiledMap {
		private TiledMapTileSets tilesets;
		private Array<? extends Disposable> ownedResources;
		public CustomTiledMap () {
			setTilesets(new TiledMapTileSets());
		}
		public void setOwnedResources (Array<? extends Disposable> resources) {
			this.ownedResources = resources;
		}
		@Override
		public void dispose () {
			if (ownedResources != null) {
				for (Disposable resource : ownedResources) {
					resource.dispose();
				}
			}
		}
		public TiledMapTileSets getTilesets() {
			return tilesets;
		}
		public void setTilesets(TiledMapTileSets tilesets) {
			this.tilesets = tilesets;
		}
	}
	
	
}
