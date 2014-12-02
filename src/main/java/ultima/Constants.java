package ultima;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;


public interface Constants {
	
	public enum Maps {
		NONE(255,"None"),
		WORLD(0,"Brittania"),
		CASTLE_OF_LORD_BRITISH_1(1,"Castle of Lord British"),
		CASTLE_OF_LORD_BRITISH_2(100,"Castle of Lord British"),
		LYCAEUM(2,"Lycaeum"),
		EMPATH_ABBEY(3,"Empath Abbey"),
		SERPENTS_HOLD(4,"Serpents Hold"),
		MOONGLOW(5,"Moonglow"),
		BRITAIN(6,"Britain"),
		JHELOM(7,"Jhelom"),
		YEW(8,"Yew"),
		MINOC(9,"Minoc"),
		TRINSIC(10,"Trinsic"),
		SKARABRAE(11,"Skara Brae"),
		MAGINCIA(12,"Magincia"),
		PAWS(13,"Paws"),
		BUCCANEERS_DEN(14,"Buccaneers Den"),
		VESPER(15,"Vesper"),
		COVE(16,"Cove"),
		DECEIT(17,"Dungeon of Deceit"),
		DESPISE(18,"Dungeon of Despise"),
		DESTARD(19,"Dungeon of Destard"),
		WRONG(20,"Dungeon of Wrong"),
		COVETOUS(21,"Dungeon of Covetous"),
		SHAME(22,"Dungeon of Shame"),
		HYTHLOTH(23,"Dungeon of Hythloth"),
		ABYSS(24,"The Abyss"),
		SHRINE_HONESTY(25,"Shrine of Honesty"),
		SHRINE_COMPASSION(26,"Shrine of Compassion"),
		SHRINE_VALOR(27,"Shrine of Valor"),
		SHRINE_JUSTICE(28,"Shrine of Justice"),
		SHRINE_SACRIFICE(29,"Shrine of Sacrifice"),
		SHRINE_HONOR(30,"Shrine of Honor"),
		SHRINE_SPIRITUALITY(31,"Shrine of Sprituality"),
		SHRINE_HUMILITY(32,"Shrine of Humility"),
		BRICK_CON(33,""),
		BRIDGE_CON(34,""),
		BRUSH_CON(35,""),
		CAMP_CON(36,""),
		DNG0_CON(37,""),
		DNG1_CON(38,""),
		DNG2_CON(39,""),
		DNG3_CON(40,""),
		DNG4_CON(41,""),
		DNG5_CON(42,""),
		DNG6_CON(43,""),
		DUNGEON_CON(44,""),
		FOREST_CON(45,""),
		GRASS_CON(46,""),
		HILL_CON(47,""),
		INN_CON(48,""),
		MARSH_CON(49,""),
		SHIPSEA_CON(50,""),
		SHIPSHIP_CON(51,""),
		SHIPSHOR_CON(52,""),
		SHORE_CON(53,""),
		SHORSHIP_CON(54,""),
		CAMP_DNG(55,"");
		
		private int id;
		private String label;
		
		private Maps(int id, String label) {
			this.id = id;
			this.label = label;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}
		
