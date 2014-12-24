package util;

import objects.Tile;
import ultima.Constants;
import ultima.DungeonScreen.DungeonRoom;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;

public class DungeonRoomTiledMapLoader implements Constants {
	
	private TextureAtlas atlas;
	public DungeonRoom room;
	public Direction entryDir;
	
	private int mapWidth;
	private int mapHeight;
	private int tileWidth;
	private int tileHeight;
	
	public DungeonRoomTiledMapLoader(DungeonRoom room, Direction entryDir, TextureAtlas atlas) {
		this.atlas = atlas;
		this.room = room;
		this.mapWidth = 11;
		this.mapHeight = 11;
		this.tileWidth = 16;
		this.tileHeight = 16;
		this.entryDir = entryDir;
	}
	
	public TiledMap load() {
		
		TiledMap map = new TiledMap();

		MapProperties mapProperties = map.getProperties();
		mapProperties.put("dungeonRoom", room);
		mapProperties.put("orientation", "orthogonal");
		mapProperties.put("width", mapWidth);
		mapProperties.put("height", mapHeight);
		mapProperties.put("tilewidth", tileWidth);
		mapProperties.put("tileheight", tileHeight);
		mapProperties.put("backgroundcolor", "#000000");
		
		TiledMapTileLayer layer = new TiledMapTileLayer(mapWidth, mapHeight, tileWidth, tileHeight);
		layer.setName("Map Layer");
		layer.setVisible(true);
		
		for (int y = 0; y < mapHeight; y++) {
			for (int x = 0; x < mapWidth; x++) {
				Tile ct = room.getTile(x,y);
				//if (room.getTriggerAt(x, y) != null) ct = GameScreen.baseTileSet.getTileByIndex(3); //temp debugging triggers
				Cell cell = new Cell();
				TextureRegion tileRegion = atlas.findRegion(ct.getName());
				TiledMapTile tmt = new StaticTiledMapTile(tileRegion);
				tmt.setId(y * mapWidth + x);
				cell.setTile(tmt);
				layer.setCell(x, mapHeight - 1 - y, cell);
			}
		}
					
		map.getLayers().add(layer);
		
		try {
			loadCombatPositions(map);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return map;
	}
	
	private void loadCombatPositions(TiledMap map) throws Exception {
				
//		0x0 	16 	start_x for monsters 0-15
//		0x10 	16 	start_y for monsters 0-15
//		0x20 	8 	start_x for party members 0-7
//		0x28 	8 	start_y for party members 0-7 
		
		MapLayer mlayer = new MapLayer();
		mlayer.setName("Monster Positions");
		for (int i=0;i<16;i++) {
			MapObject object = new MapObject();
			object.getProperties().put("index", 0);
			object.getProperties().put("tile", (int)room.monsters[i]&0xff);
			object.getProperties().put("startX", (int)room.monStartX[i]&0xff);
			object.getProperties().put("startY", (int)room.monStartY[i]&0xff);
			mlayer.getObjects().add(object);
		}
		
		map.getLayers().add(mlayer);

		
		MapLayer player = new MapLayer();
		player.setName("Player Positions");
		
		byte[] px = null;
		byte[] py = null;
		switch(entryDir) {
		case EAST:px = room.plStartXEast;py = room.plStartYEast;break;
		case NORTH:px = room.plStartXNorth;py = room.plStartYNorth;break;
		case SOUTH:px = room.plStartXSouth;py = room.plStartYSouth;break;
		case WEST:px = room.plStartXWest;py = room.plStartYWest;break;		
		}
		
		Position[] playerPos = new Position[8];
		for (int i=0;i<8;i++) {
			playerPos[i] = new Position(i, (int)px[i], 0);
			playerPos[i].startY = (int)py[i];
		}
		for (int i=0;i<8;i++) {
			MapObject object = new MapObject();
			object.getProperties().put("index", playerPos[i].index&0xff);
			object.getProperties().put("startX", playerPos[i].startX&0xff);
			object.getProperties().put("startY", playerPos[i].startY&0xff);
			player.getObjects().add(object);
		}
		map.getLayers().add(player);

		
	}
	
	class Position {
		int index;
		int startX;
		int startY;
		private Position(int index, int startX, int startY) {
			this.index = index;
			this.startX = startX;
			this.startY = startY;
		}
	}

	

}
