package generator;

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
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Region;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
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
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.UBJsonReader;
import objects.Portal;
import static ultima.BaseScreen.mainGame;
import ultima.Constants;
import ultima.Context;
import ultima.MixtureScreen;
import util.PartyDeathException;

public class StaticGeneratedDungeonScreen extends BaseScreen {

    public static final int DUNGEON_MAP = 35;
    public static final int DUNGEON_LEVELS = 2;

    public Maps dngMap;
    public GameScreen gameScreen;

    public Environment[] environment = new Environment[DUNGEON_LEVELS];
    public ModelBatch modelBatch;
    private SpriteBatch batch;
    private DecalBatch decalBatch;

    //public CameraInputController inputController;

    public AssetManager assets;
    BitmapFont font;

    //3d models
    public Model fountainModel;
    public Model ladderModel;
    public Model chestModel;
    public Model orbModel;
    public Model altarModel;
    public Model rocksModel;
    public Model campfireModel;

    boolean showMiniMap = true;

    boolean isTorchOn = true;
    private Vector3 vdll = new Vector3(.04f, .04f, .04f);
    private Vector3 nll2 = new Vector3(1f, 0.8f, 0.6f);
    private Vector3 nll = new Vector3(.96f, .58f, 0.08f);

    PointLight fixedLight;
    float lightFactor;

    public String[] mapTileIds;

    public TiledMapTileLayer[] layers = new TiledMapTileLayer[DUNGEON_LEVELS];

    public DungeonTile[][][] dungeonTiles = new DungeonTile[DUNGEON_LEVELS][DUNGEON_MAP][DUNGEON_MAP];

    public List<DungeonTileModelInstance> modelInstances = new ArrayList<>();

    public int currentLevel = 0;
    public Vector3 currentPos;
    public Direction currentDir = Direction.EAST;

    public SecondaryInputProcessor sip;

