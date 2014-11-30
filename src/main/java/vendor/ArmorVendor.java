package vendor;

import objects.Party;

public class ArmorVendor extends BaseVendor {
	
	String welcomeMessage = "Welcome to %s\n%s says:\nWelcome friend!\nArt thou here to Buy or Sell?";
	
	public ArmorVendor(Vendor vendor, Party party) {
		super(vendor, party);
		state = ConvState.ASK_BUY_SELL;
	}
	


	@Override
	public boolean nextDialog() {
		int haveCount = 0;

		switch(state) {
		case ASK_BUY_SELL:
			displayToScreen(String.format(welcomeMessage, vendor.getName(), vendor.getOwner()));
			break;
		case WAIT_BUY_INPUT:
			String list = "Very Good!\nWe Have:\n";
			for (Item i : vendor.getInventoryItems()) {
				list += i.getChoice().toUpperCase() + " - " + i.getName()+" for " + i.getPrice() + "gp.\n";
			}
            list += "Your Interest?";
			displayToScreen(list);
			break;
		case WAIT_BUY_HOW_MANY:
			displayToScreen(String.format(currentSelectedItem.getDescription(), currentSelectedItem.getPrice()));
			displayToScreen("How many would you like?");
			break;
		case WAIT_BUY_ONE:
			displayToScreen("Take it?");
			break;

		case BUY_ITEM:
			haveCount = party.getSaveGame().armor[currentSelectedItem.getArmorType().ordinal()];
			party.getSaveGame().armor[currentSelectedItem.getArmorType().ordinal()] = haveCount + currentCount;
			party.getSaveGame().gold = party.getSaveGame().gold - currentSelectedItem.getPrice()*currentCount;
			displayToScreen(String.format("%s says: A fine choice! anything else?", vendor.getOwner()));
			break;
			
			
		case WAIT_SELL_INPUT:
			String sellList = "Excellent!\n";
			for (Item i : vendor.getVendorClass().getItemCatalog()) {
				int c = party.getSaveGame().armor[i.getArmorType().ordinal()];
				if (c > 0) {
					sellList += i.getChoice().toUpperCase() + " - " + i.getName()+" for " + i.getPrice()/2 + "gp. ("+c+")\n";
				} else {
					continue;
				}
			}
			sellList += "Which wouldst you sell?";
			displayToScreen(sellList);
			break;
			
		case WAIT_SELL_HOW_MANY:
			displayToScreen(String.format("I will give you %dgp each.",currentSelectedItem.getPrice()/2));
			displayToScreen("How many would thee like to sell?");
			break;
		case WAIT_SELL_ONE:
			displayToScreen(String.format("I will give you %dgp for that %s. Deal?",currentSelectedItem.getPrice()/2,currentSelectedItem.getName()));
			break;
		case WAIT_SELL_DONT_HAVE_THAT_MANY:
			displayToScreen("You don't have that many swine! Anything else?");
			state = ConvState.ASK_BUY_SELL;
			break;
			
		case SELL_ITEM:
			haveCount = party.getSaveGame().armor[currentSelectedItem.getArmorType().ordinal()];
			party.getSaveGame().armor[currentSelectedItem.getArmorType().ordinal()] = haveCount - currentCount;
			party.getSaveGame().gold = party.getSaveGame().gold + (currentSelectedItem.getPrice()/2) * currentCount;
			displayToScreen("Fine! Anything else?");
			break;
			
		case FAREWELL:
			displayToScreen(String.format("%s says: Fare thee well!", vendor.getOwner()));
			return false;
		default:
			displayToScreen("I cannot help thee with that.");
			state = ConvState.ASK_BUY_SELL;
			break;
		
		}
		
		return true;
		
	}
	
	@Override
	public void setResponse(String input) {
		

		
		if (state == ConvState.ASK_BUY_SELL) {
			if (input.startsWith("b")) {
				state = ConvState.WAIT_BUY_INPUT;
			} else if (input.startsWith("s")) {
				state = ConvState.WAIT_SELL_INPUT;
			} else {
				state = ConvState.ASK_BUY_SELL;
			}
			
			
		} else if (state == ConvState.WAIT_BUY_INPUT) {
			Item item = vendor.getItem(input);
			if (item != null && checkCanPay(item)) {
				currentSelectedItem = item;
			}
		} else if (state == ConvState.WAIT_BUY_HOW_MANY) {
			int count = 1;
			try {
				count = Integer.parseInt(input);
			} catch (Exception e) {
				count = 1;
			}
			if (checkCanPay(currentSelectedItem, count)) {
				currentCount = count;
			} else {
				currentCount = 0;
			}
		} else if (state == ConvState.WAIT_BUY_ONE) {
			if (input.startsWith("y")) {
				currentCount = 1;
				state = ConvState.BUY_ITEM;
			} else {
				currentCount = 0;
				state = ConvState.ASK_BUY_SELL;
			}
		} else if (state == ConvState.BUY_ITEM) {
			if (input.startsWith("y")) {
				state = ConvState.ASK_BUY_SELL;
			} else {
				state = ConvState.FAREWELL;
			}
			
			
			
		} else if (state == ConvState.WAIT_SELL_INPUT) {
			Item item = vendor.getVendorClass().getItemForChoice(input);
			currentSelectedItem = item;
			if (item == null) {
				state = ConvState.WAIT_SELL_DONT_HAVE_THAT_MANY;
			} else {
				int count = party.getSaveGame().armor[item.getArmorType().ordinal()];
				if (count > 1) {
					state = ConvState.WAIT_SELL_HOW_MANY;
				} else {
					state = ConvState.WAIT_SELL_ONE;
				}
			}

		} else if (state == ConvState.WAIT_SELL_HOW_MANY) {
			int count = 1;
			try {
				count = Integer.parseInt(input);
			} catch (Exception e) {
				count = 1;
			}
			int haveCount = party.getSaveGame().armor[currentSelectedItem.getArmorType().ordinal()];
			if (count <= haveCount) {
				currentCount = count;
				state = ConvState.SELL_ITEM;
			} else {
				state = ConvState.WAIT_SELL_DONT_HAVE_THAT_MANY;
			}
		} else if (state == ConvState.WAIT_SELL_ONE) {
			if (input.startsWith("y")) {
				currentCount = 1;
				state = ConvState.SELL_ITEM;
			} else {
				currentCount = 0;
				state = ConvState.ASK_BUY_SELL;
			}
		} else if (state == ConvState.SELL_ITEM) {
			if (input.startsWith("y")) {
				state = ConvState.WAIT_SELL_INPUT;
			} else {
				state = ConvState.FAREWELL;
			}
			
			
			
			
		} else if (state == ConvState.FAREWELL || input == null || input.length() < 1 || input.equalsIgnoreCase("bye")) {
			state = ConvState.FAREWELL;
		}

		
	}
	

	@Override
	public void init() {
		state = ConvState.ASK_BUY_SELL;
	}

}
