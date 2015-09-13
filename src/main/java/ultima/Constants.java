package ultima;

import java.util.Random;

import objects.Armor;
import objects.BaseMap;
import objects.Creature;
import objects.Drawable;
import objects.Weapon;

import org.apache.commons.lang3.StringUtils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import java.io.File;

public interface Constants {

    public enum ScreenType {

        MAIN, COMBAT, DUNGEON, SHRINE, CODEX, RANDOMDNG, TMXDUNGEON;
    }

    public static int tilePixelWidth = 32;
    public static int tilePixelHeight = 32;

    public enum Maps {

        NONE(255, "None"),
        WORLD(0, "Brittania"),
        CASTLE_OF_LORD_BRITISH_1(1, "Castle"),
        CASTLE_OF_LORD_BRITISH_2(100, "Castle"),
        LYCAEUM(2, "Lycaeum"),
        EMPATH_ABBEY(3, "Empath Abbey"),
        SERPENTS_HOLD(4, "Serpents Hold"),
        MOONGLOW(5, "Moonglow"),
        BRITAIN(6, "Britain"),
        JHELOM(7, "Jhelom"),
        YEW(8, "Yew"),
        MINOC(9, "Minoc"),
        TRINSIC(10, "Trinsic"),
        SKARABRAE(11, "Skara Brae"),
        MAGINCIA(12, "Magincia"),
        PAWS(13, "Paws"),
        BUCCANEERS_DEN(14, "Buccaneers Den"),
        VESPER(15, "Vesper"),
        COVE(16, "Cove"),
        DECEIT(17, "Dungeon of Deceit"),
        DESPISE(18, "Dungeon of Despise"),
        DESTARD(19, "Dungeon of Destard"),
        WRONG(20, "Dungeon of Wrong"),
        COVETOUS(21, "Dungeon of Covetous"),
        SHAME(22, "Dungeon of Shame"),
        HYTHLOTH(23, "Dungeon of Hythloth"),
        ABYSS(24, "The Abyss"),
        SHRINE_HONESTY(25, "Shrine of Honesty"),
        SHRINE_COMPASSION(26, "Shrine of Compassion"),
        SHRINE_VALOR(27, "Shrine of Valor"),
        SHRINE_JUSTICE(28, "Shrine of Justice"),
        SHRINE_SACRIFICE(29, "Shrine of Sacrifice"),
        SHRINE_HONOR(30, "Shrine of Honor"),
        SHRINE_SPIRITUALITY(31, "Shrine of Sprituality"),
        SHRINE_HUMILITY(32, "Shrine of Humility"),
        BRICK_CON(33, ""),
        BRIDGE_CON(34, ""),
        BRUSH_CON(35, ""),
        CAMP_CON(36, ""),
        DNG0_CON(37, ""),
        DNG1_CON(38, ""),
        DNG2_CON(39, ""),
        DNG3_CON(40, ""),
        DNG4_CON(41, ""),
        DNG5_CON(42, ""),
        DNG6_CON(43, ""),
        DUNGEON_CON(44, ""),
        FOREST_CON(45, ""),
        GRASS_CON(46, ""),
        HILL_CON(47, ""),
        INN_CON(48, ""),
        MARSH_CON(49, ""),
        SHIPSEA_CON(50, ""),
        SHIPSHIP_CON(51, ""),
        SHIPSHOR_CON(52, ""),
        SHORE_CON(53, ""),
        SHORSHIP_CON(54, ""),
        CAMP_DNG(55, ""),
        DELVE_SORROWS(56, "The Delve of Sorrows!"),
        CASTLE_OF_LORD_BRITISH_3(101, "Castle"),
        AQUARIA_1(57, "Aquaria"),
        AQUARIA_2(58, "Aquaria"),
        BLACK_ROCK(59, "Black Rock"),
        BODANIA(60, "Bodania"),
        THORANGUARD_1(61, "Thoranguard"),
        THORANGUARD_2(62, "Thoranguard"),
        DEVILS_GUARD_1(63, "Devil's Guard"),
        DEVILS_GUARD_2(64, "Devil's Guard");
        
        private int id;
        private String label;
        private BaseMap baseMap;

        private Maps(int id, String label) {
            this.id = id;
            this.label = label;
        }

        public int getId() {
            return id;
        }

        public String getLabel() {
            return label;
        }

        public static Maps get(int id) {
            for (Maps m : Maps.values()) {
                if (m.getId() == id) {
                    return m;
                }
            }
            return null;
        }

        public BaseMap getMap() {
            return baseMap;
        }

