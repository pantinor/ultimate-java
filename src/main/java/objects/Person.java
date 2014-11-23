package objects;

import ultima.Constants;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.Vector3;

public class Person implements Constants {
	
	private int id;
	private int start_x;
	private int start_y;
	private int x;
	private int y;
	private ObjectMovementBehavior movement;
	private Tile tile;
	private int tileMapId;
	private int dialogId;
	private int tileIndex;
	
	private Animation anim;
	private Vector3 currentPos;
	private Conversation conversation;
	private boolean isTalking = false;
	private PersonRole role;
	
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
	public int getTileIndex() {
		return tileIndex;
	}
	public void setTileIndex(int tileIndex) {
		this.tileIndex = tileIndex;
	}

	public Vector3 getCurrentPos() {
		return currentPos;
	}
	public Conversation getConversation() {
		return conversation;
	}
	public void setCurrentPos(Vector3 currentPos) {
		this.currentPos = currentPos;
	}
	public void setConversation(Conversation conversation) {
		this.conversation = conversation;
	}
	public Animation getAnim() {
		return anim;
	}
	public void setAnim(Animation anim) {
		this.anim = anim;
	}
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	public void setX(int x) {
		this.x = x;
	}
	public void setY(int y) {
		this.y = y;
	}
	public boolean isTalking() {
		return isTalking;
	}
	public void setTalking(boolean isTalking) {
		this.isTalking = isTalking;
	}
	public PersonRole getRole() {
		return role;
	}
	public void setRole(PersonRole role) {
		this.role = role;
	}
	
	
}