package util;

import java.util.Iterator;
import java.util.List;

import objects.Party;
import objects.Party.PartyMember;
import ultima.Ultima4;
import ultima.Constants.StatusType;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;

public class LogDisplay {
	
	Texture background;
	List<String> logs = new FixedSizeArrayList<String>(5);
	BitmapFont font;
	
	int width = 180;
	int height = 20*5;
	
	public LogDisplay(BitmapFont font) {
		
		this.font = font;
		
		Pixmap pixmap = new Pixmap(width, height, Format.RGBA8888);
		pixmap.setColor(0.2f,0.2f,0.2f,0.7f);
		pixmap.fillRectangle(0, 0, width, height);
		background = new Texture(pixmap);
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
		
		y = 18 * 5;

		batch.draw(background, 0, 0);
		
		font.setColor(Color.WHITE);

		Iterator<String> iter = logs.iterator();
		while (iter.hasNext()) {
			String next = iter.next();
			
			TextBounds bounds = font.getWrappedBounds(next, width - 8);
			font.drawWrapped(batch, next, 5, y, width - 8);
			y = y - bounds.height - 3;
			
			//font.draw(batch, iter.next(), 5, y);
			//y = y - 18;
		}
	}

}
