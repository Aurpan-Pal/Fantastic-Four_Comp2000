import java.awt.*;
import java.awt.event.*;

public class PanelDemo {

    // Size of the drawing area, and how big each dot is.
    private static final int SIM_WIDTH  = 1200;
    private static final int SIM_HEIGHT = 800;
    private static final int DOT_SIZE   = 10;

    private static final int HEALTHY_COUNT = 70;
    private static final int VIRUS_COUNT   = 10;

    // How often the timer fires, in milliseconds. 50ms = 20 ticks a second.
    // Treatment time also counts down by this much every tick.
    private static final int TICK_MS = 50;

    // ---------- checks used by count() ----------
    // Each one answers a yes/no question about one person. The type in <...>
    // says what kind of item it checks.

    private static final Check<Healthy> INFECTED = new Check<Healthy>() {
        @Override
        public boolean test(Healthy person) {
            return person.isInfected();
        }
    };

    private static final Check<Healthy> IN_TREATMENT = new Check<Healthy>() {
        @Override
        public boolean test(Healthy person) {
            return person.isInTreatment();
        }
    };

    private static final Check<Healthy> RECOVERED = new Check<Healthy>() {
        @Override
        public boolean test(Healthy person) {
            return person.isRecovered();
        }
    };

    private static final Check<Healthy> DEAD = new Check<Healthy>() {
        @Override
        public boolean test(Healthy person) {
            return person.isDead();
        }
    };

