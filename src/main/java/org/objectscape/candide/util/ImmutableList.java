package org.objectscape.candide.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Created with IntelliJ IDEA.
 * User: Oliver
 * Date: 24.05.13
 * Time: 18:20
 * To change this template use File | Settings | File Templates.
 */
public class ImmutableList<T> {

    // assuming JDK5 memory model is used, which is reasonable as
    // generics also did not exist before JDK5
    private final List<T> contents = new CopyOnWriteArrayList<>();

    public ImmutableList() {
        super();
    }

    public ImmutableList(T element) {
        super();
        contents.add(element);
    }

    public ImmutableList(List<T> list) {
        super();
        contents.addAll(list);
    }

    public T get(int index) {
        return contents.get(index);
    }

    public int indexOf(Object o) {
        return contents.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        return contents.lastIndexOf(o);
    }

    public List<T> subList(int fromIndex, int toIndex) {
        return contents.subList(fromIndex, toIndex);
    }

    public int size() {
        return contents.size();
    }

    public <T> T[] toArray(T[] a) {
        return contents.toArray(a);
    }

    public boolean containsAll(Collection<?> c) {
        return containsAll(c);
    }

    public ImmutableIterator<T> iterator() {
        return new ImmutableIterator<>(contents.iterator());
    }

    public Object[] toArray() {
        return contents.toArray();
    }

    public List<T> mutableList() {
        return new ArrayList<>(contents);
    }

    public boolean isEmpty() {
        return contents.isEmpty();
    }

    public boolean contains(Object o) {
        return contents.contains(o);
    }

    public void forEach(Consumer<? super T> action) {
        contents.forEach(action);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;

        if (!(o instanceof List))
            return false;

        return contents.equals((List) o);
    }

    @Override
    public int hashCode() {
        return contents.hashCode();
    }
}
