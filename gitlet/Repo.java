package gitlet;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

/** Class that refers to all the functions of a gitlet system.
 * @author Pranav Bhimani */
public class Repo implements Serializable {

    /** Initializing a git repository in the current directory. */
    protected static void init() throws IOException {
        hidden = new File(".gitlet");
        hidden.mkdir();
        commits = new File(".gitlet/commits");
        commits.mkdir();
        blobs = new File(".gitlet/blobs");
        blobs.mkdir();
        branches = new File(".gitlet/branches");
        branches.mkdir();
        stage = new HashMap<>();
        Utils.writeObject(new File(".gitlet/stage"), stage);
        removeStage = new ArrayList<>();
        Utils.writeObject(new File(".gitlet/removeStage"), removeStage);
        Commit initial = new Commit();
        head = initial.myId();
        Utils.serialize(head);
        Utils.writeObject(new File(".gitlet/head"), head);
        currentBranchName = "master";
        Utils.writeObject(new File(".gitlet/currentBranch"), currentBranchName);
        currentBranch = initial.myId();
        Utils.writeObject(new File(".gitlet/branches/master"), currentBranch);
        File initCommit = new File(".gitlet/commits/" + head);
        initCommit.createNewFile();
        Utils.writeObject(initCommit, initial);
    }
    /** Add file to be staged for commit.
     @param addFile file to be added to staging area.*/
    @SuppressWarnings("unchecked")
    protected static void add(String addFile) throws IOException {
        File added = new File(addFile);
        if (!added.exists()) {
            System.out.println("File does not exist.");
            return;
        } else {
            String sha1added = Utils.sha1(Utils.readContents(added))
                    + Utils.sha1(Utils.serialize(added));
            File blob = new File(".gitlet/blobs/" + sha1added);
            if (!blob.exists()) {
                blob.createNewFile();
                Utils.writeContents(blob, Utils.readContentsAsString(added));
            }
            File headFile = new File(".gitlet/head");
            String recentCommit = Utils.readObject(headFile, String.class);
            File lastcommitFile = new File(".gitlet/commits/" + recentCommit);
            Commit lastCommit = Utils.readObject(lastcommitFile, Commit.class);
            stage = Utils.readObject(new File(".gitlet/stage"), HashMap.class);
            String check = lastCommit.trackedFiles.get(addFile);
            if (stage.containsKey(addFile)
                    && lastCommit.trackedFiles.containsKey(addFile)) {
                if (stage.get(addFile).equals(check)) {
                    stage.remove(addFile);
                }
            }

            if (lastCommit.trackedFiles == null
                    || check == null
                    || !(check.equals(sha1added))) {
                stage.put(addFile, sha1added);
            }

            Utils.writeObject(new File(".gitlet/stage"), stage);
            File stageRemoval = new File(".gitlet/removeStage");
            removeStage = Utils.readObject(stageRemoval, ArrayList.class);
            removeStage.remove(addFile);
            Utils.writeObject(new File(".gitlet/removeStage"), removeStage);
        }

    }
    /** Make a commit.
     @param message the commit message.
     @param mergeParent the second merge parent of the commit.*/
    @SuppressWarnings("unchecked")
    protected static void makeCommit(String message, String mergeParent) {
        File stageFind = new File(".gitlet/stage");
        stage = Utils.readObject(stageFind, HashMap.class);
        File removeFind = new File(".gitlet/removeStage");
        removeStage = Utils.readObject(removeFind, ArrayList.class);
        if (stage.isEmpty() && removeStage.isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }
        if (message.isEmpty()) {
            System.out.println("Please enter a commit message.");
            return;
        }

        File commitFind1 = new File(".gitlet/head");
        String recentCommit = Utils.readObject(commitFind1, String.class);
        File commitFind2 = new File(".gitlet/commits/" + recentCommit);
        Commit lastCommit = Utils.readObject(commitFind2, Commit.class);
        HashMap<String, String> newCommitFiles =
                new HashMap<>(lastCommit.trackedFiles);
        if (!stage.isEmpty()) {
            for (String elem : stage.keySet()) {
                newCommitFiles.put(elem, stage.get(elem));
            }
        }
        if (!removeStage.isEmpty()) {
            for (String elem: removeStage) {
                newCommitFiles.remove(elem);
            }
        }
        Commit nextCommit =
                new Commit(message, recentCommit, newCommitFiles, mergeParent);
        head = nextCommit.myId();
        Utils.writeObject(new File(".gitlet/head"), head);
        Utils.writeObject(new File(".gitlet/commits/" + head), nextCommit);
        File currentBranchFile = new File(".gitlet/currentBranch");
        currentBranchName = Utils.readObject(currentBranchFile, String.class);
        File cbFile = new File(".gitlet/branches/" + currentBranchName);
        Utils.writeObject(cbFile, head);
        stage.clear();
        Utils.writeObject(new File(".gitlet/stage"), stage);
        removeStage.clear();
        Utils.writeObject(new File(".gitlet/removeStage"), removeStage);

    }
    /** Make a commit.
     @param fileName the file to be removed.*/
    @SuppressWarnings("unchecked")
    protected static void remove(String fileName) {
        File stageFind = new File(".gitlet/stage");
        stage = Utils.readObject(stageFind, HashMap.class);
        File commitFind1 = new File(".gitlet/head");
        String recentCommit = Utils.readObject(commitFind1, String.class);
        File commitFind2 = new File(".gitlet/commits/" + recentCommit);
        Commit lastCommit = Utils.readObject(commitFind2, Commit.class);
        File check = new File(fileName);
        if ((stage.isEmpty() || !stage.containsKey(fileName))
                && !lastCommit.trackedFiles.containsKey(fileName)) {
            System.out.println("No reason to remove the file.");
            return;
        }

        if (stage.containsKey(fileName)) {
            stage.remove(fileName);
        }

        if (lastCommit.trackedFiles.containsKey(fileName)) {
            File removeFind = new File(".gitlet/removeStage");
            removeStage = Utils.readObject(removeFind, ArrayList.class);
            removeStage.add(fileName);
            String cwd = System.getProperty("user.dir");
            if (Utils.plainFilenamesIn(cwd).contains(fileName)) {
                check.delete();
            }
            Utils.writeObject(new File(".gitlet/removeStage"), removeStage);
        }
        Utils.writeObject(new File(".gitlet/stage"), stage);
    }

