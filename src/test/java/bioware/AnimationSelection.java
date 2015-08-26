package bioware;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class AnimationSelection extends InputAdapter implements ApplicationListener {

    BamAnimationStore store;
    Batch batch;
    float time = 0;

    BitmapFont font;
    List<String> animNames = new ArrayList<String>();
    int index = 0;
    int currentPrefixIndex = 0;

    Object mutex = new Object();

    public static final String BAMDIR = "C:\\Users\\Paul\\Desktop\\BAMS";

    public static final String[] prefixes = {
        "MAIR", "MAIS", "MAKH", "MASG", "MASL", "MBAS", "MBEG",
        "MBEH", "MBER", "MBES", "MCAR", "MDJI", "MDJL", "MDKN",
        "MDLI", "MDOG",
        //"MDOP","MDRO","MDSW","MEAE","MEAS","METN","METT","MEYE","MFDR","MFIE","MFIG","MFIS",
        //"MGCL","MGCP","MGHL","MGIB","MGIT","MGLC","MGNL","MGO1","MGO2","MGO3","MGO4","MGWE",
        //"MHOB","MIGO","MIMP","MKOB","MLER","MLIC","MLIZ",
        //"MMEL","MMIN","MMIS",
        //"MMST","MMUM","MMY2","MMYC","MNO1","MNO2","MNO3","MOGH","MOGM","MOGN","MOGR","MOR1",
        //"MOR2","MOR3","MOR4","MOR5","MOTY",
        //"MRAK","MRAV","MSA2","MSAH","MSAI","MSAL",
        //"MSAT","MSHD","MSIR","MSKA","MSKB",
        //"MSKT","MSLI","MSLY","MSNK","MSOG",
        //"MMUM","MSOL","MSPI","MSPL","MSPS","MTAN","MTAS","MTRO","MTRS","MUMB","MVAF","MVAM","MWER",
        "MWFM", "MWLF", "MWLS", "MWYV", "MXVT", "MYU1", "MYU2", "MYU3", "MZOM"};

    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "Bioware Baldurs Gate Sprite Viewer";
        cfg.width = 1200;
        cfg.height = 800;
        new LwjglApplication(new AnimationSelection(), cfg);
    }

    @Override
    public void create() {

        font = new BitmapFont();

        initStore(prefixes[currentPrefixIndex]);

        batch = new SpriteBatch();

        Gdx.input.setInputProcessor(this);

    }

    public void initStore(String prefix) {

        animNames.clear();
        index = 0;
        synchronized (mutex) {

            store = new BamAnimationStore(BAMDIR, prefix);
            store.init();

            System.out.println("animations size=" + store.gdxAnimations.size());
            Iterator<String> iter = store.gdxAnimations.keySet().iterator();
            while (iter.hasNext()) {
                String key = iter.next();

				//removed all the animations that are not standing animations 
                //cause there are too many others dont care about
                if (!key.startsWith("STAND")) {
                    iter.remove();
                    continue;
                }

                System.out.println(key + " size=" + store.gdxAnimations.get(key).getKeyFrames().length);
                animNames.add(key);
            }

        }

    }

    public void render() {

        time += Gdx.graphics.getDeltaTime();

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        String key = animNames.get(index);

        font.draw(batch, store.sheetName, 10, 800 - 10);
        font.draw(batch, key, 10, 800 - 30);

        synchronized (mutex) {
            Animation anim = store.gdxAnimations.get(key);
            batch.draw(anim.getKeyFrame(time, true), 100, 800 - 200);
        }

        batch.end();

    }

    @Override
    public boolean keyUp(int key) {
        if (key == Keys.SPACE) {
            index++;
            if (index >= animNames.size()) {
                index = 0;
            }
        } else if (key == Keys.ENTER) {
            currentPrefixIndex++;
            if (currentPrefixIndex >= prefixes.length) {
                index = 0;
            }
            initStore(prefixes[currentPrefixIndex]);
        }
        return false;
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
    public void dispose() {
		// TODO Auto-generated method stub

    }

}
