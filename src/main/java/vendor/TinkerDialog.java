package vendor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import objects.Party;
import ultima.BaseScreen;
import ultima.Constants;
import ultima.GameScreen;
import ultima.Sound;
import ultima.Sounds;

public class TinkerDialog extends Window implements Constants {

    private Skin skin;
    boolean cancelHide;
    Actor previousKeyboardFocus, previousScrollFocus;
    FocusListener focusListener;

    Party party;
    GameScreen screen;
    Stage stage;

    List<String> ownedList;
    List<String> tinkeringList;

    TextButton add;
    TextButton clear;
    TextButton mix;
    TextButton exit;

    public static int width = 400;
    public static int height = 300;
    static BitmapFont font = new BitmapFont(Gdx.files.internal("assets/fonts/corsiva-20.fnt"), false);

    Table internalTable;
    Table buttonTable;

    public TinkerDialog(Party party, GameScreen screen, Stage stage, Skin skin) {
        super("", skin.get("dialog", WindowStyle.class));
        setSkin(skin);
        this.skin = skin;
        this.stage = stage;
        this.screen = screen;
        this.party = party;
        initialize();
    }

    private void initialize() {
        
        screen.gameTimer.active = false;

        setModal(true);

        internalTable = new Table(skin);
        internalTable.defaults().pad(5);

        defaults().space(10).pad(2);
        add(internalTable).expand().fill();
        row();

        internalTable.add(new Label("Owned Items", skin)).align(Align.left);
        internalTable.add();
        internalTable.add(new Label("Tinkering Table", skin)).align(Align.left);
        internalTable.row();

        Array<String> tmp = new Array<>();
        for (Constants.Item item : Constants.Item.values()) {
            if ((party.getSaveGame().items & (1 << item.ordinal())) > 0 && item.isVisible()) {
                tmp.add(item.getDesc());
            }
        }

        ownedList = new List<>(skin);
        ownedList.setItems(tmp);

        tinkeringList = new List<>(skin);
        tinkeringList.setItems(new Array<>());

        buttonTable = new Table(skin);
        buttonTable.defaults().padLeft(20).padRight(20).padTop(5);

        add = new TextButton("Add", skin, "wood");
        add.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                String sel = ownedList.getSelected();
                if (sel == null) {
                    return;
                }
                tinkeringList.getItems().add(sel);
                //tinkeringList.setItems(tmp);
            }
        });
        buttonTable.add(add).expandX().left().width(100);

        clear = new TextButton("Clear", skin, "wood");
        clear.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                tinkeringList.clearItems();
            }
        });
        buttonTable.row();
        buttonTable.add(clear).expandX().left().width(100).padBottom(30);

        mix = new TextButton("Tinker", skin, "wood");
        mix.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {

                if (tinkeringList == null || tinkeringList.getItems() == null || tinkeringList.getItems().size == 0) {
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                    animateText("Bah, that won't work!", Color.WHITE, 10, 180, 350, 180, 2f);
                    return;
                }

                if (500 > party.getSaveGame().gold) {
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                    animateText("I fear you have not the funds, perhaps another time..", Color.WHITE, 10, 180, 200, 180, 2.5f);
                    return;
                }

                if (tinkeringList.getItems().contains(Constants.Item.IRON_ORE.getDesc(), false)
                        && tinkeringList.getItems().contains(Constants.Item.RUNE_MOLD.getDesc(), false)) {

                    Sounds.play(Sound.POSITIVE_EFFECT);
                    animateText("Excellent! you have made the Iron Rune!", Color.GREEN, 10, 180, 250, 180, 2.5f);

                    party.adjustGold(-500);
                    party.getSaveGame().items = (party.getSaveGame().items & ~Constants.Item.IRON_ORE.getLoc());
                    party.getSaveGame().items = (party.getSaveGame().items & ~Constants.Item.RUNE_MOLD.getLoc());
                    party.getSaveGame().items |= Constants.Item.IRON_RUNE.getLoc();
                    party.getMember(0).awardXP(400);
                    party.adjustKarma(Constants.KarmaAction.FOUND_ITEM);

                } else if (tinkeringList.getItems().contains(Constants.Item.IRON_RUNE.getDesc(), false)
                        && tinkeringList.getItems().contains(Constants.Item.SONG_HUM.getDesc(), false)
                        && tinkeringList.getItems().contains(Constants.Item.PARCH.getDesc(), false)
                        && (party.getSaveGame().runes & Constants.Virtue.HUMILITY.getLoc()) == 0) {

                    Sounds.play(Sound.POSITIVE_EFFECT);
                    animateText("Congatulations! You now have the Rune of Humility!", Color.GREEN, 10, 180, 250, 180, 2.5f);

                    party.adjustGold(-500);
                    party.getSaveGame().items = (party.getSaveGame().items & ~Constants.Item.IRON_RUNE.getLoc());
                    party.getSaveGame().items = (party.getSaveGame().items & ~Constants.Item.SONG_HUM.getLoc());
                    party.getSaveGame().items = (party.getSaveGame().items & ~Constants.Item.PARCH.getLoc());
                    party.getSaveGame().runes |= Constants.Virtue.HUMILITY.getLoc();
                    party.adjustKarma(Constants.KarmaAction.FOUND_ITEM);
                    party.getMember(0).awardXP(500);

                } else {

                    Sounds.play(Sound.NEGATIVE_EFFECT);
                    animateText("Oh I'm afraid t'is a messy failure we have heahh..", Color.RED, 10, 180, 250, 180, 2f);
                }

                tinkeringList.clearItems();
                ownedList.clearItems();

                Array<String> tmp = new Array<>();
                for (Constants.Item item : Constants.Item.values()) {
                    if ((party.getSaveGame().items & (1 << item.ordinal())) > 0 && item.isVisible()) {
                        tmp.add(item.getDesc());
                    }
                }
                ownedList.setItems(tmp);

            }
        });
        buttonTable.row();
        buttonTable.add(mix).expandX().left().width(100);

        exit = new TextButton("Quit", skin, "wood");
        exit.addListener(new ChangeListener() {
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                hide();
            }
        });
        buttonTable.row();
        buttonTable.add(exit).expandX().left().width(100);

        internalTable.add(ownedList).align(Align.top).minWidth(150).minHeight(150);
        internalTable.add(buttonTable).align(Align.top);
        internalTable.add(tinkeringList).align(Align.top).padRight(10).minWidth(150).minHeight(150);
        internalTable.row();

        focusListener = new FocusListener() {
            @Override
            public void keyboardFocusChanged(FocusListener.FocusEvent event, Actor actor, boolean focused) {
                if (!focused) {
                    focusChanged(event);
                }
            }

            @Override
            public void scrollFocusChanged(FocusListener.FocusEvent event, Actor actor, boolean focused) {
                if (!focused) {
                    focusChanged(event);
                }
            }

            private void focusChanged(FocusListener.FocusEvent event) {
                Stage stage = getStage();
                if (isModal() && stage != null && stage.getRoot().getChildren().size > 0 && stage.getRoot().getChildren().peek() == TinkerDialog.this) {
                    Actor newFocusedActor = event.getRelatedActor();
                    if (newFocusedActor != null && !newFocusedActor.isDescendantOf(TinkerDialog.this) && !(newFocusedActor.equals(previousKeyboardFocus) || newFocusedActor.equals(previousScrollFocus))) {
                        event.cancel();
                    }
                }
            }
        };

    }

    private void animateText(String text, Color color, float sx, float sy, float dx, float dy, float delay) {
        Label label = new Label(text, skin);
        label.setPosition(sx, sy);
        label.setColor(color);
        stage.addActor(label);
        label.addAction(sequence(Actions.moveTo(dx, dy, delay), Actions.fadeOut(1f), Actions.removeActor(label)));
    }

    protected void workspace(Stage stage) {
        if (stage == null) {
            addListener(focusListener);
        } else {
            removeListener(focusListener);
        }
        super.setStage(stage);
    }

    public TinkerDialog show(Action action) {
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
        //stage.setKeyboardFocus(input);
        stage.setScrollFocus(this);

        if (action != null) {
            addAction(action);
        }
        
        animateText("Welcome to the my tinker's shop, how can I help you?", Color.YELLOW, 10, 180, 200, 180, 3f);

        return this;
    }

    public TinkerDialog show() {
        show(sequence(Actions.alpha(0), Actions.fadeIn(0.4f, Interpolation.fade)));
        setPosition(Math.round((stage.getWidth() - getWidth()) / 2), Math.round((stage.getHeight() - getHeight()) / 2));
        return this;
    }

    public void hide(Action action) {

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

    }

    public void hide() {
        hide(sequence(fadeOut(0.4f, Interpolation.fade), Actions.removeListener(ignoreTouchDown, true), Actions.removeActor()));
    }

    protected InputListener ignoreTouchDown = new InputListener() {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            event.cancel();
            return false;
        }
    };

}
