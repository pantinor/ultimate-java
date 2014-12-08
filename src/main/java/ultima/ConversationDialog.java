package ultima;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import objects.Conversation.Topic;
import objects.LordBritishConversation;
import objects.Party;
import objects.Party.PartyMember;
import objects.Person;
import vendor.BaseVendor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;

public class ConversationDialog extends Window implements Constants {
	
	private Skin skin;
	boolean cancelHide;
	Actor previousKeyboardFocus, previousScrollFocus;
	FocusListener focusListener;
	GameScreen mainGame;
	Person person;
	BaseVendor vendor;
	
	public static int width = 300;
	public static int height = 400;
	static BitmapFont font = new BitmapFont(Gdx.files.classpath("fonts/corsiva-20.fnt"), false);
	
	Table internalTable;
	TextField input;
	LogScrollPane scrollPane;
	Topic previousTopic;

	public ConversationDialog(Person p, GameScreen game, Skin skin) {
		super("", skin.get("dialog", WindowStyle.class));
		setSkin(skin);
		this.skin = skin;
		this.mainGame = game;
		this.person = p;
		initialize();
	}

	private void initialize() {
		setModal(true);
		
		defaults().space(10);
		add(internalTable = new Table(skin)).expand().fill();
		row();

		internalTable.defaults().pad(1);
		
		scrollPane = new LogScrollPane(skin, width, "logs");
		scrollPane.setHeight(height);
		
		input = new TextField("",skin);
		input.setTextFieldListener(new TextFieldListener() {
			public void keyTyped (TextField tf, char key) {
				
				if (key == '\r') {
					
					if (tf.getText().length() == 0) {
						if (!cancelHide) {
							hide();
						}
						cancelHide = false;
					}
					
					if (person.getConversation() != null) {
						
						String query = tf.getText();
						Topic t = person.getConversation().matchTopic(query);
						if (t != null) {
							
							if (t.getQuery().equals("join")) {
								String name = person.getConversation().getName();
								Virtue virtue = GameScreen.context.getParty().getVirtueForJoinable(name);
								if (virtue != null) {
									CannotJoinError join = GameScreen.context.getParty().join(name);
									if (join == CannotJoinError.JOIN_SUCCEEDED) {
										scrollPane.add("I am honored to join thee!");
									} else {
										scrollPane.add("Thou art not " + (join == CannotJoinError.JOIN_NOT_VIRTUOUS ? virtue.getDescription() : "experienced") + " enough for me to join thee.");
									}
								}
							} else {
							
								scrollPane.add(t.getPhrase());
								if (t.getQuestion() != null) {
									scrollPane.add(t.getQuestion());
								}
							}
							
							previousTopic = t;
						} else {
							
							if (previousTopic != null && previousTopic.getQuestion() != null) {
								if (query.toLowerCase().contains("y")) {
									scrollPane.add(previousTopic.getYesResponse());
								} else {
									scrollPane.add(previousTopic.getNoResponse());
									if (previousTopic.isLbHeal()) {
										for (PartyMember pm : GameScreen.context.getParty().getMembers()) {
											pm.heal(HealType.CURE);
											pm.heal(HealType.FULLHEAL);
										}
										Sounds.play(Sound.MOONGATE);
									}
								}
							} else {
								scrollPane.add("That I cannot help thee with.");
							}
							previousTopic = null;
						}
						
					} else if (person.getRole() != null && vendor != null) {

						String input = tf.getText();
						vendor.setResponse(input);
						vendor.nextDialog();

					}
					
					tf.setText("");
				}
			}
		});
		
		defaults().pad(5);
		
		internalTable.add(scrollPane).maxWidth(width).width(width);
		internalTable.row();
		internalTable.add(input).maxWidth(width).width(width);

		focusListener = new FocusListener() {
			public void keyboardFocusChanged(FocusEvent event, Actor actor, boolean focused) {
				if (!focused)
					focusChanged(event);
			}

			public void scrollFocusChanged(FocusEvent event, Actor actor, boolean focused) {
				if (!focused)
					focusChanged(event);
			}

			private void focusChanged(FocusEvent event) {
				Stage stage = getStage();
				if (isModal() && stage != null && stage.getRoot().getChildren().size > 0 && stage.getRoot().getChildren().peek() == ConversationDialog.this) {
					Actor newFocusedActor = event.getRelatedActor();
					if (newFocusedActor != null && !newFocusedActor.isDescendantOf(ConversationDialog.this) && !(newFocusedActor.equals(previousKeyboardFocus) || newFocusedActor.equals(previousScrollFocus)))
						event.cancel();
				}
			}
		};
		
		
		person.setTalking(true);
		
		if (person.getConversation() != null) {
			
			if (person.getRole() != null && person.getRole().getRole().equals("lordbritish")) {
				
				LordBritishConversation conv = (LordBritishConversation)person.getConversation();
				scrollPane.add(conv.intro());
				
				boolean playSound = false;
				Party party = GameScreen.context.getParty();
				if (party.getMember(0).getPlayer().status == StatusType.DEAD) {
					party.getMember(0).heal(HealType.RESURRECT);
					party.getMember(0).heal(HealType.FULLHEAL);
					playSound = true;
				}
				
				for (int i = 0; i < party.getMembers().size(); i++) {
					PartyMember pm = party.getMember(i);
					if (pm.getPlayer().advanceLevel()) {
						playSound = true;
						scrollPane.add(pm.getPlayer().name + " thou art now level "+pm.getPlayer().getLevel());
					}
				}
				
				if (playSound) Sounds.play(Sound.MOONGATE);
								
			} else {
				scrollPane.add("You meet " + person.getConversation().getDescription().toLowerCase() + ".");
			}
			
		} else if (person.getRole() != null && person.getRole().getInventoryType() != null) {
			
			vendor = GameScreen.vendorClassSet.getVendorImpl(person.getRole().getInventoryType(), 
					Maps.get(GameScreen.context.getCurrentMap().getId()), GameScreen.context.getParty());
			
			vendor.setScrollPane(scrollPane);
			vendor.nextDialog();
		}

	}

