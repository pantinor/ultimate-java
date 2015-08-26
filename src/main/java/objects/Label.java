package objects;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "label")
public class Label {

    private String name;
    private int x;
    private int y;
    private int z;

    @XmlAttribute
    public String getName() {
        return name;
    }

    @XmlAttribute
    public int getX() {
        return x;
    }

    @XmlAttribute
    public int getY() {
        return y;
    }

    @XmlAttribute
    public int getZ() {
        return z;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setZ(int z) {
        this.z = z;
    }

    @Override
    public String toString() {

        String template = "<object name=\"label\" type=\"label\" x=\"%s\" y=\"%s\" width=\"16\" height=\"16\">\n"
                + "<properties>\n"
                + "<property name=\"x\" value=\"%s\"/>\n"
                + "<property name=\"y\" value=\"%s\"/>\n"
                + "<property name=\"name\" value=\"%s\"/>\n"
                + "</properties>\n"
                + "</object>\n";

        return String.format(template, x * 16, y * 16, x, y, name);
    }

}
