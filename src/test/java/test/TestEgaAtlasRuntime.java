package test;

import java.util.List;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Align;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import util.AtlasAnimationGeneration.Group;
import static util.AtlasAnimationGeneration.forceNearest;
import static util.AtlasAnimationGeneration.installRuntimeAnimations;

public class TestEgaAtlasRuntime extends Game {


    private static final int WIDTH = 1280;
    private static final int HEIGHT = 800;

    private static final int GRID_COLS = 5;
    private static final float HEADER = 82f;
    private static final float CELL_H = 170f;
    private static final float PREVIEW = 72f;
    private static final float MINI = 18f;
    private static final float GAP = 2f;

    private TextureAtlas sourceAtlas;
    private TextureAtlas generatedAtlas;
    private Texture runtimeTexture;

    private List<Group> sourceGroups;
    private List<Group> generatedGroups;

    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera camera;

    private boolean generatedMode = true;
    private boolean paused = false;

    private float time = 0f;
    private float frameDuration = 0.20f;
    private float scroll = 0f;

    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();

        cfg.title = "Ultima IV EGA Runtime Animation Test";
        cfg.width = WIDTH;
        cfg.height = HEIGHT;
        cfg.resizable = true;

        new LwjglApplication(new TestEgaAtlasRuntime(), cfg);
    }

    @Override
    public void create() {
        FileHandle atlasFile = Gdx.files.classpath("assets/tilemaps/latest-ega-atlas.txt");
        FileHandle pngFile = atlasFile.parent().child("tiles-ega-32px.png");

        sourceAtlas = new TextureAtlas(atlasFile);
        generatedAtlas = new TextureAtlas(atlasFile);

        forceNearest(sourceAtlas);
        forceNearest(generatedAtlas);

        runtimeTexture = installRuntimeAnimations(generatedAtlas, pngFile);

        sourceGroups = buildGroups(sourceAtlas);
        generatedGroups = buildGroups(generatedAtlas);

        batch = new SpriteBatch();
        font = new BitmapFont();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        installInput();

    }

    private void installInput() {
        Gdx.input.setInputProcessor(new InputAdapter() {

            @Override
            public boolean keyDown(int keycode) {

                if (keycode == Input.Keys.T) {
                    generatedMode = !generatedMode;
                    System.out.println(generatedMode ? "VIEW = GENERATED" : "VIEW = SOURCE");
                    return true;
                }

                if (keycode == Input.Keys.SPACE) {
                    paused = !paused;
                    return true;
                }

                if (keycode == Input.Keys.R) {
                    time = 0f;
                    return true;
                }

                if (keycode == Input.Keys.UP) {
                    scrollBy(-82f);
                    return true;
                }

                if (keycode == Input.Keys.DOWN) {
                    scrollBy(82f);
                    return true;
                }

                if (keycode == Input.Keys.PAGE_UP) {
                    scrollBy(-600f);
                    return true;
                }

                if (keycode == Input.Keys.PAGE_DOWN) {
                    scrollBy(600f);
                    return true;
                }

                if (keycode == Input.Keys.HOME) {
                    scroll = 0f;
                    return true;
                }

                if (keycode == Input.Keys.END) {
                    scroll = maxScroll();
                    return true;
                }

                if (keycode == Input.Keys.EQUALS) {
                    frameDuration = Math.max(0.04f, frameDuration - 0.03f);
                    return true;
                }

                if (keycode == Input.Keys.MINUS) {
                    frameDuration = Math.min(1.0f, frameDuration + 0.03f);
                    return true;
                }

                if (keycode == Input.Keys.ESCAPE) {
                    Gdx.app.exit();
                    return true;
                }

                return false;
            }

            @Override
            public boolean scrolled(float amountX, float amountY) {
                scrollBy(amountY * 82f);
                return true;
            }
        });
    }

    private List<Group> buildGroups(TextureAtlas atlas) {
        Map<String, List<TextureAtlas.AtlasRegion>> map = new LinkedHashMap<>();

        for (TextureAtlas.AtlasRegion r : atlas.getRegions()) {
            List<TextureAtlas.AtlasRegion> list = map.get(r.name);

            if (list == null) {
                list = new ArrayList<>();
                map.put(r.name, list);
            }

            list.add(r);
        }

        List<Group> result = new ArrayList<>();

        for (Map.Entry<String, List<TextureAtlas.AtlasRegion>> e : map.entrySet()) {
            Collections.sort(e.getValue(), new Comparator<TextureAtlas.AtlasRegion>() {
                @Override
                public int compare(TextureAtlas.AtlasRegion a, TextureAtlas.AtlasRegion b) {
                    if (a.index < b.index) {
                        return -1;
                    }
                    if (a.index > b.index) {
                        return 1;
                    }
                    return 0;
                }
            });

            result.add(new Group(e.getKey(), e.getValue()));
        }

        return result;
    }

    @Override
    public void render() {
        if (!paused) {
            time += Gdx.graphics.getDeltaTime();
        }

        clampScroll();

        Gdx.gl.glClearColor(0.035f, 0.035f, 0.035f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        float cellW = w / GRID_COLS;

        List<Group> groups = generatedMode ? generatedGroups : sourceGroups;

        TextureAtlas atlas = generatedMode ? generatedAtlas : sourceAtlas;

        batch.begin();

        font.draw(batch, generatedMode ? "MODE: GENERATED RUNTIME ANIMATIONS" : "MODE: SOURCE / STATIC EGA", 12f, h - 10f);

        float gridTop = h - HEADER;

        for (int i = 0; i < groups.size(); i++) {
            int col = i % GRID_COLS;
            int row = i / GRID_COLS;

            float x = col * cellW;
            float top = gridTop - row * CELL_H + scroll;

            float bottom = top - CELL_H;

            if (top < 0 || bottom > gridTop) {
                continue;
            }

            drawGroup(groups.get(i), x, bottom, cellW, CELL_H);
        }

        batch.end();
    }

    private void drawGroup(Group group, float cellX, float cellBottom, float cellWidth, float cellHeight) {
        int count = group.frames.size();

        int frame = count <= 1 ? 0 : ((int) (time / frameDuration)) % count;

        AtlasRegion current = group.frames.get(frame);

        float left = cellX + 10f;
        float labelY = cellBottom + cellHeight - 8f;

        font.draw(batch, group.name, left, labelY, cellWidth - 20f, Align.left, false);
        font.draw(batch, "frames=" + count + " index=" + current.index + " xy=" + current.getRegionX() + "," + current.getRegionY(), left, labelY - 19f);
        batch.draw(current, left, cellBottom + 38f, PREVIEW, PREVIEW);

        float stripX = left + PREVIEW + 10f;
        float stripY = cellBottom + 40f;

        int perLine = Math.max(1, (int) ((cellWidth - PREVIEW - 28f) / (MINI + GAP)));

        for (int i = 0; i < count; i++) {
            int line = i / perLine;
            int pos = i % perLine;

            batch.draw(group.frames.get(i), stripX + pos * (MINI + GAP), stripY - line * (MINI + GAP), MINI, MINI);
        }
    }

    private void scrollBy(float amount) {
        scroll += amount;
        clampScroll();
    }

    private void clampScroll() {
        if (scroll < 0) {
            scroll = 0;
        }

        float max = maxScroll();

        if (scroll > max) {
            scroll = max;
        }
    }

    private float maxScroll() {
        List<Group> groups = generatedMode ? generatedGroups : sourceGroups;

        int rows = (groups.size() + GRID_COLS - 1) / GRID_COLS;

        float content = rows * CELL_H;
        float visible = Gdx.graphics.getHeight() - HEADER - 18f;

        return Math.max(0f, content - visible);
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
        clampScroll();
    }

    @Override
    public void dispose() {
        if (batch != null) {
            batch.dispose();
        }

        if (font != null) {
            font.dispose();
        }

        if (sourceAtlas != null) {
            sourceAtlas.dispose();
        }

        if (generatedAtlas != null) {
            generatedAtlas.dispose();
        }

        if (runtimeTexture != null) {
            runtimeTexture.dispose();
        }
    }

}
