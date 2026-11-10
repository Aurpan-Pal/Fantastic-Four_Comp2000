import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Healthy extends Person{

    // How long someone must be clear of every virus before a touch counts as
    // a new contact. 20 ticks = 1 second at 20 ticks a second.
    private static final int CLEAR_TICKS_NEEDED = 20;

    // STATE PATTERN: the person's health is one HealthState object instead of
    // three boolean flags. Changing health means swapping this object. The
    // Healthy object itself never leaves the array -- it just starts behaving
    // differently because a different state is doing the work.
    private HealthState state = new HealthyState();

    // STRATEGY PATTERN: how this person walks normally, and how they walk
    // while infected. For most people both are the same random walk. Someone
    // who follows health advice gets HeadToNearestCentre for when infected.
    private MovementStrategy normalMovement;
    private MovementStrategy infectedMovement;

    // OBSERVER PATTERN: everyone who wants to hear about health changes.
    private final List<StatusObserver> observers = new ArrayList<>();

    // How many ticks in a row this person has not been touching any virus.
    // Dots overlap for several ticks when they pass each other, and the small
    // random wobble can make one pass look like touch, gap, touch. Counting
    // clear ticks is how a genuinely new contact is told apart from that.
    // It starts "clear", so the very first touch always counts.
    private int ticksClear = CLEAR_TICKS_NEEDED;

    public Healthy(String name, int age, int xPos, int yPos){
        super(name, age, xPos, yPos);
        normalMovement = getMovement();
        infectedMovement = normalMovement;
    }

    public Healthy(int id, String name, int age, int xPos, int yPos){
        super(id, name, age, xPos, yPos);
        normalMovement = getMovement();
        infectedMovement = normalMovement;
    }

    // ---------- state ----------

    /**
     * Swaps in a new health state, picks the matching way to walk, and tells
     * every observer what changed. Only the states themselves (and die /
     * startTreatment below) call this.
     */
    public void setState(HealthState newState){

        HealthState oldState = state;
        state = newState;

        if (newState instanceof InfectedState){
            setMovement(infectedMovement);
        } else {
            setMovement(normalMovement);
        }

        notifyObservers(oldState, newState);
    }

    /**
     * Called on each new contact with a virus. What happens is decided by the
     * current state: a healthy person becomes infected, an infected person
     * throws PersonDiedException (a second hit is fatal), and everyone else is
     * left alone.
     */
    public void infect() throws PersonDiedException {
        state.virusContact(this);
    }

    /** Called when this person has been hit a second time. */
    public void die(){
        setState(new DeadState());
    }

    /** Called by a recovery centre when this person is admitted. */
    public void startTreatment(int milliseconds){
        setState(new InTreatmentState(milliseconds));
    }

    /** Lets the state count down treatment. Does nothing in other states. */
    public void updateTreatment(int elapsedMilliseconds){
        state.tick(this, elapsedMilliseconds);
    }

    // ---------- strategy ----------

    /** This person will walk using the given strategy whenever infected. */
    public void setInfectedMovement(MovementStrategy movement){
        this.infectedMovement = movement;
    }

    /** True if this person heads for a centre when infected. */
    public boolean followsAdvice(){
        return infectedMovement != normalMovement;
    }

    // ---------- observer (Healthy is the subject) ----------

    public void addObserver(StatusObserver observer){
        observers.add(observer);
    }

    // An observer that is no longer needed must be removed. Otherwise this
    // list keeps a reference to it forever, so it can never be garbage
    // collected (the "lapsed listener" memory leak).
    public void removeObserver(StatusObserver observer){
        observers.remove(observer);
    }

    // Tells every observer what changed.
    private void notifyObservers(HealthState from, HealthState to){
        observers.forEach(observer -> observer.statusChanged(this, from, to));
    }

    // ---------- contact tracking ----------

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

    // ---------- questions about the person (answered by the state) ----------

    public boolean isInfected(){
        return state.isInfected();
    }

    public boolean isRecovered(){
        return state instanceof RecoveredState;
    }

    public boolean isDead(){
        return state instanceof DeadState;
    }

    public boolean isInTreatment(){
        return state instanceof InTreatmentState;
    }

    /** Infected, and not already being looked after at a recovery centre. */
    public boolean needsTreatment(){
        return state instanceof InfectedState;
    }

    public boolean canMove(){
        return state.canMove();
    }

    public int getSpeed(){
        return state.getSpeed();
    }

    public Color getColor(){
        return state.getColor();
    }

    @Override
    public String getStatus(){
        return state.getName();
    }
}
