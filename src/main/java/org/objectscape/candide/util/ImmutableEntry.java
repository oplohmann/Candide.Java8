package org.objectscape.candide.util;

/**
 * Created with IntelliJ IDEA.
 * User: Oliver
 * Date: 07.06.13
 * Time: 21:48
 * To change this template use File | Settings | File Templates.
 */
public class ImmutableEntry<K, V> {

    private final K key;
    private final V value;

    private ImmutableEntry() {
        key = null;
        value = null;
    }

    public ImmutableEntry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public V getValue() {
        return value;
    }

    public K getKey() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ImmutableEntry that = (ImmutableEntry) o;

        if(key == null && that.key != null)
            return false;
        if (!key.equals(that.key))
            return false;

        if(value == null && that.value != null)
            return false;

        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }
}
