package gitlet;

import javax.naming.InitialContext;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.BiFunction;

import static gitlet.Utils.*;
import static gitlet.Repository.*;

/**
 * Driver class for Gitlet, a subset of the Git version-control system.
 *
 * @author Louis Lu
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) throws IOException {
        try {
            if (args.length == 0) {
                throw new GitletException("Please enter a command.");
            }
            String firstArg = args[0];
            Repository repo;
            switch (firstArg) {
                case "init":
                    // Start a Gitlet repository in current work directory
                    validateNumArgs(args, 1, equally);
                    checkGitletInit(false);
                    repo = new Repository();
                    saveState(repo);
                    break;
                case "add":
                    checkGitletInit(true);
                    validateNumArgs(args, 2, equally);
                    repo = loadState();
                    String fileName = args[1];
                    if (!checkFileExist(fileName)) {
                        throw new GitletException("File does not exist.");
                    }
                    // Does not consider "./fileName"
                    repo.add(Paths.get(CWD, fileName));
                    saveState(repo);
                    break;
                case "commit":
                    checkGitletInit(true);
                    validateNumArgs(args, 2, equally);
                    repo = loadState();
                    String message = args[1];
                    if (message.isBlank()) {
                        throw new GitletException("Please enter a commit message.");
                    }
                    repo.commit(message);
                    saveState(repo);
                    break;
                case "rm":
                    checkGitletInit(true);
                    validateNumArgs(args, 2, equally);
                    repo = loadState();
                    repo.remove(Paths.get(CWD, args[1]));
                    saveState(repo);
                    break;
                case "checkout":
                    checkGitletInit(true);
                    repo = loadState();
                    handleCheckout(args, repo);
                    saveState(repo);
                    break;
                case "branch":
                    checkGitletInit(true);
                    validateNumArgs(args, 2, equally);
                    repo = loadState();
                    repo.createBranch(args[1]);
                    saveState(repo);
                    break;
                case "merge":
                    checkGitletInit(true);
                    validateNumArgs(args, 2, equally);
                    repo = loadState();
                    repo.merge(args[1]);
                    saveState(repo);
                    break;
                case "log":
                    checkGitletInit(true);
                    validateNumArgs(args, 1, equally);
                    repo = loadState();
                    repo.log();
                    break;
                case "global-log":
                    checkGitletInit(true);
                    validateNumArgs(args, 1, equally);
                    repo = loadState();
                    repo.globalLog();
                    break;
                case "find":
                    checkGitletInit(true);
                    validateNumArgs(args, 2, equally);
                    repo = loadState();
                    repo.find(args[1]);
                    break;
                case "status":
                    checkGitletInit(true);
                    validateNumArgs(args, 1, equally);
                    repo = loadState();
                    repo.status();
                    break;
                case "rm-branch":
                    checkGitletInit(true);
                    validateNumArgs(args, 2, equally);
                    repo = loadState();
                    repo.removeBranch(args[1]);
                    saveState(repo);
                    break;
                case "reset":
                    checkGitletInit(true);
                    validateNumArgs(args, 2, equally);
                    repo = loadState();
                    repo.reset(args[1]);
                    saveState(repo);
                    break;
                default:
                    throw new GitletException("No command with that name exists.");
            }
        } catch (GitletException e) {
            exitsWithMessage(e.getMessage());
        }
    }

    static void handleCheckout(String[] args, Repository repo) throws IOException {
        switch (args.length) {
            case 2:
                repo.checkoutToBranch(args[1]);
                break;
            case 3:
                repo.checkout(args[2]);
                break;
            case 4:
                repo.checkout(args[3], args[1]);
                break;
            default:
                throw new GitletException("Incorrect operands");
        }
    }
}
