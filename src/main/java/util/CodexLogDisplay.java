package util;

import com.badlogic.gdx.graphics.Color;
import java.util.List;

import org.apache.commons.collections.iterators.ReverseListIterator;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.Align;

public class CodexLogDisplay {

    Texture logbkgrnd;
    List<String> logs = new FixedSizeArrayList<>(5);
    BitmapFont font;

    int width = 400;
    int height = 150;

    public CodexLogDisplay(BitmapFont font) {

        this.font = font;

        Pixmap pixmap = new Pixmap(width, height, Format.RGBA8888);
        pixmap.setColor(0f, 0f, 0f, 0.65f);
        pixmap.fillRectangle(0, 0, width, height);
        logbkgrnd = new Texture(pixmap);
        pixmap.dispose();

    }

    public void append(String s) {
        if (logs.isEmpty()) {
            logs.add("");
        }
        String l = logs.get(logs.size() - 1);
        l = l + s;
        logs.remove(logs.size() - 1);
        logs.add(l);
    }

    public void logDeleteLastChar() {
        if (logs.isEmpty()) {
            return;
        }
        String l = logs.get(logs.size() - 1);
        l = l.substring(0, l.length() - 1);
        logs.remove(logs.size() - 1);
        logs.add(l);
    }

    public void add(String s) {
        logs.add(s);
    }

    public void clear() {
        this.logs.clear();
    }

    public void render(Batch batch) {

        batch.draw(logbkgrnd, 200, 500);

        int h = 20;
        float y = 500 + 10;
        
        if (!logs.isEmpty()) {

            ReverseListIterator iter = new ReverseListIterator(logs);
            while (iter.hasNext()) {
                String next = (String) iter.next();
                GlyphLayout layout = new GlyphLayout(font, next, Color.WHITE, width - 20, Align.left, true);
                y = y + layout.height + 6;
                h += layout.height + 6;
                if (h > height + 20) {
                    break;
                }
                font.draw(batch, layout, 220, y);
            }
        }

    }
}
