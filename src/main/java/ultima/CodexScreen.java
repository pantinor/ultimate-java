package ultima;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.UBJsonReader;

public class CodexScreen extends BaseScreen {
	
	public GameScreen gameScreen;
	
	public Environment environment;
	public ModelBatch modelBatch;
	private SpriteBatch batch;
	
	public CameraInputController inputController;
	
	public Vector3 currentPos;

	public PerspectiveCamera cam;
	public AssetManager assets;
	BitmapFont font;
	
	//3d models
	public Model altarModel;
	
	public List<ModelInstance> modelInstances = new ArrayList<ModelInstance>();
	public List<ModelInstance> floor = new ArrayList<ModelInstance>();
	public List<ModelInstance> ceiling = new ArrayList<ModelInstance>();
	
	private Vector3 nll2 = new Vector3(1f, 0.8f, 0.6f);
	private Vector3 center = new Vector3(5.5f, 0, 5.5f);

	Model lightModel;
	Renderable pLight;
	PointLight fixedLight;
	float lightFactor;
	
	int inc;
	
	public CodexScreen(Stage stage, GameScreen gameScreen) {
		
		scType = ScreenType.DUNGEON;
		this.gameScreen = gameScreen;
		this.stage = stage;

		init();
	}
	
	@Override
	public void show() {
		Gdx.input.setInputProcessor(new InputMultiplexer(this, stage, inputController));
	}
	
