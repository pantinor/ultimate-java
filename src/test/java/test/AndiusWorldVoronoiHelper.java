package test;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import objects.BaseMap;
import objects.MapSet;
import objects.Tile;
import objects.TileSet;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Region;
import objects.Moongate;
import objects.Portal;
import ultima.Constants;
import util.Utils;

public class AndiusWorldVoronoiHelper implements ApplicationListener {

    public static void main(String[] args) throws Exception {

        new LwjglApplication(new AndiusWorldVoronoiHelper());
    }

    @Override
    public void create() {

        try {

            File file2 = new File("assets/xml/tileset-base.xml");
            JAXBContext jaxbContext = JAXBContext.newInstance(TileSet.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            TileSet ts = (TileSet) jaxbUnmarshaller.unmarshal(file2);
            ts.setMaps();

            File file3 = new File("assets/xml/maps.xml");
            jaxbContext = JAXBContext.newInstance(MapSet.class);
            jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            MapSet ms = (MapSet) jaxbUnmarshaller.unmarshal(file3);
            ms.init(ts);

            //load the atlas and determine the tile indexes per tilemap position
            FileHandle f = new FileHandle("assets/tilemaps/latest-atlas.txt");
            TextureAtlasData atlas = new TextureAtlasData(f, f.parent(), false);
            int png_grid_width = 24;
            Tile[] mapTileIds = new Tile[png_grid_width * Constants.tilePixelWidth + 1];
            for (Region r : atlas.getRegions()) {
                int x = r.left / r.width;
                int y = r.top / r.height;
                int i = x + (y * png_grid_width) + 1;
                mapTileIds[i] = ts.getTileByName(r.name);
            }

            BaseMap world = Constants.Maps.WORLD.getMap();
            Utils.setMapTiles(world, ts);
            Tile[] tiles = world.getTiles();

            int count = 1;
            StringBuilder sb = new StringBuilder("tmp.add(\"");
            for (int i = 0; i < tiles.length; i++) {
                Tile t = tiles[i];
                sb.append(t.getName()).append(",");
                count++;
                if (count > 256) {
                    sb.append("\");\ntmp.add(\"");
                    count = 1;
                }
            }

            System.out.println(sb);

            for (Portal p : world.getPortals()) {
                p.setName(Constants.Maps.get(p.getDestmapid()).toString());
            }
            

            count = 1;
            for (Portal p : world.getPortals()) {
                if (p == null) {
                    continue;
                }
                System.out.print(String.format("%s,%s,%s,%s|", count, p.getName().toUpperCase(), p.getX() * 24, +p.getY() * 24));
                count++;
            }
            

            count = 1;
            for (Moongate p : world.getMoongates()) {
                if (p == null) {
                    continue;
                }
                System.out.print(String.format("%s,%s,%s,%s,%s,%s,%s|",
                        count, p.getPhase(), p.getX() * 24, p.getY() * 24,
                        p.getD1().toUpperCase(), p.getD2().toUpperCase(), p.getD3().toUpperCase()));
                count++;
            }
            
            
            for (Tile t : ts.getTiles()) {
                System.out.println(String.format("case \"%s\":\nc.height=21;\nbreak;\n", t.getName()));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("DONE");
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void render() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
    }

}
