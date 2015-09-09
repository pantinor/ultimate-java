package test;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import objects.JournalEntries;
import ultima.JournalScreen;

public class JournalTest extends Game {

    public static int SCREEN_WIDTH = 1024;
    public static int SCREEN_HEIGHT = 768;

    public static Texture backGround;

    public static void main(String[] args) {

        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "Journal";
        cfg.width = SCREEN_WIDTH;
        cfg.height = SCREEN_HEIGHT;
        new LwjglApplication(new JournalTest(), cfg);

    }

    @Override
    public void create() {

        Skin skin = new Skin(Gdx.files.internal("assets/skin/uiskin.json"));
        backGround = new Texture(Gdx.files.internal("assets/graphics/frame.png"));

        try {
            File file = new File("journal.save");
            JAXBContext jaxbContext = JAXBContext.newInstance(JournalEntries.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            JournalEntries entries = (JournalEntries) jaxbUnmarshaller.unmarshal(file);
            Screen j = new JournalScreen(null, null, entries);
            setScreen(j);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