    /** Prints out the current log in the branch.*/
    protected static void log() {
        File headgetter = new File(".gitlet/head");
        String temp = Utils.readObject(headgetter, String.class);

        while (temp != null) {
            File commitGetter = new File(".gitlet/commits/" + temp);
            Commit lastCommit = Utils.readObject(commitGetter, Commit.class);
            lastCommit.printLogMessage();
            temp = lastCommit.getparent();
        }
    }

    /** Prints out all commits made since initialization.*/
    protected static void globalLog() {
        ArrayList<String> allCommits =
                new ArrayList<>(Utils.plainFilenamesIn(".gitlet/commits"));
        for (String commitSha1: allCommits) {
            File commitGetter = new File(".gitlet/commits/" + commitSha1);
            Commit lastCommit = Utils.readObject(commitGetter, Commit.class);
            lastCommit.printLogMessage();
        }

    }

    /** Finds a commit with the given message.
     @param message the commit message to look for.*/
    protected static void find(String message) {
        ArrayList<String> allCommits =
                new ArrayList<>(Utils.plainFilenamesIn(".gitlet/commits"));
        boolean error = true;
        for (String commitSha1: allCommits) {
            File commitGetter = new File(".gitlet/commits/" + commitSha1);
            Commit lastCommit = Utils.readObject(commitGetter, Commit.class);
            if (lastCommit.getmessage().equals(message)) {
                System.out.println(commitSha1);
                error = false;
            }
        }

        if (error) {
            System.out.println("Found no commit with that message.");
            return;
        }


    }
    /** Replaces file to most recent committed version.
     @param revert the file to be reverted.*/
    protected static void checkoutFile(String revert) {
        File headGetter = new File(".gitlet/head");
        String recentCommit = Utils.readObject(headGetter, String.class);
        File commitGetter = new File(".gitlet/commits/" + recentCommit);
        Commit lastCommit = Utils.readObject(commitGetter, Commit.class);
        if (!lastCommit.trackedFiles.containsKey(revert)) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        String sha1Revert = lastCommit.trackedFiles.get(revert);
        File reverted = new File(".gitlet/blobs/" + sha1Revert);
        File torevert = new File(revert);
        Utils.writeContents(torevert, Utils.readContentsAsString(reverted));
    }

