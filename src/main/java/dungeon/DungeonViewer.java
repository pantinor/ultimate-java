package dungeon;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import ultima.Constants;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

public class DungeonViewer implements ApplicationListener, InputProcessor, Constants {
	
	public Environment environment;
	public ModelBatch modelBatch;
	public ModelBatch shadowBatch;
	public SpriteBatch spriteBatch;
	public CameraInputController inputController;
	public PerspectiveCamera cam;
	public AssetManager assets;
	BitmapFont font;

	public static final int DUNGEON_MAP = 8;
	public DungeonTile[][][] dungeonTiles = new DungeonTile[DUNGEON_MAP][DUNGEON_MAP][DUNGEON_MAP];

	public List<DungeonTileModelInstance> modelInstances = new ArrayList<DungeonTileModelInstance>();
	public ModelInstance floor;
	
	public static Texture MINI_MAP_TEXTURE;
	private SpriteBatch batch;
	private int currentLevel = 0;
	private Vector3 currentPos;
	public enum Direction {NORTH, SOUTH, EAST, WEST};
	public Direction currentDir = Direction.EAST;

	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "DungeonViewer";
		cfg.width = 1000;
		cfg.height = 800;
		new LwjglApplication(new DungeonViewer(), cfg);
	}
	

	@Override
	public void create() {
		
		assets = new AssetManager(new ClasspathFileHandleResolver());
		//assets.load("skydome.g3db", Model.class);
		assets.load("graphics/dirt.png", Texture.class);
		assets.load("graphics/rock.png", Texture.class);
		assets.load("graphics/map.png", Texture.class);

		assets.update(2000);
		//skydome = new ModelInstance(assets.get("skydome.g3db", Model.class));
		
		MINI_MAP_TEXTURE = assets.get("graphics/map.png", Texture.class);
		font = new BitmapFont();
		font.setColor(Color.WHITE);
		
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1.f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
		
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.near = 0.1f;
		cam.far = 1000f;
		cam.update();
		
		batch = new SpriteBatch();

		
		inputController = new CameraInputController(cam);
		inputController.rotateLeftKey = inputController.rotateRightKey = inputController.forwardKey = inputController.backwardKey = 0;
		inputController.translateUnits = 30f;
		
		Gdx.input.setInputProcessor(new InputMultiplexer(this, inputController));


		modelBatch = new ModelBatch();
		spriteBatch = new SpriteBatch();
		
		ModelBuilder builder = new ModelBuilder();
		Model sf = builder.createBox(12, 1, 12, new Material(ColorAttribute.createDiffuse(Color.GRAY)), Usage.Position | Usage.Normal);
		//Model sf = builder.createBox(100, 2, 100, new Material(TextureAttribute.createDiffuse(assets.get("graphics/dirt.png", Texture.class))),	Usage.Position | Usage.TextureCoordinates | Usage.Normal);
		floor = new ModelInstance(sf, new Vector3(4,-.5f,4));
		
		
		try {
			
			InputStream is = DungeonViewer.class.getResourceAsStream("/data/despise.dng");
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
			
			
			//not sure how to do the wrapping stuff yet
//			for (int i = 0;i<DUNGEON_MAP;i++) {
//				{
//					int y = 0;
//					for (int x = 0; x < DUNGEON_MAP; x++) {
//						DungeonTile tile = dungeonTiles[i][x][y + DUNGEON_MAP - 1];
//						addBlock(i, tile, x+.5f,.5f,y-.5f);
//					}
//					for (int x = 0; x < DUNGEON_MAP; x++) {
//						DungeonTile tile = dungeonTiles[i][x][y];
//						addBlock(i, tile, x+.5f,.5f,y+.5f+DUNGEON_MAP);
//					}
//				}
//				{
//					int x = 0;
//					for (int y = 0; y < DUNGEON_MAP; y++) {
//						DungeonTile tile = dungeonTiles[i][x][y];
//						addBlock(i, tile, x+.5f+DUNGEON_MAP,.5f,y+.5f);
//					}
//					for (int y = 0; y < DUNGEON_MAP; y++) {
//						DungeonTile tile = dungeonTiles[i][x+DUNGEON_MAP-1][y];
//						addBlock(i, tile, x-.5f,.5f,y+.5f);
//					}
//				}
//			}
			

		
		} catch (Exception e) {
			e.printStackTrace();
		}


		
							
		createAxes();
		
		
	}
	
	@Override
	public void render() {
		
		cam.update();
		
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClearColor(0,0,0,0);
		Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);
		
		modelBatch.begin(cam);
		
		modelBatch.render(floor, environment);
						
		for (DungeonTileModelInstance i : modelInstances) {
			if (i.getLevel() == currentLevel) {
				modelBatch.render(i.getInstance(), environment);
			}
		}
		
		
        modelBatch.render(axesInstance);

		modelBatch.end();
		
		drawMiniMap();

		
		//for (int k : PRESSED_KEYS) {
		//	if (Gdx.input.isKeyPressed(k)) keyDown(k);
		//}
							
	}
	
	public void addBlock(int level, DungeonTile tile, float tx, float ty, float tz) {
		if (tile != DungeonTile.WALL) return;
		ModelBuilder builder = new ModelBuilder();
		Model model = builder.createBox(1, 1, 1, new Material(TextureAttribute.createDiffuse(assets.get("graphics/rock.png", Texture.class))),	Usage.Position | Usage.TextureCoordinates | Usage.Normal);		
		ModelInstance instance = new ModelInstance(model, tx, ty, tz);
		DungeonTileModelInstance in = new DungeonTileModelInstance(instance, tile, level);
		modelInstances.add(in);
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
		pixmap.setColor(0.3f, 0.3f, 0.3f, 0.7f);
		for (int y = 0; y < DUNGEON_MAP; y++) {
			for (int x = 0; x < DUNGEON_MAP; x++) {
				DungeonTile tile = dungeonTiles[currentLevel][x][y];
				if (tile == DungeonTile.WALL) {
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
		
		font.draw(batch, "current x,y: " + (Math.round(currentPos.x)-1) + ", " + (Math.round(currentPos.z)-1), 10, 40);
		font.draw(batch, "current direction: " + currentDir , 10, 20);

		
		batch.end();
	}
	
	public void setStartPosition() {
		for (int y = 0; y < DUNGEON_MAP; y++) {
			for (int x = 0; x < DUNGEON_MAP; x++) {
				DungeonTile tile = dungeonTiles[currentLevel][x][y];
				if (tile == DungeonTile.LADDER_UP) {
					currentPos = new Vector3(x+.5f,.5f,y+.5f);
				}
			}
		}
	}
	

	

	
	@Override
	public boolean keyDown (int keycode) {
		
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
			int x = (Math.round(currentPos.x)-1);
			int y = (Math.round(currentPos.z)-1);
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
		} else if (keycode == Keys.K) {
			//klimb
		} else if (keycode == Keys.D) {
			//descend
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


	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}
	
	
	
	final float GRID_MIN = -1*1000;
	final float GRID_MAX = 1*1000;
	final float GRID_STEP = 1;
	public Model axesModel;
	public ModelInstance axesInstance;

	private void createAxes() {
		ModelBuilder modelBuilder = new ModelBuilder();
		modelBuilder.begin();
		// grid
		MeshPartBuilder builder = modelBuilder.part("grid", GL30.GL_LINES, Usage.Position | Usage.ColorUnpacked, new Material());
		builder.setColor(Color.LIGHT_GRAY);
		for (float t = GRID_MIN; t <= GRID_MAX; t += GRID_STEP) {
			builder.line(t, 0, GRID_MIN, t, 0, GRID_MAX);
			builder.line(GRID_MIN, 0, t, GRID_MAX, 0, t);
		}
		// axes
		builder = modelBuilder.part("axes", GL30.GL_LINES, Usage.Position | Usage.ColorUnpacked, new Material());
		builder.setColor(Color.RED);
		builder.line(0, 0, 0, 500, 0, 0);
		builder.setColor(Color.GREEN);
		builder.line(0, 0, 0, 0, 500, 0);
		builder.setColor(Color.BLUE);
		builder.line(0, 0, 0, 0, 0, 500);
		axesModel = modelBuilder.end();
		axesInstance = new ModelInstance(axesModel);
	}	

}