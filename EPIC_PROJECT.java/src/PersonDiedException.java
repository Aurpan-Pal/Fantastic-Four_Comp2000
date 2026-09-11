/**
 * Thrown when a virus hits someone who is already infected. A second hit is
 * fatal, and the simulation has to deal with it.
 *
 * It extends Exception, which makes it a checked exception: Java won't
 * compile any code that calls infect() unless that code deals with it.
 */
public class PersonDiedException extends Exception {

    public PersonDiedException(String message) {
        super(message);
    }
}
