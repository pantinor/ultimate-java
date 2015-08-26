package objects;

import objects.Party.PartyMember;
import ultima.Constants;

public class HawkwindConversation extends Conversation implements Constants {

    /* Hawkwind text indexes */
    public static final int HW_SPEAKONLYWITH = 40;
    public static final int HW_RETURNWHEN = 41;
    public static final int HW_ISREVIVED = 42;
    public static final int HW_WELCOME = 43;
    public static final int HW_GREETING1 = 44;
    public static final int HW_GREETING2 = 45;
    public static final int HW_PROMPT = 46;
    public static final int HW_DEFAULT = 49;
    public static final int HW_ALREADYAVATAR = 50;
    public static final int HW_GOTOSHRINE = 51;
    public static final int HW_BYE = 52;

    Party party;

    public HawkwindConversation() {
        this.index = 1;
        this.turnAwayProb = 0;
        this.respAffectsHumility = 0;

        this.name = "Hawkwind";
        this.pronoun = "He";
    }

    public void setParty(Party p) {
        this.party = p;
    }

    public Topic matchTopic(String query) {

        if (query.equals("none") || query.equals("bye")) {
            return new Topic("bye", "\n\n" + hawkwindText[HW_BYE], null, null, null);
        }

        int virtue = -1, virtueLevel = -1;

        for (Virtue v : Virtue.values()) {
            if (query.toLowerCase().contains(v.toString().toLowerCase().substring(0, 4))) {
                virtue = v.ordinal();
                virtueLevel = party.getSaveGame().karma[v.ordinal()];
                break;
            }
        }

        String text;
        if (virtue != -1) {
            text = "\n";
            if (virtueLevel == 0) {
                text += hawkwindText[HW_ALREADYAVATAR] + "\n";
            } else if (virtueLevel < 80) {
                text += hawkwindText[(virtueLevel / 20) * 8 + virtue];
            } else if (virtueLevel < 99) {
                text += hawkwindText[3 * 8 + virtue];
            } else /* virtueLevel >= 99 */ {
                text = hawkwindText[4 * 8 + virtue] + "\n" + hawkwindText[HW_GOTOSHRINE];
            }

            text += "\n\n" + hawkwindText[HW_PROMPT];
        } else {
            text = hawkwindText[HW_DEFAULT];
        }

        return new Topic(query, text, null, null, null);

    }

    public String intro() {
        StringBuffer sb = new StringBuffer();
        PartyMember pm = party.getMember(0);

        if (pm.getPlayer().status == StatusType.SLEEPING || pm.getPlayer().status == StatusType.DEAD) {
            sb.append(hawkwindText[HW_SPEAKONLYWITH]);
            sb.append(pm.getPlayer().name);
            sb.append(hawkwindText[HW_RETURNWHEN] + "\n");
            sb.append(pm.getPlayer().name);
            sb.append(hawkwindText[HW_ISREVIVED]);
        } else {
            sb.append(hawkwindText[HW_WELCOME] + "\n");
            sb.append(pm.getPlayer().name + "\n");
            sb.append(hawkwindText[HW_GREETING1] + "\n");
            sb.append(hawkwindText[HW_GREETING2]);
        }

        party.adjustKarma(KarmaAction.HAWKWIND);

        return sb.toString();
    }

}
