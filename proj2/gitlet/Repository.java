package gitlet;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
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

    private static class StagingArea {
        Map<String> removal;
        Map<String> additon;

        static getStagingArea()
    }

    /**
     * Represents the saved contents of files.
     */
    private static class Blob implements Serializable {
        String id;
        String fileName;
        byte[] contents;

        @Serial
        private static final long serialVersionUID = 24L;

        private Blob() {
        }

        Blob(Path file) {
            this.fileName = file.getFileName().toString();
            this.contents = readContents(file);
            this.id = sha1(fileName, contents);
        }

        static Blob mergeConflict(String fileName, Blob inHEAD, Blob other) {
            Blob newBlog = new Blob();
            byte[] inHeadContent = inHEAD == null ? new byte[0] : inHEAD.contents;
            byte[] otherContent = other == null ? new byte[0] : other.contents;
            String strBuilder = "<<<<<<< HEAD\n" +
                    new String(inHeadContent, StandardCharsets.UTF_8) +
                    "=======\n" +
                    new String(otherContent, StandardCharsets.UTF_8) +
                    ">>>>>>>\n";
            newBlog.contents = strBuilder.getBytes(StandardCharsets.UTF_8);
            newBlog.id = sha1(newBlog.fileName, newBlog.contents);
            return newBlog;
        }
    }

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
    public static final String CWD = System.getProperty("user.dir");

    /**
     * Directory associated to gitlet system.
     */
    private static final String GITLET_DIR_NAME = ".gitlet";
    private static final String STAGE_DIR_NAME = String.join(DELIMITER, GITLET_DIR_NAME, "StagingArea");
    private static final String BLOBS_DIR_NAME = String.join(DELIMITER, GITLET_DIR_NAME, "Blobs");
    private static final String COMMITS_DIR_NAME = String.join(DELIMITER, GITLET_DIR_NAME, "Commits");
    private static final String STATES_FILE_NAME = String.join(DELIMITER, GITLET_DIR_NAME, "State");

    private static final Path GITLET_DIR = Paths.get(CWD, GITLET_DIR_NAME);
    ;
    private static final Path STAGING_AREA = Paths.get(CWD, STAGE_DIR_NAME);
    private static final Path BLOBS_DIR = Paths.get(CWD, BLOBS_DIR_NAME);
    private static final Path COMMITS_DIR = Paths.get(CWD, COMMITS_DIR_NAME);
    private static final Path STATES_FILE = Paths.get(CWD, STATES_FILE_NAME);

    /**
     * Constructor initiate a gitlet system in CWD
     */
    public Repository() throws IOException {
        try {
            Files.createDirectories(GITLET_DIR);
            Files.createDirectories(STAGING_AREA);
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
        // removal = new HashSet<>();

        saveCmt(initCmt);
    }


    /**
     * Save states of Repository
     */
    public static void saveState(Repository repo) throws IOException {
        writeObject(STATES_FILE.toFile(), repo);
    }

    /**
     * Save states of Repository
     */
    public static Repository loadState() throws IOException {
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
    void add(Path file) throws IOException {
        Commit headCommit = getHEADCmt();
        String commitedFileID = headCommit.getFileIDByFileName(file.getFileName().toString());
        Blob fileInCWD = new Blob(file);

        // TODO test, TO BE DELETED later
        System.out.println("Id is: " + fileInCWD.id + "\nfileName is: " + fileInCWD.fileName);

        if (fileInCWD.id.equals(commitedFileID)) {
            if (checkFileExist(STAGING_AREA.resolve(fileInCWD.fileName)))
                removeFromStage(fileInCWD.fileName);
        } else {
            writeObject(STAGING_AREA.resolve(fileInCWD.fileName).toFile(), fileInCWD);
        }
        // Try to delete it from removal
        removeFromRemoval(fileInCWD.fileName);
    }

    void saveToCWD(Blob blob) {
        Path path = Paths.get(CWD).resolve(blob.fileName);
        writeContents(path.toFile(), new String(blob.contents, StandardCharsets.UTF_8));
    }

    void saveToStage(Blob blob) {
        Path path = STAGING_AREA.resolve(blob.fileName);
        if (!checkFileExist(path))
            writeObject(path.toFile(), blob);
    }

    /* RELATED TO GITLET COMMIT */

    /**
     * Make a Commit with `message`, saving snap of tracked files
     * TODO
     */
    void commit(String message) throws IOException {
        Commit cmt = Commit.createCommitAsChildOf(getHEADCmt(), message);
        List<String> stagedFiles = plainFilenamesIn(STAGING_AREA);

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
        Path commitPath = Files.createFile(COMMITS_DIR.resolve(cmt.getId()));
        writeObject(commitPath.toFile(), cmt);
    }

    /**
     * Return commit from repository according to id
     */
    private Commit getCmt(String id) {
        if (id == null || !checkFileExist(COMMITS_DIR.resolve(id))) {
            return null;
        }
        return readObject(COMMITS_DIR.resolve(id).toFile(), Commit.class);
    }

    /**
     * Get the commit the HEAD pointer pointed to
     */
    private Commit getHEADCmt() {
        return getCmt(HEAD);
    }

    private Blob getBlobFromStage(String fileName) {
        Path blobPath = STAGING_AREA.resolve(fileName);
        return readObject(blobPath.toFile(), Blob.class);
    }

    private Blob getBlobFromRepo(String ID) {
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

    /* RELATED TO GITLET REMOVE */

    /**
     * Remove a file from Gitlet Repository.
     * if the file is staged in Staging Area, untrack it
     * if the file is tracked by HEAD, stage it into Removal for committing
     * And if the file exist in CWD, delete it
     * if the file is not tracked and is not in Staging Area, exist with the message
     */
    void remove(Path file) throws IOException {
        Path fileInStage = STAGING_AREA.resolve(file.getFileName().toString());
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
        Files.delete(STAGING_AREA.resolve(fileName));
    }

    private void clearStage() throws IOException {
        for (String fileName : plainFilenamesIn(STAGING_AREA))
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
        List<String> cmtIDs = plainFilenamesIn(COMMITS_DIR);
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
        List<String> cmtIDs = plainFilenamesIn(COMMITS_DIR);
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
        for (String fileName : plainFilenamesIn(STAGING_AREA)) {
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
        // TODO fix removal files in modified
        Commit HEAD = getHEADCmt();

        List<String> filesInCWD = plainFilenamesIn(CWD);
        Set<String> filesTracked = HEAD.getAllTrackedFiles();
        List<String> filesStaged = plainFilenamesIn(STAGING_AREA);
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
        if (cmtId.length() < 40) {
            cmtId = getFullCmtId(cmtId);
        }
        Commit cmt = getCmt(cmtId);
        if (cmt == null) {
            throw new GitletException("No commit with that id exists.");
        }
        String fileId = cmt.getFileIDByFileName(fileName);
        if (fileId == null) {
            throw new GitletException("File does not exist in that commit.");
        }
        Blob fileInBlob = getBlobFromRepo(fileId);
        saveToCWD(fileInBlob);
        // Files.write(Paths.get(CWD, fileName), fileInBlob.contents);
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
        List<String> stagedNotInCoutCmtFiles = plainFilenamesIn(STAGING_AREA);

        for (String fileName : coutCmt.getAllTrackedFiles()) {
            String fileId = coutCmt.getFileIDByFileName(fileName);
            Blob fileInRepo = getBlobFromRepo(fileId);
            // Files.write(Paths.get(CWD, fileName), fileInRepo.contents);
            saveToCWD(fileInRepo);

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
        Commit cmt = getCmt(cmdId);
        Commit HEAD = getHEADCmt();

        Set<String> filesInCWD = new HashSet<>(plainFilenamesIn(CWD));
        Set<String> filesTracked = cmt.getAllTrackedFiles();

        for (String fileName : filesTracked) {
            if (filesInCWD.contains(fileName)) {
                // Current CWD contains file tracked by commit specified by cmdId
                String fileInCWDId = new Blob(Paths.get(CWD, fileName)).id;
                String fileStagedId = checkFileExist(STAGING_AREA.resolve(fileName)) ? getBlobFromStage(fileName).id : null;
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

    boolean merge(String branchName) throws IOException {
        String splitPoint = getSplitPointWithOtherBranch(branches.get(branchName));
        if (splitPoint == null) return true;
        Commit currBranchCmt = getCmt(HEAD);
        Commit otherBranchCmt = getCmt(branches.get(branchName));
        Commit splitPointCmt = getCmt(splitPoint);

        Set<String> currBranchFiles = currBranchCmt.getAllTrackedFiles();
        Set<String> otherBranchFiles = otherBranchCmt.getAllTrackedFiles();
        Set<String> splitPointFiles = splitPointCmt.getAllTrackedFiles();
        for (String fileName : splitPointFiles) {
            String fileInSplitId = splitPointCmt.getFileIDByFileName(fileName);
            String fileInCurrId = currBranchCmt.getFileIDByFileName(fileName);
            String fileInOtherId = otherBranchCmt.getFileIDByFileName(fileName);

            boolean isCurrModified = !fileInSplitId.equals(fileInCurrId);
            boolean isOtherModified = !fileInSplitId.equals(fileInOtherId);
            boolean isOtherDeleted = fileInOtherId == null;
            boolean isCurrDeleted = fileInCurrId == null;

            Path fileInCWD = Paths.get(CWD, fileName);
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
            String fileInOtherId = otherBranchCmt.getFileIDByFileName(fileName);
            String fileInCurrId = currBranchCmt.getFileIDByFileName(fileName);
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
        Blob fileInCurrBlob = getBlobFromRepo(id1);
        Blob fileInOtherBlob = getBlobFromRepo(id2);
        String fileName = fileInCurrBlob == null ? fileInOtherBlob.fileName : fileInCurrBlob.fileName;
        Blob mergedBlob = Blob.mergeConflict(fileName, fileInCurrBlob, fileInOtherBlob);
        saveToCWD(mergedBlob);
        // add(fileInCWD);
        saveToStage(mergedBlob);
    }

    private String getSplitPointWithOtherBranch(String otherBranchName) throws IOException {
        Commit currBranchCmt = getCmt(branches.get(currentBranch));
        Commit otherBranchCmt = getCmt(branches.get(otherBranchName));
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
            currBranchCmt = currBranchCmt == null ? null : getCmt(currBranchCmt.getParentId());
            otherBranchCmt = otherBranchCmt == null ? null : getCmt(otherBranchCmt.getParentId());
        }
        return ret;
    }

    /* UTILITIES RELATED TO REPOSITORY */

    static boolean deleteFileInCWD(String fileName) {
        Path path = Paths.get(CWD, fileName);
        if (checkFileExist(path)) {
            restrictedDelete(path.toFile());
            return true;
        }
        return false;
    }

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
