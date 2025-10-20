package gitlet;

// TODO: any imports you need here

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Set;

import static gitlet.Utils.*;


/**
 * Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 * <p>
 * A Commit saves a snapshot of tracked files so they can be restored at a later time,
 *
 * @author Louid Lu
 */
public class Commit implements Serializable {

    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /* META DATA */

    /**
     * The message of this Commit.
     */
    private String message;

    /**
     * Created time for this commit.
     */
    private Long timestamp;

    /**
     * SHA-1 hash code for Commit Object, through `message`, `Timestamp` TODO
     */
    private String id;

    /* MAPPINGS */

    /**
     * The parent commit id of this commit
     */
    private String parentId;

    /**
     * The another parent commit id in merging of this commit, is null if the commit was created by merging
     */
    private String mergedParentId;

    /**
     * a mapping of file names to blob references, Key is file names, and Value is Blob id
     */
    private HashMap<String, String> fileMap;



    /* TODO: fill in the rest of this class. */

    /* GETTER AND SETTER */

    public String getFileIDByFileName(String name) {
        return fileMap.get(name);
    }

    public String putInFileMap(String fileName, String BlobId) {
        return fileMap.put(fileName, BlobId);
    }

    public String getId() {
        return id;
    }

    String removeFileMap(String fileName) {
        return fileMap.remove(fileName);
    }

    public String getMessage() {
        return message;
    }

    public String getMergedParentId() {
        return mergedParentId;
    }

    public void setMergedParentId(String mergedParentId) {
        this.mergedParentId = mergedParentId;
    }

    public String getParentId() {
        return parentId;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public Set<String> getAllTrackedFiles() {
        return this.fileMap.keySet();
    }

    /* FACTORY METHODS */

    /**
     * Creating a new commit.
     * A new Commit tracks the same files with the current HEAD commit by default
     * this method should change meta-data, and check Staging Area to find anything to change in tracked files
     */
    public static Commit createCommit(String message) {
        Commit newCommit = new Commit();
        // Update meta-data
        newCommit.message = message;
        newCommit.timestamp = new Timestamp(System.currentTimeMillis()).getTime();
        newCommit.parentId = null;
        newCommit.mergedParentId = null;
        newCommit.id = newCommit.createID();
        newCommit.fileMap = new HashMap<>();
        return newCommit;
    }

    /** Create a Commit as the other Commit's child, where the child is identical to
     *  its parent except its meta-data and parentIds. */
    public static Commit createCommitAsChildOf(Commit parent, String message) {
        Commit newCommit = createCommit(message);
        newCommit.fileMap = parent.fileMap;
        newCommit.parentId = parent.id;
        newCommit.mergedParentId = null;
        return newCommit;
    }

    /**
     * A commit that contains no files and has the commit message "initial commit",
     * commited when a repository initiates.
     */
    public static Commit initCommit() {
        Commit initCommit = new Commit();

        initCommit.message = "initial commit";
        initCommit.timestamp = new Timestamp(0).getTime();
        initCommit.parentId = null;
        initCommit.mergedParentId = null;
        initCommit.fileMap = new HashMap<>();
        initCommit.id = initCommit.createID();
        return initCommit;
    }

    private String createID() {
        return sha1(this.message, this.timestamp.toString());
    }
}

