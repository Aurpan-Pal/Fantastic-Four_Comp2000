public class Person implements Movable {
    private static int nextId =1;

    private int id;
    private String name;
    private int age;
    private int xPos;
    private int yPos;

    // STRATEGY: how this person walks. The walking code itself lives in the
    // strategy object (RandomWalk by default), not in Person. Healthy swaps
    // this when its health state changes.
    private MovementStrategy movement = new RandomWalk();

    public Person(String name, int age, int xPos, int yPos){
        this.id = nextId;
        nextId++;

        this.name = name;
        this.age = age;
        this.xPos = xPos;
        this.yPos = yPos;
    }

    public Person(int id, String name, int age, int xPos, int yPos){
        this.id = id;
        this.name = name;
        this.age = age;
        this.xPos = xPos;
        this.yPos = yPos;
    }

    public int getId(){
        return id;
    }
    public String getName(){
        return name;
    }
    public int getAge(){
        return age;
    }
    @Override
    public int getXPos(){
        return xPos;
    }
    @Override
    public int getYPos(){
        return yPos;
    }
    @Override
    public void setXPos(int xPos){
        this.xPos = xPos;
    }
    @Override
    public void setYPos(int yPos){
        this.yPos = yPos;
    }
    public String getStatus(){
        return "Person";
    }

    public final MovementStrategy getMovement(){
        return movement;
    }

    public void setMovement(MovementStrategy movement){
        this.movement = movement;
    }

    // Same name as before so nothing that calls it has to change. The person
    // no longer knows HOW it moves -- it just asks its strategy to do it.
    public void moveRandom(int speed, int maxX, int maxY){
        movement.move(this, speed, maxX, maxY);
    }

    @Override
    public String toString(){
        return "ID: " + id + ", Name: " + name + ", Age: " + age + ", Status: " + getStatus();
    }
}
