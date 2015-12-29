package ultima;

import java.util.ArrayList;
import java.util.List;

import ultima.Constants.Direction;
import util.CodexLogDisplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
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
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.utils.UBJsonReader;
import objects.Party;
import objects.SaveGame;

public class CodexScreen extends BaseScreen {

    private final Screen returnScreen;
    private final Party party;
    public Environment environment;
    public ModelBatch modelBatch;
    private SpriteBatch batch;

    public CameraInputController inputController;

    public Vector3 currentPos;

    public AssetManager assets;
    BitmapFont font;

    //3d models
    public Model altarModel;
    public Model avatarModel;

    public List<ModelInstance> modelInstances = new ArrayList<>();
    public List<ModelInstance> floor = new ArrayList<>();

    private Vector3 nll2 = new Vector3(1f, 0.8f, 0.6f);
    private Vector3 center = new Vector3(5.5f, 0, 5.5f);

    Model lightModel;
    Renderable pLight;
    PointLight fixedLight;
    float lightFactor;

    CodexLogDisplay logs;

    int codexQuestionCount;

    enum CodexQuestion {

        honesty("What dost thou possess if all may rely upon your every word?"),
        compassion("What quality compels one to share in the journeys of others?"),
        valor("What answers when great deeds are called for?"),
        justice("What should be the same for Lord and Serf alike?"),
        sacrifice("What is loath to place the self above aught else?"),
        honor("What shirks no duty?"),
        spirituality("What, in knowing the true self, knows all?"),
        humility("What is that which Serfs are born with but Nobles must strive to obtain?"),
        truth("If all else is imaginary, this is real..."),
        love("What plunges to the depths, while soaring on the heights?"),
        courage("What turns not away from any peril?"),
        infinity("Then what is the one thing which encompasses and is the whole of all undeniable Truth, unending Love, and unyielding Courage?");
        private String question;

        private CodexQuestion(String q) {
            this.question = q;
        }

        public static CodexQuestion getQ(int val) {
            CodexQuestion ret = CodexQuestion.honesty;
            for (CodexQuestion d : CodexQuestion.values()) {
                if (val == d.ordinal()) {
                    ret = d;
                    break;
                }
            }
            return ret;
        }
    };

    public static final String[] text1 = {
        "The boundless knowledge of the Codex of Ultimate Wisdom is revealed unto thee.",
        "The voice says: Thou hast proven thyself to be truly good in nature.",
        "Thou must know that thy quest to become an Avatar is the endless quest of a lifetime.",
        "Avatarhood is a living gift.  It must always and forever be nurtured to flourish.",
        "For if thou dost stray from the paths of virtue, thy way may be lost forever.",
        "Return now unto thine own world. Live there as an example to thy people, as our memory of thy gallant deeds serves us.",
        "As the sound of the voice trails off, darkness seems to rise around you. There is a moment of intense, wrenching vertigo",
        "You open your eyes to a familiar circle of stones.  You wonder of your recent adventures.",
        "It seems a time and place very distant.  You wonder if it really happened. Then you realize that in your hand you hold The Ankh.",
        "You walk away from the circle, knowing that you can always return from whence you came, since you now know the secret of the gates.",
        "CONGRATULATIONS!",
        "Thou hast completed ULTIMA IV Quest of the AVATAR in %s turns!",
        "Report thy feat unto Lord British at Origin Systems!"
    };

    State state = State.wordOfPassage;

    public enum State {

        wordOfPassage,
        codexQuestions,
        endText,
        done;
    }

