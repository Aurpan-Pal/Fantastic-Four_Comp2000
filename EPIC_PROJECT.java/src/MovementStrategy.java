/**
 * STRATEGY PATTERN.
 *
 * A way of moving around the map. The thing being moved (a person or a virus)
 * holds a MovementStrategy and calls move() every tick. It doesn't know or care
 * which strategy it has, so the walking behaviour can be swapped at runtime.
 *
 * Before this, Person and Virus each had their own identical copy of the
 * random-walk code (moveRandom, resizeStep, pickDirection). Now that code lives
 * once, in RandomWalk.
 */
public interface MovementStrategy {

    /** Moves the thing one step, keeping it inside 0..maxX and 0..maxY. */
    void move(Movable thing, int speed, int maxX, int maxY);
}
