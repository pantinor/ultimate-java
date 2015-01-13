package objects;

import java.util.Random;

import util.Utils;

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
	private int mapId;
	
	//drawable is used for ships
	private int shipHull = 50;

	public Drawable(int id, int cx, int cy, Tile tile, TextureAtlas atlas) {
		super();
		this.cx = cx;
		this.cy = cy;
		this.tile = tile;
		this.mapId = id;
		this.texture = atlas.findRegion(tile.getName());
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
	
	public int getShipHull() {
		return shipHull;
	}
	public void setShipHull(int shipHull) {
		this.shipHull = shipHull;
	}
	
    public int damageShip(int minDamage, int maxDamage) {
		int damage = ((minDamage >= 0) && (minDamage < maxDamage)) ? new Random().nextInt((maxDamage + 1) - minDamage) + minDamage : maxDamage;
		int newStr = Utils.adjustValue(-damage, 0, 50, 0);
		this.shipHull = newStr;
		return this.shipHull;
    }
	public int getMapId() {
		return mapId;
	}
	
}
