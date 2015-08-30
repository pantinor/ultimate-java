package objects;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import objects.Party.PartyMember;
import ultima.BaseScreen;
import ultima.Constants;
import ultima.GameScreen;
import util.Utils;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;

@XmlRootElement(name = "map")
public class BaseMap implements Constants {

    private boolean initialized = false;

    private int id;
    private String fname;
    private MapType type;
    private int width;
    private int height;
    private int levels;
    private int chunkwidth;
    private int chunkheight;
    private int offset;
    private boolean showavatar;
    private boolean nolineofsight;
    private boolean firstperson;
    private boolean contextual; //??
    private String music;
    private MapBorderBehavior borderbehavior;
    private String tileset;
    private String tilemap;

    private List<Portal> portals;
    private List<Label> labels;
    private City city;
    private Dungeon dungeon;
    private Shrine shrine;

    private List<Creature> creatures = new ArrayList<Creature>();
    private List<PartyMember> combatPlayers;
    private Stage surfaceMapStage;

    private List<Moongate> moongates;
    private Tile[] tiles;
    private float[][] shadownMap;

	//used to keep the pace of wandering to every 2 moves instead of every move, 
    //otherwise cannot catch up and talk to the character
    private long wanderFlag = 0;

    private List<DoorStatus> doors = new ArrayList<DoorStatus>();

    public Moongate getMoongate(int phase) {
        if (moongates == null) {
            return null;
        }
        for (Moongate m : moongates) {
            if (m.getPhase() == phase) {
                return m;
            }
        }
        return null;
    }

    public Portal getPortal(int id) {
        if (portals == null) {
            return null;
        }
        for (Portal p : portals) {
            if (p.getDestmapid() == id) {
                return p;
            }
        }
        return null;
    }

    public Portal getPortal(float x, float y) {
        if (portals == null) {
            return null;
        }
        for (Portal p : portals) {
            if (p.getX() == x && p.getY() == y) {
                return p;
            }
        }
        return null;
    }

    public List<Portal> getPortals(int x, int y, int z) {
        List<Portal> ps = new ArrayList<Portal>();
        if (portals == null) {
            return ps;
        }
        for (Portal p : portals) {
            if (p.getX() == x && p.getY() == y && p.getZ() == z) {
                ps.add(p);
            }
        }
        return ps;
    }

    @XmlAttribute
    public int getId() {
        return id;
    }

    @XmlAttribute
    public String getFname() {
        return fname;
    }

    @XmlAttribute(name = "type")
    @XmlJavaTypeAdapter(MapTypeAdapter.class)
    public MapType getType() {
        return type;
    }

    @XmlAttribute
    public int getWidth() {
        return width;
    }

    @XmlAttribute
    public int getHeight() {
        return height;
    }

    @XmlAttribute
    public int getLevels() {
        return levels;
    }

    @XmlAttribute
    public int getOffset() {
        return offset;
    }

    @XmlAttribute
    public boolean getShowavatar() {
        return showavatar;
    }

    @XmlAttribute
    public boolean getNolineofsight() {
        return nolineofsight;
    }

    @XmlAttribute
    public boolean getFirstperson() {
        return firstperson;
    }

    @XmlAttribute
    public boolean getContextual() {
        return contextual;
    }

    @XmlAttribute
    public String getMusic() {
        return music;
    }

    @XmlAttribute
    public String getTileset() {
        return tileset;
    }

    @XmlAttribute
    public String getTilemap() {
        return tilemap;
    }

    @XmlAttribute
    public int getChunkwidth() {
        return chunkwidth;
    }

    @XmlAttribute
    public int getChunkheight() {
        return chunkheight;
    }

    @XmlAttribute(name = "borderbehavior")
    @XmlJavaTypeAdapter(BorderTypeAdapter.class)
    public MapBorderBehavior getBorderbehavior() {
        return borderbehavior;
    }

    @XmlElement(name = "portal")
    public List<Portal> getPortals() {
        return portals;
    }

    @XmlElement(name = "label")
    public List<Label> getLabels() {
        return labels;
    }

    @XmlElement
    public City getCity() {
        return city;
    }

    @XmlElement
    public Shrine getShrine() {
        return shrine;
    }

    @XmlElement(name = "moongate")
    public List<Moongate> getMoongates() {
        return moongates;
    }

