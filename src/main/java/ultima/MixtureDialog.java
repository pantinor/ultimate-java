package ultima;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import java.util.ArrayList;

import objects.Party;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.utils.Align;

public class MixtureDialog extends Window implements Constants {

    boolean cancelHide;
    Actor previousKeyboardFocus, previousScrollFocus;
    FocusListener focusListener;

    Party party;
    BaseScreen screen;
    Stage stage;

    ReagentCount[] owned;
    ReagentCount[] mixing;
    MixtureCount[] mixtures;

    List<ReagentCount> ownedList;
    List<ReagentCount> mixedList;
    List<MixtureCount> mixtureList;

    TextButton add;
    TextButton clear;
    TextButton mix;
    TextButton exit;

    SelectBox<Spell> spellSelect;

    public static int width = 400;
    public static int height = 300;

    Table internalTable;
    Table buttonTable;

    public MixtureDialog(Party party, BaseScreen screen, Stage stage) {
        super("", Ultima4.skin.get("dialog", WindowStyle.class));
        setSkin(Ultima4.skin);
        this.stage = stage;
        this.screen = screen;
        this.party = party;

        initialize();
    }

    class ReagentCount {

        public Reagent rgnt;
        public int count;

        ReagentCount(Reagent rt, int count) {
            this.rgnt = rt;
            this.count = count;
        }

        @Override
        public String toString() {
            return rgnt.getDesc() + " (" + count + ")";
        }
    }

    class MixtureCount {

        public Spell spell;
        public int count;

        MixtureCount(Spell sp, int count) {
            this.spell = sp;
            this.count = count;
        }

        @Override
        public String toString() {
            return spell.getDesc() + " (" + count + ")";
        }
    }

