package dungeon;


import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import objects.BaseMap;
import objects.Creature;
import objects.Tile;
import objects.TileSet;

import org.apache.commons.io.IOUtils;

import ultima.BaseScreen;
import ultima.CombatScreen;
import ultima.DeathScreen;
import ultima.GameScreen;
import ultima.Ultima4;
import util.DungeonRoomTiledMapLoader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader.Config;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.UBJsonReader;

public class DungeonScreen extends BaseScreen {
	
	private Maps dngMap;
	private String dungeonFileName;
	private GameScreen gameScreen;
	private Stage stage;
	
	public Environment environment;
	public ModelBatch modelBatch;
	private SpriteBatch batch;

	public PerspectiveCamera cam;
	public AssetManager assets;
	BitmapFont font;
	
	//3d models
	public static Model fountainModel;
	public static Model ladderModel;
	public static Model chestModel;
	public static Model orbModel;
	public static Model altarModel;
	
	boolean isTorchOn = true;
	private Vector3 vdll = new Vector3(.04f, .04f, .04f);
	private Vector3 nll2 = new Vector3(1f, 0.8f, 0.6f);
	private Vector3 nll = new Vector3(.96f, .58f, 0.08f);

	private PointLight circlingLight;
	private DirectionalLight fixedLight;
	private float lightPosition = 0;
	private Vector3 lightPathCenter = new Vector3(4, 4, 4);
	private float lightPathRadius = 3f;
	
	public static final int DUNGEON_MAP = 8;
	public DungeonTile[][][] dungeonTiles = new DungeonTile[DUNGEON_MAP][DUNGEON_MAP][DUNGEON_MAP];
	public DungeonRoom[] rooms = new DungeonRoom[64];//can have up to 64 rooms (abyss), 11x11 map grid
	public List<RoomLocater> locaters = new ArrayList<RoomLocater>();
	
	public List<DungeonTileModelInstance> modelInstances = new ArrayList<DungeonTileModelInstance>();
	public List<ModelInstance> floor = new ArrayList<ModelInstance>();
	public List<ModelInstance> ceiling = new ArrayList<ModelInstance>();

	public static Texture MINI_MAP_TEXTURE;
	public int currentLevel = 0;
	public Vector3 currentPos;
	public Direction currentDir = Direction.EAST;
		
	public DungeonScreen(Ultima4 mainGame, Stage stage, GameScreen gameScreen, Maps map) {
		
		scType = ScreenType.DUNGEON;
		this.dngMap = map;
		this.dungeonFileName = map.getMap().getFname();
		this.mainGame = mainGame;
		this.gameScreen = gameScreen;
		this.stage = stage;
		init();
	}
	
