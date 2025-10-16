package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.HashMap;

/**
 * Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
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

    /** The message of this Commit. */
    private String message;

    /** Created time for this commit. */
    private long timestamp;

    /** SHA-1 hash code for Commit Object, through `message`, `Timestamp` TODO */
    private String id;

    /* MAPPINGS */

    /** The parent commit id of this commit */
    private String parentId;

    /** The another parent commit id in merging of this commit, is null if the commit was created by merging */
    private String mergedParentID;

    /** a mapping of file names to blob references, Key is file names, and Value is Blob id */
    private HashMap<String, String> fileMap;


    public String getFileIDByFileName(String name) {
        return fileMap.getOrDefault(name, "");
    }

    public void saveToFile() throws IOException {
        File commitFile = Files.createFile(Paths.get(Repository.COMMIT_PATH, this.id)).toFile();
        Utils.writeObject(commitFile, this);
    }

    /* TODO: fill in the rest of this class. */
    /* Get the commit the HEAD pointer pointed to */
    public static Commit getHEADCommit() {
        // TODO
        return new Commit();
    }

    /**
     * Creating a new commit.
     * A new Commit tracks the same files with the current HEAD commit by default
     * this method should change meta-data, and check Staging Area to find anything to change in tracked files
     * */
    public static Commit createNewCommit(String message) {
        Commit newCommit = getHEADCommit();

        // Update meta-data
        newCommit.message = message;
        newCommit.timestamp = new Timestamp(System.currentTimeMillis()).getTime();

        newCommit.parentId = newCommit.id;
        newCommit.id = Utils.sha1(newCommit.message, newCommit.timestamp);

        // TODO Check Staging Area, update the file mapping
        // TODO Check Removal
        // TODO Store commit to Repository in Commits Area

        return newCommit;
    }

    /**
     * A commit that contains no files and has the commit message "initial commit",
     * commited when a repository initiates.
     * */
    public static void initCommit() throws IOException {
        Commit initCommit = new Commit();

        initCommit.message = "initial commit";
        initCommit.parentId = null;
        initCommit.timestamp = new Timestamp(0).getTime();

        initCommit.saveToFile();
        //TODO
    }
}

