import java.awt.Color;

/**
 * Infected and walking around. Slower than a healthy person.
 * A second virus contact in this state is fatal.
 */
public class InfectedState extends HealthState {

    @Override
    public String getName(){
        return "Infected";
    }

    @Override
    public Color getColor(){
        return Color.yellow;
    }

    @Override
    public int getSpeed(){
        return 2;
    }

    @Override
    public boolean isInfected(){
        return true;
    }

    @Override
    public void virusContact(Healthy person) throws PersonDiedException {
        throw new PersonDiedException(person.getName() + " has died");
    }
}
