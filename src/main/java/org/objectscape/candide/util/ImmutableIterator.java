package org.objectscape.candide.util;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Created with IntelliJ IDEA.
 * User: Oliver
 * Date: 07.06.13
 * Time: 08:48
 * To change this template use File | Settings | File Templates.
 */
public class ImmutableIterator<E> {

    private final Iterator<E> iterator;

    private ImmutableIterator() {
        this.iterator = null;
    }

    public ImmutableIterator(Iterator<E> iterator) {
        this.iterator = iterator;
    }

    boolean hasNext() {
        return iterator.hasNext();
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration
     * @throws java.util.NoSuchElementException if the iteration has no more elements
     */
    E next() {
        return iterator.next();
    }

    /**
     * Performs the given action, in the order elements occur when iterating,
     * until all remaining elements have been processed or the action throws
     * an {@code Exception}.  Exceptions occurring as a result of performing
     * this action are relayed to the caller.
     *
     * <p>The default implementation should be overridden by implementations if
     * they can provide a more performant implementation than an iterator-based
     * one, or to include this method within their synchronization protocol.
     *
     * @implSpec
     * <p>The default implementation behaves as if:
     * <pre>
     *     while (hasNext())
     *         action.accept(next());
     * </pre>
     *
     * <p>This implementation does not have knowledge of the synchronization
     * protocol used by the implementing Collection.  It is the caller's
     * responsibility to ensure that usage follows the correct synchronization
     * protocol for the implementing {@code Iterator}.
     *
     * @param action The action to be performed for each element
     * @throws NullPointerException if the specified action is null
     * @since 1.8
     */
    void forEach(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        while (hasNext())
            action.accept(next());
    }
}
