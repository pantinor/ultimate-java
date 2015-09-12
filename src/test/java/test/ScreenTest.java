package test;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import objects.Party;
import objects.SaveGame;
import ultima.Constants;
import ultima.MixtureScreen;

public class ScreenTest extends Game {

    public static int SCREEN_WIDTH = 1024;
    public static int SCREEN_HEIGHT = 768;

    public static void main(String[] args) {

        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "ScreenTest";
        cfg.width = SCREEN_WIDTH;
        cfg.height = SCREEN_HEIGHT;
        new LwjglApplication(new ScreenTest(), cfg);

    }

    @Override
    public void create() {

        Skin skin = new Skin(Gdx.files.internal("assets/skin/uiskin.json"));

        SaveGame sg = new SaveGame();
        try {
            sg.read(Constants.PARTY_SAV_BASE_FILENAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Party party = new Party(sg);
        party.getMember(0).getPlayer().mp = 999;
        for (Constants.Spell sp : Constants.Spell.values()) {
            party.getSaveGame().mixtures[sp.ordinal()] = 99;
        }
        
        sg.reagents = new int[]{90, 93, 94, 90, 90, 90, 90, 90};
        
        setScreen(new MixtureScreen(null, null, skin, party));

    }

}
