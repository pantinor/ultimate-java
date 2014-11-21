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
	
	/*
	 * bitmasks for LOS shadows on a 13x13 (or smaller, for odd size dimensions only) grid, 
	 * will not work for larger grids.
	 */
	public static int ____H = 0x01; // obscured along the horizontal face
	public static int ___C_ = 0x02; // obscured at the center
	public static int __V__ = 0x04; // obscured along the vertical face
	public static int _N___ = 0x80; // start of new raster

	public static int ___CH = 0x03;
	public static int __VCH = 0x07;
	public static int __VC_ = 0x06;

	public static int _N__H = 0x81;
	public static int _N_CH = 0x83;
	public static int _NVCH = 0x87;
	public static int _NVC_ = 0x86;
	public static int _NV__ = 0x84;
	
    public static final int shadowRaster[][] = {
	        { 6, __VCH, 4, _N_CH, 1, __VCH, 3, _N___, 1, ___CH, 1, __VCH, 1 },    // raster_1_0
	        { 6, __VC_, 1, _NVCH, 2, __VC_, 1, _NVCH, 3, _NVCH, 2, _NVCH, 1 },    // raster_1_1
	        //
	        { 4, __VCH, 3, _N__H, 1, ___CH, 1, __VCH, 1,     0, 0,     0, 0 },    // raster_2_0
	        { 6, __VC_, 2, _N_CH, 1, __VCH, 2, _N_CH, 1, __VCH, 1, _N__H, 1 },    // raster_2_1
	        { 6, __V__, 1, _NVCH, 1, __VC_, 1, _NVCH, 1, __VC_, 1, _NVCH, 1 },    // raster_2_2
	        //
	        { 2, __VCH, 2, _N__H, 2,     0, 0,     0, 0,     0, 0,     0, 0 },    // raster_3_0
	        { 3, __VC_, 2, _N_CH, 1, __VCH, 1,     0, 0,     0, 0,     0, 0 },    // raster_3_1
	        { 3, __VC_, 1, _NVCH, 2, _N_CH, 1,     0, 0,     0, 0,     0, 0 },    // raster_3_2
	        { 3, _NVCH, 1, __V__, 1, _NVCH, 1,     0, 0,     0, 0,     0, 0 },    // raster_3_3
	        //
	        { 2, __VCH, 1, _N__H, 1,     0, 0,     0, 0,     0, 0,     0, 0 },    // raster_4_0
	        { 2, __VC_, 1, _N__H, 1,     0, 0,     0, 0,     0, 0,     0, 0 },    // raster_4_1
	        { 2, __VC_, 1, _N_CH, 1,     0, 0,     0, 0,     0, 0,     0, 0 },    // raster_4_2
	        { 2, __V__, 1, _NVCH, 1,     0, 0,     0, 0,     0, 0,     0, 0 },    // raster_4_3
	        { 2, __V__, 1, _NVCH, 1,     0, 0,     0, 0,     0, 0,     0, 0 }     // raster_4_4
	    };
    
	public class ClasspathFileHandleResolver implements FileHandleResolver {
		public FileHandle resolve (String fileName) {
			return Gdx.files.classpath(fileName);
		}
	}
	
	
	public static final String PARTY_SAV_BASE_FILENAME = "party.sav";
	public static final String MONSTERS_SAV_BASE_FILENAME = "monsters.sav";
	public static final String OUTMONST_SAV_BASE_FILENAME = "outmonst.sav";
	public static final int MONSTERTABLE_SIZE = 32;
	public static final int MONSTERTABLE_CREATURES_SIZE = 8;
	
	
	public enum WeaponType {
		WEAP_HANDS,
		WEAP_STAFF,
		WEAP_DAGGER,
		WEAP_SLING,
		WEAP_MACE,
		WEAP_AXE,
		WEAP_SWORD,
		WEAP_BOW,
		WEAP_CROSSBOW,
		WEAP_OIL,
		WEAP_HALBERD,
		WEAP_MAGICAXE,
		WEAP_MAGICSWORD,
		WEAP_MAGICBOW,
		WEAP_MAGICWAND,
		WEAP_MYSTICSWORD,
		WEAP_MAX;
		public static WeaponType get(int v) {
			for (WeaponType x : values()) {
				if (x.ordinal() == (v&0xff)) {
					return x;
				}
			}
			return null;
		}
	}
	
	public enum ArmorType {
		ARMR_NONE,
		ARMR_CLOTH,
		ARMR_LEATHER,
		ARMR_CHAIN,
		ARMR_PLATE,
		ARMR_MAGICCHAIN,
		ARMR_MAGICPLATE,
		ARMR_MYSTICROBES,
		ARMR_MAX;
		public static ArmorType get(int v) {
			for (ArmorType x : values()) {
				if (x.ordinal() == (v&0xff)) {
					return x;
				}
			}
			return null;
		}
	}
	
	public enum ClassType {
		CLASS_MAGE,
		CLASS_BARD,
		CLASS_FIGHTER,
		CLASS_DRUID,
		CLASS_TINKER,
		CLASS_PALADIN,
		CLASS_RANGER,
		CLASS_SHEPHERD;
		public static ClassType get(int v) {
			for (ClassType x : values()) {
				if (x.ordinal() == (v&0xff)) {
					return x;
				}
			}
			return null;
		}
	}
	
	public enum SexType {
		
		SEX_MALE(0xB),
		SEX_FEMALE(0xC);

		private int b;

		private SexType(int value) {
			b = value;
		}

		public int getValue() {
			return b;
		}
		
		public static SexType get(byte v) {
			for (SexType x : values()) {
				if (x.getValue() == (v&0xff)) {
					return x;
				}
			}
			return null;
		}

	}
	
	public enum StatusType {
		STAT_GOOD('G'),
		STAT_POISONED('P'),
		STAT_SLEEPING('S'),
		STAT_DEAD('D');

		private char intValue;

		private StatusType(char value) {
			intValue = value;
		}

		public char getValue() {
			return intValue;
		}
		
		public static StatusType get(byte v) {
			for (StatusType x : values()) {
				if (x.getValue() == (char)(v&0xff)) {
					return x;
				}
			}
			return null;
		}

	}
	
	public enum Virtue {
		VIRT_HONESTY,
		VIRT_COMPASSION,
		VIRT_VALOR,
		VIRT_JUSTICE,
		VIRT_SACRIFICE,
		VIRT_HONOR,
		VIRT_SPIRITUALITY,
		VIRT_HUMILITY,
		VIRT_MAX;
		public static Virtue get(int v) {
			for (Virtue x : values()) {
				if (x.ordinal() == (v&0xff)) {
					return x;
				}
			}
			return null;
		}
	}
	
	public enum Reagent {
		REAG_ASH,
		REAG_GINSENG,
		REAG_GARLIC,
		REAG_SILK,
		REAG_MOSS,
		REAG_PEARL,
		REAG_NIGHTSHADE,
		REAG_MANDRAKE,
		REAG_MAX;
		
		public static Reagent get(int v) {
			for (Reagent x : values()) {
				if (x.ordinal() == (v&0xff)) {
					return x;
				}
			}
			return null;
		}
	}
	
	public enum BaseVirtue {
		VIRT_NONE(0x00),
		VIRT_TRUTH(0x01),
		VIRT_LOVE(0x02),
		VIRT_COURAGE(0x04);

		private int intValue;

		private BaseVirtue(int value) {
			intValue = value;
		}

		public int getValue() {
			return intValue;
		}
		
		public static BaseVirtue get(int v) {
			for (BaseVirtue x : values()) {
				if (x.getValue() == (v&0xff)) {
					return x;
				}
			}
			return null;
		}

	}
	
	
	public enum Item {
		ITEM_SKULL(0x01),
		ITEM_SKULL_DESTROYED(0x02),
		ITEM_CANDLE(0x04),
		ITEM_BOOK(0x08),
		ITEM_BELL(0x10),
		ITEM_KEY_C(0x20),
		ITEM_KEY_L(0x40),
		ITEM_KEY_T(0x80),
		ITEM_HORN(0x100),
		ITEM_WHEEL(0x200),
		ITEM_CANDLE_USED(0x400),
		ITEM_BOOK_USED(0x800),
		ITEM_BELL_USED(0x1000);

		private int intValue;

		private Item(int value) {
			intValue = value;
		}

		public int getValue() {
			return intValue;
		}
		
		public static Item get(int v) {
			for (Item x : values()) {
				if (x.getValue() == (v&0xff)) {
					return x;
				}
			}
			return null;
		}

	}

	public enum Stone {
		STONE_BLUE(0x01),
		STONE_YELLOW(0x02),
		STONE_RED(0x04),
		STONE_GREEN(0x08),
		STONE_ORANGE(0x10),
		STONE_PURPLE(0x20),
		STONE_WHITE(0x40),
		STONE_BLACK(0x80);

		private int intValue;

		private Stone(int value) {
			intValue = value;
		}

		public int getValue() {
			return intValue;
		}
		
		public static Stone get(int v) {
			for (Stone x : values()) {
				if (x.getValue() == (v&0xff)) {
					return x;
				}
			}
			return null;
		}

	}

	public enum Rune {
		RUNE_HONESTY(0x01),
		RUNE_COMPASSION(0x02),
		RUNE_VALOR(0x04),
		RUNE_JUSTICE(0x08),
		RUNE_SACRIFICE(0x10),
		RUNE_HONOR(0x20),
		RUNE_SPIRITUALITY(0x40),
		RUNE_HUMILITY(0x80);

		private int intValue;

		private Rune(int value) {
			intValue = value;
		}

		public int getValue() {
			return intValue;
		}
		
		public static Rune get(int v) {
			for (Rune x : values()) {
				if (x.getValue() == (v&0xff)) {
					return x;
				}
			}
			return null;
		}

	}

	public static final int SPELL_MAX = 26;
	
	
	public enum KarmaAction {
		KA_FOUND_ITEM,
		KA_STOLE_CHEST,
		KA_GAVE_TO_BEGGAR,
		KA_GAVE_ALL_TO_BEGGAR,
		KA_BRAGGED,
		KA_HUMBLE,
		KA_HAWKWIND,
		KA_MEDITATION,
		KA_BAD_MANTRA,
		KA_ATTACKED_GOOD,
		KA_FLED_EVIL,
		KA_FLED_GOOD,
		KA_HEALTHY_FLED_EVIL,
		KA_KILLED_EVIL,
		KA_SPARED_GOOD,
		KA_DONATED_BLOOD,
		KA_DIDNT_DONATE_BLOOD,
		KA_CHEAT_REAGENTS,
		KA_DIDNT_CHEAT_REAGENTS,
		KA_USED_SKULL,
		KA_DESTROYED_SKULL;
	}

	public enum HealType {
		HT_NONE,
		HT_CURE,
		HT_FULLHEAL,
		HT_RESURRECT,
		HT_HEAL,
		HT_CAMPHEAL,
		HT_INNHEAL;
	}

	public enum InventoryItem {
		INV_NONE,
		INV_WEAPON,
		INV_ARMOR,
		INV_FOOD,
		INV_REAGENT,
		INV_GUILDITEM,
		INV_HORSE;
	}

	public enum CannotJoinError {
		JOIN_SUCCEEDED,
		JOIN_NOT_EXPERIENCED,
		JOIN_NOT_VIRTUOUS;
	}

	public enum EquipError {
		EQUIP_SUCCEEDED,
		EQUIP_NONE_LEFT,
		EQUIP_CLASS_RESTRICTED;
	}
	
	public enum PartyEventType {
		GENERIC,
		LOST_EIGHTH,
		ADVANCED_LEVEL,
		STARVING,
		TRANSPORT_CHANGED,
		PLAYER_KILLED,
		ACTIVE_PLAYER_CHANGED,
		MEMBER_JOINED,
		PARTY_REVIVED,
		INVENTORY_ADDED;
	}

	public enum CreatureAttrib {
		MATTR_STEALFOOD(0x1),
		MATTR_STEALGOLD(0x2),
		MATTR_CASTS_SLEEP(0x4),
		MATTR_UNDEAD(0x8),
		MATTR_GOOD(0x10),
		MATTR_WATER(0x20),
		MATTR_NONATTACKABLE(0x40),
		MATTR_NEGATE(0x80),
		MATTR_CAMOUFLAGE(0x100),
		MATTR_NOATTACK(0x200),
		MATTR_AMBUSHES(0x400),
		MATTR_RANDOMRANGED(0x800),
		MATTR_INCORPOREAL(0x1000),
		MATTR_NOCHEST(0x2000),
		MATTR_DIVIDES(0x4000),
		MATTR_SPAWNSONDEATH(0x8000),
		MATTR_FORCE_OF_NATURE(0x10000);

		private int intValue;

		private CreatureAttrib(int value) {
			intValue = value;
		}

		public int getValue() {
			return intValue;
		}

	}

	public enum CreatureMovementAttrib {
		MATTR_STATIONARY(0x1),
		MATTR_WANDERS(0x2),
		MATTR_SWIMS(0x4),
		MATTR_SAILS(0x8),
		MATTR_FLIES(0x10),
		MATTR_TELEPORT(0x20),
		MATTR_CANMOVECREATURES(0x40),
		MATTR_CANMOVEAVATAR(0x80);

		private int intValue;

		private CreatureMovementAttrib(int value) {
			intValue = value;
		}

		public int getValue() {
			return intValue;
		}
	}

	public enum CreatureStatus {
		FINE,
		DEAD,
		FLEEING,
		CRITICAL,
		HEAVILYWOUNDED,
		LIGHTLYWOUNDED,
		BARELYWOUNDED;
	}
	
	public enum CreatureType {
		HORSE1_ID(0),
		HORSE2_ID(1),

		MAGE_ID(2),
		BARD_ID(3),
		FIGHTER_ID(4),
		DRUID_ID(5),
		TINKER_ID(6),
		PALADIN_ID(7),
		RANGER_ID(8),
		SHEPHERD_ID(9),

		GUARD_ID(10),
		VILLAGER_ID(11),
		SINGINGBARD_ID(12),
		JESTER_ID(13),
		BEGGAR_ID(14),
		CHILD_ID(15),
		BULL_ID(16),
		LORDBRITISH_ID(17),

		PIRATE_ID(18),
		NIXIE_ID(19),
		GIANT_SQUID_ID(20),
		SEA_SERPENT_ID(21),
		SEAHORSE_ID(22),
		WHIRLPOOL_ID(23),
		STORM_ID(24),
		RAT_ID(25),
		BAT_ID(26),
		GIANT_SPIDER_ID(27),
		GHOST_ID(28),
		SLIME_ID(29),
		TROLL_ID(30),
		GREMLIN_ID(31),
		MIMIC_ID(32),
		REAPER_ID(33),
		INSECT_SWARM_ID(34),
		GAZER_ID(35),
		PHANTOM_ID(36),
		ORC_ID(37),
		SKELETON_ID(38),
		ROGUE_ID(39),
		PYTHON_ID(40),
		ETTIN_ID(41),
		HEADLESS_ID(42),
		CYCLOPS_ID(43),
		WISP_ID(44),
		EVILMAGE_ID(45),
		LICH_ID(46),
		LAVA_LIZARD_ID(47),
		ZORN_ID(48),
		DAEMON_ID(49),
		HYDRA_ID(50),
		DRAGON_ID(51),
		BALRON_ID(52);

		private int intValue;

		private CreatureType(int value) {
			intValue = value;
		}

		public int getValue() {
			return intValue;
		}
	}

	public static final int MAX_CREATURES_ON_MAP = 4;
	public static final int MAX_CREATURE_DISTANCE = 16;
	
	
	public enum SlowedType {
		SLOWED_BY_NOTHING,
		SLOWED_BY_TILE,
		SLOWED_BY_WIND;
	}

	public enum MoveResult {
		MOVE_SUCCEEDED(0x0001),
		MOVE_END_TURN(0x0002),
		MOVE_BLOCKED(0x0004),
		MOVE_MAP_CHANGE(0x0008),
		MOVE_TURNED(0x0010), // dungeons and ship movement
		MOVE_DRIFT_ONLY(0x0020), // balloon -- no movement
		MOVE_EXIT_TO_PARENT(0x0040),
		MOVE_SLOWED(0x0080),
		MOVE_MUST_USE_SAME_EXIT(0x0100);

		private int intValue;

		private MoveResult(int value) {
			intValue = value;
		}

		public int getValue() {
			return intValue;
		}

	}

}
