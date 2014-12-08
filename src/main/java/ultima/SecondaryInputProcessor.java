package ultima;

import objects.BaseMap;
import objects.City;
import objects.Person;
import objects.Tile;
import ultima.Constants.Direction;
import ultima.Constants.TileRule;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class SecondaryInputProcessor extends InputAdapter {
	
	private GameScreen game;
	private Stage stage;
	private int initialKeyCode;
	private BaseMap bm;
	private int currentX;
	private int currentY;
	
	public SecondaryInputProcessor(GameScreen game, Stage stage) {
		this.game = game;
		this.stage = stage;
	}
	
	public void setinitialKeyCode(int k, BaseMap bm, int x, int y) {
		this.initialKeyCode = k;
		this.bm = bm;
		this.currentX = x;
		this.currentY = y;
	}
	
	@Override
	public boolean keyUp (int keycode) {
		Direction dir = Direction.NORTH;
		
		int x=currentX, y=currentY;

		if (keycode == Keys.UP) {
			dir = Direction.NORTH;
			y = y - 1;
		} else if (keycode == Keys.DOWN) {
			dir = Direction.SOUTH;
			y = y + 1;
		} else if (keycode == Keys.LEFT) {
			dir = Direction.WEST;
			x = x - 1;
		} else if (keycode == Keys.RIGHT) {
			dir = Direction.EAST;
			x = x + 1;
		}
		


		ConversationDialog dialog = null;
		
		if (initialKeyCode == Keys.T) {
			
			game.log("Talk > " + dir.toString());

			Tile tile = bm.getTile(x, y);
			if (tile.getRule() == TileRule.signs) {
				//talking to vendor so get the vendor on other side of sign
				switch (dir) {
				case NORTH: y = y - 1; break;
				case SOUTH:	y = y + 1; break;
				case EAST: x = x + 1; break;
				case WEST: x = x - 1; break;
				}
			}
			
			City city = bm.getCity();
			if (city != null) {
				Person p = city.getPersonAt(x, y);
				if (p != null && (p.getConversation() != null || p.getRole() != null)) {
					Gdx.input.setInputProcessor(stage);
					dialog = new ConversationDialog(p, this.game, this.game.skin).show(stage);
				} else {
					game.log("Funny, no response! ");
				}
			}
			
		} else if (initialKeyCode == Keys.O) {
			
			game.log("Open > " + dir.toString());
			if (bm.openDoor(x, y)) {
				game.log("Opened!");
			} else {
				game.log("Can't!");
			}

		} else if (initialKeyCode == Keys.L) {
			
			game.log("Look > " + dir.toString());
			
		} else if (initialKeyCode == Keys.S) {
			
			game.log("Search > " + dir.toString());
			
		}
		
		if(dialog == null) {
			Gdx.input.setInputProcessor(new InputMultiplexer(game, stage));
		}
		
		game.finishTurn(currentX, currentY);

		
		return false;
	}

}
