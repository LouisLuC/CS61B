package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

import static gitlet.Utils.*;

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
public class Repository implements Serializable {
    /**
     * TODO: add instance variables here.
     * <p>
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */


    @Serial
    private static final long serialVersionUID = 42L;

    /**
     * States that would be changed for a Repository
     */
    private String HEAD;
    private String currentBranch;
    private Map<String, String> branches;
    private StagingArea stagingArea;

    private static class StagingArea implements Serializable {
        Set<String> removal;
        Map<String, String> addition;

        void stageForAddition(Blob blob) {
            addition.put(blob.fileName, blob.id);
            blob.save();
        }

        StagingArea() {
            this.addition = new TreeMap<>();
            this.removal = new TreeSet<>();
        }

        void clear() {
            addition.clear();
            removal.clear();
        }

        boolean isEmpty() {
            return addition.isEmpty() && removal.isEmpty();
        }
    }


    /**
     * The .gitlet and associated directory.
     * The Structure of .gitlet directory is like:
     * .gitlet
     * ├── Blobs  // Contains all the blobs that gitlet system tracked
     * │         ├── 9d4cc50909a76fddee78aa3e1109984797c0a6fe
     * │         ├── e8d38523fee7e92cf365d4a7ca1a62cc326f191d
     * │         └── ...
     * ├── Commits // Contains all the commits
     * │         ├── 90d14aef18a17e13b4e2222df26332d25092be3f
     * │         ├── dfc960a42c1426126ed638d45186e88e1ea4624d
     * │         └── ...
     * └── States // Store system states, including pointers(HEAD, currentBranch and so on)
     * // and file mappings for staging area
     */
    public static final String CWD = System.getProperty("user.dir");

    /**
     * Directory associated to gitlet system.
     */
    static final String GITLET_DIR_NAME = ".gitlet";
    static final String BLOBS_DIR_NAME = String.join(File.separator, GITLET_DIR_NAME, "Blobs");
    static final String COMMITS_DIR_NAME = String.join(File.separator, GITLET_DIR_NAME, "Commits");
    static final String STATES_FILE_NAME = String.join(File.separator, GITLET_DIR_NAME, "State");

    private static final Path CWD_PATH = Paths.get(CWD);
    static final Path GITLET_DIR = CWD_PATH.resolve(GITLET_DIR_NAME);
    static final Path BLOBS_DIR = CWD_PATH.resolve(BLOBS_DIR_NAME);
    static final Path COMMITS_DIR = CWD_PATH.resolve(COMMITS_DIR_NAME);
    static final Path STATES_FILE = CWD_PATH.resolve(STATES_FILE_NAME);

    /**
     * Constructor initiate a gitlet system in CWD
     */
    public Repository() throws IOException {
        try {
            Files.createDirectories(GITLET_DIR);
            // Files.createDirectories(STAGING_AREA);
            Files.createDirectories(COMMITS_DIR);
            Files.createDirectories(BLOBS_DIR);
            Files.createFile(STATES_FILE);
        } catch (FileAlreadyExistsException e) {
            throw new GitletException("A Gitlet version-control system already exists in the current directory.");
        }
        Commit initCmt = Commit.initCommit();

        HEAD = initCmt.getId();
        currentBranch = "master";
        branches = new TreeMap<>();
        branches.put("master", initCmt.getId());
        stagingArea = new StagingArea();

        initCmt.save();
    }


    /**
     * Save states of Repository
     */
    public static void saveState(Repository repo) {
        writeObject(STATES_FILE.toFile(), repo);
    }

