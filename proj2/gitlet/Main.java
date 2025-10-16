package gitlet;

import java.io.IOException;
import java.nio.file.Paths;

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
        if (args.length == 0) {
            exitsWithMessage("Please enter a command.");
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
                /** add multible files version
                validateNumArgs(args, 2, largerAndEqual);
                List<String> fileNames;
                if (args[1].equals("*") || args[1].equals(".")) {
                    fileNames = Utils.plainFilenamesIn("./");
                } else {
                    fileNames = new ArrayList<>(Arrays.asList(args).subList(1, args.length));
                }
                handleAdd(fileNames);
                */
                String fileName = args[1];
                if(!Utils.checkFileExist(fileName)) {
                    Utils.exitsWithMessage("File does not exist.");
                }
                add(Paths.get(CWD, fileName));
                break;
            case "commit":
                checkGitletInit(true);
                validateNumArgs(args, 2, equally);
                String Message = args[1];

                // TODO handle commit
                break;
            case "checkout":
                checkGitletInit(true);
                validateNumArgs(args, 1, largerAndEqual);
                //TODO handle checkout
                break;
            case "branch":
                checkGitletInit(true);
                // TODO check nums
                // TODO handle branch
                break;
            case "merge":
                checkGitletInit(true);
                // TODO
                break;
            default:
                exitsWithMessage("No command with that name exists.");
                // TODO: FILL THE REST IN
        }
    }
}
