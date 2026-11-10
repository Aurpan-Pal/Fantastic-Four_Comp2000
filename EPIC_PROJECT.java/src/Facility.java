import java.awt.*;

/**
 * The shared part of every recovery centre: a square in a fixed spot on the
 * map. Hospital and MedicalCamp extend this and only fill in what is
 * different about them.
 *
 * It is abstract because a plain "facility" doesn't exist in the city --
 * only hospitals and medical camps do.
 *
 * getRecoverySeconds() is not written here. It comes from the RecoveryCentre
 * interface, and because this class is abstract it is allowed to leave that
 * method for Hospital and MedicalCamp to fill in.
 */
public abstract class Facility implements RecoveryCentre {

    private int xPos;
    private int yPos;
    private int size;   // width and height, since every centre is a square

    public Facility(int xPos, int yPos, int size){
        this.xPos = xPos;
        this.yPos = yPos;
        this.size = size;
    }

    // Each kind of facility decides these for itself.
    public abstract String getName();
    public abstract Color getColor();

    @Override
    public boolean contains(int x, int y){
        return x >= xPos && x <= xPos + size
            && y >= yPos && y <= yPos + size;
    }

    @Override
    public void admit(Healthy patient){
        // getRecoverySeconds() runs the Hospital or MedicalCamp version,
        // whichever this object actually is.
        patient.startTreatment(getRecoverySeconds() * 1000);
    }

    @Override
    public int getCentreX(){
        return xPos + size / 2;
    }

    @Override
    public int getCentreY(){
        return yPos + size / 2;
    }

    @Override
    public void draw(Graphics g){
        g.setColor(getColor());
        g.fillRect(xPos, yPos, size, size);

        g.setColor(Color.white);
        g.drawString(getName() + " (" + getRecoverySeconds() + "s)", xPos, yPos - 4);
    }
}
