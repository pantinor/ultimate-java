package test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Align;

/**
 * Visual test harness for latest-ega-atlas.txt + tiles-ega-32px.png.
 *
 * It groups atlas regions by name. If a name has multiple regions, the large
 * preview animates through them. All source frames are also shown as a small
 * strip underneath so duplicate/static frames are easy to spot.
 *
 * Expected layout:
 *
 * assets/latest-ega-atlas.txt assets/tiles-ega-32px.png
 *
 * The atlas file itself refers to tiles-ega-32px.png, so keep the PNG beside
 * the atlas text file.
 */
public class TestEgaAtlas extends Game {

    private static final String[] ATLAS_CANDIDATES = {
        "assets/tilemaps/latest-ega-atlas.txt",
        "latest-ega-atlas.txt"
    };

    private static final int WINDOW_WIDTH = 1280;
    private static final int WINDOW_HEIGHT = 800;

    private static final int COLS = 5;
    private static final int ROWS = 4;
    private static final int ITEMS_PER_PAGE = COLS * ROWS;

    private static final float PREVIEW_SIZE = 72f;
    private static final float MINI_SIZE = 18f;
    private static final float MINI_GAP = 2f;

    private TextureAtlas atlas;
    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera camera;

    private final List<RegionGroup> groups = new ArrayList<RegionGroup>();

