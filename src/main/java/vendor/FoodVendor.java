package vendor;

import objects.Party;
import ultima.Constants.InventoryType;
import ultima.GameScreen;

public class FoodVendor extends BaseVendor {
	Item food;

	public FoodVendor(Vendor vendor, Party party) {
		super(vendor, party);
		for (Item i : vendor.getInventoryItems()) {
			if (i.getType() == InventoryType.FOOD) {
				food = i;
			}
		}
	}

	@Override
	public void init() {
        welcomeMessage = "Welcome to %s";
        state = ConvState.ASK_BUY;
	}

	@Override
	public boolean nextDialog() {
		int haveCount = 0;

		switch(state) {
		case ASK_BUY:
			displayToScreen(String.format(welcomeMessage, vendor.getName()));
			displayToScreen(String.format("%s says: Good day, and Welcome friend.", vendor.getOwner()));
			displayToScreen("May I interest you in some rations?");
			break;
			
		case WAIT_BUY_HOW_MANY:
			displayToScreen("We have the best adventure rations, "+food.getQuantity()+" for only "+food.getPrice()+"gp.");
			displayToScreen("How many packs of "+food.getQuantity()+" would you like?");
			break;

		case BUY_FOOD:
			haveCount = party.getSaveGame().food;
			party.getSaveGame().food = haveCount + currentCount * food.getQuantity() * 100;
			party.adjustGold(-food.getPrice()*currentCount);
			displayToScreen("Thank you.");
			displayToScreen("Anything else?");
			state = ConvState.ANYTHING_ELSE;
			break;

		case ANYTHING_ELSE:
			displayToScreen("Anything else?");
			break;
		case DECLINE_BUY:
			displayToScreen("Too bad. Maybe next time.");
			state = ConvState.FAREWELL;
			break;
			
		case FAREWELL:
			displayToScreen("Goodbye. Come again!");
			return false;
		default:
			displayToScreen("I cannot help you with that.");
			state = ConvState.ANYTHING_ELSE;
			break;
			
		}
		return true;
	}

	@Override
	public void setResponse(String input) {
		if (state == ConvState.ASK_BUY || state == ConvState.ANYTHING_ELSE) {
			if (input.startsWith("y")) {
				state = ConvState.WAIT_BUY_HOW_MANY;
			} else {
				state = ConvState.FAREWELL;
			}
			
		} else if (state == ConvState.WAIT_BUY_HOW_MANY) {
			
			int count = 1;
			try {
				count = Integer.parseInt(input);
			} catch (Exception e) {
				count = 1;
			}
			currentCount = count;
			if (currentCount == 0 || !checkCanPayFood()) {
				state = ConvState.FAREWELL;
			} else {
				state = ConvState.BUY_FOOD;
			}
			
		} else if (state == ConvState.FAREWELL || input == null || input.length() < 1 || input.equalsIgnoreCase("bye")) {
			state = ConvState.FAREWELL;
		}
		
	}
	
	public boolean checkCanPayFood() {
		
		boolean ret = false;

		if (food.getPrice()*currentCount > party.getSaveGame().gold) {
			displayToScreen("You can only afford "+Math.round(GameScreen.context.getParty().getSaveGame().gold / food.getPrice())+" packs.");
		} else {
			ret = true;
		}
		
		return ret;
	}
	
	

}
