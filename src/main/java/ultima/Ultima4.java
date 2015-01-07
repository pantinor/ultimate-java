package ultima;

import util.LogDisplay;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class Ultima4 extends Game {
	
	public static int SCREEN_WIDTH = 800;
	public static int SCREEN_HEIGHT = 600;
	
	public static LogDisplay hud;
	public static BitmapFont logFont;
	
	public static StartScreen startScreen;
		
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Ultima 4 - Quest of the Avatar";
		cfg.width = SCREEN_WIDTH;
		cfg.height = SCREEN_HEIGHT;
		cfg.addIcon("assets/graphics/ankh.png", FileType.Internal);
		new LwjglApplication(new Ultima4(), cfg);

	}

	@Override
	public void create() {
		
		logFont = new BitmapFont();
		logFont.setColor(Color.WHITE);	
		hud = new LogDisplay(logFont);

		startScreen = new StartScreen(this);
		setScreen(startScreen);
		
	}



}
