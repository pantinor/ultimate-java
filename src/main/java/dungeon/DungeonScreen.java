package dungeon;


import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import objects.BaseMap;
import objects.Creature;
import objects.Party.PartyMember;
import objects.Tile;
import objects.TileSet;

import org.apache.commons.io.IOUtils;

import ultima.BaseScreen;
import ultima.CombatScreen;
import ultima.DeathScreen;
import ultima.GameScreen;
import ultima.SecondaryInputProcessor;
import ultima.Sound;
import ultima.Sounds;
import ultima.Ultima4;
import util.DungeonRoomTiledMapLoader;
import util.Utils;

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
import com.badlogic.gdx.graphics.g2d.Batch;
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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.UBJsonReader;

public class DungeonScreen extends BaseScreen {
	
	public Maps dngMap;
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
		
	public SecondaryInputProcessor sip;
	private Texture miniMap;
	private MiniMapIcon miniMapIcon;
	
	public static final int xalignMM = Ultima4.SCREEN_WIDTH - 165 - 16;
	public static final int yalignMM = 10;


	public DungeonScreen(Ultima4 mainGame, Stage stage, GameScreen gameScreen, Maps map) {
		
		scType = ScreenType.DUNGEON;
		this.dngMap = map;
		this.dungeonFileName = map.getMap().getFname();
		this.mainGame = mainGame;
		this.gameScreen = gameScreen;
		this.stage = stage;
		
		sip = new SecondaryInputProcessor(this, stage);

		init();
	}
	
	@Override
	public void show() {
		if (stage != null) {
			Gdx.input.setInputProcessor(new InputMultiplexer(this, stage));
		} else {
			Gdx.input.setInputProcessor(this);
		}
		GameScreen.context.getParty().addObserver(this);
	}
	
