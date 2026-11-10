import java.awt.Color;

/**
 * Inside a recovery centre. Still infected, but protected from new contacts
 * and not moving. When the treatment time runs out the person recovers.
 *
 * This is the only state that holds data of its own (the time left), which is
 * why each patient gets a new InTreatmentState object.
 */
public class InTreatmentState extends HealthState {

    private int timeLeft;   // milliseconds

    public InTreatmentState(int milliseconds){
        this.timeLeft = milliseconds;
    }

    @Override
    public String getName(){
        return "In treatment";
    }

    @Override
    public Color getColor(){
        return Color.yellow;
    }

    @Override
    public int getSpeed(){
        return 0;
    }

    @Override
    public boolean canMove(){
        return false;
    }

    @Override
    public boolean isInfected(){
        return true;
    }

    @Override
    public void tick(Healthy person, int elapsedMilliseconds){
        timeLeft = timeLeft - elapsedMilliseconds;

        if (timeLeft <= 0){
            person.setState(new RecoveredState());
        }
    }
}
