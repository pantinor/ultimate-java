package test;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import objects.Person;
import test.SpriteAtlasTool.MyListItem;
import vendor.BaseVendor;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;

public class PopupDialog extends Window {

    private Skin skin;
    boolean cancelHide;
    Actor previousKeyboardFocus, previousScrollFocus;
    FocusListener focusListener;
    Person person;
    BaseVendor vendor;

    public static int width = 300;
    public static int height = 400;
    static BitmapFont font = new BitmapFont();

    Table internalTable;
    java.util.List<MyListItem> gridNames;
    TextButton okBtn;
    TextButton deleteBtn;
    ScrollPane scrollPane;
    List<MyListItem> list;
    MyListItem selectedItem;

    public PopupDialog(Skin skin, java.util.List<MyListItem> gridNames) {
        super("", skin.get("dialog", WindowStyle.class));
        setSkin(skin);
        this.skin = skin;
        this.gridNames = gridNames;
        initialize();
    }

    private void initialize() {
        setModal(true);

        internalTable = new Table(skin);
        internalTable.setFillParent(true);
        internalTable.defaults().pad(2);

        defaults().space(10).pad(5);

        add(internalTable).expand().fill();

        list = new List<>(skin);
        list.setItems(gridNames.toArray(new MyListItem[gridNames.size()]));
        list.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                selectedItem = list.getSelected();
            }
        });

        scrollPane = new ScrollPane(list, skin);

        internalTable.row();
        internalTable.add(new Label("", skin)).expandX().left().width(150).height(10);

        okBtn = new TextButton("OK", skin, "default");
        okBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hide();
            }
        });
        internalTable.row();
        internalTable.add(okBtn).expandX().left().width(150);

        deleteBtn = new TextButton("DELETE", skin, "default");
        deleteBtn.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                gridNames.remove(selectedItem);
            }
        });

        internalTable.row();
        internalTable.add(deleteBtn).expandX().left().width(150);

        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setHeight(height);
        scrollPane.setWidth(width);

        internalTable.row();
        internalTable.add(scrollPane).expandX().left().width(150).maxHeight(height);
        internalTable.setPosition(height - 150, 0);

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
                if (isModal() && stage != null && stage.getRoot().getChildren().size > 0 && stage.getRoot().getChildren().peek() == PopupDialog.this) {
                    Actor newFocusedActor = event.getRelatedActor();
                    if (newFocusedActor != null && !newFocusedActor.isDescendantOf(PopupDialog.this) && !(newFocusedActor.equals(previousKeyboardFocus) || newFocusedActor.equals(previousScrollFocus))) {
                        event.cancel();
                    }
                }
            }
        };

    }

    protected void workspace(Stage stage) {
        if (stage == null) {
            addListener(focusListener);
        } else {
            removeListener(focusListener);
        }
        super.setStage(stage);
    }

    public PopupDialog show(Stage stage, Action action) {
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
        stage.setKeyboardFocus(okBtn);
        stage.setScrollFocus(this);

        if (action != null) {
            addAction(action);
        }

        return this;
    }

    public PopupDialog show(Stage stage) {
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
