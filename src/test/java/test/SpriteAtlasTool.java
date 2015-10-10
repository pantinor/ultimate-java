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

    static int screenWidth = 1000;
    static int screenHeight = 800;

    int dim = 32;
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

        Texture tx = new Texture(Gdx.files.internal("assets/tilemaps/latest.png"));
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
            TileSet ts = (TileSet) Utils.loadXml("tileset-base.xml", TileSet.class);
            MyListItem[] tileNames = new MyListItem[ts.getTiles().size()];
            int x = 0;
            for (Tile t : ts.getTiles()) {
                tileNames[x] = new MyListItem(t.getName(), 0, 0);
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
        table.add(makeButton).expandX().left().width(150);
        table.row();
        table.add(scrollPane).expandX().left().width(150).maxHeight(screenHeight);
        table.setPosition(screenWidth - 150, 0);
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
            font.draw(batch, n, it.x * dim + 5, screenHeight - (it.y * dim) - dim + (dim/2));
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
                String xy = lines.get(i + 2).trim();
                String[] sp = xy.split(":|,| ");
                //System.out.println(name + " "  + sp[2] + "," + sp[4]);

                int mx = Integer.parseInt(sp[2]) / 32;
                int my = Integer.parseInt(sp[4]) / 32;
                MyListItem item = new MyListItem(name, mx, my);
                gridItems.add(item);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
