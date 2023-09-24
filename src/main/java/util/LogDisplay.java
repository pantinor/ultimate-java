package util;

import java.util.List;

import objects.Party;
import objects.Party.PartyMember;

import org.apache.commons.collections.iterators.ReverseListIterator;

import ultima.Constants.StatusType;
import ultima.Constants.TransportContext;
import ultima.Ultima4;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.Align;

public class LogDisplay {

    final List<String> logs = new FixedSizeArrayList<>(20);
    final BitmapFont font;

    static final int LOG_AREA_WIDTH = 256;
    static final int LOG_AREA_TOP = 384;

    static final int LOG_X = 736;

    public LogDisplay(BitmapFont font) {
        this.font = font;
    }

    public void append(String s) {
        synchronized (logs) {
            if (logs.isEmpty()) {
                logs.add("");
            }
            String l = logs.get(logs.size() - 1);
            l = l + s;
            logs.remove(logs.size() - 1);
            logs.add(l);
        }
    }

    public void logDeleteLastChar() {
        synchronized (logs) {
            if (logs.isEmpty()) {
                return;
            }
            String l = logs.get(logs.size() - 1);
            l = l.substring(0, l.length() - 1);
            logs.remove(logs.size() - 1);
            logs.add(l);
        }
    }

    public void add(String s) {
        synchronized (logs) {
            logs.add(s);
        }
    }

    public void render(Batch batch, Party party) {

        int food = party.getSaveGame().food / 100;
        font.setColor(food < 5 ? Color.RED : Color.WHITE);
        font.draw(batch, "Food  " + food, LOG_X + 8, 438);
        font.setColor(Color.WHITE);
        if (party.getContext().getTransportContext() == TransportContext.SHIP) {
            font.draw(batch, "Hull  " + party.getSaveGame().shiphull, LOG_X + 8 + 120, 438);
        } else {
            font.draw(batch, "Gold  " + party.getSaveGame().gold, LOG_X + 8 + 140, 438);
        }

        float y = Ultima4.SCREEN_HEIGHT - 48;
        for (int i = 0; i < party.getMembers().size(); i++) {
            PartyMember pm = party.getMember(i);

            String s = (i + 1) + " - " + pm.getPlayer().name;

            font.setColor(i == party.getActivePlayer() ? new Color(.35f, .93f, 0.91f, 1) : Color.WHITE);
            if (pm.getPlayer().status == StatusType.POISONED) {
                font.setColor(Color.GREEN);
            }
            if (pm.getPlayer().status == StatusType.SLEEPING) {
                font.setColor(Color.YELLOW);
            }
            if (pm.getPlayer().status == StatusType.DEAD) {
                font.setColor(Color.GRAY);
            }

            int x = LOG_X + 8;
            font.draw(batch, s, x, y);
            font.draw(batch, "HP " + pm.getPlayer().hp, x += 100, y);
            font.draw(batch, "MP " + pm.getPlayer().mp, x += 60, y);
            font.draw(batch, "" + pm.getPlayer().status.getValue(), x += 60, y);

            y = y - 24;

        }

        font.setColor(Color.WHITE);
        y = 32;

        synchronized (logs) {
            ReverseListIterator iter = new ReverseListIterator(logs);
            while (iter.hasNext()) {
                String next = (String) iter.next();
                GlyphLayout layout = new GlyphLayout(font, next, Color.WHITE, LOG_AREA_WIDTH - 8, Align.left, true);
                y += layout.height + 10;
                if (y > LOG_AREA_TOP) {
                    break;
                }
                font.draw(batch, layout, LOG_X + 8, y);
            }
        }
    }
}