    public static void main(String[] args) {

        // ---------- build the healthy people ----------
        // Still Healthy[], not Person[]. Nothing ever gets swapped out of this
        // array -- a person who catches the virus is the SAME object with a
        // flag flipped, so the array type never has to change.
        final Healthy[] healthyPeople = new Healthy[HEALTHY_COUNT];

        for (int i = 0; i < healthyPeople.length; i++) {

            int x = (int)(Math.random() * (SIM_WIDTH  - DOT_SIZE));
            int y = (int)(Math.random() * (SIM_HEIGHT - DOT_SIZE));
            int age = 18 + (int)(Math.random() * 50);

            healthyPeople[i] = new Healthy("P" + (i + 1), age, x, y);
        }

        // ---------- build the viruses ----------
        final Virus[] viruses = new Virus[VIRUS_COUNT];

        for (int i = 0; i < viruses.length; i++) {

            int x = (int)(Math.random() * (SIM_WIDTH  - DOT_SIZE));
            int y = (int)(Math.random() * (SIM_HEIGHT - DOT_SIZE));
            int percentage = (int)(Math.random() * 101);

            viruses[i] = new Virus("Zombie Virus", percentage, x, y);
        }

        // ---------- build the recovery centres ----------
        // The array type is the interface, so one array holds both kinds.
        // Positions are fixed so the centres never overlap each other.
        final RecoveryCentre[] centres = new RecoveryCentre[5];

        centres[0] = new Hospital(250, 200);
        centres[1] = new Hospital(900, 550);
        centres[2] = new MedicalCamp(150, 620);
        centres[3] = new MedicalCamp(600, 380);
        centres[4] = new MedicalCamp(1000, 150);

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
                for (int i = 0; i < centres.length; i++) {
                    centres[i].draw(bg);
                }

                // Each person is asked what state it is in, and gets the
                // matching colour.
                for (int i = 0; i < healthyPeople.length; i++) {

                    if (healthyPeople[i].isDead()) {
                        bg.setColor(Color.gray);
                    } else if (healthyPeople[i].isRecovered()) {
                        bg.setColor(Color.cyan);
                    } else if (healthyPeople[i].isInfected()) {
                        bg.setColor(Color.yellow);
                    } else {
                        bg.setColor(Color.green);
                    }

                    bg.fillOval(healthyPeople[i].getXPos(),
                                healthyPeople[i].getYPos(),
                                DOT_SIZE, DOT_SIZE);
                }

                bg.setColor(Color.red);
                for (int i = 0; i < viruses.length; i++) {
                    bg.fillOval(viruses[i].getXPos(),
                                viruses[i].getYPos(),
                                DOT_SIZE, DOT_SIZE);
                }

                // Live counts in the top-left corner, all from the one
                // generic count() method.
                int infected    = count(healthyPeople, INFECTED);
                int inTreatment = count(healthyPeople, IN_TREATMENT);
                int recovered   = count(healthyPeople, RECOVERED);
                int dead        = count(healthyPeople, DEAD);
                int healthy     = healthyPeople.length - infected - recovered - dead;

                bg.setColor(Color.white);
                bg.drawString("Healthy: " + healthy
                        + "    Infected: " + infected
                        + " (in treatment: " + inTreatment + ")"
                        + "    Recovered: " + recovered
                        + "    Dead: " + dead, 10, 20);

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
        // Fires every TICK_MS. Each fire moves every object one random step
        // (or counts down treatment), checks for contact and admissions,
        // then asks the panel to redraw.
        javax.swing.Timer timer = new javax.swing.Timer(TICK_MS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                for (int i = 0; i < healthyPeople.length; i++) {

                    if (healthyPeople[i].isInTreatment()) {
                        // Patients stay put inside the centre until they are cured.
                        healthyPeople[i].updateTreatment(TICK_MS);
                    } else if (!healthyPeople[i].isDead()) {
                        // The speed comes from the person, not from a number typed
                        // here, so an infected person slows down on its own.
                        healthyPeople[i].moveRandom(healthyPeople[i].getSpeed(), SIM_WIDTH - DOT_SIZE, SIM_HEIGHT - DOT_SIZE);
                    }
                    // The dead stay where they fell.
                }

                for (int i = 0; i < viruses.length; i++) {
                    viruses[i].moveRandom(2, SIM_WIDTH - DOT_SIZE, SIM_HEIGHT - DOT_SIZE);
                }

                checkInfections(healthyPeople, viruses);
                checkAdmissions(healthyPeople, centres);

                panel.repaint();
            }
        });

        timer.start();
    }

    // Every living person against every virus. What a new contact does is
    // decided by the person (see Healthy.infect()): a healthy person gets
    // infected, and an infected person hit a second time dies.
    private static void checkInfections(Healthy[] healthyPeople, Virus[] viruses) {

        for (int i = 0; i < healthyPeople.length; i++) {

            if (healthyPeople[i].isDead()) {
                continue;
            }

            boolean touching = false;

            for (int j = 0; j < viruses.length; j++) {
                if (isTouching(healthyPeople[i], viruses[j])) {
                    touching = true;
                    break;
                }
            }

            if (healthyPeople[i].isNewContact(touching)) {
                try {
                    healthyPeople[i].infect();
                } catch (PersonDiedException ex) {
                    // A second hit while already infected is fatal.
                    healthyPeople[i].die();
                    System.out.println(ex.getMessage());
                }
            }
        }
    }

    // Any infected person who walks into a recovery centre gets admitted there.
    // The centre decides how long treatment takes, so this method never needs
    // to know whether it is a hospital or a medical camp.
    private static void checkAdmissions(Healthy[] healthyPeople, RecoveryCentre[] centres) {

        for (int i = 0; i < healthyPeople.length; i++) {

            if (!healthyPeople[i].needsTreatment()) {
                continue;
            }

            // Use the middle of the dot, so "inside" means the dot is really in.
            int midX = healthyPeople[i].getXPos() + DOT_SIZE / 2;
            int midY = healthyPeople[i].getYPos() + DOT_SIZE / 2;

            for (int j = 0; j < centres.length; j++) {

                if (centres[j].contains(midX, midY)) {
                    centres[j].admit(healthyPeople[i]);
                    break;
                }
            }
        }
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
    // <T> is filled in by Java from the arguments, so it works for any type:
    // here it's Healthy[] with a Check<Healthy>.
    private static <T> int count(T[] items, Check<T> check) {

        int total = 0;

        for (int i = 0; i < items.length; i++) {
            if (check.test(items[i])) {
                total++;
            }
        }
        return total;
    }
}