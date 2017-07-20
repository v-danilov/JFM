package sample;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
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

    @FXML
    private Label currentFolderNameLable;

    /*@FXML
    private Label statusField;*/


    private final Image closedFolderIco =new Image(ClassLoader.getSystemResourceAsStream("images/closedFolder.png"));
    private final Image openFolderIco =new Image(ClassLoader.getSystemResourceAsStream("images/openFolder.png"));
    private final Image fileIco=new Image(ClassLoader.getSystemResourceAsStream("images/fileico.png"));

    private ContextMenu contextMenu = new ContextMenu();

    private Menu createFileMenu = new Menu("Create new...");
    private MenuItem createFileItem = new MenuItem("File");
    private MenuItem createDirItem = new MenuItem("Directory");

    private MenuItem renameItem = new MenuItem("Rename");
    private MenuItem replaceItem = new MenuItem("Replace");
    private MenuItem deleteItem = new MenuItem("Delete");

    private String currentPath;


    @FXML
    public void initialize(){
        System.out.println("Hello");

        showTree();

        createFileMenu.getItems().addAll(createFileItem,createDirItem);
        contextMenu.getItems().addAll(createFileMenu, renameItem, replaceItem, deleteItem);
        listView.setContextMenu(contextMenu);
        listView.setPlaceholder(new Label("<- Choose folder..."));


        /*systemTree.setCellFactory(new Callback<TreeView<File>, TreeCell<File>>() {

            public TreeCell<File> call(TreeView<File> tv) {
                return new TreeCell<File>() {

                    @Override
                    protected void updateItem(File item, boolean empty) {
                        super.updateItem(item, empty);
                        if(!empty){
                            setText(item.getName());
                        }

                        ImageView imageView = new ImageView();
                        imageView.setFitHeight(20);
                        imageView.setFitWidth(20);

                        setGraphic(imageView);
                    }

                };
            }
        });*/

        listView.setCellFactory(listView -> new ListCell<File>() {
            private ImageView imageView = new ImageView();

            @Override
            public void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText("");
                } else {
                    imageView.setFitWidth(20);
                    imageView.setFitHeight(20);

                    if(item.isFile()){
                        imageView.setImage(fileIco);
                    }
                    else {
                        imageView.setImage(closedFolderIco);
                    }

                    setGraphic(imageView);
                    setText(item.getName());
                }
            }
        });


        systemTree.addEventHandler(MouseEvent.ANY, event -> {
            if (event.getClickCount() == 2 && event.getButton().equals(MouseButton.PRIMARY)) {
                if (event.getEventType().equals(MouseEvent.MOUSE_CLICKED)) {
                    folderNavigation();
                }
                event.consume();
            }
        });

        contextMenu.setOnShowing(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                if(listView.getSelectionModel().getSelectedItem() == null){
                    renameItem.setVisible(false);
                    replaceItem.setVisible(false);
                    deleteItem.setVisible(false);
                }else {
                    renameItem.setVisible(true);
                    replaceItem.setVisible(true);
                    deleteItem.setVisible(true);
                }
            }
        });

        createFileItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                createNewFile();
            }
        });
        createDirItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                createNewDir();
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
        setDirImage(root, true);
        File[] files = dir.listFiles();
        /*for (File file : files) {
            if (file.isDirectory()) {
                createTree(file, root);
            }
        }*/

        if (parent == null) {
            systemTree.setRoot(root);
        } else {
            parent.getChildren().add(root);
        }

    }

    @FXML
    private void folderNavigation(){
        TreeItem<File> selectedItem = systemTree.getSelectionModel().getSelectedItem();

        if(selectedItem !=null){

            //There is reverse logic for goUp and goDown
            //because treeView default double click expands folder first
            if(selectedItem.isExpanded() || selectedItem.isLeaf()){

                //goUp(selectedItem);

                //Lazy loading block

                //Loading circle
                ProgressIndicator pInd = new ProgressIndicator();
                pInd.setPrefSize(20,20);
                pInd.setStyle(" -fx-progress-color: red;");
                selectedItem.setGraphic(pInd);

                //New thread
                Thread update = new Thread() {
                    public void run() {
                        try {
                            //Delay 2sec
                            Thread.sleep(2000);
                        }
                        catch (InterruptedException ie){
                            System.err.println(ie.getMessage());
                        }
                        //Update UI
                        Platform.runLater(new Runnable() {
                            public void run() {
                                goDown(selectedItem);
                            }
                        });
                    }
                };
                update.start();

                // Usual (in time) loading
                // goDown(selectedItem);


            }
            else {

                //goDown(selectedItem);

                goUp(selectedItem);

            }
        }
    }

    private void goUp(TreeItem<File> item){
        setDirImage(item, true);
        item.getChildren().clear();
        if(isRoot(item)){
            displayFiles(item.getValue());
        }else {
            displayFiles(item.getParent().getValue());
        }
        collapseAllNodes(item);
    }

    private void goDown(TreeItem<File> item){
        setDirImage(item, false);
        item.setExpanded(true);
        File currentFile = item.getValue();
        File[] leafs = currentFile.listFiles();
        for(File leaf : leafs){
            if(leaf.isDirectory()) {
                createTree(leaf, item);
            }
        }
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
        currentPath = directory.getPath();
        listView.getItems().clear();
        File[] dirFiles = directory.listFiles();
        currentFolderNameLable.setText(directory.getName());
        if(dirFiles.length != 0) {

            for(File file : dirFiles ){
                    listView.getItems().add(file);
            }

        }
        else {
            listView.setPlaceholder(new Label(directory.getName() + " is empty "));
        }
    }

    private void collapseAllNodes(TreeItem<File> item){
        if(item != null){
            setDirImage(item, true);
            for(TreeItem<File> child:item.getChildren()){
                collapseAllNodes(child);
            }
        }
    }

    private void setDirImage(TreeItem<File> treeItem, boolean closed){

        ImageView icon = new ImageView();
        icon.setFitHeight(20);
        icon.setFitWidth(20);

        if(closed){
            icon.setImage(closedFolderIco);
        }
        else {
            icon.setImage(openFolderIco);
        }
        treeItem.setGraphic(icon);

    }

    private void renameFile(){

        File fileToRename = listView.getSelectionModel().getSelectedItem();

            String oldName = fileToRename.getName();
            String newName = "";

            newName = getNewName(fileToRename);

            if (!newName.isEmpty()) {
                String filePath = fileToRename.getPath();
                String renamedPath = filePath.replace(oldName, newName);
                File renamedFile = new File(renamedPath);
                fileToRename.renameTo(renamedFile);
            }

            refreshTree();

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

    private String getNewName(){

        //Create window for input
        TextInputDialog textInputDialog = new TextInputDialog();
        textInputDialog.setHeaderText("Input new name");


        //While name is not valid
        String name;

            do{
                textInputDialog.showAndWait();
                name = textInputDialog.getResult();
            }
            while (!checkFileName(name));


        return name;
    }

    private String getNewPath(File file){

        boolean type = file.isFile();
        String pathOnly = file.getParent();

        //Create window for input
        TextInputDialog textInputDialog = new TextInputDialog();
        textInputDialog.setHeaderText("New path for " + file.getName());

        //Fil old name
        textInputDialog.getEditor().setText(pathOnly);


        //While name is not valid
        String name;
        boolean wrongPath = true;

        do {
            textInputDialog.showAndWait();
            name = textInputDialog.getResult();
            String[] dirs = name.split("/");
            for(String dir : dirs){
                if(checkDirName(dir)){
                    wrongPath = false;
                }else {
                    wrongPath = true;
                    break;
                }
            }
        }
        while (wrongPath);

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
            String newPath;
            newPath = getNewPath(fileToMove);
            fileToMove.renameTo(new File(newPath + fileToMove.getName()));
    }

    //Delete file
    private void deleteFile(){
        File fileToDel= listView.getSelectionModel().getSelectedItem();

            String mes = "Delete " + fileToDel.getName() + "?";

            //Take confirmation
            if (confirmationAlert(mes)) {
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

    private void createNewFile(){
        System.out.println(currentPath);
        String newFileName = getNewName();
        File newFile = new File(currentPath + "/" + newFileName);
        try {
            newFile.createNewFile();
        }
        catch (IOException ioe){
            System.err.println(ioe.getStackTrace());
        }
        refreshTree();
    }

    private void createNewDir(){

    }

    private void refreshTree(){
        systemTree.setRoot(null);
        showTree();
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