    private int page = 0;
    private boolean paused = false;
    private float stateTime = 0f;
    private float frameDuration = 0.22f;

    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "Ultima IV EGA Atlas Viewer";
        cfg.width = WINDOW_WIDTH;
        cfg.height = WINDOW_HEIGHT;
        cfg.resizable = true;
        new LwjglApplication(new TestEgaAtlas(), cfg);
    }

    @Override
    public void create() {
        FileHandle atlasFile = findAtlasFile();

        atlas = new TextureAtlas(atlasFile);

        // Force crisp EGA pixels even if texture defaults/settings change.
        for (com.badlogic.gdx.graphics.Texture texture : atlas.getTextures()) {
            texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        }

        batch = new SpriteBatch();
        font = new BitmapFont();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        buildGroups();

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.RIGHT || keycode == Input.Keys.PAGE_DOWN) {
                    page = Math.min(page + 1, getPageCount() - 1);
                    return true;
                }
                if (keycode == Input.Keys.LEFT || keycode == Input.Keys.PAGE_UP) {
                    page = Math.max(page - 1, 0);
                    return true;
                }
                if (keycode == Input.Keys.HOME) {
                    page = 0;
                    return true;
                }
                if (keycode == Input.Keys.END) {
                    page = Math.max(0, getPageCount() - 1);
                    return true;
                }
                if (keycode == Input.Keys.SPACE) {
                    paused = !paused;
                    return true;
                }
                if (keycode == Input.Keys.R) {
                    stateTime = 0f;
                    return true;
                }
                if (keycode == Input.Keys.UP || keycode == Input.Keys.EQUALS) {
                    frameDuration = Math.max(0.04f, frameDuration - 0.03f);
                    return true;
                }
                if (keycode == Input.Keys.DOWN || keycode == Input.Keys.MINUS) {
                    frameDuration = Math.min(1.0f, frameDuration + 0.03f);
                    return true;
                }
                if (keycode == Input.Keys.ESCAPE) {
                    Gdx.app.exit();
                    return true;
                }
                return false;
            }
        });

        System.out.println("Loaded atlas: " + atlasFile.path());
        System.out.println("Logical region names: " + groups.size());
        System.out.println("Atlas records: " + atlas.getRegions().size);
        System.out.println("Pages: " + getPageCount());
    }

    private FileHandle findAtlasFile() {
        for (String path : ATLAS_CANDIDATES) {
            FileHandle fh = Gdx.files.internal(path);
            if (fh.exists()) {
                return fh;
            }
        }

        throw new IllegalStateException(
                "Could not find latest-ega-atlas.txt. Tried: "
                + ATLAS_CANDIDATES[0] + " and " + ATLAS_CANDIDATES[1]
        );
    }

    /**
     * TextureAtlas.getRegions() contains one AtlasRegion per atlas record.
     * Group them by region name while preserving first-seen atlas order.
     */
    private void buildGroups() {
        Map<String, List<AtlasRegion>> byName
                = new LinkedHashMap<String, List<AtlasRegion>>();

        for (AtlasRegion region : atlas.getRegions()) {
            List<AtlasRegion> frames = byName.get(region.name);
            if (frames == null) {
                frames = new ArrayList<AtlasRegion>();
                byName.put(region.name, frames);
            }
            frames.add(region);
        }

        for (Map.Entry<String, List<AtlasRegion>> e : byName.entrySet()) {
            List<AtlasRegion> frames = e.getValue();

            Collections.sort(frames, new Comparator<AtlasRegion>() {
                @Override
                public int compare(AtlasRegion a, AtlasRegion b) {
                    return Integer.compare(a.index, b.index);
                }
            });

            groups.add(new RegionGroup(e.getKey(), frames));
        }
    }

    @Override
    public void render() {
        if (!paused) {
            stateTime += Gdx.graphics.getDeltaTime();
        }

        Gdx.gl.glClearColor(0.04f, 0.04f, 0.04f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();

        float topArea = 74f;
        float bottomArea = 36f;
        float cellWidth = width / COLS;
        float cellHeight = (height - topArea - bottomArea) / ROWS;

        int first = page * ITEMS_PER_PAGE;
        int last = Math.min(first + ITEMS_PER_PAGE, groups.size());

        batch.begin();

        font.getData().setScale(1.0f);
        font.draw(
                batch,
                "EGA atlas viewer   page " + (page + 1) + "/" + getPageCount()
                + "   names=" + groups.size()
                + "   records=" + atlas.getRegions().size
                + "   frame=" + String.format("%.2fs", frameDuration)
                + (paused ? "   [PAUSED]" : ""),
                12f,
                height - 12f
        );

        font.getData().setScale(0.8f);
        font.draw(
                batch,
                "Left/Right or PgUp/PgDn: page   Space: pause   Up/Down or +/-: speed   R: reset   Esc: quit",
                12f,
                height - 38f
        );

        for (int i = first; i < last; i++) {
            int local = i - first;
            int col = local % COLS;
            int row = local / COLS;

            float cellX = col * cellWidth;
            float cellBottom = height - topArea - (row + 1) * cellHeight;
            drawGroup(groups.get(i), cellX, cellBottom, cellWidth, cellHeight);
        }

        batch.end();
    }

    private void drawGroup(
            RegionGroup group,
            float cellX,
            float cellBottom,
            float cellWidth,
            float cellHeight
    ) {
        float left = cellX + 10f;
        float labelY = cellBottom + cellHeight - 8f;

        int frameCount = group.frames.size();
        int frameIndex = getAnimationFrameIndex(frameCount);

        AtlasRegion current = group.frames.get(frameIndex);

        font.getData().setScale(0.82f);
        font.draw(
                batch,
                group.name,
                left,
                labelY,
                cellWidth - 20f,
                Align.left,
                false
        );

        font.getData().setScale(0.65f);
        font.draw(
                batch,
                "frames=" + frameCount
                + "  index=" + current.index
                + "  xy=" + current.getRegionX() + "," + current.getRegionY(),
                left,
                labelY - 19f
        );

        // Animated/current-frame preview.
        float previewX = left;
        float previewY = cellBottom + 38f;
        batch.draw(current, previewX, previewY, PREVIEW_SIZE, PREVIEW_SIZE);

        // Small strip showing every atlas frame for this logical name.
        float stripX = left + PREVIEW_SIZE + 10f;
        float stripY = cellBottom + 40f;

        for (int i = 0; i < frameCount; i++) {
            TextureRegion frame = group.frames.get(i);

            // Wrap the frame strip if a future atlas has unusually many frames.
            float x = stripX + i * (MINI_SIZE + MINI_GAP);
            float y = stripY;
            float maxX = cellX + cellWidth - MINI_SIZE - 8f;

            if (x > maxX) {
                int perLine = Math.max(
                        1,
                        (int) ((cellWidth - PREVIEW_SIZE - 28f) / (MINI_SIZE + MINI_GAP))
                );
                int line = i / perLine;
                int pos = i % perLine;
                x = stripX + pos * (MINI_SIZE + MINI_GAP);
                y = stripY - line * (MINI_SIZE + MINI_GAP);
            }

            batch.draw(frame, x, y, MINI_SIZE, MINI_SIZE);
        }
    }

    private int getAnimationFrameIndex(int frameCount) {
        if (frameCount <= 1) {
            return 0;
        }
        return ((int) (stateTime / frameDuration)) % frameCount;
    }

    private int getPageCount() {
        return Math.max(1, (groups.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE);
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }

    @Override
    public void dispose() {
        if (batch != null) {
            batch.dispose();
        }
        if (font != null) {
            font.dispose();
        }
        if (atlas != null) {
            atlas.dispose();
        }
    }

    private static class RegionGroup {

        final String name;
        final List<AtlasRegion> frames;

        RegionGroup(String name, List<AtlasRegion> frames) {
            this.name = name;
            this.frames = frames;
        }
    }
}
