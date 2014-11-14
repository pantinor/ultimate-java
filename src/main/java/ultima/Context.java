package ultima;

import com.badlogic.gdx.maps.tiled.TiledMap;

import objects.BaseMap;


public class Context implements Constants {
	
    //private Party party;
    //private SaveGame saveGame;
    private BaseMap currentMap;
    private TiledMap currentTiledMap;

    private int line, col;
    //private StatsArea stats;
    private int moonPhase = 0;
    private Direction windDirection = Direction.NORTH;
    private int windCounter;
    private boolean windLock;
    //private Aura aura;    
    private int horseSpeed;
    private int opacity;
    private TransportContext transportContext;
    private long lastCommandTime;
    private Object lastShip;
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
	public Object getLastShip() {
		return lastShip;
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
	public void setLastShip(Object lastShip) {
		this.lastShip = lastShip;
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

    
    

}
