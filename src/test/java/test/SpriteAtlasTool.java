package test;



import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import objects.Tile;
import objects.TileSet;

import org.apache.commons.io.FileUtils;

import test.AtlasWriter.Rect;
import util.Utils;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
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
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings;

public class SpriteAtlasTool extends InputAdapter implements ApplicationListener {
			
	Batch batch;
	
	static int screenWidth = 1600;
	static int screenHeight = 1024;
	
	int dim = 32;
	int canvasGridWidth;
	int canvasGridHeight;

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
		
	MyListItem[] gridNames;
	Texture box;
	
	public void create () {
		
		Pixmap pixmap = new Pixmap(dim,dim, Format.RGBA8888);
		pixmap.setColor(new Color(1, 1, 0, .8f));
		int w = 1;
		pixmap.fillRectangle(0, 0, w, dim);
		pixmap.fillRectangle(dim - w, 0, w, dim);
		pixmap.fillRectangle(w, 0, dim-2*w, w);
		pixmap.fillRectangle(w, dim - w, dim-2*w, w);
		box = new Texture(pixmap);
		
		t = new Texture(Gdx.files.internal("assets/tilemaps/monsters.png"));
		canvasGridWidth = t.getWidth() / dim;
		canvasGridHeight = t.getHeight() / dim;

		sprBg = new Sprite(t, 0, 0, t.getWidth(), t.getHeight());
		
		gridNames = new MyListItem[canvasGridWidth * canvasGridHeight];

	
		font = new BitmapFont();
		font.setColor(Color.WHITE);
		
		batch = new SpriteBatch();
		skin = new Skin(Gdx.files.internal("assets/skin/uiskin.json"));
		
		readAtlas();
		
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
						
		batch.begin();
		
		sprBg.draw(batch);
		
		font.draw(batch, "current mouse coords: " + currentMapCoords, 10, screenHeight - 60);
		font.draw(batch, "selectedMapCoords: " + selectedMapCoords, 10, screenHeight - 40);
		font.draw(batch, "selectedTileName: " + selectedTileName, 10, screenHeight - 20);
		
		for (int i=0;i<gridNames.length;i++) {
				MyListItem it = gridNames[i];
				if (it == null) continue;
				batch.draw(box, it.x*dim, screenHeight - it.y*dim);
				font.draw(batch, it.name.subSequence(0, 2), it.x*dim + 5, screenHeight - it.y*dim + 16);
			
		}

		batch.end();
		
		stage.act();
		stage.draw();
	}
	
	@Override
	public boolean mouseMoved (int screenX, int screenY) {
		currentMapCoords = new MyVector(Math.round(screenX/dim), Math.abs(Math.round((screenHeight - screenY)/dim - (t.getHeight()/dim))+1));		 
		return false;
	}
	
	@Override
	public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		int y = Math.round((screenHeight - screenY)/dim - (t.getHeight()/dim))+1;
		int x = Math.round(screenX/dim);
		
		if (y <= 0 && x < canvasGridWidth) {
			
			int nx = x;
			int ny = y*-1;
			
			selectedMapCoords = new MyVector(nx, ny);	
			
			if (selectedTileName != null) {
				
				MyListItem it = getItem(nx, ny);
				if (it == null) {
					setItem(nx, ny, new MyListItem(selectedTileName.name,nx, ny));
				} else {
					setItem(nx, ny, null);
				}
			}
			
			
		}
				
		return false;
	}
	
	@Override
	public boolean keyUp(int keycode) {
		if (keycode == Keys.SPACE) {
			new PopupDialog(this.skin, this.gridNames).show(stage);
		}
		return false;
	}
	
	public MyListItem getItem(int x, int y) {
		if (x < 0 || y < 0) {
			return null;
		}
		if (x + (y * dim) >= gridNames.length) {
			return null;
		}
		return gridNames[x + (y * dim)];
	}
	
	public void setItem(int x, int y, MyListItem item) {
		if (x < 0 || y < 0) {
			return;
		}
		if (x + (y * dim) >= gridNames.length) {
			return;
		}
		gridNames[x + (y * dim)] = item;
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
		public String name;
		public int x;
		public int y;
		public MyListItem(String name, int x, int y) {
			this.name = name;
			this.x = x;
			this.y = y;
		}
		@Override
		public String toString() {
			return String.format("%s %s, %s", name, x, y);
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + x;
			result = prime * result + y;
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			
			MyListItem other = (MyListItem) obj;

			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (x != other.x)
				return false;
			if (y != other.y)
				return false;
			return true;
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
		
		Settings settings = new Settings();
		AtlasWriter mrp = new AtlasWriter(settings);
		

		
		ArrayList<Rect> packedRects = new ArrayList<Rect>();
		for (int i=0;i<gridNames.length;i++) {
			MyListItem it = gridNames[i];
			if (it == null) continue;
			Rect rect = new Rect(it.x*dim, it.y*dim, dim, dim);
			rect.name = it.name;
			rect.index = 0;
			packedRects.add(rect);
		}
		
		System.out.println("Writing: number of sprites: " +packedRects.size());

		try {
			mrp.writePackFileWithRects(new File("."), "sprites-atlas.txt",packedRects, "sprites.png");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	    System.out.println("done");
	
	}
	
	@SuppressWarnings("unchecked")
	public void readAtlas() {
		
		try {
			java.util.List<String> lines = FileUtils.readLines(new File("sprites-atlas.txt"));
			for (int i=4;i<lines.size();i+=7) {
				String name = lines.get(i).trim();
				String xy = lines.get(i+2).trim();
				String[] sp = xy.split(":|,| ");
				//System.out.println(name + " "  + sp[2] + "," + sp[4]);
				
				int mx = Integer.parseInt(sp[2]) / 32;
				int my = Integer.parseInt(sp[4]) / 32;
				MyListItem item = new MyListItem(name, mx, my);
				setItem(mx, my, item);
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	
	
}
