package vendor;

import objects.Party;
import ultima.Constants.GuildItemType;

public class GuildVendor extends BaseVendor {

    public GuildVendor(Vendor vendor, Party party) {
        super(vendor, party);
    }

    @Override
    public void init() {
        welcomeMessage = "Avast ye mate!";
        state = ConvState.ASK_BUY;
    }

    @Override
    public boolean nextDialog() {

        switch (state) {
            case ASK_BUY:
                displayToScreen(String.format(welcomeMessage, vendor.getName()));
                displayToScreen(String.format("Shure ye wishes to buy from ol' %s?", vendor.getOwner()));
                displayToScreen(String.format("Welcome to %s.\nLike to see my goods?", vendor.getName()));
                break;

            case WAIT_BUY_INPUT:
                displayToScreen(String.format("%s says: Good Mate! Ya see I gots:\n", vendor.getOwner()));

                String list = "";
                for (Item i : vendor.getInventoryItems()) {
                    if (i.getGuildItemType() != GuildItemType.sextant) //sextant is hidden
                    {
                        list += String.format("%s - %s (%s) for %sgp\n", i.getChoice().toUpperCase(), i.getName(), i.getQuantity(), i.getPrice());
                    }
                }
                list += "Wat'l it be?";
                displayToScreen(list);
                break;
            case WAIT_BUY_ONE:
                displayToScreen(String.format(currentSelectedItem.getDescription(), currentSelectedItem.getPrice()));
                displayToScreen("Will ya buy?");
                break;

            case BUY_ITEM:

                switch (currentSelectedItem.getGuildItemType()) {
                    case gem:
                        party.getSaveGame().gems += currentSelectedItem.getQuantity();
                        break;
                    case key:
                        party.getSaveGame().keys += currentSelectedItem.getQuantity();
                        break;
                    case sextant:
                        party.getSaveGame().sextants += currentSelectedItem.getQuantity();
                        break;
                    case torch:
                        party.getSaveGame().torches += currentSelectedItem.getQuantity();
                        break;
                }

                party.adjustGold(-currentSelectedItem.getPrice());
                displayToScreen(String.format("%s says: See more?", vendor.getOwner()));
                state = ConvState.ANYTHING_ELSE;
                break;

            case ANYTHING_ELSE:
                displayToScreen(String.format("%s says: See more?", vendor.getOwner()));
                break;
            case DECLINE_BUY:
                displayToScreen("Hmmm...Grmbl...");
                state = ConvState.FAREWELL;
                break;

            case FAREWELL:
                displayToScreen("See ya matie!");
                return false;
            default:
                displayToScreen("I cannot help ya with that.");
                state = ConvState.ANYTHING_ELSE;
                break;

        }
        return true;
    }

    @Override
    public void setResponse(String input) {
        if (state == ConvState.ASK_BUY || state == ConvState.ANYTHING_ELSE) {
            if (input.startsWith("y")) {
                state = ConvState.WAIT_BUY_INPUT;
            } else {
                state = ConvState.FAREWELL;
            }

        } else if (state == ConvState.WAIT_BUY_INPUT) {
            Item item = vendor.getItem(input);
            if (item != null && checkCanPay(item)) {
                currentSelectedItem = item;
                state = ConvState.WAIT_BUY_ONE;
            } else {
                state = ConvState.FAREWELL;
            }
        } else if (state == ConvState.WAIT_BUY_ONE) {
            if (input.startsWith("y")) {
                currentCount = 1;
                state = ConvState.BUY_ITEM;
            } else {
                currentCount = 0;
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
            displayToScreen("What? Can't pay! Buzz off swine!");
        } else {
            ret = true;
        }

        return ret;
    }

}
