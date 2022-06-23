package gitlet;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import static gitlet.Utils.serialize;
import static gitlet.Utils.sha1;

/** Class representing a commit.
 * Will contain info for parent/child commits.
 * Contains file information snapshot for all tracked files
 *  @author Ezra Apple
 */
public class Commit implements Serializable {

    /**
     * String for the date and time.
     */
    private String dateTime;
    /**
     * String for has of parent commit.
     */
    private String parent;
    /**
     * String for has of merge parent commit.
     */
    private String mergeParent;
    /**
     * String for hash of self.
     */
    private String self;
    /**
     * String for commit log message.
     */
    private String msg;
    /**
     * String for map of blobs fileName --> fileContents.
     */
    private HashMap<String, String> blobMap;


    public Commit(HashMap<String, String> blobs,
                  String prevCommit, String message) {
        mergeParent = null;
        parent = prevCommit;
        msg = message;
        blobMap = blobs;
        LocalDateTime now = LocalDateTime.now();
        String format = "EEE MMM dd HH:mm:ss yyyy";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        dateTime = formatter.format(now) + " -0800";
        if (parent == null) {
            dateTime = "Wed Dec 31 04:00:00 1969 -0800";
        }
        self = sha1(serialize(this));
    }

    public Commit(HashMap<String, String> blobs,
                  String prevCommit, String message, String mParent) {
        mergeParent = mParent;
        parent = prevCommit;
        msg = message;
        blobMap = blobs;
        LocalDateTime now = LocalDateTime.now();
        String format = "EEE MMM dd HH:mm:ss yyyy";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        dateTime = formatter.format(now) + " -0800";
        if (parent == null) {
            dateTime = "Wed Dec 31 04:00:00 1969 -0800";
        }
        self = sha1(serialize(this));
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getParentHash() {
        return parent;
    }

    public String getHash() {
        return self;
    }

    public String getMsg() {
        return msg;
    }

    public HashMap<String, String> getBlobMap() {
        return blobMap;
    }

    public String getMergeParent() {
        return mergeParent;
    }

    public String getLogMsg() {
        String start = "===\n";
        String id = "commit " + getHash() + "\n";
        String date = "Date: " + dateTime + "\n";
        String message = msg + "\n";
        String end = "\n";
        return start + id + date + message + end;
    }
}
