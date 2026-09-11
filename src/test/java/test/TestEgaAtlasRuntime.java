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
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Align;

/**
 * Runtime animation test for latest-ega-atlas.txt + tiles-ega-32px.png.
 *
 * IMPORTANT: - The PNG is NEVER modified or written. - Generated frames live in
 * a temporary 256x256 Texture created at runtime. - Existing AtlasRegion
 * objects are repointed to the runtime texture. -
 * TextureAtlas.findRegions(name) continues to work normally.
 *
 * Press T to toggle: GENERATED = runtime-generated animation frames SOURCE =
 * untouched static atlas
 */
public class TestEgaAtlasRuntime extends Game {

    private static final String[] ATLAS_CANDIDATES = {
        "assets/tilemaps/latest-ega-atlas.txt",
        "latest-ega-atlas.txt"
    };

    private static final String PNG_NAME = "tiles-ega-32px.png";

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 800;

    private static final int TILE = 32;
    private static final int RUNTIME_COLS = 8;
    private static final int RUNTIME_ROWS = 8;
    private static final int RUNTIME_SIZE = TILE * RUNTIME_COLS; // 256

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

    private int generatedFrames = 0;

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
        FileHandle atlasFile = findAtlas();
        FileHandle pngFile = findPng(atlasFile);

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
        camera.setToOrtho(
                false,
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight()
        );

        installInput();

