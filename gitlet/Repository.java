package gitlet;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.Collections;
import java.util.List;

import static gitlet.Utils.readObject;
import static gitlet.Utils.writeObject;
import static gitlet.Utils.writeContents;
import static gitlet.Utils.readContentsAsString;
import static gitlet.Utils.restrictedDelete;
import static gitlet.Utils.plainFilenamesIn;
import static gitlet.Utils.join;

/** Class containing functionality for serializing/running a repository.
 * Should store:
 * -commit history
 * -staging area info
 * -repository info
 * -git commands
 *  @author Ezra Apple
 */

public class Repository {

    /**
     * Points to head branch.
     */
    private String _HEAD = "master";
    /**
     * Path to file storing this info.
     */
    private final String headPath = ".gitlet/HEAD.txt";

    /**
     * STaging area object.
     */
    private StagingArea stage;
    /**
     * Path to stage object.
     */
    private final String stagePath = ".gitlet/stage/currentStage.txt";
    /**
     * CWD File.
     */
    static final File CWD  = new File(System.getProperty("user.dir"));

    /**
     * Path to .gitlet/.
     */
    private final String gitletPath;


    public Repository() {
        gitletPath = String.valueOf(join(CWD, ".gitlet"));

        if (new File(headPath).exists()) {
            _HEAD = readContentsAsString(headPath);
        }

        if (new File(stagePath).exists()) {
            stage = readObject(stagePath, StagingArea.class);
        }
    }

    /**
     * If repository doesn't exist, creates repository.
     * Makes directories for stage, branches, commits
     *  **/
    public void init() {
        File gitlet = new File(gitletPath);

        if (gitlet.exists()) {
            System.out.println("A Gitlet version-control"
                    + " system already exists in the current directory");
        } else {
            gitlet.mkdirs();

            new File(gitletPath + "/stage").mkdirs();

            new File(gitletPath + "/branches").mkdirs();

            new File(gitletPath + "/commits").mkdirs();

            new File(gitletPath + "/blobs").mkdirs();


            stage = new StagingArea();
            File currentStage = new File(stagePath);
            saveStage();

            Commit initCommit = new Commit(new HashMap<String, String>(),
                    null, "initial commit");
            writeObject(new File(gitletPath + "/commits/"
                    + initCommit.getHash() + ".txt"), initCommit);

            writeContents(new File(gitletPath
                    + "/branches/master.txt"), initCommit.getHash());

            writeContents(new File(gitletPath + "/HEAD.txt"), "master");
        }
    }

