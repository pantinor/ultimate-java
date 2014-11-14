package ultima;

public interface Constants {
	
	public enum Maps {
		NONE(255), WORLD(0), CASTLE_OF_LORD_BRITISH(1), LYCAEUM(2), EMPATH_ABBEY(3), SERPENTS_HOLD(4), MOONGLOW(5), BRITAIN(6), JHELOM(7), YEW(8), MINOC(9), TRINSIC(10), SKARABRAE(11), MAGINCIA(12), PAWS(13), BUCCANEERS_DEN(14), VESPER(15), COVE(16), DECEIT(
				17), DESPISE(18), DESTARD(19), WRONG(20), COVETOUS(21), SHAME(22), HYTHLOTH(23), ABYSS(24), SHRINE_HONESTY(25), SHRINE_COMPASSION(26), SHRINE_VALOR(27), SHRINE_JUSTICE(28), SHRINE_SACRIFICE(29), SHRINE_HONOR(30), SHRINE_SPIRITUALITY(
				31), SHRINE_HUMILITY(32), BRICK_CON(33), BRIDGE_CON(34), BRUSH_CON(35), CAMP_CON(36), DNG0_CON(37), DNG1_CON(38), DNG2_CON(39), DNG3_CON(40), DNG4_CON(41), DNG5_CON(42), DNG6_CON(43), DUNGEON_CON(44), FOREST_CON(45), GRASS_CON(46), HILL_CON(
				47), INN_CON(48), MARSH_CON(49), SHIPSEA_CON(50), SHIPSHIP_CON(51), SHIPSHOR_CON(52), SHORE_CON(53), SHORSHIP_CON(54), CAMP_DNG(55);
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
		MOVEMENT_FIXED,
		MOVEMENT_WANDER,
		MOVEMENT_FOLLOW_AVATAR,
		MOVEMENT_ATTACK_AVATAR;
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
	    NONE,
	    WEST,
	    NORTH,
	    EAST,
	    SOUTH,
	    ADVANCE,
	    RETREAT
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


}
