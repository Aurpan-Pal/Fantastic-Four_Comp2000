import java.awt.Color;

/** Cured and immune from then on. Back to normal speed. */
public class RecoveredState extends HealthState {

    @Override
    public String getName(){
        return "Recovered";
    }

    @Override
    public Color getColor(){
        return Color.cyan;
    }

    @Override
    public int getSpeed(){
        return 6;
    }
}
