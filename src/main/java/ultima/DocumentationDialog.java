package ultima;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import util.LogScrollPane;

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
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;

public class DocumentationDialog extends Window implements Constants {
	
	private Skin skin;
	boolean cancelHide;
	Actor previousKeyboardFocus, previousScrollFocus;
	FocusListener focusListener;
	
	BaseScreen screen;
	Stage stage;
	LogScrollPane scrollPane;

	Table internalTable;
	
	public static int width = 600;
	public static int height = 500;
	static BitmapFont font = new BitmapFont(Gdx.files.internal("assets/fonts/corsiva-20.fnt"), false);
	
	public DocumentationDialog(BaseScreen screen, Stage stage, Skin skin) {
		super("", skin.get("dialog", WindowStyle.class));
		setSkin(skin);
		this.skin = skin;
		this.stage = stage;
		this.screen = screen;
		initialize();
	}
	
	private void initialize() {
		setModal(true);
		
		internalTable = new Table(skin);
		internalTable.defaults().pad(5);
		
		TextButton tb = new TextButton("X", skin);
		getButtonTable().add(tb).height(getPadTop());
		tb.addListener(new ClickListener() {
			public void clicked (InputEvent event, float x, float y) {
				if (!cancelHide) {
					hide();
				}
				cancelHide = false;
			}
		});
		
		defaults().space(10).pad(2);
		add(internalTable).expand().fill();
		row();
		
		scrollPane = new LogScrollPane(skin, width, "logs");
		scrollPane.setHeight(height);
		scrollPane.setSmoothScrolling(true);
		scrollPane.setFlickScroll(false);

		try {
			List<String> commands = FileUtils.readLines(Gdx.files.internal("assets/data/commands.txt").file());
			for (String s : commands) {
				String t = s.trim();
				scrollPane.add(s.length() < 1?" ":t, false);
			}
			scrollPane.add(" ", false);
			List<String> book = FileUtils.readLines(Gdx.files.internal("assets/data/book.txt").file());
			for (String s : book) {
				String t = s.trim();
				scrollPane.add(s.length() < 1?" ":t, false);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		internalTable.add(scrollPane).maxWidth(width).width(width);
		internalTable.row();
		


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
				if (isModal() && stage != null && stage.getRoot().getChildren().size > 0 && stage.getRoot().getChildren().peek() == DocumentationDialog.this) {
					Actor newFocusedActor = event.getRelatedActor();
					if (newFocusedActor != null && !newFocusedActor.isDescendantOf(DocumentationDialog.this) && !(newFocusedActor.equals(previousKeyboardFocus) || newFocusedActor.equals(previousScrollFocus)))
						event.cancel();
				}
			}
		};
		
		


	}
	
	protected void workspace(Stage stage) {
		if (stage == null)
			addListener(focusListener);
		else
			removeListener(focusListener);
		super.setStage(stage);
	}
	
	public DocumentationDialog show(Action action) {
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
		stage.setScrollFocus(this);
		
		if (action != null)
			addAction(action);

		return this;
	}
	
	public DocumentationDialog show() {
		show(sequence(Actions.alpha(0), Actions.fadeIn(0.4f, Interpolation.fade)));
		setPosition(Math.round((stage.getWidth() - getWidth()) / 2), Math.round((stage.getHeight() - getHeight()) / 2));
		return this;
	}

	public void hide(Action action) {
		
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
		
		Gdx.input.setInputProcessor(new InputMultiplexer(screen, stage));
		
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
