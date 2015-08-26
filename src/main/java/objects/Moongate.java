package objects;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;

@XmlRootElement(name = "moongate")
public class Moongate {

    private String name;
    private int phase;
    private int x;
    private int y;

    private String d1;
    private String d2;
    private String d3;
    private int dm1;
    private int dm2;
    private int dm3;

    private int mapTileId;

    //if the moongate is active this texture will be not null
    private AtlasRegion currentTexture;

    @XmlAttribute
    public int getPhase() {
        return phase;
    }

    @XmlAttribute
    public int getX() {
        return x;
    }

    @XmlAttribute
    public int getY() {
        return y;
    }

    public void setPhase(int phase) {
        this.phase = phase;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public String toString() {

        String template = "<object name=\"moongate\" type=\"portal\" x=\"%s\" y=\"%s\" width=\"16\" height=\"16\">\n"
                + "<properties>\n"
                + "<property name=\"x\" value=\"%s\"/>\n"
                + "<property name=\"y\" value=\"%s\"/>\n"
                + "<property name=\"phase\" value=\"%s\"/>\n"
                + "</properties>\n"
                + "</object>\n";

        return String.format(template, x * 16, y * 16, x, y, phase);
    }

    public AtlasRegion getCurrentTexture() {
        return currentTexture;
    }

    public void setCurrentTexture(AtlasRegion currentTexture) {
        this.currentTexture = currentTexture;
    }

    public int getMapTileId() {
        return mapTileId;
    }

    public void setMapTileId(int mapTileId) {
        this.mapTileId = mapTileId;
    }

    @XmlAttribute
    public String getName() {
        return name;
    }

    @XmlAttribute
    public String getD1() {
        return d1;
    }

    @XmlAttribute
    public String getD2() {
        return d2;
    }

    @XmlAttribute
    public String getD3() {
        return d3;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setD1(String d1) {
        this.d1 = d1;
    }

    public void setD2(String d2) {
        this.d2 = d2;
    }

    public void setD3(String d3) {
        this.d3 = d3;
    }

    @XmlAttribute
    public int getDm1() {
        return dm1;
    }

    @XmlAttribute
    public int getDm2() {
        return dm2;
    }

    @XmlAttribute
    public int getDm3() {
        return dm3;
    }

    public void setDm1(int dm1) {
        this.dm1 = dm1;
    }

    public void setDm2(int dm2) {
        this.dm2 = dm2;
    }

    public void setDm3(int dm3) {
        this.dm3 = dm3;
    }

}
