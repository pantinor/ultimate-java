package ultima;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class LogScrollPane extends ScrollPane {
	
	private static Table log = new Table();
	private static Label.LabelStyle labelStyle;

	public LogScrollPane(Skin skin) {
		super(log, skin, "gray-background");
		
		labelStyle = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
		labelStyle.fontColor = Color.WHITE;
		
		clear();
		setScrollingDisabled(true, false);

	}
	
//	// get the default style from the skin
//	ScrollPane.ScrollPaneStyle transparentStyle = new ScrollPane.ScrollPaneStyle(skin.get(ScrollPane.ScrollPaneStyle.class));
	
//	// set the background to be a new drawable, in this case a textureregion that is your transparent background.
//	transparentStyle.background = new TextureRegionDrawable(transparentTextureRegion);
	
//	// add the new style into the skin
//	skin.add("transparentScrollPane", transparentStyle);

	public void add(String text) {
		Label label = new Label(text, labelStyle);
		log.add(label);
		log.row();
		scrollTo(0, 0, 0, 0);
		pack();
	}

	public void clear() {
		log.clear();
		pack();
	}
	
	
	@Override
	public float getPrefWidth () {
		return this.getWidth();
	}
	@Override
	public float getPrefHeight () {
		return this.getHeight();
	}
	@Override
	public float getMaxWidth () {
		return this.getWidth();
	}
	@Override
	public float getMaxHeight () {
		return this.getHeight();
	}
}