package sample;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controller {

    @FXML
    private TreeView<File> systemTree;

    @FXML
    private Button showButton;

    @FXML
    private TableView<File> filesTable;

    @FXML
    private ListView<File> listView;

    /*@FXML
    private Label statusField;*/


    private final Image closedFolder=new Image(ClassLoader.getSystemResourceAsStream("images/closedFolder.png"));
    private final Image openFolder=new Image(ClassLoader.getSystemResourceAsStream("images/openFolder.png"));
    private final Image fileIco=new Image(ClassLoader.getSystemResourceAsStream("images/fileico.png"));

    private ContextMenu contextMenu = new ContextMenu();

    private MenuItem renameItem = new MenuItem("Rename");
    private MenuItem replaceItem = new MenuItem("Replace");
    private MenuItem deleteItem = new MenuItem("Delete");


    @FXML
    public void initialize(){
        System.out.println("Hello");

        showTree();


        contextMenu.getItems().addAll(renameItem, replaceItem, deleteItem);
        listView.setContextMenu(contextMenu);
        listView.setPlaceholder(new Label("<- Choose folder..."));


        systemTree.addEventHandler(MouseEvent.ANY, event -> {
            if (event.getClickCount() == 2 && event.getButton().equals(MouseButton.PRIMARY)) {
                if (event.getEventType().equals(MouseEvent.MOUSE_CLICKED)) {
                    folderNavigation();
                }

                event.consume();
            }
        });
        renameItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                renameFile();
            }
        });

        replaceItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
               moveFile();
            }
        });

        deleteItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                deleteFile();
            }
        });

    }

    private void showTree(){
        File root_directory = new File("Root");
        systemTree.setRoot(new TreeItem<>(root_directory));
        createTree(root_directory, null);
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

            //There is reverse logic because treeView default double click event opens folder (maybe)
            if(selectedItem.isExpanded()){
                //goUp(selectedItem);
                goDown(selectedItem);
            }
            else {
                //goDown(selectedItem);
                goUp(selectedItem);
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
        File[] dirFiles = directory.listFiles();
        if(dirFiles.length != 0) {
            for(File file : dirFiles ){
                if(file.isFile()){
                    listView.getItems().add(file);
                }
            }

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

    private void renameFile(){

        File fileToRename = listView.getSelectionModel().getSelectedItem();
        String oldName = fileToRename.getName();
        String newName = "";

        newName = getNewName(fileToRename);

        if(!newName.isEmpty()) {
            String filePath = fileToRename.getPath();
            String renamedPath = filePath.replace(oldName, newName);
            File renamedFile = new File(renamedPath);
        }

    }

    //Get new file name from user
    private String getNewName(File file){

        boolean type = file.isFile();
        //Create window for input
        TextInputDialog textInputDialog = new TextInputDialog();
        textInputDialog.setHeaderText("Input new name");

        //Fil old name
        textInputDialog.getEditor().setText(file.getName());

        //While name is not valid
        String name;

        if(type){
            do{
                textInputDialog.showAndWait();
                name = textInputDialog.getResult();
            }
            while (!checkFileName(name));
        }else {
            do{
                textInputDialog.showAndWait();
                name = textInputDialog.getResult();
            }
            while (!checkDirName(name));
        }

        return name;
    }

    //Check file name for regexp
    private boolean checkFileName(String str){
        Pattern p = Pattern.compile("[^?:\"<>*\\/\\|]+\\.[A-Za-z0-9]+");
        Matcher m = p.matcher(str);
        return m.matches();
    }

    //Check directory name for regexp
    private boolean checkDirName(String str){
        Pattern p = Pattern.compile("[^~#%&*{}\\:<>/?\\+\\|\"\\.]+");
        Matcher m = p.matcher(str);
        return m.matches() ;
    }

    //Move file
    private void moveFile(){

        File fileToMove = listView.getSelectionModel().getSelectedItem();
        String oldpath = fileToMove.getPath();
        String newPath= "";
        newPath = getNewName(fileToMove);
    }

    //Delete file
    private void deleteFile(){
        File fileToDel= listView.getSelectionModel().getSelectedItem();
        String mes = "Delete " + fileToDel.getName() + "?";

        //Take confirmation
        if(confirmationAlert(mes)){
            fileToDel.delete();
        }
        systemTree.refresh();
    }

    //Confirmation func
    private boolean confirmationAlert(String mes){

        //Prepare alert
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, mes, ButtonType.YES, ButtonType.CANCEL);
        alert.showAndWait();

        //Get choice
        if (alert.getResult() == ButtonType.YES) {
            return true;
        }else {
            return false;
        }
    }

   /* private void statusShow(boolean status){
        if(status){
            statusField.setTextFill(Color.web("00ff00"));
            statusField.setText("Success");
        }else{
            statusField.setTextFill(Color.web("#ff0000"));
            statusField.setText("Failed");
        }
    }*/

}
