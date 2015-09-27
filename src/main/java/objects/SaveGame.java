package objects;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;

import ultima.Constants;
import util.Utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;
import util.XORShiftRandom;

public class SaveGame implements Constants {

    public SaveGamePlayerRecord[] players = new SaveGamePlayerRecord[8];

    public int unknown1 = 0;
    public int moves = 0;
    public int food = 30000;
    public int gold = 200;
    public int torches = 2;
    public int gems = 0;
    public int keys = 0;
    public int sextants = 0;

    public int[] karma = {50, 50, 50, 50, 50, 50, 50, 50};
    public int[] armor = new int[8];
    public int[] weapons = new int[16];
    public int[] reagents = {0, 3, 4, 0, 0, 0, 0, 0};
    public int[] mixtures = new int[SPELL_MAX];

    public int lastrage = 0;
    
    public int items = 0;
    public int x = 0;
    public int y = 0;
    public int stones = 0;
    public int runes = 0;
    public int members = 1;
    public int transport = 0x1f; //avatar tile id

    public int balloonstate = 0;
    public int balloonfound = 0;
    public int balloonx = 0;
    public int balloony = 0;

    public int torchduration = 0;

    public int trammelphase = 0;
    public int feluccaphase = 0;
    public int shiphull = 50;
    public int lbintro = 0;
    public int lastcamp = 0;
    public int lastreagent = 0;
    public int lastmeditation = 0;
    public int lastvirtue = 0;
    public int dngx = 0;
    public int dngy = 0;
    //dungeon orientation
    public int orientation = 0;
    public int dnglevel = 0;
    //see Constants.Maps enum for locations
    public int location = 0;

    private final Random rand = new XORShiftRandom();

    Texture zstatsBox;

    public void write(String strFilePath) throws Exception {

        FileOutputStream fos = new FileOutputStream(strFilePath);
        LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(fos);

        dos.writeInt(unknown1);
        dos.writeInt(moves);

        for (int i = 0; i < 8; i++) {
            if (players[i] == null) {
                players[i] = new SaveGamePlayerRecord();
            }
            players[i].write(dos);
        }

        dos.writeInt(food);
        dos.writeShort(gold);

        for (int i = 0; i < 8; i++) {
            dos.writeShort(karma[i]);
        }

        dos.writeShort(torches);
        dos.writeShort(gems);
        dos.writeShort(keys);
        dos.writeShort(sextants);

        for (int i = 0; i < 8; i++) {
            dos.writeShort(armor[i]);
        }

        for (int i = 0; i < 16; i++) {
            dos.writeShort(weapons[i]);
        }

        for (int i = 0; i < 8; i++) {
            dos.writeShort(reagents[i]);
        }

        for (int i = 0; i < SPELL_MAX; i++) {
            dos.writeShort(mixtures[i]);
        }

        dos.writeShort(items);
        dos.writeByte(x);
        dos.writeByte(y);
        dos.writeByte(stones);
        dos.writeByte(runes);
        dos.writeShort(members);
        dos.writeShort(transport);
        dos.writeShort(balloonstate);
        dos.writeShort(trammelphase);
        dos.writeShort(feluccaphase);
        dos.writeShort(shiphull);
        dos.writeShort(lbintro);
        dos.writeShort(lastcamp);
        dos.writeShort(lastreagent);
        dos.writeShort(lastmeditation);
        dos.writeShort(lastvirtue);
        dos.writeByte(dngx);
        dos.writeByte(dngy);
        dos.writeShort(orientation);
        dos.writeShort(dnglevel);
        dos.writeShort(location);
        dos.writeShort(balloonfound);
        dos.writeShort(balloonx);
        dos.writeShort(balloony);
        dos.writeByte(lastrage);
        dos.close();

    }

    public void read(String strFilePath) throws Exception {
        InputStream is;
        LittleEndianDataInputStream dis = null;
        try {
            is = new FileInputStream(Gdx.files.internal(strFilePath).file());
            dis = new LittleEndianDataInputStream(is);
        } catch (Exception e) {
            throw new Exception("Could not read save game file.");
			//is = this.getClass().getResourceAsStream("/data/" + PARTY_SAV_BASE_FILENAME);
            //dis = new LittleEndianDataInputStream(is);
        }
        read(dis);
    }

