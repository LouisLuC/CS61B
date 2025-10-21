package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

import static gitlet.Utils.*;

// TODO: any imports you need here

/**
 * Represents a gitlet repository, containing current states of Repo:
 * - HEAD: the nearest commit on current branch
 * - currentBranch: the current branch the repo on
 * - branches: record mapping of branch name to its newest commit id of all the branch in repo
 * - Addition: record file name of files in current Staging Area for addition
 * - Removal: record file names of files in current Staging Area for removal
 * - CommitTree: record All commits and their links
 *
 * @author Louis Lu
 */
public class RepositoryRefactor implements Serializable {
    /**
     * TODO: add instance variables here.
     * <p>
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */


    private String HEAD;
    private String currentBranch;
    private Map<String, String> branches;
    private Set<String> removal;

    // private CommitTree cmtTree;

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

    private static class CommitTree {
        static class Node {
            String commitId;
            String parentId;
            String mergedParentId;

            Node(String commitId, String parentId, String mergedParentId) {
                this.mergedParentId = mergedParentId;
                this.commitId = mergedParentId;
                this.parentId = mergedParentId;
            }
        }

        Node init;

        CommitTree(Node init) {
            this.init = init;
        }
    }

    // public static final String DELIMITER = "/";

    /**
     * The current working directory.
     */
    public static final String CWD = System.getProperty("user.dir");

    /**
     * The .gitlet and associated directory.
     * The Structure of .gitlet directory is like:
     * .gitlet
     * ├── Blobs
     * │         ├── 9d4cc50909a76fddee78aa3e1109984797c0a6fe
     * │         ├── e8d38523fee7e92cf365d4a7ca1a62cc326f191d
     * │         └── ...
     * ├── Commits
     * │         ├── 90d14aef18a17e13b4e2222df26332d25092be3f
     * │         ├── dfc960a42c1426126ed638d45186e88e1ea4624d
     * │         └── ...
     * ├── StagingArea
     * │    ├── FileName1
     * │    └── FileName2
     * └── States
     */
    private static final String GITLET_DIR_NAME = ".gitlet";
    private static final String STAGE_DIR_NAME = String.join(DELIMITER, GITLET_DIR_NAME, "StagingArea");
    private static final String BLOBS_DIR_NAME = String.join(DELIMITER, GITLET_DIR_NAME, "Blobs");
    private static final String COMMITS_DIR_NAME = String.join(DELIMITER, GITLET_DIR_NAME, "Commits");
    private static final String STATES_FILE_NAME = String.join(DELIMITER, GITLET_DIR_NAME, "State");

    private Path gitletDir;
    private Path stagingArea;
    private Path blobsDir;
    private Path commitsDir;
    private Path statesFile;


    // public static final String ADDITION_DIR = String.join(DELIMITER, STAGE_DIR, "addition");
    // public static final String REMOVAL_FILE = String.join(DELIMITER, STAGE_DIR, "Removal");

    /**
     * Directory contains things about repository, like Blobs, Commits
     */

    /**
     * Directory contains Blob files
     */
    /**
     * Directory contains Commits
     */
    // public static final String POINTER_FILE = String.join(DELIMITER, REPO_DIR, "Pointers");
    private static Path getAbsolutePath(String relativePath) {
        return Paths.get(CWD, relativePath);
    }


    /* TODO: fill in the rest of this class. */

    /**
     * Constructor initiate a gitlet system in CWD
     */
    public RepositoryRefactor(String cwd) throws IOException {
        this.gitletDir = Paths.get(cwd, GITLET_DIR_NAME);
        this.stagingArea = Paths.get(cwd, STAGE_DIR_NAME);
        this.commitsDir = Paths.get(cwd, COMMITS_DIR_NAME);
        this.statesFile = Paths.get(cwd, STATES_FILE_NAME);
        this.blobsDir = Paths.get(cwd, BLOBS_DIR_NAME);

        try {
            Files.createDirectories(gitletDir);
            Files.createDirectories(stagingArea);
            Files.createDirectories(commitsDir);
            Files.createDirectories(blobsDir);
            Files.createFile(statesFile);
        } catch (FileAlreadyExistsException e) {
            throw new GitletException("A Gitlet version-control system already exists in the current directory.");
        }
        Commit initCmt = Commit.initCommit();

        this.HEAD = initCmt.getId();
        this.currentBranch = "master";
        this.branches = new HashMap<>();
        this.branches.put("master", initCmt.getId());
        this.removal = new HashSet<>();

        this.saveCmt(initCmt);
    }


