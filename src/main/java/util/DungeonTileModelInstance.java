package util;

import ultima.Constants.DungeonTile;

import com.badlogic.gdx.graphics.g3d.ModelInstance;

public class DungeonTileModelInstance {

    private ModelInstance instance;
    private DungeonTile tile;
    private int level;
    public int x;
    public int y;

    public DungeonTileModelInstance(ModelInstance instance, DungeonTile tile, int level) {
        this.instance = instance;
        this.tile = tile;
        this.level = level;
    }

    public ModelInstance getInstance() {
        return instance;
    }

    public DungeonTile getTile() {
        return tile;
    }

    public int getLevel() {
        return level;
    }

    public void setInstance(ModelInstance instance) {
        this.instance = instance;
    }

    public void setTile(DungeonTile tile) {
        this.tile = tile;
    }

    public void setLevel(int level) {
        this.level = level;
    }

}
