package test;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import objects.BaseMap;
import objects.MapSet;
import objects.Person;
import objects.Tile;
import objects.TileSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Region;
import java.io.FileInputStream;
import java.util.List;
import ultima.Constants;

public class UltMapTmxConvert implements ApplicationListener {

    public static void main(String[] args) throws Exception {

        new LwjglApplication(new UltMapTmxConvert());
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
            FileHandle f = new FileHandle("assets/tilemaps/tiles-enhanced-vga-atlas.txt");
            TextureAtlasData atlas = new TextureAtlasData(f, f.parent(), false);
            int png_grid_width = 20;
            Tile[] mapTileIds = new Tile[png_grid_width * Constants.tilePixelWidth + 1];
            for (Region r : atlas.getRegions()) {
                int x = r.left / r.width;
                int y = r.top / r.height;
                int i = x + (y * png_grid_width) + 1;
                mapTileIds[i] = ts.getTileByName(r.name);
            }

            for (BaseMap map : ms.getMaps()) {

                if (!map.getFname().endsWith("ult")) {
                    continue;
                }

                FileInputStream is = new FileInputStream("assets/data/" + map.getFname());
                byte[] bytes = IOUtils.toByteArray(is);

                Tile[] tiles = new Tile[map.getWidth() * map.getHeight()];
                int pos = 0;
                for (int y = 0; y < map.getHeight(); y++) {
                    for (int x = 0; x < map.getWidth(); x++) {
                        int index = bytes[pos] & 0xff; // convert a byte to an unsigned int value
                        pos++;
                        Tile tile = ts.getTileByIndex(index);
                        if (tile == null) {
                            System.out.println("Tile index cannot be found: " + index + " using index 129 for black space.");
                            tile = ts.getTileByIndex(129);
                        }
                        tiles[x + y * map.getWidth()] = tile;
                    }
                }

                //map layer
                StringBuilder data = new StringBuilder();
                int count = 1;
                int total = 1;
                for (int i = 0; i < tiles.length; i++) {
                    Tile t = tiles[i];
                    data.append(findTileMapId(mapTileIds, t.getName())).append(",");
                    count++;
                    total++;
                    if (count > png_grid_width * 32) {
                        data.append("\n");
                        count = 1;
                    }
                    if (total > png_grid_width * 32 * 24 * 32) {
                        break;
                    }
                }

                String d = data.toString();
                d = d.substring(0, d.length() - 2);

                List<Person> people = map.getCity().getPeople();

                StringBuilder peopleBuffer = new StringBuilder();
                if (people != null) {
                    for (int y = 0; y < map.getHeight(); y++) {
                        for (int x = 0; x < map.getWidth(); x++) {
                            Person p = findPersonAtCoords(people, x, y);
                            if (p == null) {
                                peopleBuffer.append("0,");
                            } else {
                                peopleBuffer.append(findTileMapId(mapTileIds, p.getTile().getName())).append(",");
                            }
                        }
                        peopleBuffer.append("\n");
                    }
                }

                String p = peopleBuffer.toString();
                if (p == null || p.length() < 1) {
                    count = 1;
                    //make empty
                    for (int i = 0; i < map.getWidth() * map.getHeight(); i++) {
                        peopleBuffer.append("0,");
                        count++;
                        if (count > map.getWidth()) {
                            peopleBuffer.append("\n");
                            count = 1;
                        }
                    }
                    p = peopleBuffer.toString();
                }
                p = p.substring(0, p.length() - 2);

                Formatter c = new Formatter(map.getFname(), "tiles-enhanced-vga.png", map.getWidth(), map.getHeight(),
                        Constants.tilePixelWidth, Constants.tilePixelWidth, d, p, people);

                String tmxFName = String.format("tmx/map_%s_%s.tmx", map.getId(), map.getCity().getName().replace(" ", ""));

                FileUtils.writeStringToFile(new File(tmxFName), c.toString());

                System.out.printf("Wrote: %s\n",tmxFName);

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

    private int findTileMapId(Tile[] tiles, String name) {
        for (int i = 1; i < tiles.length; i++) {
            if (tiles[i] == null) {
                continue;
            }
            if (StringUtils.equals(tiles[i].getName(), name)) {
                return i;
            }
        }
        return 0;
    }
    
    private Person findPersonAtCoords(List<Person> people, int x, int y) {
        for (Person p : people) {
            if (p != null && (p.getStart_x() == x && p.getStart_y() == y)) {
                return p;
            }
        }
        return null;
    }

    private static class Formatter {

        private String tilesetName;
        private String imageSource;
        private int mapWidth;
        private int mapHeight;
        private int tileWidth;
        private int tileHeight;
        private String data;
        private String people;
        private List<Person> persons;

        public Formatter(String tilesetName, String imageSource, int mapWidth, int mapHeight, int tileWidth, int tileHeight, String data, String people, List<Person> persons) {
            this.tilesetName = tilesetName;
            this.imageSource = imageSource;
            this.mapWidth = mapWidth;
            this.mapHeight = mapHeight;
            this.tileWidth = tileWidth;
            this.tileHeight = tileHeight;
            this.data = data;
            this.people = people;
            this.persons = persons;
        }

        @Override
        public String toString() {

            StringBuilder personsString = new StringBuilder();
            if (persons != null) {
                for (Person p : persons) {
                    if (p == null) {
                        continue;
                    }
                    //System.out.println(p);
                    personsString.append(p.toTMXString());
                }
            }

            String template = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<map version=\"1.0\" orientation=\"orthogonal\" width=\"%s\" height=\"%s\" tilewidth=\"%s\" tileheight=\"%s\" backgroundcolor=\"#000000\">\n"
                    + "<tileset firstgid=\"1\" name=\"%s\" tilewidth=\"%s\" tileheight=\"%s\">\n"
                    + "<image source=\"%s\" width=\"640\" height=\"768\"/>\n</tileset>\n"
                    + "<layer name=\"Map Layer\" width=\"%s\" height=\"%s\">\n"
                    + "<data encoding=\"csv\">\n%s\n</data>\n</layer>\n"
                    + "<layer name=\"People Layer\" width=\"%s\" height=\"%s\">\n"
                    + "<data encoding=\"csv\">\n%s\n</data>\n</layer>\n"
                    + "<objectgroup name=\"Person Properties\" width=\"%s\" height=\"%s\">\n%s\n</objectgroup>\n"
                    + "</map>";

            return String.format(template, mapWidth, mapHeight, tileWidth, tileHeight,
                    tilesetName, tileWidth, tileHeight,
                    imageSource,
                    mapWidth, mapHeight, data,
                    mapWidth, mapHeight, people,
                    mapWidth, mapHeight, personsString.toString());

        }
    }
}
