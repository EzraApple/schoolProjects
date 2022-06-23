package enigma;
import java.util.ArrayList;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Ezra Apple
 */
class Permutation {

    /** alphabet variable. */
    private Alphabet _alphabet;

    /** Arraylist for cycles. */
    private ArrayList<String> _cycles;

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        _cycles = new ArrayList<>();
        for (String s : cycles.split("[\\(\\)\s]")) {
            if (!s.equals("")) {
                _cycles.add(s);
            }
        }
    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {
        _cycles.add(cycle);
    }

    /** Return the value of P modulo the size of this permutation. */

    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    final int wrap(int p, int size) {
        int r = p % size;
        if (r < 0) {
            r += size;
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        int pMod = wrap(p);
        return _alphabet.toInt(permute(_alphabet.toChar(pMod)));
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        int cMod = wrap(c);
        return _alphabet.toInt(invert(_alphabet.toChar(cMod)));
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        if (!_alphabet.contains(p)) {
            throw new EnigmaException("yo this stuff broken foo");
        }
        for (String s : _cycles) {
            char[] cList = s.toCharArray();
            for (int i = 0; i < cList.length; i++) {
                if (cList[i] == p) {
                    return cList[wrap(i + 1, cList.length)];
                }
            }
        }
        return p;
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        if (!_alphabet.contains(c)) {
            throw new EnigmaException("yo this stuff broken foo");
        }
        for (String s : _cycles) {
            char[] cList = s.toCharArray();
            for (int i = 0; i < cList.length; i++) {
                if (cList[i] == c) {
                    return cList[wrap(i - 1, cList.length)];
                }
            }
        }
        return c;
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        int length = 0;
        for (String s : _cycles) {
            length += s.length();
        }
        return length == _alphabet.size();
    }
}
