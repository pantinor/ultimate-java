package vendor;

import ultima.Context;
import ultima.GameScreen;
import util.Utils;

public class InnService extends BaseVendor {

    public InnService(Vendor vendor, Context context) {
        super(vendor, context);
    }

    @Override
    public void init() {
        welcomeMessage = "The Innkeeper says: Welcome to %s\nI am %s.";
        state = ConvState.ASK_LODGING;
    }

    @Override
    public boolean nextDialog() {
        switch (state) {
            case ASK_LODGING:
                displayToScreen(String.format(welcomeMessage, vendor.getName(), vendor.getOwner()));
                displayToScreen("Are you in need of lodging?");
                break;

            case WAIT_RENT_ROOM:
                displayToScreen(vendor.getGenericField1());
                break;

            case RENT_ROOM:
                party.adjustGold(-currentSelectedItem.getPrice());
                if (screen != null) {
                    ((GameScreen) screen).newMapPixelCoords = screen.getMapPixelCoords(currentSelectedItem.getRoomX(), currentSelectedItem.getRoomY());
                }
                displayToScreen("Very good. Have a pleasant night.");
                if (Utils.rand.nextInt(4) == 0) {
                    displayToScreen("Oh, and don't mind the strange noises, it's only rats!");
                }
                return false;
            case DECLINE_LODGING:
                displayToScreen("Then you have come to the wrong place! Good day.");
                return false;
            case DECLINE_RENT:
                displayToScreen("You won't find a better deal in this towne!");
                return false;
            case FAREWELL:
                displayToScreen("Fare thee well!");
                return false;
            default:
                displayToScreen("I cannot help thee with that.");
                state = ConvState.ASK_LODGING;
                break;

        }
        return true;
    }

    @Override
    public void setResponse(String input) {
        if (state == ConvState.ASK_LODGING) {
            if (input.startsWith("y")) {
                state = ConvState.WAIT_RENT_ROOM;
            } else {
                state = ConvState.DECLINE_LODGING;
            }
        } else if (state == ConvState.WAIT_RENT_ROOM) {
            Item item = vendor.getItem(input);
            currentSelectedItem = item;
            if (item != null && checkCanPay(item)) {
                currentSelectedItem = item;
                state = ConvState.RENT_ROOM;
            } else {
                state = ConvState.DECLINE_RENT;
            }

        } else if (state == ConvState.RENT_ROOM) {
            state = ConvState.FAREWELL;

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
            displayToScreen("If you can't pay, you can't stay! Good Bye.");
        } else {
            ret = true;
        }

        return ret;
    }

}
