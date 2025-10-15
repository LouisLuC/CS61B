package gitlet;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import static gitlet.Utils.*;

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
        if (args.length == 0) {
            exitsWithMessage("Please enter a command.");
        }
        String firstArg = args[0];
        switch (firstArg) {
            case "init":
                // Start a Gitlet repository in current work directory
                validateNumArgs(args, 1, equally);
                handleInit();
                break;
            case "add":
                checkGitletInit();
                validateNumArgs(args, 2, largerAndEqual);
                List<String> fileNames;
                if (args[1].equals("*") || args[1].equals(".")) {
                    fileNames = Utils.plainFilenamesIn("./");
                } else {
                    fileNames = new ArrayList<>(Arrays.asList(args).subList(1, args.length));
                }
                handleAdd(fileNames);
                break;
            case "commit":
                checkGitletInit();
                validateNumArgs(args, 2, equally);
                String Message = args[1];

                // TODO handle commit
                break;
            case "checkout":
                checkGitletInit();
                validateNumArgs(args, 1, largerAndEqual);
                //TODO handle checkout
                break;
            case "branch":
                checkGitletInit();
                // TODO check nums
                // TODO handle branch
                break;
            case "merge":
                checkGitletInit();
                // TODO
                break;
            default:
                exitsWithMessage("No command with that name exists.");
                // TODO: FILL THE REST IN
        }
        System.exit(1);
    }

    static void handleInit() throws IOException {
        Path gitlet = Paths.get("./.gitlet");
        try {
            // Create .gitlet, and relative files
            Files.createDirectories(gitlet);
            // TODO Create content in .gitlet
        } catch (FileAlreadyExistsException e) {
            // TODO check if message is the same with spec
            exitsWithMessage("Gitlet has already init.");
        }
    }

    static void handleAdd(List<String> fileNames) {
        // TODO: handle the `add *` or `add [file1] [file2]` command
        for(String fileName:fileNames) {
            // TODO check if file changed add changed file
        }
    }

    static void handleCommit(String message) {
        // TODO
        Commit commit = new Commit(message);
        // commit.saveToFile();
    }

    static void checkGitletInit() {
        if(!Utils.checkDirExist("./.gitlet")) {
            // TODO check spec
            exitsWithMessage("Gitlet has not init yet.");
        }
    }

    static BiFunction<Integer, Integer, Boolean> equally = (argsNum, num) -> {
        if (argsNum == num) {
            return true;
        }
        return false;
    };
    static BiFunction<Integer, Integer, Boolean> largerAndEqual = (argsNum, num) -> {
        if (argsNum >= num) {
            return true;
        }
        return false;
    };


}