    public void read(LittleEndianDataInputStream dis) throws Exception {

        unknown1 = dis.readInt();
        moves = dis.readInt();

        for (int i = 0; i < 8; i++) {
            players[i] = new SaveGamePlayerRecord();
            players[i].read(dis);
        }

        food = dis.readInt();
        gold = dis.readShort() & 0xff;

        for (int i = 0; i < 8; i++) {
            karma[i] = dis.readShort() & 0xff;
        }

        torches = dis.readShort() & 0xff;
        gems = dis.readShort() & 0xff;
        keys = dis.readShort() & 0xff;
        sextants = dis.readShort() & 0xff;

        for (int i = 0; i < 8; i++) {
            armor[i] = dis.readShort() & 0xff;
        }

        for (int i = 0; i < 16; i++) {
            weapons[i] = dis.readShort() & 0xff;

        }

        for (int i = 0; i < 8; i++) {
            reagents[i] = dis.readShort() & 0xff;
        }

        for (int i = 0; i < SPELL_MAX; i++) {
            mixtures[i] = dis.readShort() & 0xff;
        }

        items = dis.readShort() & 0xff;
        x = dis.readByte() & 0xff;
        y = dis.readByte() & 0xff;
        stones = dis.readByte() & 0xff;
        runes = dis.readByte() & 0xff;
        members = dis.readShort() & 0xff;
        transport = dis.readShort() & 0xff;
        balloonstate = dis.readShort() & 0xff;
        trammelphase = dis.readShort() & 0xff;
        feluccaphase = dis.readShort() & 0xff;
        shiphull = dis.readShort() & 0xff;
        lbintro = dis.readShort() & 0xff;
        lastcamp = dis.readShort() & 0xff;
        lastreagent = dis.readShort() & 0xff;
        lastmeditation = dis.readShort() & 0xff;
        lastvirtue = dis.readShort() & 0xff;
        dngx = dis.readByte() & 0xff;
        dngy = dis.readByte() & 0xff;
        orientation = dis.readShort() & 0xff;
        dnglevel = dis.readShort();
        location = dis.readShort() & 0xff;
        balloonfound = dis.readShort() & 0xff;
        balloonx = dis.readShort() & 0xff;
        balloony = dis.readShort() & 0xff;
        lastrage = dis.readByte() & 0xff;
        /* workaround of U4DOS bug to retain savegame compatibility */
        if (location == 0 && dnglevel == 0) {
            dnglevel = (short) 0xFFFF;
        }

        dis.close();
    }

    /**
     * The Ultima IV savegame player record data.
     */
    public class SaveGamePlayerRecord {

        public String name = "";

        public int hp = 0;
        public int hpMax = 0;
        public int xp = 0;
        public int str = 0;
        public int dex = 0;
        public int intel = 0;
        public int mp = 0;
        public int unknown = 0;

        public WeaponType weapon = WeaponType.HANDS;
        public ArmorType armor = ArmorType.NONE;
        public SexType sex = SexType.MALE;
        public ClassType klass = ClassType.MAGE;
        public StatusType status = StatusType.GOOD;

        public int write(LittleEndianDataOutputStream dos) throws Exception {

            dos.writeShort(hp);
            dos.writeShort(hpMax);
            dos.writeShort(xp);
            dos.writeShort(str);
            dos.writeShort(dex);
            dos.writeShort(intel);
            dos.writeShort(mp);
            dos.writeShort(unknown);
            dos.writeShort(weapon.ordinal());
            dos.writeShort(armor.ordinal());

            String paddedName = StringUtils.rightPad(name, 16);

            byte[] nameArray = paddedName.getBytes();
            for (int i = 0; i < 16; i++) {
                if (nameArray[i] == 32) {
                    nameArray[i] = 0;
                }
                dos.writeByte(nameArray[i]);
            }

            dos.writeByte(sex.getValue());
            dos.writeByte(klass.ordinal());
            dos.writeByte((byte) status.getValue());

            return 1;
        }

        public int read(LittleEndianDataInputStream dis) throws Exception {

            hp = dis.readShort();
            hpMax = dis.readShort();
            xp = dis.readShort();
            str = dis.readShort();
            dex = dis.readShort();
            intel = dis.readShort();
            mp = dis.readShort();
            unknown = dis.readShort();

            int s = dis.readShort();
            weapon = WeaponType.get(s);

            s = dis.readShort();
            armor = ArmorType.get(s);

            byte[] nameArray = new byte[16];
            boolean end = false;
            for (int i = 0; i < 16; i++) {
                byte b = dis.readByte();
                if (b == 0) {
                    end = true;
                };
                if (!end) {
                    nameArray[i] = b;
                }
            }
            name = new String(nameArray).trim();

            byte ch = dis.readByte();
            sex = SexType.get(ch);

            ch = dis.readByte();
            klass = ClassType.get(ch);

            ch = dis.readByte();
            status = StatusType.get(ch);

            return 1;
        }

