import java.awt.Color;

/** Not infected yet. A virus contact makes the person infected. */
public class HealthyState extends HealthState {

    @Override
    public String getName(){
        return "Healthy";
    }

    @Override
    public Color getColor(){
        return Color.green;
    }

    @Override
    public int getSpeed(){
        return 6;
    }

    @Override
    public void virusContact(Healthy person){
        person.setState(new InfectedState());
    }
}
