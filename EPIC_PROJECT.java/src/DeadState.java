import java.awt.Color;

/** Hit a second time while infected. Stays where they fell. */
public class DeadState extends HealthState {

    @Override
    public String getName(){
        return "Dead";
    }

    @Override
    public Color getColor(){
        return Color.gray;
    }

    @Override
    public int getSpeed(){
        return 0;
    }

    @Override
    public boolean canMove(){
        return false;
    }
}
