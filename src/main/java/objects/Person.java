package objects;

import com.badlogic.gdx.graphics.Texture;
import ultima.Constants;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import vendor.Vendor;

public class Person implements Constants {

    private int id;
    private int start_x;
    private int start_y;
    private int x;
    private int y;
    private ObjectMovementBehavior movement;
    private Tile tile;
    private int tileMapId;
    private int dialogId;
    private int tileIndex;

    private TextureRegion textureRegion;
    private Animation anim;
    private Vector3 currentPos;
    private Conversation conversation;
    private boolean isTalking = false;
    private PersonRole role;
    private Creature emulatingCreature;
    private boolean removedFromMap;
    private Vendor vendor;

    public int getId() {
        return id;
    }

    public int getStart_x() {
        return start_x;
    }

    public int getStart_y() {
        return start_y;
    }

    public ObjectMovementBehavior getMovement() {
        return movement;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setStart_x(int start_x) {
        this.start_x = start_x;
    }

    public void setStart_y(int start_y) {
        this.start_y = start_y;
    }

    public void setMovement(ObjectMovementBehavior movement) {
        this.movement = movement;
    }

    public Tile getTile() {
        return tile;
    }

    public void setTile(Tile tile) {
        this.tile = tile;
    }

    public int getTileMapId() {
        return tileMapId;
    }

    public void setTileMapId(int tileMapId) {
        this.tileMapId = tileMapId;
    }

    public int getDialogId() {
        return dialogId;
    }

    public void setDialogId(int dialogId) {
        this.dialogId = dialogId;
    }

    public String toTMXString() {

        String template = "<object name=\"%s\" type=\"%s\" x=\"%s\" y=\"%s\" width=\"32\" height=\"32\">\n"
                + "<properties>\n"
                + "<property name=\"id\" value=\"%s\"/>\n"
                + "<property name=\"tileType\" value=\"%s\"/>\n"
                + "<property name=\"movement\" value=\"%s\"/>\n"
                + "<property name=\"startX\" value=\"%s\"/>\n"
                + "<property name=\"startY\" value=\"%s\"/>\n"
                + "<property name=\"dialogId\" value=\"%s\"/>\n"
                + "</properties>\n"
                + "</object>\n";

        String name = (conversation != null ? conversation.getName() : "anonymous");

        return String.format(template,
                name, tile.getName(), start_x * 32, start_y * 32, id, tile.getName(), movement, start_x, start_y, dialogId);
    }

    public String toTMXString48() {

        String template = "<object id=\"%s\" name=\"%s\" type=\"%s\" x=\"%s\" y=\"%s\" width=\"48\" height=\"48\">\n"
                + "<properties>\n"
                + "<property name=\"movement\" value=\"%s\"/>\n"
                + "<property name=\"tileType\" value=\"%s\"/>\n"
                + "</properties>\n"
                + "</object>\n";

        if (this.vendor != null) {

            template = "<object id=\"%s\" name=\"%s\" type=\"%s\" x=\"%s\" y=\"%s\" width=\"48\" height=\"48\">\n"
                    + "<properties>\n"
                    + "<property name=\"movement\" value=\"%s\"/>\n"
                    + "<property name=\"inventoryType\" value=\"%s\"/>\n"
                    + "<property name=\"vendorName\" value=\"%s\"/>\n"
                    + "<property name=\"tileType\" value=\"%s\"/>\n"
                    + "</properties>\n"
                    + "</object>\n";

        }

        String name = (conversation != null ? conversation.getName() : toCamelCase(tile.getName()));

        String r = "FRIENDLY";
        if (role != null && role.getInventoryType() != null) {

            switch (role.getInventoryType()) {
                case WEAPON:
                case ARMOR:
                case FOOD:
                case REAGENT:
                case TINKER:
                case HORSE:
                case GUILDITEM:
                case TAVERNINFO:
                case TAVERN:
                    r = "MERCHANT";
                    break;
                case INN:
                    r = "INNKEEPER";
                    break;
                case HEALER:
                    r = "TEMPLE";
                    break;
                default:

            }

        }

        if (this.vendor != null) {
            return String.format(template, id,
                    vendor.getOwner(),
                    r, start_x * 48, start_y * 48,
                    movement,
                    vendor.getVendorType(),
                    vendor.getName(),
                    tile.getName());
        } else {
            return String.format(template, id,
                    name,
                    r, start_x * 48, start_y * 48,
                    movement,
                    tile.getName());
        }
    }
   
    public static String toCamelCase(String s) {
        if (s == null) {
            return null;
        }
        
        s = s.replace("_", " ");

        final StringBuilder ret = new StringBuilder(s.length());

        for (String word : s.split(" ")) {
            if (!word.isEmpty()) {
                ret.append(word.substring(0, 1).toUpperCase());
                ret.append(word.substring(1).toLowerCase());
            }
            if (!(ret.length() == s.length())) {
                ret.append(" ");
            }
        }

        return ret.toString();
    }

    @Override
    public String toString() {
        return String.format("Person [id=%s, start_x=%s, start_y=%s, dialogId=%s, role=%s tileIndex=%s conv: %s]", id, start_x, start_y, dialogId, role, tileIndex, conversation);
    }

    public int getTileIndex() {
        return tileIndex;
    }

    public void setTileIndex(int tileIndex) {
        this.tileIndex = tileIndex;
    }

    public Vector3 getCurrentPos() {
        return currentPos;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public void setCurrentPos(Vector3 currentPos) {
        this.currentPos = currentPos;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public Animation<TextureRegion> getAnim() {
        return anim;
    }

    public void setAnim(Animation anim) {
        this.anim = anim;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public boolean isTalking() {
        return isTalking;
    }

    public void setTalking(boolean isTalking) {
        this.isTalking = isTalking;
    }

    public PersonRole getRole() {
        return role;
    }

    public void setRole(PersonRole role) {
        this.role = role;
    }

    public Creature getEmulatingCreature() {
        return emulatingCreature;
    }

    public void setEmulatingCreature(Creature emulatingCreature) {
        this.emulatingCreature = emulatingCreature;
    }

    public TextureRegion getTextureRegion() {
        return textureRegion;
    }

    public void setTextureRegion(TextureRegion textureRegion) {
        this.textureRegion = textureRegion;
    }

    public boolean isRemovedFromMap() {
        return removedFromMap;
    }

    public void setRemovedFromMap(boolean removedFromMap) {
        this.removedFromMap = removedFromMap;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

}
