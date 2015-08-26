package test;

import java.util.Iterator;

import objects.ArmorSet;
import objects.CreatureSet;
import objects.MapSet;
import objects.Party;
import objects.SaveGame;
import objects.TileSet;
import objects.WeaponSet;
import ultima.Constants;
import ultima.Constants.Maps;
import ultima.Constants.NpcDefaults;
import ultima.Constants.WeaponType;
import ultima.Context;
import ultima.StartScreen;
import util.Utils;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Region;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.utils.Array;

public class TestMain extends Game {

    TextureAtlas atlas;
    Animation beast1;
    Animation beast2;
    Texture tr;

    float time = 0;
    Batch batch2;

    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "test";
        cfg.width = 800;
        cfg.height = 600;
        new LwjglApplication(new TestMain(), cfg);
    }

    @Override
    public void create() {

        try {
            int TILE_SIZE = 16;
            FileHandle f = new FileHandle("assets/tilemaps/tiles-vga-atlas.txt");
            TextureAtlasData a = new TextureAtlasData(f, f.parent(), false);
            String[] mapTileIds = new String[a.getRegions().size + 1];
            for (Region r : a.getRegions()) {
                int x = r.left / r.width;
                int y = r.top / r.height;
                int i = y * TILE_SIZE + x + 1;
                mapTileIds[i] = r.name;
            }

            TiledMap map = new TmxMapLoader().load("assets/tilemaps/delveOfSorrows.tmx");
            Iterator<MapLayer> iter = map.getLayers().iterator();
            while (iter.hasNext()) {
                TiledMapTileLayer layer = (TiledMapTileLayer) iter.next();
                System.out.println(mapTileIds[layer.getCell(0, 0).getTile().getId()]);
            }

            TileSet baseTileSet = (TileSet) Utils.loadXml("tileset-base.xml", TileSet.class);
            baseTileSet.setMaps();

            MapSet maps = (MapSet) Utils.loadXml("maps.xml", MapSet.class);
            maps.init(baseTileSet);

            WeaponSet weapons = (WeaponSet) Utils.loadXml("weapons.xml", WeaponSet.class);
            ArmorSet armors = (ArmorSet) Utils.loadXml("armors.xml", ArmorSet.class);

            CreatureSet cs = (CreatureSet) Utils.loadXml("creatures.xml", CreatureSet.class);
            cs.init();

            Context context = new Context();
            SaveGame sg = new SaveGame();
            try {
                sg.read(Constants.PARTY_SAV_BASE_FILENAME);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Party party = new Party(sg);
            context.setParty(party);

            sg.players[0].hpMax = 500;

            party.join(NpcDefaults.Geoffrey.name());
            party.join(NpcDefaults.Shamino.name());
            party.join(NpcDefaults.Katrina.name());

            sg.players[0].weapon = WeaponType.SLING;

            TextureAtlas a1 = new TextureAtlas(Gdx.files.internal("assets/tilemaps/tiles-vga-atlas.txt"));
            TextureAtlas a2 = new TextureAtlas(Gdx.files.internal("assets/tilemaps/monsters-u4.atlas"));

			//TiledMap tmap = new UltimaTiledMapLoader(Maps.GRASS_CON, a1, Maps.GRASS_CON.getMap().getWidth(), Maps.GRASS_CON.getMap().getHeight(), 16, 16).load();
            //CombatScreen sc = new CombatScreen(null, null, context, Maps.WORLD, Maps.GRASS_CON.getMap(), tmap, CreatureType.skeleton, cs, a2, a1);
			//sc.logs = new LogDisplay(new BitmapFont());
			//setScreen(sc);
            atlas = a1;
            tr = Utils.peerGem(Maps.LYCAEUM, a1);

            batch2 = new SpriteBatch();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void init() {

        atlas = new TextureAtlas(Gdx.files.classpath("graphics/beasties-atlas.txt"));

        Array<AtlasRegion> anim1 = atlas.findRegions("beast");
        Array<AtlasRegion> anim2 = atlas.findRegions("dragon");

        Array<AtlasRegion> tmp1 = new Array<AtlasRegion>(StartScreen.beast1FrameIndexes.length);
        Array<AtlasRegion> tmp2 = new Array<AtlasRegion>(StartScreen.beast2FrameIndexes.length);

        for (int i = 0; i < StartScreen.beast1FrameIndexes.length; i++) {
            tmp1.add(anim1.get(StartScreen.beast1FrameIndexes[i]));
        }
        for (int i = 0; i < StartScreen.beast2FrameIndexes.length; i++) {
            tmp2.add(anim2.get(StartScreen.beast2FrameIndexes[i]));
        }

        beast1 = new Animation(0.25f, tmp1);
        beast2 = new Animation(0.25f, tmp2);

        batch2 = new SpriteBatch();

    }

    public void render() {
        time += Gdx.graphics.getDeltaTime();

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch2.begin();
		//batch2.draw(beast1.getKeyFrame(time, true), 100, 100, 48*2, 31*2);
        //batch2.draw(beast2.getKeyFrame(time, true), 200, 200, 48*2, 31*2);

        batch2.draw(tr, 5, 5);

        batch2.end();

    }

}