		public String getLabel() {
			return label;
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
	

	
	
	public static final String PARTY_SAV_BASE_FILENAME = "party.sav";
	public static final String MONSTERS_SAV_BASE_FILENAME = "monsters.sav";
	public static final String OUTMONST_SAV_BASE_FILENAME = "outmonst.sav";
	public static final int MONSTERTABLE_SIZE = 32;
	public static final int MONSTERTABLE_CREATURES_SIZE = 8;
	
	
	public enum WeaponType {
		HANDS,
		STAFF,
		DAGGER,
		SLING,
		MACE,
		AXE,
		SWORD,
		BOW,
		CROSSBOW,
		OIL,
		HALBERD,
		MAGICAXE,
		MAGICSWORD,
		MAGICBOW,
		MAGICWAND,
		MYSTICSWORD;
		
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
		NONE,
		CLOTH,
		LEATHER,
		CHAIN,
		PLATE,
		MAGICCHAIN,
		MAGICPLATE,
		MYSTICROBE;
		
		public static ArmorType get(int v) {
			for (ArmorType x : values()) {
				if (x.ordinal() == (v&0xff)) {
					return x;
				}
			}
			return null;
		}
	}
	

	
	public enum Virtue {
		
		HONESTY("honest", "AHM", 0x01),
		COMPASSION("compassionate", "MU", 0x02),
		VALOR("valiant", "RA", 0x04),
		JUSTICE("just", "BEH", 0x08),
		SACRIFICE("sacrificial", "CAH", 0x10),
		HONOR("honorable", "SUMM", 0x20),
		SPIRITUALITY("spiritual", "OM", 0x40),
		HUMILITY("humble", "LUM", 0x80);
		
		private String description;
		private String mantra;
		private int loc;
		
		private Virtue(String d, String ab, int loc) {
			this.description = d;
			this.mantra = ab;
			this.loc = loc;
		}
		public static Virtue get(int v) {
			for (Virtue x : values()) {
				if (x.ordinal() == (v&0xff)) {
					return x;
				}
			}
			return null;
		}
		public String getDescription() {
			return description;
		}
		public String getMantra() {
			return mantra;
		}
		public int getLoc() {
			return loc;
		}
	}
	
	public enum ClassType {
				
		MAGE(Virtue.HONESTY, WeaponType.STAFF,  ArmorType.CLOTH,   2, 125, 231, 136),
		BARD(Virtue.COMPASSION, WeaponType.SLING,  ArmorType.CLOTH,   3, 240,  83, 105),
		FIGHTER(Virtue.VALOR, WeaponType.AXE,    ArmorType.LEATHER, 3, 205,  35, 221),
		DRUID(Virtue.JUSTICE, WeaponType.DAGGER, ArmorType.CLOTH,   2, 175,  59,  44),
		TINKER(Virtue.SACRIFICE, WeaponType.MACE,   ArmorType.LEATHER, 2, 110, 158,  21),
		PALADIN(Virtue.HONOR, WeaponType.SWORD,  ArmorType.CHAIN,   3, 325, 105, 183),
		RANGER(Virtue.SPIRITUALITY, WeaponType.SWORD,  ArmorType.LEATHER, 2, 150,  23, 129),
		SHEPHERD(Virtue.HUMILITY, WeaponType.STAFF,  ArmorType.CLOTH,   1,   5, 186, 171);
		
		private Virtue virtue;
		private WeaponType initialWeapon;
		private ArmorType initialArmor;
		private int initialLevel;
		private int initialExp;
		private int startX;
		private int startY;

		private ClassType(Virtue virtue, WeaponType initialWeapon, ArmorType initialArmor, int initialLevel, int initialExp, int startX, int startY) {
			this.virtue = virtue;
			this.initialWeapon = initialWeapon;
			this.initialArmor = initialArmor;
			this.initialLevel = initialLevel;
			this.initialExp = initialExp;
			this.startX = startX;
			this.startY = startY;
		}

		public static ClassType get(int v) {
			for (ClassType x : values()) {
				if (x.ordinal() == (v&0xff)) {
					return x;
				}
			}
			return null;
		}

		public Virtue getVirtue() {
			return virtue;
		}

		public WeaponType getInitialWeapon() {
			return initialWeapon;
		}

		public ArmorType getInitialArmor() {
			return initialArmor;
		}

		public int getInitialLevel() {
			return initialLevel;
		}

		public int getInitialExp() {
			return initialExp;
		}

		public int getStartX() {
			return startX;
		}

		public int getStartY() {
			return startY;
		}
		

	}

	
	public enum SexType {
		
		SEX_MALE(0xB,"Male"),
		SEX_FEMALE(0xC, "Female");

		private int b;
		private String desc;
		
		private SexType(int value, String d) {
			b = value;
			desc = d;
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

		public String getDesc() {
			return desc;
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
		
	public enum Reagent {
		ASH,
		GINSENG,
		GARLIC,
		SILK,
		MOSS,
		PEARL,
		NIGHTSHADE,
		MANDRAKE;
		
		public static Reagent get(int v) {
			for (Reagent x : values()) {
				if (x.ordinal() == (v&0xff)) {
					return x;
				}
			}
			return null;
		}
	}
	
	public enum SpellNames {
		
		awaken, blink, cure, dispel, energy, fireball, gate, heal, 
		iceball, jinx, kill, light, magicmissile, negate, open, 
		protection, quickness, resurrect, sleep, tremor, undead, 
		view, winds, xit, yup, zdown;
		
		public static SpellNames get(int i) {
			for (SpellNames x : values()) {
				if (x.ordinal() == i) {
					return x;
				}
			}
			return null;
		}
	}
	
	public enum BaseVirtue {
		NONE(0x00),
		TRUTH(0x01),
		LOVE(0x02),
		COURAGE(0x04);

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
		SKULL("Skull of Mondain", true, 0x01),
		SKULL_DESTROYED("",false, 0x02),
		CANDLE("Candle of Love", true, 0x04),
		BOOK("Book of Truth",true, 0x08),
		BELL("Bell of Courage",true, 0x10),
		KEY_C("Key of Courage",true, 0x20),
		KEY_L("Key of Love",true, 0x40),
		KEY_T("Key of Truth",true, 0x80),
		HORN("Silver Horn",true, 0x100),
		WHEEL("Wheel of HMS Cape",true, 0x200),
		CANDLE_USED("",false, 0x400),
		BOOK_USED("",false, 0x800),
		BELL_USED("",false, 0x1000);
		
		private boolean visible;
		private String desc;
		private int loc;
		private Item(String desc, boolean v, int loc) {
			this.desc = desc;
			this.visible = v;
			this.loc = loc;
		}
		
		public static Item get(int v) {
			for (Item x : values()) {
				if (x.ordinal() == (v&0xff)) {
					return x;
				}
			}
			return null;
		}
		public boolean isVisible() {
			return this.visible;
		}
		public String getDesc() {
			return this.desc;
		}
		public int getLoc() {
			return loc;
		}

	}
	
	public enum ItemMapLabels {
		
		balloon("the Ballon"),
		lockelake("Locke Lake"),
		telescope("a telescope"),
		
		mandrake1("Mandrake Root"),
		mandrake2("Mandrake Root"),
		nightshade1("Nightshade"),
		nightshade2("Nightshade"),
		
		bell("the Bell of Courage"),
		horn("the Silver Horn"),
		wheel("the Wheel from the H.M.S. Cape"),
		skull("the Skull of Modain the Wizard"),
		candle("the Candle of Love"),
		book("the Book of Truth"),
				
		mysticarmor("Mystic Robes"),
		mysticswords("Mystic Swords"),
		
		honestyrune("the rune of Honesty"),
		compassionrune("the rune of Compassion"),
		valorrune("the rune of Valor"),
		justicerune("the rune of Justice"),
		sacrificerune("the rune of Sacrifice"),
		honorrune("the rune of Honor"),
		humilityrune("the rune of Humility"),
		spiritualityrune("the rune of Spirituality"),

		blackstone("the Black Stone"),
		whitestone("the White Stone"),
		bluestone("the Blue Stone"),
		yellowstone("the Yellow Stone"),
		redstone("the Red Stone"),
		greenstone("the Green Stone"),
		orangestone("the Orange Stone"),
		purplestone("the Purple Stone");
		
		private String desc;

		private ItemMapLabels(String desc) {
			this.desc = desc;
		}
		public String getDesc() {
			return this.desc;
		}
	}
	
	
	public enum GuildItemType {
		gem,
		key,
		sextant,
		torch;
	}

	public enum Stone {
		BLUE(0x01),
		YELLOW(0x02),
		RED(0x04),
		GREEN(0x08),
		ORANGE(0x10),
		PURPLE(0x20),
		WHITE(0x40),
		BLACK(0x80);
		private int loc;
		
		private Stone(int loc) {
			this.loc = loc;
		}

		public static Stone get(int v) {
			for (Stone x : values()) {
				if (x.ordinal() == (v&0xff)) {
					return x;
				}
			}
			return null;
		}
		
		public int getLoc() {
			return loc;
		}
	}


	public static final int SPELL_MAX = 26;
	
	
	public enum KarmaAction {
		FOUND_ITEM,
		STOLE_CHEST,
		GAVE_TO_BEGGAR,
		GAVE_ALL_TO_BEGGAR,
		BRAGGED,
		HUMBLE,
		HAWKWIND,
		MEDITATION,
		BAD_MANTRA,
		ATTACKED_GOOD,
		FLED_EVIL,
		FLED_GOOD,
		HEALTHY_FLED_EVIL,
		KILLED_EVIL,
		SPARED_GOOD,
		DONATED_BLOOD,
		DIDNT_DONATE_BLOOD,
		CHEAT_REAGENTS,
		DIDNT_CHEAT_REAGENTS,
		USED_SKULL,
		DESTROYED_SKULL;
	}

	public enum HealType {
		NONE,
		CURE,
		FULLHEAL,
		RESURRECT,
		HEAL,
		CAMPHEAL,
		INNHEAL;
	}

	public enum InventoryType {
		WEAPON,
		ARMOR,
		FOOD,
		REAGENT,
		GUILDITEM,
		INN,
		TAVERN,
		HEALER,
		TAVERNINFO,
		HORSE;
	}

	public enum CannotJoinError {
		JOIN_SUCCEEDED,
		JOIN_NOT_EXPERIENCED,
		JOIN_NOT_VIRTUOUS;
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
	
	public class ClasspathFileHandleResolver implements FileHandleResolver {
		public FileHandle resolve(String fileName) {
			return Gdx.files.classpath(fileName);
		}
	}


}
