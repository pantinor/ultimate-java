package ultima;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import objects.Conversation;
import objects.Conversation.Topic;
import objects.CustomInputConversation;
import objects.HawkwindConversation;
import objects.LordBritishConversation;
import objects.Party;
import objects.Party.PartyMember;
import objects.Person;
import util.LogScrollPane;
import vendor.BaseVendor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;

public class ConversationDialog extends Window implements Constants {

    boolean cancelHide;
    Actor previousKeyboardFocus, previousScrollFocus;
    FocusListener focusListener;
    BaseScreen screen;
    Person person;
    BaseVendor vendor;
    Stage stage;

    public static int width = 300;
    public static int height = 400;

    Table internalTable;
    TextField input;
    LogScrollPane scrollPane;
    Topic previousTopic;

    public ConversationDialog(Person p, BaseScreen screen, Stage stage) {
        super("", Ultima4.skin.get("dialog", WindowStyle.class));
        setSkin(Ultima4.skin);
        this.stage = stage;
        this.screen = screen;
        this.person = p;
        initialize();
    }

    private void initialize() {
        setModal(true);

        defaults().space(10);
        add(internalTable = new Table(Ultima4.skin)).expand().fill();
        row();

        internalTable.defaults().pad(1);

        scrollPane = new LogScrollPane(Ultima4.skin, width);
        scrollPane.setHeight(height);

        input = new TextField("", Ultima4.skin);
        input.setTextFieldListener(new TextFieldListener() {
            @Override
            public void keyTyped(TextField tf, char key) {

                if (key == '\r') {

                    if (tf.getText().length() == 0) {
                        if (!cancelHide) {
                            hide();
                        }
                        cancelHide = false;
                    }

                    Conversation conversation = person.getConversation();

                    if (conversation != null) {

                        if (conversation instanceof CustomInputConversation) {
                            ((CustomInputConversation) conversation).setParty(screen.context.getParty());
                        }

                        String query = tf.getText();
                        Topic t = conversation.matchTopic(query);
                        if (t != null) {

                            if (t.getQuery() != null && t.getQuery().equals("join")) {
                                String name = conversation.getName();
                                Virtue virtue = screen.context.getParty().getVirtueForJoinable(name);
                                if (virtue != null) {
                                    CannotJoinError join = screen.context.getParty().join(name);
                                    if (join == CannotJoinError.JOIN_SUCCEEDED) {
                                        scrollPane.add("I am honored to join thee!");
                                        screen.context.getCurrentMap().removeJoinedPartyMemberFromPeopleList(screen.context.getParty());
                                    } else {
                                        scrollPane.add("Thou art not " + (join == CannotJoinError.JOIN_NOT_VIRTUOUS ? virtue.getDescription() : "experienced") + " enough for me to join thee.");
                                    }
                                } else {
                                    scrollPane.add("I cannot join thee.");
                                }

                            } else {
                                
                                if (!conversation.isStandardQuery(query)) {
                                    screen.context.addEntry(conversation.getName(), conversation.getMap(), t.getPhrase());
                                }
                                
                                scrollPane.add(t.getPhrase());
                                if (t.getQuestion() != null) {
                                    scrollPane.add(t.getQuestion());
                                }
                            }

                            previousTopic = t;
                        } else {

                            if (previousTopic != null && previousTopic.getQuestion() != null) {
                                if (query.toLowerCase().contains("y")) {
                                    
                                    screen.context.addEntry(conversation.getName(), conversation.getMap(), previousTopic.getYesResponse());
                                    scrollPane.add(previousTopic.getYesResponse());

                                    if (conversation.getRespAffectsHumility() > 0) {
                                        screen.context.getParty().adjustKarma(KarmaAction.BRAGGED);
                                    }
                                } else {
                                    
                                    screen.context.addEntry(conversation.getName(), conversation.getMap(), previousTopic.getNoResponse());
                                    scrollPane.add(previousTopic.getNoResponse());
                                    
                                    if (previousTopic.isLbHeal()) {
                                        for (PartyMember pm : screen.context.getParty().getMembers()) {
                                            pm.heal(HealType.CURE);
                                            pm.heal(HealType.FULLHEAL);
                                        }
                                        Sounds.play(Sound.HEALING);
                                    }
                                    if (conversation.getRespAffectsHumility() > 0) {
                                        screen.context.getParty().adjustKarma(KarmaAction.HUMBLE);
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
            @Override
            public void keyboardFocusChanged(FocusEvent event, Actor actor, boolean focused) {
                if (!focused) {
                    focusChanged(event);
                }
            }

            @Override
            public void scrollFocusChanged(FocusEvent event, Actor actor, boolean focused) {
                if (!focused) {
                    focusChanged(event);
                }
            }

            private void focusChanged(FocusEvent event) {
                Stage stage = getStage();
                if (isModal() && stage != null && stage.getRoot().getChildren().size > 0 && stage.getRoot().getChildren().peek() == ConversationDialog.this) {
                    Actor newFocusedActor = event.getRelatedActor();
                    if (newFocusedActor != null && !newFocusedActor.isDescendantOf(ConversationDialog.this) && !(newFocusedActor.equals(previousKeyboardFocus) || newFocusedActor.equals(previousScrollFocus))) {
                        event.cancel();
                    }
                }
            }
        };

        person.setTalking(true);

        if (person.getConversation() != null) {

            if (person.getRole() != null && person.getRole().getRole().equals("lordbritish")) {

                LordBritishConversation conv = (LordBritishConversation) person.getConversation();
                scrollPane.add(conv.intro(screen.context));

                SequenceAction seq = Actions.action(SequenceAction.class);
                Party party = screen.context.getParty();
                if (party.getMember(0).getPlayer().status == StatusType.DEAD) {
                    party.getMember(0).heal(HealType.RESURRECT);
                    party.getMember(0).heal(HealType.FULLHEAL);
                    seq.addAction(Actions.run(new LBAction(Sound.HEALING, "I resurrect thee.")));
                    seq.addAction(Actions.delay(3f));
                }

                for (int i = 0; i < party.getMembers().size(); i++) {
                    PartyMember pm = party.getMember(i);
                    if (pm.getPlayer().advanceLevel()) {
                        seq.addAction(Actions.run(new LBAction(Sound.MAGIC, pm.getPlayer().name + " thou art now level " + pm.getPlayer().getLevel())));
                        seq.addAction(Actions.delay(3f));
                    }
                }

                stage.addAction(seq);

            } else if (person.getRole() != null && person.getRole().getRole().equals("hawkwind")) {

                HawkwindConversation conv = (HawkwindConversation) person.getConversation();
                conv.setParty(screen.context.getParty());
                scrollPane.add(conv.intro());

            } else {
                scrollPane.add("You meet " + person.getConversation().getDescription().toLowerCase() + ".");
            }

        } else if (person.getRole() != null && person.getRole().getInventoryType() != null) {

            vendor = Ultima4.vendorClassSet.getVendorImpl(person.getRole().getInventoryType(),
                    Maps.get(screen.context.getCurrentMap().getId()), screen.context);
            vendor.setScreen(screen);
            vendor.setScrollPane(scrollPane);
            vendor.nextDialog();
        }

    }

    class LBAction implements Runnable {

        private Sound sound;
        private String message;

        public LBAction(Sound sound, String message) {
            this.sound = sound;
            this.message = message;
        }

        @Override
        public void run() {
            Sounds.play(sound);
            scrollPane.add(message);
        }
    }

    protected void workspace(Stage stage) {
        if (stage == null) {
            addListener(focusListener);
        } else {
            removeListener(focusListener);
        }
        super.setStage(stage);
    }

    public ConversationDialog show(Stage stage, Action action) {
        clearActions();
        removeCaptureListener(ignoreTouchDown);

        previousKeyboardFocus = null;
        Actor actor = stage.getKeyboardFocus();
        if (actor != null && !actor.isDescendantOf(this)) {
            previousKeyboardFocus = actor;
        }

        previousScrollFocus = null;
        actor = stage.getScrollFocus();
        if (actor != null && !actor.isDescendantOf(this)) {
            previousScrollFocus = actor;
        }

        pack();
        stage.addActor(this);
        stage.setKeyboardFocus(input);
        stage.setScrollFocus(this);

        if (action != null) {
            addAction(action);
        }

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
            if (previousKeyboardFocus != null && previousKeyboardFocus.getStage() == null) {
                previousKeyboardFocus = null;
            }
            Actor actor = stage.getKeyboardFocus();
            if (actor == null || actor.isDescendantOf(this)) {
                stage.setKeyboardFocus(previousKeyboardFocus);
            }

            if (previousScrollFocus != null && previousScrollFocus.getStage() == null) {
                previousScrollFocus = null;
            }
            actor = stage.getScrollFocus();
            if (actor == null || actor.isDescendantOf(this)) {
                stage.setScrollFocus(previousScrollFocus);
            }
        }
        if (action != null) {
            addCaptureListener(ignoreTouchDown);
            addAction(sequence(action, Actions.removeListener(ignoreTouchDown, true), Actions.removeActor()));
        } else {
            remove();
        }

        Gdx.input.setInputProcessor(new InputMultiplexer(screen, stage));

        if (screen.context.getCurrentMap().getCity() != null) {
            screen.context.getCurrentMap().getCity().resetTalkingFlags();
        }

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
