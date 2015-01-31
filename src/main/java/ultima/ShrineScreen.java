package ultima;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import java.util.Random;

import objects.Party;

import org.apache.commons.lang3.StringUtils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class ShrineScreen extends BaseScreen {
	
	private AvatarActor avatar;
	public Party party;
	private Stage stage;
	private Virtue virtue;
	private TiledMap tmap;
	private OrthogonalTiledMapRenderer renderer;
	private SpriteBatch batch;
	private SecondaryInputProcessor sip;
	TextureAtlas runeVisionAtlas;
	
	//only for shrines
	private Virtue meditationVirtue;
	private String mantra;
	private StringBuilder buffer;
	
	private Random rand = new Random();
	
	public static final int SHRINE_MEDITATION_INTERVAL = 100;
	public static final int MEDITATION_MANTRAS_PER_CYCLE = 16;
	
	private int completedCycles;
	private int cycles;

	public ShrineScreen(BaseScreen returnScreen, Virtue virtue, TiledMap tmap, TextureAtlas a1, TextureAtlas a2) {
		
		scType = ScreenType.SHRINE;

		this.returnScreen = returnScreen;
		this.party = GameScreen.context.getParty();
		this.tmap = tmap;
		this.virtue = virtue;
		
		renderer = new OrthogonalTiledMapRenderer(tmap, 2f);
		
		MapProperties prop = tmap.getProperties();
		mapPixelHeight = prop.get("height", Integer.class) * tilePixelWidth;
		
		mapCamera = new OrthographicCamera();
		mapCamera.setToOrtho(false);
		stage = new Stage();
		stage.setViewport(new ScreenViewport(mapCamera));

		runeVisionAtlas = new TextureAtlas(Gdx.files.internal("assets/tilemaps/runes-visions.atlas"));

		
		Vector3 v1 = getMapPixelCoords(5, 10);
		Vector3 v2 = getMapPixelCoords(5, 9);
		Vector3 v3 = getMapPixelCoords(5, 8);
		Vector3 v4 = getMapPixelCoords(5, 7);
		Vector3 v5 = getMapPixelCoords(5, 6);
		
		avatar = new AvatarActor(a1.findRegion("avatar"));
		avatar.setPos(v1);
		stage.addActor(avatar);
		
		avatar.addAction(sequence(
				delay(.8f, moveTo(v2.x, v2.y, .1f)),
				delay(.8f, moveTo(v3.x, v3.y, .1f)),
				delay(.8f, moveTo(v4.x, v4.y, .1f)),
				delay(.8f, moveTo(v5.x, v5.y, .1f)), new Action() {
					public boolean act(float delta) {
					    log("Upon which virtue dost thou meditate?");
					    log("");
						return true;
					}
				}
				));

		batch = new SpriteBatch();
		
		font = new BitmapFont();
		font.setColor(Color.WHITE);		

		sip = new SecondaryInputProcessor(this, stage);
						
		newMapPixelCoords = getMapPixelCoords(5, 5);
		changeMapPosition = true;
		
		log("You enter the ancient shrine and sit before the altar...");
		
		buffer = new StringBuilder();
				
	}
	
	@Override
	public void show() {
		Gdx.input.setInputProcessor(new ShrineInputAdapter(this));
		party.addObserver(this);
	}
	
	@Override
	public void hide() {
		party.deleteObserver(this);
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
				
		batch.begin();
//		font.draw(batch, "moves: " + party.getSaveGame().moves, 5, 360);
//		font.draw(batch, "lastmeditation: " + party.getSaveGame().lastmeditation, 5, 340);
//		font.draw(batch, "divisor moves: " + party.getSaveGame().moves / SHRINE_MEDITATION_INTERVAL, 5, 320);
//		font.draw(batch, "karma: " + party.getSaveGame().karma[virtue.ordinal()], 5, 300);


		Ultima4.hud.render(batch, party);
		batch.end();
		
		stage.act();
		stage.draw();

		
	}
	
	public void partyDeath() {
		//not used here
	}

	@Override
	public void finishTurn(int currentX, int currentY) {
		// TODO Auto-generated method stub
		
	}
	
	class AvatarActor extends Actor {
		TextureRegion texture;
		boolean visible = true;
		AvatarActor(TextureRegion texture) {
			this.texture = texture;
		}
		void setPos(Vector3 v) {
			setX(v.x);
			setY(v.y);
		}

		@Override
		public void draw(Batch batch, float parentAlpha) {
			Color color = getColor();
			batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
			
			batch.draw(texture, getX(), getY());
		}
		
	}
	
	@Override
	public boolean keyUp (int keycode) {
		mainGame.setScreen(returnScreen);
		return false;
	}
	
	public void meditate(int cycles) {
		if (meditationVirtue == null || meditationVirtue != this.virtue) {
			log("Thou art unable to focus thy thoughts on this subject!");
		} else {
		    if (((party.getSaveGame().moves / SHRINE_MEDITATION_INTERVAL) >= 0x10000) || 
		    		(((party.getSaveGame().moves / SHRINE_MEDITATION_INTERVAL) & 0xffff) != party.getSaveGame().lastmeditation)) {
		        log("Begin Meditation");
		        log("");
		        this.cycles = cycles;
		        meditationCycle();
		        
		    } else { 
		        log("Thy mind is still weary from thy last Meditation!");
				mainGame.setScreen(returnScreen);
		    }
		}
	}
	
	private void meditationCycle() {
        party.getSaveGame().lastmeditation = (party.getSaveGame().moves / SHRINE_MEDITATION_INTERVAL) & 0xffff;
        
		SequenceAction seq = Actions.action(SequenceAction.class);
        for (int i = 0; i < MEDITATION_MANTRAS_PER_CYCLE; i++) {
			seq.addAction(Actions.run(new LogAction()));
			seq.addAction(Actions.delay(1f));
		}
		seq.addAction(Actions.run(new MeditateAction()));
		stage.addAction(seq);
	}
	
	public void showVision(boolean elevated) {
	    if (elevated) {
	        log("Thou art granted a vision!");
	        TextureRegion vision = runeVisionAtlas.findRegion(virtue.toString());
	        AvatarActor runeVisionActor = new AvatarActor(vision);
			Vector3 pos = getMapPixelCoords(3, 7);
	        runeVisionActor.setPos(pos);
	        runeVisionActor.addAction(sequence(fadeOut(.01f), fadeIn(3f)));
	        stage.addActor(runeVisionActor);
	        Sounds.play(Sound.SLEEP);
	    } else {
	    	int index = virtue.ordinal() * 3 + completedCycles - 1;
	    	if (index >= shrineAdvice.length) index = shrineAdvice.length - 1;
	        log(shrineAdvice[index]);
	    }
		Gdx.input.setInputProcessor(this);
	}
		
	class LogAction implements Runnable {
		@Override
		public void run() {
			logAppend(". ");
		}
	}
	
	class MeditateAction implements Runnable {
		@Override
		public void run() {
	        log("Mantra?");
	        log("");
			Gdx.input.setInputProcessor(new MantraInputAdapter(ShrineScreen.this));
		}
	}
	
	
	class ShrineInputAdapter extends InputAdapter {
		public Virtue v;
		ShrineScreen screen;

		public ShrineInputAdapter(ShrineScreen screen) {
			this.screen = screen;
			buffer = new StringBuilder();
		}

		@Override
		public boolean keyUp(int keycode) {
			if (keycode == Keys.ENTER) {
				if (buffer.length() < 1) return false;
				String text = buffer.toString().toUpperCase();	
				try {
					meditationVirtue = Virtue.valueOf(Virtue.class, text);
					screen.log("For how many Cycles (0-3)?");
					Gdx.input.setInputProcessor(sip);

				} catch (IllegalArgumentException e) {
					screen.log("Thou art unable to focus thy thoughts on this subject!");
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
	
	class MantraInputAdapter extends InputAdapter {
		ShrineScreen screen;
		public MantraInputAdapter(ShrineScreen screen) {
			this.screen = screen;
			buffer = new StringBuilder();
		}

		@Override
		public boolean keyUp(int keycode) {
			if (keycode == Keys.ENTER) {
				if (buffer.length() < 1) return false;
				mantra = buffer.toString().toUpperCase();	
			    if (!StringUtils.equals(mantra, virtue.getMantra())) {
			        party.adjustKarma(KarmaAction.BAD_MANTRA);
			        returnScreen.log("Thou art not able to focus thy thoughts with that Mantra!");
					mainGame.setScreen(returnScreen);
			    } else {
			        if (--cycles > 0) {
			            completedCycles++;
				        party.adjustKarma(KarmaAction.MEDITATION);
				        log("");
			            meditationCycle();
			        } else {
			            completedCycles++;
				        party.adjustKarma(KarmaAction.MEDITATION);
			            boolean elevated = completedCycles == 3 && party.attemptElevation(virtue);
			            if (elevated) {
			                log("Thou hast achieved partial Avatarhood");
			                log("in the Virtue of " + virtue.toString());
			            } else {
			                log("Thy thoughts are pure. ");
			            }
			            showVision(elevated);
			        }
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

}
