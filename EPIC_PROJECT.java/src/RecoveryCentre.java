import java.awt.*;

/**
 * Anywhere an infected person can go to get better.
 *
 * This is the contract: every recovery centre must be able to do these
 * things. PanelDemo only ever uses this type, so it never needs to know
 * whether it is dealing with a hospital or a medical camp.
 */
public interface RecoveryCentre {

    /** True if the point (x, y) is inside this centre. */
    boolean contains(int x, int y);

    /** Starts treating an infected person who has walked in. */
    void admit(Healthy patient);

    /** How long treatment takes here, in seconds. */
    int getRecoverySeconds();

    /** Draws the centre on the map. */
    void draw(Graphics g);

    /** The middle of the centre, so people can walk towards it. */
    int getCentreX();
    int getCentreY();
}