	@Override
	public void show() {
		if (stage != null) {
			Gdx.input.setInputProcessor(new InputMultiplexer(this, stage));
		} else {
			Gdx.input.setInputProcessor(this);
		}
	}

	
	public void init() {
				
		assets = new AssetManager();
		assets.load("assets/graphics/dirt.png", Texture.class);
		assets.load("assets/graphics/rock.png", Texture.class);
		assets.load("assets/graphics/map.png", Texture.class);
		assets.load("assets/graphics/Stone_Masonry.jpg", Texture.class);
		assets.load("assets/graphics/door.png", Texture.class);


		assets.update(2000);
		
		ModelLoader<?> gloader = new G3dModelLoader(new UBJsonReader(), new ClasspathFileHandleResolver());
		fountainModel = gloader.loadModel(Gdx.files.internal("assets/graphics/fountain2.g3db"));
		ladderModel = gloader.loadModel(Gdx.files.internal("assets/graphics/ladder.g3db"));
		chestModel = gloader.loadModel(Gdx.files.internal("assets/graphics/chest.g3db"));
		orbModel = gloader.loadModel(Gdx.files.internal("assets/graphics/orb.g3db"));
		altarModel = gloader.loadModel(Gdx.files.internal("assets/graphics/altar.g3db"));
				
		MINI_MAP_TEXTURE = assets.get("assets/graphics/map.png", Texture.class);
		font = new BitmapFont();
		font.setColor(Color.WHITE);
		
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.05f, 0.05f, 0.05f, 1f));
		
		DefaultShader.Config config = new Config();
		config.numDirectionalLights = 1;
		config.numPointLights = 1;
		config.numSpotLights = 0;
		
		circlingLight = new PointLight().set(1, .8f, .6f, 0f, 4f, 0f, 10);
		environment.add(circlingLight);
		
		fixedLight = new DirectionalLight().set(1f, 0.8f, 0.6f, 4f, 4f, 4f);
		environment.add(fixedLight);
		
		modelBatch = new ModelBatch(new DefaultShaderProvider(config));
		batch = new SpriteBatch();
		
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.near = 0.1f;
		cam.far = 1000f;
		cam.update();
				

		ModelBuilder builder = new ModelBuilder();
		for (int x=0;x<12;x++) {
			for (int y=0;y<12;y++) {
				Model sf = builder.createBox(1, 1, 1, new Material(TextureAttribute.createDiffuse(assets.get("assets/graphics/dirt.png", Texture.class))),	Usage.Position | Usage.TextureCoordinates | Usage.Normal);
				floor.add(new ModelInstance(sf, new Vector3(x-1.5f,-.5f,y-1.5f)));
				ceiling.add(new ModelInstance(sf, new Vector3(x-1.5f,1.5f,y-1.5f)));
			}
		}
		
		try {
			InputStream is = new FileInputStream("assets/data/" + dungeonFileName.toUpperCase());
			byte[] bytes = IOUtils.toByteArray(is);	
						
			int pos = 0 ;
			for (int i = 0;i<DUNGEON_MAP;i++) { 
				for (int y = 0; y < DUNGEON_MAP; y++) {
					for (int x = 0; x < DUNGEON_MAP; x++) {
						int index = bytes[pos] & 0xff;
						pos ++;
						DungeonTile tile = DungeonTile.getTileByValue(index);
						dungeonTiles[i][x][y] = tile;
						addBlock(i, tile, x+.5f,.5f,y+.5f);
					}
				}
			}
			
			//rooms
			pos = 0x200;
			for (int i=0;i<rooms.length;i++) {
				if (pos >= bytes.length) continue;
				rooms[i] = new DungeonRoom(bytes, pos);
				pos = pos + 256;
			}
			
			for (int i = 0;i<DUNGEON_MAP;i++) { 
				for (int y = 0; y < DUNGEON_MAP; y++) {
					for (int x = 0; x < DUNGEON_MAP; x++) {
						DungeonTile tile = dungeonTiles[i][x][y];
						if (tile.getValue() >= 208 && tile.getValue() <= 223) {
							DungeonRoom room = rooms[tile.getValue() - 207 - 1];
							if (dngMap == Maps.ABYSS) {
								if (i==0 || i==1) {
									//nothing
								} else if (i==2 || i==3) {
									room = rooms[tile.getValue() - 207 + 16 - 1];
								} else if (i==4 || i==5) {
									room = rooms[tile.getValue() - 207 + 32 - 1];
								} else if (i==6 || i==7) {
									room = rooms[tile.getValue() - 207 + 48 - 1];
								}
							}
							RoomLocater loc = new RoomLocater(x,y,i,room);
							locaters.add(loc);
						}
					}
				}
			}
			
			setStartPosition();
			
			cam.position.set(currentPos);
			cam.lookAt(currentPos.x+1, currentPos.y, currentPos.z);
			//cam.position.set(4,10,4);
			//cam.lookAt(4,0,4);
			
			//duplicate some of the outer edge tiles around the outside so that the wrapping is not so naked on the sides
			for (int i = 0;i<DUNGEON_MAP;i++) {
				{
					int y = 0;
					for (int x = 0; x < DUNGEON_MAP; x++) {
						DungeonTile tile = dungeonTiles[i][x][y + DUNGEON_MAP - 1];
						addBlock(i, tile, x+.5f,.5f,y-.5f);
					}
					for (int x = 0; x < DUNGEON_MAP; x++) {
						DungeonTile tile = dungeonTiles[i][x][y];
						addBlock(i, tile, x+.5f,.5f,y+.5f+DUNGEON_MAP);
					}
				}
				{
					int x = 0;
					for (int y = 0; y < DUNGEON_MAP; y++) {
						DungeonTile tile = dungeonTiles[i][x][y];
						addBlock(i, tile, x+.5f+DUNGEON_MAP,.5f,y+.5f);
					}
					for (int y = 0; y < DUNGEON_MAP; y++) {
						DungeonTile tile = dungeonTiles[i][x+DUNGEON_MAP-1][y];
						addBlock(i, tile, x-.5f,.5f,y+.5f);
					}
				}
			}
			

		
		} catch (Exception e) {
			e.printStackTrace();
		}
				
		
	}
	
	public void setStartPosition() {
		for (int y = 0; y < DUNGEON_MAP; y++) {
			for (int x = 0; x < DUNGEON_MAP; x++) {
				DungeonTile tile = dungeonTiles[currentLevel][x][y];
				if (tile == DungeonTile.NOTHING && currentPos == null) {
					currentPos = new Vector3(x+.5f,.5f,y+.5f);
				}
				if (tile == DungeonTile.LADDER_UP) {
					currentPos = new Vector3(x+.5f,.5f,y+.5f);
				}
			}
		}
	}
	
	public void restoreSaveGameLocation(int x, int y, int z, Direction orientation) {
		
		currentPos = new Vector3(x+.5f,.5f,y+.5f);
		cam.position.set(currentPos);
		currentDir = orientation;
		currentLevel = z;

		if (currentDir == Direction.EAST) {
			cam.lookAt(currentPos.x+1, currentPos.y, currentPos.z);
		} else if (currentDir == Direction.WEST) {
			cam.lookAt(currentPos.x-1, currentPos.y, currentPos.z);
		} else if (currentDir == Direction.NORTH) {
			cam.lookAt(currentPos.x, currentPos.y, currentPos.z-1);
		} else if (currentDir == Direction.SOUTH) {
			cam.lookAt(currentPos.x, currentPos.y, currentPos.z+1);
		}
	}
	
	@Override
	public void dispose() {
		modelBatch.dispose();
		batch.dispose();
	}
	
	@Override
	public void render(float delta) {
		
		cam.update();
		
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClearColor(0,0,0,0);
		Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);
		
		lightPosition += Gdx.graphics.getDeltaTime() * 1.0f;
		float lx = (float) (lightPathRadius * Math.cos(lightPosition));
		float ly = (float) (lightPathRadius * Math.sin(lightPosition));
		Vector3 lightVector = new Vector3(lx, 0, ly).add(lightPathCenter);
		
		Vector3 ll = isTorchOn ? nll : vdll;
		circlingLight.set(ll.x, ll.y,  ll.z, lightVector, 10);
		ll = isTorchOn ? nll2 : vdll;
		fixedLight.set(ll.x, ll.y,  ll.z, 4f, 4f, 4f);
				
		modelBatch.begin(cam);
		
		for (ModelInstance i : floor) {
			modelBatch.render(i, environment);
		}
		for (ModelInstance i : ceiling) {
			modelBatch.render(i, environment);
		}
						
		for (DungeonTileModelInstance i : modelInstances) {
			if (i.getLevel() == currentLevel) {
				modelBatch.render(i.getInstance(), environment);
			}
		}
		
		modelBatch.end();

		drawMiniMap();
		
		if (stage != null) {
			stage.act();
			stage.draw();
		}

							
	}
	
	public void addBlock(int level, DungeonTile tile, float tx, float ty, float tz) {
		ModelBuilder builder = new ModelBuilder();
		if (tile == DungeonTile.WALL) {
			Model model = builder.createBox(1, 1, 1, new Material(TextureAttribute.createDiffuse(assets.get("assets/graphics/rock.png", Texture.class))),	Usage.Position | Usage.TextureCoordinates | Usage.Normal);		
			ModelInstance instance = new ModelInstance(model, tx, ty, tz);
			DungeonTileModelInstance in = new DungeonTileModelInstance(instance, tile, level);
			modelInstances.add(in);
		} else if (tile.getValue() >= 144 && tile.getValue() <= 148) {
			ModelInstance instance = new ModelInstance(fountainModel, tx-.15f, 0, tz+.2f);
			instance.nodes.get(0).scale.set(.010f, .010f, .010f);
			instance.calculateTransforms();
			DungeonTileModelInstance in = new DungeonTileModelInstance(instance, tile, level);
			modelInstances.add(in);
		} else if (tile.getValue() >= 10 && tile.getValue() <= 48) {
			ModelInstance instance = new ModelInstance(ladderModel, tx-.15f, 0, tz+.2f);
			//instance.transform.setToRotation(Vector3.Y, 45);
			instance.nodes.get(0).scale.set(.060f, .060f, .060f);
			instance.calculateTransforms();
			DungeonTileModelInstance in = new DungeonTileModelInstance(instance, tile, level);
			modelInstances.add(in);
			
			Model manhole = builder.createCylinder(.75f, .02f, .75f, 32, new Material(ColorAttribute.createDiffuse(Color.DARK_GRAY)), Usage.Position | Usage.Normal);
			if (tile == DungeonTile.LADDER_DOWN) {
				instance = new ModelInstance(manhole, tx, 0, tz);
				modelInstances.add(new DungeonTileModelInstance(instance, tile, level));
			} else if (tile == DungeonTile.LADDER_UP) {
				instance = new ModelInstance(manhole, tx, 1, tz);
				modelInstances.add(new DungeonTileModelInstance(instance, tile, level));
			} else if (tile == DungeonTile.LADDER_UP_DOWN) {
				instance = new ModelInstance(manhole, tx, 0, tz);
				modelInstances.add(new DungeonTileModelInstance(instance, tile, level));
				instance = new ModelInstance(manhole, tx, 1, tz);
				modelInstances.add(new DungeonTileModelInstance(instance, tile, level));
			}
			
		} else if (tile == DungeonTile.CHEST) {
			ModelInstance instance = new ModelInstance(chestModel, tx-.15f, 0, tz+.2f);
			instance.nodes.get(0).scale.set(.010f, .010f, .010f);
			instance.calculateTransforms();
			DungeonTileModelInstance in = new DungeonTileModelInstance(instance, tile, level);
			modelInstances.add(in);
		} else if (tile == DungeonTile.ORB) {
			ModelInstance instance = new ModelInstance(orbModel, tx, .5f, tz);
			instance.nodes.get(0).scale.set(.0025f, .0025f, .0025f);
			instance.calculateTransforms();
			DungeonTileModelInstance in = new DungeonTileModelInstance(instance, tile, level);
			modelInstances.add(in);
		} else if (tile == DungeonTile.ALTAR) {
			ModelInstance instance = new ModelInstance(altarModel, tx-.40f, 0, tz+.45f);
			instance.nodes.get(0).scale.set(.0040f, .0040f, .0040f);
			instance.calculateTransforms();
			DungeonTileModelInstance in = new DungeonTileModelInstance(instance, tile, level);
			modelInstances.add(in);
		} else if (tile.getValue() >= 160 && tile.getValue() <= 163) {
			Color c = Color.GREEN;
			if (tile == DungeonTile.FIELD_ENERGY) c = Color.ORANGE;
			if (tile == DungeonTile.FIELD_FIRE) c = Color.RED;
			if (tile == DungeonTile.FIELD_SLEEP) c = Color.YELLOW;
			Model model = builder.createBox(1, .02f, 1, new Material(ColorAttribute.createDiffuse(c), ColorAttribute.createSpecular(c), new BlendingAttribute(0.4f)), Usage.Position | Usage.Normal);		
			ModelInstance instance = new ModelInstance(model, tx, 0, tz);
			DungeonTileModelInstance in = new DungeonTileModelInstance(instance, tile, level);
			modelInstances.add(in);
		} else if (tile.getValue() >= 208 && tile.getValue() <= 223) { //room indicator
			Model model = builder.createBox(1, 1, 1, new Material(ColorAttribute.createDiffuse(Color.DARK_GRAY), ColorAttribute.createSpecular(Color.DARK_GRAY), new BlendingAttribute(0.6f)), Usage.Position | Usage.Normal);		
			ModelInstance instance = new ModelInstance(model, tx, .5f, tz);
			DungeonTileModelInstance in = new DungeonTileModelInstance(instance, tile, level);
			modelInstances.add(in);
		} else if (tile == DungeonTile.DOOR || tile == DungeonTile.SECRET_DOOR) {
			Model model = builder.createBox(1, 1, 1, new Material(TextureAttribute.createDiffuse(assets.get("assets/graphics/rock.png", Texture.class))),	Usage.Position | Usage.TextureCoordinates | Usage.Normal);		
			ModelInstance instance = new ModelInstance(model, tx, ty, tz);
			DungeonTileModelInstance in = new DungeonTileModelInstance(instance, tile, level);
			modelInstances.add(in);
			
			String texture = tile == DungeonTile.DOOR?"assets/graphics/door.png":"assets/graphics/rock.png";
			
			model = builder.createBox(1.04f, .85f, .6f, new Material(TextureAttribute.createDiffuse(assets.get(texture, Texture.class))),	Usage.Position | Usage.TextureCoordinates | Usage.Normal);		
			instance = new ModelInstance(model, tx, .40f, tz);
			in = new DungeonTileModelInstance(instance, tile, level);
			modelInstances.add(in);
			
			model = builder.createBox(.6f, .85f, 1.04f, new Material(TextureAttribute.createDiffuse(assets.get(texture, Texture.class))),	Usage.Position | Usage.TextureCoordinates | Usage.Normal);		
			instance = new ModelInstance(model, tx, .40f, tz);
			in = new DungeonTileModelInstance(instance, tile, level);
			modelInstances.add(in);
		}
	}
	
	public class DungeonTileModelInstance {
		private ModelInstance instance;
		private DungeonTile tile;
		private int level;
		
		private DungeonTileModelInstance(ModelInstance instance, DungeonTile tile, int level) {
			this.instance = instance;
			this.tile = tile;
			this.level = level;
		}
		public ModelInstance getInstance() {
			return instance;
		}
		public DungeonTile getTile() {
			return tile;
		}
		public int getLevel() {
			return level;
		}
		public void setInstance(ModelInstance instance) {
			this.instance = instance;
		}
		public void setTile(DungeonTile tile) {
			this.tile = tile;
		}
		public void setLevel(int level) {
			this.level = level;
		}
		
	}
	
	public void drawMiniMap() {
		
		Pixmap pixmap = new Pixmap(MINI_MAP_TEXTURE.getWidth(), MINI_MAP_TEXTURE.getHeight(), Format.RGBA8888);
		for (int y = 0; y < DUNGEON_MAP; y++) {
			for (int x = 0; x < DUNGEON_MAP; x++) {
				DungeonTile tile = dungeonTiles[currentLevel][x][y];
				if (tile == DungeonTile.WALL || tile == DungeonTile.SECRET_DOOR  ) {
					pixmap.setColor(0.3f, 0.3f, 0.3f, 0.7f);
					pixmap.fillRectangle(35 + (x * 12), 35 + (y * 12), 12, 12);
				} else if (tile == DungeonTile.DOOR) {
					pixmap.setColor(0.6f, 0.6f, 0.6f, 0.7f);
					pixmap.fillRectangle(35 + (x * 12), 35 + (y * 12), 12, 12);
				} else if (tile.getValue() >= 208 && tile.getValue() <= 223) { //room indicator
					pixmap.setColor(0.36f, 0.04f, 0.04f, 0.7f);
					pixmap.fillRectangle(35 + (x * 12), 35 + (y * 12), 12, 12);
				}
			}
		}
		
		pixmap.setColor(1f, 0f, 0f, 0.7f);
		int x = (int)(32 + currentPos.x * 12);
		int y = (int)(32 + currentPos.z * 12);
		pixmap.fillRectangle(x, y, 7, 7);
		if (currentDir == Direction.EAST) {
			pixmap.fillRectangle(x+7, y+2, 3, 3);
		}
		if (currentDir == Direction.WEST) {
			pixmap.fillRectangle(x-3, y+2, 3, 3);
		}
		if (currentDir == Direction.NORTH) {
			pixmap.fillRectangle(x+2, y-2, 3, 3);
		}
		if (currentDir == Direction.SOUTH) {
			pixmap.fillRectangle(x+2, y+7, 3, 3);
		}
		
		Texture texture = new Texture(pixmap);
		pixmap.dispose();
		
		batch.begin();
		
		batch.draw(MINI_MAP_TEXTURE, 16, 10, 16, 16, MINI_MAP_TEXTURE.getWidth(), MINI_MAP_TEXTURE.getHeight(), 
				1f, 1f, 0, 0, 0, MINI_MAP_TEXTURE.getWidth(), MINI_MAP_TEXTURE.getHeight(), false, false);
		
		batch.draw(texture, 16, 10, 16, 16, MINI_MAP_TEXTURE.getWidth(), MINI_MAP_TEXTURE.getHeight(), 
				1f, 1f, 0, 0, 0, MINI_MAP_TEXTURE.getWidth(), MINI_MAP_TEXTURE.getHeight(), false, false);
		
		font.draw(batch, "Level: " + (currentLevel+1) + " x,y: " + (Math.round(currentPos.x)-1) + ", " + (Math.round(currentPos.z)-1), 10, 40);
		font.draw(batch, "Direction: " + currentDir, 10, 20);

		
		batch.end();
	}
		
	public void enterRoom(RoomLocater loc, Direction entryDir) {
		
		if (loc == null) return;
		Maps contextMap = Maps.get(dngMap.getId());
		
		TiledMap tiledMap = new DungeonRoomTiledMapLoader(loc.room, entryDir, GameScreen.standardAtlas).load();
		
		BaseMap baseMap = new BaseMap();
		baseMap.setTiles(loc.room.tiles);
		baseMap.setWidth(11);
		baseMap.setHeight(11);
		baseMap.setType(MapType.dungeon);
		baseMap.setPortals(dngMap.getMap().getPortals(loc.x, loc.y, loc.z));
		
		CombatScreen sc = new CombatScreen(mainGame, this, GameScreen.context, contextMap, baseMap, tiledMap, null, GameScreen.creatures, GameScreen.enhancedAtlas, GameScreen.standardAtlas);
		
		MapLayer mLayer = tiledMap.getLayers().get("Monster Positions");
		Iterator<MapObject> iter = mLayer.getObjects().iterator();
		while(iter.hasNext()) {
			MapObject obj = iter.next();
			int tile = (Integer)obj.getProperties().get("tile");
			int startX = (Integer)obj.getProperties().get("startX");
			int startY = (Integer)obj.getProperties().get("startY");
			
			if (tile == 0) continue;
			
			Tile t = GameScreen.baseTileSet.getTileByIndex(tile);
			
			Creature c = GameScreen.creatures.getInstance(CreatureType.get(t.getName()), GameScreen.enhancedAtlas, GameScreen.standardAtlas);
			
			c.currentX = startX;
			c.currentY = startY;
			c.currentPos = sc.getMapPixelCoords(startX, startY);

			baseMap.addCreature(c);
		}
		
		mainGame.setScreen(sc);
		
	}
	
	@Override
	public void endCombat(boolean isWon, BaseMap combatMap) {
		
		mainGame.setScreen(this);
		
		if (isWon) {
            GameScreen.context.getParty().adjustKarma(KarmaAction.KILLED_EVIL);
		} else {
			if (GameScreen.context.getParty().didAnyoneFlee()) {
                log("Battle is lost!");
                GameScreen.context.getParty().adjustKarma(KarmaAction.FLED_EVIL);
            } else if (!GameScreen.context.getParty().isAnyoneAlive()) {
            	mainGame.setScreen(new DeathScreen(mainGame, gameScreen, GameScreen.context.getParty()));
            	gameScreen.loadNextMap(Maps.CASTLE_OF_LORD_BRITISH_2, REVIVE_CASTLE_X, REVIVE_CASTLE_Y);
            }
		}
		
		//if exiting dungeon rooms, move out of the room with orientation to next coordinate
		if (combatMap.getType() == MapType.dungeon) {
			Direction exitDirection = GameScreen.context.getParty().getActivePartyMember().combatMapExitDirection;
			currentDir = exitDirection;
			
			int x = (Math.round(currentPos.x)-1);
			int y = (Math.round(currentPos.z)-1);
			
			if (exitDirection == Direction.EAST) {
				x=x+1;if(x>7)x=0; 
			} else if (exitDirection == Direction.WEST) {
				x=x-1;if(x<0)x=7; 
			} else if (exitDirection == Direction.NORTH) {
				y=y-1;if(y<0)y=7; 
			} else if (exitDirection == Direction.SOUTH) {
				y=y+1;if(y>7)y=0; 
			}
			
			DungeonTile tile = dungeonTiles[currentLevel][x][y];
			if (tile != DungeonTile.WALL) {
				currentPos = new Vector3(x+.5f,.5f,y+.5f);
				cam.position.set(currentPos);
				if (currentDir == Direction.EAST) {
					cam.lookAt(currentPos.x+1, currentPos.y, currentPos.z);
				} else if (currentDir == Direction.WEST) {
					cam.lookAt(currentPos.x-1, currentPos.y, currentPos.z);
				} else if (currentDir == Direction.NORTH) {
					cam.lookAt(currentPos.x, currentPos.y, currentPos.z-1);
				} else if (currentDir == Direction.SOUTH) {
					cam.lookAt(currentPos.x, currentPos.y, currentPos.z+1);
				}
			}
			
			if (tile.getValue() >= 208 && tile.getValue() <= 223) {
				RoomLocater loc = null;
				for (RoomLocater r : locaters) {
					if (r.z == currentLevel && r.x == x && r.y == y) {
						loc = r;
						break;
					}
				}
				enterRoom(loc, Direction.reverse(currentDir));
			}
		}
		
	}	
	
	@Override
	public boolean keyUp (int keycode) {
		
		int x = (Math.round(currentPos.x)-1);
		int y = (Math.round(currentPos.z)-1);
		
		if (keycode == Keys.LEFT) {
			
			if (currentDir == Direction.EAST) {
				cam.lookAt(currentPos.x, currentPos.y, currentPos.z - 1);
				currentDir = Direction.NORTH;
			} else if (currentDir == Direction.WEST) {
				cam.lookAt(currentPos.x, currentPos.y, currentPos.z + 1);
				currentDir = Direction.SOUTH;
			} else if (currentDir == Direction.NORTH) {
				cam.lookAt(currentPos.x - 1, currentPos.y, currentPos.z);
				currentDir = Direction.WEST;
			} else if (currentDir == Direction.SOUTH) {
				cam.lookAt(currentPos.x + 1, currentPos.y, currentPos.z);
				currentDir = Direction.EAST;
			}
			
		} else if (keycode == Keys.RIGHT) {
			
			if (currentDir == Direction.EAST) {
				cam.lookAt(currentPos.x, currentPos.y, currentPos.z + 1);
				currentDir = Direction.SOUTH;
			} else if (currentDir == Direction.WEST) {
				cam.lookAt(currentPos.x, currentPos.y, currentPos.z - 1);
				currentDir = Direction.NORTH;
			} else if (currentDir == Direction.NORTH) {
				cam.lookAt(currentPos.x + 1, currentPos.y, currentPos.z);
				currentDir = Direction.EAST;
			} else if (currentDir == Direction.SOUTH) {
				cam.lookAt(currentPos.x - 1, currentPos.y, currentPos.z);
				currentDir = Direction.WEST;
			}
			
		} else if (keycode == Keys.UP) {
			
			//forward
			if (currentDir == Direction.EAST) {
				x=x+1;if(x>7)x=0; 
			} else if (currentDir == Direction.WEST) {
				x=x-1;if(x<0)x=7; 
			} else if (currentDir == Direction.NORTH) {
				y=y-1;if(y<0)y=7; 
			} else if (currentDir == Direction.SOUTH) {
				y=y+1;if(y>7)y=0; 
			}
			
			DungeonTile tile = dungeonTiles[currentLevel][x][y];
			if (tile != DungeonTile.WALL) {
				currentPos = new Vector3(x+.5f,.5f,y+.5f);
				cam.position.set(currentPos);
				if (currentDir == Direction.EAST) {
					cam.lookAt(currentPos.x+1, currentPos.y, currentPos.z);
				} else if (currentDir == Direction.WEST) {
					cam.lookAt(currentPos.x-1, currentPos.y, currentPos.z);
				} else if (currentDir == Direction.NORTH) {
					cam.lookAt(currentPos.x, currentPos.y, currentPos.z-1);
				} else if (currentDir == Direction.SOUTH) {
					cam.lookAt(currentPos.x, currentPos.y, currentPos.z+1);
				}
			}
			
			if (tile.getValue() >= 208 && tile.getValue() <= 223) {
				RoomLocater loc = null;
				for (RoomLocater r : locaters) {
					if (r.z == currentLevel && r.x == x && r.y == y) {
						loc = r;
						break;
					}
				}
				enterRoom(loc, Direction.reverse(currentDir));
			}

		} else if (keycode == Keys.DOWN) {

			//backwards
			if (currentDir == Direction.EAST) {
				x=x-1;if(x<0)x=7; 
			} else if (currentDir == Direction.WEST) {
				x=x+1;if(x>7)x=0; 
			} else if (currentDir == Direction.NORTH) {
				y=y+1;if(y>7)y=0; 
			} else if (currentDir == Direction.SOUTH) {
				y=y-1;if(y<0)y=7; 
			}
			
			DungeonTile tile = dungeonTiles[currentLevel][x][y];
			if (tile != DungeonTile.WALL) {
				currentPos = new Vector3(x+.5f,.5f,y+.5f);
				cam.position.set(currentPos);
				if (currentDir == Direction.EAST) {
					cam.lookAt(currentPos.x+1, currentPos.y, currentPos.z);
				} else if (currentDir == Direction.WEST) {
					cam.lookAt(currentPos.x-1, currentPos.y, currentPos.z);
				} else if (currentDir == Direction.NORTH) {
					cam.lookAt(currentPos.x, currentPos.y, currentPos.z-1);
				} else if (currentDir == Direction.SOUTH) {
					cam.lookAt(currentPos.x, currentPos.y, currentPos.z+1);
				}
			}
			
			if (tile.getValue() >= 208 && tile.getValue() <= 223) {
				RoomLocater loc = null;
				for (RoomLocater r : locaters) {
					if (r.z == currentLevel && r.x == x && r.y == y) {
						loc = r;
						break;
					}
				}
				enterRoom(loc, currentDir);
			}
			
			
		} else if (keycode == Keys.K) {
			
			DungeonTile tile = dungeonTiles[currentLevel][x][y];
			if (tile == DungeonTile.LADDER_UP || tile == DungeonTile.LADDER_UP_DOWN) {
				currentLevel --;
				if (currentLevel < 0) {
					currentLevel = 0;
					if (mainGame != null) {
						mainGame.setScreen(gameScreen);
					}
				}
			}

		} else if (keycode == Keys.D) {
			
			DungeonTile tile = dungeonTiles[currentLevel][x][y];
			if (tile == DungeonTile.LADDER_DOWN || tile == DungeonTile.LADDER_UP_DOWN) {
				currentLevel ++;
				if (currentLevel > DUNGEON_MAP) {
					currentLevel = DUNGEON_MAP;
				}
			}
			
		} else if (keycode == Keys.Q) {
			GameScreen.context.saveGame(x, y, currentLevel, currentDir, dngMap);
			log("Saved Game.");
			
		} else if (keycode == Keys.Z) {
			isTorchOn = !isTorchOn;
		}
			
			
		return false;
	}
	
	
	/**
	 * Touch the magical ball at the current location
	 */
