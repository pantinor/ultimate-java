package ultima;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import objects.SaveGame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import objects.Tile;
import org.apache.commons.io.IOUtils;
import static ultima.Constants.PARTY_SAV_BASE_FILENAME;
import static ultima.Ultima4.skin;
import util.UltimaTiledMapLoader;
import util.XORShiftRandom;

public class StartScreen implements Screen, InputProcessor, Constants {

    public static int[] beast1FrameIndexes = {1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 1, 2, 3, 4, 1, 2, 5, 6, 7, 8, 5, 6, 7, 8, 5, 6, 7, 8, 5, 6, 7, 8, 5, 6, 7, 8, 5, 6, 7, 8, 9, 10, 9, 10, 9, 10, 11, 11, 11, 11, 12, 12, 13, 13, 12, 13, 12, 13, 12, 11, 11, 11, 0, 0, 1, 2, 3, 4, 1, 2, 5, 6, 7, 8, 5, 6, 7, 8, 9, 10, 11, 11, 11, 0, 0, 14, 14, 14, 15, 16, 16, 16, 17, 17, 17, 16, 16, 16, 17, 17, 17, 16, 16, 16, 15, 14, 14, 0, 0, 11, 11, 11};
    public static int[] beast2FrameIndexes = {1, 0, 1, 2, 3, 4, 3, 2, 1, 0, 1, 2, 3, 4, 5, 6, 5, 6, 5, 6, 4, 7, 8, 9, 10, 9, 8, 7, 8, 9, 10, 11, 12, 11, 12, 13, 11, 12, 13, 1, 13, 1, 14, 1, 15, 1, 14, 1, 15, 10, 9, 8, 16, 17, 16, 17, 16, 17, 9, 8, 7, 4, 3, 2, 0};

    Animation<TextureRegion> beast1;
    Animation<TextureRegion> beast2;

    TextButton init;
    TextButton journey;
    Stage stage;

    Texture title;
    BitmapFont font;
    static TextureAtlas ta;
    TextureRegion storyTexture;

    StringBuilder nameBuffer = new StringBuilder();
    StringBuilder sexBuffer = new StringBuilder();

    NameInputAdapter nia = new NameInputAdapter();
    SexInputAdapter sia = new SexInputAdapter();
    StoryInputAdapter stia = new StoryInputAdapter();
    QuestionInputAdapter qia = new QuestionInputAdapter();
    DoneInputAdapter dia = new DoneInputAdapter();

    int storyInd = 0;

    public static int questionRound = 0;
    static int answerInd = 0;
    boolean pauseFlag = true;
    String currentQuestion = null;
    public static int[] questionTree = new int[15];

    static List<Sprite> beads = new ArrayList<>();

    int GYP_PLACES_FIRST = 0;
    int GYP_PLACES_TWOMORE = 1;
    int GYP_PLACES_LAST = 2;
    int GYP_UPON_TABLE = 3;
    int GYP_SEGUE1 = 13;
    int GYP_SEGUE2 = 14;

    float time = 0;
    Batch batch;
    OrthogonalTiledMapRenderer splashRenderer;
    OrthographicCamera camera;
    Viewport viewPort;
    TiledMap splashMap;
    Ultima4 mainGame;
    IntroAnim animator = new IntroAnim();
    
    public enum State {

        INIT,
        ASK_NAME,
        ASK_SEX,
        TELL_STORY,
        ASK_QUESTIONS,
        DONE;
    }

    State state = State.INIT;

