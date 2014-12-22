package ultima;

import java.util.Observable;
import java.util.Observer;
import java.util.Random;

import objects.BaseMap;
import objects.Creature;
import util.LogDisplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public abstract class BaseScreen implements Screen, InputProcessor, Constants, Observer {
	
	protected ScreenType scType;
	
	protected Ultima4 mainGame;
	protected BaseScreen returnScreen;
	
	protected Stage stage;
	protected Skin skin;
	
	protected float time = 0;
	protected Random rand = new Random();

	protected int mapPixelHeight;
	protected Vector3 newMapPixelCoords;
	protected boolean changeMapPosition = false;
	
	protected OrthographicCamera mapCamera;
	protected int showZstats = 0;
	
	public static LogDisplay logs;
	
	protected BitmapFont font;

	protected Vector2 currentMousePos;
	
	protected Creature currentEncounter;
	
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
	public void logAppend(String s) {
		logs.append(s);
	}
	public void logDeleteLastChar() {
		logs.logDeleteLastChar();
	}
	
	public abstract void finishTurn(int currentX, int currentY) ;

	public void endCombat(boolean isWon, BaseMap combatMap) {

	}
	
	public void update(Observable obs, Object obj) {
		if (obj instanceof PartyEvent) {
			PartyEvent ev = (PartyEvent)obj;
			switch(ev) {
			case ACTIVE_PLAYER_CHANGED:
				break;
			case ADVANCED_LEVEL:
				log("Thou art now Level");
				Sounds.play(Sound.MAGIC);
				break;
			case POSITIVE_KARMA:
				Sounds.play(Sound.POSITIVE_EFFECT);
				break;
			case NEGATIVE_KARMA:
				Sounds.play(Sound.NEGATIVE_EFFECT);
				break;
			case INVENTORY_ADDED:
				break;
			case LOST_EIGHTH:
				log("Thou hast lost an eighth of avatarhood!");
				Sounds.play(Sound.STEAL_ESSENCE);
				break;
			case MEMBER_JOINED:
				Sounds.play(Sound.POSITIVE_EFFECT);
				break;
			case PARTY_REVIVED:
				break;
			case PLAYER_KILLED:
				break;
			case POISON_DAMAGE:
				Sounds.play(Sound.POISON_DAMAGE);
				break;
			case STARVING:
				log("Starving!!!");
				Sounds.play(Sound.NEGATIVE_EFFECT);
				break;
			case TRANSPORT_CHANGED:
				break;
			default:
				break;
			
			}
		}
	}
	
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
		//mapCamera.viewportWidth = width;
		//mapCamera.viewportHeight = height;
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
