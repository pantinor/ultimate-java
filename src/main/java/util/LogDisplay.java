package util;

import java.util.List;

import objects.Party;
import objects.Party.PartyMember;

import org.apache.commons.collections.iterators.ReverseListIterator;

import ultima.Constants.StatusType;
import ultima.Ultima4;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;

public class LogDisplay {
	
	Texture logbkgrnd, playbkgrnd;
	List<String> logs = new FixedSizeArrayList<String>(5);
	BitmapFont font;
	
	int width = 180;
	int height = 18*5;
	
	int pbh = 170;
	int pbw = 142;
	
	public LogDisplay(BitmapFont font) {
		
		this.font = font;
		
		Pixmap pixmap = getPixmapRoundedRectangle(width, height, 10, new Color(0.16f,0.26f,0.75f,1f));//a blue color
		logbkgrnd = new Texture(pixmap);
		pixmap.dispose();
		
		pixmap = getPixmapRoundedRectangle(pbw, pbh, 10, new Color(0.16f,0.26f,0.75f,1f));//a blue color
		playbkgrnd = new Texture(pixmap);
		pixmap.dispose();
	}
	
	public void append(String s) {
		if (logs.size() == 0) logs.add("");
		String l = logs.get(logs.size()-1);
		l = l + s;
		logs.remove(logs.size()-1);
		logs.add(l);
	}
	
	public void logDeleteLastChar() {
		if (logs.size() == 0) return;
		String l = logs.get(logs.size()-1);
		l = l.substring(0, l.length() - 1);
		logs.remove(logs.size()-1);
		logs.add(l);
	}
	
	public void add(String s) {
		logs.add(s);
	}
	
	public void render(Batch batch, Party party) {
		
		batch.draw(playbkgrnd, Ultima4.SCREEN_WIDTH - pbw, Ultima4.SCREEN_HEIGHT - pbh);

		
		int food = party.getSaveGame().food / 100;
		font.setColor(food < 5 ? Color.RED: Color.WHITE);
		font.draw(batch, "Food: " + food, Ultima4.SCREEN_WIDTH - 135, Ultima4.SCREEN_HEIGHT - 4);
		font.setColor(Color.WHITE);
		font.draw(batch, "Gold: " + party.getSaveGame().gold, Ultima4.SCREEN_WIDTH - 65, Ultima4.SCREEN_HEIGHT - 4);

		float y = Ultima4.SCREEN_HEIGHT - 23;
		for (int i = 0; i < party.getMembers().size(); i++) {
			PartyMember pm = party.getMember(i);
			
			String s = (i + 1) + " - " + pm.getPlayer().name;
			String d = pm.getPlayer().hp + "" + pm.getPlayer().status.getValue();
			
			
			font.setColor(i == party.getActivePlayer()? new Color(.35f, .93f, 0.91f, 1) : Color.WHITE);
			if (pm.getPlayer().status == StatusType.POISONED) font.setColor(Color.GREEN);
			if (pm.getPlayer().status == StatusType.SLEEPING) font.setColor(Color.YELLOW);
			if (pm.getPlayer().status == StatusType.DEAD) font.setColor(Color.GRAY);
			
			font.draw(batch, s, Ultima4.SCREEN_WIDTH - 135, y);
			font.draw(batch, d, Ultima4.SCREEN_WIDTH - 45, y);
			
			y = y - 18;

		}
		
		batch.draw(logbkgrnd, 0, 0);
		
		font.setColor(Color.WHITE);
		int h = 18;
		y = 0;

		ReverseListIterator iter = new ReverseListIterator(logs);
		while (iter.hasNext()) {
			String next = (String)iter.next();
			TextBounds bounds = font.getWrappedBounds(next, width - 8);
			
			y = y + bounds.height + 4;
			h += bounds.height + 4;
			if (h > height + 18) {
				break;
			}
			
			font.drawWrapped(batch, next, 5, y, width - 8);
		}
	}
	
	public Pixmap getPixmapRoundedRectangle(int width, int height, int radius, Color color) {

		Pixmap pixmap = new Pixmap(width, height, Format.RGBA8888);
		pixmap.setColor(color);

		pixmap.fillRectangle(0, radius, pixmap.getWidth(), pixmap.getHeight() - 2 * radius);
		pixmap.fillRectangle(radius, 0, pixmap.getWidth() - 2 * radius, pixmap.getHeight());

		// Bottom-left circle
		pixmap.fillCircle(radius, radius, radius);

		// Top-left circle
		pixmap.fillCircle(radius, pixmap.getHeight() - radius, radius);

		// Bottom-right circle
		pixmap.fillCircle(pixmap.getWidth() - radius, radius, radius);

		// Top-right circle
		pixmap.fillCircle(pixmap.getWidth() - radius, pixmap.getHeight() - radius, radius);

		return pixmap;
	}

}
