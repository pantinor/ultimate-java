package objects;

import java.util.HashMap;
import java.util.Map;

public class Conversation {
	
	private int index;
	private String name;
	private String pronoun;
	private int turnAwayProb;
	private String description;
	private int respAffectsHumility;
	
	private Map<String, Topic> topics = new HashMap<String, Topic>();
		
	public Conversation(int index, int turnAwayProb, int questionFlag,  int respAffectsHumility, String[] strings) {
		this.index = index;
		this.turnAwayProb = turnAwayProb;
		this.respAffectsHumility = respAffectsHumility;
		
		this.name = strings[0];
		this.pronoun = strings[1];
		this.description = strings[2];
		
		topics.put("job",new Topic("job", strings[3], null, null, null));
		topics.put("health",new Topic("health", strings[4], null, null, null));

		String query1 = strings[10];
		String query2 = strings[11];
		
		if (questionFlag == 5) {
			topics.put(query1, new Topic(query1, strings[5], strings[7], strings[8], strings[9]));
			topics.put(query2, new Topic(query2, strings[6], null, null, null));
		}
		if (questionFlag == 6) {
			topics.put(query1, new Topic(query1, strings[5], null, null, null));
			topics.put(query2, new Topic(query2, strings[6], strings[7], strings[8], strings[9]));
		}
		if (questionFlag == 0) {
			topics.put(query1, new Topic(query1, strings[5], null, null, null));
			topics.put(query2, new Topic(query2, strings[6], null, null, null));
		}

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



	public Map<String, Topic> getTopics() {
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



	public void setTopics(Map<String, Topic> topics) {
		this.topics = topics;
	}



	public class Topic {
		private String query;
		private String phrase;
		private String question;
		private String yesResponse;
		private String noResponse;
		
		private Topic(String query, String phrase, String question, String yesResponse, String noResponse) {
			super();
			this.query = query;
			this.phrase = phrase;
			this.question = question;
			this.yesResponse = yesResponse;
			this.noResponse = noResponse;
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
		

	}


}