	public void init() {
		
		assets = new AssetManager();
		assets.load("assets/graphics/dirt.png", Texture.class);
		assets.load("assets/graphics/Stone_Masonry.jpg", Texture.class);
		assets.load("assets/graphics/Stone_Vein_Gray.jpg", Texture.class);
		assets.load("assets/graphics/rock.png", Texture.class);

		assets.update(2000);
		
		ModelLoader<?> gloader = new G3dModelLoader(new UBJsonReader(), new ClasspathFileHandleResolver());
		altarModel = gloader.loadModel(Gdx.files.internal("assets/graphics/altar.g3db"));
		
		font = new BitmapFont();
		font.setColor(Color.WHITE);
		
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.15f, 0.15f, 0.15f, 1f));
		
		
		fixedLight = new PointLight();
		environment.add(fixedLight);
		
		environment.add(new PointLight().set(1f, 0.8f, 0.6f, 5.5f, 3f, 2f, 8f));
		
		modelBatch = new ModelBatch();
		batch = new SpriteBatch();
		
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.near = 0.1f;
		cam.far = 1000f;
		cam.update();
		
		inputController = new CameraInputController(cam);
		inputController.rotateLeftKey = inputController.rotateRightKey = inputController.forwardKey = inputController.backwardKey = 0;
		inputController.translateUnits = 30f;

		
		ModelBuilder builder = new ModelBuilder();
		
		for (int x=0;x<64;x++) {
			for (int y=0;y<64;y++) {
				Model sf = builder.createBox(1, 1, 1, new Material(TextureAttribute.createDiffuse(assets.get("assets/graphics/dirt.png", Texture.class))),	Usage.Position | Usage.TextureCoordinates | Usage.Normal);
				floor.add(new ModelInstance(sf, new Vector3(x-24.5f,-.5f,y-24.5f)));
			}
		}
		
		for (int x=0;x<12;x++) {
			for (int y=0;y<12;y++) {
				if ((x == 0) || (y == 0) || x == 11 || y == 11) {
					Model sf = builder.createCylinder(.5f, 5f, .5f, 10, new Material(TextureAttribute.createDiffuse(assets.get("assets/graphics/Stone_Masonry.jpg", Texture.class))), Usage.Position | Usage.TextureCoordinates | Usage.Normal);
					modelInstances.add(new ModelInstance(sf, new Vector3(x-.5f,1f,y-.5f)));
				}
			}
		}
		
		Model sf = builder.createCylinder(4.5f, .2f, 4.5f, 32, new Material(ColorAttribute.createDiffuse(Color.DARK_GRAY), ColorAttribute.createSpecular(Color.DARK_GRAY), new BlendingAttribute(0.7f)), Usage.Position | Usage.Normal);		
		modelInstances.add(new ModelInstance(sf, center));
		
		ModelInstance instance2 = new ModelInstance(altarModel, 5.5f, 0f, 2f);
		instance2.nodes.get(0).scale.set(.005f, .005f, .005f);
		instance2.calculateTransforms();
		modelInstances.add(instance2);

		
		currentPos = new Vector3(5.5f, 2, 1.8f);
		cam.position.set(currentPos);
		cam.lookAt(5.5f, 0, 5.5f);
		
		//createAxes();

	}
	
	@Override
	public boolean keyUp (int keycode) {
		inc ++;
		nextPiece();
		return false;
	}
	
	private void nextPiece() {
		
		switch (inc) {
		case 1:
			modelInstances.add(createLine(3.75f, 6.5f, 5.5f, 3.5f, 0.204f, Color.WHITE));
			break;
		case 2:
			modelInstances.add(createLine(5.5f, 3.5f, 7.25f, 6.5f, 0.204f, Color.WHITE));
			break;
		case 3:
			modelInstances.add(createLine(7.25f, 6.5f, 3.75f, 6.5f, 0.204f, Color.WHITE));
			break;
		case 4:
			modelInstances.add(createLine(3.5f, 4.75f, 7.5f, 4.75f, 0.204f, Color.WHITE));
			break;
		case 5:
			modelInstances.add(createLine(7.25f, 4.25f, 5.25f, 7.5f, 0.204f, Color.WHITE));
			break;
		case 6:
			modelInstances.add(createLine(5.75f, 7.5f, 3.75f, 4.25f, 0.204f, Color.WHITE));
			break;
		case 7:
			modelInstances.add(fillCircle(.05f, center, 0.204f, Color.WHITE));
			break;
		case 8:
			modelInstances.add(createCircle(4f, .1f,center, 0.204f, Color.WHITE));
			break;
		case 9:
			modelInstances.add(createCircle(.9f, .03f, new Vector3(5.05f, 0, 5.25f), 0.204f, Color.WHITE));
			break;		
		case 10:
			modelInstances.add(createCircle(.9f, .03f,new Vector3(5.95f, 0, 5.25f), 0.204f, Color.WHITE));
			break;		
		case 11:
			modelInstances.add(createCircle(.9f, .03f,new Vector3(5.5f, 0, 6.025f), 0.204f, Color.WHITE));
			break;
		default:
			break;
		}
		
	}
	
	
	@Override
	public void render(float delta) {
		
		cam.update();
		
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClearColor(0,0,0,0);
		Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);
		
		lightFactor += Gdx.graphics.getDeltaTime();
		float lightSize = 7.75f + 0.25f * (float)Math.sin(lightFactor) + .2f * MathUtils.random();
		
		fixedLight.set(nll2.x, nll2.y,  nll2.z, 5.5f, 2, 5.5f, lightSize);
		
		modelBatch.begin(cam);
				
		for (ModelInstance i : floor) {
			modelBatch.render(i, environment);
		}
		
		for (ModelInstance i : ceiling) {
			//modelBatch.render(i, environment);
		}
						
		for (ModelInstance i : modelInstances) {
			modelBatch.render(i, environment);
		}
		
		modelBatch.end();
		
				
		stage.act();
		stage.draw();
		
        //modelBatch.render(axesInstance);


	}

	@Override
	public void finishTurn(int currentX, int currentY) {
		
	}

	@Override
	public void partyDeath() {
		
	}
	
	final float GRID_MIN = -1*12;
	final float GRID_MAX = 1*12;
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
	
	private ModelInstance fillCircle(float radius, Vector3 c, float cz, Color color) {

        Model model = null;
        ModelBuilder mb = new ModelBuilder();
        mb.begin();
        MeshPartBuilder part = mb.part("circle", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse(color), ColorAttribute.createSpecular(color), new BlendingAttribute(0.6f)));
        part.circle(radius, 32, c, new Vector3(0, 1, 0));
        model = mb.end();

        ModelInstance modelInstance = new ModelInstance(model);
        modelInstance.transform.setToTranslation(0, cz, 0);

        return modelInstance;
    }
	
	private ModelInstance createCircle(float radius, float thickness, Vector3 c, float cz, Color color) {

        Model model = null;
        ModelBuilder mb = new ModelBuilder();
        mb.begin();
        MeshPartBuilder part = mb.part("circle", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse(color), ColorAttribute.createSpecular(color), new BlendingAttribute(0.6f)));
        part.ellipse (radius, radius, radius-thickness, radius-thickness, 32, c, new Vector3(0, 1, 0));
        model = mb.end();

        ModelInstance modelInstance = new ModelInstance(model);
        modelInstance.transform.setToTranslation(0, cz, 0);

        return modelInstance;
    }
	
	private ModelInstance createLine(float sx, float sy, float ex, float ey, float z, Color color) {

        Model model = null;
        ModelBuilder mb = new ModelBuilder();
        mb.begin();
        
		MeshPartBuilder builder = mb.part("line", GL30.GL_LINES, Usage.Position | Usage.ColorUnpacked, new Material());
		builder.setColor(color);
		builder.line(sx, 0, sy, ex, 0, ey); 
		
		model = mb.end();

        ModelInstance modelInstance = new ModelInstance(model);
        modelInstance.transform.setToTranslation(0, z, 0);

        return modelInstance;
    }
	
}
