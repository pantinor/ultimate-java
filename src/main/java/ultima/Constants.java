package ultima;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;


public interface Constants {
	
	public enum Maps {
		NONE(255), WORLD(0), CASTLE_OF_LORD_BRITISH_1(1), CASTLE_OF_LORD_BRITISH_2(100), LYCAEUM(2), EMPATH_ABBEY(3), SERPENTS_HOLD(4), MOONGLOW(5), BRITAIN(6), JHELOM(7), YEW(8), MINOC(9), TRINSIC(10), SKARABRAE(11), MAGINCIA(12), PAWS(13), BUCCANEERS_DEN(
				14), VESPER(15), COVE(16), DECEIT(17), DESPISE(18), DESTARD(19), WRONG(20), COVETOUS(21), SHAME(22), HYTHLOTH(23), ABYSS(24), SHRINE_HONESTY(25), SHRINE_COMPASSION(26), SHRINE_VALOR(27), SHRINE_JUSTICE(28), SHRINE_SACRIFICE(29), SHRINE_HONOR(
				30), SHRINE_SPIRITUALITY(31), SHRINE_HUMILITY(32), BRICK_CON(33), BRIDGE_CON(34), BRUSH_CON(35), CAMP_CON(36), DNG0_CON(37), DNG1_CON(38), DNG2_CON(39), DNG3_CON(40), DNG4_CON(41), DNG5_CON(42), DNG6_CON(43), DUNGEON_CON(44), FOREST_CON(
				45), GRASS_CON(46), HILL_CON(47), INN_CON(48), MARSH_CON(49), SHIPSEA_CON(50), SHIPSHIP_CON(51), SHIPSHOR_CON(52), SHORE_CON(53), SHORSHIP_CON(54), CAMP_DNG(55);
		
		private int id;

		private Maps(int id) {
			this.setId(id);
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public static Maps convert(int id) {
			for (Maps m : Maps.values()) {
				if (m.getId() == id)
					return m;
			}
			return null;
		}
	}
	
	public static final int WITHOUT_OBJECTS = 0;
	public static final int WITH_GROUND_OBJECTS = 1;
	public static final int WITH_OBJECTS = 2;
	
	public static final int SHOW_AVATAR = (1 << 0);
	public static final int NO_LINE_OF_SIGHT = (1 << 1);
	public static final int FIRST_PERSON = (1 << 2);
	
    public enum ObjectType {
        UNKNOWN,
        CREATURE,
        PERSON
    };
	
	public enum ObjectMovementBehavior {
		FIXED,
		WANDER,
		FOLLOW_AVATAR,
		ATTACK_AVATAR;
	}

	public enum TileSpeed {
		FAST,
		SLOW,
		VSLOW,
		VVSLOW;
	}

	public enum TileEffect {
		EFFECT_NONE,
		EFFECT_FIRE,
		EFFECT_SLEEP,
		EFFECT_POISON,
		EFFECT_POISONFIELD,
		EFFECT_ELECTRICITY,
		EFFECT_LAVA;
	}

	public enum TileAnimationStyle {
		ANIM_NONE,
		ANIM_SCROLL,
		ANIM_CAMPFIRE,
		ANIM_CITYFLAG,
		ANIM_CASTLEFLAG,
		ANIM_SHIPFLAG,
		ANIM_LCBFLAG,
		ANIM_FRAMES;
	}
	
	public enum MapType {
		WORLD,
		CITY,
		SHRINE,
		COMBAT,
		DUNGEON;
	}

	public enum MapBorderBehavior {
		BORDER_WRAP,
		BORDER_EXIT2PARENT,
		BORDER_FIXED;
	}
	
	public static int MOON_PHASES = 24;
	public static int MOON_SECONDS_PER_PHASE = 4;
	public static int MOON_CHAR = 20;
	
	enum Direction {
		
	    WEST(1),
	    NORTH(2),
	    EAST(3),
	    SOUTH(4);
	    
	    private int val;
	    
	    private Direction(int v) {
	    	this.val = v;
	    }

		public int getVal() {
			return val;
		}
		
		public static int getMask(Direction dir) {
			return (1 << (dir.getVal()));
		}
		
		public static boolean isDirInMask(Direction dir, int mask) {
			int v = ((1 << (dir.getVal())) & (mask));
			return (v>0);
		}
		
		public static int addToMask(Direction dir, int mask) {
			return ((1 << (dir.getVal())) | (mask));
		}
		
		public static int removeFromMask(Direction dir, int mask) {
			return ((~(1 << (dir.getVal()))) & (mask));
		}
		
		public static Direction getRandomValidDirection(int mask) {
			int n = 0;
			Direction d[] = new Direction[4];
			for (Direction dir : values()) {
				if (isDirInMask(dir, mask)) {
					d[n] = dir;
					n++;
				}
			}
			if (n==0) return null;
			int rand = new Random().nextInt(n);
			return d[rand];
		}

	};
	
	
	
	

	
	public enum PortalTriggerAction {
		
		ACTION_NONE(0x0),
		ACTION_ENTER(0x1),
		ACTION_KLIMB(0x2),
		ACTION_DESCEND(0x4),
		ACTION_EXIT_NORTH(0x8),
		ACTION_EXIT_EAST(0x10),
		ACTION_EXIT_SOUTH(0x20),
		ACTION_EXIT_WEST(0x40);

