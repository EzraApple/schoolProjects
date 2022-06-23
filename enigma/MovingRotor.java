package enigma;


import static enigma.EnigmaException.*;

/** Class that represents a rotating rotor in the enigma machine.
 *  @author Ezra Apple
 */
class MovingRotor extends Rotor {

    /** A rotor named NAME whose permutation in its default setting is
     *  PERM, and whose notches are at the positions indicated in NOTCHES.
     *  The Rotor is initally in its 0 setting (first character of its
     *  alphabet).
     */
    private String _notches;

    MovingRotor(String name, Permutation perm, String notches) {
        super(name, perm);
        _notches = notches;
        changeRotation(true);
    }

    @Override
    void advance() {
        int next = this.permutation().wrap(this.setting() + 1);
        this.set(next);
    }

    @Override
    String notches() {
        return _notches;
    }

    @Override
    public String toString() {
        return "Moving Rotor " + this.name();
    }

}
