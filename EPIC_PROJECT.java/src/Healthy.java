public class Healthy extends Person{

    // Speeds live here because the object itself decides how fast it moves.
    private static final int NORMAL_SPEED   = 6;
    private static final int INFECTED_SPEED = 2;

    // How long someone must be clear of every virus before a touch counts as
    // a new contact. 20 ticks = 1 second at 20 ticks a second.
    private static final int CLEAR_TICKS_NEEDED = 20;

    // The person's state is held in these flags. No new class, no swapping the
    // object out of the array -- the same Healthy object just starts behaving
    // differently as they change.
    private boolean infected = false;
    private boolean recovered = false;
    private boolean dead = false;

    // Treatment time still to go, in milliseconds. 0 means not being treated.
    private int treatmentTimeLeft = 0;

    // How many ticks in a row this person has not been touching any virus.
    // Dots overlap for several ticks when they pass each other, and the small
    // random wobble can make one pass look like touch, gap, touch. Counting
    // clear ticks is how a genuinely new contact is told apart from that.
    // It starts "clear", so the very first touch always counts.
    private int ticksClear = CLEAR_TICKS_NEEDED;

    public Healthy(String name, int age, int xPos, int yPos){
        super(name, age, xPos, yPos);
    }

    public Healthy(int id, String name, int age, int xPos, int yPos){
        super(id, name, age, xPos, yPos);
    }

    /**
     * Called on each new contact with a virus.
     * A healthy person becomes infected. An infected person has now been hit
     * a second time, which is fatal, so this throws PersonDiedException.
     * Recovered people are immune, patients are protected inside their
     * centre, and the dead can't be hit again, so nothing happens to them.
     */
    public void infect() throws PersonDiedException {

        if (recovered || dead || isInTreatment()){
            return;
        }

        if (infected){
            throw new PersonDiedException(getName() + " has died");
        }

        infected = true;
    }

    /** Called when this person has been hit a second time. */
    public void die(){
        dead = true;
        infected = false;
        treatmentTimeLeft = 0;
    }

    /**
     * Called once every tick with whether this person is touching a virus.
     * Returns true only for a new contact: a touch after being clear of every
     * virus for at least a second. The same pass carrying on doesn't count.
     */
    public boolean isNewContact(boolean touchingNow){

        boolean newContact = touchingNow && ticksClear >= CLEAR_TICKS_NEEDED;

        if (touchingNow){
            ticksClear = 0;
        } else {
            ticksClear++;
        }
        return newContact;
    }

    public boolean isInfected(){
        return infected;
    }

    public boolean isRecovered(){
        return recovered;
    }

    public boolean isDead(){
        return dead;
    }

    public boolean isInTreatment(){
        return treatmentTimeLeft > 0;
    }

    /** Infected, and not already being looked after at a recovery centre. */
    public boolean needsTreatment(){
        return infected && !isInTreatment();
    }

    /** Called by a recovery centre when this person is admitted. */
    public void startTreatment(int milliseconds){
        treatmentTimeLeft = milliseconds;
    }

    /**
     * Counts treatment down by the time since the last tick. When it runs out,
     * the person is cured and stays immune from then on.
     */
    public void updateTreatment(int elapsedMilliseconds){

        if (!isInTreatment()){
            return;
        }

        treatmentTimeLeft = treatmentTimeLeft - elapsedMilliseconds;

        if (treatmentTimeLeft <= 0){
            treatmentTimeLeft = 0;
            infected = false;
            recovered = true;
        }
    }

    /**
     * The object reports its own speed, so PanelDemo does not need to know the
     * rule. Change the two numbers above and nothing else has to move.
     * A recovered person is no longer infected, so they go back to normal speed.
     */
    public int getSpeed(){
        if (infected){
            return INFECTED_SPEED;
        }
        return NORMAL_SPEED;
    }

    @Override
    public String getStatus(){
        if (dead){
            return "Dead";
        }
        if (recovered){
            return "Recovered";
        }
        if (isInTreatment()){
            return "In treatment";
        }
        if (infected){
            return "Infected";
        }
        return "Healthy";
    }
}