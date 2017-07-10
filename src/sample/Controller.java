package sample;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.File;

public class Controller {

    @FXML
    private TreeView<File> systemTree;

    @FXML
    private Button showButton;

    @FXML
    private TableView<File> filesTable;

    @FXML
    private ListView<File> listView;


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
        listView.getItems().clear();
        TreeItem<File> selected_file = systemTree.getSelectionModel().getSelectedItem();
        if(selected_file !=null){
            File currentFile = selected_file.getValue();
            listView.getItems().addAll(currentFile.listFiles());
            //filesTable.getItems().addAll(currentFile.listFiles());

            /*for(File f : currentFile.listFiles()){

                filesTable.getItems().add(f);
            }*/

            System.out.println(selected_file.getValue().getPath());
        }

    }

    public void createTree(File dir, TreeItem<File> parent) {
        TreeItem<File> root = new TreeItem<>(dir);
        root.setExpanded(false);
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                createTree(file, root);
            }
        }

        if(parent==null){
            systemTree.setRoot(root);
        } else {
            parent.getChildren().add(root);
        }
    }


}
