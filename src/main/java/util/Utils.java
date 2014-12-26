package util;


import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import objects.BaseMap;
import objects.Conversation;
import objects.Creature;
import objects.Person;
import objects.Tile;
import objects.TileSet;

import org.apache.commons.io.IOUtils;
import org.lwjgl.BufferUtils;

import ultima.Constants;
import ultima.Ultima4;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;

public class Utils implements Constants {

	public static String properCase(String s) {
		return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
	}
	
	/**
	 * load the tile indexes from the ULT file
	 */
	public static void setMapTiles(BaseMap map, TileSet ts) throws Exception {
		
		InputStream is = new FileInputStream("assets/data/" + map.getFname().toUpperCase());
		byte[] bytes = IOUtils.toByteArray(is);

		Tile[] tiles = new Tile[map.getWidth() * map.getHeight()];
		
		if (map.getType() == MapType.world || map.getType() == MapType.city) {
			
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
		    
		} else if (map.getType() == MapType.combat) {
			
			int pos = 0x40;
	    	for(int y = 0; y < map.getHeight(); y++) {
                for(int x = 0; x < map.getWidth(); x++) {                    
    				int index = bytes[pos] & 0xff;
					pos++;
					Tile tile = ts.getTileByIndex(index);
					if (tile == null) {
						System.out.println("Tile index cannot be found: " + index + " using index 127 for black space.");
						tile = ts.getTileByIndex(127);
					}
                    tiles[x + y * map.getWidth()] = tile;
                }
            }
		} else if (map.getType() == MapType.shrine) {
			int pos = 0;
	    	for(int y = 0; y < map.getHeight(); y++) {
                for(int x = 0; x < map.getWidth(); x++) {                    
    				int index = bytes[pos] & 0xff;
					pos++;
					Tile tile = ts.getTileByIndex(index);
					if (tile == null) {
						System.out.println("Tile index cannot be found: " + index + " using index 127 for black space.");
						tile = ts.getTileByIndex(127);
					}
					if (tile.getIndex() == 31) { //avatar position
						tile = ts.getTileByIndex(4);
					}
                    tiles[x + y * map.getWidth()] = tile;
                }
            }
	    }
		
	
	    map.setTiles(tiles);
	}
	
	//used for telescope viewing
	public static Texture peerGem(Maps map, TextureAtlas atlas) throws Exception {
		
		Texture t = null;

		if (map.getMap().getType() == MapType.city) {
			
			BufferedImage sheet = ImageIO.read(new File("assets/tilemaps/tiles-vga.png"));
			BufferedImage canvas = new BufferedImage(16*32, 16*32, BufferedImage.TYPE_INT_ARGB);
			
			for (int y = 0; y < 32; y++) {
				for (int x = 0; x < 32; x++) {
					Tile ct = map.getMap().getTile(x, y);
					AtlasRegion ar = (AtlasRegion)atlas.findRegion(ct.getName());
					BufferedImage sub = sheet.getSubimage(ar.getRegionX(), ar.getRegionY(), 16, 16);
					canvas.getGraphics().drawImage(sub,x*16,y*16,16,16,null);
				}
			}
			
			Pixmap p = createPixmap(
					Ultima4.SCREEN_WIDTH, 
					Ultima4.SCREEN_HEIGHT, 
					canvas, 
					(Ultima4.SCREEN_WIDTH - canvas.getWidth()) / 2,
					(Ultima4.SCREEN_HEIGHT - canvas.getHeight()) / 2);
			
			
			t = new Texture(p);
			p.dispose();
			
		} else if (map.getMap().getType() == MapType.dungeon) {
			//NO OP not needed since I added the minimap already on the HUD
		}

		return t;
		
	}
	