    /** Replaces file to one in the given commit.
     @param fileName the file to be reverted.
     @param commitID the commit to check for the given file.*/
    protected static void checkoutCommit(String commitID, String fileName) {
        File olderCommit = null;
        for (String name: Utils.plainFilenamesIn(".gitlet/commits")) {
            if (name.contains(commitID)) {
                olderCommit = new File(".gitlet/commits/" + name);
            }
        }

        if (olderCommit == null) {
            System.out.println("No commit with that id exists.");
            return;
        }

        Commit oldCommit = Utils.readObject(olderCommit, Commit.class);
        if (!oldCommit.trackedFiles.containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            return;
        }

        String sha1Revert = oldCommit.trackedFiles.get(fileName);
        File reverted = new File(".gitlet/blobs/" + sha1Revert);
        File torevert = new File(fileName);
        Utils.writeContents(torevert, Utils.readContentsAsString(reverted));
    }
    /** Replaces all files in working directory to match
     * those in front of the given branch.
     @param branch the branch to be analyzed.*/
    @SuppressWarnings("unchecked")
    protected static void checkoutBranch(String branch) {
        File currBranch = new File(".gitlet/currentBranch");
        currentBranchName = Utils.readObject(currBranch, String.class);
        if (!Utils.plainFilenamesIn(".gitlet/branches").contains(branch)) {
            System.out.println("No such branch exists.");
            return;
        }
        if (currentBranchName.equals(branch)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        File branchfile = new File(".gitlet/branches/" + branch);
        String branchCommitSha1 = Utils.readObject(branchfile, String.class);

        File commitFile = new File(".gitlet/commits/" + branchCommitSha1);
        Commit branchCommit = Utils.readObject(commitFile, Commit.class);
        File cbFinder = new File(".gitlet/branches/" + currentBranchName);
        String oldBranchCommitsha1 = Utils.readObject(cbFinder, String.class);
        File branchFile = new File(".gitlet/commits/" + oldBranchCommitsha1);
        Commit oldBranchCommit = Utils.readObject(branchFile, Commit.class);
        String property = System.getProperty("user.dir");
        List<String> cwdList = Utils.plainFilenamesIn(property);
        for (String file: cwdList) {
            if (!oldBranchCommit.trackedFiles.containsKey(file)
                    && branchCommit.trackedFiles.containsKey(file)) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                return;
            }
        }
        for (String elem: branchCommit.trackedFiles.keySet()) {
            String sha1Revert = branchCommit.trackedFiles.get(elem);
            File reverted = new File(".gitlet/blobs/" + sha1Revert);
            File toRevert = new File(elem);
            Utils.writeContents(toRevert, Utils.readContentsAsString(reverted));
        }
        for (String elem: oldBranchCommit.trackedFiles.keySet()) {
            if (!branchCommit.trackedFiles.containsKey(elem)) {
                File toDelete = new File(elem);
                toDelete.delete();
            }
        }
        currentBranchName = branch;
        head = branchCommitSha1;
        Utils.writeObject(new File(".gitlet/head"), head);
        Utils.writeObject(new File(".gitlet/currentBranch"), currentBranchName);
        stage = Utils.readObject(new File(".gitlet/stage"), HashMap.class);
        stage.clear();
        Utils.writeObject(new File(".gitlet/stage"), stage);
    }
    /** Creates a new branch pointing to current commit.
     @param newBranch the name of the new branch.*/
    protected static void branch(String newBranch) {
        File checkbranch = new File(".gitlet/branches/" + newBranch);
        if (checkbranch.exists()) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        File getHead = new File(".gitlet/head");
        String recentCommit = Utils.readObject(getHead, String.class);
        Utils.writeObject(checkbranch, recentCommit);
    }

