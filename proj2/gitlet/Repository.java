package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.*;
import java.util.Map;

import static gitlet.Utils.*;

// TODO: any imports you need here

/**
 * Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 * @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     * <p>
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */


    private static class Pointers implements Serializable {
        String HEAD;
        Map<String, String> Branches;
    }

    /**
     * Represents the saved contents of files.
     */
    private static class Blob implements Serializable {
        String id;
        String fileName;
        byte[] contents;

        public Blob(Path file) {
            this.fileName = file.getFileName().toString();
            this.contents = readContents(file);
            this.id = Utils.sha1(fileName, contents);
        }
    }

    // public static final String DELIMITER = "/";

    /**
     * The current working directory.
     */
    public static final String CWD = System.getProperty("user.dir");

    /**
     * The .gitlet and associated directory.
     */
    public static final String GITLET_DIR = String.join(DELIMITER, CWD, ".gitlet");

    /**
     * Directory contains files associated to staging for addition and removal
     */
    public static final String STAGE_DIR = String.join(DELIMITER, GITLET_DIR, "StagingArea");

    public static final String ADDITION_DIR = String.join(DELIMITER, STAGE_DIR, "addition");

    public static final String REMOVAL_FILE = String.join(DELIMITER, STAGE_DIR, "addition");

    /**
     * Directory contains things about repository, like Blobs, Commits
     */
    public static final String REPO_DIR = String.join(DELIMITER, GITLET_DIR, "Repository");

    /**
     * Directory contains Blob files
     */
    public static final String BLOBS_DIR = String.join(DELIMITER, REPO_DIR, "Blobs");

    /**
     * Directory contains Commits
     */
    public static final String COMMITS_DIR = String.join(DELIMITER, REPO_DIR, "Commits");

    public static final String POINTER_FILE = String.join(DELIMITER, COMMITS_DIR, "Pointers");


    /* TODO: fill in the rest of this class. */


    /* Related to command `gitlet init` */

    /**
     * Create a .gitlet directory if there is not, and relative directories and files
     */
    static void gitletInit() throws IOException {
        try {
            Files.createDirectories(Paths.get(GITLET_DIR));
            Files.createDirectories(Paths.get(STAGE_DIR));
            Files.createDirectories(Paths.get(COMMITS_DIR));
            Files.createDirectories(Paths.get(BLOBS_DIR));
            Files.createDirectories(Paths.get(ADDITION_DIR));

            Commit initCmt = Commit.initCommit();
            _commit(initCmt);

            Files.createFile(Paths.get(POINTER_FILE));
            createBranch("master");

            // TODO initialize these files
            Files.createFile(Paths.get(REMOVAL_FILE));
        } catch (FileAlreadyExistsException e) {
            exitsWithMessage("A Gitlet version-control system already exists in the current directory.");
        }
    }

    /* Related to `gitlet add` command */

    /**
     * Add `file` content (Make it a 'Blob') into Staging Area.
     * If `file` does not exist, do nothing and exit with 0;
     * If `file` exists and:
     * 1. is identical to the file in HEAD commit with same name, do nothing or delete former file added before.
     * 2. is not identical to HEAD, copy the file content into a Blob object,
     * serialize it into a file which carries the same name with the `file` in Staging Area
     */
    static void add(Path file) throws IOException {
        if (!checkFileExist(file)) {
            exitsWithMessage("File does not exist.");
        }
        Commit headCommit = getHEADCommit();
        String commitedFileID = headCommit.getFileIDByFileName(file.getFileName().toString());
        Blob fileInWorkingArea = new Blob(file);

        if (fileInWorkingArea.id.equals(commitedFileID)) {
            if (checkFileExist(STAGE_DIR, fileInWorkingArea.fileName))
                removeFromStage(fileInWorkingArea.fileName, false);
            // TODO The file will no longer be staged for removal (see gitlet rm),
            //  if it was at the time of the command.
        } else {
            writeObject(Paths.get(STAGE_DIR, fileInWorkingArea.fileName).toFile(), fileInWorkingArea);
        }
    }

    static void commit(String message) throws IOException {
        Commit commit = Commit.createCommitAsChildOf(getHEADCommit(), message);
        // TODO Update according to Staging Area, Removal
        _commit(commit);
    }

    /** Save a commit into a file in repository */
    private static void _commit(Commit commit) throws IOException {
        Path commitPath = Files.createFile(Paths.get(COMMITS_DIR, commit.getId()));
        writeObject(commitPath.toFile(), commit);
    }

    static void remove(String... fileName) {

    }

    static void checkGitletInit(boolean checkExistence) {
        boolean existence = Utils.checkDirExist(Repository.GITLET_DIR);
        if (checkExistence && !existence) {
            exitsWithMessage("Not in an initialized Gitlet directory.");
        } else if (!checkExistence && existence) {
            exitsWithMessage("A Gitlet version-control system already exists in the current directory.");
        }
    }

    static void createBranch(String name) {
        Pointers ptr;
        File ptrFile = Paths.get(REMOVAL_FILE).toFile();
        if (name.equals("master")) { // init commit
            ptr = new Pointers();
            ptr.HEAD = getHEADCommit().getId();
        } else
            ptr = readObject(ptrFile, Pointers.class);
        ptr.Branches.put(name, ptr.HEAD);
        writeObject(ptrFile, ptr);
    }

    /**
     * Remove a file from Staging Area, putting it into Removal according to `toRemoval`
     *
     * @param fileName  file name of one of files in Staging Area
     * @param toRemoval if to move the removed file to Removal
     */
    private static void removeFromStage(String fileName, boolean toRemoval) throws IOException {
        if (toRemoval) Files.writeString(Paths.get(REMOVAL_FILE),
                fileName,
                StandardOpenOption.APPEND);
        Files.delete(Paths.get(STAGE_DIR, fileName));
    }

    private static Pointers getPointers() {
        File ptrFile = Paths.get(REMOVAL_FILE).toFile();
        return readObject(ptrFile, Pointers.class);
    }

    /** Get the commit the HEAD pointer pointed to */
    private static Commit getHEADCommit() {
        String HEAD = getPointers().HEAD;
        return readObject(Paths.get(COMMITS_DIR, HEAD).toFile(), Commit.class);
    }

}
