package enigma;

import java.util.HashMap;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;


/** The suite of all JUnit tests for the Machine class.
 *  @author Ezra Apple
 */
public class MachineTest {

    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /* ***** TESTS ***** */

    private static final Alphabet AZ = new Alphabet(TestUtils.UPPER_STRING);

    private static final HashMap<String, Rotor> ROTORS = new HashMap<>();

    static {
        HashMap<String, String> nav = TestUtils.NAVALA;
        ROTORS.put("B", new Reflector("B", new Permutation(nav.get("B"), AZ)));
        ROTORS.put("Beta",
                new FixedRotor("Beta",
                        new Permutation(nav.get("Beta"), AZ)));
        ROTORS.put("III",
                new MovingRotor("III",
                        new Permutation(nav.get("III"), AZ), "V"));
        ROTORS.put("IV",
                new MovingRotor("IV", new Permutation(nav.get("IV"), AZ),
                        "J"));
        ROTORS.put("I",
                new MovingRotor("I", new Permutation(nav.get("I"), AZ),
                        "Q"));
    }

    private static final String[] ROTORS1 = { "B", "Beta", "III", "IV", "I" };
    private static final String SETTING1 = "AXLE";

    private Machine mach1() {
        Machine mach = new Machine(AZ, 5, 3, ROTORS.values());
        mach.insertRotors(ROTORS1);
        mach.setRotors(SETTING1);
        return mach;
    }

    @Test
    public void testInsertRotors() {
        Machine mach = new Machine(AZ, 5, 3, ROTORS.values());
        mach.insertRotors(ROTORS1);
        assertEquals(5, mach.numRotors());
        assertEquals(3, mach.numPawls());
        assertEquals(AZ, mach.alphabet());
        assertEquals(ROTORS.get("B"), mach.getRotor(0));
        assertEquals(ROTORS.get("Beta"), mach.getRotor(1));
        assertEquals(ROTORS.get("III"), mach.getRotor(2));
        assertEquals(ROTORS.get("IV"), mach.getRotor(3));
        assertEquals(ROTORS.get("I"), mach.getRotor(4));
    }

    @Test
    public void testConvertChar() {
        Machine mach = mach1();
        mach.setPlugboard(new Permutation("(YF) (HZ)", AZ));
        assertEquals(25, mach.convert(24));
    }

    @Test
    public void testConvertMsg() {
        Machine mach = mach1();
        mach.setPlugboard(new Permutation("(HQ) (EX) (IP) (TR) (BY)", AZ));
        assertEquals("QVPQSOKOILPUBKJZPISFXDW",
                mach.convert("FROMHISSHOULDERHIAWATHA"));
    }

    @Test
    public void testAdvanceRotors() {
        Machine mach = new Machine(AZ, 5, 3, ROTORS.values());
        mach.insertRotors(ROTORS1);
        mach.setRotors(SETTING1);

        assertEquals('A', mach.alphabet().toChar(mach.getRotor(1).setting()));
        assertEquals('X', mach.alphabet().toChar(mach.getRotor(2).setting()));
        assertEquals('L', mach.alphabet().toChar(mach.getRotor(3).setting()));
        assertEquals('E', mach.alphabet().toChar(mach.getRotor(4).setting()));

        mach.advanceRotors();

        assertEquals('A', mach.alphabet().toChar(mach.getRotor(1).setting()));
        assertEquals('X', mach.alphabet().toChar(mach.getRotor(2).setting()));
        assertEquals('L', mach.alphabet().toChar(mach.getRotor(3).setting()));
        assertEquals('F', mach.alphabet().toChar(mach.getRotor(4).setting()));

        for (int i = 0; i < 11; i++) {
            mach.advanceRotors();
        }

        assertEquals('A', mach.alphabet().toChar(mach.getRotor(1).setting()));
        assertEquals('X', mach.alphabet().toChar(mach.getRotor(2).setting()));
        assertEquals('Q', mach.alphabet().toChar(mach.getRotor(4).setting()));

        for (int i = 0; i < 11; i++) {
            mach.advanceRotors();
        }

        assertEquals('A', mach.alphabet().toChar(mach.getRotor(1).setting()));
        assertEquals('X', mach.alphabet().toChar(mach.getRotor(2).setting()));
        assertEquals('M', mach.alphabet().toChar(mach.getRotor(3).setting()));
        assertEquals('B', mach.alphabet().toChar(mach.getRotor(4).setting()));

        mach.setRotors("ATIP");

        assertEquals('A', mach.alphabet().toChar(mach.getRotor(1).setting()));
        assertEquals('T', mach.alphabet().toChar(mach.getRotor(2).setting()));
        assertEquals('I', mach.alphabet().toChar(mach.getRotor(3).setting()));
        assertEquals('P', mach.alphabet().toChar(mach.getRotor(4).setting()));

        mach.advanceRotors();

        assertEquals('A', mach.alphabet().toChar(mach.getRotor(1).setting()));
        assertEquals('Q', mach.alphabet().toChar(mach.getRotor(4).setting()));

        mach.setRotors("AUJC");

        mach.advanceRotors();

        assertEquals('A', mach.alphabet().toChar(mach.getRotor(1).setting()));
        assertEquals('V', mach.alphabet().toChar(mach.getRotor(2).setting()));
        assertEquals('K', mach.alphabet().toChar(mach.getRotor(3).setting()));
        assertEquals('D', mach.alphabet().toChar(mach.getRotor(4).setting()));

    }
}
