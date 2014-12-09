package ultima;

import java.util.List;
import java.util.Random;

import util.FixedSizeArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public abstract class BaseScreen implements Screen, InputProcessor, Constants {
	
	protected ScreenType scType;
	
	protected Ultima4 mainGame;
	protected Screen returnScreen;
	
	protected Stage stage;
	protected Skin skin;
	
	protected float time = 0;
	protected Random rand = new Random();

	protected int mapPixelHeight;
	protected Vector3 newMapPixelCoords;
	protected boolean changeMapPosition = true;
	
	protected OrthographicCamera mapCamera;
	protected List<String> logs = new FixedSizeArrayList<String>(5);
	protected int showZstats = 0;
	
	protected BitmapFont font;

	protected Vector2 currentMousePos;
	
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
	
	public void log(String s) {
		logs.add(s);
	}
	
	public abstract void finishTurn(int currentX, int currentY) ;

	
	@Override
	public void hide() {
		Gdx.input.setInputProcessor(null);
	}

	@Override
	public boolean mouseMoved (int screenX, int screenY) {
		currentMousePos = new Vector2(screenX, screenY);
		return false;
	}
	

	@Override
	public void resize(int width, int height) {
		mapCamera.viewportWidth = width;
		mapCamera.viewportHeight = height;
	}
	
	@Override
	public boolean keyDown(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
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
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render(float delta) {
		// TODO Auto-generated method stub
		
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
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

}