    /**
     * Save states of Repository
     */
    private static void saveState(RepositoryRefactor repo) throws IOException {
        writeObject(repo.statesFile.toFile(), repo);
    }

    /* Related to `gitlet add` command */

    /**
     * Add `file` content (Make it a 'Blob') into Staging Area.
     * If `file` does not exist, throw GitletException;
     * If `file` exists and:
     * 1. is identical to the file in HEAD commit with same name, do nothing or delete former file added before.
     * 2. is not identical to HEAD, copy the file content into a Blob object,
     * Serialize it into a file which carries the same name with the `file` in the Staging Area
     */
    void add(Path file) throws IOException {
        Commit headCommit = getHEADCmt();
        String commitedFileID = headCommit.getFileIDByFileName(file.getFileName().toString());
        Blob fileInCWD = new Blob(file);

        // TODO test, TO BE DELETED later
        System.out.println("Id is: " + fileInCWD.id + "\nfileName is: " + fileInCWD.fileName);

        if (fileInCWD.id.equals(commitedFileID)) {
            if (checkFileExist(stagingArea.resolve(fileInCWD.fileName)))
                removeFromStage(fileInCWD.fileName);
        } else {
            writeObject(stagingArea.resolve(fileInCWD.fileName).toFile(), fileInCWD);
        }
        // Try to delete it from removal
        removeFromRemoval(fileInCWD.fileName);
    }

    /* RELATED TO GITLET COMMIT */

    /**
     * Make a Commit with `message`, saving snap of tracked files
     * TODO
     */
    void commit(String message) throws IOException {
        Commit cmt = Commit.createCommitAsChildOf(getHEADCmt(), message);
        List<String> stagedFiles = plainFilenamesIn(stagingArea);

        // Check
        if (stagedFiles.isEmpty() && removal.isEmpty()) {
            // if there is no file to be tracked or removed, exist
            throw new GitletException("No changes added to the commit.");
        }

        // Add Staged files and clear Staging Area
        for (String fileName : stagedFiles) {
            Blob stagedBlob = getBlobFromStage(fileName);
            cmt.putInFileMap(fileName, stagedBlob.id);
            saveBlobToRepo(stagedBlob);
            removeFromStage(fileName);
        }
        // Remove untracked files
        for (String fileName : removal) {
            cmt.removeFileMap(fileName);
        }

        // Move pointer of branch
        branches.put(currentBranch, cmt.getId());
        // Move HEAD pointer
        HEAD = cmt.getId();

        // TODO test DELETE WHEN COMPLETE
        printCmtInLog(cmt);

        // save change into files
        clearRemoval();
        saveCmt(cmt);
    }

    private void clearRemoval() {
        removal.clear();
    }

    /**
     * Save a commit into a file in repository
     */
    private void saveCmt(Commit cmt) throws IOException {
        Path commitPath = Files.createFile(commitsDir.resolve(cmt.getId()));
        writeObject(commitPath.toFile(), cmt);
    }

    /**
     * Return commit from repository according to id
     */
    private Commit getCmt(String id) {
        if (!checkFileExist(commitsDir.resolve(id))) {
            return null;
        }
        return readObject(commitsDir.resolve(id).toFile(), Commit.class);
    }

    /**
     * Get the commit the HEAD pointer pointed to
     */
    private Commit getHEADCmt() {
        return getCmt(HEAD);
    }

