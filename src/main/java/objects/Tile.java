package objects;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import ultima.Constants.Maps;
import ultima.Constants.TileAttrib;
import ultima.Constants.TileRule;

@XmlRootElement(name = "tile")
public class Tile {

    private String name;
    private TileRule rule;
    private int frames;
    private String animation;
    private int index;
    private String directions;
    private int rowtoswap;
    private boolean opaque;
    private boolean tiledInDungeon;
    private boolean usesReplacementTileAsBackground;
    private boolean usesWaterReplacementTileAsBackground;
    private Maps combatMap = Maps.BRICK_CON;

    @XmlAttribute
    public String getName() {
        return name;
    }

    @XmlAttribute(name = "rule")
    @XmlJavaTypeAdapter(TileRuleAdapter.class)
    public TileRule getRule() {
        return rule;
    }

    @XmlAttribute
    public int getFrames() {
        return frames;
    }

    @XmlAttribute
    public String getAnimation() {
        return animation;
    }

    @XmlAttribute
    public int getIndex() {
        return index;
    }

    @XmlAttribute
    public String getDirections() {
        return directions;
    }

    @XmlAttribute
    public int getRowtoswap() {
        return rowtoswap;
    }

    @XmlAttribute
    public boolean isOpaque() {
        return opaque;
    }

    @XmlAttribute
    public boolean getTiledInDungeon() {
        return tiledInDungeon;
    }

    @XmlAttribute
    public boolean getUsesReplacementTileAsBackground() {
        return usesReplacementTileAsBackground;
    }

    @XmlAttribute
    public boolean getUsesWaterReplacementTileAsBackground() {
        return usesWaterReplacementTileAsBackground;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRule(TileRule rule) {
        this.rule = rule;
    }

    public void setFrames(int frames) {
        this.frames = frames;
    }

    public void setAnimation(String animation) {
        this.animation = animation;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setDirections(String directions) {
        this.directions = directions;
    }

    public void setRowtoswap(int rowtoswap) {
        this.rowtoswap = rowtoswap;
    }

    public void setOpaque(boolean opaque) {
        this.opaque = opaque;
    }

    public void setTiledInDungeon(boolean tiledInDungeon) {
        this.tiledInDungeon = tiledInDungeon;
    }

    public void setUsesReplacementTileAsBackground(boolean usesReplacementTileAsBackground) {
        this.usesReplacementTileAsBackground = usesReplacementTileAsBackground;
    }

    public void setUsesWaterReplacementTileAsBackground(boolean usesWaterReplacementTileAsBackground) {
        this.usesWaterReplacementTileAsBackground = usesWaterReplacementTileAsBackground;
    }

    @Override
    public String toString() {
        return String.format("Tile [name=%s, rule=%s]", name, rule);
    }

    public boolean walkable() {
        if (rule != null && !rule.has(TileAttrib.unwalkable)) {
            return true;
        }
        return false;
    }

    public boolean climbable() {
        switch (index) {
            case 27:
            case 28: {
                return true;
            }
            default:

        }

        return false;
    }

    public Maps getCombatMap() {
        return combatMap;
    }

    public void setCombatMap(Maps combatMap) {
        this.combatMap = combatMap;
    }

}
