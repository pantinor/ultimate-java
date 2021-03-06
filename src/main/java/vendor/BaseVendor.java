package vendor;

import objects.Party;
import ultima.BaseScreen;
import ultima.Constants.InventoryType;
import ultima.Context;
import util.LogScrollPane;

public abstract class BaseVendor {

    public enum ConvState {

        ASK_BUY,
        ASK_BUY_SELL,
        WAIT_BUY_INPUT,
        WAIT_BUY_ONE,
        WAIT_BUY_HOW_MANY,
        WAIT_YOU_PAY,
        BUY_ITEM,
        ASK_LODGING,
        WAIT_RENT_ROOM,
        RENT_ROOM,
        DECLINE_LODGING,
        DECLINE_RENT,
        WAIT_SELL_INPUT,
        WAIT_SELL_ONE,
        WAIT_SELL_HOW_MANY,
        WAIT_SELL_DONT_HAVE_THAT_MANY,
        SELL_ITEM,
        WAIT_NEED_HELP,
        WAIT_GIVE_BLOOD,
        GIVE_BLOOD,
        WAIT_WHICH_SERVICE,
        WAIT_WHO_SERVICE,
        WAIT_PERFORM_SERVICE,
        PERFORM_SERVICE,
        ASK_FOOD_ALE,
        BUY_FOOD,
        BUY_ALE,
        BUY_ALE_WITH_TIP,
        FOGGY_MEMORY,
        SORRY,
        TAVERN_INFO,
        DECLINE_BUY,
        ANYTHING_ELSE,
        FAREWELL;
    }

    ConvState state;
    Vendor vendor;
    Party party;
    Context context;
    InventoryType type;
    String welcomeMessage;

    Item currentSelectedItem;
    int currentCount;

    BaseScreen screen;

    private LogScrollPane scrollPane;

    public BaseVendor(Vendor vendor, Context context) {
        this.vendor = vendor;
        this.context = context;
        this.party = context.getParty();
        this.type = vendor.getVendorType();
        init();
    }

    public void setScreen(BaseScreen screen) {
        this.screen = screen;
    }

    public abstract void init();

    public abstract boolean nextDialog();

    public abstract void setResponse(String input);

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

        if (party.getSaveGame().gold > item.getPrice() * 2) {
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

        if (item.getPrice() * count > party.getSaveGame().gold) {
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