    public CodexScreen(Screen returnScreen, Party party) {

        this.scType = ScreenType.CODEX;
        this.returnScreen = returnScreen;
        this.party = party;
        this.stage = new Stage();

        assets = new AssetManager();
        assets.load("assets/graphics/dirt.png", Texture.class);
        assets.load("assets/graphics/Stone_Masonry.jpg", Texture.class);
        assets.load("assets/graphics/Stone_Vein_Gray.jpg", Texture.class);
        assets.load("assets/graphics/rock.png", Texture.class);

        assets.update(2000);

        ModelLoader<?> gloader = new G3dModelLoader(new UBJsonReader());
        altarModel = gloader.loadModel(Gdx.files.internal("assets/graphics/altar.g3db"));
        avatarModel = gloader.loadModel(Gdx.files.internal("assets/graphics/avatar.g3db"));

        font = new BitmapFont(Gdx.files.internal("assets/fonts/corsiva-20.fnt"), false);
        font.setColor(Color.WHITE);

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.15f, 0.15f, 0.15f, 1f));

        fixedLight = new PointLight();
        environment.add(fixedLight);

        environment.add(new PointLight().set(1f, 0.8f, 0.6f, 7.5f, 3f, 7f, 8f));
        environment.add(new PointLight().set(1f, 0.8f, 0.6f, 1.5f, 3f, 7f, 8f));

        modelBatch = new ModelBatch();

        batch = new SpriteBatch();

        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = 0.1f;
        camera.far = 1000f;

        inputController = new CameraInputController(camera);
        inputController.rotateLeftKey = inputController.rotateRightKey = inputController.forwardKey = inputController.backwardKey = 0;
        inputController.translateUnits = 30f;

        ModelBuilder builder = new ModelBuilder();

        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 64; y++) {
                Model sf = builder.createBox(1, 1, 1, new Material(TextureAttribute.createDiffuse(assets.get("assets/graphics/dirt.png", Texture.class))), Usage.Position | Usage.TextureCoordinates | Usage.Normal);
                floor.add(new ModelInstance(sf, new Vector3(x - 24.5f, -.5f, y - 24.5f)));
            }
        }

        for (int x = 0; x < 12; x++) {
            for (int y = 0; y < 12; y++) {
                if ((x == 0) || (y == 0) || x == 11 || y == 11) {
                    Model sf = builder.createCylinder(.5f, 5f, .5f, 10, new Material(TextureAttribute.createDiffuse(assets.get("assets/graphics/Stone_Masonry.jpg", Texture.class))), Usage.Position | Usage.TextureCoordinates | Usage.Normal);
                    modelInstances.add(new ModelInstance(sf, new Vector3(x - .5f, 1f, y - .5f)));
                }
            }
        }

        ModelInstance instance2 = new ModelInstance(altarModel, 5.5f, 0f, 2f);
        instance2.nodes.get(0).scale.set(.005f, .005f, .005f);
        instance2.calculateTransforms();
        modelInstances.add(instance2);

        modelInstances.add(getFigure(4.5f, 0.528f, 3f, 0));
