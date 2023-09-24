package ultima;

import java.util.Observable;
import java.util.Observer;
import java.util.Random;

import objects.BaseMap;
import objects.Creature;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import util.XORShiftRandom;

public abstract class BaseScreen implements Screen, InputProcessor, Constants, Observer {

    public ScreenType scType;

    public static Ultima4 mainGame;

    protected BaseScreen returnScreen;
    public Context context;
    protected Stage stage;

    protected float time = 0;
    protected Random rand = new XORShiftRandom();

    protected int mapPixelHeight;
    public Vector3 newMapPixelCoords;

    protected Viewport viewport = new ScreenViewport();

    protected Camera camera;

    protected int showZstats = 0;

    protected Vector2 currentMousePos;

    protected Creature currentEncounter;

    /**
     * translate map tile coords to world pixel coords
     */
    public abstract Vector3 getMapPixelCoords(int x, int y);

    /**
     * get the map coords at the camera center
     */
    public abstract Vector3 getCurrentMapCoords();

    @Override
    public void dispose() {
    }

    public void log(String s) {
        Ultima4.hud.add(s);
    }

    public void logAppend(String s) {
        Ultima4.hud.append(s);
    }

    public void logDeleteLastChar() {
        Ultima4.hud.logDeleteLastChar();
    }

    public Stage getStage() {
        return stage;
    }

    public abstract void finishTurn(int currentX, int currentY);

    public void endCombat(boolean isWon, BaseMap combatMap, boolean wounded) {
    }

    public final void addButtons() {
        TextButton jourButt = new TextButton("Journal", Ultima4.skin, "wood");
        jourButt.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                mainGame.setScreen(new JournalScreen(mainGame, BaseScreen.this, Ultima4.skin, context.getJournal()));
            }
        });
        jourButt.setX(530);
        jourButt.setY(15);
        stage.addActor(jourButt);

        TextButton bookButt = new TextButton("Book", Ultima4.skin, "wood");
        bookButt.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                mainGame.setScreen(new BookScreen(mainGame, BaseScreen.this, Ultima4.skin));
            }
        });
        bookButt.setX(625);
        bookButt.setY(15);

        stage.addActor(bookButt);

        int y = 42;
        for (int j = 0; j < 8; j++) {
            Virtue v = Constants.Virtue.values()[j];
            int x = (j < 4 ? 58 : 316);
            y = y - 12;
            if (y < 0) {
                y = 42;
            }
            VirtueBarDrawable bar = new VirtueBarDrawable(v);
            bar.setX(x);
            bar.setY(y);
            stage.addActor(bar);
        }

    }

    private class VirtueBarDrawable extends Actor {

        private Virtue v;

        public VirtueBarDrawable(Virtue v) {
            this.v = v;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            batch.draw(v.getBar(), getX(), getY());
            Ultima4.smallFont.draw(batch, v.getAbbr(), getX() - 30, getY() + 8);
        }
    }

    @Override
    public void update(Observable obs, Object obj) {
        if (obj instanceof PartyEvent) {
            PartyEvent ev = (PartyEvent) obj;
            switch (ev) {
                case ACTIVE_PLAYER_CHANGED:
                    break;
                case ADVANCED_LEVEL:
                    log("Thou art now Level");
                    Sounds.play(Sound.MAGIC);
                    break;
                case POSITIVE_KARMA:
                    Sounds.play(Sound.POSITIVE_EFFECT);
                    break;
                case NEGATIVE_KARMA:
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                    break;
                case INVENTORY_ADDED:
                    break;
                case LOST_EIGHTH:
                    log("Thou hast lost an eighth of avatarhood!");
                    Sounds.play(Sound.STEAL_ESSENCE);
                    break;
                case MEMBER_JOINED:
                    Sounds.play(Sound.POSITIVE_EFFECT);
                    break;
                case PARTY_REVIVED:
                case PARTY_DEATH:
                    break;
                case POISON_DAMAGE:
                    Sounds.play(Sound.POISON_DAMAGE);
                    break;
                case STARVING:
                    log("Starving!!!");
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                    break;
                case TRANSPORT_CHANGED:
                    break;
                default:
                    break;

            }
        }
    }

    public abstract void partyDeath();

    @Override
    public void hide() {
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        currentMousePos = new Vector2(screenX, screenY);
        return false;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean touchCancelled(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean scrolled(float f, float f1) {
        return false;
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    public InputProcessor getPeerGemInputProcessor() {
        return null;
    }

    public class NewOrderInputAdapter extends InputAdapter {

        int p1 = -1;
        int p2 = -1;
        BaseScreen screen;

        public NewOrderInputAdapter(BaseScreen screen) {
            this.screen = screen;
        }

        @Override
        public boolean keyUp(int keycode) {
            if (keycode >= Keys.NUM_1 && keycode <= Keys.NUM_8) {
                if (p1 == -1) {
                    p1 = keycode - 7 - 1;
                    logAppend(" " + (p1 + 1));
                    log("with #:");
                    return false;
                } else if (p2 == -1) {
                    p2 = keycode - 7 - 1;
                    logAppend(" " + (p2 + 1));
                    if (p1 == 0 || p2 == 0) {
                        log("You must lead!");
                    } else {
                        context.getParty().swapPlayers(p1, p2);;
                    }
                }
            } else {
                log("What?");
            }

            if (this.screen instanceof GameScreen) {
                Vector3 v = getCurrentMapCoords();
                finishTurn((int) v.x, (int) v.y);
            }

            Gdx.input.setInputProcessor(new InputMultiplexer(this.screen, stage));
            return false;
        }
    }

}
