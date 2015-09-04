package ultima;

import util.LogDisplay;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class Ultima4 extends Game {

    public static int SCREEN_WIDTH = 1024;
    public static int SCREEN_HEIGHT = 768;
    
    public static int MAP_WIDTH = 672;
    public static int MAP_HEIGHT = 672;
    
    public static LogDisplay hud;
    public static Texture backGround;
    
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
        
        BitmapFont logFont = new BitmapFont(Gdx.files.internal("assets/fonts/Calisto_18.fnt"));
        logFont.setColor(Color.WHITE);
        hud = new LogDisplay(logFont);
        
        backGround = new Texture(Gdx.files.internal("assets/graphics/frame.png"));

        startScreen = new StartScreen(this);
        setScreen(startScreen);

    }

}
