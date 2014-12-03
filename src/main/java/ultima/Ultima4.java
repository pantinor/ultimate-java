package ultima;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class Ultima4 extends Game {
	
	public static int SCREEN_WIDTH = 800;
	public static int SCREEN_HEIGHT = 600;
	
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Ultima 4 - Quest of the Avatar";
		cfg.width = SCREEN_WIDTH;
		cfg.height = SCREEN_HEIGHT;
		cfg.addIcon("graphics/ankh.png", FileType.Classpath);
		new LwjglApplication(new Ultima4(), cfg);

	}

	@Override
	public void create() {
		
		setScreen(new StartScreen(this));
		
	}



}
