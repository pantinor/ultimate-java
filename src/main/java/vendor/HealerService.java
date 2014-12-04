package vendor;

import objects.Party;
import objects.Party.PartyMember;
import objects.SaveGame;
import ultima.Constants.KarmaAction;

public class HealerService extends BaseVendor {
	
	
	public HealerService(Vendor vendor, Party party) {
		super(vendor, party);
	}
	
	@Override
	public void init() {
		welcomeMessage = "Welcome unto %s\n\n%s says: Peace and Joy be with you friend.";
		state = ConvState.WAIT_NEED_HELP;
	}

	@Override
	public boolean nextDialog() {
				
		switch(state) {
		case WAIT_NEED_HELP:
			displayToScreen(String.format(welcomeMessage, vendor.getName(), vendor.getOwner()));
			displayToScreen("\nAre you in need of help?");
			break;
		case WAIT_GIVE_BLOOD:
			displayToScreen("Art thou willing to give 100 pts of thy blood to aid others?");
			break;
		case GIVE_BLOOD:
			party.getMember(0).getPlayer().hp = party.getMember(0).getPlayer().hp - 100;
			party.adjustKarma(KarmaAction.DONATED_BLOOD);
			displayToScreen("Thou art a great help. We are in dire need!");
			break;
		case WAIT_WHICH_SERVICE:
			String list = "We can perform:\n\n";
			for (Item i : vendor.getInventoryItems()) {
				list += i.getChoice().toUpperCase() + " - " + i.getName()+" for " + i.getPrice() + "gp.\n";
			}
            list += "\nYour Need?";
			displayToScreen(list);
			break;
		case WAIT_WHO_SERVICE:
			displayToScreen("Who is in need?");
			break;
		case WAIT_PERFORM_SERVICE:
			displayToScreen(String.format("%s will cost thee %s gp.",currentSelectedItem.getDescription(), currentSelectedItem.getPrice()));
			displayToScreen("Wilt thou pay?");
			break;
			
		case PERFORM_SERVICE:
			if (currentCount  <= 0 || currentCount > party.getSaveGame().members) currentCount = 1;
			PartyMember pm = party.getMember(currentCount-1);
			if (pm == null) break;
			if (pm.heal(currentSelectedItem.getHealType())) {
				party.getSaveGame().gold = party.getSaveGame().gold - currentSelectedItem.getPrice();
				//Sounds.play(Sound.MOONGATE);
				displayToScreen("There you are. Please be more careful next time.");
			} else {
				displayToScreen(SaveGame.pc(pm.getPlayer().name) + ", thou art not in need of such service.");
			}
			
			displayToScreen("Anything else?");
			state = ConvState.ANYTHING_ELSE;

			break;
			
		case FAREWELL:
			displayToScreen(String.format("%s says: May thy life be guarded by the powers of good.", vendor.getOwner()));
			return false;
		default:
			displayToScreen("I cannot help thee with that.");
			state = ConvState.WAIT_NEED_HELP;
			break;
		
		}
		
		return true;
		
	}
	
	@Override
	public void setResponse(String input) {
				
		if (state == ConvState.WAIT_NEED_HELP || state == ConvState.ANYTHING_ELSE) {
			if (input.startsWith("y")) {
				state = ConvState.WAIT_WHICH_SERVICE;
			} else {
				if (this.party.getMember(0).getPlayer().hp >= 400) {
					state = ConvState.WAIT_GIVE_BLOOD;
				} else {
					state = ConvState.WAIT_NEED_HELP;
				}
			}
			
		} else if (state == ConvState.WAIT_WHICH_SERVICE) {
			Item item = vendor.getItem(input);
			if (item != null && checkCanPay(item)) {
				currentSelectedItem = item;
				state = ConvState.WAIT_WHO_SERVICE;
			} else {
				state = ConvState.WAIT_NEED_HELP;
			}
			
		} else if (state == ConvState.WAIT_WHO_SERVICE) {
			int count = 1;
			try {
				count = Integer.parseInt(input);
			} catch (Exception e) {
				count = 1;
			}
			currentCount = count;
			state = ConvState.WAIT_PERFORM_SERVICE;
			
		} else if (state == ConvState.WAIT_PERFORM_SERVICE) {
			if (input.startsWith("y")) {
				state = ConvState.PERFORM_SERVICE;
			} else {
				state = ConvState.WAIT_NEED_HELP;
			}
			
		} else if (state == ConvState.PERFORM_SERVICE) {
			state = ConvState.WAIT_NEED_HELP;

			
		} else if (state == ConvState.WAIT_GIVE_BLOOD) {
			if (input.startsWith("y")) {
				state = ConvState.GIVE_BLOOD;
			} else {
				party.adjustKarma(KarmaAction.DIDNT_DONATE_BLOOD);
				state = ConvState.WAIT_NEED_HELP;
			}
			
		} else if (state == ConvState.GIVE_BLOOD) {
			if (input.startsWith("y")) {
				state = ConvState.WAIT_NEED_HELP;
			} else {
				state = ConvState.FAREWELL;
			}
						
			
		} else if (state == ConvState.FAREWELL || input == null || input.length() < 1 || input.equalsIgnoreCase("bye")) {
			state = ConvState.FAREWELL;
		}

		
	}
	
	@Override
	public boolean checkCanPay(Item item) {
		if (item == null) {
			displayToScreen("I cannot help thee with that.");
			return false;
		}				
				
		boolean ret = false;

		if (item.getPrice() > party.getSaveGame().gold) {
			displayToScreen("I see by thy purse that thou hast not enough gold. I cannot aid thee.");
		} else {
			ret = true;
		}
		
		return ret;
	}
	



}
