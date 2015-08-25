package ultima;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import objects.BaseMap;
import objects.Creature;
import objects.Party.PartyMember;
import objects.Portal;
import objects.Tile;
import objects.TileSet;

import org.apache.commons.io.IOUtils;

import util.DungeonRoomTiledMapLoader;
import util.DungeonTileModelInstance;
import util.UltimaTiledMapLoader;
import util.Utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
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
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.UBJsonReader;

public class DungeonScreen extends BaseScreen {

    public Maps dngMap;
    private String dungeonFileName;
    public GameScreen gameScreen;

    public Environment environment;
    public ModelBatch modelBatch;
    private SpriteBatch batch;
    private DecalBatch decalBatch;

    public CameraInputController inputController;

    public PerspectiveCamera cam;
    public AssetManager assets;
    BitmapFont font;

    //3d models
    public Model fountainModel;
    public Model ladderModel;
    public Model chestModel;
    public Model orbModel;
    public Model altarModel;
    public Model blocksModel;

    boolean showMiniMap = true;

    boolean isTorchOn = true;
    private Vector3 vdll = new Vector3(.04f, .04f, .04f);
    private Vector3 nll2 = new Vector3(1f, 0.8f, 0.6f);
    private Vector3 nll = new Vector3(.96f, .58f, 0.08f);

    Model lightModel;
    Renderable pLight;
    PointLight fixedLight;
    float lightFactor;

    public static final int DUNGEON_MAP = 8;
    public DungeonTile[][][] dungeonTiles = new DungeonTile[DUNGEON_MAP][DUNGEON_MAP][DUNGEON_MAP];
    public DungeonRoom[] rooms = new DungeonRoom[64];//can have up to 64 rooms (abyss), 11x11 map grid
    public List<RoomLocater> locaters = new ArrayList<>();

    public List<DungeonTileModelInstance> modelInstances = new ArrayList<>();
    public List<ModelInstance> floor = new ArrayList<>();
    public List<ModelInstance> ceiling = new ArrayList<>();

    public static Texture MINI_MAP_TEXTURE;
    public static final int DIM = 11;
    public static final int OFST = DIM;
    public static final int MM_BKGRND_DIM = DIM * 8 + OFST * 2;
    public static final int xalignMM = Ultima4.SCREEN_WIDTH - MM_BKGRND_DIM - 10;
    public static final int yalignMM = 10;

    public int currentLevel = 0;
    public Vector3 currentPos;
    public Direction currentDir = Direction.EAST;

    public SecondaryInputProcessor sip;
    private Texture miniMap;
    private MiniMapIcon miniMapIcon;

