package org.objectscape.candide.util;

/**
 * Created by Oliver Plohmann on 27.11.2014.
 */
public class IntValue {

    private int value = 0;

    public IntValue() {
    }

    public IntValue(int value) {
        this.value = value;
    }

    public int get() {
        return value;
    }

    public Integer getObject() {
        return Integer.valueOf(value);
    }

    public void set(int value) {
        this.value = value;
    }

    public void set(Integer value) {
        this.value = value.intValue();
    }

    public void increment() {
        value++;
    }

    public void decrement() {
        value--;
    }
}
