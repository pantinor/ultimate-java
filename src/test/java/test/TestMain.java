package test;
import objects.ArmorSet;
import objects.CreatureSet;
import objects.MapSet;
import objects.Party;
import objects.SaveGame;
import objects.TileSet;
import objects.WeaponSet;
import ultima.CombatScreen;
import ultima.Constants;
import ultima.Constants.CreatureType;
import ultima.Constants.Maps;
import ultima.Constants.NpcDefaults;
import ultima.Constants.WeaponType;
import ultima.Context;
import ultima.StartScreen;
import util.LogDisplay;
import util.UltimaTiledMapLoader;
import util.Utils;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.utils.Array;

public class TestMain extends Game {
	
	TextureAtlas atlas;
	Animation beast1;
	Animation beast2;

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
			
			TiledMap tmap = new UltimaTiledMapLoader(Maps.GRASS_CON, a1, Maps.GRASS_CON.getMap().getWidth(), Maps.GRASS_CON.getMap().getHeight(), 16, 16).load();
			CombatScreen sc = new CombatScreen(null, null, context, Maps.WORLD, Maps.GRASS_CON.getMap(), tmap, CreatureType.balron, cs, a1, a2);
			
			sc.logs = new LogDisplay(new BitmapFont());
			
			setScreen(sc);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	

	public void init () {
		
		atlas = new TextureAtlas(Gdx.files.classpath("graphics/beasties-atlas.txt"));
		
		Array<AtlasRegion> anim1 = atlas.findRegions("beast");
		Array<AtlasRegion> anim2 = atlas.findRegions("dragon");

		Array<AtlasRegion> tmp1 = new Array<AtlasRegion>(StartScreen.beast1FrameIndexes.length);
		Array<AtlasRegion> tmp2 = new Array<AtlasRegion>(StartScreen.beast2FrameIndexes.length);
		
		for (int i=0;i<StartScreen.beast1FrameIndexes.length;i++) tmp1.add(anim1.get(StartScreen.beast1FrameIndexes[i]));
		for (int i=0;i<StartScreen.beast2FrameIndexes.length;i++) tmp2.add(anim2.get(StartScreen.beast2FrameIndexes[i]));
		
		beast1 = new Animation(0.25f, tmp1);
		beast2 = new Animation(0.25f, tmp2);
				
		batch2 = new SpriteBatch();

	}

	public void renderX () {
		time += Gdx.graphics.getDeltaTime();

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
				
		
		batch2.begin();
		batch2.draw(beast1.getKeyFrame(time, true), 100, 100, 48*2, 31*2);
		batch2.draw(beast2.getKeyFrame(time, true), 200, 200, 48*2, 31*2);

		batch2.end();
		
	}
	

	

	
	

	
	
}