	//used for view gem on the world map only
	public static Texture peerGem(BaseMap worldMap, int avatarX, int avatarY, TextureAtlas atlas) throws Exception {
		BufferedImage sheet = ImageIO.read(new File("assets/tilemaps/tiles-vga.png"));
		BufferedImage canvas = new BufferedImage(16*64, 16*64, BufferedImage.TYPE_INT_ARGB);
		
		int startX = avatarX - 32;
		int startY = avatarY - 32;
		int endX = avatarX + 32;
		int endY = avatarY + 32;
		int indexX = 0;
		int indexY = 0;
		for (int y = startY; y < endY; y++) {
			for (int x = startX; x < endX; x++) {
				int cx = x;
				if (x<0) {
					cx = 256+x;
				} else if(x>=256) {
					cx = x - 256;
				}
				int cy = y;
				if (y<0) {
					cy = 256+y;
				} else if(y>=256) {
					cy = y - 256;
				}
				Tile ct = worldMap.getTile(cx, cy);
				AtlasRegion ar = (AtlasRegion)atlas.findRegion(ct.getName());
				BufferedImage sub = sheet.getSubimage(ar.getRegionX(), ar.getRegionY(), 16, 16);
				canvas.getGraphics().drawImage(sub,indexX*16,indexY*16,16,16,null);
				
				Creature cr = worldMap.getCreatureAt(cx, cy);
				if (cr != null) {
					//canvas.getGraphics().setColor(java.awt.Color.RED);
					canvas.getGraphics().fillRect(indexX*16, indexY*16, 16, 16);
				}
				
				indexX++;
			}
			indexX = 0;
			indexY++;
		}
		
		//add avatar in the middle
		//canvas.getGraphics().setColor(java.awt.Color.WHITE);
		canvas.getGraphics().fillRect((16*64)/2, (16*64)/2, 16, 16);
		
		java.awt.Image tmp = canvas.getScaledInstance(16*32, 16*32, Image.SCALE_AREA_AVERAGING);
		BufferedImage scaledCanvas = new BufferedImage(16*32, 16*32, BufferedImage.TYPE_INT_ARGB);
		scaledCanvas.getGraphics().drawImage(tmp, 0, 0 , null);
		
		Pixmap p = createPixmap(
				Ultima4.SCREEN_WIDTH, 
				Ultima4.SCREEN_HEIGHT, 
				scaledCanvas, 
				(Ultima4.SCREEN_WIDTH - scaledCanvas.getWidth()) / 2,
				(Ultima4.SCREEN_HEIGHT - scaledCanvas.getHeight()) / 2);
				
		Texture t = new Texture(p);
		p.dispose();
		return t;

	}
	
	public static Pixmap createPixmap(int width, int height, BufferedImage image, int sx, int sy) {
		
		int imgWidth = image.getWidth();
		int imgHeight = image.getHeight();
		
		Pixmap pix = new Pixmap(width, height, Pixmap.Format.RGBA8888);
		pix.setColor(0f, 0f, 0f, .45f);
		pix.fillRectangle(0, 0, width, height);
		
		int[] pixels = image.getRGB(0, 0, imgWidth, imgHeight, null, 0, width);

		for (int x = 0; x < imgWidth; x++) {
			for (int y = 0; y < imgHeight; y++) {
				int pixel = pixels[y * width + x];
				pix.drawPixel(sx + x, sy + y, getRGBA(pixel));
			}
		}
		
		return pix;	
	}
	 
	public static int getRGBA(int rgb) {
		int a = rgb >> 24;
		a &= 0x000000ff;
		int rest = rgb & 0x00ffffff;
		rest <<= 8;
		rest |= a;
		return rest;
	}
		 

		 
	

	
	/**
	 * Read the TLK file and parse the conversations
	 * @param fname
	 * @return
	 */
	public static List<Conversation> getDialogs(String fname) {
		byte[] bytes;
		try {
			InputStream is = new FileInputStream("assets/data/" + fname);
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
			InputStream is = new FileInputStream("assets/data/" + fname);
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
		InputStream is = new FileInputStream("assets/xml/"+fname);
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
