package klenth.googol.ast;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class AST {

    @FunctionalInterface
    public interface NodeConsumer<N extends Node, Ex extends Exception> {
        public void accept(N node) throws Ex;
    }

    public static <Ex extends Exception> void preOrder(Node node, NodeConsumer<Node, Ex> consumer) throws Exception {
        consumer.accept(node);

        for (var child : node.children())
            consumer.accept(child);
    }

    public static <Ex extends Exception> void postOrder(Node node, NodeConsumer<Node, Ex> consumer) throws Exception {
        for (var child : node.children())
            consumer.accept(child);

        consumer.accept(node);
    }

    public static Stream<Node> traversePostOrder(Node node) {
        var split = new Spliterator<Node>() {
            private Deque<Node> stack = new LinkedList<>();
            private Deque<Iterator<? extends Node>> childrenStack = new LinkedList<>();

            @Override
            public boolean tryAdvance(Consumer<? super Node> action) {
                while (!stack.isEmpty()) {
                    if (childrenStack.peek().hasNext()) {
                        childrenStack.pop();
                        var node = stack.pop();
                        action.accept(node);
                        return true;
                    } else {
                        stack.push(childrenStack.peek().next());
                        childrenStack.push(stack.peek().children().iterator());
                    }
                }
                return false;
            }

            @Override
            public Spliterator<Node> trySplit() {
                return null;
            }

            @Override
            public long estimateSize() {
                return Long.MAX_VALUE;
            }

            @Override
            public int characteristics() {
                return Spliterator.ORDERED | Spliterator.IMMUTABLE | Spliterator.NONNULL;
            }
        };

        return StreamSupport.stream(split, false);
    }

    public static Stream<Node> traversePreOrder(Node node) {
        var split = new Spliterator<Node>() {
            private Deque<Node> stack = new LinkedList<>();
            private Deque<Iterator<? extends Node>> childrenStack = new LinkedList<>();

            @Override
            public boolean tryAdvance(Consumer<? super Node> action) {
                while (!stack.isEmpty()) {
                    if (childrenStack.peek().hasNext()) {
                        childrenStack.pop();
                        stack.pop();
                    } else {
                        stack.push(childrenStack.peek().next());
                        childrenStack.push(stack.peek().children().iterator());
                        action.accept(stack.peek());
                        return true;
                    }
                }
                return false;
            }

            @Override
            public Spliterator<Node> trySplit() {
                return null;
            }

            @Override
            public long estimateSize() {
                return Long.MAX_VALUE;
            }

            @Override
            public int characteristics() {
                return Spliterator.ORDERED | Spliterator.IMMUTABLE | Spliterator.NONNULL;
            }
        };

        return StreamSupport.stream(split, false);
    }
}
