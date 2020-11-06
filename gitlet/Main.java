package gitlet;


import java.io.File;
import java.io.IOException;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Pranav Bhimani
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) throws IOException {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        } else if (args[0].equals("init")) {
            if (new File(".gitlet").exists()) {
                System.out.println("A Gitlet version-control system "
                        + "already exists in the current directory.");
                return;
            } else {
                Repo.init();
            }
        } else if (!new File(".gitlet").exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        } else if (args[0].equals("add")) {
            Repo.add(args[1]);
        } else if (args[0].equals("commit")) {
            Repo.makeCommit(args[1], null);
        } else if (args[0].equals("log")) {
            Repo.log();
        } else if (args[0].equals("checkout")) {
            if (args[1].equals("--")) {
                Repo.checkoutFile(args[2]);
            } else if (args.length > 2 && args[2].equals("--")) {
                Repo.checkoutCommit(args[1], args[3]);
            } else if (args.length == 2) {
                Repo.checkoutBranch(args[1]);
            } else {
                System.out.println("Incorrect operands.");
                return;
            }
        } else if (args[0].equals("branch")) {
            Repo.branch(args[1]);
        } else if (args[0].equals("status")) {
            Repo.status();
        } else if (args[0].equals("rm")) {
            Repo.remove(args[1]);
        } else if (args[0].equals("global-log")) {
            Repo.globalLog();
        } else if (args[0].equals("find")) {
            Repo.find(args[1]);
        } else if (args[0].equals("rm-branch")) {
            Repo.removeBranch(args[1]);
        } else if (args[0].equals("reset")) {
            Repo.reset(args[1]);
        } else if (args[0].equals("merge")) {
            Repo.merge(args[1]);
        } else {
            System.out.println("No command with that name exists.");
            return;
        }
    }

}
