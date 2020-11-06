package gitlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/** Class that refers to a commit object.
 * @author Pranav Bhimani */
public class Commit implements Serializable {
    /** Message of Commit. */
    private String _message;

    /** Parent commit of this commit. */
    private String _parent;

    /** second parent if merge commit. */
    private String _parent2;

    /** Date formatter for logs. */
    private SimpleDateFormat dateMaker =
            new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");

    /** Date of commit. */
    private String timestamp;

    /** Unique sha-1 ID of commit. */
    private String iD;

    /** All files tracked by this commit. */
    protected HashMap<String, String> trackedFiles;

    /** Initial year of 1970. */
    private static final int INITYEAR = 70;

    /** Initial month of January. */
    private static final int INITMONTH = Calendar.JANUARY;

    /** Initial day of year. */
    private static final int INITDAY = 1;

    /** For initial timestamp of 0 hours, min, and seconds. */
    private static final int INITDEFAULT = 0;

    /** Constructor for non-initial commits with either
     * one or two parents if is a merge commit.
     * @param message inputted message of commit.
     * @param  parent inputted first parent of commit.
     * @param inputFiles inputted tracked files of commit.
     * @param parent2 inputted merge parent of commit.*/
    public Commit(String message, String parent,
                  HashMap<String, String> inputFiles, String parent2) {
        _message = message;
        _parent = parent;
        _parent2 = parent2;
        timestamp = dateMaker.format(new Date());
        trackedFiles = inputFiles;
        byte[] first = Utils.serialize(this);
        String sha1Commit = Utils.sha1(first);
        iD = sha1Commit;
    }

    /** Constructor for initial commits. */
    @SuppressWarnings("deprecation")
    public Commit() {
        _message = "initial commit";
        _parent = null;
        _parent2 = null;
        timestamp = dateMaker.format(new Date(INITYEAR, INITMONTH, INITDAY,
                INITDEFAULT, INITDEFAULT, INITDEFAULT));
        trackedFiles = new HashMap<String, String>();
        byte[] first = Utils.serialize(this);
        String sha1init = Utils.sha1(first);
        iD = sha1init;
    }

    /** Unique ID getter method.
     * @return iD the unique sha-1 ID.*/

    public String myId() {
        return iD;
    }
    /** Parent commit getter method.
     * @return _parent the parent of this commit.*/
    public String getparent() {
        return _parent;
    }
    /** Message getter method.
     * @return _message the message of this commit.*/
    public String getmessage() {
        return _message;
    }

    /** Second parent commit getter method.
     * @return _parent2 the second parent.*/
    public String getparent2() {
        return _parent2;
    }

    /** Developing the merge ID if this is
     * a merge commit.
     * @return the value of parent sha1 combined for merge ID.*/
    private String getMergeID() {
        return _parent.substring(0, 7) + " " + _parent2.substring(0, 7);
    }

    /** Printing the unique log message for this commit. */
    public void printLogMessage() {
        System.out.println("===");
        System.out.println("commit " + iD);
        if (_parent2 != null) {
            System.out.println("Merge: " + getMergeID());
        }
        System.out.println("Date: " + timestamp);
        System.out.println(_message);
        System.out.println();
    }
}
