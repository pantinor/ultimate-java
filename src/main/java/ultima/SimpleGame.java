package ultima;


import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public abstract class SimpleGame extends InputAdapter implements ApplicationListener {

	public OrthographicCamera mapCamera;

	public Stage stage;
	public Skin skin;
	
	public SimpleGame() {
	}

	public abstract void init();

	public abstract void draw(float delta);

	public void create() {
		
		stage = new Stage(new ScreenViewport());
		skin = new Skin(Gdx.files.classpath("skin/uiskin.json"));

		Gdx.input.setInputProcessor(this);
		
		init();

	}

	public void render() {
						
		draw(Gdx.graphics.getDeltaTime());
		
	}

	public boolean keyDown(int keycode) {
		return false;
	}

	public boolean keyUp(int keycode) {
		return false;
	}

	public boolean keyTyped(char character) {
		return false;
	}

	public boolean touchDown(int x, int y, int pointer, int button) {
		return false;
	}

	public boolean touchUp(int x, int y, int pointer, int button) {
		return false;
	}

	public boolean touchDragged(int x, int y, int pointer) {
		return false;
	}

	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	public boolean scrolled(int amount) {
		return false;
	}

	public void pause() {
	}

	public void resume() {
	}

	public void dispose() {
	}

	public void resize(int width, int height) {
	}
	

}
