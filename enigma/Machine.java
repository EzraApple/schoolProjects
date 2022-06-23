package enigma;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author Ezra Apple
 */
class Machine {

    /** integers to track rotors and pawls. */
    private int _numRotors, _pawls;

    /** Arraylist to hold all rotors, and those inserted. */
    private ArrayList<Rotor> _allRotorArray, _machineRotors;

    /** Permutation for the plugboard. */
    private Permutation _plugboard;

    /** Alphabet for the machine. */
    private final Alphabet _alphabet;

    /**Hashmap for rotors name/values for insertion. */
    private HashMap<String, Rotor> _nameRotorMap;

    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _numRotors = numRotors;
        _pawls = pawls;

        _allRotorArray = new ArrayList<Rotor>(allRotors);
        _nameRotorMap = new HashMap<>();
        for (Rotor r : allRotors) {
            _nameRotorMap.put(r.name(), r);
        }
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _pawls;
    }

    /** Return Rotor #K, where Rotor #0 is the reflector, and Rotor
     *  #(numRotors()-1) is the fast Rotor.  Modifying this Rotor has
     *  undefined results. */
    Rotor getRotor(int k) {
        return _machineRotors.get(k);
    }

    Alphabet alphabet() {
        return _alphabet;
    }

    ArrayList<Rotor> getMachineRotors() {
        return _machineRotors;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        _machineRotors = new ArrayList<>();
        for (String name : rotors) {
            Rotor newRotor = _nameRotorMap.get(name);
            _machineRotors.add(newRotor);
        }
        if (!checkFollowsSettings()) {
            throw error("Wrong number of moving rotors");
        }
    }

    boolean checkFollowsSettings() {
        boolean follows = true;
        int numMoving = 0;
        for (Rotor r : _machineRotors) {
            if (r instanceof MovingRotor) {
                numMoving++;
            }
        }
        if (numMoving != numPawls()) {
            follows = false;
        }
        return follows;
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        char[] settingArr = setting.toCharArray();

        for (int i = 1; i < _machineRotors.size(); i++) {
            char cposn = settingArr[i - 1];
            _machineRotors.get(i).set(cposn);
        }
    }

    /** Return the current plugboard's permutation. */
    Permutation plugboard() {
        return _plugboard;
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        advanceRotors();
        if (Main.verbose()) {
            System.err.printf("[");
            for (int r = 1; r < numRotors(); r += 1) {
                System.err.printf("%c",
                        alphabet().toChar(getRotor(r).setting()));
            }
            System.err.printf("] %c -> ", alphabet().toChar(c));
        }
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c -> ", alphabet().toChar(c));
        }
        c = applyRotors(c);
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c%n", alphabet().toChar(c));
        }
        return c;
    }

    boolean atNotch(int i) {
        return _machineRotors.get(i).atNotch();
    }
    /** Advance all rotors to their next position. */
    void advanceRotors() {
        ArrayList<Integer> toMove = new ArrayList<>();
        for (int i = numRotors() - 1; i >= (numRotors() - numPawls()); i--) {
            if (i == numRotors() - 1) {
                if (!toMove.contains(i)) {
                    toMove.add(i);
                }
            } else if (_machineRotors.get(i + 1).atNotch()) {
                if (!toMove.contains(i)) {
                    toMove.add(i);
                }
                if (!toMove.contains(i + 1)) {
                    toMove.add(i + 1);
                }
            }
        }
        for (int index : toMove) {
            _machineRotors.get(index).advance();
        }
    }


    /** Return the result of applying the rotors to the character C (as an
     *  index in the range 0..alphabet size - 1). */
    private int applyRotors(int c) {
        for (int i = _machineRotors.size() - 1; i > 0; i--) {
            c = _machineRotors.get(i).convertForward(c);
        }
        c = _machineRotors.get(0).permutation().permute(c);
        for (int i = 1; i < _machineRotors.size(); i++) {
            c = _machineRotors.get(i).convertBackward(c);
        }
        return c;
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        String result = "";
        char[] chars = msg.toCharArray();
        for (char c : chars) {
            int alphaIndex1 = _alphabet.toInt(c);
            int alphaIndex2 = convert(alphaIndex1);
            char charResult = _alphabet.toChar(alphaIndex2);
            result += charResult;
        }

        return result;
    }

}
