package gitlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class containing files which were added to staging area.
 * Contains functionality to track files staged for removal or addition
 * as well as clear.
 *  @author Ezra Apple
 */

public class StagingArea implements Serializable {

    /**
     * Hashmap containing fileName --> fileContents.
     */
    private HashMap<String, String> toAdd;
    /**
     *  Arraylist of fileNames to remove.
     */
    private ArrayList<String> toRemove;

    public StagingArea() {
        toAdd = new HashMap<>();
        toRemove = new ArrayList<>();
    }

    public void clear() {
        toAdd = new HashMap<>();
        toRemove = new ArrayList<>();
    }

    public void add(String fileName, String fileContents) {
        toAdd.put(fileName, fileContents);
    }

    public void remove(String fileName) {
        toRemove.add(fileName);
    }

    public HashMap<String, String> getToAdd() {
        return toAdd;
    }

    public ArrayList<String> getToRemove() {
        return toRemove;
    }
}