        System.out.println("==========================================");
        System.out.println("EGA RUNTIME ANIMATION VIEWER IS ACTIVE");
        System.out.println("Atlas: " + atlasFile.path());
        System.out.println("PNG:   " + pngFile.path());
        System.out.println("Generated runtime frames: " + generatedFrames);
        System.out.println("Runtime texture: 256x256");
        System.out.println("PNG IS READ-ONLY; NOTHING IS WRITTEN.");
        System.out.println("Press T to toggle GENERATED / SOURCE.");
        System.out.println("==========================================");
    }

    private FileHandle findAtlas() {
        for (int i = 0; i < ATLAS_CANDIDATES.length; i++) {
            FileHandle f = Gdx.files.internal(ATLAS_CANDIDATES[i]);
            if (f.exists()) {
                return f;
            }
        }

        throw new IllegalStateException("Could not find latest-ega-atlas.txt");
    }

    private FileHandle findPng(FileHandle atlasFile) {
        FileHandle beside = atlasFile.parent().child(PNG_NAME);
        if (beside.exists()) {
            return beside;
        }

        FileHandle assets = Gdx.files.internal("assets/" + PNG_NAME);

        if (assets.exists()) {
            return assets;
        }

        FileHandle root
                = Gdx.files.internal(PNG_NAME);

        if (root.exists()) {
            return root;
        }

        throw new IllegalStateException(
                "Could not find " + PNG_NAME
        );
    }

    private void forceNearest(TextureAtlas atlas) {
        for (Texture t : atlas.getTextures()) {
            t.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        }
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

            // Old LibGDX.
            public boolean scrolled(int amount) {
                scrollBy(amount * 82f);
                return true;
            }

            // Newer LibGDX.
            public boolean scrolled(float amountX, float amountY) {
                scrollBy(amountY * 82f);
                return true;
            }
        });
    }

    /**
     * This is the routine intended to migrate later into Ultima4.java.
     *
     * It creates a temporary 256x256 runtime texture and repoints selected
     * AtlasRegion objects into it. The original PNG stays untouched.
     */
    private Texture installRuntimeAnimations(TextureAtlas atlas, FileHandle pngFile) {
        Pixmap source = new Pixmap(pngFile);

        Pixmap runtime = new Pixmap(
                RUNTIME_SIZE,
                RUNTIME_SIZE,
                Pixmap.Format.RGBA8888
        );

        runtime.setColor(0, 0, 0, 0);
        runtime.fill();

        List<Binding> bindings = new ArrayList<Binding>();

        int slot = 0;

        // 4-frame scrolling textures: 16 frames total.
        String[] scrolling = {
            "sea", "water", "shallows", "lava"
        };

        for (int n = 0; n < scrolling.length; n++) {
            String name = scrolling[n];

            List<AtlasRegion> frames = regions(atlas, name);

            requireCount(name, frames, 4);

            Rect src = Rect.from(frames.get(0));

            for (int frame = 0; frame < 4; frame++) {
                Point p = slot(slot++);

                int shiftX = frame * 6;
                int shiftY = name.equals("lava") ? frame * 2 : 0;

                drawWrapped(
                        source,
                        src,
                        runtime,
                        p.x,
                        p.y,
                        shiftX,
                        shiftY
                );

                bindings.add(new Binding(frames.get(frame), p.x, p.y));
            }
        }

        // 4 field effects x 8 frames = 32 frames.
        String[] fields = {
            "poison_field",
            "energy_field",
            "fire_field",
            "sleep_field"
        };

        for (int n = 0; n < fields.length; n++) {
            String name = fields[n];

            List<AtlasRegion> frames = regions(atlas, name);

            requireCount(name, frames, 8);

            Rect src = Rect.from(frames.get(0));

            for (int frame = 0; frame < 8; frame++) {
                Point p = slot(slot++);

                drawField(
                        source,
                        src,
                        runtime,
                        p.x,
                        p.y,
                        frame
                );

                bindings.add(new Binding(frames.get(frame), p.x, p.y));
            }
        }

        // Campfire: 8 frames.
        {
            List<AtlasRegion> frames = regions(atlas, "campfire");

            requireCount("campfire", frames, 8);

            Rect src = Rect.from(frames.get(0));

            for (int frame = 0; frame < 8; frame++) {
                Point p = slot(slot++);

                drawCampfire(
                        source,
                        src,
                        runtime,
                        p.x,
                        p.y,
                        frame
                );

                bindings.add(new Binding(frames.get(frame), p.x, p.y));
            }
        }

        // Whirlpool and twister: 2 groups x 4 = 8 frames.
        String[] nativeTwo = {
            "whirlpool", "twister"
        };

        for (int n = 0; n < nativeTwo.length; n++) {
            String name = nativeTwo[n];

            List<AtlasRegion> frames = regions(atlas, name);

            requireCount(name, frames, 4);

            Rect a = Rect.from(frames.get(0));
            Rect b = Rect.from(frames.get(1));

            for (int frame = 0; frame < 4; frame++) {
                Point p = slot(slot++);

                drawFromTwo(
                        source,
                        a,
                        b,
                        runtime,
                        p.x,
                        p.y,
                        frame
                );

                bindings.add(new Binding(frames.get(frame), p.x, p.y));
            }
        }

        if (slot != 64) {
            source.dispose();
            runtime.dispose();

            throw new IllegalStateException(
                    "Expected 64 generated frames but created " + slot
            );
        }

        Texture result = new Texture(runtime);

        result.setFilter(
                TextureFilter.Nearest,
                TextureFilter.Nearest
        );

        runtime.dispose();
        source.dispose();

        // Repoint existing AtlasRegions; do NOT add/remove atlas records.
        for (int i = 0; i < bindings.size(); i++) {
            Binding b = bindings.get(i);
            AtlasRegion r = b.region;

            TextureRegion runtimeRegion
                    = new TextureRegion(
                            result,
                            b.x,
                            b.y,
                            TILE,
                            TILE
                    );

            r.setRegion(runtimeRegion);

            r.packedWidth = TILE;
            r.packedHeight = TILE;
            r.originalWidth = TILE;
            r.originalHeight = TILE;
            r.offsetX = 0;
            r.offsetY = 0;
        }

        generatedFrames = bindings.size();

        return result;
    }

    private void drawWrapped(
            Pixmap source,
            Rect src,
            Pixmap dest,
            int dx,
            int dy,
            int shiftX,
            int shiftY
    ) {
        for (int y = 0; y < TILE; y++) {
            for (int x = 0; x < TILE; x++) {
                int sx = src.x + wrap(x + shiftX);
                int sy = src.y + wrap(y + shiftY);

                dest.drawPixel(
                        dx + x,
                        dy + y,
                        source.getPixel(sx, sy)
                );
            }
        }
    }

    private void drawField(
            Pixmap source,
            Rect src,
            Pixmap dest,
            int dx,
            int dy,
            int frame
    ) {
        for (int y = 0; y < TILE; y++) {
            for (int x = 0; x < TILE; x++) {

                int bandY = y / 4;
                int bandX = x / 4;

                int sx
                        = src.x
                        + wrap(
                                x
                                + frame * 2
                                + (bandY % 4) * 2
                        );

                int sy
                        = src.y
                        + wrap(
                                y
                                + frame
                                + ((bandX & 1) == 0 ? 0 : 2)
                        );

                dest.drawPixel(
                        dx + x,
                        dy + y,
                        source.getPixel(sx, sy)
                );
            }
        }
    }

    private void drawCampfire(
            Pixmap source,
            Rect src,
            Pixmap dest,
            int dx,
            int dy,
            int frame
    ) {
        // Bottom quarter remains completely stable.
        final int stableY = 24;

        for (int y = 0; y < TILE; y++) {
            for (int x = 0; x < TILE; x++) {

                int sx = x;
                int sy = y;

                if (y < stableY) {
                    int wiggle = ((frame + y / 3) % 5) - 2;
                    int lift = ((frame * 2 + x / 6) % 3) - 1;
                    sx = wrap(x + wiggle);
                    sy = clamp(y + lift, 0, stableY - 1);
                }

                dest.drawPixel(
                        dx + x,
                        dy + y,
                        source.getPixel(src.x + sx, src.y + sy)
                );
            }
        }
    }

    private void drawFromTwo(
            Pixmap source,
            Rect a,
            Rect b,
            Pixmap dest,
            int dx,
            int dy,
            int frame
    ) {
        for (int y = 0; y < TILE; y++) {
            for (int x = 0; x < TILE; x++) {

                Rect chosen;

                if (frame == 0) {
                    chosen = a;
                } else if (frame == 2) {
                    chosen = b;
                } else {
                    boolean useB
                            = (((x / 2) + (y / 2) + frame) & 1) == 0;

                    chosen = useB ? b : a;
                }

                dest.drawPixel(
                        dx + x,
                        dy + y,
                        source.getPixel(chosen.x + x, chosen.y + y)
                );
            }
        }
    }

    private List<AtlasRegion> regions(TextureAtlas atlas, String name) {
        List<AtlasRegion> list = new ArrayList<AtlasRegion>();

        for (AtlasRegion r : atlas.getRegions()) {
            if (name.equals(r.name)) {
                list.add(r);
            }
        }

        Collections.sort(list, new Comparator<AtlasRegion>() {
            @Override
            public int compare(AtlasRegion a, AtlasRegion b) {
                if (a.index < b.index) {
                    return -1;
                }
                if (a.index > b.index) {
                    return 1;
                }
                return 0;
            }
        }
        );

        return list;
    }

    private void requireCount(String name, List<AtlasRegion> frames, int count) {
        if (frames.size() != count) {
            throw new IllegalStateException(
                    name
                    + " expected "
                    + count
                    + " atlas frames but found "
                    + frames.size()
            );
        }
    }

    private Point slot(int slot) {
        return new Point((slot % RUNTIME_COLS) * TILE, (slot / RUNTIME_COLS) * TILE);
    }

    private int wrap(int v) {
        int r = v % TILE;
        return r < 0 ? r + TILE : r;
    }

    private int clamp(int v, int min, int max) {
        return v < min ? min : (v > max ? max : v);
    }

    private List<Group> buildGroups(TextureAtlas atlas) {
        Map<String, List<AtlasRegion>> map = new LinkedHashMap<String, List<AtlasRegion>>();

        for (AtlasRegion r : atlas.getRegions()) {
            List<AtlasRegion> list = map.get(r.name);

            if (list == null) {
                list = new ArrayList<AtlasRegion>();
                map.put(r.name, list);
            }

            list.add(r);
        }

        List<Group> result = new ArrayList<Group>();

        for (Map.Entry<String, List<AtlasRegion>> e : map.entrySet()) {
            Collections.sort(e.getValue(), new Comparator<AtlasRegion>() {
                @Override
                public int compare(AtlasRegion a, AtlasRegion b) {
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

        font.getData().setScale(1.0f);

        font.draw(batch, generatedMode ? "MODE: GENERATED RUNTIME ANIMATIONS" : "MODE: SOURCE / STATIC EGA", 12f, h - 10f);

        font.getData().setScale(0.75f);

        font.draw(
                batch,
                "T toggle source/generated   wheel or Up/Down scroll   Space pause   +/- speed   R reset   generated="
                + generatedFrames
                + "   records="
                + atlas.getRegions().size,
                12f,
                h - 36f
        );

        font.draw(
                batch,
                "Animated runtime regions are marked [RUNTIME]. Static regions are marked [EGA]. PNG is never altered.",
                12f,
                h - 57f
        );

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

            drawGroup(
                    groups.get(i),
                    x,
                    bottom,
                    cellW,
                    CELL_H
            );
        }

        batch.end();
    }

    private void drawGroup(Group group, float cellX, float cellBottom, float cellWidth, float cellHeight) {
        int count = group.frames.size();

        int frame = count <= 1 ? 0 : ((int) (time / frameDuration)) % count;

        AtlasRegion current
                = group.frames.get(frame);

        float left = cellX + 10f;
        float labelY = cellBottom + cellHeight - 8f;

        boolean runtime = current.getTexture() == runtimeTexture;

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

        font.getData().setScale(0.63f);

        font.draw(
                batch,
                "frames="
                + count
                + " index="
                + current.index
                + " xy="
                + current.getRegionX()
                + ","
                + current.getRegionY()
                + (runtime ? " [RUNTIME]" : " [EGA]"),
                left,
                labelY - 19f
        );

        batch.draw(
                current,
                left,
                cellBottom + 38f,
                PREVIEW,
                PREVIEW
        );

        float stripX = left + PREVIEW + 10f;
        float stripY = cellBottom + 40f;

        int perLine = Math.max(1, (int) ((cellWidth - PREVIEW - 28f) / (MINI + GAP)));

        for (int i = 0; i < count; i++) {
            int line = i / perLine;
            int pos = i % perLine;

            batch.draw(
                    group.frames.get(i),
                    stripX + pos * (MINI + GAP),
                    stripY - line * (MINI + GAP),
                    MINI,
                    MINI
            );
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

    private static class Rect {

        final int x;
        final int y;

        Rect(int x, int y) {
            this.x = x;
            this.y = y;
        }

        static Rect from(AtlasRegion r) {
            return new Rect(
                    r.getRegionX(),
                    r.getRegionY()
            );
        }
    }

    private static class Point {

        final int x;
        final int y;

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private static class Binding {

        final AtlasRegion region;
        final int x;
        final int y;

        Binding(AtlasRegion region, int x, int y) {
            this.region = region;
            this.x = x;
            this.y = y;
        }
    }

    private static class Group {

        final String name;
        final List<AtlasRegion> frames;

        Group(String name, List<AtlasRegion> frames) {
            this.name = name;
            this.frames = frames;
        }
    }
}
