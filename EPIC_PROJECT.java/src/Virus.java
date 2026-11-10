public class Virus implements Movable {

    private String virusName;
    private int spreadPercentage;
    private int xPos;
    private int yPos;

    // STRATEGY: viruses always random-walk. Each virus needs its own
    // RandomWalk because the walk remembers its current direction.
    private MovementStrategy movement = new RandomWalk();

    // Old constructor kept so Client.java still compiles.
    public Virus(String virusName, int spreadPercentage) {
        this(virusName, spreadPercentage, 0, 0);
    }

    public Virus(String virusName, int spreadPercentage, int xPos, int yPos) {
        this.virusName = virusName;
        this.spreadPercentage = spreadPercentage;
        this.xPos = xPos;
        this.yPos = yPos;
    }

    public String getVirusName() {
        return virusName;
    }

    public int getSpreadPercentage() {
        return spreadPercentage;
    }

    public void setSpreadPercentage(int spreadPercentage) {
        this.spreadPercentage = spreadPercentage;
    }

    @Override
    public int getXPos() {
        return xPos;
    }

    @Override
    public int getYPos() {
        return yPos;
    }

    @Override
    public void setXPos(int xPos) {
        this.xPos = xPos;
    }

    @Override
    public void setYPos(int yPos) {
        this.yPos = yPos;
    }

    // The walking code used to be copied here from Person. Now both just
    // hand the job to a RandomWalk strategy.
    public void moveRandom(int speed, int maxX, int maxY) {
        movement.move(this, speed, maxX, maxY);
    }

    @Override
    public String toString() {
        return virusName + " - " + spreadPercentage + "%";
    }
}
