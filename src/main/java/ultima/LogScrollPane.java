package ultima;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;

public class LogScrollPane extends ScrollPane {
	
	private static Table internalTable = new Table();
	private static Label.LabelStyle labelStyle;
	private int width;

	public LogScrollPane(Skin skin, int width) {
		super(internalTable, skin, "gray-background");
		
		this.width = width;
		
		labelStyle = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
		labelStyle.fontColor = Color.WHITE;
		
		clear();
		setScrollingDisabled(true, false);
				
		internalTable.align(Align.topLeft);

	}

	public void add(String text) {
		
		Label label = new Label(text, labelStyle);
		label.setWrap(true);
		label.setAlignment(Align.topLeft, Align.left);
		
		internalTable.add(label).pad(2).width(width - 10);
		internalTable.row();
		scrollTo(0, 0, 0, 0);
		pack();
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