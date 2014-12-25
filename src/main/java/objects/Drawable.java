package objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Drawable extends Actor {
	
	private int cx;
	private int cy;
	private TextureRegion texture;
	private Tile tile;

	public Drawable(int cx, int cy, Tile tile, TextureAtlas atlas) {
		super();
		this.cx = cx;
		this.cy = cy;
		this.tile = tile;
		texture = atlas.findRegion(tile.getName());

	}
	public int getCx() {
		return cx;
	}
	public int getCy() {
		return cy;
	}

	public Tile getTile() {
		return tile;
	}
		
	@Override
	public void draw(Batch batch, float parentAlpha) {
		
		Color color = getColor();
		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
		
		batch.draw(texture, getX(), getY(), 32, 32);
	}
	
}
