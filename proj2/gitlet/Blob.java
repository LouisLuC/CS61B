package gitlet;

import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static gitlet.Repository.BLOBS_DIR;
import static gitlet.Utils.*;

/**
 * Represents the saved contents of files.
 */
public class Blob implements Serializable {
    String id;
    String fileName;
    byte[] contents;

    @Serial
    private static final long serialVersionUID = 24L;

    private Blob() {
    }

    Blob(Path file) {
        this.fileName = file.getFileName().toString();
        this.contents = readContents(file);
        this.id = sha1(fileName, contents);
    }

    static Blob mergeConflict(String fileName, Blob inHEAD, Blob other) {
        Blob newBlog = new Blob();
        byte[] inHeadContent = inHEAD == null ? new byte[0] : inHEAD.contents;
        byte[] otherContent = other == null ? new byte[0] : other.contents;
        String strBuilder = "<<<<<<< HEAD\n" +
                new String(inHeadContent, StandardCharsets.UTF_8) +
                "=======\n" +
                new String(otherContent, StandardCharsets.UTF_8) +
                ">>>>>>>\n";
        newBlog.contents = strBuilder.getBytes(StandardCharsets.UTF_8);
        newBlog.id = sha1(newBlog.fileName, newBlog.contents);
        return newBlog;
    }

    void save() {
        Path path = BLOBS_DIR.resolve(this.id);
        save(path);
    }

    void save(Path path) {
        if (!checkFileExist(path))
            writeObject(path.toFile(), this);
    }

    /**
     * Get Blob object stored in BLOBS_DIR. Return null if there is no such file.
     * */
    public static Blob getBlob(String id) {
        if (id == null) return null;
        Path path = BLOBS_DIR.resolve(id);
        if (!checkFileExist(path))
            return null;
        return readObject(path.toFile(), Blob.class);
    }
}
