package objects;

import ultima.Constants;

public class Person implements Constants {
	private int id;
	private int start_x;
	private int start_y;
	private ObjectMovementBehavior movement;
	private Tile tile;
	private int tileMapId;
	private int dialogId;
	
	public int getId() {
		return id;
	}
	public int getStart_x() {
		return start_x;
	}
	public int getStart_y() {
		return start_y;
	}
	public ObjectMovementBehavior getMovement() {
		return movement;
	}
	public void setId(int id) {
		this.id = id;
	}
	public void setStart_x(int start_x) {
		this.start_x = start_x;
	}
	public void setStart_y(int start_y) {
		this.start_y = start_y;
	}
	public void setMovement(ObjectMovementBehavior movement) {
		this.movement = movement;
	}
	public Tile getTile() {
		return tile;
	}
	public void setTile(Tile tile) {
		this.tile = tile;
	}
	public int getTileMapId() {
		return tileMapId;
	}
	public void setTileMapId(int tileMapId) {
		this.tileMapId = tileMapId;
	}
	public int getDialogId() {
		return dialogId;
	}
	public void setDialogId(int dialogId) {
		this.dialogId = dialogId;
	}
	@Override
	public String toString() {
		
		String template = "<object name=\"%s\" type=\"person\" x=\"%s\" y=\"%s\" width=\"16\" height=\"16\">\n"+
							"<properties>\n"+
							"<property name=\"tileType\" value=\"%s\"/>\n"+
							"<property name=\"movement\" value=\"%s\"/>\n"+
							"<property name=\"startX\" value=\"%s\"/>\n"+
							"<property name=\"startY\" value=\"%s\"/>\n"+
							"<property name=\"dialogId\" value=\"%s\"/>\n"+
							"</properties>\n"+
							"</object>\n";
		
		return String.format(template, 
				id, start_x*16, start_y*16, tile.getName(), movement, start_x, start_y, dialogId);
	}
	
	
}