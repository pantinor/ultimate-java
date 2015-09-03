package ultima;

import objects.Party;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;

public class DeathScreen extends BaseScreen implements Constants {

    Batch batch;
    long initTime;

    public DeathScreen(BaseScreen retScreen, Party party) {

        this.returnScreen = retScreen;

        deathMsgs[5] = String.format(deathMsgs[5], party.getMember(0).getPlayer().name);

        font = new BitmapFont(Gdx.files.internal("assets/fonts/Calisto_24.fnt"));
        font.setColor(Color.WHITE);

        batch = new SpriteBatch();

        Gdx.input.setInputProcessor(null);

        initTime = System.currentTimeMillis();

        party.reviveAll();

        //You will keep EQUIPPED weapons and armor, 
        //as well as QUEST items, and spells that you have mixed. 
        //Everything else will be gone.
        for (int i = 0; i < 8; i++) {
            party.getSaveGame().weapons[i] = 0;
            party.getSaveGame().armor[i] = 0;
            party.getSaveGame().reagents[i] = 0;
        }

        party.getSaveGame().food = 30000;
        party.getSaveGame().gold = 200;

    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        long diff = System.currentTimeMillis() - initTime;
        long secs = diff / 1000;
        int index = (int) (secs / 4);

        if (index >= deathMsgs.length) {

            mainGame.setScreen(returnScreen);

        } else {

            String s = deathMsgs[index];

            batch.begin();

            float x = Ultima4.SCREEN_WIDTH / 2 - 320;
            float y = 300;
            float width = 640;
            float height = 50;

            GlyphLayout layout = new GlyphLayout(font, s, Color.WHITE, width, Align.left, true);
            x += width / 2 - layout.width / 2;
            y += height / 2 + layout.height / 2;
            font.draw(batch, layout, x, y);

            batch.end();

        }

    }

    public void partyDeath() {

    }

    @Override
    public void finishTurn(int currentX, int currentY) {
        // TODO Auto-generated method stub

    }

    @Override
    public Vector3 getMapPixelCoords(int x, int y) {
        return null;
    }

    @Override
    public Vector3 getCurrentMapCoords() {
        return null;
    }

}