        public int getMaxMp() {
            int max_mp = -1;

            switch (klass) {
                case MAGE: // mage: 200% of int
                    max_mp = intel * 2;
                    break;

                case DRUID: // druid: 150% of int
                    max_mp = intel * 3 / 2;
                    break;

                case BARD: // bard, paladin, ranger: 100% of int
                case PALADIN:
                case RANGER:
                    max_mp = intel;
                    break;

                case TINKER: // tinker: 50% of int
                    max_mp = intel / 2;
                    break;

                case FIGHTER: // fighter, shepherd: no mp at all
                case SHEPHERD:
                    max_mp = 0;
                    break;

                default:
            }

            /* mp always maxes out at 99 */
            if (max_mp > 99) {
                max_mp = 99;
            }

            return max_mp;
        }

        public int getLevel() {
            return hpMax / 100;
        }

        public int getMaxLevel() {
            int level = 1;
            int next = 100;

            while (xp >= next && level < 8) {
                level++;
                next <<= 1;
            }

            return level;
        }

        public void adjustMp(int pts) {
            mp = Utils.adjustValueMax(mp, pts, getMaxMp());
        }

        public boolean advanceLevel() {
            if (getLevel() == getMaxLevel()) {
                return false;
            }

            status = StatusType.GOOD;
            hpMax = getMaxLevel() * 100;
            hp = hpMax;

            /* improve stats by 1-8 each */
            str += rand.nextInt(8) + 1;
            dex += rand.nextInt(8) + 1;
            intel += rand.nextInt(8) + 1;

            if (str > 50) {
                str = 50;
            }
            if (dex > 50) {
                dex = 50;
            }
            if (intel > 50) {
                intel = 50;
            }
            return true;

        }

        /**
         * Performed at game init time only from Start Screen
         *
         * @param questionTree
         */
        public void adjuestAttribsPerKarma(int[] questionTree) {
            str = 15;
            dex = 15;
            intel = 15;

            for (int i = 8; i < 15; i++) {
                karma[questionTree[i]] += 5;
                Virtue v = Virtue.get(questionTree[i]);
                switch (v) {
                    case HONESTY:
                        intel += 3;
                        break;
                    case COMPASSION:
                        dex += 3;
                        break;
                    case VALOR:
                        str += 3;
                        break;
                    case JUSTICE:
                        intel++;
                        dex++;
                        break;
                    case SACRIFICE:
                        dex++;
                        str++;
                        break;
                    case HONOR:
                        intel++;
                        str++;
                        break;
                    case SPIRITUALITY:
                        intel++;
                        dex++;
                        str++;
                        break;
                    case HUMILITY:
                        /* no stats for you! */
                        break;
                }
            }
        }

        @Override
        public String toString() {
            return String.format("SaveGamePlayerRecord [name=%s, hp=%s, xp=%s, weapon=%s, armor=%s, sex=%s, klass=%s, status=%s]", name, hp, xp, weapon, armor, sex, klass, status);
        }

    }

    //to proper case
    public static String pc(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    public String[] getZstats() {

        StringBuilder sb1 = new StringBuilder();
        for (int i = 0; i < members; i++) {
            SaveGamePlayerRecord p = players[i];

            sb1.append(
                    pc(p.name) + "  "
                    + pc(p.klass.toString()) + "|"
                    + pc(p.sex.getDesc()) + "  "
                    + p.status.getValue() + "|"
            );

            sb1.append(
                    "MP: " + p.mp + "  LV: " + Math.round(p.hpMax / 100) + "|"
                    + "STR: " + p.str + "  HP: " + p.hp + "|"
                    + "DEX: " + p.dex + "  HM: " + p.hpMax + "|"
                    + "INT: " + p.intel + "  EX: " + p.xp + "|"
                    + "W: " + pc(p.weapon.toString()) + "|"
                    + "A: " + pc(p.armor.toString()) + "|");

            sb1.append("~");

        }

        StringBuilder sb2 = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            int count = weapons[i];
            if (count == 0) {
                continue;
            }
            sb2.append(count + " - " + pc(WeaponType.get(i).toString()) + "|");
        }

        StringBuilder sb3 = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int count = armor[i];
            if (count == 0) {
                continue;
            }
            sb3.append(count + " - " + pc(ArmorType.get(i).toString()) + "|");
        }

        StringBuilder sb4 = new StringBuilder();
        sb4.append(torches + " - Torches|");
        sb4.append(gems + " - Gems|");
        if (sextants > 0) {
            sb4.append(sextants + " - Sextant|");
        }
        sb4.append(keys + " - Keys| |");

