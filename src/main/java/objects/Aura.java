package objects;

import ultima.Constants.AuraType;

public class Aura {

    private AuraType type = AuraType.NONE;
    private int duration;

    public void set(AuraType t, int d) {
        this.type = t;
        this.duration = d;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int d) {
        this.duration = d;
    }

    public AuraType getType() {
        return type;
    }

    public void setType(AuraType t) {
        this.type = t;
    }

    public boolean isActive() {
        return duration > 0;
    }

    public void passTurn() {
        if (duration > 0) {
            duration--;

            if (duration == 0) {
                type = AuraType.NONE;
            }
        }
    }

}
