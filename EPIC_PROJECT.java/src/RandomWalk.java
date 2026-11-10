/**
 * Travel in one direction, wobbling a bit as it goes. On hitting the edge,
 * pick a brand new random direction.
 *
 * This is the movement code that used to be copied in both Person and Virus.
 * It remembers its current direction between ticks, so every person and virus
 * needs its own RandomWalk object.
 */
public class RandomWalk implements MovementStrategy {

    // Current direction of travel. Kept between ticks -- this is what makes
    // the dot actually go somewhere instead of shaking on the spot.
    private int dx;
    private int dy;

    // The speed used on the previous tick, so a change can be spotted.
    private int currentSpeed;

    @Override
    public void move(Movable thing, int speed, int maxX, int maxY){

        if (dx == 0 && dy == 0){
            pickDirection(speed);
        }
        else if (speed != currentSpeed){
            // A different speed than last tick (e.g. the moment a person is
            // infected). Resize the step straight away instead of waiting for
            // the next wall bounce.
            resizeStep(speed);
        }

        currentSpeed = speed;

        // The vibrate part: a small random nudge on top of the steady heading.
        int wobbleX = (int)(Math.random() * 3) - 1;
        int wobbleY = (int)(Math.random() * 3) - 1;

        int x = thing.getXPos() + dx + wobbleX;
        int y = thing.getYPos() + dy + wobbleY;

        boolean hitWall = false;

        if (x < 0){
            x = 0;
            hitWall = true;
        }
        if (x > maxX){
            x = maxX;
            hitWall = true;
        }
        if (y < 0){
            y = 0;
            hitWall = true;
        }
        if (y > maxY){
            y = maxY;
            hitWall = true;
        }

        if (hitWall){
            pickDirection(speed);

            // The new direction is random, so it might point straight back into
            // the wall. Flip it if so, otherwise the dot gets stuck on the edge.
            if (x <= 0 && dx < 0){ dx = -dx; }
            if (x >= maxX && dx > 0){ dx = -dx; }
            if (y <= 0 && dy < 0){ dy = -dy; }
            if (y >= maxY && dy > 0){ dy = -dy; }
        }

        thing.setXPos(x);
        thing.setYPos(y);
    }

    // Keep the direction we are already travelling, change only how far each
    // step goes. atan2 recovers the current heading from dx and dy.
    private void resizeStep(int speed){

        double angle = Math.atan2(dy, dx);

        dx = (int) Math.round(Math.cos(angle) * speed);
        dy = (int) Math.round(Math.sin(angle) * speed);

        if (dx == 0 && dy == 0){
            dx = speed;
        }
    }

    // Random angle turned into a dx/dy step of the given length.
    private void pickDirection(int speed){

        double angle = Math.random() * Math.PI * 2;

        dx = (int) Math.round(Math.cos(angle) * speed);
        dy = (int) Math.round(Math.sin(angle) * speed);

        if (dx == 0 && dy == 0){
            dx = speed;
        }
    }
}
