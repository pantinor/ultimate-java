package util;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.removeActor;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import objects.BaseMap;
import objects.Conversation;
import objects.Creature;
import objects.Drawable;
import objects.Party.PartyMember;
import objects.Person;
import objects.ProjectileActor;
import objects.Tile;
import objects.TileSet;

import org.apache.commons.io.IOUtils;
import org.lwjgl.BufferUtils;

import ultima.BaseScreen;
import ultima.CombatScreen;
import ultima.Constants;
import ultima.GameScreen;
import ultima.Sound;
import ultima.Sounds;
import ultima.Ultima4;
import ultima.Constants.DungeonTile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

public class Utils implements Constants {

    public static Random rand = new Random();

    public static String properCase(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    //This gives you a random number in between low (inclusive) and high (exclusive)
    public static int getRandomBetween(int low, int high) {
        return rand.nextInt(high - low) + low;
    }

    /**
     * load the tile indexes from the ULT file
     */
    public static void setMapTiles(BaseMap map, TileSet ts) throws Exception {

        if (map.getFname().length() < 1) {
            return;
        }

        InputStream is = new FileInputStream("assets/data/" + map.getFname().toLowerCase());
        byte[] bytes = IOUtils.toByteArray(is);

        Tile[] tiles = new Tile[map.getWidth() * map.getHeight()];

        if (map.getType() == MapType.world || map.getType() == MapType.city) {

            int pos = 0;
            for (int ych = 0; ych < map.getHeight() / 32; ych++) {
                for (int xch = 0; xch < map.getWidth() / 32; xch++) {
                    for (int y = 0; y < 32; y++) {
                        for (int x = 0; x < 32; x++) {
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
            for (int y = 0; y < map.getHeight(); y++) {
                for (int x = 0; x < map.getWidth(); x++) {
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
            for (int y = 0; y < map.getHeight(); y++) {
                for (int x = 0; x < map.getWidth(); x++) {
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

    public static List<AttackVector> getDirectionalActionPath(BaseMap combatMap, int dirmask, int x, int y, int minDistance, int maxDistance,
            boolean weaponCanAttackThroughObjects, boolean checkForCreatures, boolean isCannonBall) {

        List<AttackVector> path = new ArrayList<AttackVector>();

        /*
         * try every tile in the given direction, up to the given range.
         * Stop when the the range is exceeded, or the action is blocked.
         */
        int nx = x;
        int ny = y;

        for (int distance = minDistance; distance <= maxDistance; distance++) {

            /* make sure our action isn't taking us off the map */
            if (nx > combatMap.getWidth() - 1 || nx < 0 || ny > combatMap.getHeight() - 1 || ny < 0) {
                break;
            }

            boolean blocked = combatMap.isTileBlockedForRangedAttack(nx, ny, checkForCreatures);

            Tile tile = combatMap.getTile(nx, ny);
            boolean canAttackOverSolid = (tile != null && tile.getRule() == TileRule.solid_attackover && weaponCanAttackThroughObjects);

            if (!blocked || canAttackOverSolid || isCannonBall) {
                path.add(new AttackVector(nx, ny));
            } else {
                path.add(new AttackVector(nx, ny));
                break;
            }

            if (Direction.isDirInMask(Direction.NORTH, dirmask)) {
                ny--;
            }
            if (Direction.isDirInMask(Direction.SOUTH, dirmask)) {
                ny++;
            }
            if (Direction.isDirInMask(Direction.EAST, dirmask)) {
                nx++;
            }
            if (Direction.isDirInMask(Direction.WEST, dirmask)) {
                nx--;
            }

        }

        return path;
    }

    public static Direction getPath(MapBorderBehavior borderbehavior, int width, int height, int toX, int toY, int validMovesMask, boolean towards, int fromX, int fromY) {
        /* find the directions that lead [to/away from] our target */
        int directionsToObject = towards ? getRelativeDirection(borderbehavior, width, height, toX, toY, fromX, fromY) : ~getRelativeDirection(borderbehavior, width, height, toX, toY, fromX, fromY);

        /* make sure we eliminate impossible options */
        directionsToObject &= validMovesMask;

        /* get the new direction to move */
        if (directionsToObject > 0) {
            return Direction.getRandomValidDirection(directionsToObject);
        } /* there are no valid directions that lead to our target, just move wherever we can! */ else {
            return null;//Direction.getRandomValidDirection(validMovesMask);
        }
    }

    /**
     * Returns a mask of directions that indicate where one point is relative to
     * another. For instance, if the object at (x, y) is northeast of (c.x,
     * c.y), then this function returns (MASK_DIR(DIR_NORTH) |
     * MASK_DIR(DIR_EAST)) This function also takes into account map boundaries
     * and adjusts itself accordingly.
     */
    public static int getRelativeDirection(MapBorderBehavior borderbehavior, int width, int height, int toX, int toY, int fromX, int fromY) {
        int dx = 0, dy = 0;
        int dirmask = 0;

        /* adjust our coordinates to find the closest path */
        if (borderbehavior == MapBorderBehavior.wrap) {

            if (Math.abs(fromX - toX) > Math.abs(fromX + width - toX)) {
                fromX += width;
            } else if (Math.abs(fromX - toX) > Math.abs(fromX - width - toX)) {
                fromX -= width;
            }

            if (Math.abs(fromY - toY) > Math.abs(fromY + width - toY)) {
                fromY += height;
            } else if (Math.abs(fromY - toY) > Math.abs(fromY - width - toY)) {
                fromY -= height;
            }

            dx = fromX - toX;
            dy = fromY - toY;
        } else {
            dx = fromX - toX;
            dy = fromY - toY;
        }

        /* add x directions that lead towards to_x to the mask */
        if (dx < 0) {
            dirmask |= Direction.EAST.getMask();
        } else if (dx > 0) {
            dirmask |= Direction.WEST.getMask();
        }

        /* add y directions that lead towards to_y to the mask */
        if (dy < 0) {
            dirmask |= Direction.SOUTH.getMask();
        } else if (dy > 0) {
            dirmask |= Direction.NORTH.getMask();
        }

        /* return the result */
        return dirmask;
    }

    /**
     * Finds the movement distance (not using diagonals) from point a to point b
     * on a map, taking into account map boundaries and such.
     */
    public static int movementDistance(MapBorderBehavior borderbehavior, int width, int height, int fromX, int fromY, int toX, int toY) {
        int dirmask = 0;;
        int dist = 0;

        /* get the direction(s) to the coordinates */
        dirmask = getRelativeDirection(borderbehavior, width, height, toX, toY, fromX, fromY);

        if (borderbehavior == MapBorderBehavior.wrap) {
            if (Math.abs(fromX - toX) > Math.abs(fromX + width - toX)) {
                fromX += width;
            } else if (Math.abs(fromX - toX) > Math.abs(fromX - width - toX)) {
                fromX -= width;
            }

            if (Math.abs(fromY - toY) > Math.abs(fromY + width - toY)) {
                fromY += height;
            } else if (Math.abs(fromY - toY) > Math.abs(fromY - width - toY)) {
                fromY -= height;
            }
        }

        while (fromX != toX || fromY != toY) {

            if (fromX != toX) {
                if (Direction.isDirInMask(Direction.WEST, dirmask)) {
                    fromX--;
                } else {
                    fromX++;
                }
                dist++;
            }
            if (fromY != toY) {
                if (Direction.isDirInMask(Direction.NORTH, dirmask)) {
                    fromY--;
                } else {
                    fromY++;
                }
                dist++;
            }

        }

        return dist;
    }

    /**
     * using diagonals computes distance, used with finding nearest party member
     */
    public static int distance(MapBorderBehavior borderbehavior, int width, int height, int fromX, int fromY, int toX, int toY) {
        int dist = movementDistance(borderbehavior, width, height, fromX, fromY, toX, toY);

        if (dist <= 0) {
            return dist;
        }

        /* calculate how many fewer movements there would have been */
        dist -= Math.abs(fromX - toX) < Math.abs(fromY - toY) ? Math.abs(fromX - toX) : Math.abs(fromY - toY);

        return dist;
    }

    public static void animateAttack(Stage stage, final CombatScreen scr, PartyMember attacker, Direction dir, int x, int y, int range) {

        final AttackVector av = Utils.attack(scr.combatMap, attacker, dir, x, y, range);

        final ProjectileActor p = new ProjectileActor(scr, Color.RED, x, y, av.result);

        Vector3 v = scr.getMapPixelCoords(av.x, av.y);

        p.addAction(sequence(moveTo(v.x, v.y, av.distance * .1f), new Action() {
            public boolean act(float delta) {
                switch (p.res) {
                    case HIT:
                        p.resultTexture = CombatScreen.hitTile;
                        break;
                    case MISS:
                        p.resultTexture = CombatScreen.missTile;
                        break;
                }

                scr.replaceTile(av.leaveTileName, av.x, av.y);

                scr.finishPlayerTurn();

                return true;
            }
        }, fadeOut(.2f), removeActor(p)));

        stage.addActor(p);
    }

    private static AttackVector attack(BaseMap combatMap, PartyMember attacker, Direction dir, int x, int y, int range) {

        WeaponType wt = attacker.getPlayer().weapon;
        boolean weaponCanAttackThroughObjects = wt.getWeapon().getAttackthroughobjects();

        List<AttackVector> path = Utils.getDirectionalActionPath(combatMap, dir.getMask(), x, y, 1, range, weaponCanAttackThroughObjects, true, false);

        AttackVector target = null;
        boolean foundTarget = false;
        int distance = 1;
        for (AttackVector v : path) {
            AttackResult res = attackAt(combatMap, v, attacker, dir, range, distance);
            target = v;
            target.result = res;
            target.distance = distance;
            if (res != AttackResult.NONE) {
                foundTarget = true;
                break;
            }
            distance++;
        }

        if (wt.getWeapon().getLose() || (wt.getWeapon().getLosewhenranged() && (!foundTarget || distance > 1))) {
            if (attacker.loseWeapon() == WeaponType.HANDS) {
                Ultima4.hud.add("Last One!");
            }
        }

        if (wt.getWeapon().getLeavetile() != null && combatMap.getTile(target.x, target.y).walkable()) {
            target.leaveTileName = wt.getWeapon().getLeavetile();
        }

        return target;
    }

    public static void animateMagicAttack(Stage stage, final CombatScreen scr, PartyMember attacker, Direction dir, int x, int y, Spell spell, int minDamage, int maxDamage) {

        final AttackVector av = Utils.castSpellAttack(scr.combatMap, attacker, dir, x, y, minDamage, maxDamage);

        Color color = Color.WHITE;
        switch (spell) {
            case FIREBALL:
                color = Color.RED;
                break;
            case ICEBALL:
                color = Color.BLUE;
                break;
            case KILL:
                color = Color.WHITE;
                break;
            case MAGICMISSILE:
                color = Color.BLUE;
                break;
        }

        final ProjectileActor p = new ProjectileActor(scr, color, x, y, av.result);

        Vector3 v = scr.getMapPixelCoords(av.x, av.y);

        p.addAction(sequence(moveTo(v.x, v.y, av.distance * .1f), new Action() {
            public boolean act(float delta) {

                switch (p.res) {
                    case HIT:
                        p.resultTexture = CombatScreen.hitTile;
                        break;
                    case MISS:
                        p.resultTexture = CombatScreen.missTile;
                        break;
                }

                scr.replaceTile(av.leaveTileName, av.x, av.y);

                scr.finishPlayerTurn();

                return true;
            }
        }, fadeOut(.2f), removeActor(p)));

        stage.addActor(p);
    }

    private static AttackVector castSpellAttack(BaseMap combatMap, PartyMember attacker, Direction dir, int x, int y, int minDamage, int maxDamage) {

        List<AttackVector> path = Utils.getDirectionalActionPath(combatMap, dir.getMask(), x, y, 1, 11, true, true, false);

        AttackVector target = null;
        int distance = 1;
        for (AttackVector v : path) {
            AttackResult res = castAt(combatMap, v, attacker, minDamage, maxDamage);
            target = v;
            target.result = res;
            target.distance = distance;
            if (res != AttackResult.NONE) {
                break;
            }
            distance++;
        }

        return target;
    }

    private static AttackResult attackAt(BaseMap combatMap, AttackVector target, PartyMember attacker, Direction dir, int range, int distance) {
        AttackResult res = AttackResult.NONE;
        Creature creature = null;
        for (Creature c : combatMap.getCreatures()) {
            if (c.currentX == target.x && c.currentY == target.y) {
                creature = c;
                break;
            }
        }

        WeaponType wt = attacker.getPlayer().weapon;
        boolean wrongRange = (wt.getWeapon().getAbsolute_range() > 0 && (distance != range));

        if (creature == null || wrongRange) {
            if (!wt.getWeapon().getDontshowtravel()) {
            }
            return res;
        }

        if ((combatMap.getId() == Maps.ABYSS.getId() && !wt.getWeapon().getMagic()) || !attackHit(attacker, creature)) {
            Ultima4.hud.add("Missed!\n");
            res = AttackResult.MISS;
        } else {
            Sounds.play(Sound.NPC_STRUCK);
            dealDamage(attacker, creature, attacker.getDamage());
            res = AttackResult.HIT;
        }

        return res;
    }

    private static AttackResult castAt(BaseMap combatMap, AttackVector target, PartyMember attacker, int minDamage, int maxDamage) {

        AttackResult res = AttackResult.NONE;
        Creature creature = null;
        for (Creature c : combatMap.getCreatures()) {
            if (c.currentX == target.x && c.currentY == target.y) {
                creature = c;
                break;
            }
        }

        if (creature == null) {
            return res;
        }

        if (!attackHit(attacker, creature)) {
            Ultima4.hud.add("Missed!\n");
            res = AttackResult.MISS;
        } else {
            Sounds.play(Sound.NPC_STRUCK);

            int attackDamage = ((minDamage >= 0) && (minDamage < maxDamage))
                    ? rand.nextInt((maxDamage + 1) - minDamage) + minDamage
                    : maxDamage;

            dealDamage(attacker, creature, attackDamage);
            res = AttackResult.HIT;
        }

        return res;
    }

    public static void animateCannonFire(final BaseScreen screen, final Stage stage, final BaseMap map, final AttackVector av, final int sx, final int sy, final boolean avatarAttack) {

        Sounds.play(Sound.CANNON);

        final ProjectileActor p = new ProjectileActor(screen, Color.WHITE, sx, sy, av.result);

        Vector3 d = screen.getMapPixelCoords(av.x, av.y);

        p.addAction(sequence(moveTo(d.x, d.y, av.distance * .1f), Actions.run(new Runnable() {
            public void run() {
                switch (p.res) {
                    case HIT:
                        p.resultTexture = CombatScreen.hitTile;
                        map.removeCreature(av.impactedCreature);
                        if (av.impactedDrawable != null && av.impactedDrawable.getShipHull() <= 0) {
                            av.impactedDrawable.remove();
                        }
                        break;
                    case MISS:
                        p.resultTexture = CombatScreen.missTile;
                        break;
                }

                if (avatarAttack) {
                    Vector3 v = screen.getCurrentMapCoords();
                    screen.finishTurn((int) v.x, (int) v.y);
                }

            }
        }), Actions.fadeOut(.3f), removeActor(p)));

        stage.addActor(p);
    }

    public static AttackVector enemyfireCannon(Stage stage, BaseMap combatMap, Direction dir, int startX, int startY, int avatarX, int avatarY) {

        List<AttackVector> path = Utils.getDirectionalActionPath(combatMap, dir.getMask(), startX, startY, 1, 4, true, false, true);

        AttackVector target = null;
        int distance = 1;
        for (AttackVector v : path) {
            AttackResult res = fireAt(stage, combatMap, v, false, avatarX, avatarY);
            target = v;
            target.result = res;
            target.distance = distance;
            if (res != AttackResult.NONE) {
                break;
            }
            distance++;
        }

        return target;
    }

    public static AttackVector avatarfireCannon(Stage stage, BaseMap combatMap, Direction dir, int startX, int startY) {

        List<AttackVector> path = Utils.getDirectionalActionPath(combatMap, dir.getMask(), startX, startY, 1, 4, true, true, true);

        AttackVector target = null;
        int distance = 1;
        for (AttackVector v : path) {
            AttackResult res = fireAt(stage, combatMap, v, true, 0, 0);
            target = v;
            target.result = res;
            target.distance = distance;
            if (res != AttackResult.NONE) {
                break;
            }
            distance++;
        }

        return target;
    }

    private static AttackResult fireAt(Stage stage, BaseMap combatMap, AttackVector target, boolean avatarAttack, int avatarX, int avatarY) {

        AttackResult res = AttackResult.NONE;

        //check for ship
        Drawable ship = null;
        for (Actor a : stage.getActors()) {
            if (a instanceof Drawable) {
                Drawable d = (Drawable) a;
                if (d.getTile().getName().equals("ship") && d.getCx() == target.x && d.getCy() == target.y) {
                    ship = d;
                }
            }
        }

        if (ship != null) {
            ship.damageShip(-1, 10);
            target.impactedDrawable = ship;
            return AttackResult.HIT;
        }

        if (avatarAttack) {

            Creature creature = null;
            for (Creature c : combatMap.getCreatures()) {
                if (c.currentX == target.x && c.currentY == target.y) {
                    creature = c;
                    break;
                }
            }

            if (creature == null) {
                return res;
            }

            if (rand.nextInt(4) == 0) {
                res = AttackResult.HIT;
                target.impactedCreature = creature;
            } else {
                res = AttackResult.MISS;
            }

        } else if (target.x == avatarX && target.y == avatarY) {

            if (GameScreen.context.getTransportContext() == TransportContext.SHIP) {
                GameScreen.context.damageShip(-1, 10);
            } else {
                GameScreen.context.getParty().damageParty(10, 25);
            }

            res = AttackResult.HIT;
        }

        return res;
    }

    public static AttackResult attackHit(Creature attacker, PartyMember defender) {
        int attackValue = rand.nextInt(0x100) + attacker.getAttackBonus();
        int defenseValue = defender.getDefense();
        return attackValue > defenseValue ? AttackResult.HIT : AttackResult.MISS;
    }

    private static boolean attackHit(PartyMember attacker, Creature defender) {
        int attackValue = rand.nextInt(0x100) + attacker.getAttackBonus();
        int defenseValue = defender.getDefense();
        return attackValue > defenseValue;
    }

    public static boolean dealDamage(PartyMember attacker, Creature defender, int damage) {
        int xp = defender.getExp();
        if (!damageCreature(defender, damage, true)) {
            attacker.awardXP(xp);
            return false;
        }
        return true;
    }

    public static boolean dealDamage(Creature attacker, PartyMember defender) {
        int damage = attacker.getDamage();
        return defender.applyDamage(damage, true);
    }

    public static boolean damageCreature(Creature cr, int damage, boolean byplayer) {

        if (cr.getTile() != CreatureType.lord_british) {
            cr.setHP(Utils.adjustValueMin(cr.getHP(), -damage, 0));
        }

        switch (cr.getDamageStatus()) {

            case DEAD:
                if (byplayer) {
                    Ultima4.hud.add(String.format("%s Killed! Exp. %d", cr.getName(), cr.getExp()));
                } else {
                    Ultima4.hud.add(String.format("%s Killed!", cr.getName()));
                }
                return false;
            case FLEEING:
                Ultima4.hud.add(String.format("%s Fleeing!", cr.getName()));
                break;

            case CRITICAL:
                Ultima4.hud.add(String.format("%s Critical!", cr.getName()));
                break;

            case HEAVILYWOUNDED:
                Ultima4.hud.add(String.format("%s Heavily Wounded!", cr.getName()));
                break;

            case LIGHTLYWOUNDED:
                Ultima4.hud.add(String.format("%s Lightly Wounded!", cr.getName()));
                break;

            case BARELYWOUNDED:
                Ultima4.hud.add(String.format("%s Barely Wounded!", cr.getName()));
                break;
            case FINE:
                break;
            default:
                break;
        }

        return true;
    }

    public static Texture peerGem(TiledMapTileLayer layer, String[] ids, TextureAtlas atlas, int cx, int cy) throws Exception {
        BufferedImage sheet = ImageIO.read(new File("assets/tilemaps/tiles-vga.png"));
        BufferedImage canvas = new BufferedImage(16 * layer.getWidth(), 16 * layer.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < layer.getHeight(); y++) {
            for (int x = 0; x < layer.getWidth(); x++) {
                String val = ids[layer.getCell(x, layer.getHeight() - y - 1).getTile().getId()];
                DungeonTile tile = DungeonTile.getTileByName(val);
                if (tile == null) {
                    val = "brick_floor";
                }
                if (x == cx && y == cy) {
                    val = "avatar";
                }
                AtlasRegion ar = (AtlasRegion) atlas.findRegion(val);
                BufferedImage sub = sheet.getSubimage(ar.getRegionX(), ar.getRegionY(), 16, 16);
                canvas.getGraphics().drawImage(sub, x * 16, y * 16, 16, 16, null);
            }
        }

        Pixmap p = Utils.createPixmap(canvas.getWidth(), canvas.getHeight(), canvas, 0, 0);

        Texture t = new Texture(p);
        p.dispose();

        return t;
    }

    //used for telescope viewing
    public static Texture peerGem(Maps map, TextureAtlas atlas) throws Exception {

        Texture t = null;

        if (map.getMap().getType() == MapType.city) {

            BufferedImage sheet = ImageIO.read(new File("assets/tilemaps/tiles-vga.png"));
            BufferedImage canvas = new BufferedImage(16 * 32, 16 * 32, BufferedImage.TYPE_INT_ARGB);

            for (int y = 0; y < 32; y++) {
                for (int x = 0; x < 32; x++) {
                    Tile ct = map.getMap().getTile(x, y);
                    AtlasRegion ar = (AtlasRegion) atlas.findRegion(ct.getName());
                    BufferedImage sub = sheet.getSubimage(ar.getRegionX(), ar.getRegionY(), 16, 16);
                    canvas.getGraphics().drawImage(sub, x * 16, y * 16, 16, 16, null);
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
        BufferedImage canvas = new BufferedImage(16 * 64, 16 * 64, BufferedImage.TYPE_INT_ARGB);

        int startX = avatarX - 32;
        int startY = avatarY - 32;
        int endX = avatarX + 32;
        int endY = avatarY + 32;
        int indexX = 0;
        int indexY = 0;
        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                int cx = x;
                if (x < 0) {
                    cx = 256 + x;
                } else if (x >= 256) {
                    cx = x - 256;
                }
                int cy = y;
                if (y < 0) {
                    cy = 256 + y;
                } else if (y >= 256) {
                    cy = y - 256;
                }
                Tile ct = worldMap.getTile(cx, cy);
                AtlasRegion ar = (AtlasRegion) atlas.findRegion(ct.getName());
                BufferedImage sub = sheet.getSubimage(ar.getRegionX(), ar.getRegionY(), 16, 16);
                canvas.getGraphics().drawImage(sub, indexX * 16, indexY * 16, 16, 16, null);

                Creature cr = worldMap.getCreatureAt(cx, cy);
                if (cr != null) {
                    //canvas.getGraphics().setColor(java.awt.Color.RED);
                    canvas.getGraphics().fillRect(indexX * 16, indexY * 16, 16, 16);
                }

                indexX++;
            }
            indexX = 0;
            indexY++;
        }

        //add avatar in the middle
        //canvas.getGraphics().setColor(java.awt.Color.WHITE);
        canvas.getGraphics().fillRect((16 * 64) / 2, (16 * 64) / 2, 16, 16);

        java.awt.Image tmp = canvas.getScaledInstance(16 * 32, 16 * 32, Image.SCALE_AREA_AVERAGING);
        BufferedImage scaledCanvas = new BufferedImage(16 * 32, 16 * 32, BufferedImage.TYPE_INT_ARGB);
        scaledCanvas.getGraphics().drawImage(tmp, 0, 0, null);

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
     *
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
        for (int i = 0; i < 16; i++) {

            int pos = i * block;

            int questionFlag = 0;
            int respAffectsHumility = 0;
            int probTurnAway = 0;

            CharBuffer bb = BufferUtils.createCharBuffer(block);
            String[] strings = new String[12];
            int stringIndex = 0;

            for (int y = 0; y < block; y++) {

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
                        bb.put((char) bytes[pos]);
                    }
                }
                pos++;
            }
            Conversation c = new Conversation(i + 1, probTurnAway, questionFlag, respAffectsHumility, strings);
            dialogs.add(c);
        }
        return dialogs;

    }

    /**
     * Read the ULT file and parse the people
     *
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
        for (int i = startOffset; i < end; i++) {
            int index = bytes[i] & 0xff;
            if (index == 0) {
                count++;
                continue;
            }

            Person p = new Person();
            p.setId(count);
            p.setTileIndex(index);

            if (ts != null) {
                Tile t = ts.getTileByIndex(index);
                if (t == null) {
                    System.err.printf("tile index %s could not be found.\n", index);
                }
                p.setTile(t);
            }

            people[count] = p;
            count++;
        }

        startOffset = MAP_WIDTH * MAP_WIDTH + MAX_PEOPLE * 1;
        end = startOffset + MAX_PEOPLE;
        count = 0;
        for (int i = startOffset; i < end; i++) {
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
        for (int i = startOffset; i < end; i++) {
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
            if (m == 0) {
                p.setMovement(ObjectMovementBehavior.FIXED);
            } else if (m == 1) {
                p.setMovement(ObjectMovementBehavior.WANDER);
            } else if (m == 0x80) {
                p.setMovement(ObjectMovementBehavior.FOLLOW_AVATAR);
            } else if (m == 0xFF) {
                p.setMovement(ObjectMovementBehavior.ATTACK_AVATAR);
            }

            count++;
        }

        startOffset = MAP_WIDTH * MAP_WIDTH + MAX_PEOPLE * 7;
        end = startOffset + MAX_PEOPLE;
        count = 0;
        for (int i = startOffset; i < end; i++) {
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
        InputStream is = new FileInputStream("assets/xml/" + fname);
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
