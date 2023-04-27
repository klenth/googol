package klenth.util;

import java.util.Iterator;

public final class Zipper {

    private Zipper() {
        throw new IllegalStateException();
    }

    public static <S, T> Iterable<Pair<S, T>> zip(Iterable<? extends S> firstIterable, Iterable<? extends T> secondIterable) {
        var firstIt = firstIterable.iterator();
        var secondIt = secondIterable.iterator();

        return () -> new Iterator<>() {
            @Override
            public boolean hasNext() {
                return firstIt.hasNext() && secondIt.hasNext();
            }

            @Override
            public Pair<S, T> next() {
                return new Pair<>(firstIt.next(), secondIt.next());
            }
        };
    }

    public static <T> Iterable<Pair<Integer, T>> withIndices(Iterable<? extends T> iterable) {
        var it = iterable.iterator();

        return () -> new Iterator<>() {
            int index = 0;

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Pair<Integer, T> next() {
                return new Pair<>(index++, it.next());
            }
        };
    }
}