    public StaticGeneratedDungeonScreen(GameScreen gameScreen, Context context, Maps map) {
        this.dngMap = map;
        scType = ScreenType.TMXDUNGEON;
        this.gameScreen = gameScreen;
        this.context = context;
        this.stage = new Stage();
        sip = new SecondaryInputProcessor(this, stage);
        addButtons();
        init();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputMultiplexer(this, stage));//inputController));
        context.getParty().addObserver(this);
    }

    @Override
    public void hide() {
        context.getParty().deleteObserver(this);
    }

    public void init() {

        FileHandleResolver resolver = new Constants.ClasspathResolver();

        assets = new AssetManager(resolver);
        assets.load("assets/graphics/dirt.png", Texture.class);
        assets.load("assets/graphics/map.png", Texture.class);
        assets.load("assets/graphics/Stone_Masonry.jpg", Texture.class);
        assets.load("assets/graphics/door.png", Texture.class);
        assets.load("assets/graphics/mortar.png", Texture.class);
        assets.load("assets/graphics/rock.png", Texture.class);

        assets.update(2000);

        assets.get("assets/graphics/rock.png", Texture.class).setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        assets.get("assets/graphics/door.png", Texture.class).setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        assets.get("assets/graphics/mortar.png", Texture.class).setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        assets.get("assets/graphics/dirt.png", Texture.class).setFilter(TextureFilter.Nearest, TextureFilter.Nearest);

        ModelLoader<?> gloader = new G3dModelLoader(new UBJsonReader());
        fountainModel = gloader.loadModel(Gdx.files.classpath("assets/graphics/fountain2.g3db"));
        ladderModel = gloader.loadModel(Gdx.files.classpath("assets/graphics/ladder.g3db"));
        chestModel = gloader.loadModel(Gdx.files.classpath("assets/graphics/chest.g3db"));
        orbModel = gloader.loadModel(Gdx.files.classpath("assets/graphics/orb.g3db"));
        altarModel = gloader.loadModel(Gdx.files.classpath("assets/graphics/altar.g3db"));
        rocksModel = gloader.loadModel(Gdx.files.classpath("assets/graphics/rocks.g3db"));
        campfireModel = gloader.loadModel(Gdx.files.classpath("assets/graphics/campfire.g3db"));

        font = new BitmapFont();
        font.setColor(Color.WHITE);

        fixedLight = new PointLight().set(1f, 0.8f, 0.6f, 4f, 4f, 4f, 5f);

        modelBatch = new ModelBatch();

        batch = new SpriteBatch();

        camera = new PerspectiveCamera(67, Ultima4.MAP_WIDTH, Ultima4.MAP_HEIGHT);
        
        camera.near = 0.1f;
        camera.far = 1000f;

        decalBatch = new DecalBatch(new CameraGroupStrategy(camera));

//        inputController = new CameraInputController(camera);
//        inputController.rotateLeftKey = inputController.rotateRightKey = inputController.forwardKey = inputController.backwardKey = 0;
//        inputController.translateUnits = 30f;

        ModelBuilder builder = new ModelBuilder();

        Model fm = builder.createBox(1, 1, 1, new Material(TextureAttribute.createDiffuse(assets.get("assets/graphics/rock.png", Texture.class))), Usage.Position | Usage.TextureCoordinates | Usage.Normal);
        Model cm = builder.createBox(1, 1, 1, new Material(TextureAttribute.createDiffuse(assets.get("assets/graphics/dirt.png", Texture.class))), Usage.Position | Usage.TextureCoordinates | Usage.Normal);

        try {

            int TILE_SIZE = 16;
            FileHandle f = resolver.resolve("assets/tilemaps/tiles-vga-atlas.txt");
            TextureAtlasData a = new TextureAtlasData(f, f.parent(), false);
            mapTileIds = new String[a.getRegions().size + 1];
            for (Region r : a.getRegions()) {
                int x = r.left / r.width;
                int y = r.top / r.height;
                int i = y * TILE_SIZE + x + 1;
                mapTileIds[i] = r.name;
            }

            TiledMap map = new TmxMapLoader().load("assets/tilemaps/delveOfSorrows.tmx");
            Iterator<MapLayer> iter = map.getLayers().iterator();
            int level = 0;
            while (iter.hasNext()) {

                environment[level] = new Environment();
                environment[level].set(new ColorAttribute(ColorAttribute.Ambient, 0.5f, 0.5f, 0.5f, 1f));
                environment[level].add(fixedLight);

                layers[level] = (TiledMapTileLayer) iter.next();
                for (int y = 0; y < DUNGEON_MAP; y++) {
                    for (int x = 0; x < DUNGEON_MAP; x++) {

                        String val = mapTileIds[layers[level].getCell(x, DUNGEON_MAP - y - 1).getTile().getId()];
                        DungeonTile tile = DungeonTile.getTileByName(val);

                        if (tile == null) {
                            CreatureType ct = CreatureType.get(val);
                            if (ct != null) {
                                Creature creature = Ultima4.creatures.getInstance(ct, Ultima4.standardAtlas);
                                creature.currentX = x;
                                creature.currentY = y;
                                creature.currentLevel = level;
                                creature.getDecal().setPosition(creature.currentX + .5f, .3f, creature.currentY + .5f);
                                dngMap.getMap().addCreature(creature);

                            } else {
                                System.err.println(val);
                            }
                            dungeonTiles[level][x][y] = DungeonTile.NOTHING;

                        } else if (tile == DungeonTile.WATER) {
                            Model w = builder.createBox(1, 1, 1, getMaterial(Color.BLUE, .9f), Usage.Position | Usage.Normal);
                            ModelInstance wi = new ModelInstance(w, x + .5f, -.5f, y + .5f);
                            DungeonTileModelInstance fin = new DungeonTileModelInstance(wi, DungeonTile.WATER, level);
                            modelInstances.add(fin);
                            dungeonTiles[level][x][y] = DungeonTile.NOTHING;
                        } else {

                            dungeonTiles[level][x][y] = tile;
                            addBlock(level, tile, x + .5f, .5f, y + .5f);
                        }

                        if (tile == null || tile != DungeonTile.WATER) {
                            DungeonTileModelInstance fin = new DungeonTileModelInstance(new ModelInstance(fm, new Vector3(x + .5f, -.5f, y + .5f)), DungeonTile.FLOOR, level);
                            modelInstances.add(fin);
                        }

                        DungeonTileModelInstance cin = new DungeonTileModelInstance(new ModelInstance(cm, new Vector3(x + .5f, 1.5f, y + .5f)), DungeonTile.FLOOR, level);
                        modelInstances.add(cin);
                    }

                }
                level++;
            }

            setStartPosition();

            camera.position.set(currentPos);
            camera.lookAt(currentPos.x + 1, currentPos.y, currentPos.z);

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
                    fixedLight.set(nll2.x, nll2.y, nll2.z, currentPos.x, currentPos.y + .35f, currentPos.z, 4.75f);
                }
            }
        }

    }
    
    public void restoreSaveGameLocation(int x, int y, int z, Direction orientation) {

        currentPos = new Vector3(x + .5f, .5f, y + .5f);
        camera.position.set(currentPos);
        currentDir = orientation;
        currentLevel = z;

        if (currentDir == Direction.EAST) {
            camera.lookAt(currentPos.x + 1, currentPos.y, currentPos.z);
        } else if (currentDir == Direction.WEST) {
            camera.lookAt(currentPos.x - 1, currentPos.y, currentPos.z);
        } else if (currentDir == Direction.NORTH) {
            camera.lookAt(currentPos.x, currentPos.y, currentPos.z - 1);
        } else if (currentDir == Direction.SOUTH) {
            camera.lookAt(currentPos.x, currentPos.y, currentPos.z + 1);
        }

    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        batch.dispose();
        decalBatch.dispose();
        font.dispose();
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);
        
        lightFactor += Gdx.graphics.getDeltaTime();
        float lightSize = 4.75f + 0.25f * (float) Math.sin(lightFactor) + .2f * MathUtils.random();

        Vector3 ll = isTorchOn ? nll : vdll;
        ll = isTorchOn ? nll2 : vdll;
        fixedLight.set(ll.x, ll.y, ll.z, currentPos.x, currentPos.y + .35f, currentPos.z, lightSize);

        Gdx.gl.glViewport(32, 64, Ultima4.MAP_WIDTH, Ultima4.MAP_HEIGHT);
        
        camera.update();
        
        modelBatch.begin(camera);

        for (DungeonTileModelInstance i : modelInstances) {
            if (i.getLevel() == currentLevel) {
                modelBatch.render(i.getInstance(), environment[currentLevel]);
            }
        }

        modelBatch.end();

        for (Creature cr : dngMap.getMap().getCreatures()) {
            if (cr.currentLevel != this.currentLevel) {
                continue;
            }
            decalBatch.add(cr.getDecal());
        }
        decalBatch.flush();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        
        batch.begin();
        batch.draw(Ultima4.backGround, 0, 0);

        Ultima4.hud.render(batch, context.getParty());
        Ultima4.font.draw(batch, "Level " + (currentLevel + 1) + " facing " + currentDir, 305, Ultima4.SCREEN_HEIGHT - 7);
        if (showZstats > 0) {
            context.getParty().getSaveGame().renderZstats(showZstats, Ultima4.font, batch, Ultima4.SCREEN_HEIGHT);
        }
        batch.end();
        
        stage.act();
        stage.draw();

    }

    private Material getMaterial(Texture t) {
        Material mat = new Material(TextureAttribute.createDiffuse(t));
        //mat.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 1f));
        return mat;
        //return new Material(TextureAttribute.createNormal(t), TextureAttribute.createDiffuse(t), TextureAttribute.createSpecular(t));
        //return new Material(TextureAttribute.createDiffuse(t), TextureAttribute.createSpecular(t));
    }

    private Material getMaterial(Color c, float blending) {
        return new Material(ColorAttribute.createDiffuse(c), ColorAttribute.createSpecular(c), new BlendingAttribute(blending));
    }

    public void addBlock(int level, DungeonTile tile, float tx, float ty, float tz) {
        ModelBuilder builder = new ModelBuilder();
        if (tile == DungeonTile.WALL) {
            Model model = builder.createBox(1, 1, 1, getMaterial(assets.get("assets/graphics/mortar.png", Texture.class)), Usage.Position | Usage.Normal | Usage.TextureCoordinates);
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
        } else if (tile == DungeonTile.ROCKS) {
            ModelInstance instance = new ModelInstance(rocksModel, tx, 0, tz);
            instance.nodes.get(0).scale.set(.010f, .010f, .010f);
            instance.calculateTransforms();
            DungeonTileModelInstance in = new DungeonTileModelInstance(instance, tile, level);
            modelInstances.add(in);
        } else if (tile == DungeonTile.MOONGATE) {
            Color c = Color.CYAN;
            Model model = builder.createBox(1, 1, 1, getMaterial(c, .7f), Usage.Position | Usage.Normal);
            ModelInstance instance = new ModelInstance(model, tx, .5f, tz);
            DungeonTileModelInstance in = new DungeonTileModelInstance(instance, tile, level);
            modelInstances.add(in);
            in.x = (int) tx;
            in.y = (int) tz;
        } else if (tile.getValue() >= 10 && tile.getValue() <= 48) {
            ModelInstance instance = new ModelInstance(ladderModel, tx, 0, tz);
            instance.nodes.get(0).scale.set(.060f, .060f, .060f);
            instance.calculateTransforms();
            DungeonTileModelInstance in = new DungeonTileModelInstance(instance, tile, level);
            modelInstances.add(in);

            Model manhole = builder.createCylinder(.75f, .02f, .75f, 32, getMaterial(Color.DARK_GRAY, 1), Usage.Position | Usage.Normal);
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
        } else if (tile == DungeonTile.COLUMN) {
            Model sf = builder.createCylinder(.35f, 2.5f, .35f, 32, getMaterial(assets.get("assets/graphics/Stone_Masonry.jpg", Texture.class)), Usage.Position | Usage.TextureCoordinates | Usage.Normal);
            ModelInstance instance = new ModelInstance(sf);
            instance.transform.setToTranslation(tx, 0, tz);
            DungeonTileModelInstance in = new DungeonTileModelInstance(instance, tile, level);
            in.x = (int) tx;
            in.y = (int) tz;
            modelInstances.add(in);
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

        } else if (tile == DungeonTile.LIGHT) {
//			PointLight pl = new PointLight().set(nll2.x, nll2.y,  nll2.z, tx, .8f, tz, 2f);
//			DirectionalLight pl = new DirectionalLight().set(nll2.x, nll2.y,  nll2.z, tx, .8f, tz);
//			environment[level].add(pl);

//			Model lm = builder.createSphere(.02f, .02f, .02f, 10, 10, getMaterial(new Color(nll2.x, nll2.y,  nll2.z, 1), .7f), Usage.Position);
//	        ModelInstance instance = new ModelInstance(lm);
//	        instance.transform.setToTranslation(tx, .8f, tz);
//			DungeonTileModelInstance in = new DungeonTileModelInstance(instance, tile, level);
//			in.x=(int)tx; in.y=(int)tz;
//			modelInstances.add(in);
        } else if (tile == DungeonTile.FIRE) {
            ModelInstance instance = new ModelInstance(campfireModel, tx, 0, tz);
            instance.nodes.get(0).scale.set(.510f, .510f, .510f);
            instance.calculateTransforms();
            DungeonTileModelInstance in = new DungeonTileModelInstance(instance, tile, level);
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
            Model model = builder.createBox(1, 1, 1, getMaterial(c, .7f), Usage.Position | Usage.Normal);
            ModelInstance instance = new ModelInstance(model, tx, .5f, tz);
            DungeonTileModelInstance in = new DungeonTileModelInstance(instance, tile, level);
            modelInstances.add(in);
            in.x = (int) tx;
            in.y = (int) tz;

        } else if (tile == DungeonTile.DOOR || tile == DungeonTile.SECRET_DOOR || tile == DungeonTile.LOCKED_DOOR) {
            Model model = builder.createBox(1, 1, 1, getMaterial(assets.get("assets/graphics/mortar.png", Texture.class)), Usage.Position | Usage.TextureCoordinates | Usage.Normal);
            ModelInstance instance = new ModelInstance(model, tx, ty, tz);
            instance.transform.setFromEulerAngles(0, 0, 90).trn(tx, ty, tz);
            DungeonTileModelInstance in = new DungeonTileModelInstance(instance, tile, level);
            modelInstances.add(in);

            Material matDoor = null;
            if (tile == DungeonTile.DOOR || tile == DungeonTile.LOCKED_DOOR) {
                matDoor = getMaterial(assets.get("assets/graphics/door.png", Texture.class));
            } else {
                matDoor = getMaterial(Color.DARK_GRAY, .3f);
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

    public void battleWandering(Creature cr, int x, int y) {
        if (cr == null) {
            return;
        }
        Maps contextMap = Maps.get(dngMap.getId());
        DungeonTile tile = dungeonTiles[currentLevel][x][y];
        TiledMap tmap = new UltimaTiledMapLoader(tile.getCombatMap(), Ultima4.standardAtlas, 11, 11, tilePixelWidth, tilePixelHeight).load();
        context.setCurrentTiledMap(tmap);
        CombatScreen sc = new CombatScreen(this, context, contextMap, tile.getCombatMap().getMap(), tmap, cr.getTile(), Ultima4.creatures, Ultima4.standardAtlas);
        mainGame.setScreen(sc);
        currentEncounter = cr;
    }


    @Override
    public void partyDeath() {
        mainGame.setScreen(new DeathScreen(gameScreen, context.getParty()));
        gameScreen.loadNextMap(Maps.CASTLE_OF_LORD_BRITISH_2, REVIVE_CASTLE_X, REVIVE_CASTLE_Y);
    }

    @Override
    public void endCombat(boolean isWon, BaseMap combatMap, boolean wounded) {

        mainGame.setScreen(this);

        if (isWon) {

            if (currentEncounter != null) {
                log("Victory!");
                context.getParty().adjustKarma(KarmaAction.KILLED_EVIL);
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
            if (combatMap.getType() == MapType.combat && context.getParty().didAnyoneFlee()) {
                log("Battle is lost!");
                //no flee penalty in dungeons
            } else if (!context.getParty().isAnyoneAlive()) {
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
                camera.lookAt(currentPos.x, currentPos.y, currentPos.z - 1);
                currentDir = Direction.NORTH;
            } else if (currentDir == Direction.WEST) {
                camera.lookAt(currentPos.x, currentPos.y, currentPos.z + 1);
                currentDir = Direction.SOUTH;
            } else if (currentDir == Direction.NORTH) {
                camera.lookAt(currentPos.x - 1, currentPos.y, currentPos.z);
                currentDir = Direction.WEST;
            } else if (currentDir == Direction.SOUTH) {
                camera.lookAt(currentPos.x + 1, currentPos.y, currentPos.z);
                currentDir = Direction.EAST;
            }
            setCreatureRotations();
            return false;

        } else if (keycode == Keys.RIGHT) {

            if (currentDir == Direction.EAST) {
                camera.lookAt(currentPos.x, currentPos.y, currentPos.z + 1);
                currentDir = Direction.SOUTH;
            } else if (currentDir == Direction.WEST) {
                camera.lookAt(currentPos.x, currentPos.y, currentPos.z - 1);
                currentDir = Direction.NORTH;
            } else if (currentDir == Direction.NORTH) {
                camera.lookAt(currentPos.x + 1, currentPos.y, currentPos.z);
                currentDir = Direction.EAST;
            } else if (currentDir == Direction.SOUTH) {
                camera.lookAt(currentPos.x - 1, currentPos.y, currentPos.z);
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
            if (tile != DungeonTile.WALL && tile != DungeonTile.FIELD_ENERGY) {
                currentPos = new Vector3(x + .5f, .5f, y + .5f);
                camera.position.set(currentPos);
                if (currentDir == Direction.EAST) {
                    camera.lookAt(currentPos.x + 1, currentPos.y, currentPos.z);
                } else if (currentDir == Direction.WEST) {
                    camera.lookAt(currentPos.x - 1, currentPos.y, currentPos.z);
                } else if (currentDir == Direction.NORTH) {
                    camera.lookAt(currentPos.x, currentPos.y, currentPos.z - 1);
                } else if (currentDir == Direction.SOUTH) {
                    camera.lookAt(currentPos.x, currentPos.y, currentPos.z + 1);
                }
                try {
                    checkTileAffects(tile, x, y);
                } catch (PartyDeathException e) {
                    partyDeath();
                    return false;
                }
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
            if (tile != DungeonTile.WALL && tile != DungeonTile.FIELD_ENERGY) {
                currentPos = new Vector3(x + .5f, .5f, y + .5f);
                camera.position.set(currentPos);
                if (currentDir == Direction.EAST) {
                    camera.lookAt(currentPos.x + 1, currentPos.y, currentPos.z);
                } else if (currentDir == Direction.WEST) {
                    camera.lookAt(currentPos.x - 1, currentPos.y, currentPos.z);
                } else if (currentDir == Direction.NORTH) {
                    camera.lookAt(currentPos.x, currentPos.y, currentPos.z - 1);
                } else if (currentDir == Direction.SOUTH) {
                    camera.lookAt(currentPos.x, currentPos.y, currentPos.z + 1);
                }
                try {
                    checkTileAffects(tile, x, y);
                } catch (PartyDeathException e) {
                    partyDeath();
                    return false;
                }
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
                }
            }
            return false;

        } else if (keycode == Keys.D) {
            if (tile == DungeonTile.LADDER_DOWN || tile == DungeonTile.LADDER_UP_DOWN) {
                currentLevel++;
                if (currentLevel > DUNGEON_MAP) {
                    currentLevel = DUNGEON_MAP;
                }
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
            Gdx.input.setInputProcessor(new SpellInputProcessor(this, context, stage, x, y, null));

        } else if (keycode == Keys.I) {

            isTorchOn = !isTorchOn;

        } else if (keycode == Keys.G || keycode == Keys.R || keycode == Keys.W) {
            log("Which party member?");
            Gdx.input.setInputProcessor(sip);
            sip.setinitialKeyCode(keycode, tile, x, y);

        } else if (keycode == Keys.H) {
            CombatScreen.holeUp(this.dngMap, x, y, this, context, Ultima4.creatures, Ultima4.standardAtlas, false);
            return false;

        } else if (keycode == Keys.V) {

        } else if (keycode == Keys.M) {

            mainGame.setScreen(new MixtureScreen(mainGame, this, Ultima4.skin, context.getParty()));

        } else if (keycode == Keys.S) {
            if (tile == DungeonTile.ALTAR) {
                log("Search Altar");
                ItemMapLabels l = dngMap.getMap().searchLocation(this, context.getParty(), x, y, currentLevel);
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
        } else if (keycode == Keys.Q) {
            context.saveGame(x, y, currentLevel, currentDir, dngMap);
            log("Saved Game.");
            return false;
        } else if (keycode == Keys.Z) {
            showZstats = showZstats + 1;
            if (showZstats >= STATS_PLAYER1 && showZstats <= STATS_PLAYER8) {
                if (showZstats > context.getParty().getMembers().size()) {
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
        context.getAura().passTurn();

        if (checkRandomDungeonCreatures()) {
            spawnDungeonCreature(null, currentX, currentY);
        }

        moveDungeonCreatures(this, currentX, currentY);
    }

    public void checkTileAffects(DungeonTile tile, int x, int y) throws PartyDeathException {
        switch (tile) {
            case WIND_TRAP:
                log("Wind extinguished your torch!");
                Sounds.play(Sound.WIND);
                isTorchOn = false;
                break;
            case PIT_TRAP:
                log("Pit!");
                context.getParty().applyEffect(TileEffect.LAVA);
                Sounds.play(Sound.BOOM);
                dungeonTiles[currentLevel][x][y] = DungeonTile.NOTHING;
                break;
            case ROCK_TRAP:
                log("Falling Rocks!");
                context.getParty().applyEffect(TileEffect.LAVA);
                Sounds.play(Sound.ROCKS);
                dungeonTiles[currentLevel][x][y] = DungeonTile.NOTHING;
                break;
            case MOONGATE:
                Portal p = dngMap.getMap().getPortal(x, y, currentLevel);
                if (p != null) {
                    log(p.getMessage());
                    Sounds.play(Sound.MOONGATE);
                    mainGame.setScreen(gameScreen);
                    gameScreen.loadNextMap(Maps.WORLD, p.getStartx(), p.getStarty());
                    dispose();
                }
                break;
            case FIELD_POISON:
                context.getParty().applyEffect(TileEffect.POISONFIELD);
                Sounds.play(Sound.POISON_DAMAGE);
                break;
            case FIELD_SLEEP:
                context.getParty().applyEffect(TileEffect.SLEEP);
                Sounds.play(Sound.SLEEP);
                break;
            case FIELD_FIRE:
                context.getParty().applyEffect(TileEffect.LAVA);
                Sounds.play(Sound.FIREFIELD);
                break;
        }
    }

    public void dungeonDrinkFountain(DungeonTile type, int index) {
        try {
            if (index >= context.getParty().getMembers().size()) {
                return;
            }
            PartyMember pm = context.getParty().getMember(index);
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
        } catch (PartyDeathException pde) {
            partyDeath();
        }
    }

    public void dungeonTouchOrb(int index) {
        try {
            if (index >= context.getParty().getMembers().size()) {
                return;
            }
            PartyMember pm = context.getParty().getMember(index);
            int x = (Math.round(currentPos.x) - 1);
            int y = (Math.round(currentPos.z) - 1);

            int stats = 0;
            int damage = 0;

            if (dngMap == Maps.DELVE_SORROWS) {
                if (currentLevel == 1) {
                    stats = STATSBONUS_INT | STATSBONUS_DEX;
                } else {
                    stats = STATSBONUS_INT | STATSBONUS_DEX | STATSBONUS_STR;
                }
            } else {
                stats = STATSBONUS_STR;
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
            
            Sounds.play(Sound.LIGHTNING);

            pm.applyDamage(damage, false);

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
        } catch (PartyDeathException pde) {
            partyDeath();
        }

    }

    public boolean validTeleportLocation(int x, int y, int z) {
        return dungeonTiles[z][x][y] == DungeonTile.NOTHING;
    }

    public void getChest(int index, int x, int y) {
        try {
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
                PartyMember pm = context.getParty().getMember(index);
                context.getChestTrapHandler(pm);
                log(String.format("The Chest Holds: %d Gold", context.getParty().getChestGold()));

                //remove chest model instance
                modelInstances.remove(chest);
                dungeonTiles[currentLevel][x][y] = DungeonTile.NOTHING;

            } else {
                log("Not Here!");
            }
        } catch (PartyDeathException e) {
            partyDeath();
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

            creature = Ultima4.creatures.getInstance(monster, Ultima4.standardAtlas);
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

            if (cr.currentLevel != this.currentLevel) {
                continue;
            }

            //dont move the creature unless the avatar gets close
            if (Utils.movementDistance(MapBorderBehavior.wrap, DUNGEON_MAP, DUNGEON_MAP, avatarX, avatarY, cr.currentX, cr.currentY) > 3) {
                continue;
            }

            int mask = getValidMovesMask(cr.currentX, cr.currentY, cr, avatarX, avatarY);
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

    @Override
    public InputProcessor getPeerGemInputProcessor() {
        return new StaticGeneratedDungeonScreen.PeerGemInputAdapter();
    }

    private class PeerGemInputAdapter extends InputAdapter {

        Image img;

        public PeerGemInputAdapter() {
            try {
                int x = (Math.round(currentPos.x) - 1);
                int y = (Math.round(currentPos.z) - 1);
                Texture t = Utils.peerGem(layers[currentLevel], mapTileIds, Ultima4.standardAtlas, x, y);
                img = new Image(t);
                img.setX(Ultima4.SCREEN_WIDTH / 2 - t.getWidth() / 2);
                img.setY(Ultima4.SCREEN_HEIGHT / 2 - t.getHeight() / 2);
                img.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(1f, Interpolation.fade)));
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
            Gdx.input.setInputProcessor(new InputMultiplexer(StaticGeneratedDungeonScreen.this, stage));
            return false;
        }
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
