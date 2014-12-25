package ultima;

import java.util.Random;

import objects.Aura;
import objects.BaseMap;
import objects.Drawable;
import objects.Party;
import objects.Portal;
import objects.Tile;

import com.badlogic.gdx.maps.tiled.TiledMap;


public class Context implements Constants {
	
    private Party party;
    private BaseMap currentMap;
    private TiledMap currentTiledMap;
    private int locationMask;
    
    private int line, col;
    private int moonPhase = 0;
    private Direction windDirection = Direction.NORTH;
    private int windCounter;
    private boolean windLock;
    private Aura aura = new Aura();    
    private int horseSpeed;
    private int opacity;
    private TransportContext transportContext;
    private Drawable lastShip;
    private Drawable currentShip;

    private long lastCommandTime = System.currentTimeMillis();
    private Random rand = new Random();
    
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
	public int getWindCounter() {
		return windCounter;
	}
	public boolean isWindLock() {
		return windLock;
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
	public long getLastCommandTime() {
		return lastCommandTime;
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
	public void setWindCounter(int windCounter) {
		this.windCounter = windCounter;
	}
	public void setWindLock(boolean windLock) {
		this.windLock = windLock;
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
	public void setLastCommandTime(long lastCommandTime) {
		this.lastCommandTime = lastCommandTime;
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

    public void saveGame(float x, float y, float z, Direction orientation, Maps map) {
    	
    	if (map == Maps.WORLD) {
    		party.getSaveGame().x = (int)x;
    		party.getSaveGame().y = (int)y;
			party.getSaveGame().dngx = (int)x;
			party.getSaveGame().dngy = (int)y;
	    	party.getSaveGame().dnglevel = 0;
    		party.getSaveGame().orientation = 0;
    	} else {
    		Portal p = Maps.WORLD.getMap().getPortal(map.getId());
    		party.getSaveGame().x = (int)x;
    		party.getSaveGame().y = (int)y;
    		party.getSaveGame().dngx = p.getX();
    		party.getSaveGame().dngy = p.getY();
    		party.getSaveGame().dnglevel = (int)z;
    		party.getSaveGame().orientation = orientation.getVal()-1;
    	}

		party.getSaveGame().location = map.getId();

		try {
			party.getSaveGame().write(PARTY_SAV_BASE_FILENAME);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
     * Default handler for slowing movement.
     * Returns true if slowed, false if not slowed
     */
    public boolean slowedByTile(Tile tile) {
        boolean slow;
        
        TileRule ts = tile.getRule();
        if (ts == null) return false;
        
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
     * Slowed depending on the direction of object with respect to wind direction
     * Returns true if slowed, false if not slowed
     */
    public boolean slowedByWind(Direction direction) {
        /* 1 of 4 moves while trying to move into the wind succeeds */
        if (direction == this.windDirection)
            return (party.getSaveGame().moves % 4) != 0;
        /* 1 of 4 moves while moving directly away from wind fails */
        else if (direction == Direction.reverse(windDirection))
            return (party.getSaveGame().moves % 4) == 3;    
        else
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
    

}
