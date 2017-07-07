package sample;

import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.io.File;

public class Controller {

    @FXML
    private TreeView<File> systemTree;

    @FXML
    private void showFiles(){
        System.out.println("Hello");

        File root_directory = new File("Root");

        systemTree.setRoot(new TreeItem<>(root_directory));
        createTree(root_directory, null);

    }

    @FXML
    private void getElementPath(){
        TreeItem<File> selected_file = systemTree.getSelectionModel().getSelectedItem();
        System.out.println(selected_file.getValue().getPath());
    }

    public void createTree(File dir, TreeItem<File> parent) {
        TreeItem<File> root = new TreeItem<>(dir);
        root.setExpanded(true);
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                createTree(file, root);
            } else {
                root.getChildren().add(new TreeItem<>(file));
            }
        }

        if(parent==null){
            systemTree.setRoot(root);
        } else {
            parent.getChildren().add(root);
        }
    }


}