	@Override
	public void hide() {
		Gdx.input.setInputProcessor(null);
		GameScreen.context.getParty().deleteObserver(this);
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
							
							System.out.println(dngMap.name() + " " + tile.toString() + " " + x + "," + y + ", " + i);
							for (int j=0;j<4;j++) if (room.triggers[j].tile.getIndex() != 0) System.out.println(room.triggers[j].toString());
							
							if (room.hasAltar) {
					            if (x == 3) room.altarRoomVirtue = BaseVirtue.LOVE;
					            else if (x <= 2) room.altarRoomVirtue = BaseVirtue.TRUTH;
					            else room.altarRoomVirtue = BaseVirtue.COURAGE;
							}
							
							RoomLocater loc = new RoomLocater(x,y,i,room);
							locaters.add(loc);
						}
					}
				}
			}
			
			miniMapIcon = new MiniMapIcon();
			miniMapIcon.setOrigin(5,5);
			
			stage = new Stage();
			stage.addActor(miniMapIcon);
			
			setStartPosition();
			
			cam.position.set(currentPos);
			cam.lookAt(currentPos.x+1, currentPos.y, currentPos.z);
			//cam.position.set(4,10,4);
			//cam.lookAt(4,0,4);
			
			//duplicate some of the outer edge tiles around the outside 
			//so that the wrapping is not so naked on the sides
			//i went 2 layers duplicated on each edge
			for (int i = 0;i<DUNGEON_MAP;i++) {
				{
					int y = 0;
					for (int x = 0; x < DUNGEON_MAP; x++) {
						DungeonTile tile = dungeonTiles[i][x][y + DUNGEON_MAP - 1];
						addBlock(i, tile, x+.5f,.5f,y-.5f);
						
						tile = dungeonTiles[i][x][y + DUNGEON_MAP - 2];
						addBlock(i, tile, x+.5f,.5f,y-1.5f);
					}
					for (int x = 0; x < DUNGEON_MAP; x++) {
						DungeonTile tile = dungeonTiles[i][x][y];
						addBlock(i, tile, x+.5f,.5f,y+.5f+DUNGEON_MAP);
						
						tile = dungeonTiles[i][x][y+1];
						addBlock(i, tile, x+.5f,.5f,y+.5f+DUNGEON_MAP+1);
					}
				}
				{
					int x = 0;
					for (int y = 0; y < DUNGEON_MAP; y++) {
						DungeonTile tile = dungeonTiles[i][x][y];
						addBlock(i, tile, x+.5f+DUNGEON_MAP,.5f,y+.5f);
						
						tile = dungeonTiles[i][x+1][y];
						addBlock(i, tile, x+.5f+DUNGEON_MAP+1,.5f,y+.5f);
					}
					for (int y = 0; y < DUNGEON_MAP; y++) {
						DungeonTile tile = dungeonTiles[i][x+DUNGEON_MAP-1][y];
						addBlock(i, tile, x-.5f,.5f,y+.5f);
						
						tile = dungeonTiles[i][x+DUNGEON_MAP-2][y];
						addBlock(i, tile, x-1.5f,.5f,y+.5f);
					}
				}
			}
			
			createMiniMap();
		
		} catch (Exception e) {
			e.printStackTrace();
		}
				
		
	}
	
	private void setStartPosition() {
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
		
		createMiniMap();
		moveMiniMapIcon();

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
		
		createMiniMap();
		moveMiniMapIcon();
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
		
		stage.act();
		stage.draw();

							
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
			in.x=(int)tx;in.y=(int)tz;
		} else if (tile == DungeonTile.ORB) {
			ModelInstance instance = new ModelInstance(orbModel, tx, .5f, tz);
			instance.nodes.get(0).scale.set(.0025f, .0025f, .0025f);
			instance.calculateTransforms();
			DungeonTileModelInstance in = new DungeonTileModelInstance(instance, tile, level);
			in.x=(int)tx;in.y=(int)tz;
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
			Model model = builder.createBox(1, 1, 1, new Material(ColorAttribute.createDiffuse(c), ColorAttribute.createSpecular(c), new BlendingAttribute(0.7f)), Usage.Position | Usage.Normal);		
			ModelInstance instance = new ModelInstance(model, tx, .5f, tz);
			DungeonTileModelInstance in = new DungeonTileModelInstance(instance, tile, level);
			modelInstances.add(in);
			in.x=(int)tx;in.y=(int)tz;
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
		public int x;
		public int y;
		
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
	
	private void createMiniMap() {
		
		if (miniMap != null) {
			miniMap.dispose();
		}
		
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
				} else if (tile.getValue() >= 160 && tile.getValue() <= 163) { //fields
					Color c = Color.GREEN;
					if (tile == DungeonTile.FIELD_ENERGY) c = Color.ORANGE;
					if (tile == DungeonTile.FIELD_FIRE) c = Color.RED;
					if (tile == DungeonTile.FIELD_SLEEP) c = Color.YELLOW;
					pixmap.setColor(c);
					pixmap.fillRectangle(35 + (x * 12), 35 + (y * 12), 12, 12);
				}
			}
		}
		
		miniMap = new Texture(pixmap);
		pixmap.dispose();
		
	}
	
	public Texture createMiniMapIcon(Direction dir) {
		Pixmap pixmap = new Pixmap(9, 9, Format.RGBA8888);
		pixmap.setColor(1f, 0f, 0f, 0.7f);
		if (dir == Direction.EAST) {
			pixmap.fillRectangle(0,0, 6, 9);
			pixmap.fillRectangle(6, 3, 3, 3);
		} else if (dir == Direction.NORTH) {
			pixmap.fillRectangle(0,3, 9, 6);
			pixmap.fillRectangle(3, 0, 3, 3);
		} else if (dir == Direction.WEST) {
			pixmap.fillRectangle(3,0, 6, 9);
			pixmap.fillRectangle(0, 3, 3, 3);
		} else if (dir == Direction.SOUTH) {
			pixmap.fillRectangle(0,0, 9, 6);
			pixmap.fillRectangle(3, 6, 3, 3);
		}
		Texture texture = new Texture(pixmap);
		pixmap.dispose();
		return texture;
	}
	
	public class MiniMapIcon extends Actor {
		private Texture north;
		private Texture south;
		private Texture east;
		private Texture west;

		public MiniMapIcon() {
			super();
			//could not get rotateBy to work so needed to do it this way
			this.north = createMiniMapIcon(Direction.NORTH);
			this.east = createMiniMapIcon(Direction.EAST);
			this.west = createMiniMapIcon(Direction.WEST);
			this.south = createMiniMapIcon(Direction.SOUTH);
		}
		@Override
		public void draw(Batch batch, float parentAlpha) {
			Color color = getColor();
			batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
			Texture t = north;
			if (currentDir == Direction.EAST) t = east;
			if (currentDir == Direction.WEST) t = west;
			if (currentDir == Direction.SOUTH) t = south;
			batch.draw(t, getX(), getY());
		}
	}
	
	private void moveMiniMapIcon() {
		miniMapIcon.setX(xalignMM + 37 + (Math.round(currentPos.x)-1) * 12);
		miniMapIcon.setY(yalignMM + 165 - (33 + Math.round(currentPos.z) * 12));
	}
	
	public void drawMiniMap() {
				
		batch.begin();
		
		batch.draw(MINI_MAP_TEXTURE, xalignMM, yalignMM);
		batch.draw(miniMap, xalignMM, yalignMM);
		
		font.draw(batch, (Math.round(currentPos.x)-1) + ", " + (Math.round(currentPos.z)-1) + ", " + (currentLevel+1), 5, Ultima4.SCREEN_HEIGHT - 5);

		font.setColor(Color.WHITE);
		logs.render(batch);
		
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
		
	    if (loc.room.hasAltar) {
	        sc.log("The Altar Room of " + loc.room.altarRoomVirtue.toString());    
	    } 
		
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
				//no flee penalty in dungeons
            } else if (!GameScreen.context.getParty().isAnyoneAlive()) {
            	mainGame.setScreen(new DeathScreen(mainGame, gameScreen, GameScreen.context.getParty()));
            	gameScreen.loadNextMap(Maps.CASTLE_OF_LORD_BRITISH_2, REVIVE_CASTLE_X, REVIVE_CASTLE_Y);
            }
		}
		
		//if exiting dungeon rooms, move out of the room with orientation to next coordinate
		if (combatMap.getType() == MapType.dungeon) {
			Direction exitDirection = GameScreen.context.getParty().getActivePartyMember().combatMapExitDirection;
			if (exitDirection != null) {
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
					checkTrap(tile, x, y);
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
			} else {
				//no one exited, perhaps they all died, death screen will show up next
			}
		}
		
	}	
	
	@Override
	public boolean keyUp (int keycode) {
		
		int x = (Math.round(currentPos.x)-1);
		int y = (Math.round(currentPos.z)-1);
		DungeonTile tile = dungeonTiles[currentLevel][x][y];
		
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
			
			tile = dungeonTiles[currentLevel][x][y];
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
				moveMiniMapIcon();
				checkTrap(tile, x, y);
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
			tile = dungeonTiles[currentLevel][x][y];
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
				moveMiniMapIcon();
				checkTrap(tile, x, y);
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
			if (tile == DungeonTile.LADDER_UP || tile == DungeonTile.LADDER_UP_DOWN) {
				currentLevel --;
				if (currentLevel < 0) {
					currentLevel = 0;
					if (mainGame != null) {
						mainGame.setScreen(gameScreen);
					}
				} else {
					createMiniMap();
				}
			}

		} else if (keycode == Keys.D) {
			if (tile == DungeonTile.LADDER_DOWN || tile == DungeonTile.LADDER_UP_DOWN) {
				currentLevel ++;
				if (currentLevel > DUNGEON_MAP) {
					currentLevel = DUNGEON_MAP;
				} else {
					createMiniMap();
				}
			}
			
		} else if (keycode == Keys.Q) {
			GameScreen.context.saveGame(x, y, currentLevel, currentDir, dngMap);
			log("Saved Game.");
			
		} else if (keycode == Keys.I) {
			
			isTorchOn = !isTorchOn;
			
		} else if (keycode == Keys.S) {
			if (tile == DungeonTile.ALTAR) {
				log("Search Altar");
				ItemMapLabels l = dngMap.getMap().searchLocation(GameScreen.context.getParty(), x, y, currentLevel);
				if (l != null) {
					log("You found " + l.getDesc() + ".");
				} else {
					log("Nothing!");
				}
			} else {
				
				if (tile.getValue() >= 144 && tile.getValue() <= 148) {
					log("You find a Fountain. Who drinks?");
				} else if (tile == DungeonTile.ORB) {
					log("You find a Magical Orb...Who touches?");
				} else {
					log("Who searches?");
				}

				Gdx.input.setInputProcessor(sip);
				sip.setinitialKeyCode(keycode, tile, x, y);
				return false;
			}
		}
			
			
		return false;
	}
	
	@SuppressWarnings("incomplete-switch")
	public void checkTrap(DungeonTile tile, int x, int y) {
		switch (tile) {
		case WIND_TRAP:
			log("Wind extinguished your torch!");
            Sounds.play(Sound.WIND);
			isTorchOn = false;
			break;
		case PIT_TRAP:
	        log("Pit!");
	        GameScreen.context.getParty().applyEffect(TileEffect.LAVA);
            Sounds.play(Sound.BOOM);
    		dungeonTiles[currentLevel][x][y] = DungeonTile.NOTHING;
			break;
		case ROCK_TRAP:
	        log("Falling Rocks!");
	        GameScreen.context.getParty().applyEffect(TileEffect.LAVA);
            Sounds.play(Sound.ROCKS);
    		dungeonTiles[currentLevel][x][y] = DungeonTile.NOTHING;
			break;
		}
	}
	
	public void dungeonTouchOrb(int index) {
		
	    PartyMember pm = GameScreen.context.getParty().getMember(index);
		int x = (Math.round(currentPos.x)-1);
		int y = (Math.round(currentPos.z)-1);
		
	    int stats = 0;
	    int damage = 0;       

	    switch(dngMap) {
	    case DECEIT:    stats = STATSBONUS_INT; break;
	    case DESPISE:   stats = STATSBONUS_DEX; break;
	    case DESTARD:   stats = STATSBONUS_STR; break;
	    case WRONG:     stats = STATSBONUS_INT | STATSBONUS_DEX; break;
	    case COVETOUS:  stats = STATSBONUS_DEX | STATSBONUS_STR; break;
	    case SHAME:     stats = STATSBONUS_INT | STATSBONUS_STR; break;
	    case HYTHLOTH:  stats = STATSBONUS_INT | STATSBONUS_DEX | STATSBONUS_STR; break;
	    default: break;
	    }

	    if ((stats & STATSBONUS_STR) > 0) {
	        log("Strength + 5");
			int n = Utils.adjustValueMax(pm.getPlayer().str, 5, 50);
			pm.getPlayer().str = n;
	        damage += 200;
	    }
	    if ((stats & STATSBONUS_DEX) > 0) {
	        log("Dexterity + 5");
			int n = Utils.adjustValueMax(pm.getPlayer().dex, 5, 50);
			pm.getPlayer().dex = n;
	        damage += 200;
	    }
	    if ((stats & STATSBONUS_INT) > 0) {
	        log("Intelligence + 5");
			int n = Utils.adjustValueMax(pm.getPlayer().intel, 5, 50);
			pm.getPlayer().intel = n;
	        damage += 200;
	    }   
	    
	    pm.applyDamage(damage, false);
	    
	    Sounds.play(Sound.LIGHTNING);
	    
	    //remove orb model instance
	    DungeonTileModelInstance orb = null;
	    for (DungeonTileModelInstance dmi : modelInstances) {
	    	if (dmi.tile == DungeonTile.ORB) {
	    		if (dmi.x == x && dmi.y == y) {
	    			orb = dmi;
		    		break;
	    		}
	    	}
	    }
	    modelInstances.remove(orb);
		dungeonTiles[currentLevel][x][y] = DungeonTile.NOTHING;

	}
	

	@SuppressWarnings("incomplete-switch")
	public void dungeonDrinkFountain(DungeonTile type, int index) {
	    PartyMember pm = GameScreen.context.getParty().getMember(index);
	    switch(type) {
	    case FOUNTAIN_PLAIN: 
	        log("Hmmm--No Effect!");
	        break;
	    case FOUNTAIN_HEAL: 
	        if (pm.heal(HealType.FULLHEAL)) {
	            Sounds.play(Sound.HEALING);
	        	log("Ahh-Refreshing!");
	        } else {
	        	log("Hmmm--No Effect!");
	        }
	        break;
	    case FOUNTAIN_ACID:
	        pm.applyDamage(100, false); 
            Sounds.play(Sound.DAMAGE_EFFECT);
	        log("Bleck--Nasty!");
	        break;
	    case FOUNTAIN_CURE:
	        if (pm.heal(HealType.CURE)) {
	            Sounds.play(Sound.HEALING);
	        	log("Hmmm--Delicious!");
	        } else {
	        	log("Hmmm--No Effect!");
	        }
	        break;
	    case FOUNTAIN_POISON: 
	        if (pm.getPlayer().status != StatusType.POISONED) {
	            Sounds.play(Sound.DAMAGE_EFFECT);
	            pm.applyEffect(TileEffect.POISON);
	            pm.applyDamage(100, false);            
	            log("Argh-Choke-Gasp!");
	        } else {
	        	log("Hmm--No Effect!");
	        }
	        break;
	    }
	}
	
	public boolean validTeleportLocation(int x, int y, int z) {
		return 	dungeonTiles[z][x][y] == DungeonTile.NOTHING;
	}
	

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
				
		public Trigger[] triggers = new Trigger[4];
		
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
		
		public boolean hasAltar;
		public BaseVirtue altarRoomVirtue;
		
		public DungeonRoom(byte[] data, int pos) {
			
			byte[][] tr = new byte[4][4];
			for (int i=0;i<4;i++) {
				for (int j=0;j<4;j++) {
					tr[i][j] = data[pos];
					pos++;
				}
			}
			
			for (int i=0;i<4;i++) {
				triggers[i] = new Trigger(tr[i]);
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
					Tile t = ts.getTileByIndex(data[pos]&0xff);
					tiles[x+(y*11)] = t;
					if (t.getIndex() == 74) hasAltar = true;
					pos++;
				}
			}
			
		}
		
		public Trigger getTriggerAt(int x, int y) {
			
			for (int i=0;i<4;i++) 
				if (triggers[i].tile.getIndex() != 0 && triggers[i].trigX == x && triggers[i].trigY == y) 
					return triggers[i];
			
			return null;
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
	
	public class Trigger {
		public Tile tile;
		public int trigX;
		public int trigY;
		public int t1X;
		public int t1Y;
		public int t2X;
		public int t2Y;
		public Trigger(byte[] data) {
			this.tile = GameScreen.baseTileSet.getTileByIndex(data[0]&0xff);
			this.trigX = (data[1] >> 4) & 0x0f;
			this.trigY = data[1] & 0x0f;
			this.t1X = (data[2] >> 4) & 0x0f;
			this.t1Y = data[2] & 0x0f;
			this.t2X = (data[3] >> 4) & 0x0f;
			this.t2Y = data[3] & 0x0f;
		}
		@Override
		public String toString() {
			return String.format("Trigger [tile=%s, trigX=%s, trigY=%s, t1X=%s, t1Y=%s, t2X=%s, t2Y=%s]", tile.getName(), trigX, trigY, t1X, t1Y, t2X, t2Y);
		}

	}



}