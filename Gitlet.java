package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Ved Mistry
 */
public class Gitlet {
    /** Current Working Directory. */
    static final File CWD = new File(".");
    /** Repo folder/file. */
    static final File COMMITTING =
            new File(Utils.join(".gitlet"), "committing");
    /** Repo folder/file. */
    static final File HEAD =
            new File(Utils.join(".gitlet", "committing"),
                    "head");
    /** Repo folder/file. */
    static final File BRANCHES =
            new File(Utils.join(".gitlet", "committing"),
                    "branches");
    /** Repo folder/file. */
    static final File CURRENTBRANCH =
            new File(Utils.join(".gitlet", "committing"),
                    "currentbranch");
    /** Repo folder/file. */
    static final File COMMITS =
            new File(Utils.join(".gitlet", "committing"),
                    "commits");
    /** Repo folder/file. */
    static final File STAGING =
            new File(Utils.join(".gitlet"), "staging");
    /** Repo folder/file. */
    static final File ADD =
            new File(Utils.join(".gitlet", "staging"),
                    "add");
    /** Repo folder/file. */
    static final File REMOVE =
            new File(Utils.join(".gitlet", "staging"),
                    "remove");
    /** Repo folder/file. */
    static final File BLOBS =
            new File(Utils.join(".gitlet"),
                    "blobs");
    /** Repo folder/file. */
    static final File GLOBALLOG =
            new File(Utils.join(".gitlet"),
                    "log");

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public Gitlet() {
    }

    /** @return Boolean to determine if repository is initialized */
    public Boolean initTest() {
        return Main.GITLET.exists();
    }

