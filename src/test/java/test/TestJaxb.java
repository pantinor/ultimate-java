package test;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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
import org.testng.annotations.Test;

import ultima.Constants;
import ultima.Constants.Direction;
import ultima.Constants.Item;
import ultima.Constants.KarmaAction;
import ultima.Constants.Maps;
import ultima.Constants.TileAttrib;
import ultima.Constants.TileRule;
import ultima.Constants.Vector;
import ultima.Constants.Virtue;
import util.ShadowFOV;
import util.Utils;
import vendor.VendorClass;
import vendor.VendorClassSet;

import com.google.common.io.LittleEndianDataInputStream;


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

		String r = t.replaceAll("index=\"([0-9]+/*\\.*[0-9]*)\"","|");
		
		StringTokenizer st = new StringTokenizer(r,"|");
		StringBuffer sb = new StringBuffer();
		int count=128;
		while (st.hasMoreTokens()) {
			sb.append(st.nextToken());
			sb.append("index=\""+count+"\"");
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
	public void parseTlkFiles() throws Exception {
		Person[] people = Utils.getPeople("britain.ult", null);
		List<Conversation> cons = Utils.getDialogs("britain.tlk");
		
		for (Person p: people) {
			if (p != null) {
				for (Conversation c : cons) {
					if (c.getIndex() == p.getDialogId()) {
						p.setConversation(c);
					}
				}
			}
		}
		System.out.println(people);
	}
	
	//@Test
	public void testDirectionMask() throws Exception {
		
		Direction dir = Direction.WEST;
		
		int mask = Direction.addToMask(Direction.NORTH, 0);
		assert(!Direction.isDirInMask(dir, mask));
		
		mask = Direction.addToMask(Direction.EAST, mask);
		assert(!Direction.isDirInMask(dir, mask));

		mask = Direction.addToMask(Direction.WEST, mask);
		assert(Direction.isDirInMask(dir, mask));
		
		mask = Direction.removeFromMask(Direction.WEST, mask);
		assert(!Direction.isDirInMask(dir, mask));
		
		Direction dir2 = Direction.getRandomValidDirection(mask);
		assert(true);

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
        	String st = ((sg.stones & (1 << i)) > 0 ? "+STONE" : "") ;
        	String ru = ((sg.runes & (1 << i)) > 0 ? "+RUNE" : "") ;
        	//System.err.println(v + " " + st + " " + ru);
        }
        
        System.err.println("---------------");
		sg.runes |= Virtue.HUMILITY.getLoc();
		
        for (int i = 0; i < 8; i++) {
        	Virtue v = Constants.Virtue.get(i);
        	String st = ((sg.stones & (1 << i)) > 0 ? "+STONE" : "") ;
        	String ru = ((sg.runes & (1 << i)) > 0 ? "+RUNE" : "") ;
        	//System.err.println(v + " " + st + " " + ru);
        }
        
        sg.items |= Item.BELL.getLoc();
        
        for (Item item : Constants.Item.values()) {
        	if (!item.isVisible()) continue;
        	//System.err.println((sg.items & (1 << item.ordinal())) > 0 ? item.getDesc() : "") ;
        }
        
        sg.items |= Item.HORN.getLoc();
        
        for (Item item : Constants.Item.values()) {
        	if (!item.isVisible()) continue;
        	//System.err.println((sg.items & (1 << item.ordinal())) > 0 ? item.getDesc() : "") ;
        }
		
	}
	
	//@Test
	public void testCreateParty() throws Exception {
		
		SaveGame sg = new SaveGame();
		sg.read("D:\\ultima\\ULTIMA4\\"+Constants.PARTY_SAV_BASE_FILENAME);
		
		Party p = new Party(sg);
		
		assert(true);

	}
	

	
	//@Test
	public void testLOS2() throws Exception {
		
		long t = System.currentTimeMillis();
		
		int dim = 21;
		
		float[][] vt = new float[dim][dim];
		for (int x=0;x<dim;x++) {
			for (int y=0;y<dim;y++) {
				//vt[x][y] = new Tile();
				//vt[x][y].setOpaque(true);
			}
		}
		
		vt[12][2] = 1f;
		vt[11][8] = 1f;
		vt[18][1] = 1f;
		vt[18][8] = 1f;
		vt[18][7] = 1f;
		vt[18][6] = 1f;
		
		ShadowFOV fov = new ShadowFOV();

		float[][] los = fov.calculateFOV(vt, 10, 10, dim);
		
		for (int y=0;y<dim;y++) {
			for (int x=0;x<dim;x++) {
				System.out.print(los[x][y] <= 0?"X":"0");
				//System.out.print("|"+los[x][y]);
			}
			System.out.println("");

		}
		
		System.out.println("testLOS2 time: " + (System.currentTimeMillis() - t));



	}
	
	//@Test
	public void testMapShadows() throws Exception {
				
		File file2 = new File("target/classes/xml/tileset-base.xml");
		JAXBContext jaxbContext = JAXBContext.newInstance(TileSet.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		TileSet ts = (TileSet) jaxbUnmarshaller.unmarshal(file2);
		ts.setMaps();
		
		File file3 = new File("target/classes/xml/maps.xml");
		jaxbContext = JAXBContext.newInstance(MapSet.class);
		jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		MapSet ms = (MapSet) jaxbUnmarshaller.unmarshal(file3);
		ms.init(ts);
		
		BaseMap m = Maps.WORLD.getMap();

		int startx = 56;
		int starty = 65;
		
		long t = System.currentTimeMillis();

		
		ShadowFOV fov = new ShadowFOV();

		float[][] lightMap = fov.calculateFOV(m.getShadownMap(), startx, starty, 20);
		
		for (int y=0;y<256;y++) {
			for (int x=0;x<256;x++) {
				System.out.print(lightMap[x][y] <= 0?"X":"0");
				//System.out.print("|"+los[x][y]);
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
		
		assert(TileRule.water.has(TileAttrib.unwalkable));
		assert(!TileRule.grass.has(TileAttrib.unwalkable));
		
	}
	
	//@Test
	public void testMovement() throws Exception {
				
		TileSet baseTileSet = (TileSet) Utils.loadXml("tileset-base.xml", TileSet.class);	
		baseTileSet.setMaps();
					
		MapSet maps = (MapSet) Utils.loadXml("maps.xml", MapSet.class);
		maps.init(baseTileSet);
		
		BaseMap map = Maps.BRICK_CON.getMap();
		
		//int d = map.movementDistance(8, 3, 1, 9);

	    int dirmask = map.getRelativeDirection(7, 7, 1, 1);

		

	}
	
	@Test
	public void testNibbles() throws Exception {
		byte[] data = new byte[4];
		data[1] = (byte)0x85;
		int x = (data[1] >> 4) & 0x0f;
		int y = data[1] & 0x0f; 
		
		int z = x;
	}


}
