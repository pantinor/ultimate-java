import java.util.Iterator;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
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
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class TestMain extends InputAdapter implements ApplicationListener {
	TextureAtlas atlas;
	Animation player;
	float time = 0;
	private TiledMap map;
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
		
		TiledMapTileLayer mgLayer = (TiledMapTileLayer)map.getLayers().get("Moongate Layer");
		MapLayer mgLayerProperties = map.getLayers().get("Moongate Properties");
		Iterator<MapObject> objs = mgLayerProperties.getObjects().iterator();
		
		while (objs.hasNext()) {
			MapObject obj = objs.next();
			MapProperties props = obj.getProperties();
			int phase = Integer.parseInt(props.get("phase").toString());
			int x = Integer.parseInt(props.get("x").toString());
			int y = Integer.parseInt(props.get("y").toString());
			Cell c = mgLayer.getCell(x, y-1);
			System.out.println(c);
		}
		mapCamera = new OrthographicCamera();

		currentMapPixelCoords = getMapPixelCoords(86,108);
		

		Gdx.input.setInputProcessor(this);

	}

	public void resume () {
	}

	public void render () {
		time += Gdx.graphics.getDeltaTime();

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		if (initMapPosition) {
			mapCamera.position.set(currentMapPixelCoords);
			initMapPosition = false;
		}

		mapCamera.update();

		renderer.setView(mapCamera);
		renderer.getViewBounds().set(mapCamera.position.x - 550,
									 mapCamera.position.y - 375, 
									 mapViewBoundsWidth, mapViewBoundsHeight);
	
		renderer.render();
		
		mapBatch.begin();
		mapBatch.draw(player.getKeyFrame(time, true), mapCamera.position.x - 225, mapCamera.position.y, 32, 32);
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
		
		currentMapPixelCoords = mapCamera.unproject(new Vector3(375, 400, 0));

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
	
	

	
	
}