		private int intValue;

		private PortalTriggerAction(int i) {
			this.intValue = i;
		}

		public int getIntValue() {
			return intValue;
		}
		

	}
	
	public enum TransportContext {
		TRANSPORT_FOOT(0x1),
		TRANSPORT_HORSE(0x2),
		TRANSPORT_SHIP(0x4),
		TRANSPORT_BALLOON(0x8),
		TRANSPORT_FOOT_OR_HORSE(0x1 | 0x2),
		TRANSPORT_ANY(0xffff);
		
		private int intValue;

		private TransportContext(int intValue) {
			this.intValue = intValue;
		}

		public int getIntValue() {
			return intValue;
		}
	}
	
	public enum MusicType {
		NONE,
		OUTSIDE,
		TOWNS,
		SHRINES,
		SHOPPING,
		RULEBRIT,
		FANFARE,
		DUNGEON,
		COMBAT,
		CASTLES,
		MAX;
	}
	
	
	public enum DungeonTile {
		
		NOTHING(0x00,"Nothing","blank"),
		LADDER_UP(0x10 	,"Ladder Up", "up_ladder"),
		LADDER_DOWN(0x20 	,"Ladder Down", "down_ladder"),
		LADDER_UP_DOWN(0x30 	,"Ladder Up & Down", "down_ladder"),
		CHEST(0x40 	,"Treasure Chest", "chest"),
		CEILING_HOLE(0x50 	,"Ceiling Hole", "rocks"),
		FLOOR_HOLE(0x60 	,"Floor Hole", "rocks"),
		ORB(0x70 	,"Magic Orb", "hit_flash"),
		WIND_TRAP(0x80 	,"Winds/Darknes Trap", "swamp"),
		ROCK_TRAP(0x81 	,"Falling Rock Trap", "swamp"),
		PIT_TRAP(0x8E 	,"Pit Trap", "swamp"),
		FOUNTAIN_PLAIN(0x90 	,"Plain Fountain", "magic_flash"),
		FOUNTAIN_HEAL(0x91 	,"Healing Fountain", "magic_flash"),
		FOUNTAIN_ACID(0x92 	,"Acid Fountain", "magic_flash"),
		FOUNTAIN_CURE(0x93 	,"Cure Fountain", "magic_flash"),
		FOUNTAIN_POISON(0x94 	,"Poison Fountain", "magic_flash"),
		FIELD_POISON(0xA0 	,"Poison Field", "poison_field"),
		FIELD_ENERGY(0xA1 	,"Energy Field", "energy_field"),
		FIELD_FIRE(0xA2 	,"Fire Field", "fire_field"),
		FIELD_SLEEP(0xA3 	,"Sleep Field", "sleep_field"),
		ALTAR(0xB0 	,"Altar", "altar"),
		DOOR(0xC0 	,"Door", "locked_door"),
		ROOM_1(0xD0 	,"Dungeon Room 1", "spacer_square"),
		ROOM_2(0xD1 	,"Dungeon Room 2", "spacer_square"),
		ROOM_3(0xD2 	,"Dungeon Room 3", "spacer_square"),
		ROOM_4(0xD3 	,"Dungeon Room 4", "spacer_square"),
		ROOM_5(0xD4 	,"Dungeon Room 5", "spacer_square"),
		ROOM_6(0xD5 	,"Dungeon Room 6", "spacer_square"),
		ROOM_7(0xD6 	,"Dungeon Room 7", "spacer_square"),
		ROOM_8(0xD7 	,"Dungeon Room 8", "spacer_square"),
		ROOM_9(0xD8 	,"Dungeon Room 9", "spacer_square"),
		ROOM_10(0xD9 	,"Dungeon Room 10", "spacer_square"),
		ROOM_11(0xDA 	,"Dungeon Room 11", "spacer_square"),
		ROOM_12(0xDB 	,"Dungeon Room 12", "spacer_square"),
		ROOM_13(0xDC 	,"Dungeon Room 13", "spacer_square"),
		ROOM_14(0xDD 	,"Dungeon Room 14", "spacer_square"),
		ROOM_15(0xDE 	,"Dungeon Room 15", "spacer_square"),
		ROOM_16(0xDF 	,"Dungeon Room 16", "spacer_square"),
		SECRET_DOOR(0xE0 	,"Secret Door", "secret_door"),
		WALL(0xF0 	,"Wall ", "stone_wall");
		
		private int value;
		private String type;
		private String tileName;
		
		private DungeonTile(int value, String type, String tileName) {
			this.value = value;
			this.type = type;
			this.tileName = tileName;
		}

		public int getValue() {
			return value;
		}

		public String getType() {
			return type;
		}

		public String getTileName() {
			return tileName;
		}

		public void setValue(int value) {
			this.value = value;
		}

		public void setType(String type) {
			this.type = type;
		}

		public void setTileName(String tileName) {
			this.tileName = tileName;
		}	
		
		public static DungeonTile getTileByValue(int val) {
			DungeonTile ret = DungeonTile.NOTHING;
			for (DungeonTile d : DungeonTile.values()) {
				if (val == d.getValue()) {
					ret = d;
					break;
				}
			}
			return ret;
		}
		
	}
	
	
	public class ClasspathFileHandleResolver implements FileHandleResolver {
		public FileHandle resolve (String fileName) {
			return Gdx.files.classpath(fileName);
		}
	}



}
