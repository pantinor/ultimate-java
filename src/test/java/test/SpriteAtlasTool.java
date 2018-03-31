package test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import objects.Tile;
import objects.TileSet;

import org.apache.commons.io.FileUtils;

import test.AtlasWriter.Rect;
import util.Utils;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings;
import java.util.Collections;

public class SpriteAtlasTool extends InputAdapter implements ApplicationListener {

    Batch batch;

    static int screenWidth = 1920;
    static int screenHeight = 768;

    int dim = 48;
    int canvasGridWidth;
    int canvasGridHeight;

    boolean initMapPosition = true;

    MyVector currentMapCoords;
    MyVector selectedMapCoords;

    BitmapFont font;
    Sprite sprBg;

    Stage stage;
    Skin skin;
    MyListItem selectedTileName;

    java.util.List<MyListItem> gridItems;
    Texture box;

    @Override
    public void create() {

        Pixmap pixmap = new Pixmap(dim, dim, Format.RGBA8888);
        pixmap.setColor(new Color(1, 1, 0, .8f));
        int w = 1;
        pixmap.fillRectangle(0, 0, w, dim);
        pixmap.fillRectangle(dim - w, 0, w, dim);
        pixmap.fillRectangle(w, 0, dim - 2 * w, w);
        pixmap.fillRectangle(w, dim - w, dim - 2 * w, w);
        box = new Texture(pixmap);

        Texture tx = new Texture(Gdx.files.absolute("D:\\work\\gdx-andius\\src\\main\\resources\\assets\\data\\uf_heroes.png"));
        canvasGridWidth = tx.getWidth() / dim;
        canvasGridHeight = tx.getHeight() / dim;

        sprBg = new Sprite(tx, 0, 0, tx.getWidth(), tx.getHeight());

        gridItems = new ArrayList<>();

        font = new BitmapFont();
        font.setColor(Color.WHITE);

        batch = new SpriteBatch();
        skin = new Skin(Gdx.files.internal("assets/skin/uiskin.json"));
        stage = new Stage();

        readAtlas();

        final List<MyListItem> list = new List<>(skin);
        try {
            MyListItem[] tileNames = new MyListItem[Tile.values().length];
            int x = 0;
            for (Tile t : Tile.values()) {
                tileNames[x] = new MyListItem(t.toString(), 0, 0);
                x++;
            }
            list.setItems(tileNames);
        } catch (Exception ex) {
        }

        list.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                selectedTileName = list.getSelected();
            }
        });

        ScrollPane scrollPane = new ScrollPane(list, skin);
        scrollPane.setScrollingDisabled(true, false);

        TextButton makeButton = new TextButton("Make Atlas", skin, "default");
        makeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                makeAtlas();
            }
        });

        Table table = new Table(skin);
        table.defaults().pad(2);
        table.add(makeButton).expandX().left().width(175);
        table.row();
        table.add(scrollPane).expandX().left().width(175).maxHeight(screenHeight);
        table.setPosition(screenWidth - 175, 0);
        table.setFillParent(true);

        stage.addActor(table);

        Gdx.input.setInputProcessor(new InputMultiplexer(stage, this));

    }

    @Override
    public void render() {

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        batch.draw(sprBg, 0, screenHeight - sprBg.getHeight());

        font.draw(batch, "current mouse coords: " + currentMapCoords, 10, 60);
        font.draw(batch, "selectedMapCoords: " + selectedMapCoords, 10, 40);
        font.draw(batch, "selectedTileName: " + selectedTileName, 10, 20);

        for (MyListItem it : gridItems) {

            String n = (String) (it.name.length() > 1 ? it.name.subSequence(0, 2) : it.name);

            batch.draw(box, it.x * dim, screenHeight - (it.y * dim) - dim);
            font.draw(batch, n, it.x * dim + 5, screenHeight - (it.y * dim) - dim + (dim / 2));
        }

        batch.end();

        stage.act();
        stage.draw();
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {

        currentMapCoords = new MyVector(
                Math.round(screenX / dim),
                Math.round((screenHeight / dim) - ((screenHeight - screenY) / dim) - 1)
        );

        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {

        int x = Math.round(screenX / dim);
        int y = Math.round((screenHeight / dim) - ((screenHeight - screenY) / dim) - 1);

        if (y <= canvasGridHeight && x <= canvasGridWidth) {

            selectedMapCoords = new MyVector(x, y);

            if (selectedTileName != null) {

                MyListItem it = null;
                for (MyListItem temp : gridItems) {
                    if (temp.x == x && temp.y == y) {
                        it = temp;
                        break;
                    }
                }
                if (it == null) {
                    gridItems.add(new MyListItem(selectedTileName.name, x, y));
                } else {
                    gridItems.remove(it);
                }
            }

        }

        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Keys.SPACE) {
            //new PopupDialog(this.skin, this.gridItems).show(stage);
        }
        return false;
    }

    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "Sprite Atlas Tool";
        cfg.width = screenWidth;
        cfg.height = screenHeight;
        new LwjglApplication(new SpriteAtlasTool(), cfg);

    }

    public class MyVector {

        private int x;
        private int y;

        private MyVector(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return String.format("%s, %s", x, y);
        }

    }

    public class MyListItem implements Comparable<MyListItem> {

        public String name;
        public int x;
        public int y;

        public MyListItem(String name, int x, int y) {
            this.name = name;
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return String.format("%s %s, %s", name, x, y);
        }

        @Override
        public int compareTo(MyListItem o) {
            return this.name.compareTo(o.name);
        }

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
    public void dispose() {
        // TODO Auto-generated method stub

    }

    @Override
    public void resume() {
        // TODO Auto-generated method stub

    }

    public void makeAtlas() {

        Settings settings = new Settings();
        AtlasWriter mrp = new AtlasWriter(settings);
        Collections.sort(gridItems);

        ArrayList<Rect> packedRects = new ArrayList<>();
        String last = null;
        int idx = 0;
        for (MyListItem it : gridItems) {
            Rect rect = new Rect(it.x * dim, it.y * dim, dim, dim);
            rect.name = it.name;
            if (rect.name.equals(last)) {
                idx++;
            } else {
                idx = 0;
            }
            rect.index = idx;
            packedRects.add(rect);
            last = rect.name;
        }

        System.out.println("Writing: number of sprites: " + packedRects.size());

        try {
            mrp.writePackFileWithRects(new File("."), "sprites-atlas.txt", packedRects, "assets/tilemaps/latest.png");
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("done");

    }

    @SuppressWarnings("unchecked")
    public void readAtlas() {

        try {
            java.util.List<String> lines = FileUtils.readLines(new File("sprites-atlas.txt"));
            for (int i = 4; i < lines.size(); i += 7) {
                String name = lines.get(i).trim();
                try {
                    if (Tile.valueOf(name) == null) {
                        continue;
                    }
                } catch (Exception e) {
                    continue;
                }
                String xy = lines.get(i + 2).trim();
                String[] sp = xy.split(":|,| ");

                int mx = Integer.parseInt(sp[2]) / dim;
                int my = Integer.parseInt(sp[4]) / dim;
                MyListItem item = new MyListItem(name, mx, my);
                gridItems.add(item);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public enum Tile {

        WIZARD,
        CLERIC,
        PALADIN,
        RANGER,
        BARBARIAN,
        THIEF,
        DRUID,
        TORTURER,
        FIGHTER,
        SWASHBUCKLER,
        KNIGHT,
        WITCH,
        BAT_MAJOR,
        BAT_MINOR,
        SPIDER_MAJOR,
        SPIDER_MINOR,
        BLACK_WIDOW_MAJOR,
        BLACK_WIDOW_MINOR,
        DWARF_FIGHTER,
        SKELETON,
        SKELETON_SWORDSMAN,
        LICHE,
        SKELETON_ARCHER,
        ORC,
        ORC_SHIELDSMAN,
        TROLL,
        OGRE_SHAMAN,
        OGRE,
        ORC_SHAMAN,
        RAT_MAJOR,
        RAT_MINOR,
        ZOMBIE_GREEN,
        ZOMBIE_BLUE,
        WRAITH,
        DWARF_CLERIC,
        DWARF_LORD,
        MINOTAUR,
        VAMPIRE_RED,
        VAMPIRE_BLUE,
        SORCERER,
        SORCERER_EVIL,
        WOLF_BLACK,
        WOLF_BROWN,
        MERMAN_SWORDSMAN,
        MERMAN_PIKE,
        MERMAN_SHAMAN,
        MERMAN_SWORDSMAN_BLUE,
        MERMAN_PIKE_BLUE,
        MERMAN_SHAMAN_BLUE,
        GAZER,
        GAZER_BLUE,
        PHANTOM_BLUE,
        PHANTOM_RED,
        PHANTOM_GREY,
        PIXIE,
        PIXIE_RED,
        DEMON_RED,
        DEMON_BLUE,
        DEMON_GREEN,
        ANGEL,
        DARK_ANGEL,
        HALFLING,
        HALFLING_RANGER,
        HALFLING_SHIELDSMAN,
        HALFLING_WIZARD,
        WISP_MAJOR,
        WISP_MINOR,
        DRAGON_BLACK,
        DRAGON_RED,
        DRAGON_BLUE,
        DRAGON_GREEN,
        HAWK_WHITE,
        HAWK_BROWN,
        CROW,
        MUMMY,
        MUMMY_KING,
        GOLEM_STONE,
        GOLEM_FIRE,
        GOLEM_EARTH,
        GOLEM_ICE,
        GOLEM_MUD,
        COBRA_MAJOR,
        COBRA_MINOR,
        KING_RED,
        QUEEN_RED,
        KING_BLUE,
        QUEEN_BLUE,
        BEETLE_BLACK,
        BEETLE_RED,
        BEETLE_BLACK_MINOR,
        BEETLE_RED_MINOR,
        GHOST_MINOR,
        GHOST_MAJOR,
        SLIME_GREEN,
        SLIME_RED,
        SLIME_PURPLE,
        GRUB_MINOR,
        GRUB_MAJOR,
        ELEMENTAL_PURPLE,
        ELEMENTAL_BLUE,
        ELEMENTAL_ORANGE,
        ELEMENTAL_CYAN,
        ELEMENTAL_BROWN,
        BUTTERFLY_WHITE,
        BUTTERFLY_RED,
        BUTTERFLY_BLACK,
        FROG_GREEN,
        FROG_BLUE,
        FROG_BROWN,
        INSECT_SWARM,
        MIMIC,
        SHOPKEEPER_BROWN,
        SHOPKEEPER_BLOND,
        BLOOD_PRIEST,
        BARBARIAN_AXE,
        DEMON_LORD,
        DARK_WIZARD,
        FIGHTER_RED,
        HOLY_AVENGER,
        SWASHBUCKLER_BLUE,
        DEATH_KNIGHT,
        BRAWLER,
        BRAWLER_DARK,
        BRAWLER_BLOND,
        ELVEN_SWORDSMAN_GREEN,
        ELVEN_WIZARD_GREEN,
        ELVEN_ARCHER_GREEN,
        ELVEN_SWORDSMAN_BLUE,
        ELVEN_WIZARD_BLUE,
        ELVEN_ARCHER_BLUE,
        
        ;

    }

}
