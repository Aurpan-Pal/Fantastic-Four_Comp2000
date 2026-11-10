import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PanelDemo {

    // Size of the drawing area, and how big each dot is.
    private static final int SIM_WIDTH  = 1200;
    private static final int SIM_HEIGHT = 800;
    private static final int DOT_SIZE   = 10;

    private static final int HEALTHY_COUNT = 70;
    private static final int VIRUS_COUNT   = 10;

    // Out of every 100 people, roughly how many follow health advice: the
    // moment they are infected they walk straight to the nearest recovery
    // centre. Everyone else keeps wandering. The live death rates on screen
    // show what difference this makes.
    private static final int ADVICE_PERCENT = 40;

    // How often the timer fires, in milliseconds. 50ms = 20 ticks a second.
    // Treatment time also counts down by this much every tick.
    private static final int TICK_MS = 50;

    // Furthest a dot's top-left corner can go and still be fully on screen.
    private static final int MAX_X = SIM_WIDTH  - DOT_SIZE;
    private static final int MAX_Y = SIM_HEIGHT - DOT_SIZE;

    public static void main(String[] args) {

        // ---------- build the recovery centres ----------
        // The array type is the interface, so one array holds both kinds.
        // Positions are fixed so the centres never overlap each other.
        final RecoveryCentre[] centres = {
            new Hospital(250, 200),
            new Hospital(900, 550),
            new MedicalCamp(150, 620),
            new MedicalCamp(600, 380),
            new MedicalCamp(1000, 150)
        };

        // STRATEGY: one shared "walk to the nearest centre" route for everyone
        // who follows advice. It holds no per-person data, so one is enough.
        final MovementStrategy goToCare = new HeadToNearestCentre(centres);

        // OBSERVERS: every person reports health changes to these two.
        // The stats tracker is a full class; the logger is short enough to be
        // a lambda, because StatusObserver is a functional interface.
        final OutbreakStats stats = new OutbreakStats();
        final StatusObserver deathLogger = (person, from, to) -> {
            if (to instanceof DeadState) {
                System.out.println(person.getName() + " has died");
            }
        };

        // ---------- build the healthy people ----------
        // A stream of the numbers 0..69, each turned into a person, collected
        // into an array. Healthy[]::new tells toArray what type of array to make.
        // Kept sequential on purpose: Person's id counter (nextId++) is not
        // thread-safe, so building people in parallel could give two the same id.
        final Healthy[] healthyPeople = IntStream.range(0, HEALTHY_COUNT)
                .mapToObj(i -> makePerson(i, goToCare, stats, deathLogger))
                .toArray(Healthy[]::new);

        // ---------- build the viruses ----------
        final Virus[] viruses = IntStream.range(0, VIRUS_COUNT)
                .mapToObj(i -> new Virus("Zombie Virus",
                                         (int)(Math.random() * 101),
                                         randomX(), randomY()))
                .toArray(Virus[]::new);

        Frame f = new Frame("Life - Epidemic Simulation");
        f.setLayout(null);
        f.setResizable(false);

        // ---------- the panel ----------
        final Panel panel = new Panel() {

            private Image buffer;

            // AWT clears the panel to the background colour before every paint,
            // which makes all these moving dots flicker badly. Overriding
            // update() and drawing to an offscreen image first fixes it: one
            // finished frame gets copied to the screen in a single go.
            @Override
            public void update(Graphics g) {
                paint(g);
            }

            @Override
            public void paint(Graphics g) {

                if (buffer == null) {
                    buffer = createImage(SIM_WIDTH, SIM_HEIGHT);
                }

                Graphics bg = buffer.getGraphics();

                bg.setColor(getBackground());
                bg.fillRect(0, 0, SIM_WIDTH, SIM_HEIGHT);

                // Centres first, so the dots are drawn on top of them and
                // people being treated stay visible.
                Arrays.stream(centres).forEach(centre -> centre.draw(bg));

                // Each person's state decides its colour. People who follow
                // advice get a white ring so you can pick them out.
                Arrays.stream(healthyPeople).forEach(person -> {
                    bg.setColor(person.getColor());
                    bg.fillOval(person.getXPos(), person.getYPos(), DOT_SIZE, DOT_SIZE);

                    if (person.followsAdvice()) {
                        bg.setColor(Color.white);
                        bg.drawOval(person.getXPos() - 1, person.getYPos() - 1,
                                    DOT_SIZE + 1, DOT_SIZE + 1);
                    }
                });

                bg.setColor(Color.red);
                Arrays.stream(viruses).forEach(virus ->
                        bg.fillOval(virus.getXPos(), virus.getYPos(), DOT_SIZE, DOT_SIZE));

                drawStats(bg, healthyPeople, stats);

                bg.dispose();
                g.drawImage(buffer, 0, 0, null);
            }
        };

        panel.setBackground(new Color(18, 20, 26));
        f.add(panel);

        f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                f.dispose();
                System.exit(0);
            }
        });

        // Insets are 0 until the window has a native peer. Ask for the peer
        // first, otherwise the title bar eats the bottom of the panel.
        f.addNotify();
        Insets in = f.getInsets();

        panel.setBounds(in.left, in.top, SIM_WIDTH, SIM_HEIGHT);
        f.setSize(in.left + SIM_WIDTH + in.right, in.top + SIM_HEIGHT + in.bottom);
        f.setLocationRelativeTo(null);

        f.setVisible(true);

        // ---------- the time function ----------
        // ActionListener has one method, so the whole listener is a lambda.
        javax.swing.Timer timer = new javax.swing.Timer(TICK_MS, e -> {
            tick(healthyPeople, viruses, centres);
            panel.repaint();
        });

        timer.start();
    }

    // One step of the simulation: move everything, then check for contact
    // and admissions.
    private static void tick(Healthy[] healthyPeople, Virus[] viruses, RecoveryCentre[] centres) {

        Arrays.stream(healthyPeople).forEach(person -> {
            if (person.isInTreatment()) {
                // Patients stay put inside the centre until they are cured.
                person.updateTreatment(TICK_MS);
            } else if (person.canMove()) {
                // Speed comes from the state, and the route from the strategy.
                person.moveRandom(person.getSpeed(), MAX_X, MAX_Y);
            }
            // The dead stay where they fell.
        });

        Arrays.stream(viruses).forEach(virus -> virus.moveRandom(2, MAX_X, MAX_Y));

        checkInfections(healthyPeople, viruses);
        checkAdmissions(healthyPeople, centres);
    }

    // Every living person against every virus. What a new contact does is
    // decided by the person's state (see HealthState.virusContact).
    //
    // PARALLEL STREAM: people are split across several threads and checked at
    // the same time. This is safe because:
    //   - each person only ever changes its OWN fields here,
    //   - viruses are only read (they already moved earlier in this tick),
    //   - the one shared object the observers write to, OutbreakStats, is
    //     synchronized.
    // forEach waits for every thread to finish before returning, so paint()
    // always sees a finished tick.
    // Honest note: with only 70 people, splitting the work costs more than it
    // saves. It pays off with thousands of people (try HEALTHY_COUNT = 5000).
    private static void checkInfections(Healthy[] healthyPeople, Virus[] viruses) {

        Arrays.stream(healthyPeople)
              .parallel()
              .filter(person -> !person.isDead())
              .forEach(person -> {

                  boolean touching = Arrays.stream(viruses)
                                           .anyMatch(virus -> isTouching(person, virus));

                  if (person.isNewContact(touching)) {
                      try {
                          person.infect();
                      } catch (PersonDiedException ex) {
                          // A second hit while already infected is fatal.
                          // The death logger observer reports it.
                          person.die();
                      }
                  }
              });
    }

    // Any infected person who walks into a recovery centre gets admitted there.
    // The centre decides how long treatment takes, so this method never needs
    // to know whether it is a hospital or a medical camp.
    private static void checkAdmissions(Healthy[] healthyPeople, RecoveryCentre[] centres) {

        Arrays.stream(healthyPeople)
              .filter(Healthy::needsTreatment)
              .forEach(person -> {

                  // Use the middle of the dot, so "inside" means the dot is really in.
                  int midX = person.getXPos() + DOT_SIZE / 2;
                  int midY = person.getYPos() + DOT_SIZE / 2;

                  Arrays.stream(centres)
                        .filter(centre -> centre.contains(midX, midY))
                        .findFirst()
                        .ifPresent(centre -> centre.admit(person));
              });
    }

    // Two lines of live numbers in the top-left corner.
    private static void drawStats(Graphics bg, Healthy[] healthyPeople, OutbreakStats stats) {

        // groupingBy: one pass over everyone, counted by status name.
        // e.g. {Healthy=40, Infected=12, In treatment=3, Recovered=10, Dead=5}
        Map<String, Long> byStatus = Arrays.stream(healthyPeople)
                .collect(Collectors.groupingBy(Healthy::getStatus, Collectors.counting()));

        // partitioningBy: split everyone into "follows advice" (true) and
        // "doesn't" (false), and average 100-for-dead / 0-for-alive in each
        // group. That average is the death rate as a percentage.
        Map<Boolean, Double> deathRate = Arrays.stream(healthyPeople)
                .collect(Collectors.partitioningBy(Healthy::followsAdvice,
                         Collectors.averagingInt(person -> person.isDead() ? 100 : 0)));

        // Our own generic count() still works, now with a lambda as the Check.
        int headingToCare = count(healthyPeople,
                                  person -> person.needsTreatment() && person.followsAdvice());

        // Dark box behind the text so dots passing underneath don't hide it.
        bg.setColor(new Color(0, 0, 0, 180));
        bg.fillRect(4, 5, 720, 40);

        bg.setColor(Color.white);
        bg.drawString("Healthy: "         + byStatus.getOrDefault("Healthy", 0L)
                    + "    Infected: "     + byStatus.getOrDefault("Infected", 0L)
                    + "    In treatment: " + byStatus.getOrDefault("In treatment", 0L)
                    + "    Recovered: "    + byStatus.getOrDefault("Recovered", 0L)
                    + "    Dead: "         + byStatus.getOrDefault("Dead", 0L), 10, 20);

        bg.drawString("Peak infected at once: " + stats.getPeakInfected()
                    + "    Heading to a centre: " + headingToCare
                    + "    Death rate - follows advice (ringed): " + Math.round(deathRate.get(true)) + "%"
                    + "  vs  ignores advice: " + Math.round(deathRate.get(false)) + "%", 10, 38);
    }

    // Builds one random person, decides if they follow advice, and signs up
    // the observers.
    private static Healthy makePerson(int i, MovementStrategy goToCare,
                                      StatusObserver stats, StatusObserver logger) {

        int age = 18 + (int)(Math.random() * 50);
        Healthy person = new Healthy("P" + (i + 1), age, randomX(), randomY());

        if (Math.random() * 100 < ADVICE_PERCENT) {
            person.setInfectedMovement(goToCare);
        }

        person.addObserver(stats);
        person.addObserver(logger);
        return person;
    }

    private static int randomX() {
        return (int)(Math.random() * MAX_X);
    }

    private static int randomY() {
        return (int)(Math.random() * MAX_Y);
    }

    // Both dots are DOT_SIZE wide, so their centres are each offset by the same
    // amount and the offsets cancel out -- the raw corner difference works.
    // Touching means centres are within one full DOT_SIZE (radius 5 + radius 5).
    // Compares squared distances so there is no Math.sqrt call per pair.
    private static boolean isTouching(Healthy person, Virus virus) {

        int dx = person.getXPos() - virus.getXPos();
        int dy = person.getYPos() - virus.getYPos();

        return (dx * dx + dy * dy) <= (DOT_SIZE * DOT_SIZE);
    }

    // Generic method: counts how many items in an array pass a check.
    // check::test turns our own Check into the Predicate that filter() wants.
    private static <T> int count(T[] items, Check<T> check) {
        return (int) Arrays.stream(items).filter(check::test).count();
    }
}
