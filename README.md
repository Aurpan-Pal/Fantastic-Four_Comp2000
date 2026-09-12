# Life - Epidemic Simulation

A Java simulation of a virus spreading through a city. People move around randomly and get infected when they touch a virus. Infected people can recover at hospitals and medical camps, but if they touch a virus again before that, they die.



## How to run

Requires Java 8 or newer.

```
javac *.java
java App
```

Or open the folder in VS Code and run `App.java`.

## How it works

- Green = healthy, yellow = infected, cyan = recovered, grey = dead, red = virus.
- A healthy person who touches a virus becomes infected and moves slower.
- An infected person who touches a virus again dies. A new touch only counts after they've been away from viruses for at least 1 second.
- Infected people who walk into a hospital (blue) or medical camp (purple) get treated and are safe while inside. Hospitals take 5 seconds and camps take 10 seconds.
- Recovered people can't be infected again.
- The counter at the top shows how many people are healthy, infected, recovered and dead.

## Files

- `App.java` - starts the program
- `PanelDemo.java` - sets up the city, runs the simulation and draws it
- `Person.java` - base class for a person (ID, name, age, position, movement)
- `Healthy.java` - a person's health state (infected, in treatment, recovered, dead)
- `Virus.java` - a virus and its movement
- `RecoveryCentre.java` - interface for recovery centres
- `Facility.java` - abstract class with the shared code for recovery centres
- `Hospital.java` - 40x40 centre, 5 second recovery
- `MedicalCamp.java` - 20x20 centre, 10 second recovery
- `Check.java` - generic interface used for counting
- `PersonDiedException.java` - exception thrown when an infected person is hit again

## OOP concepts used

- Inheritance: `Healthy` extends `Person`, and `Hospital` and `MedicalCamp` extend `Facility`
- Interface: `RecoveryCentre`
- Abstract class: `Facility`
- Polymorphism: hospitals and camps are stored in one `RecoveryCentre[]` array
- Overloading: `Person`, `Healthy` and `Virus` each have two constructors
- Generics: `Check<T>` and the generic `count()` method in `PanelDemo`
- Exceptions: `PersonDiedException` is thrown in `infect()` and caught in `PanelDemo`