    @XmlElement
    public Dungeon getDungeon() {
        return dungeon;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public void setType(MapType type) {
        this.type = type;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setLevels(int levels) {
        this.levels = levels;
    }

    public void setChunkWidth(int chunk_width) {
        this.chunkwidth = chunk_width;
    }

    public void setChunkHeight(int chunk_height) {
        this.chunkheight = chunk_height;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setShowavatar(boolean showavatar) {
        this.showavatar = showavatar;
    }

    public void setNolineofsight(boolean nolineofsight) {
        this.nolineofsight = nolineofsight;
    }

    public void setFirstperson(boolean firstperson) {
        this.firstperson = firstperson;
    }

    public void setContextual(boolean contextual) {
        this.contextual = contextual;
    }

    public void setMusic(String music) {
        this.music = music;
    }

    public void setTileset(String tileset) {
        this.tileset = tileset;
    }

    public void setTilemap(String tilemap) {
        this.tilemap = tilemap;
    }

    public void setChunkwidth(int chunkwidth) {
        this.chunkwidth = chunkwidth;
    }

    public void setChunkheight(int chunkheight) {
        this.chunkheight = chunkheight;
    }

    public void setBorderbehavior(MapBorderBehavior borderbehavior) {
        this.borderbehavior = borderbehavior;
    }

    public void setPortals(List<Portal> portals) {
        this.portals = portals;
    }

    public void setLabels(List<Label> labels) {
        this.labels = labels;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public void setShrine(Shrine shrine) {
        this.shrine = shrine;
    }

    public List<Creature> getCreatures() {
        return creatures;
    }

    public void addCreature(Creature cr) {
        if (cr == null) {
            return;
        }
        creatures.add(cr);
    }

    public void removeCreature(Creature cr) {
        if (cr == null) {
            return;
        }
        creatures.remove(cr);
    }

    public void clearCreatures() {
        creatures.clear();
    }

    public Creature getCreatureAt(int x, int y) {
        for (Creature cre : creatures) {
            if (cre.currentX == x && cre.currentY == y) {
                return cre;
            }
        }
        return null;
    }

    public void setMoongates(List<Moongate> moongate) {
        this.moongates = moongate;
    }

    public void setDungeon(Dungeon dungeon) {
        this.dungeon = dungeon;
    }

    @Override
    public String toString() {
        return String.format("BaseMap [id=%s, fname=%s, portals=%s]", id, fname, portals);
    }

    public Tile[] getTiles() {
        return tiles;
    }

    public void setTiles(Tile[] tiles) {
        this.tiles = tiles;
    }

    public void setTile(Tile tile, int x, int y) {
        if (x < 0 || y < 0) {
            return;
        }
        if (x + (y * width) >= tiles.length) {
            return;
        }
        tiles[x + (y * width)] = tile;
    }

    public synchronized Tile getTile(int x, int y) {
        if (x < 0 || y < 0) {
            return null;
        }
        if (x + (y * width) >= tiles.length) {
            return null;
        }

        //allows walking on chests frigates horses and balloons
        if (surfaceMapStage != null) {
            for (Actor a : surfaceMapStage.getActors()) {
                if (a instanceof Drawable) {
                    Drawable d = (Drawable) a;
                    if (d.getCx() == x && d.getCy() == y) {
                        return d.getTile();
                    }
                }
            }
        }

        return tiles[x + (y * width)];
    }

    public synchronized Tile getTile(Vector3 v) {
        return getTile((int) v.x, (int) v.y);
    }

    public float[][] getShadownMap() {
        return shadownMap;
    }

    public void setShadownMap(float[][] shadownMap) {
        this.shadownMap = shadownMap;
    }

    public void initObjects(GameScreen screen, TextureAtlas atlas1, TextureAtlas atlas2) {

        if (initialized) {

            if (city != null) {
                for (Person p : city.getPeople()) {
                    if (p == null) {
                        continue;
                    }
                    p.setRemovedFromMap(false);
                }
            }

            return;
        }

        if (city != null) {

            for (Person p : city.getPeople()) {
                if (p == null) {
                    continue;
                }
                String tname = p.getTile().getName();

                Array<AtlasRegion> arr = atlas1.findRegions(tname);
                if (arr == null || arr.size == 0) {
                    arr = atlas2.findRegions(tname);
                }
                
                if (arr.size == 0) {
                    System.err.printf("%s - tname is empty %s",p, tname);
                }
                
                p.setTextureRegion(arr.first());

                if (arr.size > 1) {
                    //random rate between 1 and 4
                    int frameRate = Utils.getRandomBetween(1, 4);
                    p.setAnim(new Animation(frameRate, arr));
                }

                Vector3 pixelPos = screen.getMapPixelCoords(p.getStart_x(), p.getStart_y());
                p.setCurrentPos(pixelPos);
                p.setX(p.getStart_x());
                p.setY(p.getStart_y());

                CreatureType ct = CreatureType.get(tname);
                if (ct != null) {
                    p.setEmulatingCreature(ct.getCreature());
                }

            }

        }

        //set doors
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                Tile t = getTile(x, y);
                if (t.getName().equals("door")) {
                    doors.add(new DoorStatus(x, y, false, 0));
                } else if (t.getName().equals("locked_door")) {
                    doors.add(new DoorStatus(x, y, true, 0));
                }
            }
        }

        initialized = true;
    }

    public void moveObjects(GameScreen screen, int avatarX, int avatarY) {

        if (city != null) {

            wanderFlag++;

            for (Person p : city.getPeople()) {
                if (p == null || p.isRemovedFromMap()) {
                    continue;
                }

                Vector3 pos = null;
                Vector3 pixelPos = null;
                Direction dir = null;

                switch (p.getMovement()) {
                    case ATTACK_AVATAR: {
                        int dist = Utils.movementDistance(borderbehavior, width, height, p.getX(), p.getY(), avatarX, avatarY);
                        if (dist <= 1) {
                            Maps cm = GameScreen.context.getCombatMap(p.getEmulatingCreature(), this, p.getX(), p.getY(), avatarX, avatarY);
                            Creature attacker = GameScreen.creatures.getInstance(p.getEmulatingCreature().getTile(), GameScreen.standardAtlas);
                            attacker.currentX = p.getX();
                            attacker.currentY = p.getY();
                            attacker.currentPos = screen.getMapPixelCoords(p.getX(), p.getY());
                            screen.attackAt(cm, attacker);
                            p.setRemovedFromMap(true);
                            continue;
                        }
                        int mask = getValidMovesMask(p.getX(), p.getY(), p.getEmulatingCreature(), avatarX, avatarY);
                        dir = Utils.getPath(borderbehavior, width, height, avatarX, avatarY, mask, true, p.getX(), p.getY());
                    }
                    break;
                    case FOLLOW_AVATAR: {
                        int mask = getValidMovesMask(p.getX(), p.getY(), p.getEmulatingCreature(), avatarX, avatarY);
                        dir = Utils.getPath(borderbehavior, width, height, avatarX, avatarY, mask, true, p.getX(), p.getY());
                    }
                    break;
                    case FIXED:
                        break;
                    case WANDER: {
                        if (wanderFlag % 2 == 0) {
                            continue;
                        }
                        if (p.isTalking()) {
                            continue;
                        }
                        dir = Direction.getRandomValidDirection(getValidMovesMask(p.getX(), p.getY(), p.getEmulatingCreature(), avatarX, avatarY));
                    }
                    break;
                    default:
                        break;

                }

                if (dir == null) {
                    continue;
                }
                if (dir == Direction.NORTH) {
                    pos = new Vector3(p.getX(), p.getY() - 1, 0);
                }
                if (dir == Direction.SOUTH) {
                    pos = new Vector3(p.getX(), p.getY() + 1, 0);
                }
                if (dir == Direction.EAST) {
                    pos = new Vector3(p.getX() + 1, p.getY(), 0);
                }
                if (dir == Direction.WEST) {
                    pos = new Vector3(p.getX() - 1, p.getY(), 0);
                }
                pixelPos = screen.getMapPixelCoords((int) pos.x, (int) pos.y);
                p.setCurrentPos(pixelPos);
                p.setX((int) pos.x);
                p.setY((int) pos.y);

            }

        }

        Iterator<Creature> i = creatures.iterator();
        while (i.hasNext()) {

            Creature cr = i.next();

            int dist = Utils.movementDistance(borderbehavior, width, height, cr.currentX, cr.currentY, avatarX, avatarY);
            if (dist > MAX_CREATURE_DISTANCE) {
                i.remove();
                continue;
            }

            if (cr.getTile() == CreatureType.pirate_ship) {
                int relDirMask = Utils.getRelativeDirection(borderbehavior, width, height, avatarX, avatarY, cr.currentX, cr.currentY);
                if (avatarX == cr.currentX) {
                    relDirMask = Direction.removeFromMask(relDirMask, Direction.EAST, Direction.WEST);
                } else if (avatarY == cr.currentY) {
                    relDirMask = Direction.removeFromMask(relDirMask, Direction.NORTH, Direction.SOUTH);
                } else {
                    relDirMask = 0;
                }
                int broadsidesDirs = Direction.getBroadsidesDirectionMask(cr.sailDir);
                if (relDirMask > 0 && (dist == 3 || dist == 2) && Direction.isDirInMask(relDirMask, broadsidesDirs)) {
                    Direction fireDir = Direction.getByMask(relDirMask);
                    AttackVector av = Utils.enemyfireCannon(surfaceMapStage, this, fireDir, cr.currentX, cr.currentY, avatarX, avatarY);
                    Utils.animateCannonFire(screen, screen.projectilesStage, this, av, cr.currentX, cr.currentY, false);
                    continue;
                } else if (relDirMask > 0 && (dist == 3 || dist == 2) && !Direction.isDirInMask(relDirMask, broadsidesDirs) && Utils.rand.nextInt(2) == 0) {
                    cr.sailDir = Direction.goBroadsides(broadsidesDirs);
                    continue;
                } else if (dist <= 1) {
                    if (Direction.isDirInMask(relDirMask, broadsidesDirs)) {
                        screen.attackAt(Maps.SHIPSHIP_CON, cr);
                        break;
                    } else {
                        cr.sailDir = Direction.goBroadsides(broadsidesDirs);
                        continue;
                    }
                }

            } else if (dist <= 1) {

                if (cr.getWontattack()) {
                    if (cr.getTile() == CreatureType.whirlpool) {
                        GameScreen.context.damageShip(-1, 10);
                        //teleport to lock lake
                        screen.newMapPixelCoords = screen.getMapPixelCoords(127, 78);
                        screen.changeMapPosition = true;
                        i.remove();
                        continue;
                    } else if (cr.getTile() == CreatureType.twister) {
                        if (GameScreen.context.getTransportContext() == TransportContext.SHIP) {
                            GameScreen.context.damageShip(10, 30);
                        } else if (GameScreen.context.getTransportContext() != TransportContext.BALLOON) {
                            GameScreen.context.getParty().damageParty(0, 75);
                        }
                        continue;
                    }
                } else {
                    Maps cm = GameScreen.context.getCombatMap(cr, this, cr.currentX, cr.currentY, avatarX, avatarY);
                    screen.attackAt(cm, cr);
                    break;
                }

            }

            int mask = getValidMovesMask(cr.currentX, cr.currentY, cr, avatarX, avatarY);
            Direction dir = null;
            if (cr.getWontattack()) {
                dir = Direction.getRandomValidDirection(mask);
            } else {
                dir = Utils.getPath(borderbehavior, width, height, avatarX, avatarY, mask, true, cr.currentX, cr.currentY);
            }
            if (dir == null) {
                continue;
            }

            if (cr.getTile() == CreatureType.pirate_ship) {
                if (cr.sailDir != dir) {
                    cr.sailDir = dir;
                    continue;
                }
            }

            if (borderbehavior == MapBorderBehavior.wrap) {
                if (dir == Direction.NORTH) {
                    cr.currentY = cr.currentY - 1 < 0 ? height - 1 : cr.currentY - 1;
                }
                if (dir == Direction.SOUTH) {
                    cr.currentY = cr.currentY + 1 >= height ? 0 : cr.currentY + 1;
                }
                if (dir == Direction.EAST) {
                    cr.currentX = cr.currentX + 1 >= width ? 0 : cr.currentX + 1;
                }
                if (dir == Direction.WEST) {
                    cr.currentX = cr.currentX - 1 < 0 ? width - 1 : cr.currentX - 1;
                }
            } else {
                if (dir == Direction.NORTH) {
                    cr.currentY = cr.currentY - 1 < 0 ? cr.currentY : cr.currentY - 1;
                }
                if (dir == Direction.SOUTH) {
                    cr.currentY = cr.currentY + 1 >= height ? cr.currentY : cr.currentY + 1;
                }
                if (dir == Direction.EAST) {
                    cr.currentX = cr.currentX + 1 >= width ? cr.currentX : cr.currentX + 1;
                }
                if (dir == Direction.WEST) {
                    cr.currentX = cr.currentX - 1 < 0 ? cr.currentX : cr.currentX - 1;
                }
            }

            cr.currentPos = screen.getMapPixelCoords(cr.currentX, cr.currentY);

        }

    }

    public boolean isTileBlockedForRangedAttack(int x, int y, boolean checkForCreatures) {
        Tile tile = getTile(x, y);
        TileRule rule = tile.getRule();
        boolean blocked = false;
        if (rule != null) {
            //projectiles cannot go thru walls, but can over water or if they can be attacked over, like certain solids
            blocked = rule.has(TileAttrib.unwalkable) && !rule.has(TileAttrib.canattackover) && !rule.has(TileAttrib.swimmable);
        }
        if (checkForCreatures) {
            for (Creature cre : creatures) {
                if (cre.currentX == x && cre.currentY == y) {
                    blocked = true;
                    break;
                }
            }
        }
        return blocked;
    }

    public int getValidMovesMask(int x, int y) {
        return getValidMovesMask(x, y, null, 0, 0);
    }

    public int getValidMovesMask(int x, int y, Creature cr, int avatarX, int avatarY) {

        int mask = 0;

        if (this.getBorderbehavior() == MapBorderBehavior.wrap) {

            Tile north = getTile(x, y - 1 < 0 ? height - 1 : y - 1);
            Tile south = getTile(x, y + 1 >= height ? 0 : y + 1);
            Tile east = getTile(x + 1 >= width - 1 ? 0 : x + 1, y);
            Tile west = getTile(x - 1 < 0 ? width - 1 : x - 1, y);

            mask = addToMask(Direction.NORTH, mask, north, x, y - 1 < 0 ? height - 1 : y - 1, cr, avatarX, avatarY);
            mask = addToMask(Direction.SOUTH, mask, south, x, y + 1 >= height ? 0 : y + 1, cr, avatarX, avatarY);
            mask = addToMask(Direction.EAST, mask, east, x + 1 >= width - 1 ? 0 : x + 1, y, cr, avatarX, avatarY);
            mask = addToMask(Direction.WEST, mask, west, x - 1 < 0 ? width - 1 : x - 1, y, cr, avatarX, avatarY);

        } else {

            Tile north = getTile(x, y - 1);
            Tile south = getTile(x, y + 1);
            Tile east = getTile(x + 1, y);
            Tile west = getTile(x - 1, y);

            mask = addToMask(Direction.NORTH, mask, north, x, y - 1, cr, avatarX, avatarY);
            mask = addToMask(Direction.SOUTH, mask, south, x, y + 1, cr, avatarX, avatarY);
            mask = addToMask(Direction.EAST, mask, east, x + 1, y, cr, avatarX, avatarY);
            mask = addToMask(Direction.WEST, mask, west, x - 1, y, cr, avatarX, avatarY);
        }

        return mask;

    }

    private int addToMask(Direction dir, int mask, Tile tile, int x, int y, Creature cr, int avatarX, int avatarY) {
        if (tile != null) {

            TileRule rule = tile.getRule();
            boolean canmove = false;
            if (rule != null) {
                if (cr != null) {
                    if (cr.getSails() && rule.has(TileAttrib.sailable)) {
                        canmove = true;
                    } else if (cr.getSails() && !rule.has(TileAttrib.unwalkable)) {
                        canmove = false;
                    } else if (cr.getSwims() && rule.has(TileAttrib.swimmable)) {
                        canmove = true;
                    } else if (cr.getSwims() && !rule.has(TileAttrib.unwalkable)) {
                        canmove = false;
                    } else if (cr.getFlies() && !rule.has(TileAttrib.unflyable)) {
                        canmove = true;
                    } else if (rule.has(TileAttrib.creatureunwalkable)) {
                        canmove = false;
                    } else if (!rule.has(TileAttrib.unwalkable)) {
                        canmove = true;
                    }
                } else {
                    TransportContext tc = GameScreen.context.getTransportContext();
                    if (tc == null || id != Maps.WORLD.getId() || tc == TransportContext.FOOT) {
                        if (!rule.has(TileAttrib.unwalkable) || rule == TileRule.ship || rule.has(TileAttrib.chest) || rule == TileRule.horse || rule == TileRule.balloon) {
                            canmove = true;
                        }
                    } else if (tc == TransportContext.HORSE) {
                        if (!rule.has(TileAttrib.creatureunwalkable) && !rule.has(TileAttrib.unwalkable)) {
                            canmove = true;
                        } else {
                            canmove = false;
                        }
                    } else if (tc == TransportContext.SHIP) {
                        if (rule.has(TileAttrib.sailable)) {
                            canmove = true;
                        } else {
                            canmove = false;
                        }
                    } else if (tc == TransportContext.BALLOON) {
                        if (!rule.has(TileAttrib.unflyable)) {
                            canmove = true;
                        } else {
                            canmove = false;
                        }
                    }
                }
            } else {
                canmove = false;
            }

            //NPCs cannot go thru the secret doors or walk where the avatar is
            if (cr != null) {
                if (tile.getIndex() == 73 || (avatarX == x && avatarY == y)) {
                    canmove = false;
                }
            }

            //see if another person is there
            if (city != null) {
                for (Person p : city.getPeople()) {
                    if (p == null || p.isRemovedFromMap()) {
                        continue;
                    }
                    if (p.getX() == x && p.getY() == y) {
                        canmove = false;
                        break;
                    }
                }
            }

            for (Creature cre : creatures) {
                if (cre.currentX == x && cre.currentY == y) {
                    canmove = false;
                    break;
                }
            }

            if (combatPlayers != null) {
                for (PartyMember p : combatPlayers) {
                    if (p.combatCr == null || p.fled) {
                        continue;
                    }
                    if (p.combatCr.currentX == x && p.combatCr.currentY == y) {
                        canmove = false;
                        break;
                    }
                }
            }

            if (rule == null || canmove || isDoorOpen(x, y)) {
                mask = Direction.addToMask(dir, mask);
            }
        } else {
			//if the tile is not on the map then it is OOB, 
            //so add this direction anyway so that monster flee operations work.
            if (cr != null && cr.getDamageStatus() == CreatureStatus.FLEEING) {
                mask = Direction.addToMask(dir, mask);
            }
        }
        return mask;
    }

    public void removeJoinedPartyMemberFromPeopleList(Party party) {
        if (city == null) {
            return;
        }
        for (PartyMember pm : party.getMembers()) {
            String name = pm.getPlayer().name;
            for (int i = 0; i < city.getPeople().length; i++) {
                Person p = city.getPeople()[i];
                if (p != null
                        && p.getRole() != null
                        && p.getRole().getRole().equals("companion")
                        && p.getConversation() != null
                        && p.getConversation().getName().equals(name)) {

                    city.getPeople()[i] = null;
                }
            }
        }
    }

    public DoorStatus getDoor(int x, int y) {
        for (DoorStatus ds : doors) {
            if (ds.x == x && ds.y == y) {
                return ds;
            }
        }
        return null;
    }

    public boolean unlockDoor(int x, int y) {
        DoorStatus ds = getDoor(x, y);
        if (ds != null) {
            ds.locked = false;
            return true;
        }
        return false;
    }

    public boolean openDoor(int x, int y) {
        DoorStatus ds = getDoor(x, y);
        if (ds != null && !ds.locked) {
            ds.openedTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    /**
     * Door will stay open for 10 seconds and then close
     */
    public boolean isDoorOpen(int x, int y) {
        DoorStatus ds = getDoor(x, y);
        if (ds != null && System.currentTimeMillis() - ds.openedTime < 10000) {
            return true;
        }
        return false;
    }

    public boolean isDoorOpen(DoorStatus ds) {
        if (ds != null && System.currentTimeMillis() - ds.openedTime < 10000) {
            return true;
        }
        return false;
    }

    public class DoorStatus {

        public int x;
        public int y;
        public long openedTime;
        public boolean locked = false;

        private DoorStatus(int x, int y, boolean locked, long openedTime) {
            this.x = x;
            this.y = y;
            this.openedTime = openedTime;
            this.locked = locked;
        }
    }

    /**
     * Add item to inventory and add experience etc for a found item
     */
    public ItemMapLabels searchLocation(BaseScreen screen, Party p, int x, int y, int z) {
        SaveGame sg = p.getSaveGame();

        if (labels == null) {
            return null;
        }

        Label tmp = null;
        for (Label l : labels) {
            if (l.getX() == x && l.getY() == y) {
                tmp = l;
            }
        }
        if (tmp == null) {
            return null;
        }

        int expPoints = 0;
        ItemMapLabels label = ItemMapLabels.valueOf(ItemMapLabels.class, tmp.getName());
        boolean added = false;

        int conditions = label.getConditions();

        if ((conditions & SC_NEWMOONS) > 0 && !(GameScreen.trammelphase == 0 && GameScreen.feluccaphase == 0)) {
            return null;
        }

        if ((conditions & SC_FULLAVATAR) > 0) {
            for (int i = 0; i < 8; i++) {
                if (sg.karma[i] != 0) {
                    return null;
                }
            }
        }

        if ((conditions & SC_REAGENTDELAY) > 0 && (sg.moves & 0xF0) == sg.lastreagent) {
            return null;
        }

        switch (label) {

            case bell:
                if ((sg.items & Item.BELL.getLoc()) > 0) {
                    break;
                }
                sg.items |= Item.BELL.getLoc();
                p.adjustKarma(KarmaAction.FOUND_ITEM);
                expPoints = 400;
                added = true;
                break;
            case blackstone:
                if ((sg.stones & Stone.BLACK.getLoc()) > 0) {
                    break;
                }
                sg.stones |= Stone.BLACK.getLoc();
                p.adjustKarma(KarmaAction.FOUND_ITEM);
                expPoints = 200;
                added = true;
                break;
            case bluestone:
                if ((sg.stones & Stone.BLUE.getLoc()) > 0) {
                    break;
                }
                sg.stones |= Stone.BLUE.getLoc();
                p.adjustKarma(KarmaAction.FOUND_ITEM);
                expPoints = 200;
                added = true;
                break;
            case book:
                if ((sg.items & Item.BOOK.getLoc()) > 0) {
                    break;
                }
                sg.items |= Item.BOOK.getLoc();
                p.adjustKarma(KarmaAction.FOUND_ITEM);
                expPoints = 400;
                added = true;
                break;
            case candle:
                if ((sg.items & Item.CANDLE.getLoc()) > 0) {
                    break;
                }
                sg.items |= Item.CANDLE.getLoc();
                p.adjustKarma(KarmaAction.FOUND_ITEM);
                expPoints = 400;
                added = true;
                break;
            case compassionrune:
                if ((sg.runes & Virtue.COMPASSION.getLoc()) > 0) {
                    break;
                }
                sg.runes |= Virtue.COMPASSION.getLoc();
                p.adjustKarma(KarmaAction.FOUND_ITEM);
                expPoints = 100;
                added = true;
                break;
            case greenstone:
                if ((sg.stones & Stone.GREEN.getLoc()) > 0) {
                    break;
                }
                sg.stones |= Stone.GREEN.getLoc();
                p.adjustKarma(KarmaAction.FOUND_ITEM);
                expPoints = 200;
                added = true;
                break;
            case honestyrune:
                if ((sg.runes & Virtue.HONESTY.getLoc()) > 0) {
                    break;
                }
                sg.runes |= Virtue.HONESTY.getLoc();
                p.adjustKarma(KarmaAction.FOUND_ITEM);
                expPoints = 100;
                added = true;
                break;
            case honorrune:
                if ((sg.runes & Virtue.HONOR.getLoc()) > 0) {
                    break;
                }
                sg.runes |= Virtue.HONOR.getLoc();
                p.adjustKarma(KarmaAction.FOUND_ITEM);
                expPoints = 100;
                added = true;
                break;
            case horn:
                if ((sg.items & Item.HORN.getLoc()) > 0) {
                    break;
                }
                sg.items |= Item.HORN.getLoc();
                p.adjustKarma(KarmaAction.FOUND_ITEM);
                expPoints = 400;
                added = true;
                break;
            case humilityrune:
                if ((sg.runes & Virtue.HUMILITY.getLoc()) > 0) {
                    break;
                }
                sg.runes |= Virtue.HUMILITY.getLoc();
                p.adjustKarma(KarmaAction.FOUND_ITEM);
                expPoints = 100;
                added = true;
                break;
            case justicerune:
                if ((sg.runes & Virtue.JUSTICE.getLoc()) > 0) {
                    break;
                }
                sg.runes |= Virtue.JUSTICE.getLoc();
                p.adjustKarma(KarmaAction.FOUND_ITEM);
                expPoints = 100;
                added = true;
                break;
            case orangestone:
                if ((sg.stones & Stone.ORANGE.getLoc()) > 0) {
                    break;
                }
                sg.stones |= Stone.ORANGE.getLoc();
                p.adjustKarma(KarmaAction.FOUND_ITEM);
                expPoints = 200;
                added = true;
                break;
            case purplestone:
                if ((sg.stones & Stone.PURPLE.getLoc()) > 0) {
                    break;
                }
                sg.stones |= Stone.PURPLE.getLoc();
                p.adjustKarma(KarmaAction.FOUND_ITEM);
                expPoints = 200;
                added = true;
                break;
            case redstone:
                if ((sg.stones & Stone.RED.getLoc()) > 0) {
                    break;
                }
                sg.stones |= Stone.RED.getLoc();
                p.adjustKarma(KarmaAction.FOUND_ITEM);
                expPoints = 200;
                added = true;
                break;
            case sacrificerune:
                if ((sg.runes & Virtue.SACRIFICE.getLoc()) > 0) {
                    break;
                }
                sg.runes |= Virtue.SACRIFICE.getLoc();
                p.adjustKarma(KarmaAction.FOUND_ITEM);
                expPoints = 100;
                added = true;
                break;
            case skull:
                if ((sg.items & Item.SKULL.getLoc()) > 0) {
                    break;
                }
                sg.items |= Item.SKULL.getLoc();
                p.adjustKarma(KarmaAction.FOUND_ITEM);
                expPoints = 400;
                added = true;
                break;
            case spiritualityrune:
                if ((sg.runes & Virtue.SPIRITUALITY.getLoc()) > 0) {
                    break;
                }
                sg.runes |= Virtue.SPIRITUALITY.getLoc();
                p.adjustKarma(KarmaAction.FOUND_ITEM);
                expPoints = 100;
                added = true;
                break;
            case valorrune:
                if ((sg.runes & Virtue.VALOR.getLoc()) > 0) {
                    break;
                }
                sg.runes |= Virtue.VALOR.getLoc();
                p.adjustKarma(KarmaAction.FOUND_ITEM);
                expPoints = 100;
                added = true;
                break;
            case wheel:
                if ((sg.items & Item.WHEEL.getLoc()) > 0) {
                    break;
                }
                sg.items |= Item.WHEEL.getLoc();
                p.adjustKarma(KarmaAction.FOUND_ITEM);
                expPoints = 400;
                added = true;
                break;
            case whitestone:
                if ((sg.stones & Stone.WHITE.getLoc()) > 0) {
                    break;
                }
                sg.stones |= Stone.WHITE.getLoc();
                p.adjustKarma(KarmaAction.FOUND_ITEM);
                expPoints = 200;
                added = true;
                break;
            case yellowstone:
                if ((sg.stones & Stone.YELLOW.getLoc()) > 0) {
                    break;
                }
                sg.stones |= Stone.YELLOW.getLoc();
                p.adjustKarma(KarmaAction.FOUND_ITEM);
                expPoints = 200;
                added = true;
                break;

            case mysticarmor:
                if (sg.armor[ArmorType.MYSTICROBE.ordinal()] > 0) {
                    break;
                }
                sg.armor[ArmorType.MYSTICROBE.ordinal()] += 8; //all party members would have it
                p.adjustKarma(KarmaAction.FOUND_ITEM);
                expPoints = 400;
                sg.lastreagent = sg.moves & 0xF0;
                added = true;
                break;
            case mysticswords:
                if (sg.weapons[WeaponType.MYSTICSWORD.ordinal()] > 0) {
                    break;
                }
                sg.weapons[WeaponType.MYSTICSWORD.ordinal()] += 8;
                p.adjustKarma(KarmaAction.FOUND_ITEM);
                expPoints = 400;
                sg.lastreagent = sg.moves & 0xF0;
                added = true;
                break;

            case mandrake1:
            case mandrake2:
                sg.reagents[Reagent.MANDRAKE.ordinal()] += new Random().nextInt(8) + 2;
                if (sg.reagents[Reagent.MANDRAKE.ordinal()] > 99) {
                    sg.reagents[Reagent.MANDRAKE.ordinal()] = 99;
                }
                p.adjustKarma(KarmaAction.FOUND_ITEM);
                sg.lastreagent = sg.moves & 0xF0;
                added = true;
                break;
            case nightshade1:
            case nightshade2:
                sg.reagents[Reagent.NIGHTSHADE.ordinal()] += new Random().nextInt(8) + 2;
                if (sg.reagents[Reagent.NIGHTSHADE.ordinal()] > 99) {
                    sg.reagents[Reagent.NIGHTSHADE.ordinal()] = 99;
                }
                p.adjustKarma(KarmaAction.FOUND_ITEM);
                sg.lastreagent = sg.moves & 0xF0;
                added = true;
                break;

            case telescope:
                if (screen instanceof GameScreen) {
                    GameScreen gameScreen = (GameScreen) screen;
                    gameScreen.peerTelescope();
                }
                break;
            case balloon:
                break;
            case lockelake:
                break;

            case maskofminax:
                if ((sg.items & Item.MASK_MINAX.getLoc()) > 0) {
                    break;
                }
                sg.items |= Item.MASK_MINAX.getLoc();
                p.adjustKarma(KarmaAction.FOUND_ITEM);
                expPoints = 200;
                sg.lastreagent = sg.moves & 0xF0;
                added = true;
                break;

            case rageofgod:
                if ((sg.items & Item.RAGE_GOD.getLoc()) > 0) {
                    break;
                }
                sg.items |= Item.RAGE_GOD.getLoc();
                p.adjustKarma(KarmaAction.FOUND_ITEM);
                expPoints = 400;
                sg.lastreagent = sg.moves & 0xF0;
                added = true;
                break;
                
            case ironrunemold:
                if ((sg.items & Item.RUNE_MOLD.getLoc()) > 0) {
                    break;
                }
                sg.items |= Item.RUNE_MOLD.getLoc();
                p.adjustKarma(KarmaAction.FOUND_ITEM);
                expPoints = 200;
                sg.lastreagent = sg.moves & 0xF0;
                added = true;
                break;

            case blackironore:
                if ((sg.items & Item.IRON_ORE.getLoc()) > 0) {
                    break;
                }
                sg.items |= Item.IRON_ORE.getLoc();
                p.adjustKarma(KarmaAction.FOUND_ITEM);
                expPoints = 200;
                sg.lastreagent = sg.moves & 0xF0;
                added = true;
                break;
                
            case magicparchment:
                if ((sg.items & Item.PARCH.getLoc()) > 0) {
                    break;
                }
                sg.items |= Item.PARCH.getLoc();
                p.adjustKarma(KarmaAction.FOUND_ITEM);
                expPoints = 200;
                sg.lastreagent = sg.moves & 0xF0;
                added = true;
                break;

            case greedrune:
                if ((sg.items & Item.GREED_RUNE.getLoc()) > 0) {
                    break;
                }
                sg.items |= Item.GREED_RUNE.getLoc();
                p.adjustKarma(KarmaAction.FOUND_ITEM);
                expPoints = 200;
                sg.lastreagent = sg.moves & 0xF0;
                added = true;
                break;
                
            case songhumility:
                if ((sg.items & Item.SONG_HUM.getLoc()) > 0) {
                    break;
                }
                sg.items |= Item.SONG_HUM.getLoc();
                p.adjustKarma(KarmaAction.FOUND_ITEM);
                expPoints = 400;
                sg.lastreagent = sg.moves & 0xF0;
                added = true;
                break;

            default:
                break;

        }

        if (expPoints > 0) {
            p.getMember(0).awardXP(expPoints);
        }

        if (added) {
            return label;
        }

        return null;
    }

    @XmlTransient
    public List<PartyMember> getCombatPlayers() {
        return combatPlayers;
    }

    public void setCombatPlayers(List<PartyMember> combatPlayers) {
        this.combatPlayers = combatPlayers;
    }

    public Stage getSurfaceMapStage() {
        return surfaceMapStage;
    }

    public void setSurfaceMapStage(Stage surfaceMapStage) {
        this.surfaceMapStage = surfaceMapStage;
    }

}