    /**
     * Save states of Repository
     */
    public static Repository loadState() {
        return readObject(STATES_FILE.toFile(), Repository.class);
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
    void add(Path file) {
        Commit headCommit = Commit.getCmt(HEAD);
        String commitedFileID = headCommit.get(file.getFileName().toString());
        Blob fileInCWD = new Blob(file);

        // TODO test, TO BE DELETED later
        System.out.println("Id is: " + fileInCWD.id + "\nfileName is: " + fileInCWD.fileName);

        if (fileInCWD.id.equals(commitedFileID)) {
            // If file is identical to version in HEAD commit, remove if from stage
            stagingArea.addition.remove(fileInCWD.fileName);
        } else {
            stagingArea.addition.put(fileInCWD.fileName, fileInCWD.id);
            fileInCWD.save();
        }
        /*
        if (fileInCWD.id.equals(commitedFileID)) {
            if (checkFileExist(STAGING_AREA.resolve(fileInCWD.fileName)))
                removeFromStage(fileInCWD.fileName);
        } else {
            writeObject(STAGING_AREA.resolve(fileInCWD.fileName).toFile(), fileInCWD);
        }
        */

        // Try to delete it from removal
        // removeFromRemoval(fileInCWD.fileName);
        stagingArea.removal.remove(fileInCWD.fileName);
    }

    /* RELATED TO GITLET COMMIT */

    /**
     * Make a Commit with `message`, saving snap of tracked files
     * TODO
     */
    void commit(String message) throws IOException {
        Commit cmt = Commit.createCommitAsChildOf(Commit.getCmt(HEAD), message);

        // Check
        if (stagingArea.isEmpty()) {
            // if there is no file to be tracked or removed, exist
            throw new GitletException("No changes added to the commit.");
        }

        // Add files in addition
        // Set<String> stagedFiles = new HashSet<>(stagingArea.additon.keySet());
        for (String fileName : stagingArea.addition.keySet()) {
            Blob stagedBlob = Blob.getBlob(stagingArea.addition.get(fileName));
            cmt.put(fileName, stagedBlob.id);
            stagedBlob.save();
            // saveBlobToRepo(stagedBlob);
            // removeFromStage(fileName);
        }
        // Remove files in removal
        for (String fileName : stagingArea.removal) {
            cmt.remove(fileName);
        }
        stagingArea.clear();

        // Move pointer of branch
        branches.put(currentBranch, cmt.getId());
        // Move HEAD pointer
        HEAD = cmt.getId();

        // TODO test DELETE WHEN COMPLETE
        printCmtInLog(cmt);

        // save change into files
        cmt.save();
    }

    /*
    private void clearRemoval() {
        removal.clear();
    }
     */

    /*
     * Save a commit into a file in repository
    private void saveCmt(Commit cmt) throws IOException {
        Path commitPath = Files.createFile(COMMITS_DIR.resolve(cmt.getId()));
        writeObject(commitPath.toFile(), cmt);
    }
     */

    /*
     * Return commit from repository according to id
    private Commit getCmt(String id) {
        if (id == null || !checkFileExist(COMMITS_DIR.resolve(id))) {
            return null;
        }
        return readObject(COMMITS_DIR.resolve(id).toFile(), Commit.class);
    }
     */

    /*
    private Blob getBlobFromStage(String fileName) {
        Path blobPath = STAGING_AREA.resolve(fileName);
        return readObject(blobPath.toFile(), Blob.class);
    }

    private Blob Blob.getBlob(String ID) {
        Path blobPath = BLOBS_DIR.resolve(ID);
        if (!checkFileExist(blobPath)) return null;
        return readObject(blobPath.toFile(), Blob.class);
    }

    private void saveBlobToRepo(Blob blob) {
        Path blobPath = BLOBS_DIR.resolve(blob.id);
        if (!Files.exists(blobPath))
            // if blob with same file name already exist (which means same content)
            // do nothing
            writeObject(blobPath.toFile(), blob);
    }
    */

    /* RELATED TO GITLET REMOVE */

    /**
     * Remove a file from Gitlet Repository.
     * if the file is staged in Staging Area, untrack it
     * if the file is tracked by HEAD, stage it into Removal for committing
     * And if the file exist in CWD, delete it
     * if the file is not tracked and is not in Staging Area, exist with the message
     */
    void remove(Path file) throws IOException {
        // Path fileInStage = STAGING_AREA.resolve(file.getFileName().toString());
        Blob stagedBlob = Blob.getBlob(stagingArea.addition.get(file.getFileName().toString()));
        String cmtBlobId = Commit.getCmt(HEAD).get(file.getFileName().toString());
        // boolean isFileInStage = checkFileExist(fileInStage);
        // boolean isFileTracked = cmtBlobId != null;
        if (cmtBlobId == null && stagedBlob == null) {
            throw new GitletException("No reason to remove the file.");
        }
        if (stagedBlob != null) {
            // Files.delete(fileInStage);
            stagingArea.addition.remove(stagedBlob.id);
        }
        if (cmtBlobId != null) {
            // stageToRemoval(file.getFileName().toString());
            stagingArea.removal.add(file.getFileName().toString());
        }
        restrictedDelete(file.toFile());
    }

    /*
     * Remove a file from Staging Area
     *
     * @param fileName file name of one of files in Staging Area
    private void removeFromStage(String fileName) throws IOException {
        Files.delete(STAGING_AREA.resolve(fileName));
    }
    */

    /*
    private void clearStage() throws IOException {
        for (String fileName : plainFilenamesIn(STAGING_AREA))
            removeFromStage(fileName);
    }
    *

    private void stageToRemoval(String fileName) {
        removal.add(fileName);
    }

    private boolean removeFromRemoval(String fileName) {
        return stagingArea.removal.remove(fileName) != null;
    }
     */

    /* RELATED TO LOG */

    void log() {
        Commit currCmt = Commit.getCmt(HEAD);
        while (currCmt != null) {
            printCmtInLog(currCmt);
            currCmt = Commit.getCmt(currCmt.getParentId());
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
        List<String> cmtIDs = plainFilenamesIn(COMMITS_DIR);
        for (String id : cmtIDs) {
            Commit cmt = Commit.getCmt(id);
            printCmtInLog(cmt);
        }
    }

    /**
     * Prints out the ids of all commits that have the given commit message, one per line.
     * If there are multiple such commits, it prints the ids out on separate lines.
     */
    void find(String msg) {
        List<String> cmtIDs = plainFilenamesIn(COMMITS_DIR);
        for (String id : cmtIDs) {
            Commit cmt = Commit.getCmt(id);
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
        for (String fileName : stagingArea.addition.keySet()) {
            System.out.println(fileName);
        }
        System.out.println();
    }

    void printRemoval() {
        System.out.println("=== Removed Files ===");
        // List<String> printable = new ArrayList<>(removal);
        // printable.sort(String.CASE_INSENSITIVE_ORDER);
        for (String fileToRemove : stagingArea.removal) {
            System.out.println(fileToRemove);
        }
        System.out.println();
    }


    void printUntrackFileAndModificationNotStaged() {
        // TODO fix removal files in modified
        Commit cmt = Commit.getCmt(HEAD);

        List<String> filesInCWD = plainFilenamesIn(CWD_PATH);
        List<String> filesTracked = new ArrayList<>(cmt.getAll());
        List<String> filesStaged = new ArrayList<>(stagingArea.addition.keySet());
        List<String> filesToRemove = new ArrayList<>(stagingArea.removal);

        List<String> modifications = new ArrayList<>();
        List<String> untracked = new ArrayList<>(filesInCWD);
        List<String> filesStagedButNotInCWD = new ArrayList<>(filesStaged);
        List<String> filesTrackedNotInCWD = new ArrayList<>(filesTracked);

        for (String fileName : filesInCWD) {
            String CWDId = new Blob(CWD_PATH.resolve(fileName)).id;
            // String addId = filesStaged.contains(fileName) ? getBlobFromStage(fileName).id : null;
            String addId = stagingArea.addition.get(fileName);
            String cmtId = cmt.get(fileName);
            if ((addId != null && !addId.equals(CWDId)) ||
                    (cmtId != null && addId == null && !cmtId.equals(CWDId))) {
                modifications.add(fileName + " (modified)");
            }
            if (addId != null || cmtId != null) {
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

    /**
     * Checkout a file from HEAD commit into CWD
     */
    void checkout(String fileName) throws IOException {
        checkout(fileName, HEAD);
    }

    /**
     * Checkout blob contents into a file
     */
    private void checkout(Blob blob) {
        Path path = CWD_PATH.resolve(blob.fileName);
        writeContents(path.toFile(), blob.contents);
    }

    /**
     * Checkout a file from a specific commit into CWD
     *
     * @param cmtId specific commit's id
     */
    void checkout(String fileName, String cmtId) throws IOException {
        if (cmtId.length() < 40) {
            cmtId = getFullCmtId(cmtId);
        }
        Commit cmt = Commit.getCmt(cmtId);
        if (cmt == null) {
            throw new GitletException("No commit with that id exists.");
        }
        String fileId = cmt.get(fileName);
        if (fileId == null) {
            throw new GitletException("File does not exist in that commit.");
        }
        Blob fileInBlob = Blob.getBlob(fileId);
        checkout(fileInBlob);
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
     * @param cmtId specific commit id
     */
    private void checkoutFilesToCmt(String cmtId) throws IOException {
        Commit coutCmt = Commit.getCmt(cmtId);
        Commit currCmt = Commit.getCmt(HEAD);

        // (*) Find any files that are tracked (which means files stored in commit or stated for addition)
        // in the current branch but are not present in the checked-out branch
        List<String> currTrackNotInCoutCmtFiles = new ArrayList<>(currCmt.getAll());
        List<String> stagedNotInCoutCmtFiles = new ArrayList<>(stagingArea.addition.keySet());

        for (String fileName : coutCmt.getAll()) {
            String fileId = coutCmt.get(fileName);
            Blob fileInRepo = Blob.getBlob(fileId);
            // Files.write(Paths.get(CWD, fileName), fileInRepo.contents);
            checkout(fileInRepo);

            // For (*)
            currTrackNotInCoutCmtFiles.remove(fileName);
            stagedNotInCoutCmtFiles.remove(fileName);
        }

        // Delete any files in (*)
        for (String fileName : currTrackNotInCoutCmtFiles)
            restrictedDelete(CWD_PATH.resolve(fileName).toFile());
        for (String fileName : stagedNotInCoutCmtFiles)
            restrictedDelete(CWD_PATH.resolve(fileName).toFile());

        // Clear the staging area
        stagingArea.clear();
    }

    void reset(String cmtId) throws IOException {
        if (cmtId.length() < 40) {
            cmtId = getFullCmtId(cmtId);
        }
        checkUntrackedChange(cmtId);
        if (!checkFileExist(COMMITS_DIR.resolve(cmtId))) throw new GitletException("No commit with that id exists.");

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
        Commit cmt = Commit.getCmt(cmdId);
        Commit headCmt = Commit.getCmt(HEAD);


        Set<String> filesInCWD = new HashSet<>(plainFilenamesIn(CWD));
        Set<String> filesTracked = cmt.getAll();

        for (String fileName : filesTracked) {
            if (filesInCWD.contains(fileName)) {
                // Current CWD contains file tracked by commit specified by cmdId
                String fileInCWDId = new Blob(CWD_PATH.resolve(fileName)).id;
                String fileStagedId = stagingArea.addition.get(fileName);
                String fileInHEADId = headCmt.get(fileName);
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

    boolean merge(String branchName) throws IOException {
        String splitPoint = getSplitPointWithOtherBranch(branches.get(branchName));
        if (splitPoint == null) return true;
        Commit currBranchCmt = Commit.getCmt(HEAD);
        Commit otherBranchCmt = Commit.getCmt(branches.get(branchName));
        Commit splitPointCmt = Commit.getCmt(splitPoint);

        Set<String> currBranchFiles = currBranchCmt.getAll();
        Set<String> otherBranchFiles = otherBranchCmt.getAll();
        Set<String> splitPointFiles = splitPointCmt.getAll();
        for (String fileName : splitPointFiles) {
            String fileInSplitId = splitPointCmt.get(fileName);
            String fileInCurrId = currBranchCmt.get(fileName);
            String fileInOtherId = otherBranchCmt.get(fileName);

            boolean isCurrModified = !fileInSplitId.equals(fileInCurrId);
            boolean isOtherModified = !fileInSplitId.equals(fileInOtherId);
            boolean isOtherDeleted = fileInOtherId == null;
            boolean isCurrDeleted = fileInCurrId == null;

            Path fileInCWD = CWD_PATH.resolve(fileName);
            if (!isCurrModified) {
                if (isOtherModified) {
                    if (isOtherDeleted) {
                        remove(fileInCWD);
                    } else {
                        checkout(fileName, otherBranchCmt.getId());
                        add(fileInCWD);
                    }
                }
            } else {
                if (!isOtherModified) {
                    if (isCurrDeleted) {
                        // do nothing
                    }
                } else {
                    if (Objects.equals(fileInCurrId, fileInOtherId)) {
                        // do nothing
                    } else {
                        mergeFileConflict(fileInCurrId, fileInOtherId);
                    }
                }
            }
            currBranchFiles.remove(fileName);
            otherBranchFiles.remove(fileName);
        }

        /* Files which not exist in Split Point Commit */

        for (String fileName : otherBranchFiles) {
            String fileInOtherId = otherBranchCmt.get(fileName);
            String fileInCurrId = currBranchCmt.get(fileName);
            if (fileInCurrId == null) {
                // TODO check checkout has staging action
                checkout(fileName, otherBranchCmt.getId());
            } else if (!fileInCurrId.equals(fileInOtherId)) {
                mergeFileConflict(fileInCurrId, fileInOtherId);
            }
            currBranchFiles.remove(fileName);
        }

        /* Remain file in currBranchFiles is files which not exist in other branch, reserve.
        for (String fileName : currBranchFiles) {
        }
        */

        return true;
    }

    private void mergeFileConflict(String id1, String id2) {
        // fileInCurrBlob as file In HEAD
        Blob fileInCurrBlob = Blob.getBlob(id1);
        Blob fileInOtherBlob = Blob.getBlob(id2);
        String fileName = fileInCurrBlob == null ? fileInOtherBlob.fileName : fileInCurrBlob.fileName;
        Blob mergedBlob = Blob.mergeConflict(fileName, fileInCurrBlob, fileInOtherBlob);
        // saveToCWD(mergedBlob);
        checkout(mergedBlob);
        // add(fileInCWD);
        add(CWD_PATH.resolve(mergedBlob.fileName));
        // saveToStage(mergedBlob);
    }

    private String getSplitPointWithOtherBranch(String otherBranchName) throws IOException {
        Commit currBranchCmt = Commit.getCmt(branches.get(currentBranch));
        Commit otherBranchCmt = Commit.getCmt(branches.get(otherBranchName));

        Set<String> trackCurr = new HashSet<>();
        Set<String> trackOther = new HashSet<>();

        String ret = null;
        while (currBranchCmt != null || otherBranchCmt != null) {
            String currId = currBranchCmt == null ? null : currBranchCmt.getId();
            String otherId = otherBranchCmt == null ? null : otherBranchCmt.getId();

            if (currId != null) trackCurr.add(currBranchCmt.getId());
            if (otherId != null) trackOther.add(otherId);

            if (trackCurr.contains(otherId)) {
                if (otherId.equals(HEAD)) {
                    // If the split point is the current branch,
                    // then the effect is to check out the given branch
                    checkoutToBranch(otherBranchName);
                    System.out.println("Current branch fast-forwarded.");
                    return null;
                }
                ret = otherId;
                break;
            }

            if (trackOther.contains(currId)) {
                if (currId.equals(branches.get(otherBranchName))) {
                    // If the split point is the same commit as the given branch
                    System.out.println("Given branch is an ancestor of the current branch.");
                    return null;
                }
                ret = currId;
                break;
            }
            currBranchCmt = currBranchCmt == null ? null : Commit.getCmt(currBranchCmt.getParentId());
            otherBranchCmt = otherBranchCmt == null ? null : Commit.getCmt(otherBranchCmt.getParentId());
        }
        return ret;
    }

    /* UTILITIES RELATED TO REPOSITORY */

    static void checkGitletInit(boolean checkExistence) {
        boolean existence = checkDirExist(GITLET_DIR);
        if (checkExistence && !existence) {
            throw new GitletException("Not in an initialized Gitlet directory.");
        } else if (!checkExistence && existence) {
            throw new GitletException("A Gitlet version-control system already exists in the current directory.");
        }
    }

    static String getFullCmtId(String abbreviate) {
        List<String> fileNames = plainFilenamesIn(COMMITS_DIR);
        for (String fileName : fileNames) {
            if (fileName.contains(abbreviate)) {
                return fileName;
            }
        }
        return null;
    }
}
