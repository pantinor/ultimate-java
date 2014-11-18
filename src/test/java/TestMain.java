import ultima.DialogWindow;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
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
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class TestMain extends InputAdapter implements ApplicationListener {
	TextureAtlas atlas;
	Animation player;
	float time = 0;
	private TiledMap map2;
	private OrthogonalTiledMapRenderer renderer;
	Batch mapBatch, batch2;
	Stage stage;
	DialogWindow dialog;
	Skin skin;
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
		
		skin = new Skin(Gdx.files.classpath("skin/uiskin.json"));

		
		font = new BitmapFont();
		font.setColor(Color.WHITE);
		batch2 = new SpriteBatch();
		stage = new Stage();
		dialog = new DialogWindow(stage,null, skin);
		stage.addActor(dialog);
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
		

		//Gdx.input.setInputProcessor(this);
		Gdx.input.setInputProcessor(new InputMultiplexer(this, stage));

	}

	public void resume () {
	}

	public void render () {
		time += Gdx.graphics.getDeltaTime();

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		mapCamera.update();
		
		stage.act();
		stage.draw();
		
		batch2.begin();
		//batch2.draw(player.getKeyFrame(time, true), mapCamera.position.x, mapCamera.position.y, 32, 32);
		batch2.end();
		
		
		batch2.begin();
		//font.draw(batch2, "current map coords: " + getCurrentMapCoords().toString(), 10, 40);
		//font.draw(batch2, "current mouse pos: " + currentMousePos, 10, 20);
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
	
	

	
	
}
