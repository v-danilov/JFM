package sample;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

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


    public final Image closedFolder=new Image(ClassLoader.getSystemResourceAsStream("images/closedFolder.png"));
    public final Image openFolder=new Image(ClassLoader.getSystemResourceAsStream("images/openFolder.png"));
    public final Image fileIco=new Image(ClassLoader.getSystemResourceAsStream("images/fileico.png"));


    @FXML
    public void showFiles(){
        System.out.println("Hello");

        File root_directory = new File("Root");

        systemTree.setRoot(new TreeItem<>(root_directory));
        createTree(root_directory, null);
        showButton.setVisible(false);
        showButton.setDisable(true);

    }

    private void createTree(File dir, TreeItem<File> parent) {

        TreeItem<File> root = new TreeItem<>(dir);
        setClosed(root);
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

    @FXML
    private void folderNavigation(){

        listView.getItems().clear();
        TreeItem<File> selectedItem = systemTree.getSelectionModel().getSelectedItem();

        if(selectedItem !=null){
            if(selectedItem.isExpanded()){
                goUp(selectedItem);
            }
            else {
                goDown(selectedItem);
            }
        }
    }

    private void goUp(TreeItem<File> item){
        item.setExpanded(false);
        setClosed(item);

        if(isRoot(item)){
            displayFiles(item.getValue());
            collapseAllNodes(item);
        }else {
            displayFiles(item.getParent().getValue());
        }
        System.out.println(item.getValue());
    }

    private void goDown(TreeItem<File> item){
        setOpen(item);
        File currentFile = item.getValue();
        displayFiles(currentFile);
    }

    private boolean isRoot(TreeItem<File> treeItem){
        if(treeItem.getParent() == null){
            return true;
        }
        else {
            return false;
        }
    }

    private void displayFiles(File directory){
        if(directory.listFiles().length != 0) {
            listView.getItems().addAll(directory.listFiles());
        }
        else {
            listView.setPlaceholder(new Label(directory.getName() + " is empty "));
        }
    }

    private void collapseAllNodes(TreeItem<File> item){
        if(item != null && !item.isLeaf()){
            setClosed(item);
            for(TreeItem<File> child:item.getChildren()){
                collapseAllNodes(child);
            }
        }
    }

    private void setClosed(TreeItem<File> treeItem){
        ImageView icon = new ImageView();
        icon.setImage(closedFolder);
        icon.setFitHeight(20);
        icon.setFitWidth(20);
        treeItem.setGraphic(icon);
        treeItem.setExpanded(false);
    }

    private void setOpen(TreeItem<File> treeItem){
        ImageView icon = new ImageView();
        icon.setImage(openFolder);
        icon.setFitHeight(20);
        icon.setFitWidth(20);
        treeItem.setGraphic(icon);
        treeItem.setExpanded(true);
    }


}
