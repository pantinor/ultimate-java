package test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import objects.Armor;
import objects.ArmorSet;
import objects.BaseMap;
import objects.Conversation;
import objects.Creature;
import objects.CreatureSet;
import objects.MapSet;
import objects.Party;
import objects.Person;
import objects.SaveGame;
import objects.Tile;
import objects.TileSet;
import objects.Weapon;
import objects.WeaponSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import ultima.Constants;
import ultima.Constants.ClassType;
import ultima.Constants.CreatureType;
import ultima.Constants.Direction;
import ultima.Constants.DungeonTile;
import ultima.Constants.HeadingDirection;
import ultima.Constants.Item;
import ultima.Constants.KarmaAction;
import ultima.Constants.MapBorderBehavior;
import ultima.Constants.Maps;
import ultima.Constants.TileAttrib;
import ultima.Constants.TileRule;
import ultima.Constants.Virtue;
import ultima.StartScreen;
import util.SpreadFOV;
import vendor.VendorClass;
import vendor.VendorClassSet;

import com.badlogic.gdx.math.Vector3;
import com.google.common.io.LittleEndianDataInputStream;
import javax.xml.bind.Marshaller;
import objects.JournalEntries;
import objects.JournalEntry;
import org.testng.annotations.Test;
import ultima.Constants.MapType;
import static ultima.DungeonScreen.DUNGEON_MAP;
import util.Utils;
import util.XORShiftRandom;

public class TestJaxb {

