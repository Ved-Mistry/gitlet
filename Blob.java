package gitlet;

import java.io.File;
import java.io.Serializable;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Ved Mistry
 */
public class Blob implements Serializable {

    /** Serial number of blob. */
    private byte[] _serial;

    /** File name of blob. */
    private String _filename;

    /** Hash of blob. */
    private String _hash;

    /** File in blob. */
    private File _file;

    /** @param file is the file Blob is storing. */
    public Blob(File file) {
        _serial = Utils.readContents(file);
        _hash = Utils.sha1(_serial);
        _filename = file.getName();
        _file = file;
    }

    public byte[] serialnumber() {
        return _serial;
    }

    public String hash() {
        return _hash;
    }

    public String filename() {
        return _filename;
    }

    public File file() {
        return _file;
    }

}