	protected void workspace(Stage stage) {
		if (stage == null)
			addListener(focusListener);
		else
			removeListener(focusListener);
		super.setStage(stage);
	}


	public ConversationDialog show(Stage stage, Action action) {
		clearActions();
		removeCaptureListener(ignoreTouchDown);

		previousKeyboardFocus = null;
		Actor actor = stage.getKeyboardFocus();
		if (actor != null && !actor.isDescendantOf(this))
			previousKeyboardFocus = actor;

		previousScrollFocus = null;
		actor = stage.getScrollFocus();
		if (actor != null && !actor.isDescendantOf(this))
			previousScrollFocus = actor;

		pack();
		stage.addActor(this);
		stage.setKeyboardFocus(input);
		stage.setScrollFocus(this);
		
		if (action != null)
			addAction(action);

		return this;
	}

	public ConversationDialog show(Stage stage) {
		show(stage, sequence(Actions.alpha(0), Actions.fadeIn(0.4f, Interpolation.fade)));
		setPosition(Math.round((stage.getWidth() - getWidth()) / 2), Math.round((stage.getHeight() - getHeight()) / 2));
		return this;
	}

	public void hide(Action action) {
		
		Stage stage = getStage();
		if (stage != null) {
			removeListener(focusListener);
			if (previousKeyboardFocus != null && previousKeyboardFocus.getStage() == null)
				previousKeyboardFocus = null;
			Actor actor = stage.getKeyboardFocus();
			if (actor == null || actor.isDescendantOf(this))
				stage.setKeyboardFocus(previousKeyboardFocus);

			if (previousScrollFocus != null && previousScrollFocus.getStage() == null)
				previousScrollFocus = null;
			actor = stage.getScrollFocus();
			if (actor == null || actor.isDescendantOf(this))
				stage.setScrollFocus(previousScrollFocus);
		}
		if (action != null) {
			addCaptureListener(ignoreTouchDown);
			addAction(sequence(action, Actions.removeListener(ignoreTouchDown, true), Actions.removeActor()));
		} else {
			remove();
		}
		
		Gdx.input.setInputProcessor(new InputMultiplexer(mainGame, stage));
		
		if (GameScreen.context.getCurrentMap().getCity()!=null) 
			GameScreen.context.getCurrentMap().getCity().resetTalkingFlags();

	}

	public void hide() {
		hide(sequence(fadeOut(0.4f, Interpolation.fade), Actions.removeListener(ignoreTouchDown, true), Actions.removeActor()));
	}
	
	protected InputListener ignoreTouchDown = new InputListener() {
		public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
			event.cancel();
			return false;
		}
	};
}
