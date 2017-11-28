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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Region;
import java.io.FileInputStream;
import java.util.List;
import java.util.Random;
import objects.Moongate;
import objects.Portal;
import ultima.Constants;
import util.Utils;

public class AndiusMapTmxConvert implements ApplicationListener {

    public static void main(String[] args) throws Exception {

        new LwjglApplication(new AndiusMapTmxConvert());
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
            String tmxFName = "tmx/andius/world.tmx";
            WorldFormatter c = new WorldFormatter(world.getWidth(), world.getHeight(), tiles, world.getPortals(), world.getMoongates());
            FileUtils.writeStringToFile(new File(tmxFName), c.toString());
            System.out.printf("Wrote: %s\n", tmxFName);

            for (BaseMap map : ms.getMaps()) {

                if (!map.getFname().endsWith("ult")) {
                    continue;
                }

                FileInputStream is = new FileInputStream("assets/data/" + map.getFname());
                byte[] bytes = IOUtils.toByteArray(is);

                tiles = new Tile[map.getWidth() * map.getHeight()];
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

                tmxFName = String.format("tmx/andius/%s.tmx", map.getCity().getName().replace(" ", "").toLowerCase());
                if (map.getFname().equals("lcb_2.ult")) {
                    tmxFName = "tmx/andius/britannia2.tmx";
                }

                Formatter fmtter = new Formatter(map.getWidth(), map.getHeight(), tiles);
                FileUtils.writeStringToFile(new File(tmxFName), fmtter.toString());

                System.out.printf("Wrote: %s\n", tmxFName);

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

    private static int findFloorId(String name) {
        int id = -1;
        switch (name) {
            case "grass":
            case "brush":
                id = 125 + new Random().nextInt(2);
                break;

            case "water":
            case "shallows":
            case "sea":
                id = 115;
                break;

            case "swamp":
            case "forest":
            case "hills":
                id = 125 + new Random().nextInt(2);
                break;

            case "bridge_piece1":
            case "bridge_piece2":
                id = 165 + new Random().nextInt(4);
                break;

            case "wood_floor":
                id = 105 + new Random().nextInt(4);
                break;

            case "lava":
                id = 215;
                break;
            case "campfire":
                id = 726;
                break;

            case "dungeon_floor":
                id = 205 + new Random().nextInt(4);
                break;

            case "sleep_field":
            case "fire_field":
            case "energy_field":
            case "poison_field":
            case "down_ladder":
            case "up_ladder":
            case "chest":
            case "brick_floor":
            case "door":
            case "locked_door":
            case "secret_door":
                id = 110;
                break;

        }
        return id + 1;
    }

    private static int findDoorId(String name) {
        int id = -1;
        switch (name) {
            case "door":
                id = 272;
                break;
            case "locked_door":
                id = 332;
                break;
            case "secret_door":
                id = 512;
                break;
        }
        return id + 1;
    }

    private static int findWallId(String name) {
        int id = -1;
        switch (name) {
            case "brick_wall":
                id = 261;
                break;
        }
        return id + 1;
    }

    private static int findPropId(String name) {
        int id = -1;
        switch (name) {
            case "down_ladder":
                id = 48;
                break;
            case "up_ladder":
                id = 48;
                break;
            case "chest":
                id = 624;
                break;
            case "ankh":
                id = 689;
                break;
            case "corpse":
                id = 626;
                break;
            case "lava":
                id = 215;
                break;
            case "campfire":
                id = 726;
                break;
            case "sleep_field":
                id = 155;
                break;
            case "fire_field":
                id = 215;
                break;
            case "energy_field":
                id = 175;
                break;
            case "poison_field":
                id = 195;
                break;
            case "dungeon":
                id = 335;
                break;
            case "city":
                id = 386;
                break;
            case "lcb_west":
            case "lcb_east":
            case "lcb_entrance":
            case "castle":
                id = 260;
                break;
            case "town":
                id = 388;
                break;
            case "ruins":
                id = 394;
                break;
            case "shrine":
                id = 420;
                break;
            case "bridge":
                id = 437;
                break;
        }
        return id + 1;
    }

    private static int findGrassId(String name) {
        int id = -1;
        switch (name) {
            case "dungeon":
            case "city":
            case "lcb_west":
            case "lcb_east":
            case "lcb_entrance":
            case "castle":
            case "town":
            case "ruins":
            case "shrine":
            case "grass":
            case "brush":
            case "swamp":
            case "forest":
            case "lava":
            case "fire_field":
            case "ankh":
            case "bridge":
            case "hills":
                id = 0;
                break;

        }
        return id + 1;
    }

    private static int findWaterId(String name) {
        int id = -1;
        switch (name) {
            case "water":
                id = 117;
                break;
            case "shallows":
                id = 92;
                break;
            case "sea":
                id = 17;
                break;

        }
        return id + 1;
    }

    private static int findMeadowId(String name) {
        int id = -1;
        switch (name) {
            case "brush":
                id = new Random().nextInt(2) == 1 ? 156 : 157;
                break;
            case "swamp":
                id = new Random().nextInt(2) == 1 ? 145 : 170;
                break;
            case "ankh":
                id = 440;
                break;

        }
        return id + 1;
    }

    private static int findForestId(String name) {
        int id = -1;
        switch (name) {
            case "fire_field":
            case "lava":
                id = 67;
                break;
            case "forest":
                id = 178;
                break;
            case "hills":
                id = 147;
                break;

        }
        return id + 1;
    }

    private static int findMountainId(String name) {
        int id = -1;
        switch (name) {
            case "mountains":
                id = 401;
                break;

        }
        return id + 1;
    }

    private static int findTileId(Tile[] tiles, String name) {
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

    private static Portal findPortalAtCoords(List<Portal> portals, int x, int y) {
        for (Portal p : portals) {
            if (p != null && (p.getX() == x && p.getY() == y)) {
                return p;
            }
        }
        return null;
    }

    private static Moongate findMoongateAtCoords(List<Moongate> moongates, int x, int y) {
        for (Moongate p : moongates) {
            if (p != null && (p.getX() == x && p.getY() == y)) {
                return p;
            }
        }
        return null;
    }

    private static class Formatter {

        private int mapWidth;
        private int mapHeight;

        private StringBuilder floor = new StringBuilder();
        private StringBuilder doors = new StringBuilder();
        private StringBuilder props = new StringBuilder();
        private StringBuilder walls = new StringBuilder();

        public Formatter(int mapWidth, int mapHeight, Tile[] tiles) {
            this.mapWidth = mapWidth;
            this.mapHeight = mapHeight;

            int count = 1;
            for (int i = 0; i < tiles.length; i++) {
                Tile t = tiles[i];
                floor.append(findFloorId(t.getName())).append(",");
                count++;
                if (count > 32) {
                    floor.append("\n");
                    count = 1;
                }
            }
            floor.deleteCharAt(floor.length() - 2);

            count = 1;
            for (int i = 0; i < tiles.length; i++) {
                Tile t = tiles[i];
                doors.append(findDoorId(t.getName())).append(",");
                count++;
                if (count > 32) {
                    doors.append("\n");
                    count = 1;
                }
            }
            doors.deleteCharAt(doors.length() - 2);

            count = 1;
            for (int i = 0; i < tiles.length; i++) {
                Tile t = tiles[i];
                props.append(findPropId(t.getName())).append(",");
                count++;
                if (count > 32) {
                    props.append("\n");
                    count = 1;
                }
            }
            props.deleteCharAt(props.length() - 2);

            count = 1;
            for (int i = 0; i < tiles.length; i++) {
                Tile t = tiles[i];
                walls.append(findWallId(t.getName())).append(",");
                count++;
                if (count > 32) {
                    walls.append("\n");
                    count = 1;
                }
            }
            walls.deleteCharAt(walls.length() - 2);

        }

        @Override
        public String toString() {

            StringBuffer sb = new StringBuffer();
            for (int y = 0; y < this.mapWidth; y++) {
                for (int x = 0; x < this.mapHeight; x++) {
                    sb.append("0,");
                }
                sb.append("\n");
            }
            sb.deleteCharAt(sb.length() - 2);

            String template = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<map version=\"1.0\" orientation=\"orthogonal\" renderorder=\"right-down\" width=\"" + this.mapWidth + "\" height=\"" + this.mapHeight + "\" tilewidth=\"48\" tileheight=\"48\" nextobjectid=\"1\">\n"
                    + " <properties>\n"
                    + "  <property name=\"startX\" value=\"15\"/>\n"
                    + "  <property name=\"startY\" value=\"31\"/>\n"
                    + " </properties>"
                    + " <tileset firstgid=\"1\" name=\"terrain\" tilewidth=\"48\" tileheight=\"48\" tilecount=\"760\" columns=\"20\">\n"
                    + "  <image source=\"uf_terrain.png\" width=\"960\" height=\"1824\"/>\n"
                    + "  <tile id=\"115\">\n"
                    + "   <animation>\n"
                    + "    <frame tileid=\"115\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"116\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"117\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"118\" duration=\"500\"/>\n"
                    + "   </animation>\n"
                    + "  </tile>\n"
                    + "  <tile id=\"135\">\n"
                    + "   <animation>\n"
                    + "    <frame tileid=\"135\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"136\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"137\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"138\" duration=\"500\"/>\n"
                    + "   </animation>\n"
                    + "  </tile>\n"
                    + "  <tile id=\"155\">\n"
                    + "   <animation>\n"
                    + "    <frame tileid=\"155\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"156\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"157\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"158\" duration=\"500\"/>\n"
                    + "   </animation>\n"
                    + "  </tile>\n"
                    + "  <tile id=\"175\">\n"
                    + "   <animation>\n"
                    + "    <frame tileid=\"175\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"176\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"177\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"178\" duration=\"500\"/>\n"
                    + "   </animation>\n"
                    + "  </tile>\n"
                    + "  <tile id=\"195\">\n"
                    + "   <animation>\n"
                    + "    <frame tileid=\"195\" duration=\"300\"/>\n"
                    + "    <frame tileid=\"196\" duration=\"300\"/>\n"
                    + "    <frame tileid=\"197\" duration=\"300\"/>\n"
                    + "    <frame tileid=\"198\" duration=\"300\"/>\n"
                    + "   </animation>\n"
                    + "  </tile>\n"
                    + "  <tile id=\"215\">\n"
                    + "   <animation>\n"
                    + "    <frame tileid=\"215\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"216\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"217\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"218\" duration=\"500\"/>\n"
                    + "   </animation>\n"
                    + "  </tile>\n"
                    + "  <tile id=\"644\">\n"
                    + "   <animation>\n"
                    + "    <frame tileid=\"644\" duration=\"200\"/>\n"
                    + "    <frame tileid=\"645\" duration=\"200\"/>\n"
                    + "   </animation>\n"
                    + "  </tile>\n"
                    + "  <tile id=\"664\">\n"
                    + "   <animation>\n"
                    + "    <frame tileid=\"664\" duration=\"200\"/>\n"
                    + "    <frame tileid=\"665\" duration=\"200\"/>\n"
                    + "   </animation>\n"
                    + "  </tile>\n"
                    + "  <tile id=\"672\">\n"
                    + "   <animation>\n"
                    + "    <frame tileid=\"672\" duration=\"750\"/>\n"
                    + "    <frame tileid=\"673\" duration=\"750\"/>\n"
                    + "   </animation>\n"
                    + "  </tile>\n"
                    + "  <tile id=\"674\">\n"
                    + "   <animation>\n"
                    + "    <frame tileid=\"674\" duration=\"750\"/>\n"
                    + "    <frame tileid=\"675\" duration=\"750\"/>\n"
                    + "   </animation>\n"
                    + "  </tile>\n"
                    + " </tileset>\n"
                    + " <tileset firstgid=\"761\" name=\"heroes\" tilewidth=\"48\" tileheight=\"48\" tilecount=\"520\" columns=\"40\">\n"
                    + "  <image source=\"uf_heroes.png\" width=\"1920\" height=\"624\"/>\n"
                    + " </tileset>"
                    + "<layer name=\"floor\" width=\"" + this.mapWidth + "\" height=\"" + this.mapHeight + "\">\n"
                    + "  <data encoding=\"csv\">\n"
                    + "   %s\n"
                    + "  </data>\n"
                    + " </layer>\n"
                    + " <layer name=\"floor 2\" width=\"" + this.mapWidth + "\" height=\"" + this.mapHeight + "\">\n"
                    + "  <data encoding=\"csv\">\n"
                    + "   " + sb.toString()
                    + "  </data>\n"
                    + " </layer>\n"
                    + " <layer name=\"props\" width=\"" + this.mapWidth + "\" height=\"" + this.mapHeight + "\">\n"
                    + "  <data encoding=\"csv\">\n"
                    + "   %s\n"
                    + "  </data>\n"
                    + " </layer>\n"
                    + " <layer name=\"creature\" width=\"" + this.mapWidth + "\" height=\"" + this.mapHeight + "\">\n"
                    + "  <data encoding=\"csv\">\n"
                    + "   " + sb.toString()
                    + "  </data>\n"
                    + " </layer>\n"
                    + " <layer name=\"water_edges\" width=\"" + this.mapWidth + "\" height=\"" + this.mapHeight + "\">\n"
                    + "  <data encoding=\"csv\">\n"
                    + "   " + sb.toString()
                    + "  </data>\n"
                    + " </layer>\n"
                    + " <layer name=\"shadows\" width=\"" + this.mapWidth + "\" height=\"" + this.mapHeight + "\">\n"
                    + "  <data encoding=\"csv\">\n"
                    + "   " + sb.toString()
                    + "  </data>\n"
                    + " </layer>\n"
                    + " <layer name=\"walls\" width=\"" + this.mapWidth + "\" height=\"" + this.mapHeight + "\">\n"
                    + "  <data encoding=\"csv\">\n"
                    + "   %s\n"
                    + "  </data>\n"
                    + " </layer>\n"
                    + " <layer name=\"door\" width=\"" + this.mapWidth + "\" height=\"" + this.mapHeight + "\">\n"
                    + "  <data encoding=\"csv\">\n"
                    + "   %s\n"
                    + "  </data>\n"
                    + " </layer>\n"
                    + " <layer name=\"torches\" width=\"" + this.mapWidth + "\" height=\"" + this.mapHeight + "\">\n"
                    + "  <data encoding=\"csv\">\n"
                    + "   " + sb.toString()
                    + "  </data>\n"
                    + " </layer>\n"
                    + " <layer name=\"webs\" width=\"" + this.mapWidth + "\" height=\"" + this.mapHeight + "\">\n"
                    + "  <data encoding=\"csv\">\n"
                    + "   " + sb.toString()
                    + "  </data>\n"
                    + " </layer>\n"
                    + " <objectgroup name=\"people\">\n"
                    + "  <object id=\"1\" name=\"Jason\" type=\"INNKEEPER\" x=\"864\" y=\"96\" width=\"48\" height=\"48\">\n"
                    + "   <properties>\n"
                    + "    <property name=\"movement\" value=\"FIXED\"/>\n"
                    + "   </properties>\n"
                    + "  </object>\n"
                    + " </objectgroup>"
                    + " <objectgroup name=\"portals\">\n"
                    + "  <object id=\"1\" name=\"WORLD\" type=\"portal\" x=\"1632\" y=\"1920\" width=\"48\" height=\"48\"/>\n"
                    + "  <object id=\"2\" name=\"WIWOLD_LVL_2\" type=\"portal\" x=\"240\" y=\"624\" width=\"48\" height=\"48\"/>\n"
                    + " </objectgroup>"
                    + " <objectgroup name=\"rooms\">\n"
                    + "  <object id=\"1\" x=\"1584\" y=\"1824\">\n"
                    + "   <polygon points=\"0,0 0,240 336,240 336,0 240,0 240,-48 96,-48 96,0\"/>\n"
                    + "  </object>"
                    + " </objectgroup>"
                    + "</map>\n";

            return String.format(template, floor, props, walls, doors);

        }
    }

    private static class WorldFormatter {

        private int mapWidth;
        private int mapHeight;

        private StringBuilder water = new StringBuilder();
        private StringBuilder grass = new StringBuilder();
        private StringBuilder meadow = new StringBuilder();
        private StringBuilder forest = new StringBuilder();
        private StringBuilder mountains = new StringBuilder();
        private StringBuilder props = new StringBuilder();

        private StringBuffer portalString = new StringBuffer();
        private StringBuffer moongateString = new StringBuffer();

        public WorldFormatter(int mapWidth, int mapHeight, Tile[] tiles,
                //String portalLayer, String moongateLayer,
                List<Portal> portals, List<Moongate> moongates) {

            this.mapWidth = mapWidth;
            this.mapHeight = mapHeight;

            int count = 1;
            for (int i = 0; i < tiles.length; i++) {
                Tile t = tiles[i];
                this.water.append(findWaterId(t.getName())).append(",");
                count++;
                if (count > 32) {
                    this.water.append("\n");
                    count = 1;
                }
            }
            this.water.deleteCharAt(this.water.length() - 2);

            count = 1;
            for (int i = 0; i < tiles.length; i++) {
                Tile t = tiles[i];
                this.grass.append(findGrassId(t.getName())).append(",");
                count++;
                if (count > 32) {
                    this.grass.append("\n");
                    count = 1;
                }
            }
            this.grass.deleteCharAt(this.grass.length() - 2);

            count = 1;
            for (int i = 0; i < tiles.length; i++) {
                Tile t = tiles[i];
                this.meadow.append(findMeadowId(t.getName())).append(",");
                count++;
                if (count > 32) {
                    this.meadow.append("\n");
                    count = 1;
                }
            }
            this.meadow.deleteCharAt(this.meadow.length() - 2);

            count = 1;
            for (int i = 0; i < tiles.length; i++) {
                Tile t = tiles[i];
                this.forest.append(findForestId(t.getName())).append(",");
                count++;
                if (count > 32) {
                    this.forest.append("\n");
                    count = 1;
                }
            }
            this.forest.deleteCharAt(this.forest.length() - 2);

            count = 1;
            for (int i = 0; i < tiles.length; i++) {
                Tile t = tiles[i];
                this.mountains.append(findMountainId(t.getName())).append(",");
                count++;
                if (count > 32) {
                    this.mountains.append("\n");
                    count = 1;
                }
            }
            this.mountains.deleteCharAt(this.mountains.length() - 2);

            count = 1;
            for (int i = 0; i < tiles.length; i++) {
                Tile t = tiles[i];
                this.props.append(findPropId(t.getName())).append(",");
                count++;
                if (count > 32) {
                    this.props.append("\n");
                    count = 1;
                }
            }
            this.props.deleteCharAt(this.props.length() - 2);

            for (Portal p : portals) {
                p.setName(Constants.Maps.get(p.getDestmapid()).toString());
            }

            count = 1;
            for (Portal p : portals) {
                if (p == null) {
                    continue;
                }
                portalString.append("<object id=\"" + count + "\" name=\"" + p.getName().toUpperCase() + "\" type=\"portal\" x=\"" + p.getX() * 24 + "\" y=\"" + p.getY() * 24 + "\" width=\"24\" height=\"24\"/>\n");
                count++;
            }

            count = 1;
            for (Moongate p : moongates) {
                if (p == null) {
                    continue;
                }
                moongateString.append("<object id=\"" + count + "\" name=\"GATE_" + p.getPhase() + "\" type=\"moongate\" x=\"" + p.getX() * 24 + "\" y=\"" + p.getY() * 24 + "\" width=\"24\" height=\"24\">\n"
                        + "<properties>\n"
                        + "<property name=\"d1\" value=\"" + p.getD1().toUpperCase() + "\"/>\n"
                        + "<property name=\"d2\" value=\"" + p.getD2().toUpperCase() + "\"/>\n"
                        + "<property name=\"d3\" value=\"" + p.getD3().toUpperCase() + "\"/>\n"
                        + "</properties>\n"
                        + "</object>\n");
                count++;
            }

        }

        @Override
        public String toString() {

            String template = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<map version=\"1.0\" orientation=\"orthogonal\" renderorder=\"right-down\" width=\"" + this.mapWidth + "\" height=\"" + this.mapWidth + "\" tilewidth=\"24\" tileheight=\"24\" nextobjectid=\"17\">\n"
                    + " <properties>\n"
                    + "  <property name=\"startX\" value=\"23\"/>\n"
                    + "  <property name=\"startY\" value=\"53\"/>\n"
                    + " </properties>\n"
                    + "<tileset firstgid=\"1\" name=\"uf_map\" tilewidth=\"24\" tileheight=\"24\" tilecount=\"525\" columns=\"25\">\n"
                    + "  <image source=\"uf_map.png\" width=\"600\" height=\"504\"/>\n"
                    + "  <terraintypes>\n"
                    + "   <terrain name=\"forest\" tile=\"178\"/>\n"
                    + "   <terrain name=\"meadow\" tile=\"147\"/>\n"
                    + "  </terraintypes>\n"
                    + "  <tile id=\"17\">\n"
                    + "   <animation>\n"
                    + "    <frame tileid=\"17\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"18\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"19\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"20\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"19\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"18\" duration=\"500\"/>\n"
                    + "   </animation>\n"
                    + "  </tile>\n"
                    + "  <tile id=\"67\">\n"
                    + "   <animation>\n"
                    + "    <frame tileid=\"67\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"68\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"69\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"70\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"69\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"68\" duration=\"500\"/>\n"
                    + "   </animation>\n"
                    + "  </tile>\n"
                    + "  <tile id=\"92\">\n"
                    + "   <animation>\n"
                    + "    <frame tileid=\"92\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"93\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"94\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"95\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"94\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"93\" duration=\"500\"/>\n"
                    + "   </animation>\n"
                    + "  </tile>\n"
                    + "  <tile id=\"117\">\n"
                    + "   <animation>\n"
                    + "    <frame tileid=\"117\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"118\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"119\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"120\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"119\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"118\" duration=\"500\"/>\n"
                    + "   </animation>\n"
                    + "  </tile>\n"
                    + "  <tile id=\"127\" terrain=\",,0,0\"/>\n"
                    + "  <tile id=\"128\" terrain=\",,0,0\"/>\n"
                    + "  <tile id=\"129\" terrain=\",,0,0\"/>\n"
                    + "  <tile id=\"144\" terrain=\"1,1,,1\"/>\n"
                    + "  <tile id=\"147\" terrain=\"1,1,1,1\"/>\n"
                    + "  <tile id=\"151\" terrain=\",,,0\"/>\n"
                    + "  <tile id=\"152\" terrain=\",0,0,0\"/>\n"
                    + "  <tile id=\"153\" terrain=\",,0,0\"/>\n"
                    + "  <tile id=\"154\" terrain=\"0,,0,0\"/>\n"
                    + "  <tile id=\"155\" terrain=\",,0,\"/>\n"
                    + "  <tile id=\"166\" terrain=\",,,1\"/>\n"
                    + "  <tile id=\"167\" terrain=\",,1,1\"/>\n"
                    + "  <tile id=\"168\" terrain=\",,1,\"/>\n"
                    + "  <tile id=\"169\" terrain=\",1,1,1\"/>\n"
                    + "  <tile id=\"175\" terrain=\",0,,0\"/>\n"
                    + "  <tile id=\"176\" terrain=\",0,,0\"/>\n"
                    + "  <tile id=\"177\" terrain=\",0,,0\"/>\n"
                    + "  <tile id=\"178\" terrain=\"0,0,0,0\"/>\n"
                    + "  <tile id=\"179\" terrain=\"0,,0,\"/>\n"
                    + "  <tile id=\"180\" terrain=\"0,,0,\"/>\n"
                    + "  <tile id=\"181\" terrain=\"0,,0,\"/>\n"
                    + "  <tile id=\"191\" terrain=\",1,,1\"/>\n"
                    + "  <tile id=\"193\" terrain=\"1,,1,\"/>\n"
                    + "  <tile id=\"194\" terrain=\"1,1,1,\"/>\n"
                    + "  <tile id=\"201\" terrain=\",0,,\"/>\n"
                    + "  <tile id=\"202\" terrain=\"0,0,,0\"/>\n"
                    + "  <tile id=\"203\" terrain=\"0,0,,\"/>\n"
                    + "  <tile id=\"204\" terrain=\"0,0,0,\"/>\n"
                    + "  <tile id=\"205\" terrain=\"0,,,\"/>\n"
                    + "  <tile id=\"216\" terrain=\",1,,\"/>\n"
                    + "  <tile id=\"217\" terrain=\"1,1,,\"/>\n"
                    + "  <tile id=\"218\" terrain=\"1,,,\"/>\n"
                    + "  <tile id=\"219\" terrain=\"1,,1,1\"/>\n"
                    + "  <tile id=\"227\" terrain=\"0,0,,\"/>\n"
                    + "  <tile id=\"228\" terrain=\"0,0,,\"/>\n"
                    + "  <tile id=\"229\" terrain=\"0,0,,\"/>\n"
                    + "  <tile id=\"260\">\n"
                    + "   <animation>\n"
                    + "    <frame tileid=\"260\" duration=\"200\"/>\n"
                    + "    <frame tileid=\"261\" duration=\"200\"/>\n"
                    + "    <frame tileid=\"262\" duration=\"200\"/>\n"
                    + "    <frame tileid=\"263\" duration=\"200\"/>\n"
                    + "   </animation>\n"
                    + "  </tile>\n"
                    + "  <tile id=\"265\">\n"
                    + "   <animation>\n"
                    + "    <frame tileid=\"265\" duration=\"200\"/>\n"
                    + "    <frame tileid=\"266\" duration=\"200\"/>\n"
                    + "    <frame tileid=\"267\" duration=\"200\"/>\n"
                    + "    <frame tileid=\"268\" duration=\"200\"/>\n"
                    + "   </animation>\n"
                    + "  </tile>\n"
                    + "  <tile id=\"285\">\n"
                    + "   <animation>\n"
                    + "    <frame tileid=\"285\" duration=\"200\"/>\n"
                    + "    <frame tileid=\"286\" duration=\"200\"/>\n"
                    + "    <frame tileid=\"287\" duration=\"200\"/>\n"
                    + "    <frame tileid=\"288\" duration=\"200\"/>\n"
                    + "   </animation>\n"
                    + "  </tile>\n"
                    + "  <tile id=\"290\">\n"
                    + "   <animation>\n"
                    + "    <frame tileid=\"290\" duration=\"200\"/>\n"
                    + "    <frame tileid=\"291\" duration=\"200\"/>\n"
                    + "    <frame tileid=\"292\" duration=\"200\"/>\n"
                    + "    <frame tileid=\"293\" duration=\"200\"/>\n"
                    + "   </animation>\n"
                    + "  </tile>\n"
                    + "  <tile id=\"388\">\n"
                    + "   <animation>\n"
                    + "    <frame tileid=\"388\" duration=\"300\"/>\n"
                    + "    <frame tileid=\"389\" duration=\"300\"/>\n"
                    + "   </animation>\n"
                    + "  </tile>\n"
                    + "  <tile id=\"390\">\n"
                    + "   <animation>\n"
                    + "    <frame tileid=\"390\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"391\" duration=\"500\"/>\n"
                    + "   </animation>\n"
                    + "  </tile>\n"
                    + "  <tile id=\"410\">\n"
                    + "   <animation>\n"
                    + "    <frame tileid=\"410\" duration=\"400\"/>\n"
                    + "    <frame tileid=\"411\" duration=\"400\"/>\n"
                    + "   </animation>\n"
                    + "  </tile>\n"
                    + "  <tile id=\"412\">\n"
                    + "   <animation>\n"
                    + "    <frame tileid=\"412\" duration=\"400\"/>\n"
                    + "    <frame tileid=\"413\" duration=\"400\"/>\n"
                    + "   </animation>\n"
                    + "  </tile>\n"
                    + "  <tile id=\"414\">\n"
                    + "   <animation>\n"
                    + "    <frame tileid=\"414\" duration=\"400\"/>\n"
                    + "    <frame tileid=\"415\" duration=\"400\"/>\n"
                    + "   </animation>\n"
                    + "  </tile>\n"
                    + "  <tile id=\"416\">\n"
                    + "   <animation>\n"
                    + "    <frame tileid=\"416\" duration=\"400\"/>\n"
                    + "    <frame tileid=\"417\" duration=\"400\"/>\n"
                    + "   </animation>\n"
                    + "  </tile>\n"
                    + "  <tile id=\"443\">\n"
                    + "   <animation>\n"
                    + "    <frame tileid=\"443\" duration=\"200\"/>\n"
                    + "    <frame tileid=\"444\" duration=\"200\"/>\n"
                    + "    <frame tileid=\"445\" duration=\"200\"/>\n"
                    + "    <frame tileid=\"446\" duration=\"200\"/>\n"
                    + "   </animation>\n"
                    + "  </tile>\n"
                    + " </tileset>\n"
                    + " <layer name=\"water\" width=\"" + this.mapWidth + "\" height=\"" + this.mapHeight + "\">\n"
                    + "  <data encoding=\"csv\">\n"
                    + "   " + water.toString()
                    + "  </data>\n"
                    + " </layer>\n"
                    + " <layer name=\"grass\" width=\"" + this.mapWidth + "\" height=\"" + this.mapHeight + "\">\n"
                    + "  <data encoding=\"csv\">\n"
                    + "   " + grass.toString()
                    + "  </data>\n"
                    + " </layer>\n"
                    + " <layer name=\"meadow\" width=\"" + this.mapWidth + "\" height=\"" + this.mapHeight + "\">\n"
                    + "  <data encoding=\"csv\">\n"
                    + "   " + meadow.toString()
                    + "  </data>\n"
                    + " </layer>\n"
                    + " <layer name=\"forest\" width=\"" + this.mapWidth + "\" height=\"" + this.mapHeight + "\">\n"
                    + "  <data encoding=\"csv\">\n"
                    + "   " + forest.toString()
                    + "  </data>\n"
                    + " </layer>\n"
                    + " <layer name=\"mountains\" width=\"" + this.mapWidth + "\" height=\"" + this.mapHeight + "\">\n"
                    + "  <data encoding=\"csv\">\n"
                    + "   " + mountains.toString()
                    + "  </data>\n"
                    + " </layer>\n"
                    + " <layer name=\"props\" width=\"" + this.mapWidth + "\" height=\"" + this.mapHeight + "\">\n"
                    + "  <data encoding=\"csv\">\n"
                    + "   " + props.toString()
                    + "  </data>\n"
                    + " </layer>\n"
                    + "<objectgroup name=\"portals\" width=\"" + this.mapWidth + "\" height=\"" + this.mapHeight + "\">\n" + this.portalString.toString() + "\n</objectgroup>\n"
                    + "<objectgroup name=\"moongates\" width=\"" + this.mapWidth + "\" height=\"" + this.mapHeight + "\">\n" + this.moongateString.toString() + "\n</objectgroup>\n"
                    + "</map>";

            return template;

        }
    }
}
