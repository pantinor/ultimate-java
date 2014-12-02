import ultima.StartScreen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;

public class TestMain extends Game {
	
	TextureAtlas atlas;
	Animation beast1;
	Animation beast2;

	float time = 0;
	Batch batch2;
	

	public void create () {
		
		atlas = new TextureAtlas(Gdx.files.classpath("graphics/beasties-atlas.txt"));
		
		Array<AtlasRegion> anim1 = atlas.findRegions("beast");
		Array<AtlasRegion> anim2 = atlas.findRegions("dragon");

		Array<AtlasRegion> tmp1 = new Array<AtlasRegion>(StartScreen.beast1FrameIndexes.length);
		Array<AtlasRegion> tmp2 = new Array<AtlasRegion>(StartScreen.beast2FrameIndexes.length);
		
		for (int i=0;i<StartScreen.beast1FrameIndexes.length;i++) tmp1.add(anim1.get(StartScreen.beast1FrameIndexes[i]));
		for (int i=0;i<StartScreen.beast2FrameIndexes.length;i++) tmp2.add(anim2.get(StartScreen.beast2FrameIndexes[i]));
		
		beast1 = new Animation(0.25f, tmp1);
		beast2 = new Animation(0.25f, tmp2);
				
		batch2 = new SpriteBatch();

	}

	public void render () {
		time += Gdx.graphics.getDeltaTime();

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
				
		
		batch2.begin();
		batch2.draw(beast1.getKeyFrame(time, true), 100, 100, 48*2, 31*2);
		batch2.draw(beast2.getKeyFrame(time, true), 200, 200, 48*2, 31*2);

		batch2.end();
		
	}
	

	
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "test";
		cfg.width = 800;
		cfg.height = 600;
		new LwjglApplication(new TestMain(), cfg);
	}
	
	

	
	
}
