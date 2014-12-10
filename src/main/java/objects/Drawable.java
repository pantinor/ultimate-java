package objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Drawable extends Actor {
	
	private int cx;
	private int cy;
	private Sprite sprite;
	private String tname;

	public Drawable(int cx, int cy, String name, TextureAtlas atlas) {
		super();
		this.cx = cx;
		this.cy = cy;
		this.tname = name;
		TextureRegion t = atlas.findRegion(name);
		sprite = new Sprite(t);

	}
	public int getCx() {
		return cx;
	}
	public int getCy() {
		return cy;
	}

	public String getTileName() {
		return tname;
	}
		

	@Override
	public void draw(Batch batch, float parentAlpha) {
		
		Color color = getColor();
		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
		
		batch.draw(sprite, getX(), getY());
	}

	
	
	

}
