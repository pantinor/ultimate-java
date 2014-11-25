import java.io.File;
import java.io.FilenameFilter;
import java.util.StringTokenizer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import objects.Armor;
import objects.ArmorSet;
import objects.BaseMap;
import objects.Creature;
import objects.CreatureSet;
import objects.MapSet;
import objects.Party;
import objects.Rule;
import objects.SaveGame;
import objects.Tile;
import objects.TileRules;
import objects.TileSet;
import objects.Weapon;
import objects.WeaponSet;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;

import ultima.Constants;
import ultima.Constants.Direction;
import ultima.Constants.Reagent;
import util.ShadowFOV;
import util.Utils;
import vendor.VendorClass;
import vendor.VendorClassSet;


public class TestJaxb {
	
	@Test
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
	public void testTileRules() throws Exception {
		File file = new File("target/classes/xml/tileRules.xml");
		JAXBContext jaxbContext = JAXBContext.newInstance(TileRules.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		TileRules ts = (TileRules) jaxbUnmarshaller.unmarshal(file);
		for (Rule t : ts.getRules()) {
			System.out.println(t);
		}
	}
	
	//@Test
	public void testMaps() throws Exception {
				
		File file2 = new File("target/classes/xml/tileset-base.xml");
		JAXBContext jaxbContext = JAXBContext.newInstance(TileSet.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		TileSet ts = (TileSet) jaxbUnmarshaller.unmarshal(file2);
		ts.setMaps();
		
		File file3 = new File("target/classes/xml/maps.xml");
		jaxbContext = JAXBContext.newInstance(MapSet.class);
		jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		MapSet ms = (MapSet) jaxbUnmarshaller.unmarshal(file3);
		
		for (BaseMap map : ms.getMaps()) {
			
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
		Utils.getPeople("britain.ult", null);
		Utils.getDialogs("britain.tlk");

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
		
		SaveGame sg = new SaveGame();
		sg.read("D:\\ultima\\ULTIMA4\\"+Constants.PARTY_SAV_BASE_FILENAME);
		
		
		SaveGame sg2 = new SaveGame();
		
		SaveGame.SaveGamePlayerRecord avatar = sg2.new SaveGamePlayerRecord();
		avatar.name = "paul";
		avatar.hp = 199;
		
		sg2.food = 30000;
		sg2.gold = 200;
		sg2.reagents[Reagent.GINSENG.ordinal()] = 3;
		sg2.reagents[Reagent.GARLIC.ordinal()] = 4;
		sg2.torches = 2;
		
		sg2.players[0] = avatar;
		
		sg2.write("mysave.sav");
				
		//byte[] bytes1 = IOUtils.toByteArray(new FileReader(new File("D:\\ultima\\ULTIMA4\\"+Constants.PARTY_SAV_BASE_FILENAME)));
		//byte[] bytes2 = IOUtils.toByteArray(new FileReader(new File("mysave.sav")));
		
	}
	
	//@Test
	public void testCreateParty() throws Exception {
		
		SaveGame sg = new SaveGame();
		sg.read("D:\\ultima\\ULTIMA4\\"+Constants.PARTY_SAV_BASE_FILENAME);
		
		Party p = new Party(sg);
		
		assert(true);

	}
	
	//@Test
	public void testLOS() throws Exception {
		
		long t = System.currentTimeMillis();

		
		int dim = 13;
		
		Tile[][] vt = new Tile[dim][dim];
		for (int x=0;x<dim;x++) {
			for (int y=0;y<dim;y++) {
				vt[x][y] = new Tile();
				//vt[x][y].setOpaque(true);
			}
		}
		
		vt[2][2].setOpaque(true);
		vt[1][8].setOpaque(true);
		vt[8][1].setOpaque(true);
		vt[8][8].setOpaque(true);
		vt[8][7].setOpaque(true);
		vt[8][6].setOpaque(true);

		int[][] los = Utils.screenFindLineOfSight(vt, 0, dim-1, 0, dim-1) ;
		
		for (int y=0;y<dim;y++) {
			for (int x=0;x<dim;x++) {
				System.out.print(los[x][y] == 1?"O":"X");
			}
			System.out.println("");

		}
		
		System.out.println("testLOS time: " + (System.currentTimeMillis() - t));



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
		
		BaseMap m = ms.getMapById(0);

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
	

	

}