    private Blob getBlobFromStage(String fileName) {
        Path blobPath = stagingArea.resolve(fileName);
        return readObject(blobPath.toFile(), Blob.class);
    }

    private Blob getBlobFromRepo(String ID) {
        Path blobPath = blobsDir.resolve(ID);
        return readObject(blobPath.toFile(), Blob.class);
    }

    private void saveBlobToRepo(Blob blob) {
        Path blobPath = blobsDir.resolve(blob.id);
        if (!Files.exists(blobPath))
            // if blob with same file name already exist (which means same content)
            // do nothing
            writeObject(blobPath.toFile(), blob);
    }

    /* RELATED TO GITLET REMOVE */

    /**
     * Remove a file from Gitlet Repository.
     * if the file is staged in Staging Area, untrack it
     * if the file is tracked by HEAD, stage it into Removal for committing
     * And if the file exist in CWD, delete it
     * if the file is not tracked and is not in Staging Area, exist with the message
     */
    void remove(Path file) throws IOException {
        Path fileInStage = stagingArea.resolve(file.getFileName().toString());
        String blobId = getHEADCmt().getFileIDByFileName(file.getFileName().toString());
        boolean isFileInStage = checkFileExist(fileInStage);
        boolean isFileTracked = blobId != null;
        if (!isFileTracked && !isFileInStage) {
            throw new GitletException("No reason to remove the file.");
        }
        if (isFileInStage) {
            Files.delete(fileInStage);
        }
        if (isFileTracked) {
            stageToRemoval(file.getFileName().toString());
        }
        restrictedDelete(file.toFile());
    }

    /**
     * Remove a file from Staging Area
     *
     * @param fileName file name of one of files in Staging Area
     */
    private void removeFromStage(String fileName) throws IOException {
        Files.delete(stagingArea.resolve(fileName));
    }

    private void clearStage() throws IOException {
        for (String fileName : plainFilenamesIn(stagingArea))
            removeFromStage(fileName);
    }

    private void stageToRemoval(String fileName) {
        removal.add(fileName);
    }

    private boolean removeFromRemoval(String fileName) {
        return removal.remove(fileName);
    }

    /* RELATED TO LOG */

    void log() {
        Commit currCmt = getHEADCmt();
        while (currCmt != null) {
            printCmtInLog(currCmt);
            currCmt = getCmt(currCmt.getParentId());
        }
    }

