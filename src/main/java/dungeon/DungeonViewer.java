package dungeon;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import ultima.Constants;
import ultima.Ultima4;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.UBJsonReader;

public class DungeonViewer implements ApplicationListener, InputProcessor, Constants {
	
	private String dungeonFileName;
	private Ultima4 mainGame;
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
	
	private PointLight circlingLight;
	private float lightPosition = 0;
	private Vector3 lightPathCenter = new Vector3(4, 4, 4);
	private float lightPathRadius = 3f;
	
	public static final int DUNGEON_MAP = 8;
	public DungeonTile[][][] dungeonTiles = new DungeonTile[DUNGEON_MAP][DUNGEON_MAP][DUNGEON_MAP];

	public List<DungeonTileModelInstance> modelInstances = new ArrayList<DungeonTileModelInstance>();
	public List<ModelInstance> floor = new ArrayList<ModelInstance>();
	public List<ModelInstance> ceiling = new ArrayList<ModelInstance>();

	public static Texture MINI_MAP_TEXTURE;
	private int currentLevel = 0;
	private Vector3 currentPos;
	public Direction currentDir = Direction.EAST;
	

	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "DungeonViewer";
		cfg.width = 1000;
		cfg.height = 800;
		new LwjglApplication(new DungeonViewer("/data/despise.dng"), cfg);
	}
	
	public DungeonViewer(String dungeonFileName) {
		this.dungeonFileName = dungeonFileName;
	}
	
	public DungeonViewer(Stage stage, Ultima4 mainGame, String dungeonFileName) {
		this.dungeonFileName = dungeonFileName;
		this.mainGame = mainGame;
		this.stage = stage;
	}

	@Override
	public void create() {
		
		assets = new AssetManager(new ClasspathFileHandleResolver());
		assets.load("graphics/dirt.png", Texture.class);
		assets.load("graphics/rock.png", Texture.class);
		assets.load("graphics/map.png", Texture.class);
		assets.load("graphics/Stone_Masonry.jpg", Texture.class);
		assets.load("graphics/door.png", Texture.class);


		assets.update(2000);
		
		ModelLoader<?> gloader = new G3dModelLoader(new UBJsonReader(), new ClasspathFileHandleResolver());
		fountainModel = gloader.loadModel(Gdx.files.classpath("graphics/fountain2.g3db"));
		ladderModel = gloader.loadModel(Gdx.files.classpath("graphics/ladder.g3db"));
		chestModel = gloader.loadModel(Gdx.files.classpath("graphics/chest.g3db"));
		orbModel = gloader.loadModel(Gdx.files.classpath("graphics/orb.g3db"));
		altarModel = gloader.loadModel(Gdx.files.classpath("graphics/altar.g3db"));
				
		MINI_MAP_TEXTURE = assets.get("graphics/map.png", Texture.class);
		font = new BitmapFont();
		font.setColor(Color.WHITE);
		
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		
		DefaultShader.Config config = new Config();
		config.numDirectionalLights = 1;
		config.numPointLights = 1;
		config.numSpotLights = 0;
		
		circlingLight = new PointLight().set(1, .8f, .6f, 0f, 4f, 0f, 10);
		environment.add(circlingLight);
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, 4f, 4f, 4f));
		
		modelBatch = new ModelBatch(new DefaultShaderProvider(config));
		batch = new SpriteBatch();
		
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.near = 0.1f;
		cam.far = 1000f;
		cam.update();
				
		if (stage != null) {
			Gdx.input.setInputProcessor(new InputMultiplexer(this, stage));
		} else {
			Gdx.input.setInputProcessor(this);
		}

		ModelBuilder builder = new ModelBuilder();
		for (int x=0;x<12;x++) {
			for (int y=0;y<12;y++) {
				Model sf = builder.createBox(1, 1, 1, new Material(TextureAttribute.createDiffuse(assets.get("graphics/dirt.png", Texture.class))),	Usage.Position | Usage.TextureCoordinates | Usage.Normal);
				floor.add(new ModelInstance(sf, new Vector3(x-1.5f,-.5f,y-1.5f)));
				ceiling.add(new ModelInstance(sf, new Vector3(x-1.5f,1.5f,y-1.5f)));
			}
		}
		
		try {
			
			InputStream is = DungeonViewer.class.getResourceAsStream(dungeonFileName);
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
	
	@Override
	public void dispose() {
		modelBatch.dispose();
		batch.dispose();
	}
	
	@Override
	public void render() {
		
		cam.update();
		
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClearColor(0,0,0,0);
		Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);
		
		lightPosition += Gdx.graphics.getDeltaTime() * 1.0f;
		float lx = (float) (lightPathRadius * Math.cos(lightPosition));
		float ly = (float) (lightPathRadius * Math.sin(lightPosition));
		Vector3 lightVector = new Vector3(lx, 0, ly).add(lightPathCenter);
		circlingLight.set(0.8f, 0.8f, 0.8f, lightVector, 10);

				
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
			Model model = builder.createBox(1, 1, 1, new Material(TextureAttribute.createDiffuse(assets.get("graphics/rock.png", Texture.class))),	Usage.Position | Usage.TextureCoordinates | Usage.Normal);		
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
			Model model = builder.createBox(1, 1, 1, new Material(ColorAttribute.createDiffuse(Color.DARK_GRAY), ColorAttribute.createSpecular(Color.DARK_GRAY), new BlendingAttribute(0.4f)), Usage.Position | Usage.Normal);		
			ModelInstance instance = new ModelInstance(model, tx, .5f, tz);
			DungeonTileModelInstance in = new DungeonTileModelInstance(instance, tile, level);
			modelInstances.add(in);
		} else if (tile == DungeonTile.DOOR || tile == DungeonTile.SECRET_DOOR) {
			Model model = builder.createBox(1, 1, 1, new Material(TextureAttribute.createDiffuse(assets.get("graphics/rock.png", Texture.class))),	Usage.Position | Usage.TextureCoordinates | Usage.Normal);		
			ModelInstance instance = new ModelInstance(model, tx, ty, tz);
			DungeonTileModelInstance in = new DungeonTileModelInstance(instance, tile, level);
			modelInstances.add(in);
			
			String texture = tile == DungeonTile.DOOR?"graphics/door.png":"graphics/rock.png";
			
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
				}
				if (tile == DungeonTile.DOOR) {
					pixmap.setColor(0.6f, 0.6f, 0.6f, 0.7f);
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
		font.draw(batch, "Direction: " + currentDir , 10, 20);

		
		batch.end();
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
	

	

	
	@Override
	public boolean keyDown (int keycode) {
		
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
			
			
		} else if (keycode == Keys.K) {
			//klimb
			currentLevel --;
			if (currentLevel < 0) {
				currentLevel = 0;
				if (mainGame != null) {
					mainGame.resurfaceFromDungeon();
				}
			}

		} else if (keycode == Keys.D) {
			//descend
			currentLevel ++;
			if (currentLevel > DUNGEON_MAP) currentLevel = DUNGEON_MAP;
		}
			
		return false;
	}


	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
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
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	


}