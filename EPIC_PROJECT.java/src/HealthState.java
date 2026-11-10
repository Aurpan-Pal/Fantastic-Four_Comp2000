import java.awt.Color;

/**
 * STATE PATTERN.
 *
 * Each health state (Healthy, Infected, In treatment, Recovered, Dead) is its
 * own class. A person holds exactly one of these and hands its behaviour over
 * to it: what colour to draw, how fast to walk, and what a virus contact does.
 *
 * Before this, Healthy kept three boolean flags plus a timer, and every method
 * (getStatus, getSpeed, infect, and the colour code in PanelDemo) had its own
 * if-else chain over those flags. Adding a new state meant editing all of
 * them. Now a new state is one new class.
 *
 * It is an abstract class (not an interface) so the common "do nothing"
 * behaviour is written once here and states only override what is different.
 */
public abstract class HealthState {

    /** Shown on screen and used to group people by status. */
    public abstract String getName();

    /** Colour of the dot. */
    public abstract Color getColor();

    /** How far the person walks each tick. */
    public abstract int getSpeed();

    /** Patients and the dead stay where they are. */
    public boolean canMove(){
        return true;
    }

    /** Carrying the virus. Being in treatment still counts. */
    public boolean isInfected(){
        return false;
    }

    /**
     * A new contact with a virus. Default: nothing happens (the person is
     * immune, protected inside a centre, or already dead). States that react
     * override this, and may move the person to a new state.
     */
    public void virusContact(Healthy person) throws PersonDiedException {
    }

    /** Called every tick with the time passed. Most states have nothing to do. */
    public void tick(Healthy person, int elapsedMilliseconds){
    }
}
