package test;

import objects.Party;
import objects.SaveGame;
import ultima.Constants;
import ultima.Constants.NpcDefaults;
import ultima.Constants.WeaponType;
import ultima.Context;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import ultima.CombatScreen;
import ultima.Constants.CreatureType;
import ultima.Constants.Maps;
import ultima.Ultima4;
import util.UltimaTiledMapLoader;

public class TestMain extends Game {

    Animation a1,a2,a3;
    Texture tr;

    float time = 0;
    Batch batch2;

    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "test";
        cfg.width = 1024;
        cfg.height = 768;
        new LwjglApplication(new TestMain(), cfg);
    }

    @Override
    public void create() {

        try {
            
            Ultima4 ult = new Ultima4();
            ult.create();

            Context context = new Context();
            SaveGame sg = new SaveGame();
            try {
                sg.read(Constants.PARTY_SAV_BASE_FILENAME);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Party party = new Party(sg);
            context.setParty(party);
            context.setCurrentMap(Maps.WORLD.getMap());

            sg.players[0].hpMax = 700;

            party.join(NpcDefaults.Geoffrey.name());

            sg.players[0].weapon = WeaponType.SLING;

            TiledMap tmap = new UltimaTiledMapLoader(Maps.MARSH_CON, Ultima4.standardAtlas, Maps.MARSH_CON.getMap().getWidth(), Maps.MARSH_CON.getMap().getHeight(), 32, 32).load();
            CombatScreen sc = new CombatScreen(null, context, Maps.WORLD, Maps.MARSH_CON.getMap(), tmap, CreatureType.whirlpool, Ultima4.creatures, Ultima4.standardAtlas);
            
            setScreen(sc);
            //atlas = a1;
            //tr = Utils.peerGem(Maps.LYCAEUM, a1);

            batch2 = new SpriteBatch();
            
//            atlas = new TextureAtlas(Gdx.files.internal("sprites-atlas.txt"));
//
//            a1 = new Animation(0.45f, atlas.findRegions("shallows"));
//            a2 = new Animation(0.45f, atlas.findRegions("water"));
//            a3 = new Animation(0.45f, atlas.findRegions("sea"));
        
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

//    @Override
//    public void render() {
//        time += Gdx.graphics.getDeltaTime();
//
//        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
//
//        batch2.begin();
//        
//        int dim = 32;
//        
//        for (int x=0;x<3;x++) {
//            for (int y=0;y<3;y++) {
//                int dx = dim*x + 100;
//                int dy = dim*y + 200;
//                batch2.draw(a1.getKeyFrame(time, true),dx,dy);
//            }
//        }
//        
//        for (int x=0;x<3;x++) {
//            for (int y=0;y<3;y++) {
//                int dx = dim*x + 300;
//                int dy = dim*y + 200;
//                batch2.draw(a2.getKeyFrame(time, true),dx,dy);
//            }
//        }
//                
//        for (int x=0;x<3;x++) {
//            for (int y=0;y<3;y++) {
//                int dx = dim*x + 600;
//                int dy = dim*y + 200;
//                batch2.draw(a3.getKeyFrame(time, true),dx,dy);
//            }
//        }
//
//        batch2.end();
//
//    }

}
