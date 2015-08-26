package vendor;

import objects.Party;
import ultima.Constants.KarmaAction;
import util.Utils;

public class ReagentService extends BaseVendor {

    int youPay;

    public ReagentService(Vendor vendor, Party party) {
        super(vendor, party);
    }

    @Override
    public void init() {
        welcomeMessage = "A blind woman turns to you\nand says: Welcome to %s,\nI am %s";
        state = ConvState.ASK_BUY;
    }

    @Override
    public boolean nextDialog() {
        switch (state) {
            case ASK_BUY:
                displayToScreen(String.format(welcomeMessage, vendor.getName(), vendor.getOwner()));
                displayToScreen("Are you in need of Reagents?");
                break;
            case ANYTHING_ELSE:
                break;

            case WAIT_BUY_INPUT:
                String list = "Very well.\nI Have:\n";
                for (Item i : vendor.getInventoryItems()) {
                    list += i.getChoice().toUpperCase() + " - " + i.getName() + "\n";
                }
                list += "Your Interest?";
                displayToScreen(list);
                break;

            case WAIT_BUY_HOW_MANY:
                String howmny = "Very well,\nI sell %s for %dgp.\nHow many would you like? (0-9)";
                displayToScreen(String.format(howmny, currentSelectedItem.getName(), currentSelectedItem.getPrice()));
                break;

            case WAIT_YOU_PAY:
                String willbe = "Very good, that will be %dgp.\nYou pay:";
                displayToScreen(String.format(willbe, currentSelectedItem.getPrice() * currentCount));
                break;

            case BUY_ITEM:
                int have = party.getSaveGame().reagents[currentSelectedItem.getReagentType().ordinal()];
                party.getSaveGame().reagents[currentSelectedItem.getReagentType().ordinal()] = Utils.adjustValueMax(currentCount, have, Short.MAX_VALUE);
                party.adjustGold(-youPay);
                if (youPay < currentSelectedItem.getPrice() * currentCount) {
                    party.adjustKarma(KarmaAction.CHEAT_REAGENTS);
                } else if (youPay > currentSelectedItem.getPrice() * currentCount) {
                    party.adjustKarma(KarmaAction.DIDNT_CHEAT_REAGENTS);
                }
                displayToScreen("Very good. Anything else?");
                state = ConvState.ANYTHING_ELSE;
                break;

            case DECLINE_BUY:
                displayToScreen("I see, then");
                displayToScreen("Anything else?");
                break;

            case FAREWELL:
            default:
                String bye = "%s says:\nPerhaps another time then....\nand slowly turns away.";
                displayToScreen(String.format(bye, vendor.getOwner()));
                return false;
        }

        return true;
    }

    @Override
    public void setResponse(String input) {
        if (state == ConvState.ASK_BUY || state == ConvState.DECLINE_BUY || state == ConvState.ANYTHING_ELSE) {
            if (input.startsWith("y")) {
                state = ConvState.WAIT_BUY_INPUT;
            } else {
                state = ConvState.FAREWELL;
            }

        } else if (state == ConvState.WAIT_BUY_INPUT) {
            Item item = vendor.getItem(input);
            currentSelectedItem = item;
            if (currentSelectedItem == null) {
                state = ConvState.DECLINE_BUY;
            } else {
                state = ConvState.WAIT_BUY_HOW_MANY;
            }

        } else if (state == ConvState.WAIT_BUY_HOW_MANY) {
            int count = 1;
            try {
                count = Integer.parseInt(input);
            } catch (Exception e) {
                count = 1;
            }
            currentCount = count;
            if (currentCount == 0) {
                state = ConvState.DECLINE_BUY;
            } else {
                state = ConvState.WAIT_YOU_PAY;
            }

        } else if (state == ConvState.WAIT_YOU_PAY) {
            int amnt = currentSelectedItem.getPrice() * currentCount;
            try {
                amnt = Integer.parseInt(input);
            } catch (Exception e) {
            }
            youPay = amnt;

            if (youPay == 0) {
                state = ConvState.DECLINE_BUY;
            } else {
                if (checkCanPay(currentSelectedItem)) {
                    state = ConvState.BUY_ITEM;
                } else {
                    state = ConvState.FAREWELL;
                }
            }

        } else if (state == ConvState.BUY_ITEM) {
            if (input.startsWith("y")) {
                state = ConvState.ASK_BUY;
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
            displayToScreen("It seems you have not the gold!");
        } else {
            ret = true;
        }

        return ret;
    }

}