    public void init() {
        if (Main.GITLET.exists()) {
            System.out.println("A Gitlet version-control "
                    + "system already exists in the current directory.");
            return;
        }
        Main.GITLET.mkdir();
        COMMITTING.mkdir();
        try {
            HEAD.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            BRANCHES.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            CURRENTBRANCH.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            COMMITS.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        STAGING.mkdir();
        try {
            ADD.createNewFile();
            REMOVE.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Utils.writeObject(ADD, new TreeMap<String, String>());
        Utils.writeObject(REMOVE, new TreeMap<String, String>());
        BLOBS.mkdir();
        TreeMap<String, String> initial = new TreeMap<>();
        Commit initCommit = new Commit(null, "initial commit", initial);
        String initHash = initCommit.hash();
        TreeMap<String, Commit> commitTree = new TreeMap<>();
        commitTree.put(initCommit.hash(), initCommit);
        Utils.writeObject(HEAD, initHash);
        TreeMap<String, String> branches = new TreeMap<>();
        branches.put("master", initHash);
        Utils.writeObject(BRANCHES, branches);
        Utils.writeObject(CURRENTBRANCH, "master");
        Utils.writeObject(COMMITS, commitTree);
        try {
            GLOBALLOG.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Utils.writeObject(GLOBALLOG, "===" + "\n" + "commit "
                + initHash + "\n" + "Date: " + initCommit.timestamp()
                + "\n" + initCommit.logmessage());
    }

    public void addfile(String[] args) {
        @SuppressWarnings("unchecked") TreeMap<String, String> toAdd
                = (TreeMap<String, String>) Utils
                .readObject(ADD, TreeMap.class);
        String parent = Utils.readObject(HEAD, String.class);
        @SuppressWarnings("unchecked") TreeMap<String, Commit> commitTree
                = (TreeMap<String, Commit>) Utils.readObject(COMMITS,
                TreeMap.class);
        @SuppressWarnings("unchecked") TreeMap<String, String> toRemove
                = (TreeMap<String, String>) Utils
                .readObject(REMOVE, TreeMap.class);
        Commit commit = commitTree.get(parent);
        for (int i = 1; i < args.length; i++) {
            if (toRemove.keySet().contains(args[i])) {
                toRemove.remove(args[i]);
                continue;
            }
            if (!Utils.join(args[i]).exists()) {
                System.out.println("File does not exist.");
                return;
            }
            File updated = new File(args[i]);
            Blob blobby = new Blob(updated);
            if (blobby.hash().equals(commit.getfiles().get(args[i]))) {
                continue;
            }
            File toAddFile = Utils.join(BLOBS, blobby.hash());
            Utils.writeContents(toAddFile, Utils.readContents(blobby.file()));
            toAdd.put(args[i], blobby.hash());
        }
        Utils.writeObject(ADD, toAdd);
        Utils.writeObject(REMOVE, toRemove);
    }

    public void removefile(String[] args) {
        @SuppressWarnings("unchecked") TreeMap<String, String> toRemove
                = (TreeMap<String, String>) Utils
                .readObject(REMOVE, TreeMap.class);
        @SuppressWarnings("unchecked") TreeMap<String, String> toAdd
                = (TreeMap<String, String>) Utils
                .readObject(ADD, TreeMap.class);
        String parent = Utils.readObject(HEAD, String.class);
        @SuppressWarnings("unchecked") TreeMap<String, Commit> commitTree
                = (TreeMap<String, Commit>) Utils.readObject(COMMITS,
                TreeMap.class);
        Commit commit = commitTree.get(parent);


        for (int i = 1; i < args.length; i++) {
            Boolean didsmth = false;
            if (toAdd.keySet().contains(args[i])) {
                didsmth = true;
                toAdd.remove(args[i]);
            }
            if (commit.getfiles().keySet().contains(args[i])) {
                didsmth = true;
                toRemove.put(args[i], commit.getfiles().get(args[i]));
                Utils.join(args[i]).delete();
            }
            if (!didsmth) {
                System.out.println("No reason to remove the file.");
                return;
            }
        }
        Utils.writeContents(ADD, Utils.serialize(toAdd));
        Utils.writeContents(REMOVE, Utils.serialize(toRemove));
    }

    public void commit(String[] args) {
        String parent = Utils.readObject(HEAD, String.class);
        String currentBranch = Utils.readObject(CURRENTBRANCH, String.class);
        @SuppressWarnings("unchecked") TreeMap<String, String> toAdd
                = (TreeMap<String, String>) Utils
                .readObject(ADD, TreeMap.class);
        @SuppressWarnings("unchecked") TreeMap<String, String> toRemove
                = (TreeMap<String, String>) Utils
                .readObject(REMOVE, TreeMap.class);
        @SuppressWarnings("unchecked") TreeMap<String, Commit> commitTree
                = (TreeMap<String, Commit>) Utils
                .readObject(COMMITS, TreeMap.class);
        @SuppressWarnings("unchecked") TreeMap<String, String> previousFiles
                = (TreeMap<String, String>) commitTree
                .get(parent).getfiles().clone();
        @SuppressWarnings("unchecked") TreeMap<String, String> branches
                = (TreeMap<String, String>)
                Utils.readObject(BRANCHES, TreeMap.class);
        if (toAdd.isEmpty() && toRemove.isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }
        Object[] keyarray = previousFiles.keySet().toArray();
        for (Object s : keyarray) {
            if (toRemove.keySet().contains((String) s)) {
                previousFiles.remove((String) s);
                toRemove.remove(s);
            }
        }

        previousFiles.putAll(toAdd);

        if (args.length == 1 || args[1].length() == 0) {
            System.out.println("Please enter a commit message.");
            return;
        }

        Commit newCommit = new Commit(parent, args[1], previousFiles);

        String hash = newCommit.hash();

        commitTree.put(hash, newCommit);

        Utils.writeObject(GLOBALLOG, "===" + "\n" + "commit "
                + hash + "\n" + "Date: " + newCommit.timestamp()
                + "\n" + newCommit.logmessage() + "\n" + "\n"
                + Utils.readObject(GLOBALLOG, String.class));

        Utils.writeObject(ADD, new TreeMap<String, String>());
        Utils.writeObject(REMOVE, new TreeMap<String, String>());

        Utils.writeObject(HEAD, hash);
        branches.put(currentBranch, hash);
        Utils.writeObject(BRANCHES, branches);
        Utils.writeObject(COMMITS, commitTree);

    }

    public void log() {
        @SuppressWarnings("unchecked") TreeMap<String, Commit> commitTree
                = (TreeMap<String, Commit>) Utils
                .readObject(COMMITS, TreeMap.class);
        String hash = Utils.readObject(HEAD, String.class);

        Commit node = commitTree.get(hash);
        System.out.print("===" + "\n" + "commit " + node.hash()
                + "\n" + "Date: " + node.timestamp() + "\n"
                + node.logmessage() + "\n");
        hash = node.parent();

        while (hash != null) {
            node = commitTree.get(hash);
            System.out.print("\n" + "===" + "\n" + "commit " + node.hash()
                    + "\n" + "Date: " + node.timestamp() + "\n"
                    + node.logmessage() + "\n");
            hash = node.parent();
        }

    }

    public void globalLog() {
        System.out.println(Utils.readObject(GLOBALLOG, String.class));
    }

    public void fileCheckout(String[] args) {
        String parent = Utils.readObject(HEAD, String.class);
        @SuppressWarnings("unchecked") TreeMap<String, Commit> commitTree
                = (TreeMap<String, Commit>) Utils
                .readObject(COMMITS, TreeMap.class);
        Commit commit = commitTree.get(parent);
        if (!commit.getfiles().keySet().contains(args[2])) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        String blobhash = commit.getfiles().get(args[2]);
        File blobfile = new File(".gitlet/blobs", blobhash);
        Utils.writeContents(Utils.join(args[2]), Utils.readContents(blobfile));
    }

    public void commitCheckout(String[] args) {
        String parent = args[1];
        @SuppressWarnings("unchecked") TreeMap<String, Commit> commitTree
                = (TreeMap<String, Commit>) Utils
                .readObject(COMMITS, TreeMap.class);
        Commit commit = commitTree.get(parent);
        Boolean test = false;
        int ind = 0;
        if (commit == null) {
            Object[] keys = commitTree.keySet().toArray();
            for (int i = 0; i < keys.length; i++) {
                if (((String) keys[i]).contains(parent)) {
                    for (int j = 0; j < parent.length(); j++) {
                        if (((String) keys[i]).charAt(j)
                                != (parent.charAt(j))) {
                            System.out.println("No "
                                    + "commit with that id exists.");
                            return;
                        }
                        if (j == parent.length() - 1) {
                            test = true;
                            ind = i;
                        }
                    }
                }
            }

            if (test) {
                commit = commitTree.get(keys[ind]);
            } else {
                System.out.println("No commit with that id exists.");
                return;
            }
        }
        if (!commit.getfiles().keySet().contains(args[3])) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        String blobhash = commit.getfiles().get(args[3]);
        File blobfile = new File(".gitlet/blobs", blobhash);
        Utils.writeContents(Utils.join(args[3]), Utils.readContents(blobfile));
    }

    public void branchCheckout(String[] args) {
        String parent = Utils.readObject(HEAD, String.class);
        @SuppressWarnings("unchecked") TreeMap<String, String> branches =
                (TreeMap<String, String>) Utils
                        .readObject(BRANCHES, TreeMap.class);
        String currentBranch = Utils.readObject(CURRENTBRANCH, String.class);
        String toBranch = args[1];
        @SuppressWarnings("unchecked") TreeMap<String, String> toAdd
                = (TreeMap<String, String>) Utils
                .readObject(ADD, TreeMap.class);
        @SuppressWarnings("unchecked") TreeMap<String, Commit> commitTree
                = (TreeMap<String, Commit>) Utils
                .readObject(COMMITS, TreeMap.class);
        if (!branches.keySet().contains(toBranch)) {
            System.out.println("No such branch exists.");
            return;
        } else if (toBranch.equals(currentBranch)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        Commit commit = commitTree.get(parent);
        Commit test = commitTree.get(branches.get(toBranch));
        @SuppressWarnings("unchecked") List<String> filesinCWD
                = Utils.plainFilenamesIn(CWD);
        for (int i = 0; i < filesinCWD.size(); i++) {
            if (!commit.getfiles().keySet().contains(filesinCWD.get(i))
                    && !toAdd.keySet().contains(filesinCWD.get(i))
                    && test.getfiles().keySet().contains(filesinCWD.get(i))) {
                System.out.println("There is an untracked file in the way;"
                        + " delete it, or add and commit it first.");
                return;
            }
        }
        @SuppressWarnings("unchecked") TreeMap<String, String> previousFiles
                = (TreeMap<String, String>) commit.getfiles().clone();
        Object[] keys = previousFiles.keySet().toArray();
        for (Object s : keys) {
            if (!test.getfiles().containsKey((String) s)) {
                previousFiles.remove((String) s);
                Utils.join((String) s).delete();
            } else {
                commitCheckout(new String[]{"checkout",
                        branches.get(toBranch), "--", (String) s});
            }
        }
        for (String s : test.getfiles().keySet()) {
            if (!filesinCWD.contains(s)) {
                File f = new File(s);
                Utils.writeContents(f, Utils.readContents
                        (new File(BLOBS, test.getfiles().get(s))));
                try {
                    f.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Utils.writeObject(HEAD, branches.get(toBranch));
        Utils.writeObject(CURRENTBRANCH, toBranch);
    }

    public void find(String[] args) {
        int c = 0;
        @SuppressWarnings("unchecked") TreeMap<String, Commit> commitTree
                = (TreeMap<String, Commit>) Utils
                .readObject(COMMITS, TreeMap.class);
        @SuppressWarnings("unchecked") Object[] commits
                = commitTree.values().toArray();
        for (int i = 0; i < commits.length; i++) {
            if (((Commit) commits[i]).logmessage().equals(args[1])) {
                System.out.println(((Commit) commits[i]).hash());
                c++;
            }
        }
        if (c == 0) {
            System.out.println("Found no commit with that message.");
            return;
        }
    }

    /** @param args from the main command portal. Resets system. */
    public void reset(String[] args) {
        String parent = args[1];
        String recent = Utils.readObject(HEAD, String.class);
        String currentBranch = Utils.readObject(CURRENTBRANCH, String.class);
        @SuppressWarnings("unchecked") TreeMap<String, String> toAdd
                = (TreeMap<String, String>) Utils
                .readObject(ADD, TreeMap.class);
        @SuppressWarnings("unchecked") TreeMap<String, Commit> commitTree
                = (TreeMap<String, Commit>) Utils
                .readObject(COMMITS, TreeMap.class);
        @SuppressWarnings("unchecked") TreeMap<String, String> branches =
                (TreeMap<String, String>) Utils
                        .readObject(BRANCHES, TreeMap.class);
        Commit commit = commitTree.get(parent);
        if (commit == null) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit test = commitTree.get(recent);
        @SuppressWarnings("unchecked") Object[] committedFiles
                = test.getfiles().keySet().toArray();
        @SuppressWarnings("unchecked") List<String> filesinCWD
                = Utils.plainFilenamesIn(CWD);
        for (int i = 0; i < filesinCWD.size(); i++) {
            if (!test.getfiles().keySet().contains(filesinCWD.get(i))
                    && !toAdd.keySet().contains(filesinCWD.get(i))
                    && commit.getfiles().keySet().contains(filesinCWD.get(i))) {
                System.out.println("There is an untracked file in the way;"
                        + " delete it, or add and commit it first.");
                return;
            }
        }
        for (int i = 0; i < committedFiles.length; i++) {

            if (!commit.getfiles().keySet().contains(committedFiles[i])) {
                removefile(new String[]{"remove", (String) committedFiles[i]});
            } else {
                String blobhash = commit.getfiles().get(committedFiles[i]);
                File blobfile = new File(".gitlet/blobs", blobhash);
                Utils.writeContents(Utils.join((String) committedFiles[i]),
                        Utils.readContents(blobfile));
            }
        }
        Utils.writeObject(HEAD, parent);
        branches.put(currentBranch, parent);
        Utils.writeObject(BRANCHES, branches);
        Utils.writeObject(ADD, new TreeMap<String, String>());
        Utils.writeObject(REMOVE, new TreeMap<String, String>());
    }

    public void status() {
        String parent = Utils.readObject(HEAD, String.class);
        String currentBranch = Utils.readObject(CURRENTBRANCH, String.class);
        @SuppressWarnings("unchecked") TreeMap<String, String> branches =
                (TreeMap<String, String>) Utils
                        .readObject(BRANCHES, TreeMap.class);
        @SuppressWarnings("unchecked") TreeMap<String, Commit> commitTree
                = (TreeMap<String, Commit>) Utils
                .readObject(COMMITS, TreeMap.class);
        Commit commit = commitTree.get(parent);
        Object[] keyset = commit.getfiles().keySet().toArray();
        List<String> filesinCWD = Utils.plainFilenamesIn(CWD);
        @SuppressWarnings("unchecked") TreeMap<String, String> toAdd
                = (TreeMap<String, String>) Utils
                .readObject(ADD, TreeMap.class);
        Object[] addArray = toAdd.keySet().toArray();
        @SuppressWarnings("unchecked") TreeMap<String, String> toRemove
                = (TreeMap<String, String>) Utils
                .readObject(REMOVE, TreeMap.class);
        Object[] removeArray = toRemove.keySet().toArray();
        System.out.println("=== Branches ===");
        for (String s : branches.keySet()) {
            if (s.equals(currentBranch)) {
                System.out.println("*" + s);
            } else {
                System.out.println(s);
            }
        }
        System.out.println("\n" + "=== Staged Files ===");
        for (int i = 0; i < addArray.length; i++) {
            System.out.println(addArray[i]);
        }
        System.out.println("\n" + "=== Removed Files ===");
        for (int i = 0; i < removeArray.length; i++) {
            System.out.println(removeArray[i]);
        }
        System.out.println("\n"
                + "=== Modifications Not Staged For Commit ===");
        for (int i = 0; i < commit.getfiles().size(); i++) {
            if (!filesinCWD.contains(keyset[i])
                    && !toRemove.containsKey(keyset[i])) {
                System.out.println(keyset[i] + "(deleted)");
            } else if (Utils.join((String) keyset[i]).exists()) {
                Blob blobby = new Blob(new File((String) keyset[i]));
                if (!blobby.hash().equals(commit.getfiles().get(keyset[i]))
                        && (toAdd.get(keyset[i]) == null
                        || !toAdd.get(keyset[i]).equals(blobby.hash()))) {
                    System.out.println(keyset[i] + "(modified)");
                }
            }
        }
        System.out.println("\n" + "=== Untracked Files ===");
        for (int i = 0; i < filesinCWD.size(); i++) {
            if (!commit.getfiles().keySet().contains(filesinCWD.get(i))
                    && !toAdd.keySet().contains(filesinCWD.get(i))) {
                System.out.println(filesinCWD.get(i));
            }
        }
    }

    public void branch(String[] args) {
        String parent = Utils.readObject(HEAD, String.class);
        @SuppressWarnings("unchecked") TreeMap<String, String> branches =
                (TreeMap<String, String>) Utils
                        .readObject(BRANCHES, TreeMap.class);
        if (branches.keySet().contains(args[1])) {
            System.out.println("A branch with that name already exists.");
            return;
        } else {
            branches.put(args[1], parent);
            Utils.writeObject(BRANCHES, branches);
        }
    }

    public void removeBranch(String[] args) {
        String toDelete = args[1];
        String currentBranch = Utils.readObject(CURRENTBRANCH, String.class);
        @SuppressWarnings("unchecked") TreeMap<String, String> branches =
                (TreeMap<String, String>) Utils
                        .readObject(BRANCHES, TreeMap.class);
        if (toDelete.equals(currentBranch)) {
            System.out.println("Cannot remove the current branch.");
            return;
        } else if (!branches.keySet().contains(toDelete)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        branches.remove(toDelete);
        Utils.writeObject(BRANCHES, branches);
    }

    public void checkoutSorter(String[] args) {
        if (args.length == 3 && args[1].equals("--")) {
            fileCheckout(args);
        } else if (args.length == 4 && args[2].equals("--")) {
            commitCheckout(args);
        } else if (args.length == 2) {
            branchCheckout(args);
        } else {
            System.out.println("incorrect operands");
        }
    }

    public void merger(String[] args) {
        String currentBranch = Utils.readObject(CURRENTBRANCH, String.class);
        @SuppressWarnings("unchecked") TreeMap<String, String> branches =
                (TreeMap<String, String>) Utils
                        .readObject(BRANCHES, TreeMap.class);
        @SuppressWarnings("unchecked") TreeMap<String, Commit> commitTree
                = (TreeMap<String, Commit>) Utils
                .readObject(COMMITS, TreeMap.class);
        if (!branches.containsKey(args[1])) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        Commit test = commitTree.get(branches.get(args[1]));
        Commit commit = commitTree.get(branches.get(currentBranch));
        List<String> filesinCWD = Utils.plainFilenamesIn(CWD);
        @SuppressWarnings("unchecked") TreeMap<String, String> toAdd = (TreeMap
                <String, String>) Utils.readObject(ADD, TreeMap.class);
        @SuppressWarnings("unchecked") TreeMap<String, String> toRemove
                = (TreeMap<String, String>) Utils
                .readObject(REMOVE, TreeMap.class);
        ArrayList<String> otherAncestors = new ArrayList<>();
        ArrayList<String> currentAncestors = new ArrayList<>();
        for (int i = 0; i < filesinCWD.size(); i++) {
            if (!commit.getfiles().keySet().contains(filesinCWD.get(i))
                    && !toAdd.keySet().contains(filesinCWD.get(i))
                    && test.getfiles().keySet().contains(filesinCWD.get(i))) {
                System.out.println("There is an untracked file in the way;"
                        + " delete it, or add and commit it first.");
                return;
            }
        }
        if (args[1].equals(currentBranch)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        } else if (toAdd.size() + toRemove.size() > 0) {
            System.out.println("You have uncommitted changes.");
            return;
        }
        String hash = branches.get(args[1]);
        while (hash != null) {
            Commit node = commitTree.get(hash);
            otherAncestors.add(hash);
            hash = node.parent();
        }
        hash = branches.get(currentBranch);
        while (hash != null) {
            Commit node = commitTree.get(hash);
            currentAncestors.add(hash);
            hash = node.parent();
        }
        if (currentAncestors.contains(otherAncestors.get(0))) {
            System.out.println("Given branch is an ancestor of the current "
                    + "branch.");
            return;
        } else if (otherAncestors.contains(currentAncestors.get(0))) {
            branchCheckout(new String[]{"checkout", args[1]});
            System.out.println("Current branch fast-forwarded.");
            return;
        }
    }
}
