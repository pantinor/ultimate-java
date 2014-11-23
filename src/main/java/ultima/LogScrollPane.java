package ultima;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;

public class LogScrollPane extends ScrollPane {
	
	private static Table internalTable = new Table();
	
	private int width;
	private Skin skin;
	private String styleName;

	public LogScrollPane(Skin skin, int width, String styleName) {
		
		super(internalTable, skin, styleName);
		this.skin = skin;
		this.width = width;
		this.styleName = styleName;
		
		clear();
		setScrollingDisabled(true, false);
				
		internalTable.align(Align.topLeft);
	}

	public void add(String text) {
		
		if (text == null) return;
		
		Label label = new Label(text, skin, styleName);
		label.setWrap(true);
		label.setAlignment(Align.topLeft, Align.left);

		internalTable.add(label).pad(1).width(width - 10);
		internalTable.row();

		pack();
		scrollTo(0, 0, 0, 0);

	}

	public void clear() {
		internalTable.clear();
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