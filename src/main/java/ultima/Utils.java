package ultima;


import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import objects.BaseMap;
import objects.Tile;
import objects.TileSet;

import org.apache.commons.io.IOUtils;

public class Utils {
	
	public static void setMapTiles(BaseMap map, TileSet ts) throws Exception {
		
		InputStream is = Utils.class.getResourceAsStream("/data/" + map.getFname());
		byte[] bytes = IOUtils.toByteArray(is);

		Tile[] tiles = new Tile[map.getWidth() * map.getHeight()];
		int pos = 0;
	    for(int ych = 0; ych < map.getHeight() / 32; ych++) {
	        for(int xch = 0; xch < map.getWidth() / 32; xch++) {
                for(int y = 0; y < 32; y++) {
                    for(int x = 0; x < 32; x++) {                    
        				int index = bytes[pos] & 0xff;
						pos++;
						Tile tile = ts.getTileByIndex(index);
						if (tile == null) {
							System.out.println("Tile index cannot be found: " + index + " using index 127 for black space.");
							tile = ts.getTileByIndex(127);
						}
                        tiles[x + (y * map.getWidth()) + (xch * 32) + (ych * 32 * map.getWidth())] = tile;
                    }
                }
	            
	        }
	    }
	    map.setTiles(tiles);
	}
	
	public static Object loadXml(String fname, Class<?> clazz) throws Exception {
		InputStream is = Utils.class.getResourceAsStream("/xml/"+fname);
		JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		return jaxbUnmarshaller.unmarshal(is);
	}

}
