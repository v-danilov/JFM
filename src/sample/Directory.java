package sample;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;


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
        if (!getName().equals("")) {
            return getName();
        } else {
            return super.getPath();
        }
    }

    @Override
    public File[] listFiles() {
        open();
        return super.listFiles();
    }

    public ArrayList<Directory> listDirectories() {
        File[] allFiles = super.listFiles((dir, name) -> !name.equals(".DS_Store"));
        ArrayList<Directory> directoriesOnly = new ArrayList<>();
        for (File file : allFiles) {
            if (file.isDirectory()) {
                directoriesOnly.add(new Directory(file));
            }
        }
        return directoriesOnly;
    }
}
