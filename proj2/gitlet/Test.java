package gitlet;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


public class Test {
    public static void main(String[] args) {
        String CWD = System.getProperty("user.dir");
        System.out.println("I'm in " + CWD);
        List<String> l = Utils.plainFilenamesIn(CWD);
        System.out.println(l);
        Path p = Paths.get(CWD, l.get(0));
        System.out.println(p.getFileName().toString());

    }
}