//		modelInstances.add(getFigure(4.0f, 0.528f, 2f, -10));
//		modelInstances.add(getFigure(3.5f, 0.528f, 3f, 30));
//		
//		modelInstances.add(getFigure(4.5f, 0.528f, 1.5f, -5));
//		modelInstances.add(getFigure(3.2f, 0.528f, 2.2f, 30));
//		
//		modelInstances.add(getFigure(6.5f, 0.528f, 2f, -20));
//		modelInstances.add(getFigure(7.0f, 0.528f, 2.3f, -30));
//		modelInstances.add(getFigure(7.2f, 0.528f, 2.8f, -40));

        currentPos = new Vector3(5.5f, 2f, 0f);
        camera.position.set(currentPos);
        camera.lookAt(5.5f, 0.2f, 5.5f);

        logs = new CodexLogDisplay(font);
        logs.add("There is a sudden darkness, and you find yourself alone in an empty chamber.");
        SaveGame saveGame = this.party.getSaveGame();
        int haveKeys = (saveGame.items & (Item.KEY_C.getLoc() | Item.KEY_L.getLoc() | Item.KEY_T.getLoc()));
        int keys = (Item.KEY_C.getLoc() | Item.KEY_L.getLoc() | Item.KEY_T.getLoc());
        if (haveKeys != keys) {
            codexEject("Thou dost not have the Key of Three Parts.");
        } else {

            SequenceAction seq = Actions.action(SequenceAction.class);
            seq.addAction(Actions.delay(5f));
            seq.addAction(Actions.run(new Runnable() {
                public void run() {
                    logs.add("You use yor key of three parts.");
                }
            }));
            seq.addAction(Actions.delay(5f));
            seq.addAction(Actions.run(new Runnable() {
                public void run() {
                    logs.add("A voice rings out:");
                    logs.add("What is the Word of Passage?");
                    logs.add(" ");
                    CodexInputAdapter cia = new CodexInputAdapter();
                    Gdx.input.setInputProcessor(cia);
                }
            }));
            stage.addAction(seq);

        }

		//createAxes();
    }
    
    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputMultiplexer(this, stage, inputController));
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public boolean keyUp(int keycode) {

        if (state == State.endText) {
            state = State.done;
            SequenceAction seq = Actions.action(SequenceAction.class);
            for (final String s : text1) {
                seq.addAction(Actions.delay(5f));
                seq.addAction(Actions.run(new Runnable() {
                    public void run() {
                        logs.add(s);
                    }
                }));
            }
            seq.addAction(Actions.run(new Runnable() {
                public void run() {
                    mainGame.setScreen(Ultima4.startScreen);
                }
            }));
            stage.addAction(seq);
        }
        return false;
    }

    private void nextPiece() {

        switch (codexQuestionCount) {
            case 1:
                //modelInstances.add(createLine(3.75f, 6.5f, 5.5f, 3.5f, 0.204f, Color.WHITE));
                modelInstances.add(createPolygonBox(Virtue.HONESTY.getColor(), .02f, .02f, 3.5f, -30f, 4.65f, 0.204f, 5.0f));
                break;
            case 2:
                //modelInstances.add(createLine(5.5f, 3.5f, 7.25f, 6.5f, 0.204f, Color.WHITE));
                modelInstances.add(createPolygonBox(Virtue.COMPASSION.getColor(), .02f, .02f, 3.5f, 30f, 6.35f, 0.204f, 5.0f));
                break;
            case 3:
                //modelInstances.add(createLine(7.25f, 6.5f, 3.75f, 6.5f, 0.204f, Color.WHITE));
                modelInstances.add(createPolygonBox(Virtue.VALOR.getColor(), .02f, .02f, 3.5f, 90f, 5.5f, 0.204f, 6.5f));
                break;
            case 4:
                //modelInstances.add(createLine(3.5f, 4.75f, 7.5f, 4.75f, 0.204f, Color.WHITE));
                modelInstances.add(createPolygonBox(Virtue.JUSTICE.getColor(), .02f, .02f, 3.7f, 90f, 5.5f, 0.204f, 4.75f));
                break;
            case 5:
                //modelInstances.add(createLine(7.25f, 4.25f, 5.25f, 7.5f, 0.204f, Color.WHITE));
                modelInstances.add(createPolygonBox(Virtue.SACRIFICE.getColor(), .02f, .02f, 3.7f, -30f, 6.2f, 0.204f, 5.9f));
                break;
            case 6:
                //modelInstances.add(createLine(5.75f, 7.5f, 3.75f, 4.25f, 0.204f, Color.WHITE));
                modelInstances.add(createPolygonBox(Virtue.HONOR.getColor(), .02f, .02f, 3.7f, 30f, 4.8f, 0.204f, 5.9f));
                break;
            case 7:
                ModelBuilder builder = new ModelBuilder();
                Model sp = builder.createSphere(.08f, .08f, .08f, 32, 32, new Material(ColorAttribute.createDiffuse(Virtue.SPIRITUALITY.getColor())), Usage.Position | Usage.Normal);
                ModelInstance modelInstance = new ModelInstance(sp);
                modelInstance.transform.setTranslation(5.5f, 0.204f, 5.5f);
                modelInstances.add(modelInstance);
                //modelInstances.add(fillCircle(.05f, center, 0.204f, Virtue.SPIRITUALITY.getColor()));
                break;
            case 8:
                //modelInstances.add(fillCircle(2.5f,center, 0.202f, Virtue.HUMILITY.getColor()));
                modelInstances.add(createCylinder(4.0f, .1f, 5.5f, 0.1f, 5.5f, Virtue.HUMILITY.getColor()));
                break;
            case 9:
                //modelInstances.add(createCircle(.9f, .03f, new Vector3(5.05f, 0, 5.25f), 0.204f, Color.BLUE));
                modelInstances.add(createCylinder(.9f, .1f, 5.05f, 0.204f, 5.25f, Color.BLUE));
                break;
            case 10:
                //modelInstances.add(createCircle(.9f, .03f,new Vector3(5.95f, 0, 5.25f), 0.204f, Color.YELLOW));
                modelInstances.add(createCylinder(.9f, .1f, 5.95f, 0.204f, 5.25f, Color.YELLOW));
                break;
            case 11:
                //modelInstances.add(createCircle(.9f, .03f,new Vector3(5.5f, 0, 6.025f), 0.204f, Color.RED));
                modelInstances.add(createCylinder(.9f, .1f, 5.5f, 0.204f, 6.025f, Color.RED));
                break;
            default:
                break;
        }

    }

    @Override
    public void render(float delta) {

        camera.update();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);

        lightFactor += Gdx.graphics.getDeltaTime();
        float lightSize = 7.75f + 0.25f * (float) Math.sin(lightFactor) + .2f * MathUtils.random();

        fixedLight.set(nll2.x, nll2.y, nll2.z, 5.5f, 2, 5.5f, lightSize);

        modelBatch.begin(camera);

        for (ModelInstance i : floor) {
            modelBatch.render(i, environment);
        }

        for (ModelInstance i : modelInstances) {
            modelBatch.render(i, environment);
        }

        modelBatch.end();

        stage.act();
        stage.draw();

        //modelBatch.render(axesInstance);
        batch.begin();
        logs.render(batch);
        batch.end();

        nextPiece();

    }

    private StringBuilder inputBuffer = new StringBuilder();

    class CodexInputAdapter extends InputAdapter {

        @Override
        public boolean keyUp(int keycode) {

            if (keycode == Keys.ENTER) {

                if (inputBuffer.length() < 1) {
                    return false;
                }
                String input = inputBuffer.toString().toLowerCase();

                if (state == State.wordOfPassage) {
                    if (input.startsWith("veramocor")) {

                        if (party.getMembers().size() != 8) {
                            codexEject("Thou art not ready.");
                            return false;
                        }

                        for (int i = 0; i < 8; i++) {
                            if (party.getSaveGame().karma[i] != 0) {
                                codexEject("Thou hast not proved thy leadership in all eight virtues.");
                                return false;
                            }
                        }

                        state = State.codexQuestions;
                        inputBuffer = new StringBuilder();
                        logs.add("Passage is granted.");
                        logs.add(CodexQuestion.honesty.question);
                        logs.add("");
                        Sounds.play(Sound.POSITIVE_EFFECT);
                    } else {
                        codexEject("Passage is not granted.");
                    }
                } else if (state == State.codexQuestions) {
                    if (input.startsWith(CodexQuestion.getQ(codexQuestionCount).toString())) {
                        inputBuffer = new StringBuilder();
                        codexQuestionCount++;
                        if (codexQuestionCount == CodexQuestion.infinity.ordinal()) {
                            logs.add("The ground rumbles beneath your feet.");
                            Sounds.play(Sound.TREMOR);
                        } else if (codexQuestionCount > CodexQuestion.infinity.ordinal()) {
                            state = State.endText;
                            Gdx.input.setInputProcessor(new InputMultiplexer(CodexScreen.this, stage, inputController));
                            return false;
                        } else {
                            Sounds.play(Sound.POSITIVE_EFFECT);
                        }
                        logs.add(CodexQuestion.getQ(codexQuestionCount).question);
                        logs.add("");
                    } else {
                        codexEject("That is not correct!");
                    }
                }

            } else if (keycode == Keys.BACKSPACE) {
                if (inputBuffer.length() > 0) {
                    inputBuffer.deleteCharAt(inputBuffer.length() - 1);
                    logs.logDeleteLastChar();
                }
            } else if (keycode >= 29 && keycode <= 54) {
                inputBuffer.append(Keys.toString(keycode).toUpperCase());
                logs.append(Keys.toString(keycode).toUpperCase());
            }
            return false;
        }
    }

    private void codexEject(String text) {
        logs.add(text);

        SequenceAction seq = Actions.action(SequenceAction.class);
        seq.addAction(Actions.run(new Runnable() {
            public void run() {
                Sounds.play(Sound.NEGATIVE_EFFECT);
            }
        }));
        seq.addAction(Actions.delay(3f));
        seq.addAction(Actions.run(new Runnable() {
            public void run() {
                mainGame.setScreen(returnScreen);
            }
        }));
        stage.addAction(seq);
    }

    final float GRID_MIN = -1 * 12;
    final float GRID_MAX = 1 * 12;
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
        MeshPartBuilder part = mb.part("circle", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse(color)));
        part.circle(radius, 32, c, new Vector3(0, 1, 0));
        model = mb.end();

        ModelInstance modelInstance = new ModelInstance(model);
        modelInstance.transform.setToTranslation(0, cz, 0);

        return modelInstance;
    }

    private ModelInstance createCylinder(float width, float height, float x, float y, float z, Color color) {
        ModelBuilder mb = new ModelBuilder();
        Model sf = mb.createCylinder(width, height, width, 32, new Material(ColorAttribute.createDiffuse(color), ColorAttribute.createSpecular(color), new BlendingAttribute(0.8f)), Usage.Position | Usage.Normal);
        ModelInstance modelInstance = new ModelInstance(sf);
        modelInstance.transform.setToTranslation(x, y, z);
        return modelInstance;
    }

    private ModelInstance createCircle(float radius, float thickness, Vector3 c, float cz, Color color) {

        Model model = null;
        ModelBuilder mb = new ModelBuilder();
        mb.begin();
        MeshPartBuilder part = mb.part("circle", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse(color)));
        part.ellipse(radius, radius, radius - thickness, radius - thickness, 32, c, new Vector3(0, 1, 0));
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

    public ModelInstance createPolygonBox(Color color, float width, float height, float length, float rotation, float x, float y, float z) {

        Vector3 corner000 = new Vector3(-width / 2, -height / 2, -length / 2);
        Vector3 corner010 = new Vector3(width / 2, -height / 2, -length / 2);
        Vector3 corner100 = new Vector3(-width / 2, -height / 2, length / 2);
        Vector3 corner110 = new Vector3(width / 2, -height / 2, length / 2);

        Vector3 corner001 = new Vector3(-width / 2, height / 2, -length / 2);
        Vector3 corner011 = new Vector3(width / 2, height / 2, -length / 2);
        Vector3 corner101 = new Vector3(-width / 2, height / 2, length / 2);
        Vector3 corner111 = new Vector3(width / 2, height / 2, length / 2);

        Material material = new Material(ColorAttribute.createDiffuse(color));
        ModelBuilder mb = new ModelBuilder();
        mb.begin();
        mb.part("box", GL30.GL_TRIANGLES, Usage.Position | Usage.Normal, material).box(corner000, corner010, corner100, corner110, corner001, corner011, corner101, corner111);
        Model model = mb.end();

        ModelInstance modelInstance = new ModelInstance(model);

        modelInstance.transform.rotate(new Vector3(0, 1, 0), rotation);
        modelInstance.transform.setTranslation(x, y, z);

        return modelInstance;
    }

    private ModelInstance getFigure(float x, float y, float z, float rotation) {
        ModelInstance instance = new ModelInstance(avatarModel, x, y, z);
        instance.nodes.get(0).scale.set(.01f, .01f, .01f);
        instance.transform.rotate(new Vector3(0, 1, 0), rotation);
        instance.calculateTransforms();
        return instance;
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        batch.dispose();
        stage.dispose();
        for (ModelInstance mi : floor) {
            mi.model.dispose();
        }
        for (ModelInstance mi : modelInstances) {
            mi.model.dispose();
        }
        font.dispose();
    }

    @Override
    public void finishTurn(int currentX, int currentY) {

    }

    @Override
    public void partyDeath() {

    }
    
    @Override
    public Vector3 getMapPixelCoords(int x, int y) {
        return null;
    }

    @Override
    public Vector3 getCurrentMapCoords() {
        return null;
    }

}
