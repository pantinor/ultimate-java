package util;

import java.util.List;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class LogDisplay {
	
	Texture background;
	List<String> logs = new FixedSizeArrayList<String>(5);
	BitmapFont font;
	
	public LogDisplay(BitmapFont font) {
		
		this.font = font;
		
		Pixmap pixmap = new Pixmap(175,20*5, Format.RGBA8888);
		pixmap.setColor(0.2f,0.2f,0.2f,0.7f);
		pixmap.fillRectangle(0, 0, 175, 20*5);
		background = new Texture(pixmap);
	}
	
	public void add(String s) {
		logs.add(s);
	}
	
	public void render(Batch batch) {
		int y = 18 * 5;

		batch.draw(background, 0, 0);
		
		for (String s : logs) {
			font.draw(batch, s, 5, y);
			y = y - 18;
		}
	}

}