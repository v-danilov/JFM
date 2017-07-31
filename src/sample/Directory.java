package sample;
import java.io.File;
import java.net.URI;


class Directory extends File {

    private boolean wasOpenned = false;

    public Directory(String pathname) {
        super(pathname);
    }

    public Directory (File file){
        super(file.getPath());
    }

    public Directory(String parent, String child) {
        super(parent, child);
    }

    public Directory(File parent, String child) {
        super(parent, child);
    }

    public Directory(URI uri) {
        super(uri);
    }

    public boolean getWasOpenned() {
        return wasOpenned;
    }

    public void open() {
        wasOpenned = true;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public File[] listFiles() {
        open();
        return super.listFiles();
    }
}
