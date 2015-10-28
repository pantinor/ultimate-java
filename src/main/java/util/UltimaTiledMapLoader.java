package util;

import java.io.FileInputStream;
import java.io.InputStream;

import objects.Tile;

import org.apache.commons.io.IOUtils;

import ultima.Constants;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.utils.Array;
import objects.TileSet;

public class UltimaTiledMapLoader implements Constants {

    private TextureAtlas atlas;
    private Maps gameMap;

    private int mapWidth;
    private int mapHeight;
    private int tileWidth;
    private int tileHeight;

    public UltimaTiledMapLoader(Maps gameMap, TextureAtlas atlas, int mapWidth, int mapHeight, int tileWidth, int tileHeight) {
        this.atlas = atlas;
        this.gameMap = gameMap;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;

    }

    public TiledMap load() {

        TiledMap map = new TiledMap();

        MapProperties mapProperties = map.getProperties();
        mapProperties.put("name", gameMap.toString());
        mapProperties.put("id", gameMap.getId());
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
                Tile ct = gameMap.getMap().getTile(x, y);
                Cell cell = new Cell();

                Array<TextureAtlas.AtlasRegion> tileRegions = atlas.findRegions(ct.getName());
                Array<StaticTiledMapTile> ar = new Array<>();
                for (TextureAtlas.AtlasRegion r : tileRegions) {
                    ar.add(new StaticTiledMapTile(r));
                }
                if (ar.size == 0) {
                    System.out.println(ct.getName());
                }

                TiledMapTile tmt = null;
                if (tileRegions.size > 1) {
                    tmt = new AnimatedTiledMapTile(.7f, ar);
                } else {
                    tmt = ar.first();
                }

                tmt.setId(y * mapWidth + x);
                cell.setTile(tmt);
                layer.setCell(x, mapHeight - 1 - y, cell);
            }
        }

        map.getLayers().add(layer);

        if (gameMap.getMap().getType() == MapType.combat) {
            try {
                loadCombatPositions(map);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return map;
    }

    private void loadCombatPositions(TiledMap map) throws Exception {

        InputStream is = new FileInputStream("assets/data/" + gameMap.getMap().getFname());
        byte[] bytes = IOUtils.toByteArray(is);

//		0x0 	16 	start_x for monsters 0-15
//		0x10 	16 	start_y for monsters 0-15
//		0x20 	8 	start_x for party members 0-7
//		0x28 	8 	start_y for party members 0-7 
        MapLayer mlayer = new MapLayer();
        mlayer.setName("Monster Positions");

        Position[] monPos = new Position[16];
        for (int i = 0; i < 16; i++) {
            monPos[i] = new Position(i, (int) bytes[i], 0);
        }
        for (int i = 0; i < 16; i++) {
            monPos[i].startY = (int) bytes[i + 16];
        }
        for (int i = 0; i < 16; i++) {
            MapObject object = new MapObject();
            object.getProperties().put("index", monPos[i].index);
            object.getProperties().put("startX", monPos[i].startX);
            object.getProperties().put("startY", monPos[i].startY);
            mlayer.getObjects().add(object);
        }

        map.getLayers().add(mlayer);

        MapLayer player = new MapLayer();
        player.setName("Player Positions");

        Position[] playerPos = new Position[8];
        for (int i = 0; i < 8; i++) {
            playerPos[i] = new Position(i, (int) bytes[i + 32], 0);
        }
        for (int i = 0; i < 8; i++) {
            playerPos[i].startY = (int) bytes[i + 40];
        }
        for (int i = 0; i < 8; i++) {
            MapObject object = new MapObject();
            object.getProperties().put("index", playerPos[i].index);
            object.getProperties().put("startX", playerPos[i].startX);
            object.getProperties().put("startY", playerPos[i].startY);
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

    //for intro map
    public TiledMap load(byte[] bytes, int width, int height, TileSet ts, int tileDim) {

        this.mapWidth = width;
        this.mapHeight = height;

        Tile[] tiles = new Tile[width * height];
        int pos = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = bytes[pos] & 0xff; // convert a byte to an unsigned int value
                pos++;
                Tile tile = ts.getTileByIndex(index);
                if (tile == null) {
                    System.out.println("Tile index cannot be found: " + index + " using index 129 for black space.");
                    tile = ts.getTileByIndex(129);
                }
                tiles[x + y * width] = tile;
            }
        }

        TiledMap map = new TiledMap();

        MapProperties mapProperties = map.getProperties();
        mapProperties.put("name", "none");
        mapProperties.put("id", "none");
        mapProperties.put("orientation", "orthogonal");
        mapProperties.put("width", mapWidth);
        mapProperties.put("height", mapHeight);
        mapProperties.put("tilewidth", tileDim);
        mapProperties.put("tileheight", tileDim);
        mapProperties.put("backgroundcolor", "#000000");

        TiledMapTileLayer layer = new TiledMapTileLayer(mapWidth, mapHeight, tileWidth, tileHeight);
        layer.setName("Map Layer");
        layer.setVisible(true);

        int dx = 0, dy = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Tile ct = tiles[x + y * width];
                Cell cell = new Cell();

                Array<TextureAtlas.AtlasRegion> tileRegions = atlas.findRegions(ct.getName());
                Array<StaticTiledMapTile> ar = new Array<>();
                for (TextureAtlas.AtlasRegion r : tileRegions) {
                    ar.add(new StaticTiledMapTile(r));
                }
                if (ar.size == 0) {
                    System.out.println(ct.getName());
                }

                TiledMapTile tmt = null;
                if (tileRegions.size > 1) {
                    tmt = new AnimatedTiledMapTile(.7f, ar);
                } else {
                    tmt = ar.first();
                }

                tmt.setId(dy * mapWidth + dx);
                cell.setTile(tmt);
                layer.setCell(dx, mapHeight - 1 - dy, cell);
                dx++;
            }
            dx = 0;
            dy++;
        }

        map.getLayers().add(layer);

        return map;
    }

}