    static void printCmtInLog(Commit cmt) {
        System.out.println("===");
        System.out.println("commit " + cmt.getId());
        if (cmt.getMergedParentId() != null) {
            System.out.println("Merge: " + cmt.getMergedParentId());
        }
        SimpleDateFormat dateFmt = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.ENGLISH);
        dateFmt.setTimeZone(TimeZone.getTimeZone("GMT-08:00"));
        // TODO change timezone depending on where you live
        System.out.println("Date: " + dateFmt.format(new Date(cmt.getTimestamp())));
        System.out.println(cmt.getMessage());
        System.out.println();
    }

    void globalLog() {
        List<String> cmtIDs = plainFilenamesIn(commitsDir);
        for (String id : cmtIDs) {
            Commit cmt = getCmt(id);
            printCmtInLog(cmt);
        }
    }

    /**
     * Prints out the ids of all commits that have the given commit message, one per line.
     * If there are multiple such commits, it prints the ids out on separate lines.
     */
    void find(String msg) {
        List<String> cmtIDs = plainFilenamesIn(commitsDir);
        for (String id : cmtIDs) {
            Commit cmt = getCmt(id);
            if (msg.equals(cmt.getMessage())) {
                System.out.println(cmt.getId());
            }
        }
    }

    /* RELATED TO STATUS */

    void status() {
        printBranch();
        printStage();
        printRemoval();
        printUntrackFileAndModificationNotStaged();
    }

    void printBranch() {
        System.out.println("=== Branches ===");

        List<String> printable = new ArrayList<>(branches.keySet());
        printable.sort(String.CASE_INSENSITIVE_ORDER);

        for (String name : printable) {
            if (name.equals(currentBranch)) {
                name = "*" + name;
            }
            System.out.println(name);
        }
        System.out.println();
    }

    void printStage() {
        System.out.println("=== Staged Files ===");
        for (String fileName : plainFilenamesIn(stagingArea)) {
            System.out.println(fileName);
        }
        System.out.println();
    }

    void printRemoval() {
        System.out.println("=== Removed Files ===");
        List<String> printable = new ArrayList<>(removal);
        printable.sort(String.CASE_INSENSITIVE_ORDER);
        for (String fileToRemove : removal) {
            System.out.println(fileToRemove);
        }
        System.out.println();
    }


    void printUntrackFileAndModificationNotStaged() {
        Commit HEAD = getHEADCmt();

        List<String> filesInCWD = plainFilenamesIn(CWD);
        Set<String> filesTracked = HEAD.getAllTrackedFiles();
        List<String> filesStaged = plainFilenamesIn(stagingArea);
        Set<String> filesToRemove = new HashSet<>(removal);

        List<String> modifications = new ArrayList<>();
        List<String> untracked = new ArrayList<>(filesInCWD);
        List<String> filesStagedButNotInCWD = new ArrayList<>(filesStaged);
        List<String> filesTrackedNotInCWD = new ArrayList<>(filesTracked);

        for (String fileName : filesInCWD) {
            String CWDId = new Blob(Paths.get(CWD, fileName)).id;
            String additionId = filesStaged.contains(fileName) ? getBlobFromStage(fileName).id : null;
            String commitId = HEAD.getFileIDByFileName(fileName);
            if ((additionId != null && !additionId.equals(CWDId)) ||
                    (commitId != null && additionId == null && !commitId.equals(CWDId))) {
                modifications.add(fileName + " (modified)");
            }
            if (additionId != null || commitId != null) {
                untracked.remove(fileName);
            }
            filesStagedButNotInCWD.remove(fileName);
            filesTrackedNotInCWD.remove(fileName);
        }
        for (String fileName : filesToRemove) {
            filesTrackedNotInCWD.remove(fileName);
        }
        for (String fileName : filesStagedButNotInCWD) {
            modifications.add(fileName + " (deleted)");
        }
        for (String fileName : filesTrackedNotInCWD) {
            modifications.add(fileName + " (deleted)");
        }
        modifications.sort(String.CASE_INSENSITIVE_ORDER);
        untracked.sort(String.CASE_INSENSITIVE_ORDER);
        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String fileName : modifications) {
            System.out.println(fileName);
        }
        System.out.println();
        System.out.println("=== Untracked Files ===");
        for (String file : untracked) {
            System.out.println(file);
        }
        System.out.println();
    }

    /* RELATED TO CHECKOUT COMMAND */

    void checkout(String fileName) throws IOException {
        checkout(fileName, HEAD);
    }

    void checkout(String fileName, String cmtId) throws IOException {
        // TODO the ID may be abbreviated version, see spec
        Commit cmt = getCmt(cmtId);
        if (cmt == null) {
            throw new GitletException("No commit with that id exists.");
        }

        String fileId = cmt.getFileIDByFileName(fileName);
        if (fileId == null) {
            throw new GitletException("File does not exist in that commit.");
        }

        Blob fileInBlob = getBlobFromRepo(fileId);

        Files.write(Paths.get(CWD, fileName), fileInBlob.contents);
    }

    void checkoutToBranch(String branchName) throws IOException {
        String branchCmtId = branches.get(branchName);

        if (branchName.equals(currentBranch)) throw new GitletException("No need to checkout the current branch.");
        if (branchCmtId == null) throw new GitletException("No such branch exists.");

        checkoutFilesToCmt(branchCmtId);

        // Change current branch and HEAD pointer
        currentBranch = branchName;
        HEAD = branchCmtId;
    }

    /**
     * Checkout files to a specific commit
     * Note: this method focuses on checkout files in Staging Area and CWD, would not modify pointers
     *
     * @param cmtId specified commit id
     */
    private void checkoutFilesToCmt(String cmtId) throws IOException {
        Commit coutCmt = getCmt(cmtId);
        Commit currCmt = getHEADCmt();

        // (*) Find any files that are tracked (which means files stored in commit or stated for addition)
        // in the current branch but are not present in the checked-out branch
        List<String> currTrackNotInCoutCmtFiles = new ArrayList<>(currCmt.getAllTrackedFiles());
        List<String> stagedNotInCoutCmtFiles = plainFilenamesIn(stagingArea);

        for (String fileName : coutCmt.getAllTrackedFiles()) {
            String fileId = coutCmt.getFileIDByFileName(fileName);
            Blob fileInRepo = getBlobFromRepo(fileId);
            Files.write(Paths.get(CWD, fileName), fileInRepo.contents);

            // For (*)
            currTrackNotInCoutCmtFiles.remove(fileName);
            stagedNotInCoutCmtFiles.remove(fileName);
        }

        // Delete any files in (*)
        for (String fileName : currTrackNotInCoutCmtFiles)
            restrictedDelete(Paths.get(CWD, fileName).toFile());
        for (String fileName : stagedNotInCoutCmtFiles)
            restrictedDelete(Paths.get(CWD, fileName).toFile());

        // Clear the staging area
        clearStage();
        clearRemoval();
    }

    void reset(String cmtId) throws IOException {
        checkUntrackedChange(cmtId);
        if (!checkFileExist(commitsDir.resolve(cmtId))) throw new GitletException("No commit with that id exists.");

        checkoutFilesToCmt(cmtId);

        // Change HEAD pointer
        HEAD = cmtId;
    }

    /**
     * Check if there is a file is untracked in the HEAD and
     * would be overwritten by the reset to commit specified by cmdId,
     * which means that there is a fileName in CWD, tracked by cmdId specified commit and its content is
     * not identical to one in either HEAD commit and Staging Area.
     * throw exception with message and exit; perform this check before command reset.
     **/
    void checkUntrackedChange(String cmdId) {
        Commit cmt = getCmt(cmdId);
        Commit HEAD = getHEADCmt();

        Set<String> filesInCWD = new HashSet<>(plainFilenamesIn(CWD));
        Set<String> filesTracked = cmt.getAllTrackedFiles();

        for (String fileName : filesTracked) {
            if (filesInCWD.contains(fileName)) {
                // Current CWD contains file tracked by commit specified by cmdId
                String fileInCWDId = new Blob(Paths.get(CWD, fileName)).id;
                String fileStagedId = checkFileExist(stagingArea.resolve(fileName)) ? getBlobFromStage(fileName).id : null;
                String fileInHEADId = HEAD.getFileIDByFileName(fileName);
                if (!fileInCWDId.equals(fileInHEADId) && !fileInCWDId.equals(fileStagedId)) {
                    // if this file is not identical to either file in HEAD commit and Staging Area
                    throw new GitletException("There is an untracked file in the way;" +
                            " delete it, or add and commit it first.");
                }
            }
        }
    }

    /* RELATED TO BRANCHES */

    void createBranch(String name) {
        branches.put(name, HEAD);
    }

    /**
     * Deletes the branch with the given name.
     * This only means to delete the pointer associated with the branch
     */
    void removeBranch(String name) {
        if (currentBranch.equals(name)) throw new GitletException("Cannot remove the current branch.");
        if (!branches.containsKey(name)) throw new GitletException("A branch with that name does not exist.");
        branches.remove(name);
    }

    /* UTILITIES FOR MAIN */

    static void checkGitletInit(boolean checkExistence) {
        boolean existence = checkDirExist(Repository.GITLET_DIR);
        if (checkExistence && !existence) {
            throw new GitletException("Not in an initialized Gitlet directory.");
        } else if (!checkExistence && existence) {
            throw new GitletException("A Gitlet version-control system already exists in the current directory.");
        }
    }
}