    /**
     * adds a file to the staging area, not if no change.
     * @param fileName name of file.
     *  **/
    public void add(String fileName) {
        gitletVerify();
        File toAdd = new File(CWD + "/" + fileName);
        if (!toAdd.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        String contents = readContentsAsString(toAdd);
        if (contents != null) {
            if (contents.equals(getHEAD().getBlobMap().get(fileName))) {
                stage.getToAdd().remove(fileName);
            } else {
                stage.add(fileName, contents);
            }
        }

        if (stage.getToRemove().contains(fileName)) {
            stage.getToRemove().remove(fileName);
        }
        saveStage();
    }

    /**
     * Takes files in staging area and removes those.
     * staged for removal and adds those staged for addition.
     * Updates head pointer and clears staging area as well.
     * @param msg commit message.
     * @param isMerge whether it is a merge or not
     *  **/
    public void commit(String msg, boolean isMerge) {
        gitletVerify();
        if (stage.getToAdd().isEmpty() && stage.getToRemove().isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }
        if (msg.equals("")) {
            System.out.println("Please enter a commit message.");
            return;
        }
        Commit currentCommit = getHEAD();
        HashMap<String, String> a = currentCommit.getBlobMap();
        HashMap<String, String> currentBlobs = copyHash(a);

        for (String filename : stage.getToAdd().keySet()) {
            currentBlobs.put(filename, stage.getToAdd().get(filename));
        }
        for (String filename : stage.getToRemove()) {
            currentBlobs.remove(filename);
        }
        String curr = currentCommit.getHash();
        Commit newCommit = new Commit(currentBlobs, curr, msg);

        writeContents(new File(gitletPath + "/branches/"
                + _HEAD + ".txt"), newCommit.getHash());

        writeObject(new File(gitletPath + "/commits/"
                + newCommit.getHash() + ".txt"), newCommit);

        for (String fileName : newCommit.getBlobMap().keySet()) {
            String blobName = newCommit.getBlobMap().get(fileName);
            writeContents(new File(gitletPath + "/blobs/" + fileName),
                    blobName);
        }

        stage.clear();
        saveStage();
    }

    /**
     * Unstages file if staged for addition.
     * If it is tracked in current commit.
     * stage for removal and delete from CWD.
     * Prints message if neither.
     * @param fileName name of file
     *  **/
    public void rm(String fileName) {
        gitletVerify();
        boolean isStaged = stage.getToAdd().containsKey(fileName);
        boolean isTracked = getHEAD().getBlobMap().containsKey(fileName);
        if (!isStaged && !isTracked) {
            System.out.println("No reason to remove the file.");
            return;
        }
        if (isStaged) {
            stage.getToAdd().remove(fileName);
        }
        if (isTracked) {
            stage.remove(fileName);
            restrictedDelete(fileName);
        }
        saveStage();
    }

    /**
     * Goes back from current commit printing log messages.
     *  **/
    public void log() {
        gitletVerify();
        Commit currentCommit = getHEAD();
        ArrayList<String> logs = new ArrayList<>();
        while (currentCommit != null) {
            logs.add(currentCommit.getLogMsg());
            if (currentCommit.getParentHash() != null) {
                Commit nextCommit = readObject(".gitlet/commits/"
                        + currentCommit.getParentHash()
                        + ".txt", Commit.class);
                currentCommit = nextCommit;
            } else {
                currentCommit = null;
            }
        }
        for (String logMsg : logs) {
            System.out.print(logMsg);
        }

    }

    /**
     * Prints the log message of all commits.
     *  **/
    public void globalLog() {
        gitletVerify();
        ArrayList<String> commits = getCommits();
        for (String commit : commits) {
            Commit commit1 = readObject(gitletPath
                    + "/commits/" + commit, Commit.class);
            System.out.println(commit1.getLogMsg());
        }
    }

    /**
     * prints out all the ID's of commits.
     * with a given commit message.
     * @param msg message
     *  **/
    public void find(String msg) {
        gitletVerify();
        ArrayList<String> commits = getCommits();
        int count = 0;
        for (String commit : commits) {
            File finding = new File(gitletPath
                    + "/commits/" + commit);
            if (finding.exists()) {
                Commit comm = readObject(finding, Commit.class);
                if (comm.getMsg().equals(msg)) {
                    System.out.println(comm.getHash());
                    count++;
                }
            }
        }
        if (count == 0) {
            System.out.println("Found no commit with that message.");
        }
    }

    /**
     * prints out branches, staged files, removed files.
     * has an asterisk next to current branch.
     *  **/
    public void status() {
        gitletVerify();
        String branchHeader = "=== Branches ===\n";
        String addedHeader = "=== Staged Files ===\n";
        String removedHeader = "=== Removed Files ===\n";
        String modsHeader = "=== Modifications Not Staged For Commit ===\n\n";
        String untrackedHeader = "=== Untracked Files ===\n";
        ArrayList<String> branches = new ArrayList<>();
        String branchNames = "";
        for (String branch : getBranches()) {
            branches.add(branch.substring(0, branch.length() - 4));
        }
        branches.remove(_HEAD);
        branches.add("*" + _HEAD);
        Collections.sort(branches);
        for (String branch : branches) {
            branchNames += branch + "\n";
        }
        branchNames += "\n";

        String added = "";
        TreeMap<String, String> toAdd = new TreeMap<>(stage.getToAdd());
        for (String key : toAdd.keySet()) {
            added += key + "\n";
        }
        added += "\n";

        String removed = "";
        ArrayList<String> removedList = stage.getToRemove();
        Collections.sort(removedList);
        for (String name : removedList) {
            removed += name + "\n";
        }
        removed += "\n";

        String combined = branchHeader + branchNames
                + addedHeader + added
                + removedHeader + removed
                + modsHeader + untrackedHeader;
        System.out.println(combined);
    }

    /**
     * Checks out an individual file.
     * @param fileName file to be checked out.
     */
    public void checkout1(String fileName) {
        gitletVerify();
        if (!getHEAD().getBlobMap().containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        String commitContents = getHEAD().getBlobMap().get(fileName);
        File destination = new File(CWD + "/" + fileName);
        writeContents(destination, commitContents);
    }

    /**
     * Checks out a file in a certain commit.
     * @param commitID sha1 ID of the commit.
     * @param fileName name of file to check out.
     */
    public void checkout2(String commitID, String fileName) {
        gitletVerify();
        if (!new File(".gitlet/commits/"
                + findCommit(commitID) + ".txt").exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit checkoutCommit = readObject(".gitlet/commits/"
                + findCommit(commitID)  + ".txt", Commit.class);
        if (!checkoutCommit.getBlobMap().containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        String commitContents = checkoutCommit.getBlobMap().get(fileName);
        File destination = new File(CWD + "/" + fileName);
        writeContents(destination, commitContents);
    }

    /**
     * Checks out an entire branch.
     * @param branchName Name of branch to check out.
     */
    public void checkout3(String branchName) {
        gitletVerify();
        if (!new File(gitletPath + "/branches/"
                + branchName + ".txt").exists()) {
            System.out.println("No such branch exists.");
            return;
        }
        String commitID = readContentsAsString(gitletPath
                + "/branches/"
                + branchName + ".txt");
        Commit checkoutCommit = readObject(".gitlet/commits/"
                + findCommit(commitID)  + ".txt", Commit.class);

        for (String fileCheckout : checkoutCommit.getBlobMap().keySet()) {
            String f = fileCheckout;
            if (plainFilenamesIn(CWD).contains(f)) {
                if (!getHEAD().getBlobMap().containsKey(fileCheckout)) {
                    System.out.println("There is an "
                            + "untracked file in the way; "
                            + "delete it, or add and commit it first.");
                    return;
                }
            }
        }
        if (branchName.equals(_HEAD)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        Commit head = getHEAD();
        for (String file : checkoutCommit.getBlobMap().keySet()) {
            if (!head.getBlobMap().containsKey(file)) {
                String contents = checkoutCommit.getBlobMap().get(file);
                File destination = new File(CWD + "/" + file);
                writeContents(destination, contents);
            }
        }
        for (String file : head.getBlobMap().keySet()) {
            if (!checkoutCommit.getBlobMap().containsKey(file)) {
                restrictedDelete(new File(CWD + "/" + file));
            } else {
                String contents = checkoutCommit.getBlobMap().get(file);
                File destination = new File(CWD + "/" + file);
                writeContents(destination, contents);
            }
        }
        _HEAD = branchName;
        saveHead();
        stage.clear();
        saveStage();
    }

    /**
     *
     * @param branchName name of branch to create.
     */
    public void branch(String branchName) {
        gitletVerify();
        ArrayList<String> branches = getBranches();
        if (branches.contains(branchName + ".txt")) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        writeContents(new File(gitletPath + "/branches/"
                + branchName + ".txt"), getHEAD().getHash());
    }

    /**
     * removes a branch.
     * @param branchName name of branch to be removed.
     */
    public void rmBranch(String branchName) {
        gitletVerify();
        ArrayList<String> branches = getBranches();
        if (!branches.contains(branchName + ".txt")) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (branchName.equals(_HEAD)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        File branchToDelete = new File(gitletPath
                + "/branches/" + branchName + ".txt");
        branchToDelete.delete();
    }

    /**
     * checks out an entire commit.
     * @param commitID commitid
     *  **/
    public void reset(String commitID) {
        gitletVerify();
        if (!new File(".gitlet/commits/"
                + findCommit(commitID) + ".txt").exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit head = getHEAD();
        Commit resetCommit = readObject(gitletPath + "/commits/"
                + findCommit(commitID) + ".txt", Commit.class);
        for (String fileCheckout : resetCommit.getBlobMap().keySet()) {
            if (plainFilenamesIn(CWD).contains(fileCheckout)) {
                String f = fileCheckout;
                if (!getHEAD().getBlobMap().containsKey(f)) {
                    System.out.println("There is "
                            + "an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                    return;
                }
            }
        }

        for (String file : resetCommit.getBlobMap().keySet()) {
            if (!head.getBlobMap().containsKey(file)) {
                String contents = resetCommit.getBlobMap().get(file);
                File destination = new File(CWD + "/" + file);
                writeContents(destination, contents);
            }
        }

        for (String file : head.getBlobMap().keySet()) {
            if (!resetCommit.getBlobMap().containsKey(file)) {
                restrictedDelete(new File(CWD + "/" + file));
            } else {
                String contents = resetCommit.getBlobMap().get(file);
                File destination = new File(CWD + "/" + file);
                writeContents(destination, contents);
            }
        }

        writeContents(new File(gitletPath + "/branches/"
                + _HEAD + ".txt"), resetCommit.getHash());
        stage.clear();
        saveStage();
    }

    /**
     * Merges files from the given branch into the current branch.
     * @param branchName branch to merge from.
     */
    public void merge(String branchName) {
        mergeInitialErrors(branchName);
        Commit otherCommit = branchToCommit(branchName);
        Set<String> otherFiles = otherCommit.getBlobMap().keySet();
        Commit headCommit = getHEAD();
        Set<String> headFiles = headCommit.getBlobMap().keySet();
        Commit splitCommit = splitPoint(otherCommit);
        Set<String> splitFiles = splitCommit.getBlobMap().keySet();
        boolean isConflict = false;
        HashSet<String> allFiles = new HashSet<>();
        allFiles.addAll(otherFiles);
        allFiles.addAll(headFiles);
        allFiles.addAll(splitFiles);
        for (String fileName : allFiles) {
            String splitFile = splitCommit.getBlobMap().get(fileName);
            if (splitFile == null) {
                splitFile = "";
            }
            String otherFile = otherCommit.getBlobMap().get(fileName);
            if (otherFile == null) {
                otherFile = "";
            }
            String headFile = headCommit.getBlobMap().get(fileName);
            if (headFile == null) {
                headFile = "";
            }
            if (!splitFiles.contains(fileName)) {
                if (otherFiles.contains(fileName)
                        && !headFiles.contains(fileName)) {
                    mergeCheckout(otherCommit, fileName, otherFile);
                }
            } else {
                if (otherFiles.contains(fileName)
                        && headFiles.contains(fileName)) {
                    if (!otherFile.equals(splitFile)
                            && headFile.equals(splitFile)) {
                        mergeCheckout(otherCommit, fileName, otherFile);
                    }
                    if (!otherFile.equals(splitFile)
                            && !headFile.equals(splitFile)) {
                        if (!otherFile.equals(headFile)) {
                            isConflict = true;
                            String mergeContents = "<<<<<<< HEAD\n"
                                    + headFile + "=======\n"
                                    + otherFile + ">>>>>>>";
                            stage.add(fileName, mergeContents);
                        }
                    }
                } else {
                    if (!otherFiles.contains(fileName)
                            && headFile.equals(splitFile)) {
                        rm(fileName);
                    }
                }
            }
        }
        finishMerge(branchName, isConflict);
    }

    public void finishMerge(String branchName, boolean isConflict) {
        saveStage();
        String msg = "Merged " + branchName + " into " + _HEAD + ".";
        commit(msg, true);
        if (isConflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }


    /**
     * Checks out and stages the file from the given commit.
     * @param commit commit to check out from.
     * @param fileName name of file to check out.
     * @param fileContents contents of file to add to staging area.
     */
    public void mergeCheckout(Commit commit,
                              String fileName, String fileContents) {
        stage.add(fileName, fileContents);
        checkout2(commit.getHash(), fileName);
    }

    /**
     * Finds the split point between the head branch and given branch.
     * @param merge given branch to examine.
     * @return split point between two.
     */
    public Commit splitPoint(Commit merge) {
        Commit branch = merge;
        Commit head = getHEAD();
        Commit splitPoint = null;

        HashSet<String> headHashes = new HashSet<>();
        while (head != null) {
            headHashes.add(head.getHash());
            head = idToCommit(head.getParentHash());
        }
        while (branch != null) {
            if (headHashes.contains(branch.getHash())) {
                splitPoint = branch;
                break;
            }
            branch = idToCommit(branch.getParentHash());
        }
        return splitPoint;
    }

    /**
     * Runs test for all errors that should be checked before merging.
     * @param branchName name of merge branch.
     */
    public void mergeInitialErrors(String branchName) {
        gitletVerify();
        if (stage.getToAdd().size() > 0 || stage.getToRemove().size() > 0) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        if (!getBranches().contains(branchName + ".txt")) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        Commit mergeCommit = branchToCommit(branchName);
        Commit splitCommit = splitPoint(mergeCommit);
        if (_HEAD.equals(branchName)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        if (mergeCommit.equals(splitCommit)) {
            System.out.println("Given branch is an "
                    + "ancestor of the current branch.");
            System.exit(0);
        }
        for (String fileName : plainFilenamesIn(CWD)) {
            if (!getHEAD().getBlobMap().containsKey(fileName)) {
                System.out.println("There is an untracked file in the way;"
                        + " delete it, or add and commit it first.");
                System.exit(0);
            }
        }
    }

    /**
     * Gets the current head commit.
     * @return _HEAD commit.
     */
    public Commit getHEAD() {
        String headPath1 = ".gitlet/branches/" + _HEAD + ".txt";
        String headHash =  readContentsAsString(new File(headPath1));
        return readObject(".gitlet/commits/" + headHash + ".txt", Commit.class);
    }


    /**
     * prints shrek to terminal.
     */
    public void shrek() {
        System.out.println(
                "⢀⡴⠑⡄⠀⠀⠀⠀⠀⠀⠀⣀⣀⣤⣤⣤⣀⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀ \n"
                        + "⠸⡇⠀⠿⡀⠀⠀⠀⣀⡴⢿⣿⣿⣿⣿⣿⣿⣿⣷⣦⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀ \n"
                        + "⠀⠀⠀⠀⠑⢄⣠⠾⠁⣀⣄⡈⠙⣿⣿⣿⣿⣿⣿⣿⣿⣆⠀⠀⠀⠀⠀⠀⠀⠀ \n"
                        + "⠀⠀⠀⠀⢀⡀⠁⠀⠀⠈⠙⠛⠂⠈⣿⣿⣿⣿⣿⠿⡿⢿⣆⠀⠀⠀⠀⠀⠀⠀ \n"
                        + "⠀⠀⠀⢀⡾⣁⣀⠀⠴⠂⠙⣗⡀⠀⢻⣿⣿⠭⢤⣴⣦⣤⣹⠀⠀⠀⢀⢴⣶⣆ \n"
                        + "⠀⠀⢀⣾⣿⣿⣿⣷⣮⣽⣾⣿⣥⣴⣿⣿⡿⢂⠔⢚⡿⢿⣿⣦⣴⣾⠁⠸⣼⡿ \n"
                        + "⠀⢀⡞⠁⠙⠻⠿⠟⠉⠀⠛⢹⣿⣿⣿⣿⣿⣌⢤⣼⣿⣾⣿⡟⠉⠀⠀⠀⠀⠀ \n"
                        + "⠀⣾⣷⣶⠇⠀⠀⣤⣄⣀⡀⠈⠻⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡇⠀⠀⠀⠀⠀⠀ \n"
                        + "⠀⠉⠈⠉⠀⠀⢦⡈⢻⣿⣿⣿⣶⣶⣶⣶⣤⣽⡹⣿⣿⣿⣿⡇⠀⠀⠀⠀⠀⠀ \n"
                        + "⠀⠀⠀⠀⠀⠀⠀⠉⠲⣽⡻⢿⣿⣿⣿⣿⣿⣿⣷⣜⣿⣿⣿⡇⠀⠀⠀⠀⠀⠀ \n"
                        + "⠀⠀⠀⠀⠀⠀⠀⠀⢸⣿⣿⣷⣶⣮⣭⣽⣿⣿⣿⣿⣿⣿⣿⠀⠀⠀⠀⠀⠀⠀ \n"
                        + "⠀⠀⠀⠀⠀⠀⣀⣀⣈⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠇⠀⠀⠀⠀⠀⠀⠀ \n"
                        + "⠀⠀⠀⠀⠀⠀⢿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠃⠀⠀⠀⠀⠀⠀⠀⠀ \n"
                        + "⠀⠀⠀⠀⠀⠀⠀⠹⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡿⠟⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀ \n"
                        + "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠉⠛⠻⠿⠿⠿⠿⠛⠉");

    }

    /**
     * Writes the current stage object to the stage file.
     */
    public void saveStage() {
        writeObject(new File(stagePath), stage);
    }

    /**
     * Saves the current_HEADvariable to the text file.
     */
    public void saveHead() {
        writeContents(new File(gitletPath + "/HEAD.txt"), _HEAD);
    }

    /**
     * Copies all keys and values in hashMap into new hashMap.
     * @param map hashMap to be copied.
     * @param <T> generic type of keys/values.
     * @return copied hashMap.
     */
    public <T> HashMap<T, T> copyHash(HashMap<T, T> map) {
        HashMap<T, T> returnHash = new HashMap<>();
        for (T key : map.keySet()) {
            returnHash.put(key, map.get(key));
        }
        return returnHash;
    }

    /**
     * Gets all the filenames in a folder ands returns as arraylist.
     * @param dirPath file path of directory.
     * @return Arraylist of file names.
     */
    public ArrayList<String> getFileNames(String dirPath) {
        ArrayList<String> arr = new ArrayList<>();
        File[] files = new File(dirPath).listFiles();
        for (File file : files) {
            arr.add(file.getName());
        }
        return arr;
    }

    /**
     * Gets untracked files in CWD.
     * @return an ArrayList of untracked files in CWD.
     */
    public ArrayList<String> untrackedFiles() {
        List<String> cWDFiles = plainFilenamesIn(CWD);
        ArrayList<String> blobs = getBlobs();
        ArrayList<String> untracked = new ArrayList<>();
        for (String file : cWDFiles) {
            if (!blobs.contains(file)) {
                untracked.add(file);
            }
        }
        return untracked;
    }

    /**
     * Gets an arraylist of commit names.
     * @return ArrayList of commit names.
     */
    public ArrayList<String> getCommits() {
        return getFileNames(gitletPath + "/commits");
    }

    /**
     * Gets an arraylist of branch names.
     * @return ArrayList of branch names.
     */
    public ArrayList<String> getBranches() {
        return getFileNames(gitletPath + "/branches");
    }

    /**
     * Gets an arraylist of blob names.
     * @return ArrayList of blob names.
     */
    public ArrayList<String> getBlobs() {
        return getFileNames(gitletPath + "/blobs");
    }


    /**
     * Find the correct commit for an abbreviated commit.
     * @param abbrevID the given abbreviated commit.
     * @return the full sha1 ID of the commit.
     */
    public String findCommit(String abbrevID) {
        String returnID = "";
        ArrayList<String> commits = getCommits();
        for (String commitID : commits) {
            if (commitID.startsWith(abbrevID)) {
                returnID = commitID.substring(0, commitID.length() - 4);
                break;
            }
        }
        return returnID;
    }

    /**
     * Verifies that a .gitlet directory exists before doing any other code.
     */
    public void gitletVerify() {
        if (!new File(gitletPath).exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    /**
     * Turns a branch name into the commit at the head of the branch.
     * @param branchName Name of branch to get commit from.
     * @return Commit at head of branchName.
     */
    public Commit branchToCommit(String branchName) {
        String commitID = readContentsAsString(gitletPath + "/branches/"
                + branchName + ".txt");
        return readObject(".gitlet/commits/"
                + findCommit(commitID)  + ".txt", Commit.class);
    }

    /**
     * Turns a sha1 ID intor a commit object.
     * returns null if commit doesn't exist.
     * @param sha1 The ID to be converted.
     * @return a commit from that ID.
     */
    public Commit idToCommit(String sha1) {
        File commit = new File(gitletPath + "/commits/" + sha1 + ".txt");
        if (commit.exists()) {
            return readObject(commit, Commit.class);
        } else {
            return null;
        }
    }
}
