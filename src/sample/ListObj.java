package sample;


import java.io.File;

public class ListObj {

    private String fileName;
    private String fileImage;

    public ListObj(String fileName, String fileImage) {
        this.fileName = fileName;
        this.fileImage = fileImage;
    }

    public ListObj() {
        this.fileName = "";
        this.fileImage = "";
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileImage() {
        return fileImage;
    }

    public void setFileImage(String fileImage) {
        this.fileImage = fileImage;
    }
}
