package ultima;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import java.util.Random;

import objects.ArmorSet;
import objects.BaseMap;
import objects.Creature;
import objects.CreatureSet;
import objects.Drawable;
import objects.MapSet;
import objects.Moongate;
import objects.Party;
import objects.Party.PartyMember;
import objects.Portal;
import objects.SaveGame;
import objects.Tile;
import objects.TileSet;
import objects.WeaponSet;

import org.apache.commons.lang.StringUtils;

import util.UltimaMapRenderer;
import util.UltimaTiledMapLoader;
import util.Utils;
import vendor.VendorClassSet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class GameScreen extends BaseScreen {
	
	public static Context context;
	public static TileSet baseTileSet;
	public static WeaponSet weapons;
	public static ArmorSet armors;
	public static CreatureSet creatures;
	public static VendorClassSet vendorClassSet;
	public static TextureAtlas standardAtlas;
	public static TextureAtlas enhancedAtlas;
	
	MapSet maps;
	TextureAtlas moonAtlas;
	
	public static Animation mainAvatar;
	public static Animation avatarAnim;
	public static Animation horseAnim;
	public static Animation shipAnim;
	public static Animation balloonAnim;
	public static int avatarDirection = Direction.WEST.getVal();

	
	TiledMap map;
	UltimaMapRenderer renderer;
	Batch mapBatch, batch;
	
	public Stage mapObjectsStage;
	public Stage projectilesStage;


	Array<AtlasRegion> moongateTextures = new Array<AtlasRegion>();
	public static int phase = 0, trammelphase = 0, trammelSubphase = 0, feluccaphase = 0;
	
	public SecondaryInputProcessor sip;
	Random rand = new Random();
	
	GameTimer gameTimer = new GameTimer();
	
	public GameScreen(Ultima4 mainGame) {
		
		scType = ScreenType.MAIN;
		
		GameScreen.mainGame = mainGame;
		GameScreen.skin = new Skin(Gdx.files.internal("assets/skin/uiskin.json"));
		
		try {
			standardAtlas = new TextureAtlas(Gdx.files.internal("assets/tilemaps/tiles-vga-atlas.txt"));
			enhancedAtlas = new TextureAtlas(Gdx.files.internal("assets/tilemaps/monsters-u4.atlas"));
			
			baseTileSet = (TileSet) Utils.loadXml("tileset-base.xml", TileSet.class);	
			baseTileSet.setMaps();
			
			hitTile = standardAtlas.findRegion("hit_flash");
			missTile = standardAtlas.findRegion("miss_flash");
			corpse = enhancedAtlas.findRegion("corpse");
						
			maps = (MapSet) Utils.loadXml("maps.xml", MapSet.class);
			maps.init(baseTileSet);
			
			vendorClassSet = (VendorClassSet) Utils.loadXml("vendor.xml", VendorClassSet.class);
			vendorClassSet.init();
			
			weapons = (WeaponSet) Utils.loadXml("weapons.xml", WeaponSet.class);
			armors = (ArmorSet) Utils.loadXml("armors.xml", ArmorSet.class);
			creatures = (CreatureSet) Utils.loadXml("creatures.xml", CreatureSet.class);
			creatures.init();
			weapons.init();
			armors.init();
			
			initTransportAnimations();
			mainAvatar = avatarAnim;;
					
			//textures for the moongates
			moongateTextures = standardAtlas.findRegions("moongate");
			//textures for the phases of  the moon
			moonAtlas = new TextureAtlas(Gdx.files.internal("assets/graphics/moon-atlas.txt"));

			//font = new BitmapFont(Gdx.files.classpath("fonts/Calisto_18.fnt"));
			font = new BitmapFont();

			font.setColor(Color.WHITE);		
			batch = new SpriteBatch();
			batch.enableBlending();
						
			mapCamera = new OrthographicCamera();
			mapCamera.setToOrtho(false);
			
			stage = new Stage(new ScreenViewport());
			mapObjectsStage = new Stage(new ScreenViewport(mapCamera));
			Maps.WORLD.getMap().setSurfaceMapStage(mapObjectsStage);	      
			projectilesStage = new Stage(new ScreenViewport(mapCamera));

			sip = new SecondaryInputProcessor(this, stage);
			
			SequenceAction seq1 = Actions.action(SequenceAction.class);
			seq1.addAction(Actions.delay(.25f));
			seq1.addAction(Actions.run(gameTimer));
			stage.addAction(Actions.forever(seq1));

			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private void initTransportAnimations() {
		Array<AtlasRegion> avatar = enhancedAtlas.findRegions("avatar");
		Array<AtlasRegion> horse = enhancedAtlas.findRegions("horse");
		Array<AtlasRegion> ship = standardAtlas.findRegions("ship");
		Array<AtlasRegion> balloon = standardAtlas.findRegions("balloon");

		Array<AtlasRegion> tmp = new Array<AtlasRegion>(4);
		for (int i=0;i<4;i++) tmp.add(avatar.get(0));
		avatarAnim = new Animation(0.25f, tmp);
		
		tmp = new Array<AtlasRegion>(4);
		AtlasRegion ar = new AtlasRegion(horse.get(2));
		ar.flip(true, false);
		tmp.add(ar);
		tmp.add(horse.get(2));
		tmp.add(horse.get(2));
		tmp.add(horse.get(2));
		horseAnim = new Animation(0.25f, tmp);
		
		shipAnim = new Animation(0.25f, ship);
		
		tmp = new Array<AtlasRegion>(4);
		for (int i=0;i<4;i++) tmp.add(balloon.get(0));
		balloonAnim = new Animation(0.25f, tmp);
	}
	
	class GameTimer implements Runnable {
		boolean active = true;
		public void run() {
			if (active) {
				
				updateMoons(true);		
				
				if (System.currentTimeMillis() - context.getLastCommandTime() > 20*1000) {
					keyUp(Keys.SPACE);
				}
			}
		}
	}
		
	
	@Override
	public void show() {
		Gdx.input.setInputProcessor(new InputMultiplexer(this, stage));
		gameTimer.active = true;

		//load save game if initializing
		if (context == null) {
			context = new Context();
			SaveGame sg = new SaveGame();
			try {
				sg.read(PARTY_SAV_BASE_FILENAME);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			Party party = new Party(sg);
			context.setParty(party);
			
//			party.getMember(0).getPlayer().hp = 999;
//			party.getMember(0).getPlayer().hpMax = 999;
//			party.getMember(0).getPlayer().intel = 99;
//			party.getMember(0).getPlayer().mp = 999;
//			for (Spell sp : Spell.values()) party.getSaveGame().mixtures[sp.ordinal()] = 99;
//			for (Virtue v : Virtue.values()) {
//				sg.karma[v.ordinal()] = 0;
//			}
//
//			party.join(NpcDefaults.Geoffrey.name());
//			party.join(NpcDefaults.Shamino.name());
//			party.join(NpcDefaults.Katrina.name());
//			party.join(NpcDefaults.Mariah.name());
//			party.join(NpcDefaults.Dupre.name());
//			party.join(NpcDefaults.Iolo.name());
//			party.join(NpcDefaults.Julia.name());
//			party.join(NpcDefaults.Jaana.name());
//			
//			sg.food = 30000;
//			sg.gold = 999;
//			sg.keys = 20;
//			sg.gems = 15;
//			sg.moves = 2800;
//			sg.stones = 0xff;
//			sg.runes = 0xff;
//			sg.items = 0xff;
//			
//			party.getMember(0).getPlayer().status = StatusType.GOOD;
//			party.getMember(0).getPlayer().xp = 899;
//			party.getMember(0).getPlayer().weapon = WeaponType.MYSTICSWORD;
//			party.getMember(0).getPlayer().armor = ArmorType.MYSTICROBE;
//			party.getSaveGame().weapons[9] = 99;
//			party.getSaveGame().armor[6] = 99;
//			party.getSaveGame().sextants = 1;
			
//	    	mainAvatar = shipAnim;
//			sg.transport = 0x10;
			
			//load the surface world first
			loadNextMap(Maps.WORLD, sg.x, sg.y);
			//loadNextMap(Maps.WORLD, 213, 224);

			//load the dungeon if save game starts in dungeon
			if (Maps.get(sg.location) != Maps.WORLD) {
				loadNextMap(Maps.get(sg.location), sg.x, sg.y, sg.x, sg.y, sg.dnglevel, Direction.getByValue(sg.orientation+1), true);
				//loadNextMap(Maps.ABYSS, 0, 0, 5, 5, 0, Direction.SOUTH, true);
				//loadNextMap(Maps.DESPISE, 0, 0, 5, 5, 0, Direction.NORTH, true);
			}
			
			party.setTransport(baseTileSet.getTileByIndex(sg.transport));
			
			switch (sg.transport) {
				case 31: mainAvatar = avatarAnim; break;
				case 16: 
				case 17: 
				case 18: 
				case 19: mainAvatar = shipAnim; break;
				case 20: 
				case 21: mainAvatar = horseAnim; break;
				case 24: mainAvatar = balloonAnim; break;
			}

		}
		
		context.getParty().addObserver(this);

	}
	
	@Override
	public void hide() {
		gameTimer.active = false;
		context.getParty().deleteObserver(this);
	}
	
	public void loadNextMap(Maps m, int x, int y) {
		loadNextMap(m, x, y, 0, 0, 0, null, false);
	}
		
	public void loadNextMap(Maps m, int x, int y, int dngx, int dngy, int dngLevel, Direction orientation, boolean restoreSG) {
		
		log("Entering " + m.getLabel() + "!");
		
		if (m.getMap().getType() == MapType.dungeon) {
			
			DungeonScreen sc = new DungeonScreen(stage, this, m);
			
			if (restoreSG) {
				sc.restoreSaveGameLocation(dngx, dngy, dngLevel, orientation);
			}
			
			mainGame.setScreen(sc);
			
		} else if (m.getMap().getType() == MapType.shrine) {
			BaseMap sm = m.getMap();
			map = new UltimaTiledMapLoader(m, standardAtlas, sm.getWidth(), sm.getHeight(), 16, 16).load();
			context.setCurrentTiledMap(map);
			Virtue virtue = Virtue.get(sm.getId() - 25);
			ShrineScreen sc = new ShrineScreen(this, virtue, map, enhancedAtlas, standardAtlas);
			mainGame.setScreen(sc);
		} else {
			
			BaseMap bm = m.getMap();
			context.setCurrentMap(bm);
			
			bm.removeJoinedPartyMemberFromPeopleList(context.getParty());
					
			map = new UltimaTiledMapLoader(m, standardAtlas, m.getMap().getWidth(), m.getMap().getHeight(), 16, 16).load();
			context.setCurrentTiledMap(map);

			if (renderer != null) renderer.dispose();
			renderer = new UltimaMapRenderer(standardAtlas, bm, map, 2f);

			mapBatch = renderer.getBatch();
	
			MapProperties prop = map.getProperties();
			mapPixelHeight = prop.get("height", Integer.class) * tilePixelWidth;
			
			bm.initObjects(this, enhancedAtlas, standardAtlas);
			
			newMapPixelCoords = getMapPixelCoords(x, y);
			changeMapPosition = true;
		}
						
	}
	
	public void attackAt(Maps combat, Creature cr) {
		
		Maps contextMap = Maps.get(context.getCurrentMap().getId());
		BaseMap combatMap = combat.getMap();
		
		TiledMap tmap = new UltimaTiledMapLoader(combat, standardAtlas, combat.getMap().getWidth(), combat.getMap().getHeight(), 16, 16).load();
		
		CombatScreen sc = new CombatScreen(this, context, contextMap, combatMap, tmap, cr.getTile(), creatures, enhancedAtlas, standardAtlas);
		mainGame.setScreen(sc);
		
		currentEncounter = cr;
	}
	
	@Override
	public void endCombat(boolean isWon, BaseMap combatMap) {
		
		mainGame.setScreen(this);
		
		if (currentEncounter != null) {
					
			Tile tile = context.getCurrentMap().getTile(currentEncounter.currentX, currentEncounter.currentY);
			
			if (isWon) {
				
				log("Victory!");
				
				if (!currentEncounter.getGood()) {
                    context.getParty().adjustKarma(KarmaAction.KILLED_EVIL);
				}
				
				TileRule r = tile.getRule();
				
			    /* add a chest, if the creature leaves one */
			    if (!currentEncounter.getNochest() && (r == null || !r.has(TileAttrib.unwalkable))) {
			    	Tile ct = baseTileSet.getTileByName("chest");
			    	Drawable chest = new Drawable(context.getCurrentMap().getId(), currentEncounter.currentX, currentEncounter.currentY, ct, standardAtlas);
			    	chest.setX(currentEncounter.currentPos.x);
			    	chest.setY(currentEncounter.currentPos.y);
			    	mapObjectsStage.addActor(chest);
			    }
			    /* add a ship if you just defeated a pirate ship */
			    else if (currentEncounter.getTile() == CreatureType.pirate_ship) {
			    	Tile st = baseTileSet.getTileByName("ship");
			    	Drawable ship = new Drawable(context.getCurrentMap().getId(), currentEncounter.currentX, currentEncounter.currentY, st, standardAtlas);
			    	ship.setX(currentEncounter.currentPos.x);
			    	ship.setY(currentEncounter.currentPos.y);
			    	mapObjectsStage.addActor(ship);
			    }
			} else {

				if (context.getParty().didAnyoneFlee()) {
	                log("Battle is lost!");

	                /* minus points for fleeing from evil creatures */
	                if (!currentEncounter.getGood()) {
	                	//lose karma points here
	                    context.getParty().adjustKarma(KarmaAction.FLED_EVIL);
	                } else {
	                	//get extra karma points
	                    context.getParty().adjustKarma(KarmaAction.FLED_GOOD);
	                }
	            } else if (!context.getParty().isAnyoneAlive()) {
	            	partyDeath();
	            }
			}
			
			context.getCurrentMap().removeCreature(currentEncounter);
						
			currentEncounter = null;

		}
	}
	
	public void partyDeath() {
    	//death scene
    	mainGame.setScreen(new DeathScreen(this, context.getParty()));
    	loadNextMap(Maps.CASTLE_OF_LORD_BRITISH_2, REVIVE_CASTLE_X, REVIVE_CASTLE_Y);
	}

	@Override
	public void render(float delta) {
		
		time += delta;

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		if (renderer == null) return;
		
		if (changeMapPosition) {
			mapCamera.position.set(newMapPixelCoords);
			changeMapPosition = false;
		}

		mapCamera.update();
		renderer.setView(mapCamera);
		renderer.render();

		mapBatch.begin();


		if (context.getCurrentMap().getMoongates() != null) {
			for (Moongate g : context.getCurrentMap().getMoongates()) {
				TextureRegion t = g.getCurrentTexture();
				if (t != null) {
					Vector3 v = getMapPixelCoords(g.getX(), g.getY());
					mapBatch.draw(t, v.x, v.y, tilePixelWidth, tilePixelHeight);
				}
			}
		}

		mapBatch.end();
		
		mapObjectsStage.act();
		mapObjectsStage.draw();

		batch.begin();

		//Vector3 v = getCurrentMapCoords();
		//font.draw(batch, "map coords: " + v, 10, 500);
		//font.draw(batch, "fps: " + Gdx.graphics.getFramesPerSecond(), 0, 20);
		//font.draw(batch, "mouse: " + currentMousePos, 10, 70);
		
		Ultima4.hud.render(batch, context.getParty());

		font.setColor(Color.WHITE);
		if (showZstats > 0) {
			context.getParty().getSaveGame().renderZstats(showZstats, font, batch, Ultima4.SCREEN_HEIGHT);
		}

		if (context.getCurrentMap().getId() == Maps.WORLD.getId()) {
			batch.draw(moonAtlas.findRegion("phase_" + trammelphase), 375, Ultima4.SCREEN_HEIGHT - 25, 25, 25);
			batch.draw(moonAtlas.findRegion("phase_" + feluccaphase), 400, Ultima4.SCREEN_HEIGHT - 25, 25, 25);
		}
		
		if (context.getAura().getType() != AuraType.NONE) {
			font.draw(batch, context.getAura().getType().toString(), 430, Ultima4.SCREEN_HEIGHT - 7);
		}

		batch.end();
		
		mapBatch.begin();
		mapBatch.draw(mainAvatar.getKeyFrames()[avatarDirection], mapCamera.position.x, mapCamera.position.y, tilePixelWidth, tilePixelHeight);
		mapBatch.end();
		
		projectilesStage.act();
		projectilesStage.draw();
		
		stage.act();
		stage.draw();
		
	}


	@Override
	public boolean keyUp (int keycode) {
		
		context.setLastCommandTime(System.currentTimeMillis());
				
		Vector3 v = getCurrentMapCoords();
		Tile ct = context.getCurrentMap().getTile(v);
		
		if (keycode == Keys.UP) {
			if (context.getTransportContext() == TransportContext.SHIP && avatarDirection+1 != Direction.NORTH.getVal()) {
				avatarDirection = Direction.NORTH.getVal()-1;
				finishTurn((int)v.x, (int)v.y);
				return false;
			}
			if (!preMove(v, Direction.NORTH)) return false;
			if (mapCamera.position.y + tilePixelHeight > context.getCurrentMap().getHeight() * tilePixelHeight) {
				mapCamera.position.y = 0;
				postMove(Direction.NORTH, (int)v.x, context.getCurrentMap().getHeight());
			} else {
				mapCamera.position.y = mapCamera.position.y + tilePixelHeight;
				postMove(Direction.NORTH, (int)v.x,(int)v.y-1);
			}
			avatarDirection = Direction.NORTH.getVal()-1;
		} else if (keycode == Keys.RIGHT) {
			if (context.getTransportContext() == TransportContext.SHIP && avatarDirection+1 != Direction.EAST.getVal()) {
				avatarDirection = Direction.EAST.getVal()-1;
				finishTurn((int)v.x, (int)v.y);
				return false;
			}
			if (!preMove(v, Direction.EAST)) return false;
			if (mapCamera.position.x + tilePixelWidth > context.getCurrentMap().getWidth() * tilePixelWidth) {
				mapCamera.position.x = 0;
				postMove(Direction.EAST, 0, (int)v.y);
			} else {
				mapCamera.position.x = mapCamera.position.x + tilePixelWidth;
				postMove(Direction.EAST, (int)v.x+1,(int)v.y);
			}
			avatarDirection = Direction.EAST.getVal()-1;
		} else if (keycode == Keys.LEFT) {
			if (context.getTransportContext() == TransportContext.SHIP && avatarDirection+1 != Direction.WEST.getVal()) {
				avatarDirection = Direction.WEST.getVal()-1;
				finishTurn((int)v.x, (int)v.y);
				return false;
			}
			if (!preMove(v, Direction.WEST)) return false;
			if (mapCamera.position.x - tilePixelWidth < 0) {
				mapCamera.position.x = context.getCurrentMap().getWidth() * tilePixelWidth;
				postMove(Direction.WEST, context.getCurrentMap().getWidth(),(int)v.y);
			} else {
				mapCamera.position.x = mapCamera.position.x - tilePixelWidth;
				postMove(Direction.WEST, (int)v.x-1,(int)v.y);
			}
			avatarDirection = Direction.WEST.getVal()-1;
		} else if (keycode == Keys.DOWN) {
			if (context.getTransportContext() == TransportContext.SHIP && avatarDirection+1 != Direction.SOUTH.getVal()) {
				avatarDirection = Direction.SOUTH.getVal()-1;
				finishTurn((int)v.x, (int)v.y);
				return false;
			}
			if (!preMove(v, Direction.SOUTH)) return false;
			if (mapCamera.position.y - tilePixelHeight < 0) {
				mapCamera.position.y = context.getCurrentMap().getHeight() * tilePixelHeight;
				postMove(Direction.SOUTH, (int)v.x, 0);
			} else {
				mapCamera.position.y = mapCamera.position.y - tilePixelHeight;
				postMove(Direction.SOUTH, (int)v.x,(int)v.y+1);
			}
			avatarDirection = Direction.SOUTH.getVal()-1;
		} else if (keycode == Keys.K || keycode == Keys.D) {
			if (ct.climbable()) {
				Portal p = context.getCurrentMap().getPortal(v.x, v.y);
				loadNextMap(Maps.get(p.getDestmapid()), p.getStartx(), p.getStarty());
				log(p.getMessage());
			}
		} else if (keycode == Keys.F && context.getTransportContext() == TransportContext.SHIP) {
			log("Fire Cannon > ");
			ShipInputAdapter sia = new ShipInputAdapter(v);
			Gdx.input.setInputProcessor(sia);
			return false;
			
		} else if (keycode == Keys.H) {

			CombatScreen.holeUp(Maps.WORLD, (int)v.x, (int)v.y, this, context, creatures, standardAtlas, enhancedAtlas);
			return false;

		} else if (keycode == Keys.E) {
			Portal p = context.getCurrentMap().getPortal(v.x, v.y);
			if (p != null) {
				if (Maps.get(p.getDestmapid()).getMap().getType() == MapType.shrine) {
					Virtue virtue = Virtue.get(Maps.get(p.getDestmapid()).getId() - 25);
					if (context.getParty().canEnterShrine(virtue)) {
						loadNextMap(Maps.get(p.getDestmapid()), p.getStartx(), p.getStarty());
					} else {
						log("Thou dost not bear the rune of entry!");
						log("A strange force keeps you out!");
					}
				} else {
					
					Maps dest = Maps.get(p.getDestmapid());
		            if (dest == Maps.ABYSS) {
			            if (	(context.getParty().getSaveGame().items & Item.CANDLE_USED.getLoc()) == 0 ||
			            		(context.getParty().getSaveGame().items & Item.BELL_USED.getLoc()) == 0 ||
			            		(context.getParty().getSaveGame().items & Item.BOOK_USED.getLoc()) == 0) {
							log("A strange force keeps you out!");
			    			return false;
			            }
		            }
					
					loadNextMap(dest, p.getStartx(), p.getStarty());
					return false;
				}
			}
		} else if (keycode == Keys.Q) {
			if (context.getCurrentMap().getId() == Maps.WORLD.getId()) {
				context.saveGame(v.x,v.y,0,null,Maps.WORLD);
				log("Saved Game.");
			} else {
				log("Cannot save inside!");
			}
		} else if (keycode == Keys.L) {
			
			if (context.getCurrentMap().getId() == Maps.WORLD.getId()) {
                if (context.getParty().getSaveGame().sextants >= 1) {
                	log("Locate position with sextant");
                    log(String.format("Latitude:    %s' %s\"", (char)((int)v.y / 16 + 'A'), (char)((int)v.y % 16 + 'A')));
                    log(String.format("Longitude: %s' %s\"", (char)((int)v.x / 16 + 'A'), (char)((int)v.x % 16 + 'A')));
				} else {
                    log("Locate position with what?");
				}
			} else {
            	log("Not here!");
            }
		} else if (keycode == Keys.N) {
			log("New Order:");
			log("exhange #:");
			NewOrderInputAdapter noia = new NewOrderInputAdapter(this);
			Gdx.input.setInputProcessor(noia);
			return false;
			
		} else if (keycode == Keys.S) {

			BaseMap bm = context.getCurrentMap();
			ItemMapLabels l = bm.searchLocation(this, context.getParty(), (int)v.x, (int)v.y, 0);
			if (l != null) {
				log("You found " + l.getDesc() + ".");
			} else {
            	log("Nothing here!");
			}
		} else if (keycode == Keys.M) {
			
			new MixtureDialog(context.getParty(), this, stage, skin).show();
						
		} else if (keycode == Keys.B) {
			board((int)v.x,(int)v.y);
		} else if (keycode == Keys.X) {
			if (context.getTransportContext() == TransportContext.SHIP) {
		    	Tile st = baseTileSet.getTileByName("ship");
		    	Drawable ship = new Drawable(context.getCurrentMap().getId(), (int)v.x, (int)v.y, st, standardAtlas);
		    	ship.setX(mapCamera.position.x);
		    	ship.setY(mapCamera.position.y);
		    	mapObjectsStage.addActor(ship);
			} else if (context.getTransportContext() == TransportContext.HORSE) {
				Creature cr = GameScreen.creatures.getInstance(CreatureType.horse, GameScreen.enhancedAtlas, GameScreen.standardAtlas);
		    	cr.currentX = (int)v.x;
		    	cr.currentY = (int)v.y;
		    	context.getCurrentMap().addCreature(cr);
			} else if (context.getTransportContext() == TransportContext.BALLOON) {
		    	Tile st = baseTileSet.getTileByName("balloon");
		    	Drawable balloon = new Drawable(context.getCurrentMap().getId(), (int)v.x, (int)v.y, st, standardAtlas);
		    	balloon.setX(mapCamera.position.x);
		    	balloon.setY(mapCamera.position.y);
		    	mapObjectsStage.addActor(balloon);
			}
		    context.getParty().setTransport(baseTileSet.getTileByIndex(0x1f));
		    mainAvatar = avatarAnim;
		} else if (keycode == Keys.P) {
			peerGem();
		} else if (keycode == Keys.U) {
			
			log("Use Item:");
			log("");
			ItemInputAdapter iia = new ItemInputAdapter(this);
			Gdx.input.setInputProcessor(iia);
			return false;
			
		} else if (keycode == Keys.T || keycode == Keys.O || keycode == Keys.J || keycode == Keys.L || keycode == Keys.A || keycode == Keys.G || keycode == Keys.R  || keycode == Keys.W) {
			Gdx.input.setInputProcessor(sip);
			sip.setinitialKeyCode(keycode, context.getCurrentMap(), (int)v.x, (int)v.y);
			return false;
			
		} else if (keycode == Keys.C) {
			
			log("Cast Spell: ");
			log("Who casts (1-8): ");
			Gdx.input.setInputProcessor(new SpellInputProcessor(this, stage, (int)v.x, (int)v.y, null));
			return false;	
			
		} else if (keycode == Keys.Z) {
			showZstats = showZstats + 1;
			if (showZstats >= STATS_PLAYER1 && showZstats <= STATS_PLAYER8) {
				if (showZstats > context.getParty().getMembers().size()) showZstats = STATS_WEAPONS;
			}
			if (showZstats > STATS_SPELLS) showZstats = STATS_NONE;
			
		} else if (keycode == Keys.ESCAPE) {
			
			new DocumentationDialog(this, stage, skin).show();

		} else if (keycode == Keys.SPACE) {
			log("Pass");
		}
		
		finishTurn((int)v.x, (int)v.y);

		return false;

	}
	
	private boolean preMove(Vector3 currentTile, Direction dir) {
		
		
		int nx = (int)currentTile.x;
		int ny = (int)currentTile.y;
		if (dir == Direction.NORTH) ny = (int)currentTile.y-1;
		if (dir == Direction.SOUTH) ny = (int)currentTile.y+1;
		if (dir == Direction.WEST) nx = (int)currentTile.x-1;
		if (dir == Direction.EAST) nx = (int)currentTile.x+1;
		
		BaseMap bm = context.getCurrentMap();
		if (bm.getBorderbehavior() == MapBorderBehavior.exit) {
			if (nx > bm.getWidth()-1 || nx < 0 || ny > bm.getHeight()-1 || ny < 0) {
				
				//remove any city/town actors (chests) from the map we are leaving
				for (Actor a : mapObjectsStage.getActors()) {
					if (a instanceof Drawable) {
						Drawable d = (Drawable)a;
						if (d.getMapId() != Maps.WORLD.getId() && d.getMapId() == bm.getId()) {
							d.remove();
						}
					}
				}
				
				Portal p = Maps.WORLD.getMap().getPortal(bm.getId());
				loadNextMap(Maps.WORLD, p.getX(), p.getY());
				return false;
			}
		}
		
		int mask = bm.getValidMovesMask((int)currentTile.x, (int)currentTile.y);
		if (!Direction.isDirInMask(dir, mask)) {
			Sounds.play(Sound.BLOCKED);
			return false;
		}
		
		return true;
	}
	
	private void postMove(Direction dir, int newx, int newy) {
				
		if (context.getCurrentMap().getId() == Maps.WORLD.getId()) {
			
			//check for active moongate portal
			for (Moongate g : context.getCurrentMap().getMoongates()) {
				if (g.getCurrentTexture() != null && newx == g.getX() && newy == g.getY()) {
					Sounds.play(Sound.MOONGATE);
					Vector3 d = getDestinationForMoongate(g);
					if (d != null) {
						newMapPixelCoords = getMapPixelCoords((int)d.x,(int)d.y);
						changeMapPosition = true;
					}
				}
			}
			
		    /* things that happen while not on board the balloon */
		    if (context.getTransportContext() != TransportContext.BALLOON) {
		        checkSpecialCreatures(dir, newx, newy);
				checkBridgeTrolls(newx, newy);
		    }
		}
				
		log(dir.toString());
	}
	
	public void finishTurn(int currentX, int currentY) {
			
		context.getParty().endTurn(context.getCurrentMap().getType());
		
		context.getAura().passTurn();
				
		if (checkRandomCreatures()) {
			spawnCreature(null, currentX, currentY);
		}
		
		context.getCurrentMap().moveObjects(this, currentX, currentY);	
		
		checkHullIntegrity(context.getCurrentMap(), currentX, currentY);
	}
	

	
	public void replaceTile(String name, int x, int y) {
		if (name == null) {
			return;
		}
		TextureRegion texture = standardAtlas.findRegion(name);
		TiledMapTileLayer layer = (TiledMapTileLayer)context.getCurrentTiledMap().getLayers().get("Map Layer");
		Cell cell = layer.getCell(x, context.getCurrentMap().getWidth() - 1 - y);
		TiledMapTile tmt = new StaticTiledMapTile(texture);
		tmt.setId(y * context.getCurrentMap().getWidth() + x);
		cell.setTile(tmt);
		context.getCurrentMap().setTile(baseTileSet.getTileByName(name), x, y);
	}
	
	private boolean checkRandomCreatures() {
	    if (context.getCurrentMap().getId() != Maps.WORLD.getId() ||
	    	context.getCurrentMap().getCreatures().size() >= MAX_CREATURES_ON_MAP) {
	        return false;
	    }
	    return rand.nextInt(32) == 0;
	}
	
	private boolean spawnCreature(Creature creature, int currentX, int currentY) {

		int dx = 0;
        int dy = 0;
        int tmp = 0;
            	
		boolean ok = false;
		int tries = 0;
		int MAX_TRIES = 10;

		while (!ok && (tries < MAX_TRIES)) {
			dx = 15;
			dy = rand.nextInt(15);

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

			/* make sure we can spawn the creature there */
			if (creature != null) {
				Tile tile = context.getCurrentMap().getTile(dx, dy);
				TileRule rule = tile.getRule();
				if ((creature.getSails() && rule.has(TileAttrib.sailable)) || 
						(creature.getSwims() && rule.has(TileAttrib.swimmable)) || 
						(creature.getFlies() && !rule.has(TileAttrib.unflyable))) {
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
	    	Tile tile = context.getCurrentMap().getTile(dx, dy);
	        creature = getRandomCreatureForTile(tile);
	    }
	    
	    if (creature != null) {
	    	creature.currentX = dx;
	    	creature.currentY = dy;
	    	context.getCurrentMap().addCreature(creature);
	    } else {
	    	return false;
	    }
	        
	    return true;
	}
	
	private Creature getRandomCreatureForTile(Tile tile) {

		int era = 0;
		int randId = 0;

		if (tile == null || tile.getRule() == null) {
			System.err.println("randomForTile: Tile or rule is null");
			return null;
		}

		if (tile.getRule().has(TileAttrib.creatureunwalkable)) {
			return null;
		}

		if (tile.getRule().has(TileAttrib.sailable)) {
			randId = CreatureType.pirate_ship.getValue();
			randId += rand.nextInt(7);
			Creature cr = creatures.getInstance(CreatureType.get(randId), enhancedAtlas, standardAtlas);
			return cr;
		} else if (tile.getRule().has(TileAttrib.swimmable)) {
			randId = CreatureType.nixie.getValue();
			randId += rand.nextInt(5);
			Creature cr = creatures.getInstance(CreatureType.get(randId), enhancedAtlas, standardAtlas);
			return cr;
		}

		if (context.getParty().getSaveGame().moves > 30000) {
			era = 15;
		} else if (context.getParty().getSaveGame().moves > 20000) {
			era = 7;
		} else {
			era = 3;
		}

		randId = CreatureType.orc.getValue();
		randId += era & rand.nextInt(16) & rand.nextInt(16);
		Creature cr = creatures.getInstance(CreatureType.get(randId), enhancedAtlas, standardAtlas);
		
		return cr;
	}
		
	
	private void updateMoons(boolean showmoongates) {
		
		// world map only
		if (context.getCurrentMap().getId() == 0) {
			
			context.setMoonPhase(context.getMoonPhase() + 1);
			if (context.getMoonPhase() >= MOON_PHASES * MOON_SECONDS_PER_PHASE * 4) {
				context.setMoonPhase(0);
			}

			phase = (context.getMoonPhase() / (4 * MOON_SECONDS_PER_PHASE));
			feluccaphase = phase % 8;
			trammelphase = phase / 3;
			if (trammelphase > 7) {
				trammelphase = 7;
			}
			trammelSubphase = context.getMoonPhase() % (MOON_SECONDS_PER_PHASE * 4 * 3);

			
			for (Moongate g : context.getCurrentMap().getMoongates()) {
				g.setCurrentTexture(null);
			}

			if (showmoongates) {
				Moongate gate = context.getCurrentMap().getMoongate(trammelphase);
				AtlasRegion texture = null;
				if (trammelSubphase == 0) {
					texture = moongateTextures.get(0);
				} else if (trammelSubphase == 1) {
					texture = moongateTextures.get(1);
				} else if (trammelSubphase == 2) {
					texture = moongateTextures.get(2);
				} else if (trammelSubphase == 3) {
					texture = moongateTextures.get(3);
				} else if ((trammelSubphase > 3) && (trammelSubphase < (MOON_SECONDS_PER_PHASE * 4 * 3) - 3)) {
					texture = moongateTextures.get(3);
				} else if (trammelSubphase == (MOON_SECONDS_PER_PHASE * 4 * 3) - 3) {
					texture = moongateTextures.get(2);
				} else if (trammelSubphase == (MOON_SECONDS_PER_PHASE * 4 * 3) - 2) {
					texture = moongateTextures.get(1);
				} else if (trammelSubphase == (MOON_SECONDS_PER_PHASE * 4 * 3) - 1) {
					texture = moongateTextures.get(0);
				}
				gate.setCurrentTexture(texture);
			}
			
		}
	}
	
	private Vector3 getDestinationForMoongate(Moongate m) {
		Vector3 dest = new Vector3(m.getX(), m.getY(), 0);
		String destGate = null;
		
		if (feluccaphase == m.getDm1()) destGate = m.getD1();
		if (feluccaphase == m.getDm2()) destGate = m.getD2();
		if (feluccaphase == m.getDm3()) destGate = m.getD3();
		
		 if (destGate.equals("shrine of spirituality")) {
			if (context.getParty().canEnterShrine(Virtue.SPIRITUALITY)) {
				loadNextMap(Maps.SHRINE_SPIRITUALITY, 0, 0);
			} else {
				log("Thou dost not bear the rune of entry!");
				log("A strange force keeps you out!");
			}
			return null;
		 }

		for(Moongate dm : context.getCurrentMap().getMoongates()) {
			if (dm.getName().equals(destGate)) {
				dest = new Vector3(dm.getX(), dm.getY(), 0);
			}
		}
		
		return dest;
	}
	
	private void checkSpecialCreatures(Direction dir, int x, int y) {

	    /*
	     * if heading east into pirates cove (O'A" N'N"), generate pirate ships
	     */
	    if (dir == Direction.EAST && x == 0xdd && y == 0xe0) {
	        for (PirateCoveInfo pci : PirateCoveInfo.values()) {        
				Creature pirate = creatures.getInstance(CreatureType.pirate_ship, enhancedAtlas, standardAtlas);
				pirate.currentX = pci.getX();
				pirate.currentY = pci.getY();
				pirate.currentPos = getMapPixelCoords(pci.getX(), pci.getY());
				pirate.sailDir = pci.getFacing();
		    	context.getCurrentMap().addCreature(pirate);
	        }
	    }

	    /*
	     * if heading south towards the shrine of humility, generate
	     * daemons unless horn has been blown
	     */    
	    if (dir == Direction.SOUTH && x >= 229 && x < 234 && y >= 212 && y < 217 &&	context.getAura().getType() != AuraType.HORN) {
	        for (int i = 0; i < 8; i++) {  
				Creature daemon = creatures.getInstance(CreatureType.daemon, enhancedAtlas, standardAtlas);
				daemon.currentX = 231;
				daemon.currentY = y + 1;
				daemon.currentPos = getMapPixelCoords(231, y + 1);
		    	context.getCurrentMap().addCreature(daemon);
	        }
	    }
	}
	
	private void checkBridgeTrolls(int x, int y) {
	    Tile bridge = context.getCurrentMap().getTile(x, y);
	    
	    if (bridge == null || !bridge.getName().equals("bridge")) {
	        return;
	    }

	    if (rand.nextInt(8) != 0) {
	        return;
	    }

	    log("Bridge Trolls!");
	    
		Creature troll = creatures.getInstance(CreatureType.troll, enhancedAtlas, standardAtlas);
		troll.currentX = x;
		troll.currentY = y;
		troll.currentPos = getMapPixelCoords(x, y);
		attackAt(bridge.getCombatMap(), troll);
	}
	
	public void board(int x, int y) {
		
	    if (context.getTransportContext() != TransportContext.FOOT) {
	        log("Board: Can't!");
	        return;
	    }
	    
	    Tile tile = null;
	    
	    //check for ship
		Drawable ship = null;
		for (Actor a : mapObjectsStage.getActors()) {
			if (a instanceof Drawable) {
				Drawable d = (Drawable)a;
				if (d.getTile().getName().equals("ship") && d.getCx() == x && d.getCy() == y) {
					ship = d;
					tile = d.getTile();
				}
			}
		}
		
		//check for horse
		Creature horse = context.getCurrentMap().getCreatureAt(x, y);
	    if (horse != null && (horse.getTile() == CreatureType.horse)) {
	    	tile = baseTileSet.getTileByName("horse");
	    }
		
		
	    //check for balloon
		Drawable balloon = null;
		for (Actor a : mapObjectsStage.getActors()) {
			if (a instanceof Drawable) {
				Drawable d = (Drawable)a;
				if (d.getTile().getName().equals("balloon") && d.getCx() == x && d.getCy() == y) {
					balloon = d;
					tile = d.getTile();
				}
			}
		}
	    
	    if (tile == null) {
	    	log("Board What?");
	        return;
	    }

	    if (tile.getRule().has(TileAttrib.ship)) {
	    	log("Board Frigate!");
	        if (context.getLastShip() != ship) {
	            context.getParty().adjustShipHull(50);
	        }
	        context.setCurrentShip(ship);
	    	ship.remove();
	    	mainAvatar = shipAnim;
	    	
	    } else if (tile.getRule().has(TileAttrib.horse)) {
	    	log("Mount Horse!");
	    	context.getCurrentMap().removeCreature(horse);
	    	mainAvatar = horseAnim;

	    } else if (tile.getRule().has(TileAttrib.balloon)) {
	    	log("Board Balloon!");
	    	balloon.remove();
	    	mainAvatar = balloonAnim;

	    } else {
	    	log("Board What?");
	        return;
	    }

	    context.getParty().setTransport(tile);

	}
	
	class ShipInputAdapter extends InputAdapter {
		Vector3 pos;
		ShipInputAdapter(Vector3 pos) {
			this.pos = pos;
		}
		@Override
		public boolean keyUp(int keycode) {
			Direction fireDir = null;
			
			if (keycode == Keys.LEFT) {
				if (avatarDirection+1 == Direction.NORTH.getVal()) fireDir = Direction.WEST;
				if (avatarDirection+1 == Direction.SOUTH.getVal()) fireDir = Direction.WEST;
			} else if (keycode == Keys.RIGHT) {
				if (avatarDirection+1 == Direction.NORTH.getVal()) fireDir = Direction.EAST;
				if (avatarDirection+1 == Direction.SOUTH.getVal()) fireDir = Direction.EAST;
			} else if (keycode == Keys.UP) {
				if (avatarDirection+1 == Direction.EAST.getVal()) fireDir = Direction.NORTH;
				if (avatarDirection+1 == Direction.WEST.getVal()) fireDir = Direction.NORTH;
			} else if (keycode == Keys.DOWN) {
				if (avatarDirection+1 == Direction.EAST.getVal()) fireDir = Direction.SOUTH;
				if (avatarDirection+1 == Direction.WEST.getVal()) fireDir = Direction.SOUTH;
			}
			
			if (fireDir != null) {
				logAppend(fireDir.toString());
				AttackVector av = Utils.avatarfireCannon(mapObjectsStage, GameScreen.context.getCurrentMap(), fireDir, (int)pos.x, (int)pos.y);
				Utils.animateCannonFire(GameScreen.this, projectilesStage, context.getCurrentMap(), av, (int)pos.x, (int)pos.y, true);
			} else {
				log("Broadsides only!");
			}
						
			Gdx.input.setInputProcessor(new InputMultiplexer(GameScreen.this, stage));
			return false;
		}
	}
	
	
	public void getChest(int index, int x, int y) {

		if (context.getParty().isFlying()) {
			log("Not in a ballon!");
			return;
		}
		
		boolean found = false;

		Drawable chest = null;
		Array<Actor> as = mapObjectsStage.getActors();
		for (Actor a : as) {
			if (a instanceof Drawable) {
				Drawable d = (Drawable)a;
				if (StringUtils.equals("chest", d.getTile().getName()) && d.getCx() == x && d.getCy() == y) {
					chest = (Drawable)a;
					found = true;
					chest.remove();
					break;
				}
			}
		}
		
		
		if (chest == null) {
			//check tile too, ie in cities
			Tile tile = context.getCurrentMap().getTile(x, y);
			if (tile.getRule() == TileRule.chest) {
				replaceTile("brick_floor", x, y);
				found = true;
			}
		}

		if (found) {
			PartyMember pm = context.getParty().getMember(index);
			context.getChestTrapHandler(pm);
			log(String.format("The Chest Holds: %d Gold", context.getParty().getChestGold()));
			if (context.getCurrentMap().getType() == MapType.city) {
				context.getParty().adjustKarma(KarmaAction.STOLE_CHEST);
			}
		} else {
			log("Not Here!");
		}
		
	}
	
	public void peerGem() {
		if (context.getParty().getSaveGame().gems > 0) {
	        context.getParty().getSaveGame().gems--;
	        log("Peer at a Gem!");
			Gdx.input.setInputProcessor(new PeerGemInputAdapter());
		} else {
			log("Thou dost have no gems!");
		}
	}

	public void peerTelescope() {
		log("You see a knob on the");
		log("telescope marked A-P.");
		log("You select:");
		Gdx.input.setInputProcessor(new PeerTelescopeInputAdapter());
	}
	
	public class PeerGemInputAdapter extends InputAdapter {
		Image img;
		public PeerGemInputAdapter() {
			try {
				Texture t = null;
				if (context.getCurrentMap().getId() == Maps.WORLD.getId()) {
					Vector3 v = getCurrentMapCoords();
					t = Utils.peerGem(context.getCurrentMap(), (int)v.x, (int)v.y, standardAtlas);
				} else {
					t = Utils.peerGem(Maps.get(context.getCurrentMap().getId()), standardAtlas);
				}
				img = new Image(t);
				img.setX(0);
				img.setY(0);
				img.addAction(sequence(Actions.alpha(0), Actions.fadeIn(1f, Interpolation.fade)));
		        stage.addActor(img);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		@Override
		public boolean keyUp(int keycode) {
			//if (keycode == Keys.ENTER || keycode == Keys.SPACE) {
				if (img != null) {
					img.remove();
				}
				Gdx.input.setInputProcessor(new InputMultiplexer(GameScreen.this, stage));
			//}
			return false;
		}
	}
	
	public class PeerTelescopeInputAdapter extends InputAdapter {
		Image img;
		@Override
		public boolean keyUp(int keycode) {
			if (keycode >= Keys.A && keycode <= Keys.P) {
				if (img != null) return false;
				Maps map = Maps.get(keycode - Keys.A + 1);
				log(Keys.toString(keycode).toUpperCase() + " - " + map.getLabel());
				try {
					Texture t = Utils.peerGem(map, standardAtlas);
					img = new Image(t);
					img.setX(0);
					img.setY(0);
					img.addAction(sequence(Actions.alpha(0), Actions.fadeIn(1f, Interpolation.fade)));
			        stage.addActor(img);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else { //if (keycode == Keys.ENTER || keycode == Keys.SPACE) {
				if (img != null) {
					img.remove();
				}
				Gdx.input.setInputProcessor(new InputMultiplexer(GameScreen.this, stage));
			}
			return false;
		}
	}
	
	class ItemInputAdapter extends InputAdapter {
		GameScreen screen;
		StringBuilder buffer = new StringBuilder();
		public ItemInputAdapter(GameScreen screen) {
			this.screen = screen;
		}

		@Override
		public boolean keyUp(int keycode) {
			if (keycode == Keys.ENTER) {
				if (buffer.length() < 1) return false;
				String text = buffer.toString().toUpperCase();	
				try {
					Item item = Item.valueOf(Item.class, text);

					switch (item) {
					case BOOK:
					case BELL:
					case CANDLE:
						screen.useBBC(item);
						break;
					case WHEEL:
						screen.useWheel();
						break;
					case SKULL:
						screen.useSkull();
						break;
					case HORN:
						screen.useHorn();
						break;
					default:
						screen.log("What?");
						break;
					}
					
					Gdx.input.setInputProcessor(new InputMultiplexer(screen, stage));
					
					Vector3 v = getCurrentMapCoords();
					screen.finishTurn((int)v.x, (int)v.y);
					
				} catch (IllegalArgumentException e) {
					screen.log("What?");
					Gdx.input.setInputProcessor(new InputMultiplexer(screen, stage));
				}
				
			} else if (keycode == Keys.BACKSPACE) {
				if (buffer.length() > 0) {
					buffer.deleteCharAt(buffer.length() - 1);
					screen.logDeleteLastChar();
				}
			} else if (keycode >= 29 && keycode <= 54) {
				buffer.append(Keys.toString(keycode).toUpperCase());
				screen.logAppend(Keys.toString(keycode).toUpperCase());
			}
			return false;
		}
	}
	
	

	public void useHorn() {
		
	    if ((context.getParty().getSaveGame().items & Item.HORN.getLoc()) == 0) {
	        log("None owned!");
	        return;
	    }
		
	    log("The Horn sounds an eerie tone!");
	    context.getAura().set(AuraType.HORN, 10);    
	}


	public void useWheel() {
		
	    if ((context.getParty().getSaveGame().items & Item.WHEEL.getLoc()) == 0) {
	        log("None owned!");
	        return;
	    }
	    
		if (context.getTransportContext() == TransportContext.SHIP && context.getParty().getSaveGame().shiphull == 50) {
	        log("Once mounted, the Wheel glows with a blue light!");
	        context.getParty().getSaveGame().shiphull = 99;
	    } else {
	    	log("Hmm...No effect!");    
	    }
	}

	/**
	 * Use skull on the entrance to the Abyss
	 */
	public void useSkull() {

	    if ((context.getParty().getSaveGame().items & Item.SKULL.getLoc()) == 0) {
	        log("None owned!");
	        return;
	    }
	    
	    if ((context.getParty().getSaveGame().items & Item.SKULL_DESTROYED.getLoc()) > 0) {
	        log("None owned!");
	        return;
	    }
	    
	    if (context.getCurrentMap().getId() != Maps.WORLD.getId()) {
	    	log("Hmm...No effect!");
	        return;
	    }
	    
		Vector3 v = getCurrentMapCoords();
		int x = (int)v.x;
		int y = (int)v.y;

	    if (x == 0xe9 && y == 0xe9) {
	        log("You cast the Skull of Mondain into the Abyss!");
	        context.getParty().getSaveGame().items = (context.getParty().getSaveGame().items & ~Item.SKULL.getLoc()) | Item.SKULL_DESTROYED.getLoc();
	        context.getParty().adjustKarma(KarmaAction.DESTROYED_SKULL);
	    } else {
	        log("You hold the evil Skull of Mondain the Wizard aloft...");
	        
	        PartyMember user = context.getParty().getMember(0);
	        SpellUtil.destoryAllCreatures(this, user);
	        
	        context.getParty().adjustKarma(KarmaAction.USED_SKULL);
	    }
	}
	
	/**
	 * Use bell, book, or candle on the entrance to the Abyss
	 */
	public void useBBC(Item item) {
		
	    if ((context.getParty().getSaveGame().items & item.getLoc()) == 0) {
	        log("None owned!");
	        return;
	    }
		
		Vector3 v = getCurrentMapCoords();
		int x = (int)v.x;
		int y = (int)v.y;

		
	    /* on top of the Abyss entrance */
	    if (context.getCurrentMap().getId() == Maps.WORLD.getId()  && x == 0xe9 && y == 0xe9) {
	        /* must use bell first */
	        if (item == Item.BELL) {
	            log("The Bell rings on and on!");
	            context.getParty().getSaveGame().items |= Item.BELL_USED.getLoc();
	        }
	        
	        /* then the book */
	        else if (item == Item.BOOK && (context.getParty().getSaveGame().items & Item.BELL_USED.getLoc()) > 0) {
	            log("The words resonate with the ringing!");
	            context.getParty().getSaveGame().items |= Item.BOOK_USED.getLoc();
	        }
	        
	        /* then the candle */
	        else if (item == Item.CANDLE && (context.getParty().getSaveGame().items & Item.BOOK_USED.getLoc()) > 0) {
	            log("As you light the Candle the Earth Trembles!\n");    
	            context.getParty().getSaveGame().items |= Item.CANDLE_USED.getLoc();
	        }
	        
	        else {
	        	log("Hmm...No effect!");
	        }
	        
	    } else {
	    	log("Hmm...No effect!");
	    }
	}
	
	private void checkHullIntegrity(BaseMap bm, int x, int y) {

		boolean killAll = false;
		if (context.getTransportContext() == TransportContext.SHIP && context.getParty().getSaveGame().shiphull <= 0) {
			log("Thy ship sinks!");
			killAll = true;
		} else if (context.getTransportContext() == TransportContext.FOOT && bm.getTile(x,y).getRule().has(TileAttrib.sailable)) {
			log("Trapped at sea without thy ship, thou dost drown!");
			killAll = true;
		}

		if (killAll) {
			context.getParty().killAll();
	    	context.getParty().setTransport(baseTileSet.getTileByIndex(0x1f));
	    	mainAvatar = avatarAnim;
        	partyDeath();
		}
	}
	

}
