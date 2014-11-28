package org.objectscape.candide.util.values;

/**
 * Created by Oliver Plohmann on 27.11.2014.
 */
public class BooleanValue {

    private boolean value = false;

    public BooleanValue() {
    }

    public BooleanValue(boolean value) {
        this.value = value;
    }

    public boolean get() {
        return value;
    }

    public void set(boolean value) {
        this.value = value;
    }

}
