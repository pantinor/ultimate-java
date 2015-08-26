package objects;

import ultima.Constants;
import ultima.GameScreen;

public class LordBritishConversation extends Conversation implements Constants {

    public LordBritishConversation() {

        this.index = 1;
        this.turnAwayProb = 0;
        this.respAffectsHumility = 0;

        this.name = "Lord British";
        this.pronoun = "He";

        topics.add(new Topic("look", "Thou see the King with the Royal Sceptre.", null, null, null));
        topics.add(new Topic("name", "He says: My name is Lord British, sovereign of all Britannia!", null, null, null));
        topics.add(new Topic("job", "He says: I rule all Britannia and shall do my best to help thee!", null, null, null));

        topics.add(new Topic("truth", "He says:  Many truths can  be learned at  the Lycaeum.  It  lies on the  northwestern  shore of Verity  Isle!", null, null, null));
        topics.add(new Topic("love", "He says:  Look for the  meaning of Love  at Empath Abbey.  The Abbey sits  on the western  edge of the Deep  Forest! ", null, null, null));
        topics.add(new Topic("cour", "He says:  Serpent's Castle  on the Isle of  Deeds is where  Courage should  be sought!    ", null, null, null));
        topics.add(new Topic("honesty", "He says:  The fair towne  of Moonglow on  Verity Isle is  where the virtue  of Honesty  thrives!  ", null, null, null));
        topics.add(new Topic("compas", "He says:  The bards in the  towne of Britain  are well versed  in the virtue of  Compassion!", null, null, null));
        topics.add(new Topic("valor", "He says:  Many valiant  fighters come  from Jhelom  in the Valarian  Isles!", null, null, null));
        topics.add(new Topic("justice", "He says:  In the city of  Yew, in the Deep  Forest, Justice  is served!", null, null, null));
        topics.add(new Topic("sacr", "He says:  Minoc, towne of  self-sacrifice,  lies on the  eastern shores  of Lost Hope  Bay!", null, null, null));
        topics.add(new Topic("honor", "He says:  The Paladins who  strive for Honor  are oft seen in  Trinsic, north  of the Cape of  Heroes!", null, null, null));
        topics.add(new Topic("spirit", "He says:  In Skara Brae  the Spiritual  path is taught.  Find it on an  isle near  Spiritwood!", null, null, null));
        topics.add(new Topic("humil", "He says:  Humility is the  foundation of  Virtue!  The  ruins of proud  Magincia are a  testimony unto  the Virtue of  Humility! \n\nFind the Ruins  of Magincia far  off the shores  of Britannia,  on a small isle  in the vast  Ocean!", null, null, null));
        topics.add(new Topic("pride", "He says:  Of the eight  combinations of  Truth, Love and  Courage, that  which contains  neither Truth,  Love nor Courage  is Pride.\n\nPride being not  a Virtue must be  shunned in favor  of Humility, the  Virtue which is  the antithesis  of Pride!", null, null, null));
        topics.add(new Topic("avatar", "Lord British  says:  To be an Avatar  is to be the  embodiment of  the Eight  Virtues.\n\nIt is to live a  life constantly  and forever in  the Quest to  better thyself  and the world in  which we live. ", null, null, null));
        topics.add(new Topic("quest", "Lord British  says:  The Quest of  the Avatar is  to know and  become the  embodiment of  the Eight  Virtues of  Goodness! \n\n It is known that  all who take on  this Quest must  prove themselves  by conquering  the Abyss and  Viewing the  Codex of  Ultimate Wisdom! ", null, null, null));
        topics.add(new Topic("brit", "He says:  Even though the  Great Evil Lords  have been routed  evil yet remains  in Britannia.  If but one soul  could complete  the Quest of the  Avatar, our  people would  have a new hope,  a new goal for  life.\n\nThere would be a  shining example  that there is  more to life  than the endless  struggle for  possessions  and gold!", null, null, null));
        topics.add(new Topic("ankh", "He says:  The Ankh is the  symbol of one  who strives for  Virtue.  Keep it  with thee at all  times for by  this mark thou  shalt be known!  ", null, null, null));
        topics.add(new Topic("monda", "He says:  Mondain is dead!", null, null, null));
        topics.add(new Topic("minax", "He says:  Minax is dead! ", null, null, null));
        topics.add(new Topic("exodus", "He says:  Exodus is dead! ", null, null, null));
        topics.add(new Topic("virtue", "He says:  The Eight  Virtues of the  Avatar are:  Honesty,  Compassion,  Valor,  Justice,  Sacrifice,  Honor,  Spirituality,  and Humility!", null, null, null));

        topics.add(new Topic("abyss", "He says:\nThe Great Stygian Abyss is the darkest pocket of evil "
                + "remaining in Britannia!\nIt is said that in the deepest recesses of "
                + "the Abyss is the Chamber of the Codex!\nIt is also said that only one "
                + "of highest Virtue may enter this Chamber, one such as an Avatar!!!", null, null, null));

        topics.add(new Topic("heal", "He says: I am\nwell, thank ye.", "\n\nArt thou well?", "He says: that is good.", "He says: let me heal thy wounds!", true));

    }

