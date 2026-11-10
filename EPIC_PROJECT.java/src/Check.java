/**
 * A yes/no question about one item, like "is this person dead?".
 *
 * T is the type of item being checked. Because it's generic, the same
 * interface works for people, viruses, or anything else.
 *
 * It has exactly one abstract method, which makes it a functional interface:
 * a Check can be written as a lambda (p -> p.isDead()) or a method reference
 * (Healthy::isDead) instead of a whole anonymous class.
 */
@FunctionalInterface
public interface Check<T> {

    boolean test(T item);
}