    public DungeonScreen(Stage stage, GameScreen gameScreen, Maps map) {

        scType = ScreenType.DUNGEON;
        this.dngMap = map;
        this.dungeonFileName = map.getMap().getFname();
        this.gameScreen = gameScreen;
        this.stage = stage;
        sip = new SecondaryInputProcessor(this, stage);

        init();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputMultiplexer(this, stage));
        GameScreen.context.getParty().addObserver(this);
    }

    @Override
    public void hide() {
        GameScreen.context.getParty().deleteObserver(this);
    }

    public void init() {

        assets = new AssetManager();
        assets.load("assets/graphics/dirt.png", Texture.class);
        assets.load("assets/graphics/map.png", Texture.class);
        assets.load("assets/graphics/Stone_Masonry.jpg", Texture.class);
        assets.load("assets/graphics/door.png", Texture.class);
        assets.load("assets/graphics/mortar.png", Texture.class);
        assets.load("assets/graphics/rock.png", Texture.class);

        assets.update(2000);
	
        //convert the collada dae format to the g3db format (do not use the obj format)
        //C:\Users\Paul\Desktop\blender>fbx-conv-win32.exe -o G3DB ./Chess/pawn.dae ./pawn.g3db
        ModelLoader<?> gloader = new G3dModelLoader(new UBJsonReader(), new ClasspathFileHandleResolver());
        fountainModel = gloader.loadModel(Gdx.files.internal("assets/graphics/fountain2.g3db"));
        ladderModel = gloader.loadModel(Gdx.files.internal("assets/graphics/ladder.g3db"));
        chestModel = gloader.loadModel(Gdx.files.internal("assets/graphics/chest.g3db"));
        orbModel = gloader.loadModel(Gdx.files.internal("assets/graphics/orb.g3db"));
        altarModel = gloader.loadModel(Gdx.files.internal("assets/graphics/altar.g3db"));
	//blocksModel = gloader.loadModel(Gdx.files.internal("assets/graphics/box.g3db"));

        Pixmap pixmap = new Pixmap(MM_BKGRND_DIM, MM_BKGRND_DIM, Format.RGBA8888);
        pixmap.setColor(0.8f, 0.7f, 0.5f, .8f);
        pixmap.fillRectangle(0, 0, MM_BKGRND_DIM, MM_BKGRND_DIM);
        MINI_MAP_TEXTURE = new Texture(pixmap);
        pixmap.dispose();

        font = new BitmapFont();
        font.setColor(Color.WHITE);

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.05f, 0.05f, 0.05f, 1f));
        //environment.set(new ColorAttribute(ColorAttribute.Fog, 0.13f, 0.13f, 0.13f, 1f));

        fixedLight = new PointLight().set(1f, 0.8f, 0.6f, 4f, 4f, 4f, 5f);
        environment.add(fixedLight);

        modelBatch = new ModelBatch();
        batch = new SpriteBatch();

        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.near = 0.1f;
        cam.far = 1000f;
        cam.update();

        decalBatch = new DecalBatch(new CameraGroupStrategy(cam));

        inputController = new CameraInputController(cam);
        inputController.rotateLeftKey = inputController.rotateRightKey = inputController.forwardKey = inputController.backwardKey = 0;
        inputController.translateUnits = 30f;

        ModelBuilder builder = new ModelBuilder();
        lightModel = builder.createSphere(.1f, .1f, .1f, 10, 10, new Material(ColorAttribute.createDiffuse(1, 1, 1, 1)), Usage.Position);
        lightModel.nodes.get(0).parts.get(0).setRenderable(pLight = new Renderable());

        for (int x = 0; x < 12; x++) {
            for (int y = 0; y < 12; y++) {
                Model sf = builder.createBox(1, 1, 1, new Material(TextureAttribute.createDiffuse(assets.get("assets/graphics/rock.png", Texture.class))), Usage.Position | Usage.TextureCoordinates | Usage.Normal);
                floor.add(new ModelInstance(sf, new Vector3(x - 1.5f, -.5f, y - 1.5f)));
            }
        }
        for (int x = 0; x < 12; x++) {
            for (int y = 0; y < 12; y++) {
                Model sf = builder.createBox(1, 1, 1, new Material(TextureAttribute.createDiffuse(assets.get("assets/graphics/dirt.png", Texture.class))), Usage.Position | Usage.TextureCoordinates | Usage.Normal);
                ceiling.add(new ModelInstance(sf, new Vector3(x - 1.5f, 1.5f, y - 1.5f)));
            }
        }

        try {
            InputStream is = new FileInputStream("assets/data/" + dungeonFileName.toLowerCase());
            byte[] bytes = IOUtils.toByteArray(is);

            int pos = 0;
            for (int i = 0; i < DUNGEON_MAP; i++) {
                for (int y = 0; y < DUNGEON_MAP; y++) {
                    for (int x = 0; x < DUNGEON_MAP; x++) {
                        int index = bytes[pos] & 0xff;
                        pos++;
                        DungeonTile tile = DungeonTile.getTileByValue(index);
                        dungeonTiles[i][x][y] = tile;
                        addBlock(i, tile, x + .5f, .5f, y + .5f);
                    }
                }
            }

            //rooms
            pos = 0x200;
            for (int i = 0; i < rooms.length; i++) {
                if (pos >= bytes.length) {
                    continue;
                }
                rooms[i] = new DungeonRoom(bytes, pos);
                pos = pos + 256;
            }

            for (int i = 0; i < DUNGEON_MAP; i++) {
                for (int y = 0; y < DUNGEON_MAP; y++) {
                    for (int x = 0; x < DUNGEON_MAP; x++) {
                        DungeonTile tile = dungeonTiles[i][x][y];
                        if (tile.getValue() >= 208 && tile.getValue() <= 223) {
                            DungeonRoom room = rooms[tile.getValue() - 207 - 1];
                            if (dngMap == Maps.ABYSS) {
                                if (i == 0 || i == 1) {
                                    //nothing
                                } else if (i == 2 || i == 3) {
                                    room = rooms[tile.getValue() - 207 + 16 - 1];
                                } else if (i == 4 || i == 5) {
                                    room = rooms[tile.getValue() - 207 + 32 - 1];
                                } else if (i == 6 || i == 7) {
                                    room = rooms[tile.getValue() - 207 + 48 - 1];
                                }
                            }

			    //System.out.println(dngMap.name() + " " + tile.toString() + " " + x + "," + y + ", " + i);
                            //for (int j=0;j<4;j++) if (room.triggers[j].tile.getIndex() != 0) System.out.println(room.triggers[j].toString());
                            if (room.hasAltar) {
                                if (x == 1) {
                                    room.altarRoomVirtue = BaseVirtue.TRUTH;
                                } else if (x == 7) {
                                    room.altarRoomVirtue = BaseVirtue.COURAGE;
                                } else {
                                    room.altarRoomVirtue = BaseVirtue.LOVE;
                                }
                            }

                            RoomLocater loc = new RoomLocater(x, y, i, room);
                            locaters.add(loc);
                        }
                    }
                }
            }

            miniMapIcon = new MiniMapIcon();
            miniMapIcon.setOrigin(5, 5);

            stage = new Stage();
            stage.addActor(miniMapIcon);

            setStartPosition();

            cam.position.set(currentPos);
            cam.lookAt(currentPos.x + 1, currentPos.y, currentPos.z);

	    //duplicate some of the outer edge tiles around the outside 
            //so that the wrapping is not so black hole on the sides
            //i went 2 layers duplicated on each edges + the corners
            for (int i = 0; i < DUNGEON_MAP; i++) {
                {
                    int y = 0;
                    for (int x = 0; x < DUNGEON_MAP; x++) {//bottom across the top
                        DungeonTile tile = dungeonTiles[i][x][y + DUNGEON_MAP - 1];
                        addBlock(i, tile, x + .5f, .5f, y - .5f);

                        tile = dungeonTiles[i][x][y + DUNGEON_MAP - 2];
                        addBlock(i, tile, x + .5f, .5f, y - 1.5f);
                    }
                    for (int x = 0; x < DUNGEON_MAP; x++) {//top across the bottom
                        DungeonTile tile = dungeonTiles[i][x][y];
                        addBlock(i, tile, x + .5f, .5f, y + .5f + DUNGEON_MAP);

                        tile = dungeonTiles[i][x][y + 1];
                        addBlock(i, tile, x + .5f, .5f, y + .5f + DUNGEON_MAP + 1);
                    }

                }
                {
                    int x = 0;
                    for (int y = 0; y < DUNGEON_MAP; y++) {
                        DungeonTile tile = dungeonTiles[i][x][y];
                        addBlock(i, tile, x + .5f + DUNGEON_MAP, .5f, y + .5f);

                        tile = dungeonTiles[i][x + 1][y];
                        addBlock(i, tile, x + .5f + DUNGEON_MAP + 1, .5f, y + .5f);
                    }
                    for (int y = 0; y < DUNGEON_MAP; y++) {
                        DungeonTile tile = dungeonTiles[i][x + DUNGEON_MAP - 1][y];
                        addBlock(i, tile, x - .5f, .5f, y + .5f);

                        tile = dungeonTiles[i][x + DUNGEON_MAP - 2][y];
                        addBlock(i, tile, x - 1.5f, .5f, y + .5f);
                    }
                }

                {//copy bottom right corner to the top left corner
                    DungeonTile tile = dungeonTiles[i][DUNGEON_MAP - 1][DUNGEON_MAP - 1];
                    addBlock(i, tile, -.5f, .5f, -.5f);
                }

                {//copy bottom left corner to the top right corner
                    DungeonTile tile = dungeonTiles[i][0][DUNGEON_MAP - 1];
                    addBlock(i, tile, DUNGEON_MAP + .5f, .5f, -.5f);
                }

                {//copy top right corner to the bottom left corner
                    DungeonTile tile = dungeonTiles[i][DUNGEON_MAP - 1][0];
                    addBlock(i, tile, -.5f, .5f, DUNGEON_MAP + .5f);
                }

                {//copy top left corner to the bottom right corner
                    DungeonTile tile = dungeonTiles[i][0][0];
                    addBlock(i, tile, DUNGEON_MAP + .5f, .5f, DUNGEON_MAP + .5f);
                }

            }

            createMiniMap();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * finds the up ladder on the first level and puts you there
     */
    private void setStartPosition() {
        for (int y = 0; y < DUNGEON_MAP; y++) {
            for (int x = 0; x < DUNGEON_MAP; x++) {
                DungeonTile tile = dungeonTiles[currentLevel][x][y];
                if (tile == DungeonTile.NOTHING && currentPos == null) {
                    currentPos = new Vector3(x + .5f, .5f, y + .5f);
                }
                if (tile == DungeonTile.LADDER_UP) {
                    currentPos = new Vector3(x + .5f, .5f, y + .5f);
                }
            }
        }

        createMiniMap();
        moveMiniMapIcon();

    }

    public void restoreSaveGameLocation(int x, int y, int z, Direction orientation) {

        currentPos = new Vector3(x + .5f, .5f, y + .5f);
        cam.position.set(currentPos);
        currentDir = orientation;
        currentLevel = z;

        if (currentDir == Direction.EAST) {
            cam.lookAt(currentPos.x + 1, currentPos.y, currentPos.z);
        } else if (currentDir == Direction.WEST) {
            cam.lookAt(currentPos.x - 1, currentPos.y, currentPos.z);
        } else if (currentDir == Direction.NORTH) {
            cam.lookAt(currentPos.x, currentPos.y, currentPos.z - 1);
        } else if (currentDir == Direction.SOUTH) {
            cam.lookAt(currentPos.x, currentPos.y, currentPos.z + 1);
        }

        createMiniMap();
        moveMiniMapIcon();
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        batch.dispose();
        decalBatch.dispose();
        MINI_MAP_TEXTURE.dispose();
        miniMapIcon.dispose();
        stage.dispose();
        for (ModelInstance mi : floor) {
            mi.model.dispose();
        }
        for (ModelInstance mi : ceiling) {
            mi.model.dispose();
        }
        font.dispose();
    }

    @Override
    public void render(float delta) {

        cam.update();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);

        lightFactor += Gdx.graphics.getDeltaTime();
        float lightSize = 4.75f + 0.25f * (float) Math.sin(lightFactor) + .2f * MathUtils.random();

        Vector3 ll = isTorchOn ? nll : vdll;
        ll = isTorchOn ? nll2 : vdll;
        fixedLight.set(ll.x, ll.y, ll.z, currentPos.x, currentPos.y + .35f, currentPos.z, lightSize);
        ((ColorAttribute) pLight.material.get(ColorAttribute.Diffuse)).color.set(fixedLight.color);
        pLight.worldTransform.setTranslation(fixedLight.position);

        modelBatch.begin(cam);

        modelBatch.render(pLight);

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

        for (Creature cr : dngMap.getMap().getCreatures()) {
            decalBatch.add(cr.getDecal());
        }
        decalBatch.flush();

        drawHUD();

        stage.act();
        stage.draw();

    }

    public void addBlock(int level, DungeonTile tile, float tx, float ty, float tz) {
        ModelBuilder builder = new ModelBuilder();
        if (tile == DungeonTile.WALL) {
            Model model = builder.createBox(1, 1, 1, new Material(TextureAttribute.createDiffuse(assets.get("assets/graphics/mortar.png", Texture.class))), Usage.Position | Usage.Normal | Usage.TextureCoordinates);
            ModelInstance instance = new ModelInstance(model, tx, ty, tz);
            //rotate so the texture is aligned right
            instance.transform.setFromEulerAngles(0, 0, 90).trn(tx, ty, tz);
            DungeonTileModelInstance in = new DungeonTileModelInstance(instance, tile, level);
            modelInstances.add(in);
        } else if (tile.getValue() >= 144 && tile.getValue() <= 148) {
            ModelInstance instance = new ModelInstance(fountainModel, tx - .15f, 0, tz + .2f);
            instance.nodes.get(0).scale.set(.010f, .010f, .010f);
            instance.calculateTransforms();
            DungeonTileModelInstance in = new DungeonTileModelInstance(instance, tile, level);
            modelInstances.add(in);
        } else if (tile.getValue() >= 10 && tile.getValue() <= 48) {
            ModelInstance instance = new ModelInstance(ladderModel, tx, 0, tz);
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
            ModelInstance instance = new ModelInstance(chestModel, tx, 0, tz);
            instance.nodes.get(0).scale.set(.010f, .010f, .010f);
            instance.calculateTransforms();
            DungeonTileModelInstance in = new DungeonTileModelInstance(instance, tile, level);
            modelInstances.add(in);
            in.x = (int) tx;
            in.y = (int) tz;
        } else if (tile == DungeonTile.ORB) {
            ModelInstance instance = new ModelInstance(orbModel, tx, .5f, tz);
            instance.nodes.get(0).scale.set(.0025f, .0025f, .0025f);
            instance.calculateTransforms();
            DungeonTileModelInstance in = new DungeonTileModelInstance(instance, tile, level);
            in.x = (int) tx;
            in.y = (int) tz;
            modelInstances.add(in);
        } else if (tile == DungeonTile.ALTAR) {
            ModelInstance instance = new ModelInstance(altarModel, tx, 0, tz);
            instance.nodes.get(0).scale.set(.0040f, .0040f, .0040f);
            instance.calculateTransforms();
            DungeonTileModelInstance in = new DungeonTileModelInstance(instance, tile, level);
            in.x = (int) tx;
            in.y = (int) tz;
            modelInstances.add(in);
        } else if (tile.getValue() >= 160 && tile.getValue() <= 163) {
            Color c = Color.GREEN;
            if (tile == DungeonTile.FIELD_ENERGY) {
                c = Color.BLUE;
            }
            if (tile == DungeonTile.FIELD_FIRE) {
                c = Color.RED;
            }
            if (tile == DungeonTile.FIELD_SLEEP) {
                c = Color.PURPLE;
            }
            Model model = builder.createBox(1, 1, 1, new Material(ColorAttribute.createDiffuse(c), ColorAttribute.createSpecular(c), new BlendingAttribute(0.7f)), Usage.Position | Usage.Normal);
            ModelInstance instance = new ModelInstance(model, tx, .5f, tz);
            DungeonTileModelInstance in = new DungeonTileModelInstance(instance, tile, level);
            modelInstances.add(in);
            in.x = (int) tx;
            in.y = (int) tz;
        } else if (tile.getValue() >= 208 && tile.getValue() <= 223) { //room indicator
            Model model = builder.createBox(1, 1, 1, new Material(ColorAttribute.createDiffuse(Color.DARK_GRAY), ColorAttribute.createSpecular(Color.DARK_GRAY), new BlendingAttribute(0.6f)), Usage.Position | Usage.Normal);
            ModelInstance instance = new ModelInstance(model, tx, .5f, tz);
            DungeonTileModelInstance in = new DungeonTileModelInstance(instance, tile, level);
            modelInstances.add(in);
        } else if (tile == DungeonTile.DOOR || tile == DungeonTile.SECRET_DOOR) {
            Model model = builder.createBox(1, 1, 1, new Material(TextureAttribute.createDiffuse(assets.get("assets/graphics/mortar.png", Texture.class))), Usage.Position | Usage.TextureCoordinates | Usage.Normal);
            ModelInstance instance = new ModelInstance(model, tx, ty, tz);
            instance.transform.setFromEulerAngles(0, 0, 90).trn(tx, ty, tz);
            DungeonTileModelInstance in = new DungeonTileModelInstance(instance, tile, level);
            modelInstances.add(in);

            Material matDoor = null;
            if (tile == DungeonTile.DOOR) {
                matDoor = new Material(TextureAttribute.createDiffuse(assets.get("assets/graphics/door.png", Texture.class)));
            } else {
                matDoor = new Material(new Material(ColorAttribute.createDiffuse(Color.DARK_GRAY), ColorAttribute.createSpecular(Color.DARK_GRAY), new BlendingAttribute(0.3f)));
            }

            model = builder.createBox(1.04f, .85f, .6f, matDoor, Usage.Position | Usage.TextureCoordinates | Usage.Normal);
            instance = new ModelInstance(model, tx, .4f, tz);
            in = new DungeonTileModelInstance(instance, tile, level);
            modelInstances.add(in);

            model = builder.createBox(.6f, .85f, 1.04f, matDoor, Usage.Position | Usage.TextureCoordinates | Usage.Normal);
            instance = new ModelInstance(model, tx, .4f, tz);
            in = new DungeonTileModelInstance(instance, tile, level);
            modelInstances.add(in);
        }
    }

    public void createMiniMap() {

        if (miniMap != null) {
            miniMap.dispose();
        }

        Pixmap pixmap = new Pixmap(MM_BKGRND_DIM, MM_BKGRND_DIM, Format.RGBA8888);
        for (int y = 0; y < DUNGEON_MAP; y++) {
            for (int x = 0; x < DUNGEON_MAP; x++) {
                DungeonTile tile = dungeonTiles[currentLevel][x][y];
                if (tile == DungeonTile.WALL || tile == DungeonTile.SECRET_DOOR) {
                    pixmap.setColor(0.3f, 0.3f, 0.3f, 0.7f);
                    pixmap.fillRectangle(OFST + (x * DIM), OFST + (y * DIM), DIM, DIM);
                } else if (tile == DungeonTile.DOOR) {
                    pixmap.setColor(0.6f, 0.6f, 0.6f, 0.7f);
                    pixmap.fillRectangle(OFST + (x * DIM), OFST + (y * DIM), DIM, DIM);
                } else if (tile.getValue() >= 208 && tile.getValue() <= 223) { //room indicator
                    pixmap.setColor(0.36f, 0.04f, 0.04f, 0.7f);
                    pixmap.fillRectangle(OFST + (x * DIM), OFST + (y * DIM), DIM, DIM);
                } else if (tile.getValue() >= 160 && tile.getValue() <= 163) { //fields
                    Color c = Color.GREEN;
                    if (tile == DungeonTile.FIELD_ENERGY) {
                        c = Color.BLUE;
                    }
                    if (tile == DungeonTile.FIELD_FIRE) {
                        c = Color.RED;
                    }
                    if (tile == DungeonTile.FIELD_SLEEP) {
                        c = Color.PURPLE;
                    }
                    pixmap.setColor(c);
                    pixmap.fillRectangle(OFST + (x * DIM), OFST + (y * DIM), DIM, DIM);
                } else if (tile.getValue() >= 10 && tile.getValue() <= 48) {
                    drawLadderTriangle(tile, pixmap, x, y); //ladders
                }
            }
        }

        miniMap = new Texture(pixmap);
        pixmap.dispose();
    }

    private void drawLadderTriangle(DungeonTile tile, Pixmap pixmap, int x, int y) {
        int cx = OFST + (x * DIM);
        int cy = OFST + (y * DIM);
        pixmap.setColor(Color.YELLOW);
        if (tile == DungeonTile.LADDER_DOWN) {
            pixmap.fillTriangle(cx + 2, cy + 2, cx + 6, cy + 9, cx + 9, cy + 2);
        } else if (tile == DungeonTile.LADDER_UP) {
            pixmap.fillTriangle(cx + 2, cy + 9, cx + 6, cy + 2, cx + 9, cy + 9);
        } else if (tile == DungeonTile.LADDER_UP_DOWN) {
            pixmap.fillTriangle(cx + 2, cy + 6, cx + 6, cy + 2, cx + 9, cy + 6);
            pixmap.fillTriangle(cx + 2, cy + 6, cx + 6, cy + 9, cx + 9, cy + 9);
        }
    }

    private Texture createMiniMapIcon(Direction dir) {
        Pixmap pixmap = new Pixmap(DIM, DIM, Format.RGBA8888);
        pixmap.setColor(1f, 0f, 0f, 1f);
        if (dir == Direction.EAST) {
            pixmap.fillTriangle(2, 2, 2, 9, 9, 6);
        } else if (dir == Direction.NORTH) {
            pixmap.fillTriangle(2, 9, 6, 2, 9, 9);
        } else if (dir == Direction.WEST) {
            pixmap.fillTriangle(9, 2, 2, 6, 9, 9);
        } else if (dir == Direction.SOUTH) {
            pixmap.fillTriangle(2, 2, 6, 9, 9, 2);
        }
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    public class MiniMapIcon extends Actor {

        Texture north;
        Texture south;
        Texture east;
        Texture west;

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
            if (!showMiniMap) {
                return;
            }
            Color color = getColor();
            batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
            Texture t = north;
            if (currentDir == Direction.EAST) {
                t = east;
            }
            if (currentDir == Direction.WEST) {
                t = west;
            }
            if (currentDir == Direction.SOUTH) {
                t = south;
            }
            batch.draw(t, getX(), getY());
        }

        public void dispose() {
            north.dispose();
            east.dispose();
            west.dispose();
            south.dispose();
        }
    }

    public void moveMiniMapIcon() {
        miniMapIcon.setX(xalignMM + OFST + (Math.round(currentPos.x) - 1) * DIM);
        miniMapIcon.setY(yalignMM + MM_BKGRND_DIM - OFST - (Math.round(currentPos.z)) * DIM);
    }

    public void drawHUD() {

        batch.begin();

        if (showMiniMap) {
            batch.draw(MINI_MAP_TEXTURE, xalignMM, yalignMM);
            batch.draw(miniMap, xalignMM, yalignMM);
        }

        Ultima4.hud.render(batch, GameScreen.context.getParty());

        font.draw(batch, "Level " + (currentLevel + 1), Ultima4.SCREEN_WIDTH / 2 - 20, Ultima4.SCREEN_HEIGHT - 3);

        if (showZstats > 0) {
            GameScreen.context.getParty().getSaveGame().renderZstats(showZstats, font, batch, Ultima4.SCREEN_HEIGHT);
        }

        batch.end();
    }

    public void enterRoom(RoomLocater loc, Direction entryDir) {

        if (loc == null) {
            return;
        }
        Maps contextMap = Maps.get(dngMap.getId());

        TiledMap tiledMap = new DungeonRoomTiledMapLoader(loc.room, entryDir, GameScreen.standardAtlas).load();

        BaseMap baseMap = new BaseMap();
        baseMap.setTiles(loc.room.tiles);
        baseMap.setWidth(11);
        baseMap.setHeight(11);
        baseMap.setType(MapType.dungeon);
        baseMap.setPortals(dngMap.getMap().getPortals(loc.x, loc.y, loc.z));

        CombatScreen sc = new CombatScreen(this, GameScreen.context, contextMap, baseMap, tiledMap, null, GameScreen.creatures, GameScreen.enhancedAtlas, GameScreen.standardAtlas);

        if (loc.room.hasAltar) {
            sc.log("The Altar Room of " + loc.room.altarRoomVirtue.toString());
        }

        MapLayer mLayer = tiledMap.getLayers().get("Monster Positions");
        Iterator<MapObject> iter = mLayer.getObjects().iterator();
        while (iter.hasNext()) {
            MapObject obj = iter.next();
            int tile = (Integer) obj.getProperties().get("tile");
            int startX = (Integer) obj.getProperties().get("startX");
            int startY = (Integer) obj.getProperties().get("startY");

            if (tile == 0) {
                continue;
            }

            Tile t = GameScreen.baseTileSet.getTileByIndex(tile);

            Creature c = GameScreen.creatures.getInstance(CreatureType.get(t.getName()), GameScreen.enhancedAtlas, GameScreen.standardAtlas);

            c.currentX = startX;
            c.currentY = startY;
            c.currentPos = sc.getMapPixelCoords(startX, startY);

            baseMap.addCreature(c);
        }

        mainGame.setScreen(sc);

    }

    public void battleWandering(Creature cr, int x, int y) {
        if (cr == null) {
            return;
        }
        Maps contextMap = Maps.get(dngMap.getId());
        DungeonTile tile = dungeonTiles[currentLevel][x][y];
        TiledMap tmap = new UltimaTiledMapLoader(tile.getCombatMap(), GameScreen.standardAtlas, 11, 11, GameScreen.TILE_DIM, GameScreen.TILE_DIM).load();
        GameScreen.context.setCurrentTiledMap(tmap);
        CombatScreen sc = new CombatScreen(this, GameScreen.context, contextMap, tile.getCombatMap().getMap(), tmap, cr.getTile(), GameScreen.creatures, GameScreen.enhancedAtlas, GameScreen.standardAtlas);
        mainGame.setScreen(sc);
        currentEncounter = cr;
    }

    @Override
    public void partyDeath() {
        mainGame.setScreen(new DeathScreen(gameScreen, GameScreen.context.getParty()));
        gameScreen.loadNextMap(Maps.CASTLE_OF_LORD_BRITISH_2, REVIVE_CASTLE_X, REVIVE_CASTLE_Y);
    }

    @Override
    public void endCombat(boolean isWon, BaseMap combatMap) {

        mainGame.setScreen(this);

        if (isWon) {

            if (currentEncounter != null) {
                log("Victory!");
                GameScreen.context.getParty().adjustKarma(KarmaAction.KILLED_EVIL);
                int x = (Math.round(currentPos.x) - 1);
                int y = (Math.round(currentPos.z) - 1);
                /* add a chest, if the creature leaves one */
                if (!currentEncounter.getNochest() && dungeonTiles[currentLevel][x][y] == DungeonTile.NOTHING) {
                    ModelInstance instance = new ModelInstance(chestModel, x + .5f, 0, y + .5f);
                    instance.nodes.get(0).scale.set(.010f, .010f, .010f);
                    instance.calculateTransforms();
                    DungeonTileModelInstance in = new DungeonTileModelInstance(instance, DungeonTile.CHEST, currentLevel);
                    in.x = x;
                    in.y = y;
                    modelInstances.add(in);
                    dungeonTiles[currentLevel][x][y] = DungeonTile.CHEST;
                }
            }

        } else {
            if (combatMap.getType() == MapType.combat && GameScreen.context.getParty().didAnyoneFlee()) {
                log("Battle is lost!");
                //no flee penalty in dungeons
            } else if (!GameScreen.context.getParty().isAnyoneAlive()) {
                partyDeath();
            }
        }

        if (currentEncounter != null) {
            dngMap.getMap().removeCreature(currentEncounter);
            currentEncounter = null;
        }

        //if exiting dungeon rooms, move out of the room with orientation to next coordinate
        if (combatMap.getType() == MapType.dungeon) {
            Direction exitDirection = GameScreen.context.getParty().getActivePartyMember().combatMapExitDirection;
            if (exitDirection != null) {
                currentDir = exitDirection;

                int x = (Math.round(currentPos.x) - 1);
                int y = (Math.round(currentPos.z) - 1);

                //check for portal to another dungeon
                for (Portal p : combatMap.getPortals()) {
                    if (p.getX() == x && p.getY() == y && p.getExitDirection() == exitDirection) {
                        Maps m = Maps.get(p.getDestmapid());
                        if (m == dngMap) {
                            break;
                        }
                        log("Entering " + m.getLabel() + "!");
                        DungeonScreen sc = new DungeonScreen(stage, this.gameScreen, m);
                        sc.restoreSaveGameLocation(p.getStartx(), p.getStarty(), p.getStartlevel(), currentDir);
                        mainGame.setScreen(sc);
                        this.gameScreen.newMapPixelCoords = this.gameScreen.getMapPixelCoords(p.getRetroActiveDest().getX(), p.getRetroActiveDest().getY());
                        this.gameScreen.changeMapPosition = true;
                        return;
                    }
                }

                if (exitDirection == Direction.EAST) {
                    x = x + 1;
                    if (x > 7) {
                        x = 0;
                    }
                } else if (exitDirection == Direction.WEST) {
                    x = x - 1;
                    if (x < 0) {
                        x = 7;
                    }
                } else if (exitDirection == Direction.NORTH) {
                    y = y - 1;
                    if (y < 0) {
                        y = 7;
                    }
                } else if (exitDirection == Direction.SOUTH) {
                    y = y + 1;
                    if (y > 7) {
                        y = 0;
                    }
                }

                DungeonTile tile = dungeonTiles[currentLevel][x][y];
                if (tile != DungeonTile.WALL) {
                    currentPos = new Vector3(x + .5f, .5f, y + .5f);
                    cam.position.set(currentPos);
                    if (currentDir == Direction.EAST) {
                        cam.lookAt(currentPos.x + 1, currentPos.y, currentPos.z);
                    } else if (currentDir == Direction.WEST) {
                        cam.lookAt(currentPos.x - 1, currentPos.y, currentPos.z);
                    } else if (currentDir == Direction.NORTH) {
                        cam.lookAt(currentPos.x, currentPos.y, currentPos.z - 1);
                    } else if (currentDir == Direction.SOUTH) {
                        cam.lookAt(currentPos.x, currentPos.y, currentPos.z + 1);
                    }
                    checkTrap(tile, x, y);
                    moveMiniMapIcon();
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
    public boolean keyUp(int keycode) {

        int x = (Math.round(currentPos.x) - 1);
        int y = (Math.round(currentPos.z) - 1);
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
            setCreatureRotations();
            return false;

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
            setCreatureRotations();
            return false;

        } else if (keycode == Keys.UP) {

            //forward
            if (currentDir == Direction.EAST) {
                x = x + 1;
                if (x > 7) {
                    x = 0;
                }
            } else if (currentDir == Direction.WEST) {
                x = x - 1;
                if (x < 0) {
                    x = 7;
                }
            } else if (currentDir == Direction.NORTH) {
                y = y - 1;
                if (y < 0) {
                    y = 7;
                }
            } else if (currentDir == Direction.SOUTH) {
                y = y + 1;
                if (y > 7) {
                    y = 0;
                }
            }

            tile = dungeonTiles[currentLevel][x][y];
            if (tile != DungeonTile.WALL) {
                currentPos = new Vector3(x + .5f, .5f, y + .5f);
                cam.position.set(currentPos);
                if (currentDir == Direction.EAST) {
                    cam.lookAt(currentPos.x + 1, currentPos.y, currentPos.z);
                } else if (currentDir == Direction.WEST) {
                    cam.lookAt(currentPos.x - 1, currentPos.y, currentPos.z);
                } else if (currentDir == Direction.NORTH) {
                    cam.lookAt(currentPos.x, currentPos.y, currentPos.z - 1);
                } else if (currentDir == Direction.SOUTH) {
                    cam.lookAt(currentPos.x, currentPos.y, currentPos.z + 1);
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
                return false;
            }

        } else if (keycode == Keys.DOWN) {

            //backwards
            if (currentDir == Direction.EAST) {
                x = x - 1;
                if (x < 0) {
                    x = 7;
                }
            } else if (currentDir == Direction.WEST) {
                x = x + 1;
                if (x > 7) {
                    x = 0;
                }
            } else if (currentDir == Direction.NORTH) {
                y = y + 1;
                if (y > 7) {
                    y = 0;
                }
            } else if (currentDir == Direction.SOUTH) {
                y = y - 1;
                if (y < 0) {
                    y = 7;
                }
            }
            tile = dungeonTiles[currentLevel][x][y];
            if (tile != DungeonTile.WALL) {
                currentPos = new Vector3(x + .5f, .5f, y + .5f);
                cam.position.set(currentPos);
                if (currentDir == Direction.EAST) {
                    cam.lookAt(currentPos.x + 1, currentPos.y, currentPos.z);
                } else if (currentDir == Direction.WEST) {
                    cam.lookAt(currentPos.x - 1, currentPos.y, currentPos.z);
                } else if (currentDir == Direction.NORTH) {
                    cam.lookAt(currentPos.x, currentPos.y, currentPos.z - 1);
                } else if (currentDir == Direction.SOUTH) {
                    cam.lookAt(currentPos.x, currentPos.y, currentPos.z + 1);
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
                return false;
            }

        } else if (keycode == Keys.K) {
            if (tile == DungeonTile.LADDER_UP || tile == DungeonTile.LADDER_UP_DOWN) {
                currentLevel--;
                if (currentLevel < 0) {
                    currentLevel = 0;
                    if (dngMap == Maps.HYTHLOTH) {
                        if (GameScreen.context.getParty().getSaveGame().balloonfound == 0) {
                            gameScreen.addBalloonActor(233, 242);
                            GameScreen.context.getParty().getSaveGame().balloonx = 233;
                            GameScreen.context.getParty().getSaveGame().balloony = 242;
                        }
                        GameScreen.context.getParty().getSaveGame().balloonfound = 1;
                    }
                    if (mainGame != null) {
                        mainGame.setScreen(gameScreen);
                        dispose();
                    }
                } else {
                    createMiniMap();
                }
            }
            return false;

        } else if (keycode == Keys.D) {
            if (tile == DungeonTile.LADDER_DOWN || tile == DungeonTile.LADDER_UP_DOWN) {
                currentLevel++;
                if (currentLevel > DUNGEON_MAP) {
                    currentLevel = DUNGEON_MAP;
                } else {
                    createMiniMap();
                }
            }
            return false;

        } else if (keycode == Keys.N) {
            log("New Order:");
            log("exhange #:");
            NewOrderInputAdapter noia = new NewOrderInputAdapter(this);
            Gdx.input.setInputProcessor(noia);
            return false;

        } else if (keycode == Keys.Q) {
            GameScreen.context.saveGame(x, y, currentLevel, currentDir, dngMap);
            log("Saved Game.");
            return false;

        } else if (keycode == Keys.C) {
            log("Cast Spell: ");
            log("Who casts (1-8): ");
            Gdx.input.setInputProcessor(new SpellInputProcessor(this, stage, x, y, null));

        } else if (keycode == Keys.I) {

            isTorchOn = !isTorchOn;

        } else if (keycode == Keys.G || keycode == Keys.R || keycode == Keys.W) {
            log("Which party member?");
            Gdx.input.setInputProcessor(sip);
            sip.setinitialKeyCode(keycode, tile, x, y);

        } else if (keycode == Keys.H) {
            CombatScreen.holeUp(this.dngMap, x, y, this, GameScreen.context, GameScreen.creatures, GameScreen.standardAtlas, GameScreen.enhancedAtlas);
            return false;

        } else if (keycode == Keys.V) {
            showMiniMap = !showMiniMap;
        } else if (keycode == Keys.M) {

            new MixtureDialog(GameScreen.context.getParty(), this, stage, skin).show();

        } else if (keycode == Keys.S) {
            if (tile == DungeonTile.ALTAR) {
                log("Search Altar");
                ItemMapLabels l = dngMap.getMap().searchLocation(this, GameScreen.context.getParty(), x, y, currentLevel);
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
            }

        } else if (keycode == Keys.U) {
            if (dngMap == Maps.ABYSS && tile == DungeonTile.ALTAR) {
                log("Use which item?");
                log("");
                AbyssInputAdapter aia = new AbyssInputAdapter(x, y);
                Gdx.input.setInputProcessor(aia);
                return false;
            }

        } else if (keycode == Keys.Z) {
            showZstats = showZstats + 1;
            if (showZstats >= STATS_PLAYER1 && showZstats <= STATS_PLAYER8) {
                if (showZstats > GameScreen.context.getParty().getMembers().size()) {
                    showZstats = STATS_WEAPONS;
                }
            }
            if (showZstats > STATS_SPELLS) {
                showZstats = STATS_NONE;
            }
            return false;

        } else {
            log("Pass");
        }

        finishTurn(x, y);

        return false;
    }

    @Override
    public void finishTurn(int currentX, int currentY) {
        GameScreen.context.getAura().passTurn();

        creatureCleanup(currentX, currentY);

        if (checkRandomDungeonCreatures()) {
            spawnDungeonCreature(null, currentX, currentY);
        }

        moveDungeonCreatures(this, currentX, currentY);
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
        if (index >= GameScreen.context.getParty().getMembers().size()) {
            return;
        }
        PartyMember pm = GameScreen.context.getParty().getMember(index);
        int x = (Math.round(currentPos.x) - 1);
        int y = (Math.round(currentPos.z) - 1);

        int stats = 0;
        int damage = 0;

        switch (dngMap) {
            case DECEIT:
                stats = STATSBONUS_INT;
                break;
            case DESPISE:
                stats = STATSBONUS_DEX;
                break;
            case DESTARD:
                stats = STATSBONUS_STR;
                break;
            case WRONG:
                stats = STATSBONUS_INT | STATSBONUS_DEX;
                break;
            case COVETOUS:
                stats = STATSBONUS_DEX | STATSBONUS_STR;
                break;
            case SHAME:
                stats = STATSBONUS_INT | STATSBONUS_STR;
                break;
            case HYTHLOTH:
                stats = STATSBONUS_INT | STATSBONUS_DEX | STATSBONUS_STR;
                break;
            default:
                break;
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
            if (dmi.getTile() == DungeonTile.ORB) {
                if (dmi.x == x && dmi.y == y && dmi.getLevel() == currentLevel) {
                    orb = dmi;
                    break;
                }
            }
        }
        modelInstances.remove(orb);
        dungeonTiles[currentLevel][x][y] = DungeonTile.NOTHING;

    }

    public void dungeonDrinkFountain(DungeonTile type, int index) {
        if (index >= GameScreen.context.getParty().getMembers().size()) {
            return;
        }
        PartyMember pm = GameScreen.context.getParty().getMember(index);
        switch (type) {
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
        return dungeonTiles[z][x][y] == DungeonTile.NOTHING;
    }

    public void getChest(int index, int x, int y) {

        DungeonTileModelInstance chest = null;
        for (DungeonTileModelInstance dmi : modelInstances) {
            if (dmi.getTile() == DungeonTile.CHEST) {
                if (dmi.x == x && dmi.y == y && dmi.getLevel() == currentLevel) {
                    chest = dmi;
                    break;
                }
            }
        }

        if (chest != null) {
            PartyMember pm = GameScreen.context.getParty().getMember(index);
            GameScreen.context.getChestTrapHandler(pm);
            log(String.format("The Chest Holds: %d Gold", GameScreen.context.getParty().getChestGold()));

            //remove chest model instance
            modelInstances.remove(chest);
            dungeonTiles[currentLevel][x][y] = DungeonTile.NOTHING;

        } else {
            log("Not Here!");
        }
    }

    private void creatureCleanup(int currentX, int currentY) {
        Iterator<Creature> i = dngMap.getMap().getCreatures().iterator();
        while (i.hasNext()) {
            Creature cr = i.next();
            if (cr.currentLevel != this.currentLevel) {
                i.remove();
            }
        }
    }

    private boolean checkRandomDungeonCreatures() {
        int spawnValue = 32;// - (currentLevel << 2);
        if (dngMap.getMap().getCreatures().size() >= MAX_WANDERING_CREATURES_IN_DUNGEON || rand.nextInt(spawnValue) != 0) {
            return false;
        }
        return true;
    }

    /**
     * spawn a dungeon creature in a random walkable place in the level.
     * monsters can walk thru rooms and such but not walls.
     */
    private boolean spawnDungeonCreature(Creature creature, int currentX, int currentY) {

        int dx = 0;
        int dy = 0;
        int tmp = 0;

        boolean ok = false;
        int tries = 0;
        int MAX_TRIES = 10;

        while (!ok && (tries < MAX_TRIES)) {
            dx = 7;
            dy = rand.nextInt(7);

            if (rand.nextInt(2) > 0) {
                dx = -dx;
            }
            if (rand.nextInt(2) > 0) {
                dy = -dy;
            }
            if (rand.nextInt(2) > 0) {
                tmp = dx;
                dx = dy;
                dy = tmp;
            }

            dx = currentX + dx;
            dy = currentY + dy;

            if (dx < 0) {
                dx = DUNGEON_MAP + dx;
            } else if (dx > DUNGEON_MAP - 1) {
                dx = dx - DUNGEON_MAP;
            }
            if (dy < 0) {
                dy = DUNGEON_MAP + dy;
            } else if (dy > DUNGEON_MAP - 1) {
                dy = dy - DUNGEON_MAP;
            }

            /* make sure we can spawn the creature there */
            if (creature != null) {
                DungeonTile tile = dungeonTiles[currentLevel][dx][dy];
                if (tile.getCreatureWalkable()) {
                    ok = true;
                } else {
                    tries++;
                }
            } else {
                ok = true;
            }
        }

        if (!ok) {
            return false;
        }

        if (creature != null) {

        } else {

            //Make a Weighted Random Choice with level as a factor
            int total = 0;
            for (CreatureType ct : CreatureType.values()) {
                total += ct.getSpawnLevel() <= currentLevel ? ct.getSpawnWeight() : 0;
            }

            int thresh = rand.nextInt(total);
            CreatureType monster = null;

            for (CreatureType ct : CreatureType.values()) {
                thresh -= ct.getSpawnLevel() <= currentLevel ? ct.getSpawnWeight() : 0;
                if (thresh < 0) {
                    monster = ct;
                    break;
                }
            }

            creature = GameScreen.creatures.getInstance(monster, GameScreen.enhancedAtlas, GameScreen.standardAtlas);
        }

        if (creature != null) {
            creature.currentX = dx;
            creature.currentY = dy;
            creature.currentLevel = currentLevel;
            dngMap.getMap().addCreature(creature);

            System.out.println("spawned in dungeon: " + creature.getTile());
            setCreatureRotations();
        } else {
            return false;
        }

        return true;
    }

    private void moveDungeonCreatures(BaseScreen screen, int avatarX, int avatarY) {
        for (Creature cr : dngMap.getMap().getCreatures()) {

            int mask = getValidMovesMask(cr.currentX, cr.currentY, cr, avatarX, avatarY);
            //dont use wrap border behavior with the dungeon maps
            Direction dir = Utils.getPath(MapBorderBehavior.wrap, DUNGEON_MAP, DUNGEON_MAP, avatarX, avatarY, mask, true, cr.currentX, cr.currentY);
            if (dir == null) {
                continue;
            }

            if (dir == Direction.NORTH) {
                cr.currentY = cr.currentY - 1 < 0 ? DUNGEON_MAP - 1 : cr.currentY - 1;
            }
            if (dir == Direction.SOUTH) {
                cr.currentY = cr.currentY + 1 >= DUNGEON_MAP ? 0 : cr.currentY + 1;
            }
            if (dir == Direction.EAST) {
                cr.currentX = cr.currentX + 1 >= DUNGEON_MAP ? 0 : cr.currentX + 1;
            }
            if (dir == Direction.WEST) {
                cr.currentX = cr.currentX - 1 < 0 ? DUNGEON_MAP - 1 : cr.currentX - 1;
            }

            cr.getDecal().setPosition(cr.currentX + .5f, .3f, cr.currentY + .5f);

            //if touches avatar then invoke battle!
            if (Utils.movementDistance(MapBorderBehavior.wrap, DUNGEON_MAP, DUNGEON_MAP, avatarX, avatarY, cr.currentX, cr.currentY) == 0) {
                battleWandering(cr, avatarX, avatarY);
            }

        }
    }

    //rotates the 2d sprite decal so that it faces the avatar in 3d space
    private void setCreatureRotations() {
        for (Creature cr : dngMap.getMap().getCreatures()) {
            if (currentDir == Direction.NORTH) {
                cr.getDecal().setRotationY(0);
            }
            if (currentDir == Direction.SOUTH) {
                cr.getDecal().setRotationY(0);
            }
            if (currentDir == Direction.EAST) {
                cr.getDecal().setRotationY(90);
            }
            if (currentDir == Direction.WEST) {
                cr.getDecal().setRotationY(90);
            }
        }
    }

    private int getValidMovesMask(int x, int y, Creature cr, int avatarX, int avatarY) {

        int mask = 0;

        DungeonTile north = dungeonTiles[currentLevel][x][y - 1 < 0 ? DUNGEON_MAP - 1 : y - 1];
        DungeonTile south = dungeonTiles[currentLevel][x][y + 1 >= DUNGEON_MAP ? 0 : y + 1];
        DungeonTile east = dungeonTiles[currentLevel][x + 1 >= DUNGEON_MAP ? 0 : x + 1][y];
        DungeonTile west = dungeonTiles[currentLevel][x - 1 < 0 ? DUNGEON_MAP - 1 : x - 1][y];

        mask = addToMask(Direction.NORTH, mask, north, x, y - 1 < 0 ? DUNGEON_MAP - 1 : y - 1, cr, avatarX, avatarY);
        mask = addToMask(Direction.SOUTH, mask, south, x, y + 1 >= DUNGEON_MAP ? 0 : y + 1, cr, avatarX, avatarY);
        mask = addToMask(Direction.EAST, mask, east, x + 1 >= DUNGEON_MAP - 1 ? 0 : x + 1, y, cr, avatarX, avatarY);
        mask = addToMask(Direction.WEST, mask, west, x - 1 < 0 ? DUNGEON_MAP - 1 : x - 1, y, cr, avatarX, avatarY);

        return mask;

    }

    private int addToMask(Direction dir, int mask, DungeonTile tile, int x, int y, Creature cr, int avatarX, int avatarY) {
        if (tile != null) {
            boolean canmove = false;
            if (tile.getCreatureWalkable()) {
                canmove = true;
            }
            for (Creature cre : dngMap.getMap().getCreatures()) {
                if (cre.currentX == x && cre.currentY == y && cre.currentLevel == cr.currentLevel) {
                    canmove = false;
                    break;
                }
            }
            if (canmove) {
                mask = Direction.addToMask(dir, mask);
            }
        }
        return mask;
    }

    public class RoomLocater {

        public int x, y, z;
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

        public Tile[] tiles = new Tile[11 * 11];

        public boolean hasAltar;
        public BaseVirtue altarRoomVirtue;

        public DungeonRoom(byte[] data, int pos) {

            byte[][] tr = new byte[4][4];
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    tr[i][j] = data[pos];
                    pos++;
                }
            }

            for (int i = 0; i < 4; i++) {
                triggers[i] = new Trigger(tr[i]);
            }

            for (int i = 0; i < 16; i++) {
                monsters[i] = data[pos];
                pos++;
            }
            for (int i = 0; i < 16; i++) {
                monStartX[i] = data[pos];
                pos++;
            }
            for (int i = 0; i < 16; i++) {
                monStartY[i] = data[pos];
                pos++;
            }
            for (int i = 0; i < 8; i++) {
                plStartXNorth[i] = data[pos];
                pos++;
            }
            for (int i = 0; i < 8; i++) {
                plStartYNorth[i] = data[pos];
                pos++;
            }
            for (int i = 0; i < 8; i++) {
                plStartXEast[i] = data[pos];
                pos++;
            }
            for (int i = 0; i < 8; i++) {
                plStartYEast[i] = data[pos];
                pos++;
            }
            for (int i = 0; i < 8; i++) {
                plStartXSouth[i] = data[pos];
                pos++;
            }
            for (int i = 0; i < 8; i++) {
                plStartYSouth[i] = data[pos];
                pos++;
            }
            for (int i = 0; i < 8; i++) {
                plStartXWest[i] = data[pos];
                pos++;
            }
            for (int i = 0; i < 8; i++) {
                plStartYWest[i] = data[pos];
                pos++;
            }

            TileSet ts = GameScreen.baseTileSet;

            for (int y = 0; y < 11; y++) {
                for (int x = 0; x < 11; x++) {
                    Tile t = ts.getTileByIndex(data[pos] & 0xff);
                    tiles[x + (y * 11)] = t;
                    if (t.getIndex() == 74) {
                        hasAltar = true;
                    }
                    pos++;
                }
            }

        }

        public Trigger getTriggerAt(int x, int y) {

            for (int i = 0; i < 4; i++) {
                if (triggers[i].tile.getIndex() != 0 && triggers[i].trigX == x && triggers[i].trigY == y) {
                    return triggers[i];
                }
            }

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
            this.tile = GameScreen.baseTileSet.getTileByIndex(data[0] & 0xff);
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

    private StringBuilder inputBuffer = new StringBuilder();

    class AbyssInputAdapter extends InputAdapter {

        int state = 1;
        int x, y;

        AbyssInputAdapter(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean keyUp(int keycode) {

            if (keycode == Keys.ENTER) {

                if (inputBuffer.length() < 1) {
                    return false;
                }
                String input = inputBuffer.toString().toLowerCase();

                if (state == 1) {
                    if (input.startsWith("stone") && (GameScreen.context.getParty().getSaveGame().stones & Stone.get(currentLevel).getLoc()) == 0) {
                        log("None owned!");
                        Gdx.input.setInputProcessor(new InputMultiplexer(DungeonScreen.this, stage));
                        return false;
                    }
                    if (input.startsWith("stone")) {
                        state = 2;
                        if (currentLevel == 7) {
                            log("A voice rings out: What virtue exists independently of Truth, Love and Courage?");
                        } else {
                            log("A voice rings out: What virtue dost stem from " + Virtue.get(currentLevel).getBaseVirtues() + "?");
                        }
                        log("");
                    } else {
                        log("Hmm, no effect!");
                        Gdx.input.setInputProcessor(new InputMultiplexer(DungeonScreen.this, stage));
                    }
                } else if (state == 2) {
                    if (input.startsWith(Virtue.get(currentLevel).toString().toLowerCase())) {
                        state = 3;
                        log("The voice says:");
                        log("Use thy Stone: ");
                        Sounds.play(Sound.POSITIVE_EFFECT);
                    } else {
                        log("That is not correct!");
                        Gdx.input.setInputProcessor(new InputMultiplexer(DungeonScreen.this, stage));
                    }
                } else if (state == 3) {
                    if (input.startsWith(Stone.get(currentLevel).toString().toLowerCase())) {
                        Sounds.play(Sound.POSITIVE_EFFECT);
                        if (currentLevel == 7) {
                            CodexScreen sc = new CodexScreen(DungeonScreen.this);
                            mainGame.setScreen(sc);
                        } else {
                            log("The altar changes before thyne eyes!");
                            DungeonTileModelInstance altar = null;
                            for (DungeonTileModelInstance dmi : modelInstances) {
                                if (dmi.getTile() == DungeonTile.ALTAR) {
                                    if (dmi.x == x && dmi.y == y && dmi.getLevel() == currentLevel) {
                                        altar = dmi;
                                        break;
                                    }
                                }
                            }
                            modelInstances.remove(altar);
                            dungeonTiles[currentLevel][x][y] = DungeonTile.LADDER_DOWN;

                            ModelInstance instance = new ModelInstance(ladderModel, x + .5f, 0, y + .5f);
                            instance.nodes.get(0).scale.set(.060f, .060f, .060f);
                            instance.calculateTransforms();
                            DungeonTileModelInstance in = new DungeonTileModelInstance(instance, DungeonTile.LADDER_DOWN, currentLevel);
                            modelInstances.add(in);

                            Model manhole = new ModelBuilder().createCylinder(.75f, .02f, .75f, 32, new Material(ColorAttribute.createDiffuse(Color.DARK_GRAY)), Usage.Position | Usage.Normal);
                            instance = new ModelInstance(manhole, x + .5f, 0, y + .5f);
                            modelInstances.add(new DungeonTileModelInstance(instance, DungeonTile.LADDER_DOWN, currentLevel));

                            Gdx.input.setInputProcessor(new InputMultiplexer(DungeonScreen.this, stage));
                        }

                    } else {
                        log("That is not correct!");
                        Gdx.input.setInputProcessor(new InputMultiplexer(DungeonScreen.this, stage));
                    }
                }

                inputBuffer = new StringBuilder();

            } else if (keycode == Keys.BACKSPACE) {
                if (inputBuffer.length() > 0) {
                    inputBuffer.deleteCharAt(inputBuffer.length() - 1);
                    logDeleteLastChar();
                }
            } else if (keycode >= 29 && keycode <= 54) {
                inputBuffer.append(Keys.toString(keycode).toUpperCase());
                logAppend(Keys.toString(keycode).toUpperCase());
            }
            return false;
        }
    }

}
