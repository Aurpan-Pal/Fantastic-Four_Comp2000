/**
 * A yes/no question about one item, like "is this person dead?".
 *
 * T is the type of item being checked. Because it's generic, the same
 * interface works for people, viruses, or anything else.
 */
public interface Check<T> {

    boolean test(T item);
}
