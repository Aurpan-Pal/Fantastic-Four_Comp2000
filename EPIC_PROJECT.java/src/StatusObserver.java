/**
 * OBSERVER PATTERN.
 *
 * Anything that wants to hear when a person's health state changes.
 * The person (the subject) keeps a list of observers and tells each of them
 * on every change. The person doesn't know what the observers do with it:
 * printing to the console and tracking outbreak numbers are both observers,
 * and adding a new one never touches Healthy.
 *
 * It has a single method, so it is also a functional interface: a simple
 * observer can be written as a lambda (see the death logger in PanelDemo).
 */
@FunctionalInterface
public interface StatusObserver {

    void statusChanged(Healthy person, HealthState from, HealthState to);
}
