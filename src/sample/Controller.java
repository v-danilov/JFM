package sample;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.File;

public class Controller {

    @FXML
    private TreeView<File> systemTree;

    @FXML
    private Button showButton;

    @FXML
    private TableView<File> filesTable;

    @FXML
    public void showFiles(){
        System.out.println("Hello");

        File root_directory = new File("Root");

        systemTree.setRoot(new TreeItem<>(root_directory));
        createTree(root_directory, null);
        showButton.setVisible(false);
        showButton.setDisable(true);
    }

    @FXML
    private void getElementPath(){
        TreeItem<File> selected_file = systemTree.getSelectionModel().getSelectedItem();
        if(selected_file !=null){
            for(File f : selected_file.getValue().listFiles()){
                filesTable.getItems().add(f);
            }
            filesTable.getItems().addAll(selected_file.getValue().listFiles());
            System.out.println(selected_file.getValue().getPath());
        }

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
