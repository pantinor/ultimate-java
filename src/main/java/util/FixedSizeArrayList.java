package util;

import java.util.ArrayList;

public class FixedSizeArrayList<T> extends ArrayList<T> {

    private final int maxSize;

    public FixedSizeArrayList(int maxSize) {
        super();
        this.maxSize = maxSize;
    }

    public boolean add(T t) {
        if (size() >= maxSize) {
            remove(0);
        }
        return super.add(t);
    }

}