    public StartScreen(Ultima4 mainGame) {

        this.mainGame = mainGame;

        TextureAtlas ba = new TextureAtlas(Gdx.files.internal("assets/graphics/beasties-atlas.txt"));
        Array<AtlasRegion> anim1 = ba.findRegions("beast");
        Array<AtlasRegion> anim2 = ba.findRegions("dragon");
        Array<AtlasRegion> tmp1 = new Array<>(beast1FrameIndexes.length);
        Array<AtlasRegion> tmp2 = new Array<>(beast2FrameIndexes.length);
        for (int i = 0; i < beast1FrameIndexes.length; i++) {
            tmp1.add(anim1.get(beast1FrameIndexes[i]));
        }
        for (int i = 0; i < beast2FrameIndexes.length; i++) {
            tmp2.add(anim2.get(beast2FrameIndexes[i]));
        }
        beast1 = new Animation(0.25f, tmp1);
        beast2 = new Animation(0.25f, tmp2);

        ta = new TextureAtlas(Gdx.files.internal("assets/graphics/initial-atlas.txt"));

        title = new Texture(Gdx.files.internal("assets/graphics/splash.png"));

        font = new BitmapFont(Gdx.files.internal("assets/fonts/Calisto_24.fnt"));
        font.setColor(Color.WHITE);

        init = new TextButton("New Game", skin, "wood");
        init.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                Sounds.play(Sound.TRIGGER);
                state = State.ASK_NAME;
                stage.clear();
            }
        });
        init.setX(330);
        init.setY(Ultima4.SCREEN_HEIGHT - 350);
        init.setWidth(150);
        init.setHeight(25);

        journey = new TextButton("Journey Onward", skin, "wood");
        journey.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                Sounds.play(Sound.TRIGGER);
                if (!Gdx.files.internal(PARTY_SAV_BASE_FILENAME).file().exists()) {
                    state = State.ASK_NAME;
                } else {
                    mainGame.setScreen(new GameScreen(mainGame));
                }
            }
        });
        journey.setX(530);
        journey.setY(Ultima4.SCREEN_HEIGHT - 350);
        journey.setWidth(150);
        journey.setHeight(25);

        UltimaTiledMapLoader loader = new UltimaTiledMapLoader(Maps.WORLD, Ultima4.standardAtlas, 19, 5, tilePixelWidth, tilePixelHeight);
        splashMap = loader.load(intromap, 19, 5, Ultima4.baseTileSet, tilePixelWidth);
        splashRenderer = new OrthogonalTiledMapRenderer(splashMap);
        camera = new OrthographicCamera(19 * tilePixelWidth, 5 * tilePixelHeight);
        camera.position.set(tilePixelWidth * 10, tilePixelHeight * 6, 0);
        viewPort = new ScreenViewport(camera);

        batch = new SpriteBatch();

        stage = new Stage();
        stage.addActor(init);
        stage.addActor(journey);

        SequenceAction seq1 = Actions.action(SequenceAction.class);
        seq1.addAction(Actions.delay(.1f));
        seq1.addAction(Actions.run(animator));
        stage.addAction(Actions.forever(seq1));

        Gdx.input.setInputProcessor(new InputMultiplexer(this, stage));

        Ultima4.music = Sounds.play(Sound.SPLASH, Ultima4.musicVolume);

    }

    @Override
    public void render(float delta) {
        time += Gdx.graphics.getDeltaTime();

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float x = Ultima4.SCREEN_WIDTH / 2 - 320;
        float y = 100;
        float width = 640;
        float height = 50;

        if (state == State.INIT) {

            camera.update();

            splashRenderer.setView(camera.combined, 0, 0, 19 * tilePixelWidth, 5 * tilePixelHeight);
            splashRenderer.render();

            batch.begin();
            batch.draw(title, 0, 0);
            font.draw(batch, "In another world, in a time to come.", 320, Ultima4.SCREEN_HEIGHT - 295);
            font.draw(batch, "LIBGDX Conversion by Paul Antinori", 300, 84);
            font.draw(batch, "Copyright 1987 Lord British", 350, 48);
            batch.draw(beast1.getKeyFrame(time, true), 0, Ultima4.SCREEN_HEIGHT - 31 * 2, 48 * 2, 31 * 2);
            batch.draw(beast2.getKeyFrame(time, true), Ultima4.SCREEN_WIDTH - 48 * 2, Ultima4.SCREEN_HEIGHT - 31 * 2, 48 * 2, 31 * 2);
            batch.end();

            stage.act();
            stage.draw();

        } else if (state == State.ASK_NAME) {

            batch.begin();
            batch.draw(title, 0, 0);
            font.draw(batch, "By what name shalt thou be known", 320, 315);
            font.draw(batch, "in this world and time?", 320, 290);
            font.draw(batch, nameBuffer.toString(), 320, 265);
            batch.draw(beast1.getKeyFrame(time, true), 0, Ultima4.SCREEN_HEIGHT - 31 * 2, 48 * 2, 31 * 2);
            batch.draw(beast2.getKeyFrame(time, true), Ultima4.SCREEN_WIDTH - 48 * 2, Ultima4.SCREEN_HEIGHT - 31 * 2, 48 * 2, 31 * 2);
            batch.end();

            Gdx.input.setInputProcessor(nia);

        } else if (state == State.ASK_SEX) {

            batch.begin();
            batch.draw(title, 0, 0);
            font.draw(batch, "Art thou Male or Female?", 320, 315);
            font.draw(batch, sexBuffer.toString(), 320, 275);
            batch.draw(beast1.getKeyFrame(time, true), 0, Ultima4.SCREEN_HEIGHT - 31 * 2, 48 * 2, 31 * 2);
            batch.draw(beast2.getKeyFrame(time, true), Ultima4.SCREEN_WIDTH - 48 * 2, Ultima4.SCREEN_HEIGHT - 31 * 2, 48 * 2, 31 * 2);
            batch.end();

            Gdx.input.setInputProcessor(sia);

        } else if (state == State.TELL_STORY) {

            if (storyInd == 0) {
                storyTexture = ta.findRegion("tree");
            } else if (storyInd == 6) {
                storyTexture = ta.findRegion("portal");
            } else if (storyInd == 11) {
                storyTexture = ta.findRegion("tree");
            } else if (storyInd == 15) {
                storyTexture = ta.findRegion("outside");
            } else if (storyInd == 17) {
                storyTexture = ta.findRegion("inside");
            } else if (storyInd == 20) {
                storyTexture = ta.findRegion("wagon");
            } else if (storyInd == 21) {
                storyTexture = ta.findRegion("gypsy");
            } else if (storyInd == 23) {
                storyTexture = ta.findRegion("abacus");
            }

            batch.begin();
            batch.draw(beast1.getKeyFrame(time, true), 0, Ultima4.SCREEN_HEIGHT - 31 * 2, 48 * 2, 31 * 2);
            batch.draw(beast2.getKeyFrame(time, true), Ultima4.SCREEN_WIDTH - 48 * 2, Ultima4.SCREEN_HEIGHT - 31 * 2, 48 * 2, 31 * 2);
            batch.draw(storyTexture, Ultima4.SCREEN_WIDTH / 2 - 320, 200, 640, 304);

            GlyphLayout layout = new GlyphLayout(font, initScripts[storyInd], Color.WHITE, width, Align.left, true);
            x += width / 2 - layout.width / 2;
            y += height / 2 + layout.height / 2;
            font.draw(batch, layout, x, y);
            batch.end();

            Gdx.input.setInputProcessor(stia);

        } else if (state == State.ASK_QUESTIONS || state == State.DONE) {

            batch.begin();
            batch.draw(beast1.getKeyFrame(time, true), 0, Ultima4.SCREEN_HEIGHT - 31 * 2, 48 * 2, 31 * 2);
            batch.draw(beast2.getKeyFrame(time, true), Ultima4.SCREEN_WIDTH - 48 * 2, Ultima4.SCREEN_HEIGHT - 31 * 2, 48 * 2, 31 * 2);
            batch.draw(storyTexture, Ultima4.SCREEN_WIDTH / 2 - 320, 200, 640, 304);

            for (Sprite b : beads) {
                batch.draw(b, b.getX(), b.getY(), 16, 24);
            }

            if (questionRound > 6) {
                questionRound = 6;
            }

            String v1 = Virtue.get(questionTree[questionRound * 2]).toString().toLowerCase();
            String v2 = Virtue.get(questionTree[questionRound * 2 + 1]).toString().toLowerCase();
            batch.draw(ta.findRegion(v1), 225, 232, 178, 244);
            batch.draw(ta.findRegion(v2), 625, 232, 178, 244);

            if (state == State.ASK_QUESTIONS) {

                if (pauseFlag) {

                    StringBuffer sb = new StringBuffer();
                    sb.append(gypsyText[questionRound == 0 ? GYP_PLACES_FIRST : (questionRound == 6 ? GYP_PLACES_LAST : GYP_PLACES_TWOMORE)]);
                    sb.append(gypsyText[GYP_UPON_TABLE]);
                    sb.append(String.format("%s and %s.  She says", gypsyText[questionTree[questionRound * 2] + 4], gypsyText[questionTree[questionRound * 2 + 1] + 4]));
                    sb.append("\nConsider this:");
                    String text = sb.toString();

                    GlyphLayout layout = new GlyphLayout(font, text, Color.WHITE, width, Align.left, true);
                    x += width / 2 - layout.width / 2;
                    y += height / 2 + layout.height / 2;
                    font.draw(batch, layout, x, y);

                } else {

                    GlyphLayout layout = new GlyphLayout(font, currentQuestion, Color.WHITE, width, Align.left, true);
                    x += width / 2 - layout.width / 2;
                    y += height / 2 + layout.height / 2;
                    font.draw(batch, layout, x, y);

                }

                Gdx.input.setInputProcessor(qia);

            } else {

                GlyphLayout layout = new GlyphLayout(font, gypsyText[storyInd], Color.WHITE, width, Align.left, true);
                x += width / 2 - layout.width / 2;
                y += height / 2 + layout.height / 2;
                font.draw(batch, layout, x, y);

                Gdx.input.setInputProcessor(dia);
            }

            batch.end();
        }

    }

    @Override
    public boolean keyUp(int keycode) {

        if (keycode == Keys.I) {
            state = State.ASK_NAME;
        } else if (keycode == Keys.J) {
            if (!Gdx.files.internal(PARTY_SAV_BASE_FILENAME).file().exists()) {
                state = State.ASK_NAME;
            } else {
                mainGame.setScreen(new GameScreen(mainGame));
            }
        }

        return false;
    }

    class NameInputAdapter extends InputAdapter {

        @Override
        public boolean keyUp(int keycode) {
            if (keycode == Keys.ENTER) {
                if (nameBuffer.length() < 1) {
                    return false;
                }
                state = State.ASK_SEX;
            } else if (keycode == Keys.BACKSPACE) {
                if (nameBuffer.length() > 0) {
                    nameBuffer.deleteCharAt(nameBuffer.length() - 1);
                }
            } else if (keycode >= 29 && keycode <= 54) {
                if (nameBuffer.length() < 1) {
                    nameBuffer.append(Keys.toString(keycode));
                } else {
                    nameBuffer.append(Keys.toString(keycode).toLowerCase());
                }
            }
            return false;
        }
    }

    class SexInputAdapter extends InputAdapter {

        @Override
        public boolean keyUp(int keycode) {
            if (keycode == Keys.ENTER) {
                if (sexBuffer.length() < 1) {
                    return false;
                }
                state = State.TELL_STORY;
            } else if (keycode == Keys.BACKSPACE) {
                if (sexBuffer.length() > 0) {
                    sexBuffer.deleteCharAt(sexBuffer.length() - 1);
                }
            } else if (keycode == Keys.M || keycode == Keys.F) {
                sexBuffer.append(Keys.toString(keycode));
            }
            return false;
        }
    }

    class StoryInputAdapter extends InputAdapter {

        @Override
        public boolean keyUp(int keycode) {
            if (keycode == Keys.ENTER || keycode == Keys.SPACE) {
                storyInd++;
                if (storyInd == 24) {
                    state = State.ASK_QUESTIONS;
                    questionRound = 0;
                    initQuestionTree();
                }
            }
            return false;
        }
    }

    class QuestionInputAdapter extends InputAdapter {

        @Override
        public boolean keyUp(int keycode) {

            if (pauseFlag) {
                pauseFlag = false;
                currentQuestion = getQuestion(questionTree[questionRound * 2], questionTree[questionRound * 2 + 1]);
            } else {
                if (keycode == Keys.A || keycode == Keys.B) {
                    boolean ret = doQuestion(keycode == Keys.A ? 0 : 1);
                    if (ret) {
                        state = State.DONE;
                        storyInd = 12;
                    }
                    pauseFlag = true;
                }
            }
            return false;
        }
    }

    class DoneInputAdapter extends InputAdapter {

        @Override
        public boolean keyUp(int keycode) {
            if (keycode == Keys.ENTER || keycode == Keys.SPACE) {
                storyInd++;
                if (storyInd == 14) {

                    SaveGame sg = new SaveGame();

                    SaveGame.SaveGamePlayerRecord avatar = sg.new SaveGamePlayerRecord();
                    avatar.name = nameBuffer.toString();
                    avatar.sex = sexBuffer.toString().equals("M") ? SexType.MALE : SexType.FEMALE;
                    avatar.klass = ClassType.get(questionTree[14]);

                    avatar.weapon = avatar.klass.getInitialWeapon();
                    avatar.armor = avatar.klass.getInitialArmor();
                    avatar.xp = avatar.klass.getInitialExp();
                    sg.x = avatar.klass.getStartX();
                    sg.y = avatar.klass.getStartY();

                    avatar.adjuestAttribsPerKarma(questionTree);

                    avatar.hp = avatar.hpMax = avatar.getMaxLevel() * 100;
                    avatar.mp = avatar.getMaxMp();

                    sg.players[0] = avatar;

                    int p = 1;
                    for (int i = 0; i < 8; i++) {
                        /* Initial setup for party members that aren't in your group yet... */
                        if (i != avatar.klass.ordinal()) {
                            sg.players[p] = sg.new SaveGamePlayerRecord();
                            sg.players[p].klass = ClassType.get(i);
                            sg.players[p].xp = sg.players[p].klass.getInitialExp();
                            sg.players[p].str = NpcDefaults.get(i).getStr();
                            sg.players[p].dex = NpcDefaults.get(i).getDex();
                            sg.players[p].intel = NpcDefaults.get(i).getIntell();
                            sg.players[p].weapon = sg.players[p].klass.getInitialWeapon();
                            sg.players[p].armor = sg.players[p].klass.getInitialArmor();
                            sg.players[p].name = NpcDefaults.get(i).toString();
                            sg.players[p].sex = NpcDefaults.get(i).getSex();
                            sg.players[p].hp = sg.players[p].hpMax = sg.players[p].getMaxLevel() * 100;
                            sg.players[p].mp = sg.players[p].getMaxMp();
                            p++;
                        }
                    }

                    try {
                        sg.write(PARTY_SAV_BASE_FILENAME);
                        new File("journal.save").delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    mainGame.setScreen(new GameScreen(mainGame));
                }
            }
            return false;
        }
    }

    /**
     * Initializes the question tree. The tree starts off with the first eight
     * entries set to the numbers 0-7 in a random order.
     */
    public static void initQuestionTree() {

        for (int i = 0; i < 8; i++) {
            questionTree[i] = i;
        }

        //shuffle the first 8 virtue slots, the last 8 slots are for the answers
        Random rand = new XORShiftRandom();
        for (int i = 0; i < 8; i++) {
            int r = rand.nextInt(8);
            int tmp = questionTree[r];
            questionTree[r] = questionTree[i];
            questionTree[i] = tmp;
        }
        answerInd = 8;

        if (questionTree[0] > questionTree[1]) {
            int tmp = questionTree[0];
            questionTree[0] = questionTree[1];
            questionTree[1] = tmp;
        }

    }

    public static String getQuestion(int v1, int v2) {
        int i = 0;
        int d = 7;
        while (v1 > 0) {
            i += d;
            d--;
            v1--;
            v2--;
        }
        return Constants.karmaQuestions[i + v2 - 1];
    }

    /**
     * Updates the question tree with the given answer, and advances to the next
     * round.
     *
     * @return true if all questions have been answered, false otherwise
     */
    public static boolean doQuestion(int answer) {

        if (answer == 0) {
            questionTree[answerInd] = questionTree[questionRound * 2];
        } else {
            questionTree[answerInd] = questionTree[questionRound * 2 + 1];
        }

        setAbacusBeads(questionRound, questionTree[answerInd], questionTree[questionRound * 2 + ((answer) != 0 ? 0 : 1)]);

        answerInd++;
        questionRound++;

        if (answerInd > 14) {
            return true;
        }

//		if (questionTree[questionRound * 2] > questionTree[questionRound * 2 + 1]) {
//			int tmp = questionTree[questionRound * 2];
//			questionTree[questionRound * 2] = questionTree[questionRound * 2 + 1];
//			questionTree[questionRound * 2 + 1] = tmp;
//		}
        return false;
    }

    public static void setAbacusBeads(int row, int selectedVirtue, int rejectedVirtue) {

        if (ta != null) {
            Sprite wb = ta.createSprite("white-bead");
            wb.setBounds(450 + (selectedVirtue * 11), Ultima4.SCREEN_HEIGHT - 335 - (row * 29), 16, 24);
            beads.add(wb);

            Sprite bb = ta.createSprite("black-bead");
            bb.setBounds(450 + (rejectedVirtue * 11), Ultima4.SCREEN_HEIGHT - 335 - (row * 29), 16, 24);
            beads.add(bb);
        }

    }

    @Override
    public void show() {

    }

    @Override
    public void resize(int width, int height) {
        viewPort.update(width, height, false);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        stage.clear();
    }

    @Override
    public void dispose() {

    }

    @Override
    public boolean keyDown(int keycode) {
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
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float f, float f1) {
        return false;
    }

    private class IntroAnim implements Runnable {

        Identifier[] objMap = new Identifier[5 * 19];
        int moveind = 0;

        public IntroAnim() {
            for (int x = 0; x < 5 * 19; x++) {
                objMap[x] = new Identifier();
            }
        }

        @Override
        public void run() {

            if (moveind >= movesCommands.length) {
                moveind = 0;
                return;
            }

            int command = movesCommands[moveind] >> 4;
            int data = movesCommands[moveind] & 0xf;

            if (command >= 0 && command <= 4) {
                if (objMap[data].x != -1) {
                    int idx = intromap[objMap[data].x + objMap[data].y * 19] & 0xff;
                    drawCell(idx, objMap[data].x, objMap[data].y, 0);
                }
                objMap[data].x = movesCommands[moveind + 1] & 0x1f;
                objMap[data].y = command;
                objMap[data].idx = movesData[data] & 0xff;
                
                int frame = movesCommands[moveind + 1] >> 5;
                drawCell(objMap[data].idx, objMap[data].x, objMap[data].y, frame);
                moveind += 2;
            } else if (command == 7) {
                int idx = intromap[objMap[data].x + objMap[data].y * 19] & 0xff;
                drawCell(idx, objMap[data].x, objMap[data].y, 0);
                moveind++;
            } else {
                moveind++;
            }

        }

        private void drawCell(int idx, int x, int y, int frame) {
            Tile tile = Ultima4.baseTileSet.getTileByIndex(idx);

            TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
            Array<TextureAtlas.AtlasRegion> tileRegions = Ultima4.standardAtlas.findRegions(tile.getName());
            Array<StaticTiledMapTile> ar = new Array<>();
            for (TextureAtlas.AtlasRegion r : tileRegions) {
                ar.add(new StaticTiledMapTile(r));
            }

            TiledMapTile tmt = ar.first();
            if (tile.getIndex() == 128 || tile.getIndex() == 16 || tile.getIndex() == 64) {
                tmt = ar.get(frame);
            } else if (tileRegions.size > 1) {
                tmt = new AnimatedTiledMapTile(.7f, ar);
            }

            tmt.setId(y * 19 + x);
            cell.setTile(tmt);
            ((TiledMapTileLayer) splashMap.getLayers().get(0)).setCell(x, 5 - 1 - y, cell);
        }
    }

    private class Identifier {

        int idx = -1, x = -1, y = -1;
    }

    public static final int INTRO_MAP_OFFSET = 30339;
    public static final int INTRO_SCRIPT_TABLE_SIZE = 548;
    public static final int INTRO_SCRIPT_TABLE_OFFSET = 30434;
    public static final int INTRO_BASETILE_TABLE_SIZE = 15;
    public static final int INTRO_BASETILE_TABLE_OFFSET = 16584;
    public static final byte[] intromap = new byte[19 * 5];
    public static final byte[] movesCommands = new byte[INTRO_SCRIPT_TABLE_SIZE];
    public static final byte[] movesData = new byte[INTRO_BASETILE_TABLE_SIZE];

    static {
        try {
            InputStream is = new FileInputStream("assets/data/title.exe");
            byte[] tmp = IOUtils.toByteArray(is);
            System.arraycopy(tmp, INTRO_MAP_OFFSET, intromap, 0, 19 * 5);
            System.arraycopy(tmp, INTRO_SCRIPT_TABLE_OFFSET, movesCommands, 0, INTRO_SCRIPT_TABLE_SIZE);
            System.arraycopy(tmp, INTRO_BASETILE_TABLE_OFFSET, movesData, 0, INTRO_BASETILE_TABLE_SIZE);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