    /** Deletes the given branch.
     @param oldBranch the name of branch to be deleted. */
    protected static void removeBranch(String oldBranch) {
        File checkbranch = new File(".gitlet/branches/" + oldBranch);
        if (!checkbranch.exists()) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        File currentBranchGet = new File(".gitlet/currentBranch");
        currentBranchName = Utils.readObject(currentBranchGet, String.class);
        if (currentBranchName.equals(oldBranch)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        checkbranch.delete();
    }

    /** Resets the system to the given commit.
     @param commitID the sha-1 of commit to be reverted to. */
    @SuppressWarnings("unchecked")
    protected static void reset(String commitID) {
        File check = null;
        for (String name: Utils.plainFilenamesIn(".gitlet/commits")) {
            if (name.contains(commitID)) {
                check = new File(".gitlet/commits/" + name);
            }
        }

        if (check == null) {
            System.out.println("No commit with that id exists");
            return;
        }

        Commit givenCommit = Utils.readObject(check, Commit.class);
        File headcheck = new File(".gitlet/head");
        String recentCommit = Utils.readObject(headcheck, String.class);
        File commitCheck = new File(".gitlet/commits/" + recentCommit);
        Commit currentCommit = Utils.readObject(commitCheck, Commit.class);
        String property = System.getProperty("user.dir");
        List<String> cwdList = Utils.plainFilenamesIn(property);
        for (String file: cwdList) {
            if (!currentCommit.trackedFiles.containsKey(file)
                    && givenCommit.trackedFiles.containsKey(file)) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                return;
            }
        }

        for (String file: givenCommit.trackedFiles.keySet()) {
            checkoutCommit(commitID, file);
        }

        for (String elem: currentCommit.trackedFiles.keySet()) {
            if (!givenCommit.trackedFiles.containsKey(elem)) {
                File toDelete = new File(elem);
                toDelete.delete();
            }
        }

        Utils.writeObject(new File(".gitlet/head"), commitID);
        File currentFile = new File(".gitlet/currentBranch");
        currentBranchName = Utils.readObject(currentFile, String.class);
        File cbFile = new File(".gitlet/branches/" + currentBranchName);
        Utils.writeObject(cbFile, commitID);
        stage = Utils.readObject(new File(".gitlet/stage"), HashMap.class);
        stage.clear();
        Utils.writeObject(new File(".gitlet/stage"), stage);
    }

