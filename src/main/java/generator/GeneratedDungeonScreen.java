package generator;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import objects.BaseMap;
import objects.Creature;
import objects.Party.PartyMember;
import ultima.BaseScreen;
import ultima.CombatScreen;
import ultima.DeathScreen;
import ultima.GameScreen;
import ultima.MixtureDialog;
import ultima.SecondaryInputProcessor;
import ultima.Sound;
import ultima.Sounds;
import ultima.SpellInputProcessor;
import ultima.Ultima4;
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
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.UBJsonReader;

/**
 * a randomly generated Dungeon level for this game.
 *
 * @author Paul
 */
public class GeneratedDungeonScreen extends BaseScreen {

    public Maps dngMap;
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

    public static final int DUNGEON_MAP = 32;
    public static final int DUNGEON_LEVELS = 8;

    public Dungeon[] dungeons = new Dungeon[DUNGEON_LEVELS];
    public DungeonTile[][][] dungeonTiles = new DungeonTile[DUNGEON_LEVELS][DUNGEON_MAP][DUNGEON_MAP];

    public List<DungeonTileModelInstance> modelInstances = new ArrayList<DungeonTileModelInstance>();
    public List<ModelInstance> floor = new ArrayList<ModelInstance>();
    public List<ModelInstance> ceiling = new ArrayList<ModelInstance>();

    public int currentLevel = 0;
    public Vector3 currentPos;
    public Direction currentDir = Direction.EAST;

    public SecondaryInputProcessor sip;

