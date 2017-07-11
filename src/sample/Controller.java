package sample;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.io.IOException;

public class Controller {

    @FXML
    private TreeView<File> systemTree;

    @FXML
    private Button showButton;

    @FXML
    private TableView<File> filesTable;

    @FXML
    private ListView<File> listView;

    public final Image closedFolder=new Image(ClassLoader.getSystemResourceAsStream("images/closedFolder.png"));
    public final Image openFolder=new Image(ClassLoader.getSystemResourceAsStream("images/openFolder.png"));


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
            System.out.println(selected_file.getValue().getPath());
        }

    }

    public void createTree(File dir, TreeItem<File> parent) {

            TreeItem<File> root = new TreeItem<>(dir);
            ImageView icon = new ImageView();
            icon.setImage(closedFolder);
            icon.setFitHeight(20);
            icon.setFitWidth(20);
            root.setGraphic(icon);
            root.setExpanded(false);
            File[] files = dir.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    createTree(file, root);
                }
            }

            if (parent == null) {
                systemTree.setRoot(root);
            } else {
                parent.getChildren().add(root);
            }

    }


}
