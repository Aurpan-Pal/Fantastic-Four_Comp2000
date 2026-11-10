import java.util.Arrays;

/**
 * The second movement strategy: walk straight towards the nearest recovery
 * centre. People who "follow health advice" switch to this the moment they
 * are infected, then switch back to their random walk once they leave the
 * Infected state.
 *
 * It keeps no data about any one person, so a single object is shared by
 * everyone who follows advice.
 */
public class HeadToNearestCentre implements MovementStrategy {

    private final RecoveryCentre[] centres;

    public HeadToNearestCentre(RecoveryCentre[] centres){
        this.centres = centres;
    }

    @Override
    public void move(Movable thing, int speed, int maxX, int maxY){

        // Nearest centre, found with a stream and a lambda comparator.
        RecoveryCentre target = Arrays.stream(centres)
                .min((a, b) -> distanceSquared(thing, a) - distanceSquared(thing, b))
                .get();

        int dx = target.getCentreX() - thing.getXPos();
        int dy = target.getCentreY() - thing.getYPos();

        double length = Math.sqrt(dx * dx + dy * dy);

        if (length < 1){
            return;     // already there
        }

        // One step of the given length along the straight line to the target.
        int stepX = (int) Math.round(dx / length * speed);
        int stepY = (int) Math.round(dy / length * speed);

        thing.setXPos(Math.max(0, Math.min(maxX, thing.getXPos() + stepX)));
        thing.setYPos(Math.max(0, Math.min(maxY, thing.getYPos() + stepY)));
    }

    // Squared distance is enough to compare which centre is closer.
    private static int distanceSquared(Movable thing, RecoveryCentre centre){
        int dx = centre.getCentreX() - thing.getXPos();
        int dy = centre.getCentreY() - thing.getYPos();
        return dx * dx + dy * dy;
    }
}