    //@Test
    public void testScript() throws Exception {
        File file = new File("target/classes/xml/vendor.xml");
        JAXBContext jaxbContext = JAXBContext.newInstance(VendorClassSet.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        VendorClassSet ss = (VendorClassSet) jaxbUnmarshaller.unmarshal(file);
        for (VendorClass s : ss.getVendorClasses()) {
            System.out.println(s);
        }
    }

    //@Test
    public void testTileSetBase() throws Exception {
        File file = new File("target/classes/xml/tileset-base.xml");
        JAXBContext jaxbContext = JAXBContext.newInstance(TileSet.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        TileSet ts = (TileSet) jaxbUnmarshaller.unmarshal(file);
        for (Tile t : ts.getTiles()) {
            System.out.println(t);
        }
    }

    //@Test
    public void testTileSetDungeon() throws Exception {
        File file = new File("target/classes/xml/tileset-dungeon.xml");
        JAXBContext jaxbContext = JAXBContext.newInstance(TileSet.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        TileSet ts = (TileSet) jaxbUnmarshaller.unmarshal(file);
        for (Tile t : ts.getTiles()) {
            System.out.println(t);
        }
    }

    //@Test
    public void testJournal() throws Exception {
        File file = new File("journal.save.test");
        JAXBContext jaxbContext = JAXBContext.newInstance(JournalEntries.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        JournalEntries entries = new JournalEntries();
        entries.add(new JournalEntry("john", "yew", "whatever"));
        marshaller.marshal(entries, file);

        entries.add(new JournalEntry("fred", "minoc", "whatever"));
        marshaller.marshal(entries, file);

        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        JournalEntries entries2 = (JournalEntries) jaxbUnmarshaller.unmarshal(file);
        for (JournalEntry t : entries2.getEntries()) {
            System.out.println(t);
        }
    }

    //@Test
    public void testMaps() throws Exception {

        TileSet baseTileSet = (TileSet) Utils.loadXml("tileset-base.xml", TileSet.class);
        baseTileSet.setMaps();

        MapSet maps = (MapSet) Utils.loadXml("maps.xml", MapSet.class);
        maps.init(baseTileSet);

        for (BaseMap map : maps.getMaps()) {

            if (map.getCity() == null || map.getCity().getTlk_fname() == null) {
                continue;
            }

            String fname = "D:\\xu4\\ULTIMA4\\" + map.getCity().getTlk_fname();
            System.out.println("D:\\xu4\\tools\\tlkconv.exe --toxml " + fname + " " + "D:\\work\\ultima-java\\target\\" + map.getCity().getTlk_fname() + ".xml");

        }

    }

    //@Test
    public void testWeapons() throws Exception {
        File file = new File("target/classes/xml/weapons.xml");
        JAXBContext jaxbContext = JAXBContext.newInstance(WeaponSet.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        WeaponSet ts = (WeaponSet) jaxbUnmarshaller.unmarshal(file);
        for (Weapon t : ts.getWeapons()) {
            System.out.println(t);
        }
    }

    //@Test
    public void testArmors() throws Exception {
        File file = new File("target/classes/xml/armors.xml");
        JAXBContext jaxbContext = JAXBContext.newInstance(ArmorSet.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        ArmorSet ts = (ArmorSet) jaxbUnmarshaller.unmarshal(file);
        for (Armor t : ts.getArmors()) {
            System.out.println(t);
        }
    }

    //@Test
    public void testCreatures() throws Exception {
        File file = new File("target/classes/xml/creatures.xml");
        JAXBContext jaxbContext = JAXBContext.newInstance(CreatureSet.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        CreatureSet ts = (CreatureSet) jaxbUnmarshaller.unmarshal(file);
        for (Creature t : ts.getCreatures()) {
            System.out.println(t);
        }
    }

    //@Test
    public void makeXml2() throws Exception {

        //String t = FileUtils.readFileToString(new File("src/main/resources/xml/tileset-base.xml"));
        String t = FileUtils.readFileToString(new File("beasts.txt"));

        String r = t.replaceAll("index=\"([0-9]+/*\\.*[0-9]*)\"", "|");

        StringTokenizer st = new StringTokenizer(r, "|");
        StringBuffer sb = new StringBuffer();
        int count = 128;
        while (st.hasMoreTokens()) {
            sb.append(st.nextToken());
            sb.append("index=\"" + count + "\"");
            count++;
        }

        System.out.println(sb.toString());

    }

    //@Test
    public void makeDialogXml() throws Exception {
        File dir = new File("src/main/resources/xml");
        File[] tlkxmlFiles = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith("tlk.xml");
            }
        });

        for (File f : tlkxmlFiles) {
            String t = FileUtils.readFileToString(f);
            Utils.getDialogs(f.getName());
            FileUtils.writeStringToFile(f, t);
        }

    }

    //@Test
    public void renametolowercase() throws Exception {
        File dir = new File("assets/data/test");
        File[] files = dir.listFiles();

        for (File f : files) {
            f.renameTo(new File("assets/data/test/" + f.getName().toLowerCase()));
        }

    }

    @Test
    public void parseTlkFiles() throws Exception {

        TileSet baseTileSet = (TileSet) Utils.loadXml("tileset-base.xml", TileSet.class);
        baseTileSet.setMaps();

        MapSet maps = (MapSet) Utils.loadXml("maps.xml", MapSet.class);

        for (BaseMap map : maps.getMaps()) {

            if (map.getCity() == null || map.getCity().getTlk_fname() == null) {
                continue;
            }

            List<Person> people;
            try {
                people = Utils.getPeople(map.getFname(), Maps.get(map.getId()), null);
            } catch (Exception e) {
                continue;
            }

            List<Conversation> cons = Utils.getDialogs(map.getCity().getTlk_fname());

            if (people == null) {
                continue;
            }

            for (Person p : people) {
                if (p != null) {
                    for (Conversation c : cons) {
                        if (c.getIndex() == p.getDialogId()) {
                            p.setConversation(c);
                        }
                    }
                }
            }

            for (Person p : people) {
                //System.out.println(p);
            }

            for (Conversation c : cons) {
                System.out.println(c.toXMLString2(Maps.get(map.getId())));
            }

            for (Conversation c : cons) {
                Person per = null;
                for (Person p : people) {
                    if (c.getIndex() == p.getDialogId()) {
                        per = p;
                    }
                }
                if (per == null) {
                    //System.out.println("Extra Dialog: " + c);
                }
            }
        }

        //System.out.printf("Latitude [%s' %s]", (char) ((int) 54 / 16 + 'A'), (char) ((int) 54 % 16 + 'A'));
        //System.out.printf(" Longitude [%s' %s]\n", (char) ((int) 182 / 16 + 'A'), (char) ((int) 182 % 16 + 'A'));
    }

    //@Test
    public void testDirectionMask() throws Exception {

        Direction dir = Direction.WEST;

        int mask = Direction.addToMask(Direction.NORTH, 0);
        assert (!Direction.isDirInMask(dir, mask));

        mask = Direction.addToMask(Direction.EAST, mask);
        assert (!Direction.isDirInMask(dir, mask));

        mask = Direction.addToMask(Direction.WEST, mask);
        assert (Direction.isDirInMask(dir, mask));

        mask = Direction.removeFromMask(mask, Direction.WEST);
        assert (!Direction.isDirInMask(dir, mask));

        Direction dir2 = Direction.getRandomValidDirection(mask);
        assert (true);

    }

    //@Test
    public void testReadSaveGame() throws Exception {

        InputStream is = new FileInputStream(Constants.PARTY_SAV_BASE_FILENAME);
        LittleEndianDataInputStream dis = new LittleEndianDataInputStream(is);

        SaveGame sg = new SaveGame();
        sg.read(dis);

//		SaveGame.SaveGamePlayerRecord avatar = sg.new SaveGamePlayerRecord();
//		avatar.name = "paul";
//		avatar.hp = 199;
//		
//		sg.food = 30000;
//		sg.gold = 200;
//		sg.reagents[Reagent.GINSENG.ordinal()] = 3;
//		sg.reagents[Reagent.GARLIC.ordinal()] = 4;
//		sg.reagents[Reagent.NIGHTSHADE.ordinal()] = 9;
//		sg.reagents[Reagent.MANDRAKE.ordinal()] = 6;
//		sg.torches = 2;
//		
//		sg.players[0] = avatar;
//		
//		sg.write(Constants.PARTY_SAV_BASE_FILENAME);
        Party p = new Party(sg);

        //for (int i=0;i<8;i++) 
        //System.err.println(Virtue.get(i) + " " + sg.karma[i]);
        //System.err.println("---------------");
        p.adjustKarma(KarmaAction.ATTACKED_GOOD);

        //for (int i=0;i<8;i++) 
        //System.err.println(Virtue.get(i) + " " + sg.karma[i]);
        for (int i = 0; i < 8; i++) {
            Virtue v = Constants.Virtue.get(i);
            String st = ((sg.stones & (1 << i)) > 0 ? "+STONE" : "");
            String ru = ((sg.runes & (1 << i)) > 0 ? "+RUNE" : "");
            //System.err.println(v + " " + st + " " + ru);
        }

        System.err.println("---------------");
        sg.runes |= Virtue.HUMILITY.getLoc();

        for (int i = 0; i < 8; i++) {
            Virtue v = Constants.Virtue.get(i);
            String st = ((sg.stones & (1 << i)) > 0 ? "+STONE" : "");
            String ru = ((sg.runes & (1 << i)) > 0 ? "+RUNE" : "");
            //System.err.println(v + " " + st + " " + ru);
        }

        sg.items |= Item.BELL.getLoc();
        for (Item item : Constants.Item.values()) {
            //System.err.println((sg.items & (1 << item.ordinal())) > 0 ? item.getDesc() : "") ;
        }

        sg.items |= Item.PARCH.getLoc();
        for (Item item : Constants.Item.values()) {
            System.err.println((sg.items & item.getLoc()) > 0 ? item.getDesc() : "");
        }

        sg.lastrage = 1;

        sg.write("test.sav");

    }

    //@Test
    public void testCreateParty() throws Exception {

        SaveGame sg = new SaveGame();
        sg.read("D:\\ultima\\ULTIMA4\\" + Constants.PARTY_SAV_BASE_FILENAME);

        Party p = new Party(sg);

        assert (true);

    }

    //@Test
    public void testFOV() throws Exception {

        int dim = 25;
        int startx = 13;
        int starty = 13;

        float[][] vt = new float[dim][dim];
        for (int x = 0; x < dim; x++) {
            for (int y = 0; y < dim; y++) {
                //vt[x][y] = new Tile();
                //vt[x][y].setOpaque(true);
            }
        }

        vt[1][1] = 1f;
        vt[1][6] = 1f;
        vt[6][6] = 1f;
        vt[6][1] = 1f;
        SpreadFOV fov = new SpreadFOV(dim, dim, true);

        float[][] los = fov.calculateFOV(vt, startx, starty, 10);

        for (int y = 0; y < dim; y++) {
            for (int x = 0; x < dim; x++) {
                if (startx == x && starty == y) {
                    //System.out.print("_");
                    System.out.print(los[x][y] + "|");
                } else {
                    //System.out.print(los[x][y] <= 0?"X":" ");
                    System.out.print(los[x][y] + "|");
                }
            }
            System.out.println("");
        }

    }

    //@Test
    public void testMapShadows() throws Exception {

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

        BaseMap m = Maps.BRITAIN.getMap();

        int startx = 2;
        int starty = 4;

        long t = System.currentTimeMillis();

        SpreadFOV fov = new SpreadFOV(32, 32, true);

        float[][] lightMap = fov.calculateFOV(m.getShadownMap(), startx, starty, 20);

        for (int y = 0; y < 32; y++) {
            for (int x = 0; x < 32; x++) {
//				Tile tile = m.getTile(x, y);
//				if (tile.isOpaque()) {
//					//System.out.print("W");
//					System.out.print(lightMap[x][y] <= 0?"D":"W");
//				} else 

                if (startx == x && starty == y) {
                    System.out.print("_");
                } else {
                    //System.out.print(lightMap[x][y] <= 0?"X":" ");
                    System.out.print(lightMap[x][y]);
                }
            }
            System.out.println("");

        }

        System.out.println("testLOS2 time: " + (System.currentTimeMillis() - t));

    }

    //@Test
    public void testReadTitleExe() throws Exception {
        InputStream is = TestJaxb.class.getResourceAsStream("/data/title.exe");
        byte[] bytes = IOUtils.toByteArray(is);

//		int stringIndex = 0;
//		int pos = 28;
//		CharBuffer bb = BufferUtils.createCharBuffer(288);
//			
//		while (stringIndex < 28) {
//			if (bytes[pos] == 0x0) {
//				bb.flip();
//				System.out.println(new String(bb.toString().replace("\n", " ")));
//				stringIndex++;
//				bb.clear();
//			} else {
//				bb.put((char)bytes[pos]);
//			}
//			pos ++;
//		}
        System.out.println("");

    }

    //@Test
    public void testTileAttrib() throws Exception {

        for (TileAttrib r : TileAttrib.values()) {
            System.out.println(Integer.toBinaryString(r.getVal()));
        }
        for (TileRule r : TileRule.values()) {
            System.out.println(Integer.toBinaryString(r.getAttribs()));
        }

        assert (TileRule.water.has(TileAttrib.unwalkable));
        assert (!TileRule.grass.has(TileAttrib.unwalkable));

    }

    //@Test
    public void testMovement() throws Exception {

        int dist = Utils.movementDistance(MapBorderBehavior.wrap, 8, 8, 2, 1, 4, 7);
        System.out.println(dist);

        dist = Utils.distance(MapBorderBehavior.wrap, 8, 8, 2, 1, 4, 7);
        System.out.println(dist);

    }

    //@Test
    public void testNibbles() throws Exception {
        byte[] data = new byte[4];
        data[1] = (byte) 0x85;
        int x = (data[1] >> 4) & 0x0f;
        int y = data[1] & 0x0f;

        int z = x;

        Random rand = new XORShiftRandom();
        for (int j = 0; j < 10; j++) {
            int v = 99 + (rand.nextInt(256) & 119);
            int f = v;
        }

    }

    //@Test
    public void testRandDung() throws Exception {
        Random rand = new Random();
        int currentLevel = 7;

        for (int i = 0; i < 20; i++) {

            int total = 0;
            for (CreatureType ct : CreatureType.values()) {
                total += ct.getSpawnLevel() <= currentLevel ? ct.getSpawnWeight() : 0;
            }

            int thresh = rand.nextInt(total);
            CreatureType monster = null;

            for (CreatureType ct : CreatureType.values()) {
                thresh -= ct.getSpawnLevel() <= currentLevel ? ct.getSpawnWeight() : 0;
                if (thresh < 0) {
                    monster = ct;
                    break;
                }
            }

            System.out.println(monster.toString());
        }
    }

    //@Test
    public void testDungSpawn() throws Exception {

        int dx = 0;
        int dy = 0;
        int tmp = 0;

        int currentX = 1;
        int currentY = 1;
        int DUNGEON_MAP = 8;
        Random rand = new Random();

        DungeonTile[][] dungeonTiles = new DungeonTile[8][8];
        for (int i = 0; i < DUNGEON_MAP; i++) {
            for (int j = 0; j < DUNGEON_MAP; j++) {
                dungeonTiles[i][j] = DungeonTile.NOTHING;
            }
        }

        for (int i = 0; i < 20; i++) {
            dx = 8;
            dy = rand.nextInt(8);

            if (rand.nextInt(2) > 0) {
                dx = -dx;
            }
            if (rand.nextInt(2) > 0) {
                dy = -dy;
            }
            if (rand.nextInt(2) > 0) {
                tmp = dx;
                dx = dy;
                dy = tmp;
            }

            dx = currentX + dx;
            dy = currentY + dy;

            if (dx < 0) {
                dx = DUNGEON_MAP + dx;
            } else if (dx > DUNGEON_MAP - 1) {
                dx = dx - DUNGEON_MAP;
            }
            if (dy < 0) {
                dy = DUNGEON_MAP + dy;
            } else if (dy > DUNGEON_MAP - 1) {
                dy = dy - DUNGEON_MAP;
            }

            DungeonTile tile = dungeonTiles[dx][dy];
            if (tile.getCreatureWalkable()) {
                System.out.println("ok");
            } else {
                System.out.println("bad");
            }

        }

    }

    @Test
    public void testDungeonTiles() throws Exception {

        MapSet maps = (MapSet) Utils.loadXml("maps.xml", MapSet.class);

        for (BaseMap map : maps.getMaps()) {
            
            if (map.getType() != MapType.dungeon) {
                continue;
            }

            InputStream is = MapSet.class.getResourceAsStream("/assets/data/" + map.getFname());
            byte[] bytes = IOUtils.toByteArray(is);
            System.out.println(map.getFname());

            int pos = 0;

            for (int i = 0; i < DUNGEON_MAP; i++) {
                System.out.println("-------------  " + i);
                for (int y = 0; y < DUNGEON_MAP; y++) {
                    for (int x = 0; x < DUNGEON_MAP; x++) {
                        int index = bytes[pos] & 0xff;
                        pos++;
                        DungeonTile tile = DungeonTile.getTileByValue(index);
                        System.out.print("|" + Integer.toHexString(index) + ":" + tile + "|\t");
                    }
                    System.out.println();
                }
            }
        }

    }

    //@Test
    public void findStartsForDungeons() throws Exception {

        TileSet baseTileSet = (TileSet) Utils.loadXml("tileset-base.xml", TileSet.class);
        baseTileSet.setMaps();

        MapSet maps = (MapSet) Utils.loadXml("maps.xml", MapSet.class);
        maps.init(baseTileSet);

        for (BaseMap map : maps.getMaps()) {
            if (map.getType() != MapType.dungeon) {
                continue;
            }
            InputStream is = MapSet.class.getResourceAsStream("assets/data/" + map.getFname());
            byte[] bytes = IOUtils.toByteArray(is);

            int pos = 0;
            int i = 0;
            for (int y = 0; y < DUNGEON_MAP; y++) {
                for (int x = 0; x < DUNGEON_MAP; x++) {
                    int index = bytes[pos] & 0xff;
                    pos++;
                    DungeonTile tile = DungeonTile.getTileByValue(index);
                    if (tile == DungeonTile.LADDER_UP || tile == DungeonTile.LADDER_UP_DOWN) {
                        System.out.println(map.getFname() + " x=" + x + " y=" + y);
                    }
                }
            }
        }

    }

    //@Test
    public void testDirectionalsYDown() throws Exception {

        int fromX = 2;
        int fromY = 3;
        int toX = 2;
        int toY = 7;

        HeadingDirection d = HeadingDirection.getDirection(fromX - toX, fromY - toY);

        System.out.println(String.format("heading: %s from: %d,%d to: %d,%d", d.toString(), fromX, fromY, toX, toY));
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if (fromX == x && fromY == y) {
                    System.out.print("F");
                } else if (toX == x && toY == y) {
                    System.out.print("T");
                } else {
                    System.out.print("*");
                }
            }
            System.out.println("");
        }

    }

    //@Test
    public void testStartQuestions() throws Exception {

        Map<ClassType, Integer> dist = new HashMap<>();
        for (ClassType ct : ClassType.values()) {
            dist.put(ct, 0);
        }

        for (int z = 0; z < 100; z++) {
            StartScreen.questionRound = 0;
            StartScreen.initQuestionTree();

            System.out.println("INIT\n");
            for (int i = 0; i < 15; i++) {
                System.out.println(i + ") " + Virtue.get(StartScreen.questionTree[i]));
            }

            while (!StartScreen.doQuestion(new Random().nextInt(2))) {
                printQuestionDesc(StartScreen.questionRound);
            }

            //		System.out.println("\nANSWERS\n");
            //		for (int i = 0; i < 15; i++) {
            //			System.out.println(i + ") " +Virtue.get(StartScreen.questionTree[i]));
            //		}
            SaveGame sg = new SaveGame();
            SaveGame.SaveGamePlayerRecord avatar = sg.new SaveGamePlayerRecord();
            avatar.adjuestAttribsPerKarma(StartScreen.questionTree);

            avatar.klass = ClassType.get(StartScreen.questionTree[14]);

            int[] questionTree = StartScreen.questionTree;

            //System.out.println(avatar.klass);
            dist.put(avatar.klass, dist.get(avatar.klass) + 1);
        }

        for (ClassType ct : ClassType.values()) {
            System.out.println(ct + " " + dist.get(ct));
        }

    }

    public void printQuestionDesc(int round) {
        String v1 = Virtue.get(StartScreen.questionTree[round * 2]).toString().toLowerCase();
        String v2 = Virtue.get(StartScreen.questionTree[round * 2 + 1]).toString().toLowerCase();
        System.out.println(String.format("round: %d %s %d and %s %d", round, v1.toString(), (round * 2), v2.toString(), (round * 2 + 1)));
    }

    public int yDownPixel(float y) {
        return (int) (256 * 32 - y - 32);
    }

    public Vector3 getMapPixelCoords(int x, int y) {

        Vector3 v = new Vector3(
                x * 32,
                yDownPixel(y * 32),
                0);

        return v;
    }

}