//	public void dungeonTouchOrb() {
//	    log("You find a Magical Orb...Who touches? ");
//	    int player = gameGetPlayer(false, false);
//	    if (player == -1)
//	        return;
//
//	    int stats = 0;
//	    int damage = 0;    
//	    
//	    /* Get current position and find a replacement tile for it */   
//	    Tile * orb_tile = c->location->map->tileset->getByName("magic_orb");
//	    MapTile replacementTile(c->location->getReplacementTile(c->location->coords, orb_tile));
//
//	    switch(c->location->map->id) {
//	    case MAP_DECEIT:    stats = STATSBONUS_INT; break;
//	    case MAP_DESPISE:   stats = STATSBONUS_DEX; break;
//	    case MAP_DESTARD:   stats = STATSBONUS_STR; break;
//	    case MAP_WRONG:     stats = STATSBONUS_INT | STATSBONUS_DEX; break;
//	    case MAP_COVETOUS:  stats = STATSBONUS_DEX | STATSBONUS_STR; break;
//	    case MAP_SHAME:     stats = STATSBONUS_INT | STATSBONUS_STR; break;
//	    case MAP_HYTHLOTH:  stats = STATSBONUS_INT | STATSBONUS_DEX | STATSBONUS_STR; break;
//	    default: break;
//	    }
//
//	    /* give stats bonuses */
//	    if (stats & STATSBONUS_STR) {
//	        screenMessage("Strength + 5\n");
//	        AdjustValueMax(c->saveGame->players[player].str, 5, 50);
//	        damage += 200;
//	    }
//	    if (stats & STATSBONUS_DEX) {
//	        screenMessage("Dexterity + 5\n");
//	        AdjustValueMax(c->saveGame->players[player].dex, 5, 50);        
//	        damage += 200;
//	    }
//	    if (stats & STATSBONUS_INT) {
//	        screenMessage("Intelligence + 5\n");
//	        AdjustValueMax(c->saveGame->players[player].intel, 5, 50);        
//	        damage += 200;
//	    }   
//	    
//	    /* deal damage to the party member who touched the orb */
//	    c->party->member(player)->applyDamage(damage);    
//	    /* remove the orb from the map */
//	    c->location->map->annotations->add(c->location->coords, replacementTile);
//	}
	
	/**
	 * Drink from the fountain at the current location
	 */
