package objects;


import org.apache.commons.lang3.StringUtils;

import ultima.Constants.KarmaAction;

public class CustomInputConversation  extends Conversation {
	
	private String customInputQuery;
	private String customInputResponse;
	private boolean customQueryInvoked;
	private Tile tile;
	private Party party;
	
	/**
	 * 	Allows for input of a single response to a custom query, ie used for beggars on "give".
	 */
	public CustomInputConversation(Tile tile, Conversation orig) {
		super();
		this.tile = tile;
		this.index = orig.index;
		this.turnAwayProb = orig.turnAwayProb;
		this.respAffectsHumility = orig.respAffectsHumility;
		this.name = orig.name;
		this.pronoun = orig.pronoun;
		this.description = orig.description;
		this.topics = orig.topics;
	}

	public String getCustomInputQuery() {
		return customInputQuery;
	}

	public void setCustomInputQuery(String customInputQuery) {
		this.customInputQuery = customInputQuery;
	}

	public String getCustomInputResponse() {
		return customInputResponse;
	}

	public void setCustomInputResponse(String customInputResponse) {
		this.customInputResponse = customInputResponse;
	}
	
	public Party getParty() {
		return party;
	}

	public void setParty(Party party) {
		this.party = party;
	}
	
	@Override
	public Topic matchTopic(String input) {
		
		if (customQueryInvoked) {
			customInputResponse = input;
			if (tile.getIndex() == 88 || tile.getIndex() == 89) {
				if (party != null) {
					try {
						int gold = Integer.parseInt(input);
						if (gold <= 0) throw new Exception(); 
						party.adjustGold(-gold);
						party.adjustKarma(KarmaAction.GAVE_TO_BEGGAR);
					} catch (Exception e) {
						customQueryInvoked = false;
						return new Topic("What?");
					}
				}
				customQueryInvoked = false;
				return new Topic(pronoun + " says: Oh thank thee!  I shall never forget thy kindness!");
			}
			
		}
		
		if (StringUtils.equals(input.toLowerCase(), customInputQuery)) {
			customQueryInvoked = true;
		} else {
			customQueryInvoked = false;
		}
		
		for (Topic t : topics) {
			if (input.toLowerCase().contains(t.getQuery().toLowerCase())) {
				return t;
			}
		}
		return null;
	}

}