    public Topic matchTopic(String query) {

        if (query.toLowerCase().contains("help")) {
            String resp = lordBritishGetHelp();
            return new Topic("help", resp, null, null, null);
        }

        for (Topic t : topics) {
            if (query.toLowerCase().contains(t.getQuery().toLowerCase())) {
                return t;
            }
        }

        return null;
    }

    public String intro() {

        Party party = GameScreen.context.getParty();
        SaveGame sg = GameScreen.context.getParty().getSaveGame();

        StringBuffer sb = new StringBuffer();
        if (sg.lbintro == 1) {
            if (sg.members == 1) {
                sb.append("Lord British says:  Welcome " + party.getMember(0).getPlayer().name + "!\n\n");
            } else if (sg.members == 2) {
                sb.append("Lord British says:  Welcome " + party.getMember(0).getPlayer().name + "\nand thee also " + party.getMember(1).getPlayer().name + "!\n\n");
            } else {
                sb.append("Lord British says:  Welcome " + party.getMember(0).getPlayer().name + "\nand thy worthy\nAdventurers!\n\n");
            }
        } else {
            sb.append("Lord British rises and says: At long last!\n\n" + party.getMember(0).getPlayer().name
                    + " thou hast come!\n\nWe have waited such a long, long time..."
                    + "\n\nLord British sits and says: A new age is upon Britannia. The great evil Lords are gone but our people lack direction and purpose in their lives...\n"
                    + "A champion of virtue is called for. Thou may be this champion, but only time shall tell.  I will aid thee any way that I can!\n"
                    + "How may I help thee?");

            sg.lbintro = 1;
        }

        return sb.toString();
    }

    private String lordBritishGetHelp() {

        SaveGame sg = GameScreen.context.getParty().getSaveGame();

        boolean fullAvatar = true;
        boolean partialAvatar = false;

        for (Virtue v : Virtue.values()) {
            fullAvatar &= (sg.karma[v.ordinal()] == 0);
            partialAvatar |= (sg.karma[v.ordinal()] == 0);
        }

        String text;

        if (sg.moves <= 1000) {
            text = "To survive in this hostile land thou must first know thyself! Seek ye to master thy weapons and thy magical ability!\n"
                    + "\nTake great care in these thy first travels in Britannia.\n"
                    + "\nUntil thou dost well know thyself, travel not far from the safety of the townes!\n";
        } else if (sg.members == 1) {
            text = "Travel not the open lands alone. There are many worthy people in the diverse townes whom it would be wise to ask to Join thee!\n"
                    + "\nBuild thy party unto eight travellers, for only a true leader can win the Quest!\n";
        } else if (sg.runes == 0) {
            text = "Learn ye the paths of virtue. Seek to gain entry unto the eight shrines!\n"
                    + "\nFind ye the Runes, needed for entry into each shrine, and learn each chant or \"Mantra\" used to focus thy meditations.\n"
                    + "\nWithin the Shrines thou shalt learn of the deeds which show thy inner virtue or vice!\n"
                    + "\nChoose thy path wisely for all thy deeds of good and evil are remembered and can return to hinder thee!\n";
        } else if (!partialAvatar) {
            text = "Visit the Seer Hawkwind often and use his wisdom to help thee prove thy virtue.\n"
                    + "\nWhen thou art ready, Hawkwind will advise thee to seek the Elevation unto partial Avatarhood in a virtue.\n"
                    + "\nSeek ye to become a partial Avatar in all eight virtues, for only then shalt thou be ready to seek the codex!\n";
        } else if (sg.stones == 0) {
            text = "Go ye now into the depths of the dungeons. Therein recover the 8 colored stones from the altar pedestals in the halls of the dungeons.\n"
                    + "\nFind the uses of these stones for they can help thee in the Abyss!\n";
        } else if (!fullAvatar) {
            text = "Thou art doing very well indeed on the path to Avatarhood! Strive ye to achieve the Elevation in all eight virtues!\n";
        } else if ((sg.items & Item.BELL.ordinal()) == 0 || (sg.items & Item.BOOK.ordinal()) == 0 || (sg.items & Item.CANDLE.ordinal()) == 0) {
            text = "Find ye the Bell, Book and Candle!  With these three things, one may enter the Great Stygian Abyss!\n";
        } else if ((sg.items & Item.KEY_C.ordinal()) == 0 || (sg.items & Item.KEY_L.ordinal()) == 0 || (sg.items & Item.KEY_T.ordinal()) == 0) {
            text = "Before thou dost enter the Abyss thou shalt need the Key of Three Parts, and the Word of Passage.\n"
                    + "\nThen might thou enter the Chamber of the Codex of Ultimate Wisdom!\n";
        } else {
            text = "Thou dost now seem ready to make the final journey into the dark Abyss! Go only with a party of eight!\n"
                    + "\nGood Luck, and may the powers of good watch over thee on this thy most perilous endeavor!\n"
                    + "\nThe hearts and souls of all Britannia go with thee now. Take care, my friend.\n";
        }

        return "He says: " + text;
    }

}