        public void setMap(BaseMap baseMap) {
            this.baseMap = baseMap;
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

    public enum SlowedType {

        SLOWED_BY_NOTHING,
        SLOWED_BY_TILE,
        SLOWED_BY_WIND;
    };

    public enum TileSpeed {

        FAST,
        SLOW,
        VSLOW,
        VVSLOW;
    }

    public enum TileEffect {

        NONE,
        FIRE,
        SLEEP,
        POISON,
        POISONFIELD,
        ELECTRICITY,
        LAVA;
    }

    public enum TileAnimationStyle {

        NONE,
        SCROLL,
        CAMPFIRE,
        CITYFLAG,
        CASTLEFLAG,
        SHIPFLAG,
        LCBFLAG,
        FRAMES;
    }

    public enum TileAttrib {

        unwalkable(0x1),
        creatureunwalkable(0x2),
        swimmable(0x4),
        sailable(0x8),
        unflyable(0x10),
        canattackover(0x20),
        canlandballoon(0x40),
        chest(0x80),
        dispelable(0x100),
        door(0x200),
        horse(0x400),
        ship(0x800),
        balloon(0x1000),
        replacement(0x2000),
        onWaterOnlyReplacement(0x4000),
        livingthing(0x8000),
        lockeddoor(0x10000),
        secretdoor(0x20000);
        private int val;

        private TileAttrib(int val) {
            this.val = val;
        }

        public int getVal() {
            return val;
        }
    }

    public enum TileRule {

        none(0),
        water(TileAttrib.unwalkable.getVal() | TileAttrib.swimmable.getVal() | TileAttrib.sailable.getVal() | TileAttrib.onWaterOnlyReplacement.getVal()),
        shallows(TileAttrib.unwalkable.getVal() | TileAttrib.swimmable.getVal() | TileAttrib.onWaterOnlyReplacement.getVal()),
        swamp(0, TileSpeed.SLOW, TileEffect.POISON, TileAnimationStyle.NONE),
        grass(TileAttrib.canlandballoon.getVal() | TileAttrib.replacement.getVal()),
        brush(0, TileSpeed.VSLOW, TileEffect.NONE, TileAnimationStyle.NONE),
        hills(0, TileSpeed.VVSLOW, TileEffect.NONE, TileAnimationStyle.NONE),
        mountains(TileAttrib.unwalkable.getVal() | TileAttrib.unflyable.getVal() | TileAttrib.creatureunwalkable.getVal()),
        lcb(TileAttrib.unwalkable.getVal() | TileAttrib.creatureunwalkable.getVal()),
        lcb_entrance(TileAttrib.creatureunwalkable.getVal()),
        ship(TileAttrib.ship.getVal() | TileAttrib.creatureunwalkable.getVal()),
        horse(TileAttrib.horse.getVal() | TileAttrib.creatureunwalkable.getVal()),
        floors(TileAttrib.replacement.getVal()),
        balloon(TileAttrib.balloon.getVal() | TileAttrib.creatureunwalkable.getVal()),
        person(TileAttrib.livingthing.getVal() | TileAttrib.unwalkable.getVal()),
        solid(TileAttrib.unwalkable.getVal()),
        solid_attackover(TileAttrib.canattackover.getVal() | TileAttrib.unwalkable.getVal()),
        walls(TileAttrib.unwalkable.getVal() | TileAttrib.canattackover.getVal() | TileAttrib.unflyable.getVal()),
        locked_door(TileAttrib.unwalkable.getVal() | TileAttrib.lockeddoor.getVal()),
        door(TileAttrib.unwalkable.getVal() | TileAttrib.door.getVal()),
        secret_door(TileAttrib.secretdoor.getVal()),
        chest(TileAttrib.chest.getVal()),
        poison_field(TileAttrib.dispelable.getVal(), TileSpeed.FAST, TileEffect.POISON, TileAnimationStyle.NONE),
        energy_field(TileAttrib.dispelable.getVal() | TileAttrib.unflyable.getVal() | TileAttrib.unwalkable.getVal() | TileAttrib.creatureunwalkable.getVal(), TileSpeed.FAST, TileEffect.ELECTRICITY, TileAnimationStyle.NONE),
        fire_field(TileAttrib.dispelable.getVal(), TileSpeed.VVSLOW, TileEffect.FIRE, TileAnimationStyle.NONE),
        sleep_field(TileAttrib.dispelable.getVal(), TileSpeed.FAST, TileEffect.SLEEP, TileAnimationStyle.NONE),
        lava(TileAttrib.replacement.getVal(), TileSpeed.FAST, TileEffect.LAVA, TileAnimationStyle.NONE),
        signs(TileAttrib.unwalkable.getVal() | TileAttrib.unflyable.getVal()),
        spacers(TileAttrib.unwalkable.getVal()),
        monster(TileAttrib.livingthing.getVal() & TileAttrib.unwalkable.getVal());

        private int attribs = 0;
        private TileSpeed speed = TileSpeed.FAST;
        private TileEffect effect = TileEffect.NONE;
        private TileAnimationStyle animStyle = TileAnimationStyle.NONE;

        private TileRule(int attribs, TileSpeed speed, TileEffect effect, TileAnimationStyle animStyle) {
            this.attribs = attribs;
            this.speed = speed;
            this.effect = effect;
            this.animStyle = animStyle;
        }

        private TileRule(int attribs) {
            this.attribs = attribs;
        }

        public boolean has(TileAttrib attrib) {
            return (attrib.getVal() & attribs) > 0;
        }

        public int getAttribs() {
            return attribs;
        }

        public TileSpeed getSpeed() {
            return speed;
        }

        public TileEffect getEffect() {
            return effect;
        }

        public TileAnimationStyle getAnimStyle() {
            return animStyle;
        }

    }

    public enum MapType {

        world(0x1),
        combat(0x2),
        city(0x4),
        dungeon(0x8),
        shrine(0x10);

        private int val;

        private MapType(int val) {
            this.val = val;
        }

        public int getVal() {
            return val;
        }

    }

    public enum MapBorderBehavior {

        wrap,
        exit,
        fixed;
    }

    public enum HeadingDirection {

        NORTH(0),
        NORTH_EAST(2),
        EAST(3),
        SOUTH_EAST(4),
        SOUTH(5),
        SOUTH_WEST(6),
        WEST(7),
        NORTH_WEST(8);
        private int heading;

        private HeadingDirection(int heading) {
            this.heading = heading;
        }

        public int getHeading() {
            return heading;
        }

        public static HeadingDirection getByValue(int val) {
            HeadingDirection ret = HeadingDirection.NORTH;
            for (HeadingDirection d : HeadingDirection.values()) {
                if (val == d.getHeading()) {
                    ret = d;
                    break;
                }
            }
            return ret;
        }

        /**
         * Use Y Down coordinate system with atan2, so negated the values to the
         * function.
         *
         * @param dx delta of the x coords
         * @param dy delta of the y coords
         */
        public static HeadingDirection getDirection(float dx, float dy) {
            double theta = MathUtils.atan2(-dy, -dx);
            double ang = theta * MathUtils.radDeg;
            if (ang < 0) {
                ang = 360 + ang;
            }
            ang = (ang + 90 + 45 + 22.5f) % 360;
            ang /= 45f;
            return HeadingDirection.getByValue((int) ang);
        }
    }

    public static int MOON_PHASES = 24;
    public static int MOON_SECONDS_PER_PHASE = 4;
    public static int MOON_CHAR = 20;

    enum Direction {

        WEST(1, 0x1),
        NORTH(2, 0x2),
        EAST(3, 0x4),
        SOUTH(4, 0x8);

        private int val;
        private int mask;

        private Direction(int v, int mask) {
            this.val = v;
            this.mask = mask;
        }

        public int getVal() {
            return val;
        }

        public int getMask() {
            return mask;
        }

        public static boolean isDirInMask(Direction dir, int mask) {
            int v = (mask & dir.mask);
            return (v > 0);
        }

        public static boolean isDirInMask(int dir, int mask) {
            int v = (mask & dir);
            return (v > 0);
        }

        public static int addToMask(Direction dir, int mask) {
            return (dir.mask | mask);
        }

        public static int removeFromMask(int mask, Direction... dirs) {
            for (Direction dir : dirs) {
                mask &= ~dir.getMask();
            }
            return mask;
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
            if (n == 0) {
                return null;
            }
            int rand = new Random().nextInt(n);
            return d[rand];
        }

        public static Direction reverse(Direction dir) {
            switch (dir) {
                case WEST:
                    return EAST;
                case NORTH:
                    return SOUTH;
                case EAST:
                    return WEST;
                case SOUTH:
                    return NORTH;
            }
            return null;
        }

        public static Direction getByValue(int val) {
            Direction ret = null;
            for (Direction d : Direction.values()) {
                if (val == d.getVal()) {
                    ret = d;
                    break;
                }
            }
            return ret;
        }

        public static Direction getByMask(int mask) {
            Direction ret = null;
            for (Direction d : Direction.values()) {
                if (mask == d.mask) {
                    ret = d;
                    break;
                }
            }
            return ret;
        }

        public static int getBroadsidesDirectionMask(Direction dir) {
            int mask = 0;
            switch (dir) {
                case EAST:
                case WEST:
                    mask = (Direction.NORTH.mask | Direction.SOUTH.mask);
                    break;
                case NORTH:
                case SOUTH:
                    mask = (Direction.EAST.mask | Direction.WEST.mask);
                    break;
            }
            return mask;
        }

        public static Direction goBroadsides(int broadsidesMask) {
            Direction ret = null;
            for (Direction d : Direction.values()) {
                if ((broadsidesMask & d.mask) > 0) {
                    ret = d;
                    break;
                }
            }
            return ret;
        }

    };

    public enum PortalTriggerAction {

        NONE(0x0),
        ENTER(0x1),
        KLIMB(0x2),
        DESCEND(0x4),
        EXIT_NORTH(0x8),
        EXIT_EAST(0x10),
        EXIT_SOUTH(0x20),
        EXIT_WEST(0x40);
        private int intValue;

        private PortalTriggerAction(int i) {
            this.intValue = i;
        }

        public int getIntValue() {
            return intValue;
        }
    }

    public enum TransportContext {

        FOOT(0x1),
        HORSE(0x2),
        SHIP(0x4),
        BALLOON(0x8),
        FOOT_OR_HORSE(0x1 | 0x2),
        ANY(0xffff);

        private int intValue;

        private TransportContext(int intValue) {
            this.intValue = intValue;
        }

        public int getIntValue() {
            return intValue;
        }
    }

    public enum DungeonTile {

        NOTHING(0x00, "Nothing", "brick_floor", true),
        FLOOR(0x00, "Floor", "brick_floor", true),
        WATER(0x00, "Water", "water", true),
        CEILING(0x00, "Ceiling", "none", false),
        LADDER_UP(0x10, "Ladder Up", "up_ladder", true, Maps.DNG1_CON),
        LADDER_DOWN(0x20, "Ladder Down", "down_ladder", true, Maps.DNG2_CON),
        LADDER_UP_DOWN(0x30, "Ladder Up & Down", "down_ladder", true, Maps.DNG3_CON),
        CEILING_HOLE(0x50, "Ceiling Hole", "solid", false),
        FLOOR_HOLE(0x60, "Floor Hole", "solid", false),
        ORB(0x70, "Magic Orb", "magic_flash", true),
        LIGHT(0, "Light", "miss_flash", true),
        ALTAR(0, "Altar", "altar", true),
        CHEST(0x40, "Treasure Chest", "chest", true),
        FIRE(0x40, "Fireplace", "campfire", true),
        WIND_TRAP(0x80, "Winds/Darknes Trap", "hit_flash", true),
        ROCK_TRAP(0x81, "Falling Rock Trap", "hit_flash", true),
        PIT_TRAP(0x8E, "Pit Trap", "hit_flash", true),
        FOUNTAIN_PLAIN(0x90, "Plain Fountain", "Z", true),
        FOUNTAIN_HEAL(0x91, "Healing Fountain", "H", true),
        FOUNTAIN_ACID(0x92, "Acid Fountain", "A", true),
        FOUNTAIN_CURE(0x93, "Cure Fountain", "C", true),
        FOUNTAIN_POISON(0x94, "Poison Fountain", "P", true),
        FIELD_POISON(0xA0, "Poison Field", "poison_field", false),
        FIELD_ENERGY(0xA1, "Energy Field", "energy_field", false),
        FIELD_FIRE(0xA2, "Fire Field", "fire_field", false),
        FIELD_SLEEP(0xA3, "Sleep Field", "sleep_field", false),
        DOOR(0xC0, "Door", "door", true, Maps.DNG5_CON),
        LOCKED_DOOR(0, "Locked Door", "locked_door", false, Maps.DNG5_CON),
        SECRET_DOOR(0xE0, "Secret Door", "secret_door", false, Maps.DNG6_CON),
        ROOM_1(0xD0, "Dungeon Room 1", "spacer_square", true),
        ROOM_2(0xD1, "Dungeon Room 2", "spacer_square", true),
        ROOM_3(0xD2, "Dungeon Room 3", "spacer_square", true),
        ROOM_4(0xD3, "Dungeon Room 4", "spacer_square", true),
        ROOM_5(0xD4, "Dungeon Room 5", "spacer_square", true),
        ROOM_6(0xD5, "Dungeon Room 6", "spacer_square", true),
        ROOM_7(0xD6, "Dungeon Room 7", "spacer_square", true),
        ROOM_8(0xD7, "Dungeon Room 8", "spacer_square", true),
        ROOM_9(0xD8, "Dungeon Room 9", "spacer_square", true),
        ROOM_10(0xD9, "Dungeon Room 10", "spacer_square", true),
        ROOM_11(0xDA, "Dungeon Room 11", "spacer_square", true),
        ROOM_12(0xDB, "Dungeon Room 12", "spacer_square", true),
        ROOM_13(0xDC, "Dungeon Room 13", "spacer_square", true),
        ROOM_14(0xDD, "Dungeon Room 14", "spacer_square", true),
        ROOM_15(0xDE, "Dungeon Room 15", "spacer_square", true),
        ROOM_16(0xDF, "Dungeon Room 16", "spacer_square", true),
        COLUMN(0, "Column", "column", false),
        ROCKS(0, "Rocks", "rocks", false),
        WALL(0xF0, "Wall ", "brick_wall", false);

        private int value;
        private String type;
        private String tileName;
        private Maps combatMap = Maps.DNG0_CON;
        private boolean creatureWalkable;

        private DungeonTile(int value, String type, String tileName, boolean cw) {
            this.value = value;
            this.type = type;
            this.tileName = tileName;
            this.creatureWalkable = cw;
        }

        private DungeonTile(int value, String type, String tileName, boolean cw, Maps combatMap) {
            this.value = value;
            this.type = type;
            this.tileName = tileName;
            this.combatMap = combatMap;
            this.creatureWalkable = cw;
        }

        public int getValue() {
            return value;
        }

        public boolean getCreatureWalkable() {
            return creatureWalkable;
        }

        public String getType() {
            return type;
        }

        public String getTileName() {
            return tileName;
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

        public static DungeonTile getTileByName(String name) {
            DungeonTile ret = null;
            for (DungeonTile d : DungeonTile.values()) {
                if (StringUtils.equals(name, d.getTileName())) {
                    ret = d;
                    break;
                }
            }
            return ret;
        }

        public Maps getCombatMap() {
            return combatMap;
        }

        public void setCombatMap(Maps combatMap) {
            this.combatMap = combatMap;
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

        private Weapon weapon;

        public static WeaponType get(int v) {
            for (WeaponType x : values()) {
                if (x.ordinal() == (v & 0xff)) {
                    return x;
                }
            }
            return null;
        }

        public Weapon getWeapon() {
            return weapon;
        }

        public void setWeapon(Weapon weapon) {
            this.weapon = weapon;
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

        private Armor armor;

        public static ArmorType get(int v) {
            for (ArmorType x : values()) {
                if (x.ordinal() == (v & 0xff)) {
                    return x;
                }
            }
            return null;
        }

        public Armor getArmor() {
            return armor;
        }

        public void setArmor(Armor armor) {
            this.armor = armor;
        }
    }

    public enum Virtue {

        HONESTY("HSTY", "honest", "AHM", 0x01, Color.BLUE, "Truth"),
        COMPASSION("COMP", "compassionate", "MU", 0x02, Color.YELLOW, "Love"),
        VALOR("VALO", "valiant", "RA", 0x04, Color.RED, "Courage"),
        JUSTICE("JUST", "just", "BEH", 0x08, Color.GREEN, "Truth and Love"),
        SACRIFICE("SACR", "sacrificial", "CAH", 0x10, Color.ORANGE, "Love and Courage"),
        HONOR("HONR", "honorable", "SUMM", 0x20, Color.PURPLE, "Courage and Truth"),
        SPIRITUALITY("SPIR", "spiritual", "OM", 0x40, Color.WHITE, "Truth, Love and Courage"),
        HUMILITY("HUMI", "humble", "LUM", 0x80, Color.BLACK, "");

        private String description;
        private String abbr;
        private String mantra;
        private int loc;
        private Color color;
        private String baseVirtues;

        private Virtue(String abbr, String d, String ab, int loc, Color color, String bv) {
            this.abbr = abbr;
            this.description = d;
            this.mantra = ab;
            this.loc = loc;
            this.color = color;
            this.baseVirtues = bv;
        }

        public static Virtue get(int v) {
            for (Virtue x : values()) {
                if (x.ordinal() == (v & 0xff)) {
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

        public String getAbbr() {
            return abbr;
        }

        public Color getColor() {
            return this.color;
        }

        public String getBaseVirtues() {
            return this.baseVirtues;
        }
    }

    public enum ClassType {

        MAGE(Virtue.HONESTY, WeaponType.STAFF, ArmorType.CLOTH, 2, 125, 231, 136),
        BARD(Virtue.COMPASSION, WeaponType.SLING, ArmorType.CLOTH, 3, 240, 83, 105),
        FIGHTER(Virtue.VALOR, WeaponType.AXE, ArmorType.LEATHER, 3, 205, 35, 221),
        DRUID(Virtue.JUSTICE, WeaponType.DAGGER, ArmorType.CLOTH, 2, 175, 59, 44),
        TINKER(Virtue.SACRIFICE, WeaponType.MACE, ArmorType.LEATHER, 2, 110, 158, 21),
        PALADIN(Virtue.HONOR, WeaponType.SWORD, ArmorType.CHAIN, 3, 325, 105, 183),
        RANGER(Virtue.SPIRITUALITY, WeaponType.SWORD, ArmorType.LEATHER, 2, 150, 23, 129),
        SHEPHERD(Virtue.HUMILITY, WeaponType.STAFF, ArmorType.CLOTH, 1, 5, 186, 171);

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
                if (x.ordinal() == (v & 0xff)) {
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

    public enum NpcDefaults {

        Mariah(9, 12, 20, SexType.FEMALE, ClassType.MAGE),
        Iolo(16, 19, 13, SexType.MALE, ClassType.BARD),
        Geoffrey(20, 15, 11, SexType.MALE, ClassType.FIGHTER),
        Jaana(17, 16, 13, SexType.FEMALE, ClassType.DRUID),
        Julia(15, 16, 12, SexType.FEMALE, ClassType.TINKER),
        Dupre(17, 14, 17, SexType.MALE, ClassType.PALADIN),
        Shamino(16, 15, 15, SexType.MALE, ClassType.RANGER),
        Katrina(11, 12, 10, SexType.FEMALE, ClassType.SHEPHERD);

        private int str;
        private int dex;
        private int intell;
        private SexType sex;
        private ClassType klass;

        private NpcDefaults(int str, int dex, int intell, SexType sex, ClassType klass) {
            this.str = str;
            this.dex = dex;
            this.intell = intell;
            this.sex = sex;
            this.klass = klass;
        }

        public int getStr() {
            return str;
        }

        public int getDex() {
            return dex;
        }

        public int getIntell() {
            return intell;
        }

        public SexType getSex() {
            return sex;
        }

        public ClassType getKlass() {
            return klass;
        }

        public void setStr(int str) {
            this.str = str;
        }

        public void setDex(int dex) {
            this.dex = dex;
        }

        public void setIntell(int intell) {
            this.intell = intell;
        }

        public void setSex(SexType sex) {
            this.sex = sex;
        }

        public void setKlass(ClassType klass) {
            this.klass = klass;
        }

        public static NpcDefaults get(int v) {
            for (NpcDefaults x : values()) {
                if (x.ordinal() == (v & 0xff)) {
                    return x;
                }
            }
            return null;
        }

    }

    public static final int STATS_NONE = 0;
    public static final int STATS_PLAYER1 = 1;
    public static final int STATS_PLAYER2 = 2;
    public static final int STATS_PLAYER3 = 3;
    public static final int STATS_PLAYER4 = 4;
    public static final int STATS_PLAYER5 = 5;
    public static final int STATS_PLAYER6 = 6;
    public static final int STATS_PLAYER7 = 7;
    public static final int STATS_PLAYER8 = 8;
    public static final int STATS_WEAPONS = 9;
    public static final int STATS_ARMOR = 10;
    public static final int STATS_ITEMS = 11;
    public static final int STATS_REAGENTS = 12;
    public static final int STATS_SPELLS = 13;

    //for touching orbs
    public static final int STATSBONUS_INT = 0x1;
    public static final int STATSBONUS_DEX = 0x2;
    public static final int STATSBONUS_STR = 0x4;

    public enum SexType {

        MALE(0xB, "Male"),
        FEMALE(0xC, "Female");
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
                if (x.getValue() == (v & 0xff)) {
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

        GOOD('G'),
        POISONED('P'),
        SLEEPING('S'),
        DEAD('D');
        private char intValue;

        private StatusType(char value) {
            intValue = value;
        }

        public char getValue() {
            return intValue;
        }

        public static StatusType get(byte v) {
            for (StatusType x : values()) {
                if (x.getValue() == (char) (v & 0xff)) {
                    return x;
                }
            }
            return null;
        }
    }

    public enum Reagent {

        ASH("Sulfurous Ash", 1 << 0),
        GINSENG("Ginseng", 1 << 1),
        GARLIC("Garlic", 1 << 2),
        SILK("Spider Silk", 1 << 3),
        MOSS("Blood Moss", 1 << 4),
        PEARL("Black Pearl", 1 << 5),
        NIGHTSHADE("Nightshade", 1 << 6),
        MANDRAKE("Mandrake Root", 1 << 7);

        String desc;
        int mask;

        private Reagent(String desc, int mask) {
            this.mask = mask;
            this.desc = desc;
        }

        public static Reagent get(int v) {
            for (Reagent x : values()) {
                if (x.ordinal() == (v & 0xff)) {
                    return x;
                }
            }
            return null;
        }

        public int getMask() {
            return this.mask;
        }

        public String getDesc() {
            return this.desc;
        }
    }

    public enum Spell {

        AWAKEN("Awaken", Reagent.GINSENG.getMask() | Reagent.GARLIC.getMask(), 5),
        BLINK("Blink", Reagent.SILK.getMask() | Reagent.MOSS.getMask(), 15),
        CURE("Cure", Reagent.GINSENG.getMask() | Reagent.GARLIC.getMask(), 5),
        DISPEL("Dispell", Reagent.ASH.getMask() | Reagent.GARLIC.getMask() | Reagent.PEARL.getMask(), 20),
        ENERGY("Energy Field", Reagent.ASH.getMask() | Reagent.SILK.getMask() | Reagent.PEARL.getMask(), 10),
        FIREBALL("Fireball", Reagent.ASH.getMask() | Reagent.PEARL.getMask(), 15),
        GATE("Gate", Reagent.ASH.getMask() | Reagent.PEARL.getMask() | Reagent.MANDRAKE.getMask(), 40),
        HEAL("Heal", Reagent.GINSENG.getMask() | Reagent.SILK.getMask(), 10),
        ICEBALL("Iceball", Reagent.PEARL.getMask() | Reagent.MANDRAKE.getMask(), 20),
        JINX("Jinx", Reagent.PEARL.getMask() | Reagent.NIGHTSHADE.getMask() | Reagent.MANDRAKE.getMask(), 30),
        KILL("Kill", Reagent.PEARL.getMask() | Reagent.NIGHTSHADE.getMask(), 25),
        LIGHT("Light", Reagent.ASH.getMask(), 5),
        MAGICMISSILE("Magic missile", Reagent.ASH.getMask() | Reagent.PEARL.getMask(), 5),
        NEGATE("Negate", Reagent.ASH.getMask() | Reagent.GARLIC.getMask() | Reagent.MANDRAKE.getMask(), 20),
        OPEN("Open", Reagent.ASH.getMask() | Reagent.MOSS.getMask(), 5),
        PROTECTION("Protection", Reagent.ASH.getMask() | Reagent.GINSENG.getMask() | Reagent.GARLIC.getMask(), 15),
        QUICKNESS("Quickness", Reagent.ASH.getMask() | Reagent.GINSENG.getMask() | Reagent.MOSS.getMask(), 20),
        RESURRECT("Resurrect", Reagent.ASH.getMask() | Reagent.GINSENG.getMask() | Reagent.GARLIC.getMask() | Reagent.SILK.getMask() | Reagent.MOSS.getMask() | Reagent.MANDRAKE.getMask(), 45),
        SLEEP("Sleep", Reagent.SILK.getMask() | Reagent.GINSENG.getMask(), 15),
        TREMOR("Tremor", Reagent.ASH.getMask() | Reagent.MOSS.getMask() | Reagent.MANDRAKE.getMask(), 30),
        UNDEAD("Undead", Reagent.ASH.getMask() | Reagent.GARLIC.getMask(), 15),
        VIEW("View", Reagent.NIGHTSHADE.getMask() | Reagent.MANDRAKE.getMask(), 15),
        WINDS("Winds", Reagent.ASH.getMask() | Reagent.MOSS.getMask(), 10),
        XIT("X-it", Reagent.ASH.getMask() | Reagent.SILK.getMask() | Reagent.MOSS.getMask(), 15),
        YUP("Y-up", Reagent.SILK.getMask() | Reagent.MOSS.getMask(), 10),
        ZDOWN("Z-down", Reagent.SILK.getMask() | Reagent.MOSS.getMask(), 5);

        String desc;
        int mask;
        int mp;

        private Spell(String desc, int mask, int mp) {
            this.desc = desc;
            this.mask = mask;
            this.mp = mp;
        }

        public static Spell get(int i) {
            for (Spell x : values()) {
                if (x.ordinal() == i) {
                    return x;
                }
            }
            return null;
        }

        public String getDesc() {
            return desc;
        }

        public int getMask() {
            return mask;
        }

        public int getMp() {
            return mp;
        }

        @Override
        public String toString() {
            return this.desc;
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
                if (x.getValue() == (v & 0xff)) {
                    return x;
                }
            }
            return null;
        }

    }

    public enum Item {

        SKULL("Skull of Mondain", true, 0x000001),
        SKULL_DESTROYED("Skull Destroyed", false, 0x000002),
        CANDLE("Candle of Love", true, 0x000004),
        BOOK("Book of Truth", true, 0x000008),
        BELL("Bell of Courage", true, 0x000010),
        KEY_C("Key of Courage", true, 0x000020),
        KEY_L("Key of Love", true, 0x000040),
        KEY_T("Key of Truth", true, 0x000080),
        HORN("Silver Horn", true, 0x000100),
        WHEEL("Wheel of HMS Cape", true, 0x000200),
        CANDLE_USED("Candle Used", false, 0x000400),
        BOOK_USED("Book Used", false, 0x000800),
        BELL_USED("Bell Used", false, 0x001000),
        MASK_MINAX("Mask of Minax", true, 0x002000),
        RAGE_GOD("Rage of God", true, 0x004000),
        IRON_ORE("Iron Ore", true, 0x008000),
        RUNE_MOLD("Rune Mold", true, 0x010000),
        IRON_RUNE("Iron Rune", true, 0x020000),
        SONG_HUM("Song of Humility", true, 0x040000),
        PARCH("Magic Parchment", true, 0x080000),
        GREED_RUNE("Rune of Greed", true, 0x100000);

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
                if (x.ordinal() == (v & 0xff)) {
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

    public static final int SC_NONE = 0x00;
    public static final int SC_NEWMOONS = 0x01;
    public static final int SC_FULLAVATAR = 0x02;
    public static final int SC_REAGENTDELAY = 0x04;

    public enum ItemMapLabels {

        balloon("the Ballon", 0),
        lockelake("Locke Lake", 0),
        telescope("a telescope", 0),
        mandrake1("Mandrake Root", SC_NEWMOONS | SC_REAGENTDELAY),
        mandrake2("Mandrake Root", SC_NEWMOONS | SC_REAGENTDELAY),
        nightshade1("Nightshade", SC_NEWMOONS | SC_REAGENTDELAY),
        nightshade2("Nightshade", SC_NEWMOONS | SC_REAGENTDELAY),
        bell("the Bell of Courage", 0),
        horn("the Silver Horn", 0),
        wheel("the Wheel from the H.M.S. Cape", 0),
        skull("the Skull of Modain the Wizard", SC_NEWMOONS),
        candle("the Candle of Love", 0),
        book("the Book of Truth", 0),
        mysticarmor("Mystic Robes", SC_FULLAVATAR),
        mysticswords("Mystic Swords", SC_FULLAVATAR),
        honestyrune("the Rune of Honesty", 0),
        compassionrune("the Rune of Compassion", 0),
        valorrune("the Rune of Valor", 0),
        justicerune("the Rune of Justice", 0),
        sacrificerune("the Rune of Sacrifice", 0),
        honorrune("the Rune of Honor", 0),
        humilityrune("the Rune of Humility", 0),
        spiritualityrune("the Rune of Spirituality", 0),
        blackstone("the Black Stone", SC_NEWMOONS),
        whitestone("the White Stone", 0),
        bluestone("the Blue Stone", 0),
        yellowstone("the Yellow Stone", 0),
        redstone("the Red Stone", 0),
        greenstone("the Green Stone", 0),
        orangestone("the Orange Stone", 0),
        purplestone("the Purple Stone", 0),
        //extras
        ironrunemold("Iron Rune Mold", 0),
        blackironore("Black Iron Ore", 0),
        magicparchment("Magic Parchment", 0),
        greedrune("the Rune of Greed", 0),
        songhumility("the Song of Humility", 0),
        maskofminax("the Mask of Minax", 0),
        rageofgod("the Rage of God", 0);

        private String desc;
        private int conditions;

        private ItemMapLabels(String desc, int cond) {
            this.desc = desc;
            this.conditions = cond;
        }

        public String getDesc() {
            return this.desc;
        }

        public int getConditions() {
            return this.conditions;
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
                if (x.ordinal() == (v & 0xff)) {
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
        TINKER,
        HORSE;
    }

    public enum CannotJoinError {

        JOIN_SUCCEEDED,
        JOIN_NOT_EXPERIENCED,
        JOIN_NOT_VIRTUOUS;
    }

    public enum CombatAction {

        ATTACK,
        CAST_SLEEP,
        ADVANCE,
        RANGED,
        FLEE,
        TELEPORT;
    }

    public enum PartyEvent {

        POSITIVE_KARMA,
        NEGATIVE_KARMA,
        LOST_EIGHTH,
        ADVANCED_LEVEL,
        STARVING,
        POISON_DAMAGE,
        TRANSPORT_CHANGED,
        PARTY_DEATH,
        ACTIVE_PLAYER_CHANGED,
        MEMBER_JOINED,
        PARTY_REVIVED,
        INVENTORY_ADDED,
    };

    public enum CreatureStatus {

        FINE,
        DEAD,
        FLEEING,
        CRITICAL,
        HEAVILYWOUNDED,
        LIGHTLYWOUNDED,
        BARELYWOUNDED;
    }

    public enum AuraType {

        NONE,
        HORN,
        JINX,
        NEGATE,
        PROTECTION,
        QUICKNESS;
    }

    public enum CreatureType {

        horse(0),
        horse2(1),
        mage(2),
        bard(3),
        fighter(4),
        druid(5),
        tinker(6),
        paladin(7),
        ranger(8),
        shepherd(9),
        guard(10),
        villager(11),
        bard_singing(12),
        jester(13),
        beggar(14),
        child(15),
        bull(16),
        lord_british(17),
        pirate_ship(18),
        nixie(19),
        giant_squid(20),
        sea_serpent(21),
        sea_horse(22),
        whirlpool(23),
        twister(24),
        rat(25, 5, 0),
        bat(26, 5, 0),
        spider(27, 5, 0),
        ghost(28, 5, 1),
        slime(29, 5, 0),
        troll(30, 5, 2),
        gremlin(31, 5, 3),
        mimic(32),
        reaper(33),
        insect_swarm(34),
        gazer(35, 3, 4),
        phantom(36, 3, 4),
        orc(37, 5, 0),
        skeleton(38, 5, 0),
        rogue(39),
        python(40),
        ettin(41, 5, 3),
        headless(42, 5, 3),
        cyclops(43, 5, 4),
        wisp(44, 5, 5),
        evil_mage(45, 3, 5),
        liche(46),
        lava_lizard(47),
        zorn(48),
        daemon(49, 3, 4),
        hydra(50),
        dragon(51),
        balron(52);

        private int intValue;
        private int dungeonSpawnWeight;
        private int dungeonSpawnLevel;
        private Creature creature;

        private CreatureType(int value) {
            intValue = value;
            dungeonSpawnWeight = 0;
        }

        private CreatureType(int value, int dsw, int dsl) {
            intValue = value;
            dungeonSpawnWeight = dsw;
            dungeonSpawnLevel = dsl;
        }

        public int getValue() {
            return intValue;
        }

        public int getSpawnWeight() {
            return dungeonSpawnWeight;
        }

        public int getSpawnLevel() {
            return dungeonSpawnLevel;
        }

        public static CreatureType get(int v) {
            for (CreatureType x : values()) {
                if (x.getValue() == v) {
                    return x;
                }
            }
            return null;
        }

        public static CreatureType get(String v) {
            for (CreatureType x : values()) {
                if (x.toString().equals(v)) {
                    return x;
                }
            }
            return null;
        }

        public Creature getCreature() {
            return creature;
        }

        public void setCreature(Creature creature) {
            this.creature = creature;
        }
    }

    public enum PirateCoveInfo {

        P1(224, 220, Direction.EAST), /* N'M" O'A" */
        P2(224, 228, Direction.EAST), /* O'E" O'A" */
        P3(226, 220, Direction.EAST), /* O'E" O'C" */
        P4(227, 228, Direction.EAST), /* O'E" O'D" */
        P5(228, 227, Direction.SOUTH), /* O'D" O'E" */
        P6(229, 225, Direction.SOUTH), /* O'B" O'F" */
        P7(229, 223, Direction.NORTH), /* N'P" O'F" */
        P8(228, 222, Direction.NORTH); /* N'O" O'E" */

        private int x;
        private int y;
        private Direction facing;

        private PirateCoveInfo(int x, int y, Direction facing) {
            this.x = x;
            this.y = y;
            this.facing = facing;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public Direction getFacing() {
            return facing;
        }

    }

    public static final int MAX_CREATURES_ON_MAP = 4;
    public static final int MAX_WANDERING_CREATURES_IN_DUNGEON = 2;
    public static final int MAX_CREATURE_DISTANCE = 24;

    public static String[] karmaQuestions = {
        "Entrusted to deliver an uncounted purse of gold, thou Dost meet a poor beggar. \nDost thou A) deliver the gold knowing the Trust in thee was well placed, or B) show Compassion, giving the beggar a coin, knowing it won't be missed?",
        "Thou has been prohibited by thy absent Lord from joining thy friends in a close pitched battle. \nDost thou A) refrain, so thou may Honesty claim obedience, or B) show Valor, and aid thy comrades, knowing thou may deny it later?",
        "A merchant owes thy friend money, now long past due.  Thou \nDost see the same merchant drop a purse of gold. \nDost thou A) Honestly return the purse intact, or B) Justly give thy friend a portion of the gold first?",
        "Thee and thy friend are valiant but penniless warriors.  Thou both go out to slay a mighty dragon.  Thy friend thinks he slew it; thee did.  When asked, \nDost thou A) Truthfully claim the gold, or B) Allow thy friend the large reward?",
        "Thou art sworn to protect thy Lord at any cost, yet thou know he hath committed a crime.  Authorities ask thee of the affair.  \nDost thou A) break thine oath by Honestly speaking, or B) uphold Honor by silently keeping thine oath?",
        "Thy friend seeks admittance to thy Spiritual order. Thou art asked to vouch for his purity of Spirit, of which thou art unsure.  \nDost thou A) Honestly express thy doubt, or B) Vouch for him hoping for his Spiritual improvement?",
        "Thy Lord mistakenly believes he slew a dragon.  Thou hast proof that thy lance felled the beast.  When asked, \nDost thou A) Honestly claim the kill and the prize, or B) Humbly permit thy Lord his belief?",
        "Thou Dost manage to disarm thy mortal enemy in a duel.  He is at thy mercy. \nDost thou A) show Compassion by permitting him to yield, or B) slay him as expected of a Valiant duelist?",
        "After 20 years thou hast found the slayer of thy best friends.  The villain proves to be a man who provides the sole support for a young girl.  \nDost thou A) spare him in Compassion for the girl, or B) slay him in the name of Justice?",
        "Thee and thy friends have been routed and ordered to retreat.  In defiance of thy orders, \nDost thou A) stop in Compassion to aid a wounded companion, or B) Sacrifice thyself to slow the pursuing enemy, so others may escape?",
        "Thou art sworn to uphold a Lord who participates in the forbidden torture of prisoners.  Each night their cries of pain reach thee.  \nDost thou A) show Compassion by reporting the deeds, or B) Honor thy oath and ignore the deeds?",
        "Thou hast been taught to preserve all life as sacred. A man lies fatally stung by a venomous serpent.  He pleads for a merciful death.  \nDost thou A) show Compassion and end his pain, or B) heed thy Spiritual beliefs and refuse?",
        "As one of the King's Guard, thy Captain has asked that one amongst you visit a hospital to cheer the children with tales of thy valiant deeds.  \nDost thou A) Show thy Compassion and play the braggart, or B) Humbly let another go?",
        "Thou hast been sent to secure a needed treaty with a distant Lord.  Thy host is agreeable to the proposal but insults thy country at dinner.  \nDost thou A) Valiantly bear the slurs, or B) Justly rise and demand an apology?",
        "A mighty knight accosts thee and demands thy food.  \nDost thou A) Valiantly refuse and engage the knight, or B) Sacrifice thy food unto the hungry knight?",
        "During battle thou art ordered to guard thy commander's empty tent.  The battle goes poorly and thou \nDost yearn to aid thy fellows.  \nDost thou A) Valiantly enter the battle to aid thy companions, or B) Honor thy post as guard?",
        "A local bully pushes for a fight.  \nDost thou A) Valiantly trounce the rogue, or B) Decline, knowing in thy Spirit that no lasting good will come of it?",
        "Although a teacher of music, thou art a skillful wrestler.  Thou hast been asked to fight in a local championship. \nDost thou A) accept the invitation and Valiantly fight to win, or B) Humbly decline knowing thou art sure to win?",
        "During a pitched battle, thou Dost see a fellow desert his post, endangering many.  As he flees, he is set upon by several enemies. \nDost thou A) Justly let him fight alone, or B) Risk Sacrificing thine own life to aid him?",
        "Thou hast sworn to do thy Lord's bidding in all.  He covets a piece of land and orders the owner removed.  \nDost thou A) serve Justice refusing to act, thus being disgraced, or B) Honor thine oath and unfairly evict the landowner?",
        "Thou Dost believe that virtue resides in all people.  Thou \nDost see a rogue steal from thy Lord.  \nDost thou A) call him to Justice, or B) personally try to sway him back to the Spiritual path of good?",
        "Unwitnessed, thou hast slain a great dragon in self defense.  A poor warrior claims the offered reward.  \nDost thou A) Justly step forward to claim the reward, or B) Humbly go about life, secure in thy self esteem?",
        "Thou art a bounty hunter sworn to return an alleged murder.  After his capture thou believest him to be innocent.  \nDost thou A) Sacrifice thy sizable bounty for thy belief, or B) Honor thy oath to return him as thou hast promised?",
        "Thou hast spent thy life in charitable and righteous work.  Thine uncle the innkeeper lies ill and asks you to take over his tavern.  \nDost thou A) Sacrifice thy life of purity to aid thy kin, or B) decline & follow thy Spirit's call?",
        "Thou art an elderly, wealthy eccentric. Thy end is near. \nDost thou A) donate all thy wealth to feed hundreds of starving children, and receive public adulation, or B) Humbly live out thy life, willing thy fortune to thy heirs?",
        "In thy youth thou pledged to marry thy sweetheart.  Now thou art on a sacred quest in distant lands.  Thy sweetheart asks thee to keep thy vow.  \nDost thou A) Honor thy pledge to wed, or B) follow thy Spiritual crusade?",
        "Thou art at a crossroads in thy life. \nDost thou A) Choose the Honorable life of a Paladin, striving for Truth and Courage, or B) Choose the Humble life of a Shepherd, and a world of simplicity and peace?",
        "Thy parents wish thee to become an apprentice. Two positions are available. \nDost thou A) Become an acolyte in the Spiritual order, or B) Become an assistant to a humble village cobbler?"
    };

    public static String[] initScripts = {
        "The day is warm, yet there is a cooling breeze.  The latest in a series of personal crises seems insurmountable. You are being pulled apart in all directions.",
        "Yet this afternoon walk in the countryside slowly brings relaxation to your harried mind.  The soil and strain of modern high-tech living begins to wash off in layers. That willow tree near the stream looks comfortable and inviting.",
        "The buzz of dragonflies and the whisper of the willow's swaying branches bring a deep peace.  Searching inward for tranquility and happiness, you close your eyes.",
        "A high-pitched cascading sound like crystal wind-chimes impinges on your floating awareness.  As you open your eyes, you see a shimmering blueness rise from the ground.  The sound seems to be emanating from this glowing portal.",
        "It is difficult to look at the blueness. Light seems to bend and distort around it, while the sound waves become so intense, they appear to become visible.",
        "The portal hangs there for a moment; then, with the rush of an imploding vacuum, it sinks into the ground. Something remains suspended in mid-air for a moment before falling to earth with a heavy thud.",
        "Somewhat shaken by this vision, you rise to your feet to investigate.  A crude circle of stones surrounds the spot where the portal appeared. There is something glinting in the grass.",
        "You pick up an amulet shaped like a cross with a loop at the top.  It is an Ankh, the sacred symbol of life and rebirth.  But this could not have made the thud, so you look again and find a large book wrapped in thick cloth!",
        "With trembling hands you unwrap the book.  Behold, the cloth is a map, and within lies not one book, but two.  The map is of a land strange to you, and the style speaks of ancient cartography.",
        "The script on the cover of the first book is arcane but readable.\nThe title is: The History of Britannia as told by Kyle the Younger.",
        "The other book is disturbing to look at. Its small cover appears to be fashioned out of some sort of leathery hide, but from what creature is uncertain.  The reddish-black skin radiates an intense aura suggestive of ancient power.",
        "The tongue of the title is beyond your ken.  You dare not open the book and disturb whatever sleeps within.  You decide to peruse the History.  Settling back under the willow tree, you open the book.",
        "(You read the Book of History)",
        "(No, really! Read the Book of History!)",
        "Closing the book, you again pick up the Ankh.  As you hold it, you begin to hear a hauntingly familiar, lute-like sound wafting over a nearby hill.  Still clutching the strange artifacts, you rise unbidden and climb the slope.",
        "In the valley below you see what appears to be a fair.  It seems strange that you came that way earlier and noticed nothing.  As you mull this over, your feet carry you down towards the site.",
        "This is no ordinary travelling carnival, but a Renaissance Fair.  The pennants on the tent tops blow briskly in the late afternoon breeze.",
        "The ticket taker at the RenFair's gate starts to ask you for money, but upon spotting your Ankh says, \"Welcome, friend.  Enter in peace and find your path.\"",
        "The music continues to pull you forward amongst the merchants and vendors.  Glimpses of fabulous treasures can be seen in some of the shadowy booths.",
        "These people are very happy.  They seem to glow with an inner light.  Some look up as you pass and smile, but you cannot stop - the music compels you to move onward through the crowd.",
        "Through the gathering dusk you see a secluded gypsy wagon sitting off in the woods.  The music seems to emanate from the wagon.  As you draw near, a woman's voice weaves into the music, saying: \"You may approach, O seeker.\"",
        "You enter to find an old gypsy sitting in a small curtained room.  She wears an Ankh around her neck. In front of her is a round table covered in deep green velvet.  The room smells so heavily of incense that you feel dizzy.",
        "Seeing the Ankh, the ancient gypsy smiles and warns you never to part with it.  \"We have been waiting such a long time, but at last you have come.  Sit here and I shall read the path of your future.\"",
        "Upon the table she places a curious wooden object like an abacus but without beads.  In her hands she holds eight unusual cards.  \"Let us begin the casting.\""
    };

    public static String[] gypsyText = {
        "The gypsy places the first two cards ",
        "The gypsy places two more of the cards ",
        "The gypsy places the last two cards ",
        "upon the table.  They are the cards of ",
        "Honesty",
        "Compassion",
        "Valor",
        "Justice",
        "Sacrifice",
        "Honor",
        "Spirituality",
        "Humility",
        "With the final choice, the incense swells up around you.  The gypsy speaks as if from a great distance, her voice growing fainter with each word: \"So be it!  Thy path is chosen!\"",
        "There is a moment of intense, wrenching vertigo.  As you open your eyes, a voice whispers within your mind, \"Seek the counsel of thy sovereign.\"  After a moment, the spinning subsides, and you open your eyes to..."
    };

    public static String[] shrineAdvice = {
        "Take not the gold of others found in towns and castles for yours it is not!",
        "Cheat not the merchants and peddlers for tis an evil thing to do!",
        "Second, read the Book of Truth at the entrance to the Great Stygian Abyss!",
        "Kill not the non-evil beasts of the land, and do not attack the fair people!",
        "Give of thy purse to those who beg and thy deed shall not be forgotten!",
        "Third, light the Candle of Love at the entrance to the Great Stygian Abyss!",
        "Victories scored over evil creatures help to build a valorous soul!",
        "To flee from battle with less than grievous wounds often shows a coward!",
        "First, ring the Bell of Courage at the entrance to the Great Stygian Abyss!",
        "To take the gold of others is injustice not soon forgotten. Take only thy due!",
        "Attack not a peaceful citizen for that action deserves strict punishment!",
        "Kill not a non-evil beast for they deserve not death, even if in hunger they attack thee!",
        "To give thy last gold piece unto the needy shows good measure of self-sacrifice!",
        "For thee to flee and leave thy companions is a self-serving action to be avoided!",
        "To give of thy life's blood so that others may live is a virtue of great praise!",
        "Take not the gold of others for this shall bring dishonor upon thee!",
        "To strike first a non-evil being is by no means an honorable deed!",
        "Seek ye to solve the many Quests before thee, and honor shall be a reward!",
        "Seek ye to know thyself.  Visit the seer often for he can see into thy inner being!",
        "Meditation leads to enlightenment Seek ye all Wisdom and Knowledge!",
        "If thou dost seek the White Stone, search ye not under the ground, but in Serpent's Spine!",
        "Claim not to be that which thou art not.  Humble actions speak well of thee!",
        "Strive not to wield the Great Force of Evil for its power will overcome thee!",
        "If thou dost seek the Black Stone, search ye at the Time and Place of the Gate on the darkest of all nights!"
    };

    public static String[] hawkwindText = {
        "Thou art a thief and a scoundrel. Thou may not ever become an Avatar!",
        "Thou art a cold and cruel brute.  Thou shouldst go to prison for thy crimes!",
        "Thou art a coward, thou dost flee from the hint of danger!",
        "Thou art an unjust wretch. Thou are a fulsome meddler!",
        "Thou art a self-serving Tufthunter. Thou deservest not my help, yet I grant it!",
        "Thou art a cad and a bounder. Thy presence is an affront. Thou art low as a slug!",
        "Thy spirit is weak and feeble. Thou dost not strive for Perfection!",
        "Thou art proud and vain. All other virtue in thee is a loss!",
        "Thou art not an honest soul. Thou must live a more honest life to be an Avatar!",
        "Thou dost kill where there is no need and give too little unto others!",
        "Thou dost not display a great deal of Valor. Thou dost flee before the need!",
        "Thou art cruel and unjust. In time thou will suffer for thy crimes!",
        "Thou dost need to think more of the life of others and less of thy own!",
        "Thou dost not fight with honor but with malice and deceit!",
        "Thou dost not take time to care about thy inner being, a must to be an Avatar!",
        "Thou art too proud of thy little deeds. Humility is the root of all Virtue!",
        "Thou hast made little progress on the paths of Honesty. Strive to prove thy worth!",
        "Thou hast not shown thy compassion well. Be more kind unto others!",
        "Thou art not yet a valiant warrior.  Fight to defeat evil and prove thyself!",
        "Thou hast not proven thyself to be just. Strive to do justice unto all things!",
        "Thy sacrifice is small. Give of thy life's blood so that others may live.",
        "Thou dost need to show thyself to be more honorable.  The path lies before thee!",
        "Strive to know and master more of thine inner being. Meditation lights the path!",
        "Thy progress on this path is most uncertain. Without Humility thou art empty!",
        "Thou dost seem to be an honest soul.  Continued honesty will reward thee!",
        "Thou dost show thy compassion well.  Continued goodwill should be thy guide!",
        "Thou art showing Valor in the face of danger. Strive to become yet more so!",
        "Thou dost seem fair and just. Strive to uphold Justice even more sternly!",
        "Thou art giving of thyself in some ways. Seek ye now to find yet more!",
        "Thou dost seem to be Honorable in nature.  Seek to bring Honor upon others as well!",
        "Thou art doing well on the path to inner sight continue to seek the inner light!",
        "Thou dost seem a humble soul.  Thou art setting strong stones to build virtues upon!",
        "Thou art truly an honest soul. Seek ye now to reach Elevation!",
        "Compassion is a virtue that thou hast shown well.  Seek ye now Elevation!",
        "Thou art a truly valiant warrior. Seek ye now Elevation in the virtue of valor!",
        "Thou art just and fair.  Seek ye now the Elevation!",
        "Thou art giving and good.  Thy self-sacrifice is great.  Seek now Elevation!",
        "Thou hast proven thyself to be Honorable. Seek ye now for the Elevation!",
        "Spirituality is in thy nature. Seek ye now the Elevation!",
        "Thy Humility shines bright upon thy being. Seek ye now for Elevation!",
        "The Seer says: I will speak only with ",
        "Return when ",
        " is revived!",
        "Welcome,",
        "I am Hawkwind, Seer of Souls. I see that which is within thee and drives thee to deeds of good or evil...",
        "For what path dost thou seek enlightenment?",
        "Hawkwind asks: What other path seeks clarity?",
        "none",
        "bye",
        "He says: That is not a subject for enlightenment.",
        "He says: Thou hast become a partial Avatar in that attribute. Thou need not my insights.",
        "Go to the Shrine and meditate for three Cycles!",
        "Hawkwind says: Fare thee well and may thou complete the Quest of the Avatar!"
    };

    public static final int REVIVE_WORLD_X = 86;
    public static final int REVIVE_WORLD_Y = 107;
    public static final int REVIVE_CASTLE_X = 19;
    public static final int REVIVE_CASTLE_Y = 8;

    public static String[] deathMsgs = {
        "All is Dark...",
        "But wait...",
        "Where am I?...",
        "Am I dead?...",
        "Afterlife?...",
        "You hear:  %s",
        "I feel motion...",
        "Lord British says: I have pulled thy spirit and some possessions from the void.  Be more careful in the future!"
    };

    public enum AttackResult {

        NONE,
        HIT,
        MISS;
    }

    public class AttackVector {

        public int x;
        public int y;
        public int distance;

        public AttackResult result;
        public String leaveTileName;

        public Creature impactedCreature;
        public Drawable impactedDrawable;

        public AttackVector(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public class AddActorAction implements Runnable {

        private Actor actor;
        private Stage stage;

        public AddActorAction(Stage stage, Actor actor) {
            this.actor = actor;
            this.stage = stage;
        }

        @Override
        public void run() {
            stage.addActor(actor);
        }
    }

    public class PlaySoundAction implements Runnable {

        private Sound s;

        public PlaySoundAction(Sound s) {
            this.s = s;
        }

        @Override
        public void run() {
            Sounds.play(s);
        }
    }
    
}
