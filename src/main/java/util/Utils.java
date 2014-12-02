package util;


import java.io.InputStream;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import objects.BaseMap;
import objects.Conversation;
import objects.Person;
import objects.Tile;
import objects.TileSet;

import org.apache.commons.io.IOUtils;
import org.lwjgl.BufferUtils;

import ultima.Constants;

public class Utils implements Constants {

	public static String properCase(String s) {
		return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
	}
	
	/**
	 * load the tile indexes from the ULT file
	 */
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
	
	/**
	 * Read the TLK file and parse the conversations
	 * @param fname
	 * @return
	 */
	public static List<Conversation> getDialogs(String fname) {
		byte[] bytes;
		try {
			InputStream is = Utils.class.getResourceAsStream("/data/" + fname);
			bytes = IOUtils.toByteArray(is);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		List<Conversation> dialogs = new ArrayList<Conversation>();
		
		int block = 288;
		for (int i=0;i<16;i++) {
			
			int pos = i * block;
			
			int questionFlag = 0;
			int respAffectsHumility = 0;
			int probTurnAway = 0;
			
			CharBuffer bb = BufferUtils.createCharBuffer(block);
			String[] strings = new String[12];
			int stringIndex = 0;

			for (int y=0;y<block;y++) {

				if (y == 0) {
					questionFlag = bytes[pos];
				} else if (y == 1) {
					respAffectsHumility = bytes[pos];
				} else if (y == 2) {
					probTurnAway = bytes[pos];
				} else if (y > 2) {
					if (bytes[pos] == 0x0 && stringIndex < 12) {
						bb.flip();
						strings[stringIndex] = new String(bb.toString().replace("\n", " "));
						stringIndex++;
						bb.clear();
					} else {
						bb.put((char)bytes[pos]);
					}
				}
				pos++;
			}
			Conversation c = new Conversation(i+1,probTurnAway,questionFlag,respAffectsHumility,strings);
			dialogs.add(c);
		}
		return dialogs;

	}
	
	/**
	 * Read the ULT file and parse the people
	 * @param fname
	 * @return
	 */
	public static Person[] getPeople(String fname, TileSet ts) {
		byte[] bytes;
		try {
			InputStream is = Utils.class.getResourceAsStream("/data/" + fname);
			bytes = IOUtils.toByteArray(is);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		Person[] people = new Person[32];
		
		int MAX_PEOPLE = 32;
		int MAP_WIDTH = 32;
		int startOffset = MAP_WIDTH * MAP_WIDTH;
		int end = startOffset + MAX_PEOPLE;
		int count = 0;
		for (int i=startOffset;i<end;i++) {
			int index = bytes[i] & 0xff;
			if (index == 0) {
				count ++;
				continue;
			}

			Person p = new Person();
			p.setId(count);
			p.setTileIndex(index);
			
			if (ts != null) {
				Tile t = ts.getTileByIndex(index);
				p.setTile(t);
			}
			
			people[count] = p;
			count++;
		}
		
		startOffset = MAP_WIDTH * MAP_WIDTH + MAX_PEOPLE * 1;
		end = startOffset + MAX_PEOPLE;
		count = 0;
		for (int i=startOffset;i<end;i++) {
			int start_x = bytes[i] & 0xff;
			Person p = people[count];
			if (p == null) {
				count++;
				continue;
			}
			p.setStart_x(start_x);
			count++;
		}
		
		startOffset = MAP_WIDTH * MAP_WIDTH + MAX_PEOPLE * 2;
		end = startOffset + MAX_PEOPLE;
		count = 0;
		for (int i=startOffset;i<end;i++) {
			int start_y = bytes[i] & 0xff;
			Person p = people[count];
			if (p == null) {
				count++;
				continue;
			}
			p.setStart_y(start_y);
			count++;
		}
		
		startOffset = MAP_WIDTH * MAP_WIDTH + MAX_PEOPLE * 6;
		end = startOffset + MAX_PEOPLE;
		count = 0;
		for (int i = startOffset; i < end; i++) {
			int m = bytes[i] & 0xff;
			Person p = people[count];
			if (p == null) {
				count++;
				continue;
			}
			if (m == 0)
				p.setMovement(ObjectMovementBehavior.FIXED);
			else if (m == 1)
				p.setMovement(ObjectMovementBehavior.WANDER);
			else if (m == 0x80)
				p.setMovement(ObjectMovementBehavior.FOLLOW_AVATAR);
			else if (m == 0xFF)
				p.setMovement(ObjectMovementBehavior.ATTACK_AVATAR);

			count++;
		}
		
		startOffset = MAP_WIDTH * MAP_WIDTH + MAX_PEOPLE * 7;
		end = startOffset + MAX_PEOPLE;
		count = 0;
		for (int i=startOffset;i<end;i++) {
			int id = bytes[i] & 0xff;
			Person p = people[count];
			if (p == null) {
				count++;
				continue;
			}
			p.setDialogId(id);
			count++;
		}
		
		return people;
		
	}
	
	public static Object loadXml(String fname, Class<?> clazz) throws Exception {
		InputStream is = Utils.class.getResourceAsStream("/xml/"+fname);
		JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		return jaxbUnmarshaller.unmarshal(is);
	}
	
	
	
	public static int adjustValueMax(int v, int val, int max) {
		v += val;
		if (v > max) {
			v = max;
		}
		return v;
	}

	public static int adjustValueMin(int v, int val, int min) {
		v += val;
		if (v < min) {
			v = min;
		}
		return v;
	}

	public static int adjustValue(int v, int val, int max, int min) {
		v += val;
		if (v > max) {
			v = max;
		}
		if (v < min) {
			v = min;
		}
		return v;
	}



}
