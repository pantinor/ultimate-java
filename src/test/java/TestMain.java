import ultima.LogScrollerWindow;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
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
	Animation beast1;
	Animation beast2;

	float time = 0;
	private TiledMap map2;
	private OrthogonalTiledMapRenderer renderer;
	Batch mapBatch, batch2;
	Stage stage;
	LogScrollerWindow dialog;
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
		
		atlas = new TextureAtlas(Gdx.files.classpath("graphics/beasties-atlas.txt"));
		
		beast1 = new Animation(0.25f, atlas.findRegions("beast"));
		beast2 = new Animation(0.25f, atlas.findRegions("dragon"));


		
		font = new BitmapFont();
		font.setColor(Color.WHITE);
		batch2 = new SpriteBatch();

		Gdx.input.setInputProcessor(this);

	}

	public void resume () {
	}

	public void render () {
		time += Gdx.graphics.getDeltaTime();

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
				
		
		batch2.begin();
		batch2.draw(beast1.getKeyFrame(time, true), 100, 100, 48, 31);
		batch2.draw(beast2.getKeyFrame(time, true), 200, 200, 48, 31);

		batch2.end();
		
		
	}
	
	@Override
	public boolean mouseMoved (int screenX, int screenY) {
		currentMousePos = new Vector2(screenX, screenY);
		return false;
	}
	
	@Override
	public boolean keyUp (int keycode) {

		
		return false;

	}

	@Override
	public void resize(int width, int height) {

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