    public GeneratedDungeonScreen(Stage stage, GameScreen gameScreen, Maps map) {
        this.dngMap = map;
        scType = ScreenType.RANDOMDNG;
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

        for (int x = 0; x < DUNGEON_MAP; x++) {
            for (int y = 0; y < DUNGEON_MAP; y++) {
                Model sf = builder.createBox(1, 1, 1, new Material(TextureAttribute.createDiffuse(assets.get("assets/graphics/rock.png", Texture.class))), Usage.Position | Usage.TextureCoordinates | Usage.Normal);
                floor.add(new ModelInstance(sf, new Vector3(x - 1.5f, -.5f, y - 1.5f)));
            }
        }
        for (int x = 0; x < DUNGEON_MAP; x++) {
            for (int y = 0; y < DUNGEON_MAP; y++) {
                Model sf = builder.createBox(1, 1, 1, new Material(TextureAttribute.createDiffuse(assets.get("assets/graphics/dirt.png", Texture.class))), Usage.Position | Usage.TextureCoordinates | Usage.Normal);
                ceiling.add(new ModelInstance(sf, new Vector3(x - 1.5f, 1.5f, y - 1.5f)));
            }
        }

        try {

            for (int i = 0; i < DUNGEON_LEVELS; i++) {
                dungeons[i] = new Dungeon();
                dungeons[i].createDungeon(DUNGEON_MAP, DUNGEON_MAP, 30);
            }

            for (int i = 0; i < DUNGEON_LEVELS; i++) {
                for (int y = 0; y < DUNGEON_MAP; y++) {
                    for (int x = 0; x < DUNGEON_MAP; x++) {
                        int val = dungeons[i].getCell(x, y);
                        DungeonTile tile = null;
                        switch (val) {
                            case Dungeon.tileUnused:
                                tile = DungeonTile.WALL;
                                break;
                            case Dungeon.tileDirtWall:
                                tile = DungeonTile.WALL;
                                break;
                            case Dungeon.tileDirtFloor:
                                tile = DungeonTile.NOTHING;
                                break;
                            case Dungeon.tileStoneWall:
                                tile = DungeonTile.WALL;
                                break;
                            case Dungeon.tileCorridor:
                                tile = DungeonTile.NOTHING;
                                break;
                            case Dungeon.tileDoor:
                                tile = DungeonTile.DOOR;
                                break;
                            case Dungeon.tileUpStairs:
                                tile = DungeonTile.LADDER_UP;
                                break;
                            case Dungeon.tileDownStairs:
                                tile = DungeonTile.LADDER_DOWN;
                                break;
                            case Dungeon.tileChest:
                                tile = DungeonTile.CHEST;
                                break;
                        }
                        dungeonTiles[i][x][y] = tile;
                        addBlock(i, tile, x + .5f, .5f, y + .5f);
                    }
                }
            }

            //System.out.println(dungeons[0].showDungeon());
            setStartPosition();

            cam.position.set(currentPos);
            cam.lookAt(currentPos.x + 1, currentPos.y, currentPos.z);

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

    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        batch.dispose();
        decalBatch.dispose();
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

    public void drawHUD() {

        batch.begin();

        Ultima4.hud.render(batch, GameScreen.context.getParty());

        font.draw(batch, "Level " + (currentLevel + 1), Ultima4.SCREEN_WIDTH / 2 - 20, Ultima4.SCREEN_HEIGHT - 3);

        if (showZstats > 0) {
            GameScreen.context.getParty().getSaveGame().renderZstats(showZstats, font, batch, Ultima4.SCREEN_HEIGHT);
        }

        batch.end();
    }

    public void battleWandering(Creature cr, int x, int y) {
        if (cr == null) {
            return;
        }
        Maps contextMap = Maps.get(dngMap.getId());
        DungeonTile tile = dungeonTiles[currentLevel][x][y];
        TiledMap tmap = new UltimaTiledMapLoader(tile.getCombatMap(), GameScreen.standardAtlas, 11, 11, GameScreen.TILE_DIM, GameScreen.TILE_DIM).load();
        GameScreen.context.setCurrentTiledMap(tmap);
        CombatScreen sc = new CombatScreen(this, GameScreen.context, contextMap, tile.getCombatMap().getMap(), tmap, cr.getTile(), GameScreen.creatures, GameScreen.standardAtlas);
        mainGame.setScreen(sc);
        currentEncounter = cr;
    }

    public void partyDeath() {
        //death scene
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
            } else if (currentDir == Direction.WEST) {
                x = x - 1;
            } else if (currentDir == Direction.NORTH) {
                y = y - 1;
            } else if (currentDir == Direction.SOUTH) {
                y = y + 1;
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
                checkTrap(tile, x, y);
            }

        } else if (keycode == Keys.DOWN) {

            //backwards
            if (currentDir == Direction.EAST) {
                x = x - 1;
            } else if (currentDir == Direction.WEST) {
                x = x + 1;
            } else if (currentDir == Direction.NORTH) {
                y = y + 1;
            } else if (currentDir == Direction.SOUTH) {
                y = y - 1;
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
                checkTrap(tile, x, y);
            }

        } else if (keycode == Keys.K) {
            if (tile == DungeonTile.LADDER_UP || tile == DungeonTile.LADDER_UP_DOWN) {
                currentLevel--;
                if (currentLevel < 0) {
                    currentLevel = 0;
                    if (mainGame != null) {
                        mainGame.setScreen(gameScreen);
                        dispose();
                    }
                } else {
                    Vector2 p = findOpenRoom(x, y);
                    currentPos = new Vector3(p.x + .5f, .5f, p.y + .5f);
                }
                //System.out.println(dungeons[currentLevel].showDungeon());

            }
            return false;

        } else if (keycode == Keys.D) {
            if (tile == DungeonTile.LADDER_DOWN || tile == DungeonTile.LADDER_UP_DOWN) {
                currentLevel++;

                if (currentLevel > DUNGEON_MAP) {
                    currentLevel = DUNGEON_MAP;
                } else {
                    Vector2 p = findOpenRoom(x, y);
                    currentPos = new Vector3(p.x + .5f, .5f, p.y + .5f);
                }
                //System.out.println(dungeons[currentLevel].showDungeon());

            }
            return false;

        } else if (keycode == Keys.N) {
            log("New Order:");
            log("exhange #:");
            NewOrderInputAdapter noia = new NewOrderInputAdapter(this);
            Gdx.input.setInputProcessor(noia);
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
            CombatScreen.holeUp(this.dngMap, x, y, this, GameScreen.context, GameScreen.creatures, GameScreen.standardAtlas);
            return false;

        } else if (keycode == Keys.V) {

        } else if (keycode == Keys.M) {

            new MixtureDialog(GameScreen.context.getParty(), this, stage, skin).show();

        } else if (keycode == Keys.S) {
            if (tile == DungeonTile.ALTAR) {

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

    @SuppressWarnings("incomplete-switch")
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
        int spawnValue = 24;
        if (dngMap.getMap().getCreatures().size() >= 6 || rand.nextInt(spawnValue) != 0) {
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

            creature = GameScreen.creatures.getInstance(monster, GameScreen.standardAtlas);
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

    public class PeerGemInputAdapter extends InputAdapter {

        Image img;

        public PeerGemInputAdapter() {
            try {
                int x = (Math.round(currentPos.x) - 1);
                int y = (Math.round(currentPos.z) - 1);
                Texture t = dungeons[currentLevel].peerGem(x, y);
                img = new Image(t);
                img.setX(Ultima4.SCREEN_WIDTH / 2 - t.getWidth() / 2);
                img.setY(Ultima4.SCREEN_HEIGHT / 2 - t.getHeight() / 2);
                img.addAction(sequence(Actions.alpha(0), Actions.fadeIn(1f, Interpolation.fade)));
                stage.addActor(img);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean keyUp(int keycode) {
            if (img != null) {
                img.remove();
            }
            Gdx.input.setInputProcessor(new InputMultiplexer(GeneratedDungeonScreen.this, stage));
            return false;
        }
    }

    private Vector2 findOpenRoom(int currentX, int currentY) {

        int dx = 0;
        int dy = 0;
        int tmp = 0;

        boolean ok = false;

        while (!ok) {
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

            /* make sure it is a room */
            DungeonTile tile = dungeonTiles[currentLevel][dx][dy];
            if (tile.getCreatureWalkable()) {
                ok = true;
            }

        }

        if (!ok) {
            return new Vector2(currentX, currentY);
        }
        return new Vector2(dx, dy);

    }

}
