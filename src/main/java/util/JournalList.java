package util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.ArraySelection;
import com.badlogic.gdx.scenes.scene2d.utils.Cullable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import objects.JournalEntries;
import objects.JournalEntry;

public class JournalList extends Widget implements Cullable {

    private ListStyle style;
    private final Array<JournalEntry> items;
    private final int[] filteredIndex;
    final ArraySelection<JournalEntry> selection;
    private Rectangle cullingArea;
    private float prefWidth, prefHeight;
    private float itemHeight;
    private float textOffsetX, textOffsetY;
    private final BitmapFont font;
    private final TextField filterField;
    
    public JournalList(Skin skin, TextField filterField, Array<JournalEntry> items) {

        this.font = skin.get("default-font", BitmapFont.class);
        this.filterField = filterField;
        
        this.items = items;
        this.filteredIndex = new int[items.size];
        this.selection = new ArraySelection(this.items);

        this.items.sort(JournalEntries.entryCompare);

        selection.validate();
        selection.setActor(this);
        selection.setRequired(true);
        
        for (int i = 0; i < filteredIndex.length; i++) {
            filteredIndex[i] = i;
        }

        setStyle(skin.get("default", ListStyle.class));
        setSize(getPrefWidth(), getPrefHeight());

        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (pointer == 0 && button != 0) {
                    return false;
                }
                if (selection.isDisabled()) {
                    return false;
                }
                JournalList.this.touchDown(x, y);
                return true;
            }
        });

    }

    void touchDown(float x, float y) {
        if (items.size == 0) {
            return;
        }
        float height = getHeight();

        int index = (int) ((height - y) / itemHeight);
        index = Math.max(0, index);
        index = Math.min(items.size - 1, index);
        
        JournalEntry je = null;
        for (int i = 0; i < filteredIndex.length; i++) {
            if (filteredIndex[i] == index) {
                je = items.get(i);
                break;
            }
        }

        if (je == null) {
            return;
        }
        
        selection.choose(je);
    }

    public void setStyle(ListStyle style) {
        if (style == null) {
            throw new IllegalArgumentException("style cannot be null.");
        }
        this.style = style;
        invalidateHierarchy();
    }

    public ListStyle getStyle() {
        return style;
    }

    @Override
    public void layout() {

        final Drawable selectedDrawable = style.selection;

        itemHeight = font.getCapHeight() - font.getDescent() * 2;
        itemHeight += selectedDrawable.getTopHeight() + selectedDrawable.getBottomHeight();

        textOffsetX = selectedDrawable.getLeftWidth();
        textOffsetY = selectedDrawable.getTopHeight() - font.getDescent();

        prefWidth = Gdx.graphics.getWidth() - 32;
        prefHeight = items.size * itemHeight;

    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        validate();

        Drawable selectedDrawable = style.selection;
        Color fontColorSelected = style.fontColorSelected;
        Color fontColorUnselected = style.fontColorUnselected;

        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);

        float x = getX(), y = getY(), width = getWidth(), height = getHeight();
        float itemY = height;
        
        int count = 0;
        for (int i = 0; i < items.size; i++) {
            JournalEntry item = items.get(i);
            String f = this.filterField.getText().toLowerCase().trim();
            if (f != null && f.length() > 0) {
                if (item.getLocation().toLowerCase().contains(f)
                        || item.getName().toLowerCase().contains(f)
                        || item.getText().toLowerCase().contains(f)) {
                    filteredIndex[i] = count;
                    count ++;
                } else {
                    filteredIndex[i] = -1;
                }
            } else {
                    filteredIndex[i] = i;
            }
        }

        font.setColor(fontColorUnselected.r, fontColorUnselected.g, fontColorUnselected.b, fontColorUnselected.a * parentAlpha);
        for (int i = 0; i < items.size; i++) {
            if (filteredIndex[i] == -1) {
                continue;
            }
            if (cullingArea == null || (itemY - itemHeight <= cullingArea.y + cullingArea.height && itemY >= cullingArea.y)) {
                JournalEntry item = items.get(i);

                boolean selected = selection.contains(item);
                if (selected) {
                    selectedDrawable.draw(batch, x, y + itemY - itemHeight, width, itemHeight);
                    font.setColor(fontColorSelected.r, fontColorSelected.g, fontColorSelected.b, fontColorSelected.a * parentAlpha);
                }

                font.draw(batch, item.getLocation(), x + textOffsetX, y + itemY - textOffsetY);
                
                font.setColor(Color.YELLOW);
                font.draw(batch, item.getName(), x + 100 + textOffsetX, y + itemY - textOffsetY);
                font.setColor(Color.WHITE);

                font.draw(batch, item.getText(), x + 200 + textOffsetX, y + itemY - textOffsetY);

                if (selected) {
                    font.setColor(fontColorUnselected.r, fontColorUnselected.g, fontColorUnselected.b, fontColorUnselected.a * parentAlpha);
                }
                
            } else if (itemY < cullingArea.y) {
                break;
            }
            itemY -= itemHeight;
        }
    }

    public ArraySelection<JournalEntry> getSelection() {
        return selection;
    }

    public JournalEntry getSelected() {
        return selection.first();
    }

    public void setSelected(JournalEntry item) {
        if (items.contains(item, false)) {
            selection.set(item);
        } else if (selection.getRequired() && items.size > 0) {
            selection.set(items.first());
        } else {
            selection.clear();
        }
    }

    public int getSelectedIndex() {
        ObjectSet<JournalEntry> selected = selection.items();
        return selected.size == 0 ? -1 : items.indexOf(selected.first(), false);
    }

    public void setSelectedIndex(int index) {
        if (index < -1 || index >= items.size) {
            throw new IllegalArgumentException("index must be >= -1 and < " + items.size + ": " + index);
        }
        if (index == -1) {
            selection.clear();
        } else {
            selection.set(items.get(index));
        }
    }

    public Array<JournalEntry> getItems() {
        return items;
    }

    public float getItemHeight() {
        return itemHeight;
    }

    @Override
    public float getPrefWidth() {
        validate();
        return prefWidth;
    }

    @Override
    public float getPrefHeight() {
        validate();
        return prefHeight;
    }

    @Override
    public void setCullingArea(Rectangle cullingArea) {
        this.cullingArea = cullingArea;
    }

}