    private void initialize() {
        setModal(true);

        internalTable = new Table(Ultima4.skin);
        internalTable.defaults().pad(5);

        defaults().space(10).pad(2);
        add(internalTable).expand().fill();
        row();

        spellSelect = new SelectBox<>(Ultima4.skin);
        spellSelect.setItems(Spell.values());
        spellSelect.setSelected(Spell.AWAKEN);

        internalTable.add();
        internalTable.add(new Label("Spell:", Ultima4.skin)).align(Align.right);
        internalTable.add(spellSelect);
        internalTable.add();
        internalTable.row();

        internalTable.add(new Label("Reagents", Ultima4.skin)).align(Align.left);
        internalTable.add();
        internalTable.add(new Label("Current Batch", Ultima4.skin)).align(Align.left);
        internalTable.add(new Label("Mixtures", Ultima4.skin)).align(Align.left);
        internalTable.row();

        ArrayList<ReagentCount> tmp = new ArrayList<>();
        for (Reagent r : Reagent.values()) {
            if (party.getSaveGame().reagents[r.ordinal()] > 0) {
                tmp.add(new ReagentCount(r, party.getSaveGame().reagents[r.ordinal()]));
            }
        }
        owned = (ReagentCount[]) tmp.toArray(new ReagentCount[tmp.size()]);

        ArrayList<MixtureCount> tmp2 = new ArrayList<>();
        for (int i = 0; i < Spell.values().length; i++) {
            int count = party.getSaveGame().mixtures[i];
            if (count > 0) {
                tmp2.add(new MixtureCount(Spell.get(i), count));
            }
        }
        mixtures = (MixtureCount[]) tmp2.toArray(new MixtureCount[tmp2.size()]);

        ownedList = new List<>(Ultima4.skin);
        ownedList.setItems(owned);

        mixedList = new List<>(Ultima4.skin);

        mixtureList = new List<>(Ultima4.skin);
        mixtureList.setItems(mixtures);

        buttonTable = new Table(Ultima4.skin);
        buttonTable.defaults().padLeft(20).padRight(20).padTop(5);

        add = new TextButton("Add", Ultima4.skin);
        add.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {

                ReagentCount sel = ownedList.getSelected();
                if (sel == null || sel.count == 0) {
                    return;
                }
                ReagentCount rc = new ReagentCount(sel.rgnt, 1);
                if (mixing != null) {
                    for (int i = 0; i < mixing.length; i++) {
                        if (mixing[i] != null && mixing[i].rgnt == sel.rgnt) {
                            mixing[i].count++;
                            rc = mixing[i];
                            break;
                        }
                    }
                }

                sel.count--;

                ArrayList<ReagentCount> tmp = new ArrayList<>();
                tmp.add(rc);
                if (mixing != null) {
                    for (int i = 0; i < mixing.length; i++) {
                        if (mixing[i] != null && mixing[i].rgnt != rc.rgnt) {
                            tmp.add(mixing[i]);
                        }
                    }
                }

                mixing = (ReagentCount[]) tmp.toArray(new ReagentCount[tmp.size()]);
                mixedList.setItems(mixing);

            }
        });
        buttonTable.add(add).expandX().left().width(100);

        clear = new TextButton("Clear", Ultima4.skin);
        clear.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {

                mixedList.clearItems();
                mixing = null;

                ArrayList<ReagentCount> tmp = new ArrayList<>();
                for (Reagent r : Reagent.values()) {
                    if (party.getSaveGame().reagents[r.ordinal()] > 0) {
                        tmp.add(new ReagentCount(r, party.getSaveGame().reagents[r.ordinal()]));
                    }
                }
                owned = (ReagentCount[]) tmp.toArray(new ReagentCount[tmp.size()]);
                ownedList.setItems(owned);
            }
        });
        buttonTable.row();
        buttonTable.add(clear).expandX().left().width(100).padBottom(30);

        mix = new TextButton("Mix", Ultima4.skin);
        mix.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {

                if (mixing == null) {
                    animateText("Nothing is mixed!", Color.WHITE, 300, 180, 500, 180);
                    return;
                }

                int mask = 0;
                for (ReagentCount rc : mixing) {
                    if (rc == null) {
                        continue;
                    }
                    mask = mask | rc.rgnt.getMask();
                }

                Spell sp = spellSelect.getSelected();
                if (sp == null) {
                    return;
                }
                if (mask == sp.getMask()) {
                    Sounds.play(Sound.POSITIVE_EFFECT);
                    animateText("Success!", Color.GREEN, 300, 180, 500, 180);

                    party.getSaveGame().mixtures[sp.ordinal()]++;

                    ArrayList<MixtureCount> tmp2 = new ArrayList<>();
                    for (int i = 0; i < Spell.values().length; i++) {
                        int count = party.getSaveGame().mixtures[i];
                        if (count > 0) {
                            tmp2.add(new MixtureCount(Spell.get(i), count));
                        }
                    }
                    mixtures = (MixtureCount[]) tmp2.toArray(new MixtureCount[tmp2.size()]);
                    mixtureList.setItems(mixtures);

                } else {
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                    animateText("It Fizzles!", Color.RED, 300, 180, 500, 180);
                }

                for (ReagentCount x : owned) {
                    party.getSaveGame().reagents[x.rgnt.ordinal()] = x.count;
                }

                mixedList.clearItems();
                mixing = null;

            }
        });
        buttonTable.row();
        buttonTable.add(mix).expandX().left().width(100);

        exit = new TextButton("Quit", Ultima4.skin);
        exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hide();
            }
        });
        buttonTable.row();
        buttonTable.add(exit).expandX().left().width(100);

        internalTable.add(ownedList).align(Align.top).minWidth(150).minHeight(150);
        internalTable.add(buttonTable).align(Align.top);
        internalTable.add(mixedList).align(Align.top).padRight(10).minWidth(150).minHeight(150);
        internalTable.add(mixtureList).align(Align.top).minWidth(150).minHeight(150);
        internalTable.row();

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
                if (isModal() && stage != null && stage.getRoot().getChildren().size > 0 && stage.getRoot().getChildren().peek() == MixtureDialog.this) {
                    Actor newFocusedActor = event.getRelatedActor();
                    if (newFocusedActor != null && !newFocusedActor.isDescendantOf(MixtureDialog.this) && !(newFocusedActor.equals(previousKeyboardFocus) || newFocusedActor.equals(previousScrollFocus))) {
                        event.cancel();
                    }
                }
            }
        };

    }

    private void animateText(String text, Color color, float sx, float sy, float dx, float dy) {
        Label label = new Label(text, Ultima4.skin);
        label.setPosition(sx, sy);
        label.setColor(color);
        stage.addActor(label);
        label.addAction(sequence(Actions.moveTo(dx, dy, 2f), Actions.fadeOut(1f), Actions.removeActor(label)));
    }

    protected void workspace(Stage stage) {
        if (stage == null) {
            addListener(focusListener);
        } else {
            removeListener(focusListener);
        }
        super.setStage(stage);
    }

    public MixtureDialog show(Action action) {
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

        return this;
    }

    public MixtureDialog show() {
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
