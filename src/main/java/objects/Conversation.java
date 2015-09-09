package objects;

import java.util.ArrayList;
import java.util.List;
import ultima.Constants.Maps;

public class Conversation {

    protected Maps map;
    protected int index;
    protected String name;
    protected String pronoun;
    protected int turnAwayProb;
    protected String description;
    protected int respAffectsHumility;
    protected List<Topic> topics = new ArrayList<>();
    
    public static final String[] standardQuery = {"job", "health", "look", "name","give", "join"};

    public Conversation() {

    }

    public Conversation(int index, int turnAwayProb, int questionFlag, int respAffectsHumility, String[] strings) {
        this.index = index;
        this.turnAwayProb = turnAwayProb;
        this.respAffectsHumility = respAffectsHumility;

        this.name = strings[0];
        this.pronoun = strings[1];
        this.description = strings[2];

        topics.add(new Topic(standardQuery[0], strings[3], null, null, null));
        topics.add(new Topic(standardQuery[1], strings[4], null, null, null));
        topics.add(new Topic(standardQuery[2], description, null, null, null));
        topics.add(new Topic(standardQuery[3], pronoun + " says: I am " + name, null, null, null));
        topics.add(new Topic(standardQuery[4], pronoun + " says: I do not need thy gold.  Keep it!", null, null, null));
        topics.add(new Topic(standardQuery[5], pronoun + " says: I cannot join thee.", null, null, null));

        String query1 = strings[10];
        String query2 = strings[11];

        if (questionFlag == 5) {
            topics.add(new Topic(query1, strings[5], strings[7], strings[8], strings[9]));
            topics.add(new Topic(query2, strings[6], null, null, null));
        }
        if (questionFlag == 6) {
            topics.add(new Topic(query1, strings[5], null, null, null));
            topics.add(new Topic(query2, strings[6], strings[7], strings[8], strings[9]));
        }
        if (questionFlag == 0) {
            topics.add(new Topic(query1, strings[5], null, null, null));
            topics.add(new Topic(query2, strings[6], null, null, null));
        }

    }
    
    public boolean isStandardQuery(String query) {
        for (String st : standardQuery) {
            if (query.toLowerCase().contains(st)) {
                return true;
            }
        }
        return false;
    }

    public Topic matchTopic(String query) {
        for (Topic t : topics) {
            if (query.toLowerCase().contains(t.getQuery().toLowerCase())) {
                return t;
            }
        }
        return null;
    }
    
    public void setMap(Maps m) {
        this.map = m;
    }
    
    public Maps getMap() {
        return map;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public String getPronoun() {
        return pronoun;
    }

    public int getTurnAwayProb() {
        return turnAwayProb;
    }

    public String getDescription() {
        return description;
    }

    public int getRespAffectsHumility() {
        return respAffectsHumility;
    }

    public List<Topic> getTopics() {
        return topics;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPronoun(String pronoun) {
        this.pronoun = pronoun;
    }

    public void setTurnAwayProb(int turnAwayProb) {
        this.turnAwayProb = turnAwayProb;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setRespAffectsHumility(int respAffectsHumility) {
        this.respAffectsHumility = respAffectsHumility;
    }

    public void setTopics(List<Topic> topics) {
        this.topics = topics;
    }

    @Override
    public String toString() {
        return String.format("\n\tConversation [index=%s, name=%s, pronoun=%s, turnAwayProb=%s, description=%s, respAffectsHumility=%s, topics=%s]", 
                index, name, pronoun, turnAwayProb, description, respAffectsHumility, topics);
    }

    public class Topic {

        private String query;
        private String phrase;
        private String question;
        private String yesResponse;
        private String noResponse;

        private boolean lbHeal;

        public Topic(String query, String phrase, String question, String yesResponse, String noResponse) {
            super();
            this.query = query;
            this.phrase = phrase;
            this.question = question;
            this.yesResponse = yesResponse;
            this.noResponse = noResponse;
        }

        public Topic(String query, String phrase, String question, String yesResponse, String noResponse, boolean heal) {
            super();
            this.query = query;
            this.phrase = phrase;
            this.question = question;
            this.yesResponse = yesResponse;
            this.noResponse = noResponse;
            this.lbHeal = heal;
        }

        public Topic(String phrase) {
            super();
            this.phrase = phrase;
        }

        public String getQuery() {
            return query;
        }

        public String getPhrase() {
            return phrase;
        }

        public String getQuestion() {
            return question;
        }

        public String getYesResponse() {
            return yesResponse;
        }

        public String getNoResponse() {
            return noResponse;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public void setPhrase(String phrase) {
            this.phrase = phrase;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public void setYesResponse(String yesResponse) {
            this.yesResponse = yesResponse;
        }

        public void setNoResponse(String noResponse) {
            this.noResponse = noResponse;
        }

        public boolean isLbHeal() {
            return lbHeal;
        }

        public void setLbHeal(boolean lbHeal) {
            this.lbHeal = lbHeal;
        }

        @Override
        public String toString() {
            return String.format("\n\t\tTopic %s [query=%s, phrase=%s, question=%s, yesResponse=%s, noResponse=%s]", name, query, phrase, question, yesResponse, noResponse, lbHeal);
        }

    }

}
