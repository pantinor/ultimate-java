package bioware;

import java.util.ArrayList;
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
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class BamSpriteSheetViewer extends InputAdapter implements ApplicationListener {

    Batch batch;
    float time = 0;

    BitmapFont font;
    List<String> animNames = new ArrayList<>();
    int index = 0;
    TextureAtlas atlas;
    Animation anim;

    boolean highFlag = false;

    public static final String BAMDIR = "C:\\Users\\Paul\\Desktop\\bamSprites\\";

    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "Bioware Baldurs Gate Sprite Viewer";
        cfg.width = 1200;
        cfg.height = 800;
        new LwjglApplication(new BamSpriteSheetViewer(), cfg);
    }

    @Override
    public void create() {

        font = new BitmapFont();

        batch = new SpriteBatch();
        atlas = new TextureAtlas(Gdx.files.absolute(BAMDIR + "UVOLG.txt"));
        //atlas = new TextureAtlas(Gdx.files.internal("bioware-sprites-2"));
        for (AtlasRegion ar : atlas.getRegions()) {
            if (!animNames.contains(ar.name)) {
                animNames.add(ar.name);
            }
        }
        String k = animNames.get(index);
        anim = new Animation(0.125f, atlas.createSprites(k));

        Gdx.input.setInputProcessor(this);

    }

    @Override
    public void render() {

        time += Gdx.graphics.getDeltaTime();

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        String key = animNames.get(index);

        font.draw(batch, key, 10, 800 - 10);
        
        Sprite sp = (Sprite)anim.getKeyFrame(time, true);
        batch.draw(sp, 100, 600);

        batch.end();

    }

    @Override
    public boolean keyUp(int key) {
        if (key == Keys.SPACE) {
            index++;
            if (index >= animNames.size()) {
                index = 0;
            }
            String k = animNames.get(index);
            anim = new Animation(0.125f, atlas.createSprites(k));
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