//	void dungeonDrinkFountain() {
//	    screenMessage("You find a Fountain.\nWho drinks? ");
//	    int player = gameGetPlayer(false, false);
//	    if (player == -1)
//	        return;
//
//	    Dungeon *dungeon = dynamic_cast<Dungeon *>(c->location->map);
//	    FountainType type = (FountainType) dungeon->currentSubToken();    
//
//	    switch(type) {
//	    /* plain fountain */
//	    case FOUNTAIN_NORMAL: 
//	        screenMessage("\nHmmm--No Effect!\n");
//	        break;
//
//	    /* healing fountain */
//	    case FOUNTAIN_HEALING: 
//	        if (c->party->member(player)->heal(HT_FULLHEAL))
//	            screenMessage("\nAhh-Refreshing!\n");
//	        else screenMessage("\nHmmm--No Effect!\n");
//	        break;
//	    
//	    /* acid fountain */
//	    case FOUNTAIN_ACID:
//	        c->party->member(player)->applyDamage(100); /* 100 damage to drinker */        
//	        screenMessage("\nBleck--Nasty!\n");
//	        break;
//
//	    /* cure fountain */
//	    case FOUNTAIN_CURE:
//	        if (c->party->member(player)->heal(HT_CURE))        
//	            screenMessage("\nHmmm--Delicious!\n");
//	        else screenMessage("\nHmmm--No Effect!\n");
//	        break;
//
//	    /* poison fountain */
//	    case FOUNTAIN_POISON: 
//	        if (c->party->member(player)->getStatus() != STAT_POISONED) {
//	            soundPlay(SOUND_POISON_DAMAGE);
//	            c->party->member(player)->applyEffect(EFFECT_POISON);
//	            c->party->member(player)->applyDamage(100); /* 100 damage to drinker also */            
//	            screenMessage("\nArgh-Choke-Gasp!\n");
//	        }
//	        else screenMessage("\nHmm--No Effect!\n");
//	        break;
//
//	    default:
//	        ASSERT(0, "Invalid call to dungeonDrinkFountain: no fountain at current location");
//	    }
//	}
	

	@Override
	public void finishTurn(int currentX, int currentY) {
		// TODO Auto-generated method stub
		
	}
	
	public class RoomLocater {
		public int x,y,z;
		public DungeonRoom room;
		public RoomLocater(int x, int y, int z, DungeonRoom room) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.room = room;
		}
	}
	
	
	public class DungeonRoom {
				
		public byte[][] triggers = new byte[4][4];
		
		public byte[] monsters = new byte[16];
		public byte[] monStartX = new byte[16];
		public byte[] monStartY = new byte[16];
		
		public byte[] plStartXNorth = new byte[8];
		public byte[] plStartYNorth = new byte[8];
		
		public byte[] plStartXEast = new byte[8];
		public byte[] plStartYEast = new byte[8];
		
		public byte[] plStartXSouth = new byte[8];
		public byte[] plStartYSouth = new byte[8];
		
		public byte[] plStartXWest = new byte[8];
		public byte[] plStartYWest = new byte[8];
		
		public Tile[] tiles = new Tile[11*11];
		
		public DungeonRoom(byte[] data, int pos) {
			
			for (int i=0;i<4;i++) {
				for (int j=0;j<4;j++) {
					triggers[j][i] = data[pos];
					pos++;
				}
			}
			for (int i=0;i<16;i++) {
				monsters[i] = data[pos];
				pos++;
			}
			for (int i=0;i<16;i++) {
				monStartX[i] = data[pos];
				pos++;
			}
			for (int i=0;i<16;i++) {
				monStartY[i] = data[pos];
				pos++;
			}
			for (int i=0;i<8;i++) {
				plStartXNorth[i] = data[pos];
				pos++;
			}
			for (int i=0;i<8;i++) {
				plStartYNorth[i] = data[pos];
				pos++;
			}
			for (int i=0;i<8;i++) {
				plStartXEast[i] = data[pos];
				pos++;
			}
			for (int i=0;i<8;i++) {
				plStartYEast[i] = data[pos];
				pos++;
			}
			for (int i=0;i<8;i++) {
				plStartXSouth[i] = data[pos];
				pos++;
			}
			for (int i=0;i<8;i++) {
				plStartYSouth[i] = data[pos];
				pos++;
			}
			for (int i=0;i<8;i++) {
				plStartXWest[i] = data[pos];
				pos++;
			}
			for (int i=0;i<8;i++) {
				plStartYWest[i] = data[pos];
				pos++;
			}
			
			TileSet ts = GameScreen.baseTileSet;
			
			for (int y=0;y<11;y++) {
				for (int x=0;x<11;x++) {
					tiles[x+(y*11)] = ts.getTileByIndex(data[pos]&0xff);
					pos++;
				}
			}
			
		}
		
		public Tile getTile(int x, int y) {
			if (x < 0 || y < 0) {
				return null;
			}
			if (x + (y * 11) >= tiles.length) {
				return null;
			}
			return tiles[x + (y * 11)];
		}

		

	}



}