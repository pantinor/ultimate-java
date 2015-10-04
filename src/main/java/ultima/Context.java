package ultima;

import java.util.Random;

import objects.Aura;
import objects.BaseMap;
import objects.Creature;
import objects.Drawable;
import objects.Party;
import objects.Party.PartyMember;
import objects.Portal;
import objects.Tile;

import com.badlogic.gdx.maps.tiled.TiledMap;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import objects.JournalEntries;
import objects.JournalEntry;
import util.PartyDeathException;
import util.XORShiftRandom;

public class Context implements Constants {

    private Party party;
    private BaseMap currentMap;
    private TiledMap currentTiledMap;
    private int locationMask;
    private JournalEntries journalEntries;
    private int line, col;
    private int moonPhase = 0;
    private Direction windDirection = Direction.NORTH;
    private int windCounter;
    private int balloonMovementCounter;
    private Aura aura = new Aura();
    private int horseSpeed;
    private int opacity;
    private TransportContext transportContext;
    private Drawable lastShip;
    private Drawable currentShip;

    private long lastCommandTime = System.currentTimeMillis();

    private final Random rand = new XORShiftRandom();

    public int getLine() {
        return line;
    }

    public int getCol() {
        return col;
    }

    public int getMoonPhase() {
        return moonPhase;
    }

    public Direction getWindDirection() {
        return windDirection;
    }

    public int incrementWindCounter() {
        return ++windCounter;
    }

    public void setWindCounter(int v) {
        windCounter = v;
    }

    public int incrementBalloonCounter() {
        return ++balloonMovementCounter;
    }

    public void setBalloonCounter(int v) {
        balloonMovementCounter = v;
    }

    public int getHorseSpeed() {
        return horseSpeed;
    }

    public int getOpacity() {
        return opacity;
    }

