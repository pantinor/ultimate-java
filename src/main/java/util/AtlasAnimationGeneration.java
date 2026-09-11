package util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AtlasAnimationGeneration {

    private static final String[] SCROLLING = {"sea", "water", "shallows"};
    private static final String[] FIELDS = {"lava", "poison_field", "energy_field", "fire_field", "sleep_field"};
    private static final int SCROLLING_FRAMES = 16;
    private static final int FIELD_FRAMES = 8;
    private static final int CAMPFIRE_FRAMES = 4;
    private static final int RUNTIME_FRAME_COUNT = SCROLLING.length * SCROLLING_FRAMES + FIELDS.length * FIELD_FRAMES + CAMPFIRE_FRAMES;
    private static final int TILE = 32;
    private static final int RUNTIME_COLS = 8;
    private static final int RUNTIME_WIDTH = TILE * RUNTIME_COLS;
    private static final int RUNTIME_HEIGHT = TILE * ((RUNTIME_FRAME_COUNT + RUNTIME_COLS - 1) / RUNTIME_COLS);

    public static Texture installRuntimeAnimations(TextureAtlas atlas, FileHandle pngFile) {
        Pixmap source = new Pixmap(pngFile);
        Pixmap runtime = new Pixmap(RUNTIME_WIDTH, RUNTIME_HEIGHT, Pixmap.Format.RGBA8888);

        runtime.setColor(0, 0, 0, 0);
        runtime.fill();

        List<Binding> bindings = new ArrayList<>();

        int slot = 0;

        for (int n = 0; n < SCROLLING.length; n++) {
            String name = SCROLLING[n];
            List<TextureAtlas.AtlasRegion> frames = regions(atlas, name);
            Rect src = Rect.from(frames.get(0));
            for (int frame = 0; frame < SCROLLING_FRAMES; frame++) {
                Point p = slot(slot++);
                int shiftX = 0;
                int shiftY = frame * 2;
                drawWrapped(source, src, runtime, p.x, p.y, shiftX, shiftY);
                bindings.add(new Binding(frames.get(frame), p.x, p.y));
            }
        }

        for (int n = 0; n < FIELDS.length; n++) {
            String name = FIELDS[n];
            List<TextureAtlas.AtlasRegion> frames = regions(atlas, name);
            Rect src = Rect.from(frames.get(0));
            for (int frame = 0; frame < FIELD_FRAMES; frame++) {
                Point p = slot(slot++);
                int shiftX = 0;
                int shiftY = frame * -2;
                drawWrapped(source, src, runtime, p.x, p.y, shiftX, shiftY);
                bindings.add(new Binding(frames.get(frame), p.x, p.y));
            }
        }

        FileHandle campfirePng = Gdx.files.classpath("assets/tilemaps/campfire-anim-ega.png");
        Pixmap campfire = new Pixmap(campfirePng);
        try {
            List<TextureAtlas.AtlasRegion> frames = regions(atlas, "campfire");
            for (int frame = 0; frame < CAMPFIRE_FRAMES; frame++) {
                Point p = slot(slot++);
                runtime.drawPixmap(campfire, p.x, p.y, frame * TILE, 0, TILE, TILE);
                bindings.add(new Binding(frames.get(frame), p.x, p.y));
            }
        } finally {
            campfire.dispose();
        }

        Texture result = new Texture(runtime);
        result.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        runtime.dispose();
        source.dispose();

        for (int i = 0; i < bindings.size(); i++) {
            Binding b = bindings.get(i);
            TextureAtlas.AtlasRegion r = b.region;
            TextureRegion runtimeRegion = new TextureRegion(result, b.x, b.y, TILE, TILE);
            r.setRegion(runtimeRegion);
            r.packedWidth = TILE;
            r.packedHeight = TILE;
            r.originalWidth = TILE;
            r.originalHeight = TILE;
            r.offsetX = 0;
            r.offsetY = 0;
        }

        return result;
    }

    private static void drawWrapped(Pixmap source, Rect src, Pixmap dest, int dx, int dy, int shiftX, int shiftY) {
        for (int y = 0; y < TILE; y++) {
            for (int x = 0; x < TILE; x++) {
                int sx = src.x + wrap(x + shiftX);
                int sy = src.y + wrap(y + shiftY);
                dest.drawPixel(dx + x, dy + y, source.getPixel(sx, sy));
            }
        }
    }

    private static List<TextureAtlas.AtlasRegion> regions(TextureAtlas atlas, String name) {
        List<TextureAtlas.AtlasRegion> list = new ArrayList<>();
        for (TextureAtlas.AtlasRegion r : atlas.getRegions()) {
            if (name.equals(r.name)) {
                list.add(r);
            }
        }
        Collections.sort(list, new Comparator<>() {
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
        return list;
    }

    private static Point slot(int slot) {
        return new Point((slot % RUNTIME_COLS) * TILE, (slot / RUNTIME_COLS) * TILE);
    }

    private static int wrap(int v) {
        int r = v % TILE;
        return r < 0 ? r + TILE : r;
    }

    private static class Rect {

        final int x;
        final int y;

        Rect(int x, int y) {
            this.x = x;
            this.y = y;
        }

        static Rect from(TextureAtlas.AtlasRegion r) {
            return new Rect(r.getRegionX(), r.getRegionY());
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

        final TextureAtlas.AtlasRegion region;
        final int x;
        final int y;

        Binding(TextureAtlas.AtlasRegion region, int x, int y) {
            this.region = region;
            this.x = x;
            this.y = y;
        }
    }

    public static class Group {

        public final String name;
        public final List<TextureAtlas.AtlasRegion> frames;

        public Group(String name, List<TextureAtlas.AtlasRegion> frames) {
            this.name = name;
            this.frames = frames;
        }
    }

    public static void forceNearest(TextureAtlas atlas) {
        for (Texture t : atlas.getTextures()) {
            t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        }
    }

}
