package util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

public class LogScrollPane extends ScrollPane {

    private static final Table internalTable = new Table();

    private final int width;
    private final Skin skin;
    private final LabelStyle ls;

    public LogScrollPane(Skin skin, int width) {

        super(internalTable, skin);
        this.skin = skin;
        this.width = width;
        
        this.ls = new LabelStyle(this.skin.get("default-font", BitmapFont.class), Color.WHITE);

        clear();
        setScrollingDisabled(true, false);

        internalTable.align(Align.topLeft);
    }

    public void add(String text) {
        add(text, true);
    }

    public void add(String text, boolean scrollBottom) {

        if (text == null) {
            return;
        }

        Label label = new Label(text, ls);
        label.setWrap(true);
        label.setAlignment(Align.topLeft, Align.left);

        internalTable.add(label).pad(1).width(width - 10);
        internalTable.row();

        pack();
        if (scrollBottom) {
            scrollTo(0, 0, 0, 0);
        }

    }

    @Override
    public void clear() {
        internalTable.clear();
        pack();
    }

    @Override
    public float getPrefWidth() {
        return this.getWidth();
    }

    @Override
    public float getPrefHeight() {
        return this.getHeight();
    }

    @Override
    public float getMaxWidth() {
        return this.getWidth();
    }

    @Override
    public float getMaxHeight() {
        return this.getHeight();
    }
}
