package test;

import objects.BaseMap;
import objects.MapSet;
import objects.Person;
import objects.TileSet;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class UltMapTmxConvert {

    private static final int TILE_PX = 16;

    private static final File TILESET_XML = new File("src/main/resources/assets/xml/tileset-base.xml");
    private static final File MAPS_XML = new File("src/main/resources/assets/xml/maps.xml");
    private static final File DATA_DIR = new File("src/main/resources/assets/data");
    private static final File TMX_DIR = new File("tmx");
    private static final String TILE_PNG_SOURCE_IN_TMX = "ultima-ega.png";

    public static void main(String[] args) throws Exception {
        new UltMapTmxConvert().run();
        System.out.println("DONE");
    }

    public void run() throws Exception {

        JAXBContext jaxbContext = JAXBContext.newInstance(TileSet.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        TileSet ts = (TileSet) jaxbUnmarshaller.unmarshal(TILESET_XML);
        ts.setMaps();

        jaxbContext = JAXBContext.newInstance(MapSet.class);
        jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        MapSet ms = (MapSet) jaxbUnmarshaller.unmarshal(MAPS_XML);
        ms.init(ts);

        int imgW = 256;
        int imgH = 256;

        if (!TMX_DIR.exists() && !TMX_DIR.mkdirs()) {
            throw new IllegalStateException("Could not create output directory: " + TMX_DIR.getAbsolutePath());
        }

        for (BaseMap map : ms.getMaps()) {
            if (map.getFname() == null || !map.getFname().endsWith("ult")) {
                continue;
            }

            File ultFile = new File(DATA_DIR, map.getFname());
            byte[] bytes;
            try (FileInputStream is = new FileInputStream(ultFile)) {
                bytes = IOUtils.toByteArray(is);
            }

            int expected = map.getWidth() * map.getHeight();
            if (bytes.length < expected) {
                throw new IllegalStateException(
                        "Map file too short: " + ultFile.getName() + " bytes=" + bytes.length + " expected>=" + expected
                );
            }

            String mapCsv = toCsvFromUltBytes(bytes, map.getWidth(), map.getHeight());

            List<Person> people = map.getCity() != null ? map.getCity().getPeople() : null;

            for (Person p : people) {
                int index = p.getTileIndex();
                if (index % 2 != 0) {  // if odd
                    p.setTileIndex(index - 1);
                }
            }

            String personsObjectXml = toPersonsObjectGroupXml(people);

            String tmx = TmxFormatter.format(
                    map.getFname(),
                    TILE_PNG_SOURCE_IN_TMX,
                    imgW, imgH,
                    map.getWidth(), map.getHeight(),
                    TILE_PX, TILE_PX,
                    mapCsv,
                    personsObjectXml
            );

            String name = (map.getCity() != null && map.getCity().getName() != null)
                    ? map.getCity().getName().replace(" ", "")
                    : "Unknown";

            File out = new File(TMX_DIR, String.format("%s.tmx", name.toUpperCase()));
            FileUtils.writeStringToFile(out, tmx, StandardCharsets.UTF_8);

            System.out.printf("Wrote: %s%n", out.getPath());
        }
    }

    private static String toCsvFromUltBytes(byte[] bytes, int width, int height) {
        StringBuilder sb = new StringBuilder(width * height * 3);
        int pos = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = bytes[pos++] & 0xFF;
                int gid = index + 1;  // firstgid=1

                sb.append(gid);

                boolean lastCell = (x == width - 1) && (y == height - 1);
                if (!lastCell) {
                    sb.append(',');
                }
            }
            if (y < height - 1) {
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    private static String toPersonsObjectGroupXml(List<Person> persons) {
        if (persons == null) {
            return "";
        }
        StringBuilder personsString = new StringBuilder();
        for (Person p : persons) {
            if (p == null) {
                continue;
            }
            personsString.append(p.toTMX16String());
        }
        return personsString.toString();
    }

    private static final class TmxFormatter {

        static String format(
                String tilesetName,
                String imageSource,
                int imageWidth,
                int imageHeight,
                int mapWidth,
                int mapHeight,
                int tileWidth,
                int tileHeight,
                String mapCsv,
                String personsObjectXml
        ) {
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<map version=\"1.0\" orientation=\"orthogonal\" width=\"" + mapWidth + "\" height=\"" + mapHeight
                    + "\" tilewidth=\"" + tileWidth + "\" tileheight=\"" + tileHeight + "\" backgroundcolor=\"#000000\">\n"
                    + " <properties>\n"
                    + "  <property name=\"startX\" value=\"1\"/>\n"
                    + "  <property name=\"startY\" value=\"15\"/>\n"
                    + " </properties>\n"
                    + "  <tileset firstgid=\"1\" name=\"" + escapeXml(tilesetName) + "\" tilewidth=\"" + tileWidth
                    + "\" tileheight=\"" + tileHeight + "\">\n"
                    + "    <image source=\"" + escapeXml(imageSource) + "\" width=\"" + imageWidth + "\" height=\"" + imageHeight + "\"/>\n"
                    + ANIMS
                    + "  </tileset>\n"
                    + "  <layer name=\"map\" width=\"" + mapWidth + "\" height=\"" + mapHeight + "\">\n"
                    + "    <data encoding=\"csv\">\n" + mapCsv + "\n</data>\n"
                    + "  </layer>\n"
                    + "  <objectgroup name=\"people\" width=\"" + mapWidth + "\" height=\"" + mapHeight + "\">\n"
                    + personsObjectXml + "\n"
                    + "  </objectgroup>\n"
                    + "</map>\n";
        }

        private static String escapeXml(String s) {
            if (s == null) {
                return "";
            }
            return s.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;");
        }
    }

    private static final String ANIMS
            = "        <tile id=\"0\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"256\" duration=\"300\"/>\n"
            + "                <frame tileid=\"257\" duration=\"300\"/>\n"
            + "                <frame tileid=\"258\" duration=\"300\"/>\n"
            + "                <frame tileid=\"259\" duration=\"300\"/>\n"
            + "                <frame tileid=\"260\" duration=\"300\"/>\n"
            + "                <frame tileid=\"261\" duration=\"300\"/>\n"
            + "                <frame tileid=\"262\" duration=\"300\"/>\n"
            + "                <frame tileid=\"263\" duration=\"300\"/>\n"
            + "                <frame tileid=\"264\" duration=\"300\"/>\n"
            + "                <frame tileid=\"265\" duration=\"300\"/>\n"
            + "                <frame tileid=\"266\" duration=\"300\"/>\n"
            + "                <frame tileid=\"267\" duration=\"300\"/>\n"
            + "                <frame tileid=\"268\" duration=\"300\"/>\n"
            + "                <frame tileid=\"269\" duration=\"300\"/>\n"
            + "                <frame tileid=\"270\" duration=\"300\"/>\n"
            + "                <frame tileid=\"271\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"1\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"272\" duration=\"300\"/>\n"
            + "                <frame tileid=\"273\" duration=\"300\"/>\n"
            + "                <frame tileid=\"274\" duration=\"300\"/>\n"
            + "                <frame tileid=\"275\" duration=\"300\"/>\n"
            + "                <frame tileid=\"276\" duration=\"300\"/>\n"
            + "                <frame tileid=\"277\" duration=\"300\"/>\n"
            + "                <frame tileid=\"278\" duration=\"300\"/>\n"
            + "                <frame tileid=\"279\" duration=\"300\"/>\n"
            + "                <frame tileid=\"280\" duration=\"300\"/>\n"
            + "                <frame tileid=\"281\" duration=\"300\"/>\n"
            + "                <frame tileid=\"282\" duration=\"300\"/>\n"
            + "                <frame tileid=\"283\" duration=\"300\"/>\n"
            + "                <frame tileid=\"284\" duration=\"300\"/>\n"
            + "                <frame tileid=\"285\" duration=\"300\"/>\n"
            + "                <frame tileid=\"286\" duration=\"300\"/>\n"
            + "                <frame tileid=\"287\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"2\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"288\" duration=\"300\"/>\n"
            + "                <frame tileid=\"289\" duration=\"300\"/>\n"
            + "                <frame tileid=\"290\" duration=\"300\"/>\n"
            + "                <frame tileid=\"291\" duration=\"300\"/>\n"
            + "                <frame tileid=\"292\" duration=\"300\"/>\n"
            + "                <frame tileid=\"293\" duration=\"300\"/>\n"
            + "                <frame tileid=\"294\" duration=\"300\"/>\n"
            + "                <frame tileid=\"295\" duration=\"300\"/>\n"
            + "                <frame tileid=\"296\" duration=\"300\"/>\n"
            + "                <frame tileid=\"297\" duration=\"300\"/>\n"
            + "                <frame tileid=\"298\" duration=\"300\"/>\n"
            + "                <frame tileid=\"299\" duration=\"300\"/>\n"
            + "                <frame tileid=\"300\" duration=\"300\"/>\n"
            + "                <frame tileid=\"301\" duration=\"300\"/>\n"
            + "                <frame tileid=\"302\" duration=\"300\"/>\n"
            + "                <frame tileid=\"303\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"32\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"32\" duration=\"300\"/>\n"
            + "                <frame tileid=\"33\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"34\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"34\" duration=\"300\"/>\n"
            + "                <frame tileid=\"35\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"36\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"36\" duration=\"300\"/>\n"
            + "                <frame tileid=\"37\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"38\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"38\" duration=\"300\"/>\n"
            + "                <frame tileid=\"39\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"40\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"40\" duration=\"300\"/>\n"
            + "                <frame tileid=\"41\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"42\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"42\" duration=\"300\"/>\n"
            + "                <frame tileid=\"43\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"44\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"44\" duration=\"300\"/>\n"
            + "                <frame tileid=\"45\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"46\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"46\" duration=\"300\"/>\n"
            + "                <frame tileid=\"47\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"68\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"304\" duration=\"300\"/>\n"
            + "                <frame tileid=\"305\" duration=\"300\"/>\n"
            + "                <frame tileid=\"306\" duration=\"300\"/>\n"
            + "                <frame tileid=\"307\" duration=\"300\"/>\n"
            + "                <frame tileid=\"308\" duration=\"300\"/>\n"
            + "                <frame tileid=\"309\" duration=\"300\"/>\n"
            + "                <frame tileid=\"310\" duration=\"300\"/>\n"
            + "                <frame tileid=\"311\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"69\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"312\" duration=\"300\"/>\n"
            + "                <frame tileid=\"313\" duration=\"300\"/>\n"
            + "                <frame tileid=\"314\" duration=\"300\"/>\n"
            + "                <frame tileid=\"315\" duration=\"300\"/>\n"
            + "                <frame tileid=\"316\" duration=\"300\"/>\n"
            + "                <frame tileid=\"317\" duration=\"300\"/>\n"
            + "                <frame tileid=\"318\" duration=\"300\"/>\n"
            + "                <frame tileid=\"319\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"70\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"320\" duration=\"300\"/>\n"
            + "                <frame tileid=\"321\" duration=\"300\"/>\n"
            + "                <frame tileid=\"322\" duration=\"300\"/>\n"
            + "                <frame tileid=\"323\" duration=\"300\"/>\n"
            + "                <frame tileid=\"324\" duration=\"300\"/>\n"
            + "                <frame tileid=\"325\" duration=\"300\"/>\n"
            + "                <frame tileid=\"326\" duration=\"300\"/>\n"
            + "                <frame tileid=\"327\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"71\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"328\" duration=\"300\"/>\n"
            + "                <frame tileid=\"329\" duration=\"300\"/>\n"
            + "                <frame tileid=\"330\" duration=\"300\"/>\n"
            + "                <frame tileid=\"331\" duration=\"300\"/>\n"
            + "                <frame tileid=\"332\" duration=\"300\"/>\n"
            + "                <frame tileid=\"333\" duration=\"300\"/>\n"
            + "                <frame tileid=\"334\" duration=\"300\"/>\n"
            + "                <frame tileid=\"335\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"76\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"336\" duration=\"300\"/>\n"
            + "                <frame tileid=\"337\" duration=\"300\"/>\n"
            + "                <frame tileid=\"338\" duration=\"300\"/>\n"
            + "                <frame tileid=\"339\" duration=\"300\"/>\n"
            + "                <frame tileid=\"340\" duration=\"300\"/>\n"
            + "                <frame tileid=\"341\" duration=\"300\"/>\n"
            + "                <frame tileid=\"342\" duration=\"300\"/>\n"
            + "                <frame tileid=\"343\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"80\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"80\" duration=\"300\"/>\n"
            + "                <frame tileid=\"81\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"82\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"82\" duration=\"300\"/>\n"
            + "                <frame tileid=\"83\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"84\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"84\" duration=\"300\"/>\n"
            + "                <frame tileid=\"85\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"86\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"86\" duration=\"300\"/>\n"
            + "                <frame tileid=\"87\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"88\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"88\" duration=\"300\"/>\n"
            + "                <frame tileid=\"89\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"90\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"90\" duration=\"300\"/>\n"
            + "                <frame tileid=\"91\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"92\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"92\" duration=\"300\"/>\n"
            + "                <frame tileid=\"93\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"94\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"94\" duration=\"300\"/>\n"
            + "                <frame tileid=\"95\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"132\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"132\" duration=\"300\"/>\n"
            + "                <frame tileid=\"133\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"134\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"134\" duration=\"300\"/>\n"
            + "                <frame tileid=\"135\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"136\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"136\" duration=\"300\"/>\n"
            + "                <frame tileid=\"137\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"138\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"138\" duration=\"300\"/>\n"
            + "                <frame tileid=\"139\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"140\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"140\" duration=\"300\"/>\n"
            + "                <frame tileid=\"141\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"142\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"142\" duration=\"300\"/>\n"
            + "                <frame tileid=\"143\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"144\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"144\" duration=\"300\"/>\n"
            + "                <frame tileid=\"145\" duration=\"300\"/>\n"
            + "                <frame tileid=\"146\" duration=\"300\"/>\n"
            + "                <frame tileid=\"147\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"148\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"148\" duration=\"300\"/>\n"
            + "                <frame tileid=\"149\" duration=\"300\"/>\n"
            + "                <frame tileid=\"150\" duration=\"300\"/>\n"
            + "                <frame tileid=\"151\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"152\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"152\" duration=\"300\"/>\n"
            + "                <frame tileid=\"153\" duration=\"300\"/>\n"
            + "                <frame tileid=\"154\" duration=\"300\"/>\n"
            + "                <frame tileid=\"155\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"156\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"156\" duration=\"300\"/>\n"
            + "                <frame tileid=\"157\" duration=\"300\"/>\n"
            + "                <frame tileid=\"158\" duration=\"300\"/>\n"
            + "                <frame tileid=\"159\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"160\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"160\" duration=\"300\"/>\n"
            + "                <frame tileid=\"161\" duration=\"300\"/>\n"
            + "                <frame tileid=\"162\" duration=\"300\"/>\n"
            + "                <frame tileid=\"163\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"164\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"164\" duration=\"300\"/>\n"
            + "                <frame tileid=\"165\" duration=\"300\"/>\n"
            + "                <frame tileid=\"166\" duration=\"300\"/>\n"
            + "                <frame tileid=\"167\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"168\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"168\" duration=\"300\"/>\n"
            + "                <frame tileid=\"169\" duration=\"300\"/>\n"
            + "                <frame tileid=\"170\" duration=\"300\"/>\n"
            + "                <frame tileid=\"171\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"172\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"172\" duration=\"300\"/>\n"
            + "                <frame tileid=\"173\" duration=\"300\"/>\n"
            + "                <frame tileid=\"174\" duration=\"300\"/>\n"
            + "                <frame tileid=\"175\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"176\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"176\" duration=\"300\"/>\n"
            + "                <frame tileid=\"177\" duration=\"300\"/>\n"
            + "                <frame tileid=\"178\" duration=\"300\"/>\n"
            + "                <frame tileid=\"179\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"180\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"180\" duration=\"300\"/>\n"
            + "                <frame tileid=\"181\" duration=\"300\"/>\n"
            + "                <frame tileid=\"182\" duration=\"300\"/>\n"
            + "                <frame tileid=\"183\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"184\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"184\" duration=\"300\"/>\n"
            + "                <frame tileid=\"185\" duration=\"300\"/>\n"
            + "                <frame tileid=\"186\" duration=\"300\"/>\n"
            + "                <frame tileid=\"187\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"188\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"188\" duration=\"300\"/>\n"
            + "                <frame tileid=\"189\" duration=\"300\"/>\n"
            + "                <frame tileid=\"190\" duration=\"300\"/>\n"
            + "                <frame tileid=\"191\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"192\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"192\" duration=\"300\"/>\n"
            + "                <frame tileid=\"193\" duration=\"300\"/>\n"
            + "                <frame tileid=\"194\" duration=\"300\"/>\n"
            + "                <frame tileid=\"195\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"196\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"196\" duration=\"300\"/>\n"
            + "                <frame tileid=\"197\" duration=\"300\"/>\n"
            + "                <frame tileid=\"198\" duration=\"300\"/>\n"
            + "                <frame tileid=\"199\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"200\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"200\" duration=\"300\"/>\n"
            + "                <frame tileid=\"201\" duration=\"300\"/>\n"
            + "                <frame tileid=\"202\" duration=\"300\"/>\n"
            + "                <frame tileid=\"203\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"204\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"204\" duration=\"300\"/>\n"
            + "                <frame tileid=\"205\" duration=\"300\"/>\n"
            + "                <frame tileid=\"206\" duration=\"300\"/>\n"
            + "                <frame tileid=\"207\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"208\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"208\" duration=\"300\"/>\n"
            + "                <frame tileid=\"209\" duration=\"300\"/>\n"
            + "                <frame tileid=\"210\" duration=\"300\"/>\n"
            + "                <frame tileid=\"211\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"212\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"212\" duration=\"300\"/>\n"
            + "                <frame tileid=\"213\" duration=\"300\"/>\n"
            + "                <frame tileid=\"214\" duration=\"300\"/>\n"
            + "                <frame tileid=\"215\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"216\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"216\" duration=\"300\"/>\n"
            + "                <frame tileid=\"217\" duration=\"300\"/>\n"
            + "                <frame tileid=\"218\" duration=\"300\"/>\n"
            + "                <frame tileid=\"219\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"220\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"220\" duration=\"300\"/>\n"
            + "                <frame tileid=\"221\" duration=\"300\"/>\n"
            + "                <frame tileid=\"222\" duration=\"300\"/>\n"
            + "                <frame tileid=\"223\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"224\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"224\" duration=\"300\"/>\n"
            + "                <frame tileid=\"225\" duration=\"300\"/>\n"
            + "                <frame tileid=\"226\" duration=\"300\"/>\n"
            + "                <frame tileid=\"227\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"228\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"228\" duration=\"300\"/>\n"
            + "                <frame tileid=\"229\" duration=\"300\"/>\n"
            + "                <frame tileid=\"230\" duration=\"300\"/>\n"
            + "                <frame tileid=\"231\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"232\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"232\" duration=\"300\"/>\n"
            + "                <frame tileid=\"233\" duration=\"300\"/>\n"
            + "                <frame tileid=\"234\" duration=\"300\"/>\n"
            + "                <frame tileid=\"235\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"236\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"236\" duration=\"300\"/>\n"
            + "                <frame tileid=\"237\" duration=\"300\"/>\n"
            + "                <frame tileid=\"238\" duration=\"300\"/>\n"
            + "                <frame tileid=\"239\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"240\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"240\" duration=\"300\"/>\n"
            + "                <frame tileid=\"241\" duration=\"300\"/>\n"
            + "                <frame tileid=\"242\" duration=\"300\"/>\n"
            + "                <frame tileid=\"243\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"244\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"244\" duration=\"300\"/>\n"
            + "                <frame tileid=\"245\" duration=\"300\"/>\n"
            + "                <frame tileid=\"246\" duration=\"300\"/>\n"
            + "                <frame tileid=\"247\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"248\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"248\" duration=\"300\"/>\n"
            + "                <frame tileid=\"249\" duration=\"300\"/>\n"
            + "                <frame tileid=\"250\" duration=\"300\"/>\n"
            + "                <frame tileid=\"251\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n"
            + "        <tile id=\"252\">\n"
            + "            <animation>\n"
            + "                <frame tileid=\"252\" duration=\"300\"/>\n"
            + "                <frame tileid=\"253\" duration=\"300\"/>\n"
            + "                <frame tileid=\"254\" duration=\"300\"/>\n"
            + "                <frame tileid=\"255\" duration=\"300\"/>\n"
            + "            </animation>\n"
            + "        </tile>\n";
}
