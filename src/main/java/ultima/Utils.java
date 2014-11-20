package ultima;


import java.io.InputStream;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
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

public class Utils implements Constants {
	
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
			byte m = bytes[i];
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
	

	
	 
			
	/**
	 * Needs Fixing! :)
	 */
	public static int[][] screenFindLineOfSight(Tile[][] viewportTiles, int row1, int row2, int col1, int col2) {

		int x, y;

		int VIEWPORT_W = row2 - row1 + 1;
		int VIEWPORT_H = col2 - col1;
		int[][] screenLos = new int[VIEWPORT_W + 1][VIEWPORT_H];

		int _OCTANTS = 8;
		int _NUM_RASTERS_COLS = 4;

		int octant = 0;
		int xOrigin = 0, yOrigin = 0, xSign = 0, ySign = 0, xTile = 0, yTile = 0, xTileOffset = 0, yTileOffset = 0;
		boolean reflect = false;
		for (octant = 0; octant < _OCTANTS; octant++) {
			switch (octant) {
			case 0:
				xSign = 1;
				ySign = 1;
				reflect = false;
				break; // lower-right
			case 1:
				xSign = 1;
				ySign = 1;
				reflect = true;
				break;
			case 2:
				xSign = 1;
				ySign = -1;
				reflect = true;
				break; // lower-left
			case 3:
				xSign = -1;
				ySign = 1;
				reflect = false;
				break;
			case 4:
				xSign = -1;
				ySign = -1;
				reflect = false;
				break; // upper-left
			case 5:
				xSign = -1;
				ySign = -1;
				reflect = true;
				break;
			case 6:
				xSign = -1;
				ySign = 1;
				reflect = true;
				break; // upper-right
			case 7:
				xSign = 1;
				ySign = -1;
				reflect = false;
				break;
			}

			// determine the origin point for the current LOS octant
			xOrigin = VIEWPORT_W / 2;
			yOrigin = VIEWPORT_H / 2;

			// make sure the segment doesn't reach out of bounds
			int maxWidth = xOrigin;
			int maxHeight = yOrigin;
			int currentRaster = 0;

			// just in case the viewport isn't square, swap the width and height
			if (reflect) {
				// swap height and width for later use
				maxWidth ^= maxHeight;
				maxHeight ^= maxWidth;
				maxWidth ^= maxHeight;
			}

			// check the visibility of each tile
			for (int currentCol = 1; currentCol <= _NUM_RASTERS_COLS; currentCol++) {
				for (int currentRow = 0; currentRow <= currentCol; currentRow++) {
					// swap X and Y to reflect the octant rasters
					if (reflect) {
						xTile = xOrigin + (currentRow * ySign);
						yTile = yOrigin + (currentCol * xSign);
					} else {
						xTile = xOrigin + (currentCol * xSign);
						yTile = yOrigin + (currentRow * ySign);
					}

					if (viewportTiles[xTile][yTile].isOpaque()) {
						// a wall was detected, so go through the raster for
						// this wall
						// segment and mark everything behind it with the
						// appropriate
						// shadow bitmask.
						//
						// first, get the correct raster
						//
						if ((currentCol == 1) && (currentRow == 0)) {
							currentRaster = 0;
						} else if ((currentCol == 1) && (currentRow == 1)) {
							currentRaster = 1;
						} else if ((currentCol == 2) && (currentRow == 0)) {
							currentRaster = 2;
						} else if ((currentCol == 2) && (currentRow == 1)) {
							currentRaster = 3;
						} else if ((currentCol == 2) && (currentRow == 2)) {
							currentRaster = 4;
						} else if ((currentCol == 3) && (currentRow == 0)) {
							currentRaster = 5;
						} else if ((currentCol == 3) && (currentRow == 1)) {
							currentRaster = 6;
						} else if ((currentCol == 3) && (currentRow == 2)) {
							currentRaster = 7;
						} else if ((currentCol == 3) && (currentRow == 3)) {
							currentRaster = 8;
						} else if ((currentCol == 4) && (currentRow == 0)) {
							currentRaster = 9;
						} else if ((currentCol == 4) && (currentRow == 1)) {
							currentRaster = 10;
						} else if ((currentCol == 4) && (currentRow == 2)) {
							currentRaster = 11;
						} else if ((currentCol == 4) && (currentRow == 3)) {
							currentRaster = 12;
						} else {
							currentRaster = 13;
						}

						xTileOffset = 0;
						yTileOffset = 0;

						for (int currentSegment = 0; currentSegment < shadowRaster[currentRaster][0]; currentSegment++) {
							// each shadow segment is 2 bytes
							int shadowType = shadowRaster[currentRaster][currentSegment * 2 + 1];
							int shadowLength = shadowRaster[currentRaster][currentSegment * 2 + 2];

							// update the raster length to make sure it fits in the viewport
							shadowLength = (shadowLength + 1 + yTileOffset > maxWidth ? maxWidth : shadowLength);

							// check to see if we should move up a row
							if ((shadowType & 0x80) > 0) {
								// remove the flag from the shadowType
								shadowType ^= _N___;
								// if (currentRow + yTileOffset >= maxHeight) {
								if (currentRow + yTileOffset > maxHeight) {
									break;
								}
								xTileOffset = yTileOffset;
								yTileOffset++;
							}


							for (int currentShadow = 1; currentShadow <= shadowLength; currentShadow++) {
								// apply the shadow to the shadowMap
								if (reflect) {
									screenLos[xTile + ((yTileOffset) * ySign)][yTile + ((currentShadow + xTileOffset) * xSign)] |= shadowType;
								} else {
									screenLos[xTile + ((currentShadow + xTileOffset) * xSign)][yTile + ((yTileOffset) * ySign)] |= shadowType;
								}
							}
							xTileOffset += shadowLength;
						} 
					} 
				} 
			} 
		} 

		// go through all tiles on the viewable area and set the appropriate visibility
		for (y = 0; y < VIEWPORT_H; y++) {
			for (x = 0; x < VIEWPORT_W; x++) {
				// if the shadow flags equal __VCH, hide it, otherwise it's fully visible
				if ((screenLos[x][y] & __VCH) == __VCH) {
					screenLos[x][y] = 0;
				} else {
					screenLos[x][y] = 1;
				}
			}
		}

		return screenLos;
	}
	
	
	public static void adjustValueMax(int v, int val, int max) {
		v += val;
		if (v > max) {
			v = max;
		}
	}

	public static void adjustValueMin(int v, int val, int min) {
		v += val;
		if (v < min) {
			v = min;
		}
	}

	public static void adjustValue(int v, int val, int max, int min) {
		v += val;
		if (v > max) {
			v = max;
		}
		if (v < min) {
			v = min;
		}
	}

	public static void adjustValueMax(short v, int val, int max) {
		v += val;
		if (v > max) {
			v = (short) max;
		}
	}

	public static void adjustValueMin(short v, int val, int min) {
		v += val;
		if (v < min) {
			v = (short) min;
		}
	}

	public static void adjustValue(short v, int val, int max, int min) {
		v += val;
		if (v > max) {
			v = (short) max;
		}
		if (v < min) {
			v = (short) min;
		}
	}

}
