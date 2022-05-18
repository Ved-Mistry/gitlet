package gitlet;

import java.io.File;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Ved Mistry
 */
public class Main {
    /** Current Working Directory. */
    static final File CWD = new File(".");

    /** Main metadata folder. */
    static final File GITLET = new File(".gitlet");

    /** Main repository being set up. */
    private static Gitlet repo = new Gitlet();

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        } else if (!args[0].equals("init")) {
            if (!repo.initTest()) {
                System.out.println("Not in an initialized Gitlet directory.");
                return;
            }
        }
        switch (args[0]) {
        case "init":
            repo.init();
            break;
        case "add":
            repo.addfile(args);
            break;
        case "commit":
            repo.commit(args);
            break;
        case "log":
            repo.log();
            break;
        case "global-log":
            repo.globalLog();
            break;
        case "checkout":
            repo.checkoutSorter(args);
            break;
        case "find":
            repo.find(args);
            break;
        case "reset":
            repo.reset(args);
            break;
        case "status":
            repo.status();
            break;
        case "rm":
            repo.removefile(args);
            break;
        case "branch":
            repo.branch(args);
            break;
        case "rm-branch":
            repo.removeBranch(args);
            break;
        case "merge":
            repo.merger(args);
            break;
        default:
            System.out.println("No command with that name exists.");
            return;
        }
    }

    /** Accessor method for repo.
     * @return repo is the repository set up by main. */
    public Gitlet repo() {
        return repo;
    }

}
