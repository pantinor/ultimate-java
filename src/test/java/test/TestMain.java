package test;

import objects.Party;
import objects.SaveGame;
import ultima.Constants;
import ultima.Constants.NpcDefaults;
import ultima.Constants.WeaponType;
import ultima.Context;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.BitmapFontData;
import com.badlogic.gdx.graphics.g2d.BitmapFont.Glyph;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.maps.tiled.TiledMap;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import ultima.CombatScreen;
import ultima.Constants.CreatureType;
import ultima.Constants.Maps;
import ultima.Ultima4;
import util.UltimaTiledMapLoader;
import util.Utils;

public class TestMain extends Game {

    Animation<TextureRegion> a1, a2, a3;
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

            {
                FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("assets/fonts/ultima.ttf"));
                FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
                parameter.size = 48;
                parameter.color = Color.YELLOW;
                parameter.shadowColor = new Color(0, 1f, 0, 0.75f);
                parameter.shadowOffsetX = 2;
                parameter.shadowOffsetY = 2;

                BitmapFont ultimaFont = generator.generateFont(parameter);

                Texture texture = ultimaFont.getRegion().getTexture();
                TextureData data = texture.getTextureData();
                if (!data.isPrepared()) {
                    data.prepare();
                }
                Pixmap fontPixmap = data.consumePixmap();
                Pixmap pixmap = new Pixmap(48 * 9, 48 * 3, Pixmap.Format.RGBA8888);
                BitmapFontData bmfdata = ultimaFont.getData();

                int count = 0;
                String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
                for (int y = 0; y < 3; y++) {
                    for (int x = 0; x < 9; x++) {
                        if (count < 26) {
                            char c = chars.toCharArray()[count];
                            Glyph glyph = bmfdata.getGlyph(c);
                            pixmap.drawPixmap(fontPixmap, 
                                    (x * 48) + (48 - glyph.width) / 2,
                                    (y * 48) + (48 - glyph.height) / 2,
                                    glyph.srcX, glyph.srcY,
                                    glyph.width, glyph.height);
                            count++;
                        }
                    }
                }

                BufferedImage out = Utils.toBufferedImage(pixmap);
                ImageIO.write(out, "PNG", new File("target/ultima-font.png"));
            }

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
            //sg.items |= Constants.Item.MASK_MINAX.getLoc();
            //sg.items |= Constants.Item.RAGE_GOD.getLoc();
            //sg.players[0].weapon = WeaponType.SLING;

            TiledMap tmap = new UltimaTiledMapLoader(Maps.DNG6_CON, Ultima4.standardAtlas, Maps.DNG6_CON.getMap().getWidth(), Maps.DNG6_CON.getMap().getHeight(), 32, 32).load();
            CombatScreen sc = new CombatScreen(null, context, Maps.WORLD, Maps.DNG6_CON.getMap(), tmap, CreatureType.troll, Ultima4.creatures, Ultima4.standardAtlas);

            setScreen(sc);
            //atlas = a1;
            //tr = Utils.peerGem(Maps.LYCAEUM, a1);

            //batch2 = new SpriteBatch();

            //TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("sprites-atlas.txt"));
            //a1 = new Animation(0.45f, atlas.findRegions("shallows"));
            //TextureRegion[] frames = a1.getKeyFrames();
            //a2 = new Animation(0.45f, atlas.findRegions("water"));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
