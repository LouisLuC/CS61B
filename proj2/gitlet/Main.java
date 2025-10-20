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
            switch (firstArg) {
                case "init":
                    // Start a Gitlet repository in current work directory
                    validateNumArgs(args, 1, equally);
                    checkGitletInit(false);
                    gitletInit();
                    break;
                case "add":
                    checkGitletInit(true);
                    validateNumArgs(args, 2, equally);
                    String fileName = args[1];
                    if (!checkFileExist(fileName)) {
                        throw new GitletException("File does not exist.");
                    }
                    // Does not consider "./fileName"
                    add(Paths.get(CWD, fileName));
                    break;
                case "commit":
                    checkGitletInit(true);
                    validateNumArgs(args, 2, equally);
                    String message = args[1];
                    if (message.isBlank()) {
                        throw new GitletException("Please enter a commit message.");
                    }
                    commit(message);
                    break;
                case "rm":
                    checkGitletInit(true);
                    validateNumArgs(args, 2, equally);
                    remove(Paths.get(CWD, args[1]));
                    break;
                case "checkout":
                    checkGitletInit(true);
                    handleCheckout(args);
                    break;
                case "branch":
                    checkGitletInit(true);
                    validateNumArgs(args, 2, equally);
                    createBranch(args[1]);
                    break;
                case "merge":
                    checkGitletInit(true);
                    // TODO
                    break;
                case "log":
                    checkGitletInit(true);
                    validateNumArgs(args, 1, equally);
                    log();
                    break;
                case "global-log":
                    checkGitletInit(true);
                    validateNumArgs(args, 1, equally);
                    globalLog();
                    break;
                case "find":
                    checkGitletInit(true);
                    validateNumArgs(args, 2, equally);
                    find(args[1]);
                    break;
                case "status":
                    checkGitletInit(true);
                    validateNumArgs(args, 1, equally);
                    status();
                    break;
                case "rm-branch":
                    checkGitletInit(true);
                    validateNumArgs(args, 2, equally);
                    removeBranch(args[1]);
                    break;
                case "reset":
                    break;
                default:
                    throw new GitletException("No command with that name exists.");
                    // TODO: FILL THE REST IN
            }
        } catch (GitletException e) {
            exitsWithMessage(e.getMessage());
        }
    }

    static void handleCheckout(String[] args) throws IOException {
        switch (args.length) {
            case 2:
                checkoutToBranch(args[1]);
                break;
            case 3:
                checkout(args[2]);
                break;
            case 4:
                checkout(args[3], args[1]);
                break;
            default:
                throw new GitletException("Incorrect operands");
        }
    }
}
