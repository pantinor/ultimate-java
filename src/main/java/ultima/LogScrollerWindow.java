package ultima;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class LogScrollerWindow extends Window {
	
	private Ultima4 mainGame;
	
	public static int width = 300;
	public static int height = 200;
	
	private LogScrollPane scrollPane;
	
	private boolean collapsed;
	private float collapseHeight = 20f;
	private float expandHeight;
	
	public LogScrollerWindow(final Stage stage, Ultima4 mainGame, Skin skin) {
		super("", skin);

		this.mainGame = mainGame;
		
		addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				if (getTapCount() == 2 && getHeight() - y <= getPadTop() && y < getHeight() && x > 0 && x < getWidth())
					toggleCollapsed();
			}
		});
		
		scrollPane = new LogScrollPane(skin, width, "logs");
						
		defaults().pad(2);
		
		Table bottom = new Table();
		bottom.add(scrollPane).maxWidth(width).width(width);
		add(bottom);

		pack();

		stage.addActor(this);
		
		setPosition(700, 0);

	}

	
	public void expand () {
		if (!collapsed) return;
		setHeight(expandHeight);
		setY(getY() - expandHeight + collapseHeight);
		collapsed = false;
	}

	public void collapse () {
		if (collapsed) return;
		expandHeight = getHeight();
		setHeight(collapseHeight);
		setY(getY() + expandHeight - collapseHeight);
		collapsed = true;
		if (getStage() != null) getStage().setScrollFocus(null);
	}

	public void toggleCollapsed () {
		if (collapsed)
			expand();
		else
			collapse();
	}

	public boolean isCollapsed () {
		return collapsed;
	}
	
	public void listActionInScroller(String text) {
		scrollPane.add(text);
	}
	


}
