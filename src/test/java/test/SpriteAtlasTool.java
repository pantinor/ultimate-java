package test;



import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import objects.Tile;
import objects.TileSet;
import util.Utils;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class SpriteAtlasTool extends InputAdapter implements ApplicationListener {
			
	Batch mapBatch, batch2;
	
	static int screenWidth = 1200;
	static int screenHeight = 800;
	
	int tilePixelWidth = 16;
	int tilePixelHeight = 16;

	boolean initMapPosition = true;
	
	MyVector currentMapCoords;
	MyVector selectedMapCoords;

	BitmapFont font;
	Texture t;
	Sprite sprBg;
	
	Stage stage;
	List<MyListItem> list;
	ScrollPane scrollPane;
	TextButton makeButton;
	Skin skin;
	MyListItem[] tileNames;
	MyListItem selectedTileName;
	
	MyListItem[][] gridNames;

	
	public void create () {
				
		t = new Texture(Gdx.files.absolute("C:\\Users\\Paul\\Desktop\\U4TilesV.png"));
		sprBg = new Sprite(t, 0, 0, t.getWidth(), t.getHeight());
		
		gridNames = new MyListItem[Math.round(t.getWidth()/tilePixelWidth)][Math.round(t.getHeight()/tilePixelHeight)];

	
		font = new BitmapFont();
		font.setColor(Color.WHITE);
		
		batch2 = new SpriteBatch();
		skin = new Skin(Gdx.files.internal("assets/skin/uiskin.json"));
		
		try {
			TileSet ts = (TileSet) Utils.loadXml("tileset-base.xml", TileSet.class);	
			tileNames = new MyListItem[ts.getTiles().size()];
			int x=0;
			for (Tile t : ts.getTiles()) {
				tileNames[x] = new MyListItem(t.getName(),0,0);
				x++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		


		final List<MyListItem> list = new List<MyListItem>(skin);
		list.setItems(tileNames);
		list.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				selectedTileName = list.getSelected();
			}
		});
		
		
		
		scrollPane = new ScrollPane(list, skin);
		scrollPane.setScrollingDisabled(true, false);
		
		makeButton = new TextButton("Make Atlas", skin, "default");
		makeButton.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				makeAtlas();
			}
		});
		
		Table table = new Table();
		table.defaults().pad(2);
		table.add(makeButton).expandX().left().width(150);
		table.row();
		table.add(scrollPane).expandX().left().width(150).maxHeight(screenHeight);
		table.setPosition(screenWidth-150, 0);	
		table.setFillParent(true);

		stage = new Stage();
		stage.addActor(table);

		Gdx.input.setInputProcessor(new InputMultiplexer(stage, this));

	}

	public void render () {

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
						
		batch2.begin();
		
		sprBg.draw(batch2);
		
		font.draw(batch2, "current mouse coords: " + currentMapCoords, 10, screenHeight - 60);
		font.draw(batch2, "selectedMapCoords: " + selectedMapCoords, 10, screenHeight - 40);
		font.draw(batch2, "selectedTileName: " + selectedTileName, 10, screenHeight - 20);

		batch2.end();
		
		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
		stage.draw();
	}
	
	@Override
	public boolean mouseMoved (int screenX, int screenY) {
		currentMapCoords = new MyVector(Math.round(screenX/tilePixelHeight), Math.abs(Math.round((screenHeight - screenY)/tilePixelHeight - (t.getHeight()/tilePixelHeight))+1));		 
		return false;
	}
	
	@Override
	public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		int y = Math.round((screenHeight - screenY)/tilePixelHeight - (t.getHeight()/tilePixelHeight))+1;
		int x = Math.round(screenX/tilePixelHeight);
		
		if (y <= 0 && x < tilePixelWidth) {
			selectedMapCoords = new MyVector(x, y*-1);	
			
			if (selectedTileName != null) {
				gridNames[x][y*-1] = new MyListItem(selectedTileName.name,x ,y*-1 );
			}
			
		}
				
		return false;
	}
	
	
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Sprite Atlas Tool";
		cfg.width = screenWidth;
		cfg.height = screenHeight;
		new LwjglApplication(new SpriteAtlasTool(), cfg);

	}
	
	
	public class MyVector {
		private int x;
		private int y;
		private MyVector(int x, int y) {
			this.x = x;
			this.y = y;
		}
		@Override
		public String toString() {
			return String.format("%s, %s", x, y);
		}
		
	}
	
	public class MyListItem {
		private String name;
		private int x;
		private int y;
		private MyListItem(String name, int x, int y) {
			this.name = name;
			this.x = x;
			this.y = y;
		}
		@Override
		public String toString() {
			return String.format("%s %s, %s", name, x, y);
		}
		
	}


	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}
	
	
	public void makeAtlas() {
				
		MaxRectsPacker mrp = new MaxRectsPacker();
		ArrayList<MaxRectsPacker.Rect> packedRects = new ArrayList<MaxRectsPacker.Rect>();
		int w = tilePixelWidth;
		for (int y=0;y<gridNames[0].length;y++) {
			for (int x=0;x<gridNames.length;x++) {
				MaxRectsPacker.Rect rect = new MaxRectsPacker.Rect(x*w,y*w,w,w);
				rect.name = (gridNames[x][y] != null?gridNames[x][y].name:"col-"+x+"-row-"+y );
				rect.index = 0;
				packedRects.add(rect);
			}
		}
		
		System.out.println("Writing: number of sprites: " +packedRects.size());

		try {
			mrp.writePackFileWithRects(new File("."), "sprites-atlas.txt",packedRects, "sprites.png");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	    System.out.println("done");
	
	}

	
	
}
