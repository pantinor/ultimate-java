package vendor;

import objects.Party;
import ultima.Constants.InventoryType;
import ultima.LogScrollPane;

public abstract class BaseVendor {
	
	public enum ConvState {
		ASK_BUY_SELL,
		
		WAIT_BUY_INPUT,
		WAIT_BUY_ONE,
		WAIT_BUY_HOW_MANY,
		BUY_ITEM,
		
		WAIT_SELL_INPUT,
		WAIT_SELL_ONE,
		WAIT_SELL_HOW_MANY,
		WAIT_SELL_DONT_HAVE_THAT_MANY,
		SELL_ITEM,

		FAREWELL;
	}
	
	ConvState state;
	Vendor vendor;
	Party party;
	InventoryType type;
	String welcomeMessage;
	
	Item currentSelectedItem;
	int currentCount;
	
	private LogScrollPane scrollPane;
	
	public BaseVendor(Vendor vendor, Party party) {
		this.vendor = vendor;
		this.party = party;
		this.type = vendor.getVendorType();
		init();
	}
	
	public abstract void init() ;
	
	public abstract boolean nextDialog() ;
	
	public abstract void setResponse(String input) ;
	
	public boolean checkCanPay(Item item) {
		
		if (item == null) {
			displayToScreen("I cannot help thee with that.");
			state = ConvState.ASK_BUY_SELL;
			return false;
		}
		
		if (item.getPrice() > party.getSaveGame().gold) {
			displayToScreen(String.format(item.getDescription(), item.getPrice()));
			displayToScreen("You have not the funds for even one!");
			state = ConvState.ASK_BUY_SELL;
			return false;
		}
		
		boolean ret = false;

		if (party.getSaveGame().gold > item.getPrice()*2) {
			state = ConvState.WAIT_BUY_HOW_MANY;
			ret = true;
		} else {
			state = ConvState.WAIT_BUY_ONE;
			ret = true;
		}
		
		return ret;
	}
	
	public boolean checkCanPay(Item item, int count) {
		
		if (item == null) {
			displayToScreen("I cannot help thee with that.");
			state = ConvState.ASK_BUY_SELL;
			return false;
		}				
				
		boolean ret = false;

		if (item.getPrice()*count > party.getSaveGame().gold) {
			displayToScreen("I fear you have not the funds, perhaps something else.");
			state = ConvState.ASK_BUY_SELL;
		} else {
			state = ConvState.BUY_ITEM;
			ret = true;
		}
		
		return ret;
	}
	
		
	public void displayToScreen(String s) {
		if (scrollPane != null) {
			scrollPane.add(s);
		} else {
			System.out.println(s);
		}
	}

	public LogScrollPane getScrollPane() {
		return scrollPane;
	}

	public void setScrollPane(LogScrollPane scrollPane) {
		this.scrollPane = scrollPane;
	}

}
