/**
 * Anything with a position on the map that a MovementStrategy can move.
 * Person and Virus both implement this, so one strategy class works for both.
 */
public interface Movable {

    int getXPos();
    int getYPos();
    void setXPos(int xPos);
    void setYPos(int yPos);
}
