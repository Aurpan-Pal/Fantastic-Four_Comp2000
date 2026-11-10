/**
 * An observer that tracks how many people are infected right now, and the
 * highest that number has ever been (the peak of the outbreak).
 *
 * The peak can't be worked out from a single frame -- it needs a running
 * count over time -- so it is a good job for an observer that hears every
 * state change as it happens.
 *
 * WHY synchronized: PanelDemo checks infections with a parallel stream, so
 * several threads can call statusChanged() at the same moment. "count++" is
 * really three steps (read, add one, write back). Two threads doing it at once
 * can both read the same old value and one update is lost -- a race
 * condition. synchronized lets only one thread inside at a time.
 */
public class OutbreakStats implements StatusObserver {

    private int currentlyInfected = 0;
    private int peakInfected = 0;

    @Override
    public synchronized void statusChanged(Healthy person, HealthState from, HealthState to){

        if (!from.isInfected() && to.isInfected()){
            currentlyInfected++;

            if (currentlyInfected > peakInfected){
                peakInfected = currentlyInfected;
            }
        }
        else if (from.isInfected() && !to.isInfected()){
            currentlyInfected--;
        }
        // Infected -> In treatment: still infected, so no change.
    }

    public synchronized int getCurrentlyInfected(){
        return currentlyInfected;
    }

    public synchronized int getPeakInfected(){
        return peakInfected;
    }
}
