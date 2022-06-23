package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;

import static enigma.TestUtils.*;

/** The suite of all JUnit tests for the Permutation class.
 *  @author
 */
public class PermutationTest {

    /**
     * For this lab, you must use this to get a new Permutation,
     * the equivalent to:
     * new Permutation(cycles, alphabet)
     * @return a Permutation with cycles as its cycles and alphabet as
     * its alphabet
     * @see Permutation for description of the Permutation conctructor
     */
    Permutation getNewPermutation(String cycles, Alphabet alphabet) {
        Permutation p = new Permutation(cycles, alphabet);
        return p;
    }

    /**
     * For this lab, you must use this to get a new Alphabet,
     * the equivalent to:
     * new Alphabet(chars)
     * @return an Alphabet with chars as its characters
     * @see Alphabet for description of the Alphabet constructor
     */
    Alphabet getNewAlphabet(String chars) {
        Alphabet a = new Alphabet(chars);
        return a;
    }

    /**
     * For this lab, you must use this to get a new Alphabet,
     * the equivalent to:
     * new Alphabet()
     * @return a default Alphabet with characters ABCD...Z
     * @see Alphabet for description of the Alphabet constructor
     */
    Alphabet getNewAlphabet() {
        return new Alphabet();
    }

    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /* ***** TESTING UTILITIES ***** */

    private Permutation perm;
    private String alpha = UPPER_STRING;

    /** Check that perm has an alphabet whose size is that of
     *  FROMALPHA and TOALPHA and that maps each character of
     *  FROMALPHA to the corresponding character of FROMALPHA, and
     *  vice-versa. TESTID is used in error messages. */
    private void checkPerm(String testId, String fromAlpha, String toAlpha) {
        int N = fromAlpha.length();
        assertEquals(testId + " (wrong length)", N, perm.size());
        for (int i = 0; i < N; i += 1) {
            char c = fromAlpha.charAt(i), e = toAlpha.charAt(i);
            assertEquals(msg(testId, "wrong translation of '%c'", c),
                         e, perm.permute(c));
            assertEquals(msg(testId, "wrong inverse of '%c'", e),
                         c, perm.invert(e));
            int ci = alpha.indexOf(c), ei = alpha.indexOf(e);
            assertEquals(msg(testId, "wrong translation of %d", ci),
                         ei, perm.permute(ci));
            assertEquals(msg(testId, "wrong inverse of %d", ei),
                         ci, perm.invert(ei));
        }
    }

    /* ***** TESTS ***** */

    @Test
    public void checkIdTransform() {
        perm = getNewPermutation("", UPPER);
        checkPerm("identity", UPPER_STRING, UPPER_STRING);
    }

    @Test(expected = EnigmaException.class)
    public void testNotInAlphabet() {
        Permutation p = getNewPermutation("(BACD)", getNewAlphabet("ABCD"));
        p.invert('F');
    }

    @Test(expected = EnigmaException.class)
    public void testNotInInvert() {
        Alphabet alph = getNewAlphabet("ETHANISLM");
        Permutation p1 = getNewPermutation("(SAMLTI)", alph);

        p1.invert('Z');
    }

    @Test(expected = EnigmaException.class)
    public void testNotInPermute() {
        Alphabet alph = getNewAlphabet("ETHANISLM");
        Permutation p1 = getNewPermutation("(SAMLTI)", alph);

        p1.permute('Z');
    }

    @Test
    public void testSize() {
        Alphabet alph = getNewAlphabet("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        Permutation p1 = getNewPermutation("(ZRCSW)", alph);
        assertEquals(26, p1.size());

        Alphabet alpha2 = getNewAlphabet("ABCDE");
        Permutation p2 = getNewPermutation("(ABC)", alpha2);
        assertEquals(5, p2.size());
    }

    @Test
    public void testPermute() {
        Alphabet alph = getNewAlphabet("ETHANISLM");
        Permutation p1 = getNewPermutation("(SAMLTI)", alph);

        assertEquals('A', p1.permute('S'));
        assertEquals('S', p1.permute('I'));
        assertEquals('M', p1.permute('A'));
        assertEquals('L', p1.permute('M'));
        assertEquals('T', p1.permute('L'));
        assertEquals('I', p1.permute('T'));

        assertEquals(6, p1.permute(5));
        assertEquals(3, p1.permute(6));
        assertEquals(8, p1.permute(3));
        assertEquals(7, p1.permute(8));
        assertEquals(1, p1.permute(7));
        assertEquals(5, p1.permute(1));

        assertEquals('E', p1.permute('E'));
        assertEquals(0, p1.permute(0));
        assertEquals('H', p1.permute('H'));
        assertEquals(2, p1.permute(2));
    }

    @Test
    public void testInvert() {
        Alphabet alph = getNewAlphabet("ETHANISLM");
        Permutation p1 = getNewPermutation("(SAMLTI)", alph);

        assertEquals('S', p1.invert('A'));
        assertEquals('I', p1.invert('S'));
        assertEquals('A', p1.invert('M'));
        assertEquals('M', p1.invert('L'));
        assertEquals('L', p1.invert('T'));
        assertEquals('T', p1.invert('I'));

        assertEquals(5, p1.invert(6));
        assertEquals(6, p1.invert(3));
        assertEquals(3, p1.invert(8));
        assertEquals(8, p1.invert(7));
        assertEquals(7, p1.invert(1));
        assertEquals(1, p1.invert(5));

        assertEquals('E', p1.invert('E'));
        assertEquals(0, p1.invert(0));
        assertEquals('H', p1.invert('H'));
        assertEquals(2, p1.invert(2));
    }

    @Test
    public void testAlphabet() {
        Alphabet alph = getNewAlphabet("ETHANISLM");
        Permutation p1 = getNewPermutation("(EHNSAM)", alph);
        assertEquals(alph, p1.alphabet());

        assertEquals(9, alph.size());

        assertEquals(true, alph.contains('E'));
        assertEquals(0, alph.toInt('E'));
        assertEquals('E', alph.toChar(0));

        assertEquals(false, alph.contains('Z'));
        assertEquals(5, alph.toInt('I'));
        assertEquals('A', alph.toChar(3));


    }

    @Test
    public void testDerangment() {
        Alphabet alph = getNewAlphabet("ETHANISLM");

        Permutation p1 = getNewPermutation("(THENIALMS)", alph);
        assertEquals(true, p1.derangement());

        Permutation p2 = getNewPermutation("(ETHAN)", alph);
        assertEquals(false, p2.derangement());

        Alphabet alph2 = getNewAlphabet("ZYXWVUT");

        Permutation p3 = getNewPermutation("(TUV)", alph2);
        assertEquals(true, p1.derangement());

        Permutation p4 = getNewPermutation("(ZYX)", alph2);
        assertEquals(false, p2.derangement());
    }


}
