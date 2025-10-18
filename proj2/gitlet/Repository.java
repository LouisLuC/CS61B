package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.*;
import java.util.*;

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
        Map<String, String> branches;

        Pointers(String HEAD) {
            this.HEAD = HEAD;
            branches = new HashMap<>();
        }
    }

    private static class Removal implements Serializable {
        Set<String> filesToRemove;

        Removal() {
            this.filesToRemove = new HashSet<>();
        }
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

            // Initiate Pointer file
            Path pointerFile = Paths.get(POINTER_FILE);
            Files.createFile(pointerFile);
            Pointers ptr = new Pointers(initCmt.getId());
            writeObject(pointerFile.toFile(), ptr);
            createBranch("master");

            // Initiate Removal file
            Path removalFile = Paths.get(REMOVAL_FILE);
            Files.createFile(removalFile);
            Removal removal = new Removal();
            writeObject(removalFile.toFile(), removal);
        } catch (FileAlreadyExistsException e) {
            exitsWithMessage("A Gitlet version-control system already exists in the current directory.");
        }
    }

    /* Related to `gitlet add` command */

    /**
     * Add `file` content (Make it a 'Blob') into Staging Area.
     * If `file` does not exist, do nothing and exit with 0;
     * If `file` exists and:
     *    1. is identical to the file in HEAD commit with same name, do nothing or delete former file added before.
     *    2. is not identical to HEAD, copy the file content into a Blob object,
     * Serialize it into a file which carries the same name with the `file` in the Staging Area
     */
    static void add(Path file) throws IOException {
        if (!checkFileExist(file)) {
            exitsWithMessage("File does not exist.");
        }
        Commit headCommit = getHEADCommit();
        String commitedFileID = headCommit.getFileIDByFileName(file.getFileName().toString());
        Blob fileInCWD = new Blob(file);

        if (fileInCWD.id.equals(commitedFileID)) {
            if (checkFileExist(ADDITION_DIR, fileInCWD.fileName))
                removeFromStage(fileInCWD.fileName);
        } else {
            writeObject(Paths.get(ADDITION_DIR, fileInCWD.fileName).toFile(), fileInCWD);
        }
        // Try to delete it from removal
        removeFromRemoval(fileInCWD.fileName);
    }


    /* RELATED TO GITLET COMMIT */

    /**
     * Make a Commit with `message`, saving snap of tracked files
     * TODO
     */
    static void commit(String message) throws IOException {
        Commit commit = Commit.createCommitAsChildOf(getHEADCommit(), message);
        List<String> stagedFiles = plainFilenamesIn(ADDITION_DIR);

        // Check
        Removal removal = getRemoval();
        if (stagedFiles.isEmpty() && removal.filesToRemove.isEmpty()) {
            exitsWithMessage("No changes added to the commit.");
        }

        // Add Staged files and clear Staging Area
        for (String fileName : stagedFiles) {
            Blob stagedBlob = getBlobFromStage(fileName);
            commit.putInFileMap(fileName, stagedBlob.id);
            saveBlobToRepo(stagedBlob);
            removeFromStage(fileName);
        }
        // Remove untracked files
        for (String fileName : removal.filesToRemove) {
            commit.removeFileMap(fileName);
        }
        clearRemoval();

        Pointers ptr = getPointers();
        // Move pointer of branch
        for (String key : ptr.branches.keySet()) {
            String ID = ptr.branches.get(key);
            if (ptr.HEAD.equals(ID)) {
                ptr.branches.put(key, commit.getId());
            }
        }
        // Move HEAD pointer
        ptr.HEAD = commit.getId();

        _commit(commit);
    }

    private static void clearRemoval() {
        Removal clearRemoval = new Removal();
        writeObject(Paths.get(REMOVAL_FILE).toFile(), clearRemoval);
    }

    /**
     * Save a commit into a file in repository
     */
    private static void _commit(Commit commit) throws IOException {
        Path commitPath = Files.createFile(Paths.get(COMMITS_DIR, commit.getId()));
        writeObject(commitPath.toFile(), commit);
    }

    /**
     * Get the commit the HEAD pointer pointed to
     */
    private static Commit getHEADCommit() {
        String HEAD = getPointers().HEAD;
        return readObject(Paths.get(COMMITS_DIR, HEAD).toFile(), Commit.class);
    }

    private static Blob getBlobFromStage(String fileName) {
        Path blobPath = Paths.get(ADDITION_DIR, fileName);
        return readObject(blobPath.toFile(), Blob.class);
    }

    private static Blob getBlobFromRepo(String ID) {
        Path blobPath = Paths.get(BLOBS_DIR, ID);
        return readObject(blobPath.toFile(), Blob.class);
    }

    private static void saveBlobToRepo(Blob blob) {
        Path blobPath = Paths.get(BLOBS_DIR, blob.id);
        if (!Files.exists(blobPath))
            writeObject(blobPath.toFile(), blob);
    }

    /* RELATED TO POINTERS AND BRANCHES */

    private static Pointers getPointers() {
        File ptrFile = Paths.get(POINTER_FILE).toFile();
        return readObject(ptrFile, Pointers.class);
    }

    private static void savePointers(Pointers ptr) {
        File ptrFile = Paths.get(POINTER_FILE).toFile();
        writeObject(ptrFile, ptr);
    }

    static void createBranch(String name) {
        Pointers ptr = getPointers();
        ptr.branches.put(name, ptr.HEAD);
        savePointers(ptr);
    }

    /* RELATED TO GITLET REMOVE */

    /**
     * Remove a file in Gitlet Repository.
     * if the file is staged in Staging Area, untrack it
     * if the file is tracked by HEAD, stage it into Removal for committing
     * And if the file exist in CWD, delete it
     * if the file is not tracked and is not in Staging Area, exist with the message
     * */
    private static void remove(Path file) throws IOException {
        Path fileInStage = Paths.get(ADDITION_DIR, file.getFileName().toString());
        String blobId = getHEADCommit().getFileIDByFileName(file.getFileName().toString());
        if (checkFileExist(fileInStage)) {
            Files.delete(fileInStage);
        }
        if (blobId != null) {
            stageToRemoval(file.getFileName().toString());
        }
        if (blobId == null && !checkFileExist(fileInStage)) {
            exitsWithMessage("No reason to remove the file.");
        }
        restrictedDelete(file.toFile());
    }

    /**
     * Deserialize Removal file to get a Removal object.
     * */
    private static Removal getRemoval() {
        Path removal_path = Paths.get(REMOVAL_FILE);
        return readObject(removal_path.toFile(), Removal.class);
    }

    /**
     * Serialize a Removal object into Removal file.
     * */
    private static void saveRemoval(Removal removal) {
        Path removal_path = Paths.get(REMOVAL_FILE);
        writeObject(removal_path.toFile(), removal);
    }


    /**
     * Remove a file from Staging Area
     *
     * @param fileName file name of one of files in Staging Area
     */
    private static void removeFromStage(String fileName) throws IOException {
        Files.delete(Paths.get(STAGE_DIR, fileName));
    }

    private static void stageToRemoval(String fileName) {
        Removal removal = getRemoval();
        removal.filesToRemove.add(fileName);
        saveRemoval(removal);
    }

    private static boolean removeFromRemoval(String fileName) {
        Removal removal = getRemoval();
        boolean isRemoved = removal.filesToRemove.remove(fileName);
        saveRemoval(removal);
        return isRemoved;
    }

    /* UTILITIES FOR MAIN */

    static void checkGitletInit(boolean checkExistence) {
        boolean existence = checkDirExist(Repository.GITLET_DIR);
        if (checkExistence && !existence) {
            exitsWithMessage("Not in an initialized Gitlet directory.");
        } else if (!checkExistence && existence) {
            exitsWithMessage("A Gitlet version-control system already exists in the current directory.");
        }
    }
}
