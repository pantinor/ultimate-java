import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import objects.Party;
import objects.SaveGame;

import org.testng.annotations.Test;

import ultima.Constants.InventoryType;
import ultima.Constants.Maps;
import ultima.Constants.WeaponType;
import vendor.BaseVendor;
import vendor.VendorClassSet;
import vendor.WeaponVendor;

public class VendorTest {

	@Test
	public void testWeaponVendor() throws Exception {
		
		File file = new File("target/classes/xml/vendor.xml");
		JAXBContext jaxbContext = JAXBContext.newInstance(VendorClassSet.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		VendorClassSet vcs = (VendorClassSet) jaxbUnmarshaller.unmarshal(file);
		vcs.init();
		
		SaveGame sg = new SaveGame();
		
		SaveGame.SaveGamePlayerRecord rec = sg.new SaveGamePlayerRecord();
		rec.name = "avatar";
		rec.hp=200;
		
		Party party = new Party(sg);
		party.addMember(rec);

		sg.gold = 25;
		sg.food = 200;
		
		party.getSaveGame().weapons[WeaponType.SLING.ordinal()] = 5;
		party.getSaveGame().weapons[WeaponType.MAGICAXE.ordinal()] = 5;

		
		BaseVendor v = new WeaponVendor(vcs.getVendor(InventoryType.WEAPON, Maps.BRITAIN), party);

		while (true) {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			
			if (!v.nextDialog()) {
				break;
			}
			
			String input = br.readLine();
			
			if (input != null && input.equals("bye")) {
				break;
			}
			
			v.setResponse(input);
			
		}
		
		System.err.println("sg gold = " + sg.gold);
		for (WeaponType wt : WeaponType.values()) {
			System.err.println(wt.toString() + " count=" + sg.weapons[wt.ordinal()] );
		}


	}
	
	

}
