package gitlet;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Ezra Apple
 */
public class Main {
    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        Repository repo = new Repository();
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }
        switch (args[0]) {
        case "init":
            mainInit(repo, args);
            break;
        case "add":
            mainAdd(repo, args);
            break;
        case "commit":
            mainCommit(repo, args);
            break;
        case "rm":
            mainRm(repo, args);
            break;
        case "log":
            mainLog(repo, args);
            break;
        case "global-log":
            mainGlog(repo, args);
            break;
        case "find":
            mainFind(repo, args);
            break;
        case "status":
            mainStatus(repo, args);
            break;
        case "checkout":
            mainCheckout(repo, args);
            break;
        case "branch":
            mainBranch(repo, args);
            break;
        case "rm-branch":
            mainRmb(repo, args);
            break;
        case "reset":
            mainReset(repo, args);
            break;
        case "merge":
            mainMerge(repo, args);
            break;

        case "shrek":
            mainShrek(repo, args);
            break;
        default:
            System.out.println("No command with that name exists.");
        }
        System.exit(0);
    }

    /**
     * Does stuff.
     * @param r Repository
     * @param args Main args
     */
    private static void mainInit(Repository r, String[] args) {
        if (validNumArgs(args, 0)) {
            r.init();
        } else if (!validNumArgs(args, 0)) {
            System.out.println("Incorrect operands.");
        }
    }

    /**
     * Does stuff.
     * @param r Repository
     * @param args Main args
     */
    private static void mainAdd(Repository r, String[] args) {
        if (validNumArgs(args, 1)) {
            r.add(args[1]);
        } else if (!validNumArgs(args, 1)) {
            System.out.println("Incorrect operands.");
        }
    }

    /**
     * Does stuff.
     * @param r Repository
     * @param args Main args
     */
    private static void mainCommit(Repository r, String[] args) {
        if (validNumArgs(args, 1)) {
            r.commit(args[1], false);
        } else if (!validNumArgs(args, 1)) {
            System.out.println("Incorrect operands.");
        }
    }

    /**
     * Does stuff.
     * @param r Repository
     * @param args Main args
     */
    private static void mainRm(Repository r, String[] args) {
        if (validNumArgs(args, 1)) {
            r.rm(args[1]);
        } else if (!validNumArgs(args, 1)) {
            System.out.println("Incorrect operands.");
        }
    }

    /**
     * Does stuff.
     * @param r Repository
     * @param args Main args
     */
    private static void mainLog(Repository r, String[] args) {
        if (validNumArgs(args, 0)) {
            r.log();
        } else if (!validNumArgs(args, 0)) {
            System.out.println("Incorrect operands.");
        }
    }

    /**
     * Does stuff.
     * @param r Repository
     * @param args Main args
     */
    private static void mainGlog(Repository r, String[] args) {
        if (validNumArgs(args, 0)) {
            r.globalLog();
        } else if (!validNumArgs(args, 0)) {
            System.out.println("Incorrect operands.");
        }
    }

    /**
     * Does stuff.
     * @param r Repository
     * @param args Main args
     */
    private static void mainShrek(Repository r, String[] args) {
        if (validNumArgs(args, 0)) {
            r.shrek();
        } else if (!validNumArgs(args, 0)) {
            System.out.println("Incorrect operands.");
        }
    }

    /**
     * Does stuff.
     * @param r Repository
     * @param args Main args
     */
    private static void mainFind(Repository r, String[] args) {
        if (validNumArgs(args, 1)) {
            r.find(args[1]);
        } else if (!validNumArgs(args, 1)) {
            System.out.println("Incorrect operands.");
        }
    }

    /**
     * Does stuff.
     * @param r Repository
     * @param args Main args
     */
    private static void mainCheckout(Repository r, String[] args) {
        if (args.length == 3 && args[1].equals("--")) {
            r.checkout1(args[2]);
        } else if (args.length == 4 && args[2].equals("--")) {
            r.checkout2(args[1], args[3]);
        } else if (args.length == 2) {
            r.checkout3(args[1]);
        } else {
            System.out.println("Incorrect operands.");
        }
    }

    /**
     * Does stuff.
     * @param r Repository
     * @param args Main args
     */
    private static void mainStatus(Repository r, String[] args) {
        if (validNumArgs(args, 0)) {
            r.status();
        } else if (!validNumArgs(args, 0)) {
            System.out.println("Incorrect operands.");
        }
    }

    /**
     * Does stuff.
     * @param r Repository
     * @param args Main args
     */
    private static void mainBranch(Repository r, String[] args) {
        if (validNumArgs(args, 1)) {
            r.branch(args[1]);
        } else if (!validNumArgs(args, 1)) {
            System.out.println("Incorrect operands.");
        }
    }

    /**
     * Does stuff.
     * @param r Repository
     * @param args Main args
     */
    private static void mainRmb(Repository r, String[] args) {
        if (validNumArgs(args, 1)) {
            r.rmBranch(args[1]);
        } else if (!validNumArgs(args, 1)) {
            System.out.println("Incorrect operands.");
        }
    }

    /**
     * Does stuff.
     * @param r Repository
     * @param args Main args
     */
    private static void mainReset(Repository r, String[] args) {
        if (validNumArgs(args, 1)) {
            r.reset(args[1]);
        } else if (!validNumArgs(args, 1)) {
            System.out.println("Incorrect operands.");
        }
    }

    /**
     * Does stuff.
     * @param r Repository
     * @param args Main args
     */
    private static void mainMerge(Repository r, String[] args) {
        if (validNumArgs(args, 1)) {
            r.merge(args[1]);
        } else if (!validNumArgs(args, 1)) {
            System.out.println("Incorrect operands.");
        }
    }

    /**
     * Returns whether num args is valid for given N.
     * @param args Main input.
     * @param n number of args.
     * @return true if valid number of args, false otherwise.
     */
    public static boolean validNumArgs(String[] args, int n) {
        return args.length == n + 1;
    }
}
