package ultima;

import util.LogDisplay;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

public class Ultima4 extends Game {

    public static int SCREEN_WIDTH = 1024;
    public static int SCREEN_HEIGHT = 768;
    
    public static int MAP_WIDTH = 672;
    public static int MAP_HEIGHT = 672;
    
    public static LogDisplay hud;
    public static Texture backGround;
    public static BitmapFont font;
    public static StartScreen startScreen;
    public static Skin skin;
    
    public static boolean playMusic = true;
    public static float musicVolume = 0.1f;
    public static Music music;
    
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
        
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("assets/fonts/lindberg.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 16;
        font = generator.generateFont(parameter);
        
        parameter.size = 24;
        BitmapFont fontLarger = generator.generateFont(parameter);
        
        generator.dispose();
        
        skin = new Skin(Gdx.files.internal("assets/skin/uiskin.json"));
        skin.remove("default-font", BitmapFont.class);
        skin.add("default-font", font, BitmapFont.class);
        skin.add("journal", font, BitmapFont.class);
        skin.add("death-screen", fontLarger, BitmapFont.class);
        
        Label.LabelStyle ls = Ultima4.skin.get("default", Label.LabelStyle.class);
        ls.font = font;
        TextButton.TextButtonStyle tbs = Ultima4.skin.get("default", TextButton.TextButtonStyle.class);
        tbs.font = font;
        TextButton.TextButtonStyle tbswood = Ultima4.skin.get("wood", TextButton.TextButtonStyle.class);
        tbswood.font = font;
        SelectBox.SelectBoxStyle sbs = Ultima4.skin.get("default", SelectBox.SelectBoxStyle.class);
        sbs.font = font;
        sbs.listStyle.font = font;
        CheckBox.CheckBoxStyle cbs = Ultima4.skin.get("default", CheckBox.CheckBoxStyle.class);
        cbs.font = font;
        List.ListStyle lis = Ultima4.skin.get("default", List.ListStyle.class);
        lis.font = font;
        TextField.TextFieldStyle tfs = Ultima4.skin.get("default", TextField.TextFieldStyle.class);
        tfs.font = font;

        hud = new LogDisplay(font);
        
        backGround = new Texture(Gdx.files.internal("assets/graphics/frame.png"));

        startScreen = new StartScreen(this);
        
//        SaveGame sg = new SaveGame();
//        sg.items |= Constants.Item.KEY_C.getLoc();
//        sg.items |= Constants.Item.KEY_L.getLoc();
//        sg.items |= Constants.Item.KEY_T.getLoc();
//                SaveGame.SaveGamePlayerRecord rec = sg.new SaveGamePlayerRecord();
//        rec.name = "avatar";
//        rec.hp = 200;
//
//        Party p = new Party(sg);
//        for (int i = 0; i < 8; i++) {
//            sg.karma[i] = 0;
//        }
//        for (int i = 0; i < 7; i++) {
//            try {
//                p.addMember(rec);
//            } catch (Exception ex) {
//            }
//        }

//        setScreen(new CodexScreen(startScreen, p));
        
          setScreen(startScreen);

    }

}
