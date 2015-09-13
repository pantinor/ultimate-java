package objects;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import objects.Conversation.Topic;
import ultima.Constants.MapType;
import ultima.Constants.Maps;
import util.Utils;

@XmlRootElement(name = "maps")
public class MapSet {

    private List<BaseMap> maps = null;

    @XmlElement(name = "map")
    public List<BaseMap> getMaps() {
        return maps;
    }

    public void setMaps(List<BaseMap> maps) {
        this.maps = maps;
    }

    public void init(TileSet ts) {
        for (BaseMap m : maps) {

            Maps map = Maps.get(m.getId());
            map.setMap(m);

            String tlkName = m.getCity() == null ? null : m.getCity().getTlk_fname();
            if (tlkName != null) {

                if (tlkName.endsWith(".tmx")) {
                    Utils.setTMXMap(m, map, tlkName, ts);
                } else {
                    List<Conversation> conv = Utils.getDialogs(tlkName);
                    m.getCity().setConversations(conv);

                    List<Person> people = Utils.getPeople(m.getFname(), map, ts);
                    m.getCity().setPeople(people);
                    for (Person p : people) {
                        if (p != null) {

                            for (Conversation c : conv) {
                                c.setMap(map);
                                if (c.getIndex() == p.getDialogId()) {
                                    p.setConversation(c);
                                }
                            }

                            if (m.getCity().getPersonRoles() != null) {
                                for (PersonRole pr : m.getCity().getPersonRoles()) {
                                    if (p.getId() == pr.getId()) {
                                        p.setRole(pr);
                                    }
                                }
                            }

                            //set beggars give conversations
                            if (p.getTile().getIndex() == 88 || p.getTile().getIndex() == 89) {
                                Topic giveTopic = p.getConversation().matchTopic("give");
                                giveTopic.setPhrase("How much?");
                                CustomInputConversation cic = new CustomInputConversation(p.getTile(), p.getConversation());
                                cic.setCustomInputQuery("give");
                                p.setConversation(cic);
                            }

                        }
                    }
                }
            }
            
            try {
                //only for the original ULT or world data files, no-op for TMX files
                Utils.setMapTiles(m, ts);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (m.getType() == MapType.world || m.getType() == MapType.city) {

                float[][] shadowMap = new float[m.getWidth()][m.getHeight()];
                for (int y = 0; y < m.getHeight(); y++) {
                    for (int x = 0; x < m.getWidth(); x++) {
                        shadowMap[x][y] = (m.getTile(x, y).isOpaque() ? 1 : 0);
                    }
                }

                m.setShadownMap(shadowMap);
            }

        }
    }

}
