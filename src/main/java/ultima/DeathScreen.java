package ultima;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class DeathScreen extends BaseScreen implements Constants {
	
	Batch batch;
	long initTime;
	
	public DeathScreen(Ultima4 mainGame, BaseScreen retScreen, String name) {
		
		this.mainGame = mainGame;
		this.returnScreen = retScreen;
		
		deathMsgs[5] = String.format(deathMsgs[5], name);
			
		font = new BitmapFont(Gdx.files.classpath("fonts/Calisto_24.fnt"));
		font.setColor(Color.WHITE);
		
		batch = new SpriteBatch();
		
		Gdx.input.setInputProcessor(null);
		
		initTime = System.currentTimeMillis();

	}
	
	@Override
	public void render(float delta) {
		
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		
		long diff = System.currentTimeMillis() - initTime;
		long secs = diff / 1000;
		int index = (int)(secs / 5);
		
		if (index == deathMsgs.length) {
			mainGame.setScreen(returnScreen);
		}
		
		String s = deathMsgs[index];

				
		batch.begin();
		
		float x = Ultima4.SCREEN_WIDTH/2-320;
		float y = 300;
		float width = 640;
		float height = 50;
		
		TextBounds bounds = font.getWrappedBounds(s, width);
		x += width / 2 - bounds.width / 2;
		y += height / 2 + bounds.height / 2;
		font.drawWrapped(batch, s, x, y, width);
				
		batch.end();

	}

	@Override
	public void finishTurn(int currentX, int currentY) {
		// TODO Auto-generated method stub
		
	}

}