    public TransportContext getTransportContext() {
        return transportContext;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public void setMoonPhase(int moonPhase) {
        this.moonPhase = moonPhase;
    }

    public void setWindDirection(Direction windDirection) {
        this.windDirection = windDirection;
    }

    public void setHorseSpeed(int horseSpeed) {
        this.horseSpeed = horseSpeed;
    }

    public void setOpacity(int opacity) {
        this.opacity = opacity;
    }

    public void setTransportContext(TransportContext transportContext) {
        this.transportContext = transportContext;
    }

    public TiledMap getCurrentTiledMap() {
        return currentTiledMap;
    }

    public void setCurrentTiledMap(TiledMap currentTiledMap) {
        this.currentTiledMap = currentTiledMap;
    }

    public BaseMap getCurrentMap() {
        return currentMap;
    }

    public void setCurrentMap(BaseMap currentMap) {
        this.currentMap = currentMap;
    }

    public Party getParty() {
        return party;
    }

    public void setParty(Party party) {
        this.party = party;
        party.setContext(this);
    }

    public void loadJournalEntries() {
        
        try {
            File file = new File("journal.save");
            JAXBContext jaxbContext = JAXBContext.newInstance(JournalEntries.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            this.journalEntries = (JournalEntries) jaxbUnmarshaller.unmarshal(file);
        } catch (Exception e) {
            this.journalEntries = new JournalEntries();
        }
        
    }
    
    public void addEntry(String name, Maps map, String text) {
        
        String[] words = text.split("\\s+");
        StringBuilder sb = new StringBuilder();
        List<StringBuilder> texts = new ArrayList<>();
        for (String w : words) {
            sb.append(w).append(" ");
            if (sb.length() > 90) {
                texts.add(sb);
                sb = new StringBuilder();
            }
        }
        texts.add(sb);

        for (StringBuilder b : texts) {
            JournalEntry je = new JournalEntry(name, map.getLabel(), b.toString().trim());
            this.journalEntries.add(je);
        }
    }

    public void saveGame(float x, float y, float z, Direction orientation, Maps map) {

        if (map == Maps.WORLD) {
            party.getSaveGame().x = (int) x;
            party.getSaveGame().y = (int) y;
            party.getSaveGame().dngx = (int) x;
            party.getSaveGame().dngy = (int) y;
            party.getSaveGame().dnglevel = 0;
            party.getSaveGame().orientation = 0;
        } else {
            Portal p = Maps.WORLD.getMap().getPortal(map.getId());
            party.getSaveGame().x = (int) x;
            party.getSaveGame().y = (int) y;
            party.getSaveGame().dngx = (p == null ? (int) x : p.getX());
            party.getSaveGame().dngy = (p == null ? (int) y : p.getY());
            party.getSaveGame().dnglevel = (int) z;
            party.getSaveGame().orientation = orientation.getVal() - 1;
        }

        party.getSaveGame().location = map.getId();

        try {
            party.getSaveGame().write(PARTY_SAV_BASE_FILENAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        try {
            File file = new File("journal.save");
            JAXBContext jaxbContext = JAXBContext.newInstance(JournalEntries.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
            marshaller.marshal(this.journalEntries, file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public JournalEntries getJournal() {
        return this.journalEntries;
    }

    public int getLocationMask() {
        return locationMask;
    }

    public void setLocationMask(int locationMask) {
        this.locationMask = locationMask;
    }

    public Maps getCombatMapForTile(Tile combatantTile, TransportContext transport, int x, int y) {

        boolean fromShip = false;
        boolean toShip = false;

        Tile tileUnderneathAvatar = currentMap.getTile(x, y);

        if (transport == TransportContext.SHIP) {
            fromShip = true;
        }

        if (combatantTile.getRule() == TileRule.ship) {
            toShip = true;
        }

        if (fromShip && toShip) {
            return Maps.SHIPSHIP_CON;
        }

        if (toShip) {
            return Maps.SHORSHIP_CON;
        } else if (fromShip && tileUnderneathAvatar.getRule() == TileRule.water) {
            return Maps.SHIPSEA_CON;
        } else if (tileUnderneathAvatar.getRule() == TileRule.water) {
            return Maps.SHORE_CON;
        } else if (fromShip && tileUnderneathAvatar.getRule() != TileRule.water) {
            return Maps.SHIPSHOR_CON;
        }

        if (tileUnderneathAvatar.getCombatMap() != null) {
            return tileUnderneathAvatar.getCombatMap();
        }

        return Maps.BRICK_CON;
    }

    public Aura getAura() {
        return aura;
    }

    public void setAura(AuraType t, int duration) {
        this.aura.set(t, duration);
    }

    /**
     * Default handler for slowing movement. Returns true if slowed, false if
     * not slowed
     */
    public boolean slowedByTile(Tile tile) {
        boolean slow;

        TileRule ts = tile.getRule();
        if (ts == null) {
            return false;
        }

        switch (ts.getSpeed()) {
            case SLOW:
                slow = rand.nextInt(8) == 0;
                break;
            case VSLOW:
                slow = rand.nextInt(4) == 0;
                break;
            case VVSLOW:
                slow = rand.nextInt(2) == 0;
                break;
            case FAST:
            default:
                slow = false;
                break;
        }

        return slow;
    }

    /**
     * Slowed depending on the direction of object with respect to wind
     * direction Returns true if slowed, false if not slowed
     */
    public boolean slowedByWind(Direction direction) {
        /* 1 of 4 moves while trying to move into the wind succeeds */
        if (direction == this.windDirection) {
            return (party.getSaveGame().moves % 4) != 0;
        } /* 1 of 4 moves while moving directly away from wind fails */ else if (direction == Direction.reverse(windDirection)) {
            return (party.getSaveGame().moves % 4) == 3;
        } else {
            return false;
        }
    }

    public void damageShip(int minDamage, int maxDamage) {
        if (transportContext == TransportContext.SHIP) {
            int damage = minDamage >= 0 && minDamage < maxDamage ? rand.nextInt(maxDamage + 1 - minDamage) + minDamage : maxDamage;
            party.adjustShipHull(-damage);
        }
    }

    public boolean getChestTrapHandler(PartyMember pm) throws PartyDeathException {

        TileEffect trapType;
        int randNum = rand.nextInt(4);
        boolean passTest = (rand.nextInt(2) == 0);

        /* Chest is trapped! 50/50 chance */
        if (passTest) {
            /* Figure out which trap the chest has */
            switch (randNum) {
                case 0:
                    trapType = TileEffect.FIRE;
                    break; /* acid trap (56% chance - 9/16) */

                case 1:
                    trapType = TileEffect.SLEEP;
                    break; /* sleep trap (19% chance - 3/16) */

                case 2:
                    trapType = TileEffect.POISON;
                    break; /* poison trap (19% chance - 3/16) */

                case 3:
                    trapType = TileEffect.LAVA;
                    break; /* bomb trap (6% chance - 1/16) */

                default:
                    trapType = TileEffect.FIRE;
                    break;
            }

            if (trapType == TileEffect.FIRE) {
                Ultima4.hud.add("Acid Trap!");
                Sounds.play(Sound.ACID);
            } else if (trapType == TileEffect.POISON) {
                Ultima4.hud.add("Poison Trap!");
                Sounds.play(Sound.POISON_EFFECT);
            } else if (trapType == TileEffect.SLEEP) {
                Ultima4.hud.add("Sleep Trap!");
                Sounds.play(Sound.SLEEP);
            } else if (trapType == TileEffect.LAVA) {
                Ultima4.hud.add("Bomb Trap!");
                Sounds.play(Sound.BOOM);
            }

            if (pm.getPlayer().dex + 25 < rand.nextInt(100)) {
                if (trapType == TileEffect.LAVA) {/* bomb trap */
                    party.applyEffect(trapType);
                } else {
                    pm.applyEffect(trapType);
                }
            } else {
                Ultima4.hud.add("Evaded!");
            }

            return true;
        }

        return false;
    }

    public Drawable getCurrentShip() {
        return currentShip;
    }

    public void setCurrentShip(Drawable currentShip) {
        this.currentShip = currentShip;
    }

    public Drawable getLastShip() {
        return lastShip;
    }

    public void setLastShip(Drawable lastShip) {
        this.lastShip = lastShip;
    }

    public Maps getCombatMap(Creature c, BaseMap bm, int creatureX, int creatureY, int avatarX, int avatarY) {
        Tile ct = bm.getTile(creatureX, creatureY);
        Maps cm = ct.getCombatMap();

        TileRule ptr = bm.getTile(avatarX, avatarY).getRule();
        if (c.getSwims() && !ptr.has(TileAttrib.unwalkable)) {
            cm = Maps.SHORE_CON;
        } else if (c.getSails() && !ptr.has(TileAttrib.unwalkable)) {
            cm = Maps.SHORSHIP_CON;
        }

        if (transportContext == TransportContext.SHIP) {
            if (c.getSwims()) {
                cm = Maps.SHIPSEA_CON;
            } else if (c.getSails()) {
                cm = Maps.SHIPSHIP_CON;
            } else {
                cm = Maps.SHIPSHOR_CON;
            }
        }

        return cm;
    }

    public long getLastCommandTime() {
        return lastCommandTime;
    }

    public void setLastCommandTime(long lastCommandTime) {
        this.lastCommandTime = lastCommandTime;
    }

}