        for (int i = 0; i < 8; i++) {
            Virtue v = Constants.Virtue.get(i);
            String st = ((this.stones & (1 << i)) > 0 ? "+STONE" : "");
            String ru = ((this.runes & (1 << i)) > 0 ? "+RUNE" : "");
            sb4.append(v.getAbbr() + ": " + this.karma[v.ordinal()] + " " + st + " " + ru + "|");
        }

        for (Item item : Constants.Item.values()) {
            if (!item.isVisible()) continue;
            sb4.append((this.items & (1 << item.ordinal())) > 0 ? item.getDesc() + "|" : "");
        }

        StringBuilder sb5 = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int count = reagents[i];
            if (count == 0) {
                continue;
            }
            sb5.append(count + " - " + pc(Reagent.get(i).toString()) + "|");
        }

        StringBuilder sb6 = new StringBuilder();
        for (int i = 0; i < SPELL_MAX; i++) {
            int count = mixtures[i];
            if (count == 0) {
                continue;
            }
            sb6.append(count + " - " + pc(Spell.get(i).toString()) + "|");
        }

        String[] ret = new String[6];
        ret[0] = sb1.toString();
        ret[1] = sb2.toString();
        ret[2] = sb3.toString();
        ret[3] = sb4.toString();
        ret[4] = sb5.toString();
        ret[5] = sb6.toString();

        return ret;
    }

    public void renderZstats(int showZstats, BitmapFont font, Batch batch, int SCREEN_HEIGHT) {

        if (zstatsBox == null) {
            Pixmap pixmap = new Pixmap(175, 490, Format.RGBA8888);
            pixmap.setColor(0f, 0f, 0f, 0.65f);
            pixmap.fillRectangle(0, 0, 175, 490);
            zstatsBox = new Texture(pixmap);
            pixmap.dispose();
        }

        batch.draw(zstatsBox, 5, SCREEN_HEIGHT - 5 - 490);

        int rx = 10;
        int ry = SCREEN_HEIGHT - 10;

        String[] pages = getZstats();
        if (showZstats >= STATS_PLAYER1 && showZstats <= STATS_PLAYER8) {
            // players
            String[] players = pages[0].split("\\~");
            for (int i = 0; i < players.length; i++) {
                String[] lines = players[i].split("\\|");
                if (i != showZstats - 1) {
                    continue;
                }
                rx = 10;
                ry = SCREEN_HEIGHT - 10;
                font.draw(batch, "Player " + (i + 1), rx, ry);
                ry = ry - 18;
                for (int j = 0; j < lines.length; j++) {
                    if (lines[j] == null || lines[j].length() < 1) {
                        continue;
                    }
                    font.draw(batch, lines[j], rx, ry);
                    ry = ry - 18;
                }
            }
        } else if (showZstats == STATS_WEAPONS) {
            String[] lines = pages[1].split("\\|");
            font.draw(batch, "Weapons", rx, ry);
            ry = ry - 18;
            for (int j = 0; j < lines.length; j++) {
                if (lines[j] == null || lines[j].length() < 1) {
                    continue;
                }
                font.draw(batch, lines[j], rx, ry);
                ry = ry - 18;
            }
        } else if (showZstats == STATS_ARMOR) {
            String[] lines = pages[2].split("\\|");
            font.draw(batch, "Armor", rx, ry);
            ry = ry - 18;
            for (int j = 0; j < lines.length; j++) {
                if (lines[j] == null || lines[j].length() < 1) {
                    continue;
                }
                font.draw(batch, lines[j], rx, ry);
                ry = ry - 18;
            }
        } else if (showZstats == STATS_ITEMS) {
            String[] lines = pages[3].split("\\|");
            font.draw(batch, "Items", rx, ry);
            ry = ry - 18;
            for (int j = 0; j < lines.length; j++) {
                if (lines[j] == null || lines[j].length() < 1) {
                    continue;
                }
                font.draw(batch, lines[j], rx, ry);
                ry = ry - 18;
            }
        } else if (showZstats == STATS_REAGENTS) {
            String[] lines = pages[4].split("\\|");
            font.draw(batch, "Reagents", rx, ry);
            ry = ry - 18;
            for (int j = 0; j < lines.length; j++) {
                if (lines[j] == null || lines[j].length() < 1) {
                    continue;
                }
                font.draw(batch, lines[j], rx, ry);
                ry = ry - 18;
            }
        } else if (showZstats == STATS_SPELLS) {
            String[] lines = pages[5].split("\\|");
            font.draw(batch, "Spell Mixtures", rx, ry);
            ry = ry - 18;
            for (int j = 0; j < lines.length; j++) {
                if (lines[j] == null || lines[j].length() < 1) {
                    continue;
                }
                font.draw(batch, lines[j], rx, ry);
                ry = ry - 18;
            }
        }

    }

}
