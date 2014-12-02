package ultima;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class Intro implements Screen {
	
	public static final int INTRO_TEXT_OFFSET = 17445 - 1;  // (start at zero)
	public static final int INTRO_MAP_OFFSET = 30339;
	public static final int INTRO_FIXUPDATA_OFFSET = 29806;
	public static final int INTRO_SCRIPT_TABLE_SIZE = 548;
	public static final int INTRO_SCRIPT_TABLE_OFFSET = 30434;
	
	TextureAtlas atlas;
	Animation beast1;
	Animation beast2;
	
	
	public Intro() {
		atlas = new TextureAtlas(Gdx.files.classpath("graphics/tile-atlas.txt"));

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
	public void resize(int width, int height) {
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
	public void hide() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}
	
	

	

}
