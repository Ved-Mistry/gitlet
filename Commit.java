package gitlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Ved Mistry
 */
public class Commit implements Serializable, Cloneable {

    /** Parent hashcode. */
    private String _parent;

    /** Hash of commit. */
    private String _hash;

    /** Commit message. */
    private String _logMessage;

    /** Filename, BlobHash. */
    private TreeMap<String, String> _files;

    /** Time of commit. */
    private String _timestamp;


    /** @param parent is the hash of the prior commit.
     *  @param logmessage is the commit message.
     *  @param blobhashes is the set of blobs. */
    public Commit(String parent, String logmessage,
                  TreeMap<String, String> blobhashes) {
        _parent = parent;
        _logMessage = logmessage;
        _files = blobhashes;
        SimpleDateFormat dateformat =
                new SimpleDateFormat("E MMM dd hh:mm:ss yyyy Z");
        _timestamp = dateformat.format(new Date());
        _hash = Utils.sha1(Utils.serialize(this));
    }

    /** @return Accessor method for _parent. */
    public String parent() {
        return _parent;
    }

    /** @return Accessor method for _hash. */
    public String hash() {
        return _hash;
    }

    /** @return Accessor method for _log_message. */
    public String logmessage() {
        return _logMessage;
    }

    /** @return Accessor method for _files. */
    public TreeMap<String, String> getfiles() {
        return _files;
    }

    /** @return Accessor method for _timestamp. */
    public String timestamp() {
        return _timestamp;
    }


}
