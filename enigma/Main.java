package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import ucb.util.CommandArgs;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Ezra Apple
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            CommandArgs options =
                new CommandArgs("--verbose --=(.*){1,3}", args);
            if (!options.ok()) {
                throw error("Usage: java enigma.Main [--verbose] "
                            + "[INPUT [OUTPUT]]");
            }

            _verbose = options.contains("--verbose");
            new Main(options.get("--")).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Open the necessary files for non-option arguments ARGS (see comment
      *  on main). */
    Main(List<String> args) {
        _config = getInput(args.get(0));

        if (args.size() > 1) {
            _input = getInput(args.get(1));
        } else {
            _input = new Scanner(System.in);
        }

        if (args.size() > 2) {
            _output = getOutput(args.get(2));
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;

    /** True if --verbose specified. */
    private static boolean _verbose;

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        Machine M = readConfig();
        int setUpCount = 0;
        while (_input.hasNext("\\*")) {
            String settings = _input.nextLine();
            if (settings.equals("")) {
                settings = _input.nextLine();
                _output.println();
            }
            setUp(M, settings);
            setUpCount++;
            while (_input.hasNextLine() && !_input.hasNext("[\\n\\s]*\\*")) {
                String msg = _input.nextLine();
                msg = msg.replaceAll("\\s", "");
                printMessageLine(msg, M);
            }
        }
        if (setUpCount == 0) {
            throw error("No setting line");
        }
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            _alphabet = new Alphabet(_config.next());
            int numRotors = _config.nextInt();
            int pawls = _config.nextInt();

            ArrayList<Rotor> allRotors = new ArrayList<>();
            while (_config.hasNext()) {
                allRotors.add(readRotor());
            }
            return new Machine(_alphabet, numRotors, pawls, allRotors);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {
            String name = _config.next("[^()\\s]+");
            String typeNotch = _config.next();
            String cycles = "";
            while (_config.hasNext("\\([^*\\s]+\\)")) {
                cycles += _config.next();
            }
            Permutation permutation = new Permutation(cycles, _alphabet);
            Rotor r = null;
            if (typeNotch.charAt(0) == 'M') {
                String notches = "";
                char[] tNotches = typeNotch.toCharArray();
                for (int i = 1; i < tNotches.length; i++) {
                    notches += tNotches[i];
                }
                r = new MovingRotor(name, permutation, notches);
            } else if (typeNotch.charAt(0) == 'N') {
                if (typeNotch.length() > 1) {
                    throw error("Main : notch descr. when unneeded");
                }
                r = new Rotor(name, permutation);
            } else if (typeNotch.charAt(0) == 'R') {
                if (typeNotch.length() > 1) {
                    throw error("Main : notch descr. when unneeded");
                }
                r = new Reflector(name, permutation);
            }
            if (r == null) {
                throw error("Main : Incorrect rotor config");
            }
            return r;
        } catch (NoSuchElementException excp) {
            throw error("Main : bad rotor description");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
        String set = "";
        String[] insert = new String[M.numRotors()];
        String cycles = "";

        String[] settingsArr = settings.split("\\s+");
        int index = 0;
        for (String s : settingsArr) {
            if (index > 0 && index <= M.numRotors()) {
                insert[index - 1] = s;
            } else if (index == M.numRotors() + 1) {
                set = s;
            } else if (index > M.numRotors() + 1) {
                if (s.contains("(") || s.contains("(")) {
                    cycles += s;
                } else {
                    throw error("Wrong number of rotors");
                }
            }
            index++;
        }
        checkSetting(set, M);
        Permutation plugboard = new Permutation(cycles, _alphabet);
        M.insertRotors(insert);
        M.setRotors(set);
        M.setPlugboard(plugboard);
    }

    void checkSetting(String set, Machine M) {
        int length = set.length();
        int correct = M.numRotors() - 1;
        if (length < correct) {
            throw error("Setting too short");
        }
        if (length > correct) {
            throw error("Setting too long");
        }
        char[] setChars = set.toCharArray();
        for (char c : setChars) {
            if (!_alphabet.contains(c)) {
                throw error("Invalid characters in setting");
            }
        }
    }

    /** Return true iff verbose option specified. */
    static boolean verbose() {
        return _verbose;
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). Taking in MSG and machine M */
    private void printMessageLine(String msg, Machine M) {
        msg = M.convert(msg);
        String toStream = "";
        for (int i = 1; i <= msg.length(); i++) {
            toStream += msg.charAt(i - 1);
            if (i % 5 == 0) {
                toStream += " ";
            }
        }
        _output.println(toStream);
    }

}