    /** Prints out the current status of the version-control system. */
    @SuppressWarnings("unchecked")
    protected static void status() {
        System.out.println("=== Branches ===");
        File currentGet = new File(".gitlet/currentBranch");
        currentBranchName = Utils.readObject(currentGet, String.class);
        for (String branch : Utils.plainFilenamesIn(".gitlet/branches")) {
            if (branch.equals(currentBranchName)) {
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }
        System.out.println();
        modNotStaged = new ArrayList<String>();
        stage = Utils.readObject(new File(".gitlet/stage"), HashMap.class);
        File headGet = new File(".gitlet/head");
        head = Utils.readObject(headGet, String.class);
        File headCommit = new File(".gitlet/commits/" + head);
        Commit current = Utils.readObject(headCommit, Commit.class);
        File stageRemove = new File(".gitlet/removeStage");
        removeStage = Utils.readObject(stageRemove, ArrayList.class);
        for (String fileName: current.trackedFiles.keySet()) {
            File check = new File(fileName);
            if (!check.exists()) {
                modNotStaged.add(fileName + " (deleted)");
            } else {
                String sha1inCWD = Utils.sha1(Utils.readContents(check))
                        + Utils.sha1(Utils.serialize(check));
                if (!sha1inCWD.equals(current.trackedFiles.get(fileName))) {
                    modNotStaged.add(fileName + " (modified)");
                }
            }
        }
        if (!stage.isEmpty()) {
            for (String fileName : stage.keySet()) {
                File check = new File(fileName);
                if (!check.exists()
                        && !modNotStaged.contains(fileName + " (deleted)")) {
                    modNotStaged.add(fileName + " (deleted)");
                } else {
                    String sha1inCWD = Utils.sha1(Utils.readContents(check))
                            + Utils.sha1(Utils.serialize(new File(fileName)));
                    if (!sha1inCWD.equals(stage.get(fileName))
                            && !modNotStaged.contains(fileName
                            + " (modified)")) {
                        modNotStaged.add(fileName + " (modified)");
                    }
                }
            }
        }
        Collections.sort(modNotStaged);
        System.out.println("=== Staged Files ===");
        if (!stage.isEmpty()) {
            ArrayList<String> staged = new ArrayList<String>(stage.keySet());
            Collections.sort(staged);
            for (String fileName : staged) {
                System.out.println(fileName);
            }
        }
        statusEC(current);
    }
    /** Second part of status for more clarity.
     * @param current takes in the current commit. */
    protected static void statusEC(Commit current) {
        System.out.println();
        System.out.println("=== Removed Files ===");
        if (!removeStage.isEmpty()) {
            Collections.sort(removeStage);
            for (String fileName: removeStage) {
                System.out.println(fileName);
            }
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        if (!modNotStaged.isEmpty()) {
            for (String fileName : modNotStaged) {
                if (removeStage.isEmpty()
                        || (!removeStage.contains(
                        fileName.replace(" (deleted)", ""))
                        && !removeStage.contains(
                        fileName.replace(" (modified)", "")))) {
                    System.out.println(fileName);
                }
            }
        }
        System.out.println();
        System.out.println("=== Untracked Files ===");
        Set<String> allFilesTracked = new HashSet<>();
        if (!stage.isEmpty()) {
            allFilesTracked.addAll(stage.keySet());
        }
        allFilesTracked.addAll(current.trackedFiles.keySet());
        String cwd = System.getProperty("user.dir");
        for (String file: Utils.plainFilenamesIn(cwd)) {
            if (!allFilesTracked.contains(file)) {
                System.out.println(file);
            }
        }
        System.out.println();
    }
    /** Merges two branches together and create a new merge commit.
     * But this one only accounts for the error cases.
     @param branch the branch to be merged into. */
    @SuppressWarnings("unchecked")
    protected static void merge(String branch) throws IOException {
        File getCbName = new File(".gitlet/currentBranch");
        currentBranchName = Utils.readObject(getCbName, String.class);
        File getCb = new File(".gitlet/branches/" + currentBranchName);
        String currsha1 = Utils.readObject(getCb, String.class);
        File getCurrentCom = new File(".gitlet/commits/" + currsha1);
        Commit currentCommit = Utils.readObject(getCurrentCom, Commit.class);
        File givenBranch = new File(".gitlet/branches/" + branch);
        if (!givenBranch.exists()) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (currentBranchName.equals(branch)) {
            System.out.println("Cannot merge a branch with itself");
            return;
        }
        String givenCommitsha1 = Utils.readObject(givenBranch, String.class);
        File getGiveCommit = new File(".gitlet/commits/" + givenCommitsha1);
        Commit givenCommit = Utils.readObject(getGiveCommit, Commit.class);
        head = Utils.readObject(new File(".gitlet/head"), String.class);
        String temp = head;
        while (temp != null) {
            File prevCommit = new File(".gitlet/commits/" + temp);
            Commit lastCommit = Utils.readObject(prevCommit, Commit.class);
            if (temp.equals(givenCommitsha1)) {
                System.out.println("Given branch is an ancestor "
                        + "of the current branch.");
                return;
            }
            temp = lastCommit.getparent();
        }
        temp = givenCommitsha1;
        while (temp != null) {
            File prevCommit = new File(".gitlet/commits/" + temp);
            Commit lastCommit = Utils.readObject(prevCommit, Commit.class);
            if (temp.equals(currsha1)) {
                checkoutBranch(branch);
                System.out.println("Current branch fast-forwarded.");
                return;
            }
            temp = lastCommit.getparent();
        }
        stage = Utils.readObject(new File(".gitlet/stage"), HashMap.class);
        File getRemove = new File(".gitlet/removeStage");
        removeStage = Utils.readObject(getRemove, ArrayList.class);
        if (!stage.isEmpty() || !removeStage.isEmpty()) {
            System.out.println("You have uncommited changes");
            return;
        }
        String property = System.getProperty("user.dir");
        for (String file: Utils.plainFilenamesIn(property)) {
            if (!currentCommit.trackedFiles.containsKey(file)
                    && givenCommit.trackedFiles.containsKey(file)) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                return;
            }
        }
        doMerge(currsha1, givenCommitsha1, givenCommit, currentCommit, branch);
    }
    /** Actually does the merge.
     * @param currentCommitsha1 sha1 of current commit.
     * @param givenCommitsha1 sha1 of given commit.
     * @param givenCommit the given commit.
     * @param currentCommit the current commit.
     * @param branch the given branch.*/
    private static void doMerge(
            String currentCommitsha1, String givenCommitsha1,
            Commit givenCommit, Commit currentCommit,
            String branch) throws IOException {
        Commit splitPoint = findSplit(currentCommitsha1, givenCommitsha1);
        for (String file: splitPoint.trackedFiles.keySet()) {
            if (givenCommit.trackedFiles.containsKey(file)
                    && currentCommit.trackedFiles.containsKey(file)) {
                String split = splitPoint.trackedFiles.get(file);
                if (!givenCommit.trackedFiles.containsKey(file)
                        && !currentCommit.trackedFiles.containsKey(file)) {
                    continue;
                }
                String given = givenCommit.trackedFiles.get(file);
                String current = currentCommit.trackedFiles.get(file);
                if (!split.equals(given) && split.equals(current)) {
                    checkoutCommit(givenCommitsha1, file);
                    add(file);
                }
            }
        }
        for (String file: givenCommit.trackedFiles.keySet()) {
            if (!splitPoint.trackedFiles.containsKey(file)
                    && !currentCommit.trackedFiles.containsKey(file)) {
                checkoutCommit(givenCommitsha1, file);
                add(file);
            }
        }
        for (String file: splitPoint.trackedFiles.keySet()) {
            String split = splitPoint.trackedFiles.get(file);
            if (currentCommit.trackedFiles.containsKey(file)) {
                String current = currentCommit.trackedFiles.get(file);
                if (split.equals(current)
                        && !givenCommit.trackedFiles.containsKey(file)) {
                    remove(file);
                }
            }
        }
        mergeConflict(givenCommit, currentCommit, splitPoint);
        makeCommit("Merged " + branch + " into "
                + currentBranchName + ".", givenCommitsha1);
    }
    /** Find the split point.
     * @param currentCommitsha1 sha1 of current commit.
     * @param givenCommitsha1 sha1 of given commit.
     * @return splitpoint where the branches split.*/
    private static Commit findSplit(String currentCommitsha1,
                                    String givenCommitsha1) {
        Commit splitPoint = null;
        HashMap<String, Integer> allCurrentCommits =
                currentMergeHelper(currentCommitsha1,
                        new HashMap<String, Integer>(), 0);
        ArrayList<String> allGivenCommits =
                givenMergeHelper(givenCommitsha1, new ArrayList<String>());
        HashMap<String, Integer> editedCommits = new HashMap<>();
        for (String elem: allCurrentCommits.keySet()) {
            if (allGivenCommits.contains(elem)) {
                editedCommits.put(elem, allCurrentCommits.get(elem));
            }
        }
        int smallestDistance = Collections.min(editedCommits.values());
        for (String elem: editedCommits.keySet()) {
            if (editedCommits.get(elem) == smallestDistance) {
                File splitFile = new File(".gitlet/commits/" + elem);
                splitPoint = Utils.readObject(splitFile, Commit.class);
            }
        }
        return splitPoint;
    }
    /** Actually does the merge.
     * @param splitPoint the commit where branches split.
     * @param givenCommit the given commit.
     * @param currentCommit the current commit.*/
    private static void mergeConflict(
            Commit givenCommit, Commit currentCommit, Commit splitPoint)
            throws IOException {
        boolean merged = false;
        for (String file: givenCommit.trackedFiles.keySet()) {
            File overwrite = new File(file);
            String givenblobsha1 = givenCommit.trackedFiles.get(file);
            File readFile = new File(".gitlet/blobs/" + givenblobsha1);
            String givenWriting = Utils.readContentsAsString(readFile);
            if (splitPoint.trackedFiles.containsKey(file)
                    && !currentCommit.trackedFiles.containsKey(file)) {
                String overWriting = "<<<<<<< HEAD\n=======\n"
                        + givenWriting + ">>>>>>>\n";
                if (overwrite.exists()) {
                    Utils.writeContents(overwrite, overWriting);
                    add(file);
                    merged = true;
                }
            } else if (currentCommit.trackedFiles.containsKey(file)) {
                String curblobsha1 = currentCommit.trackedFiles.get(file);
                if (!givenblobsha1.equals(curblobsha1)) {
                    File currentFile = new File(".gitlet/blobs/" + curblobsha1);
                    String writeCur = Utils.readContentsAsString(currentFile);
                    String overWriting = "<<<<<<< HEAD\n" + writeCur
                            + "=======\n" + givenWriting + ">>>>>>>\n";
                    if (overwrite.exists()) {
                        Utils.writeContents(overwrite, overWriting);
                        add(file);
                        merged = true;
                    }

                }
            }
        }
        for (String file: currentCommit.trackedFiles.keySet()) {
            File overwrite = new File(file);
            if (splitPoint.trackedFiles.containsKey(file)
                    && !givenCommit.trackedFiles.containsKey(file)) {
                String currentblobsha1 = currentCommit.trackedFiles.get(file);
                File currentFile = new File(".gitlet/blobs/" + currentblobsha1);
                String currentWriting = Utils.readContentsAsString(currentFile);
                String overWriting = "<<<<<<< HEAD\n" + currentWriting
                        + "=======\n>>>>>>>\n";
                if (overwrite.exists()) {
                    Utils.writeContents(overwrite, overWriting);
                    add(file);
                    merged = true;
                }
            }
        }
        if (merged) {
            System.out.println("Encountered a merge conflict.");
        }
    }


    /** Helper for merge to find all the previous commits of current commit.
     @param commitsha1 the sha-1 of current commit.
     @param map the hashmap to store commits
     and their distance from the current.
     @param depth the distance from the current commit.
     @return map1 the hashmap with all commits
     and their distance from the current.*/
    private static HashMap<String, Integer> currentMergeHelper(
            String commitsha1, HashMap<String, Integer> map, int depth) {
        if (commitsha1 == null) {
            return map;
        } else {
            File checkCom = new File(".gitlet/commits/" + commitsha1);
            Commit check = Utils.readObject(checkCom, Commit.class);
            if (!map.containsKey(commitsha1)) {
                map.put(commitsha1, depth);
            }
            HashMap<String, Integer> map1 =
                    currentMergeHelper(check.getparent(), map, depth + 1);
            HashMap<String, Integer> map2 =
                    currentMergeHelper(check.getparent2(), map, depth + 1);
            map1.putAll(map2);
            return map1;
        }
    }

    /** Helper for merge to find all the previous commits of given commit.
     @param commitsha1 the sha-1 of current commit.
     @param list the arraylist to store previous commits from the given.
     @return list the arraylist storing all previous commits from given.*/
    private static ArrayList<String> givenMergeHelper(
            String commitsha1, ArrayList<String> list) {
        if (commitsha1 == null) {
            return list;
        } else {
            File checkCommit = new File(".gitlet/commits/" + commitsha1);
            Commit check = Utils.readObject(checkCommit, Commit.class);
            list.add(commitsha1);
            ArrayList<String> list1 =
                    givenMergeHelper(check.getparent(), list);
            ArrayList<String> list2 =
                    givenMergeHelper(check.getparent2(), list);
            list1.addAll(list2);
            return list1;
        }
    }

    /** New .gitlet directory.*/
    private static File hidden;
    /** Directory storing all commits.*/
    private static File commits;
    /** Directory storing all the contents of each working file.*/
    private static File blobs;
    /** Directory storing all the branches.*/
    private static File branches;
    /** The current head pointer or the current commit.*/
    private static String head;
    /** The sha-1 of the current branch.*/
    private static String currentBranch;
    /** The name of the current branch.*/
    private static String currentBranchName;
    /** Stage storing all files and contents staged for addition.*/
    private static HashMap<String, String> stage;
    /** Remove stage storing all files staged for removal.*/
    private static ArrayList<String> removeStage;
    /** Helper arraylist for EC portion of status.*/
    private static ArrayList<String> modNotStaged;
}


