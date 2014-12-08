package ultima;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.forever;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import objects.BaseMap;
import objects.Creature;
import objects.CreatureSet;
import objects.Party;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class CombatScreen extends BaseScreen {
	
	public Ultima4 mainGame;
	
	public static int AREA_CREATURES = 16;
	public static int AREA_PLAYERS  =  8;
	
	private CreatureType[] crSlots = new CreatureType[AREA_CREATURES];
	
	private List<Creature> players = new ArrayList<Creature>();
	private CursorActor cursor;

	private Maps contextMap;
	private BaseMap combatMap;
	private TiledMap tmap;
	private CreatureType crType;
	CreatureSet creatureSet;
	
	private Party party;
	private Stage stage;
	
	private OrthogonalTiledMapRenderer renderer;
	private SpriteBatch batch;
	
	public CombatScreen(Screen returnScreen, Party party, Maps contextMap, BaseMap combatMap, TiledMap tmap, CreatureType cr, CreatureSet cs, TextureAtlas a1, TextureAtlas a2) {
		this.returnScreen = returnScreen;
		this.contextMap = contextMap;
		this.combatMap = combatMap;
		this.tmap = tmap;
		this.crType = cr;
		this.party = party;
		this.creatureSet = cs;
		
		
		renderer = new OrthogonalTiledMapRenderer(tmap, 2f);
		
		MapProperties prop = tmap.getProperties();
		mapPixelHeight = prop.get("height", Integer.class) * tilePixelWidth;
		
		mapCamera = new OrthographicCamera();
		mapCamera.setToOrtho(false);
		stage = new Stage();
		stage.setViewport(new ScreenViewport(mapCamera));

		cursor = new CursorActor();
		stage.addActor(cursor);
		cursor.addAction(forever(sequence(fadeOut(2), fadeIn(2))));

		batch = new SpriteBatch();
		
	    fillCreatureTable(crType);
	    
		MapLayer mLayer = tmap.getLayers().get("Monster Positions");
		Iterator<MapObject> iter = mLayer.getObjects().iterator();
		while(iter.hasNext()) {
			MapObject obj = iter.next();
			int index = Integer.parseInt((String)obj.getProperties().get("index"));
			int startX = Integer.parseInt((String)obj.getProperties().get("startX"));
			int startY = Integer.parseInt((String)obj.getProperties().get("startY"));
			
			if (crSlots[index] == null) continue;
			
			Creature c = creatureSet.getInstance(crSlots[index], a2, a1);
			
			c.currentX = startX;
			c.currentY = startY;
			c.currentPos = getMapPixelCoords(startX, startY);

			combatMap.addCreature(c);
		}
		
		MapLayer pLayer = tmap.getLayers().get("Player Positions");
		iter = pLayer.getObjects().iterator();
		while(iter.hasNext()) {
			MapObject obj = iter.next();
			int index = Integer.parseInt((String)obj.getProperties().get("index"));
			int startX = Integer.parseInt((String)obj.getProperties().get("startX"));
			int startY = Integer.parseInt((String)obj.getProperties().get("startY"));
			
			if (index + 1 > party.getSaveGame().members) continue;
			
			Creature c = creatureSet.getInstance(CreatureType.get(party.getMember(index).getPlayer().klass.toString().toLowerCase()), a2, a1);
			c.currentX = startX;
			c.currentY = startY;
			c.currentPos = getMapPixelCoords(startX, startY);

			players.add(c);
			
			if (index == 0) cursor.setPos(startX, startY);
		}
		
		newMapPixelCoords = getMapPixelCoords(5, 5);
		changeMapPosition = true;
	}
	

	@Override
	public void show() {
		Gdx.input.setInputProcessor(this);
	}
	
	private void fillCreatureTable(CreatureType ct) {
		   
		if (ct == null) return;
		
		int numCreatures = getNumberOfCreatures(ct);

		CreatureType baseType = ct;
		if (baseType == CreatureType.pirate_ship) {
			baseType = CreatureType.rogue;
		}

		for (int i = 0; i < numCreatures; i++) {
			CreatureType current = baseType;

			/* find a free spot in the creature table */
			int j = 0;
			do {
				j = rand.nextInt(AREA_CREATURES);
			} while (crSlots[j] != null);

			/* see if creature is a leader or leader's leader */
			if (CreatureType.get(baseType.getCreature().getLeader()) != baseType.getCreature().getTile() && i != (numCreatures - 1)) { 
				if (rand.nextInt(32) == 0) { // leader's leader
					CreatureType t1 = CreatureType.get(baseType.getCreature().getLeader());
					CreatureType t2 = CreatureType.get(t1.getCreature().getLeader());
					current = t2;
				}
				else if (rand.nextInt(8) == 0) { // leader
					current = CreatureType.get(baseType.getCreature().getLeader());
				}
			}

			/* place this creature in the creature table */
			crSlots[j] = current;
		}
		
	}
	
	private int getNumberOfCreatures(CreatureType ct) {
		int ncreatures = 0;

		if (contextMap == Maps.WORLD || contextMap.getMap().getType() == MapType.dungeon) {

			ncreatures = rand.nextInt(8) + 1;

			if (ncreatures == 1) {
				if (ct != null && ct.getCreature().getEncounterSize() > 0) {
					ncreatures = rand.nextInt(ct.getCreature().getEncounterSize()) + ct.getCreature().getEncounterSize() + 1;
				} else {
					ncreatures = 8;
				}
			}

			while (ncreatures > 2 * party.getSaveGame().members) {
				ncreatures = rand.nextInt(16) + 1;
			}

		} else {
			if (ct != null && ct.getCreature().getTile() == CreatureType.guard) {
				ncreatures = party.getSaveGame().members * 2;
			} else {
				ncreatures = 1;
			}
		}
    
		return ncreatures;
	}
	

	
	@Override
	public void render(float delta) {
		
		time += delta;

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		if (changeMapPosition) {
			mapCamera.position.set(newMapPixelCoords);
			changeMapPosition = false;
		}

		mapCamera.update();
		renderer.setView(mapCamera);
		renderer.render();
		
		stage.act();
		stage.draw();
		
		renderer.getBatch().begin();
		for (Creature cr : combatMap.getCreatures()) {
			if (cr.currentPos == null  ) {
				continue;
			}
			renderer.getBatch().draw(cr.getAnim().getKeyFrame(time, true), cr.currentPos.x, cr.currentPos.y, tilePixelWidth, tilePixelHeight);
		}
		
		for (Creature cr : players) {
			if (cr.currentPos == null  ) {
				continue;
			}
			renderer.getBatch().draw(cr.getAnim().getKeyFrame(time, true), cr.currentPos.x, cr.currentPos.y, tilePixelWidth, tilePixelHeight);
		}
		
		renderer.getBatch().end();


		
		
	}
	

	@Override
	public boolean keyUp (int keycode) {
		
		Creature active = players.get(party.getActivePlayer());
		
		if (keycode == Keys.UP) {
			if (!preMove(active.currentX,active.currentY, Direction.NORTH)) return false;
			active.currentY--;
			active.currentPos = getMapPixelCoords(active.currentX,active.currentY);
			
		} else if (keycode == Keys.DOWN) {
			if (!preMove(active.currentX,active.currentY, Direction.SOUTH)) return false;
			active.currentY++;
			active.currentPos = getMapPixelCoords(active.currentX,active.currentY);
			
		} else if (keycode == Keys.RIGHT) {
			if (!preMove(active.currentX,active.currentY, Direction.EAST)) return false;
			active.currentX++;
			active.currentPos = getMapPixelCoords(active.currentX,active.currentY);
			
		} else if (keycode == Keys.LEFT) {
			if (!preMove(active.currentX,active.currentY, Direction.WEST)) return false;
			active.currentX--;
			active.currentPos = getMapPixelCoords(active.currentX,active.currentY);
						
		}
		
		finishTurn(active.currentX,active.currentY);

		return false;

	}
	
	private boolean preMove(int x, int y, Direction dir) {
				
		Vector3 next = null;
		if (dir == Direction.NORTH) next = new Vector3(x,y-1,0);
		if (dir == Direction.SOUTH) next = new Vector3(x,y+1,0);
		if (dir == Direction.EAST) next = new Vector3(x+1,y,0);
		if (dir == Direction.WEST) next = new Vector3(x-1,y,0);
				
		if (next.x > combatMap.getWidth()-1 || next.x < 0 || next.y > combatMap.getHeight()-1 || next.y < 0) {
			mainGame.setScreen(returnScreen);
			return false;
		}
		
		int mask = combatMap.getValidMovesMask(x, y);
		if (!Direction.isDirInMask(dir, mask)) {
			Sounds.play(Sound.BLOCKED);
			return false;
		}
		
		return true;
	}
	
	public void finishTurn(int currentX, int currentY) {
		
		party.endTurn(MapType.combat);
	
		combatMap.moveObjects(this, currentX, currentY);
		
	}
	
	private Texture getCursorTexture() {
		Pixmap pixmap = new Pixmap(tilePixelHeight,tilePixelHeight, Format.RGBA8888);
		pixmap.setColor(0.9f, 0.9f, 0.9f, 0.7f);
		int w = 4;
		pixmap.fillRectangle(0, 0, w, tilePixelHeight);
		pixmap.fillRectangle(tilePixelHeight - w, 0, w, tilePixelHeight);
		pixmap.fillRectangle(w, 0, tilePixelHeight-2*w, w);
		pixmap.fillRectangle(w, tilePixelHeight - w, tilePixelHeight-2*w, w);
		return new Texture(pixmap);
	}
	
	class CursorActor extends Actor {
		Texture texture;
		Sprite sprite;
		int x;
		int y;
		CursorActor() {
			texture = getCursorTexture();
			sprite = new Sprite(texture);
		}
		void setPos(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		@Override
		public void draw(Batch batch, float parentAlpha) {
			Vector3 v = getMapPixelCoords(x, y);
			batch.draw(sprite, v.x, v.y);
		}
		
	}

}
