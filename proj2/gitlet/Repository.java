package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.*;
import java.util.List;

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


    private static class CommitTree implements Serializable {
        String HEAD;
        List<String> Branches;
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

    public static final String DELIMITER = "/";

    /** The current working directory. */
    public static final String CWD = System.getProperty("user.dir");

    /** The .gitlet and associated directory. */
    public static final String GITLET_DIR = String.join(DELIMITER, CWD, ".gitlet");

    /** Directory contains staged files for addition */
    public static final String STAGE_PATH = String.join(DELIMITER, GITLET_DIR, "StagingArea");

    /** Directory contains Blob files */
    public static final String BLOB_PATH = String.join(DELIMITER, GITLET_DIR, "Blobs");

    public static final String COMMIT_PATH = String.join(DELIMITER, GITLET_DIR, "Commits");

    public static final String COMMIT_TREE_PATH = String.join(DELIMITER, COMMIT_PATH, "CommitTree");

    public static final String REMOVAL_PATH = String.join(DELIMITER, GITLET_DIR, "Removal");

    /* TODO: fill in the rest of this class. */


    /* Related to command `gitlet init` */

    /**
     * TODO
     * */
    static void gitletInit() throws IOException {
        Path gitlet = Paths.get(GITLET_DIR);
        Path stage = Paths.get(STAGE_PATH);
        Path commits = Paths.get(COMMIT_PATH);
        Path commitTree = Paths.get(COMMIT_TREE_PATH);
        Path blobs = Paths.get(BLOB_PATH);
        Path removal = Paths.get(REMOVAL_PATH);
        try {
            // Create .gitlet, and relative files
            Files.createDirectories(gitlet);
            Files.createDirectories(stage);
            Files.createDirectories(commits);
            Files.createDirectories(blobs);

            commit("initial commit");
            createBranch("master");

            // TODO initialize files like commitTree
            Files.createFile(commitTree);
            Files.createFile(removal);

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
    static void add(Path file) {
        if (!checkFileExist(file)) {
            exitsWithMessage("File does not exist.");
        }
        Commit headCommit = Commit.getHEADCommit();
        String fileInHEADID = headCommit.getFileIDByFileName(file.getFileName().toString());
        Blob fileInWorkingArea = new Blob(file);

        if (fileInWorkingArea.id.equals(fileInHEADID)) {
            if (checkFileExist(STAGE_PATH, fileInWorkingArea.fileName))
                remove(STAGE_PATH, fileInWorkingArea.fileName);
            // TODO The file will no longer be staged for removal (see gitlet rm),
            //  if it was at the time of the command.
        } else {
            writeObject(new File(STAGE_PATH, fileInWorkingArea.fileName), fileInWorkingArea);
        }
    }

    static void commit(String message) {
        // TODO
        Commit commit = Commit.createNewCommit(message);
        // commit.saveToFile();
    }

    static void _commit(String message) {
        Commit commit = Commit.createNewCommit(message);

    }

    static void remove(String... fileName) {

    }

    static void checkGitletInit(boolean checkExistence) {
        boolean existence = Utils.checkDirExist(Repository.GITLET_DIR);
        if (checkExistence) {
            if (!existence) exitsWithMessage("Not in an initialized Gitlet directory.");
        } else {
            if (existence) exitsWithMessage("A Gitlet version-control system" +
                    " already exists in the current directory.");
        }
    }

    static void createBranch(String name) {
        // TODO: make a pointer pointing to HEAD commit
        //       and save it to POINTER file.
    }


}
