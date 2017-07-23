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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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


    private final Image closedFolderIco = new Image(ClassLoader.getSystemResourceAsStream("images/closedFolder.png"));
    private final Image openFolderIco = new Image(ClassLoader.getSystemResourceAsStream("images/openFolder.png"));
    private final Image fileIco = new Image(ClassLoader.getSystemResourceAsStream("images/fileico.png"));

    private ContextMenu contextMenu = new ContextMenu();

    private Menu createFileMenu = new Menu("Create new...");
    private MenuItem createFileItem = new MenuItem("File");
    private MenuItem createDirItem = new MenuItem("Directory");

    private MenuItem renameItem = new MenuItem("Rename");
    private MenuItem copyItem = new MenuItem("Copy");
    private MenuItem pasteItem = new MenuItem("Paste");
    private MenuItem replaceItem = new MenuItem("Replace");
    private MenuItem deleteItem = new MenuItem("Delete");

    private String currentPath;
    private String copyPath;


    @FXML
    public void initialize() {
        System.out.println("Hello");

        currentPath = welcomeFunc();
        showTree(currentPath);

        createFileMenu.getItems().addAll(createFileItem, createDirItem);
        pasteItem.setDisable(true);
        contextMenu.getItems().addAll(createFileMenu, copyItem, pasteItem, renameItem, replaceItem, deleteItem);
        listView.setContextMenu(contextMenu);
        listView.setPlaceholder(new Label("<- Choose folder..."));

        listView.setCellFactory(listView -> new ListCell<File>() {
            private ImageView imageView = new ImageView();

            @Override
            public void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText("");
                } else {
                    imageView.setFitWidth(40);
                    imageView.setFitHeight(40);

                    if (item.isFile()) {
                        imageView.setImage(fileIco);
                    } else {
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

            if (event.getClickCount() == 1 && event.getButton().equals(MouseButton.PRIMARY)) {
                if (event.getEventType().equals(MouseEvent.MOUSE_CLICKED)) {
                    TreeItem<File> selectedItem = systemTree.getSelectionModel().getSelectedItem();
                    if (selectedItem.isExpanded()) {
                        File selectedDir = systemTree.getSelectionModel().getSelectedItem().getValue();
                        displayFiles(selectedDir);
                    }else {
                        currentFolderNameLable.setText(selectedItem.getValue().getName());
                        listView.getItems().clear();
                        listView.setPlaceholder(new Label("Doble click on folder to download it"));
                    }

                }

                event.consume();
            }
        });

        contextMenu.setOnShowing(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                if (listView.getSelectionModel().getSelectedItem() == null) {
                    renameItem.setVisible(false);
                    copyItem.setVisible(false);
                    replaceItem.setVisible(false);
                    deleteItem.setVisible(false);
                } else {
                    renameItem.setVisible(true);
                    copyItem.setVisible(true);
                    if (copyPath != null) {
                        pasteItem.setDisable(false);
                    }
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
        copyItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                copyPath = listView.getSelectionModel().getSelectedItem().getPath();
            }
        });

        pasteItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                copyFile();
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

    private String welcomeFunc() {
        TextInputDialog welcomeMessage = new TextInputDialog();
        welcomeMessage.setTitle("Hello!");
        welcomeMessage.setHeaderText("Please, choose the start node\n"
                + "or close/cancel dialog to use default value.");
        welcomeMessage.setContentText("Start node:");
        welcomeMessage.getEditor().setText("Root");
        welcomeMessage.showAndWait();
        String rootPath = welcomeMessage.getResult();
        if (rootPath == null) {
            return "Root";
        } else {
            return rootPath;
        }
    }

    private void showTree(String rootPath) {
        File root_directory = new File(rootPath);
        systemTree.setRoot(new TreeItem<>(root_directory));
        createTree(root_directory, null);
    }


    private void createTree(File dir, TreeItem<File> parent) {

        TreeItem<File> root = new TreeItem<>(dir);
        setDirImage(root, true);
        File[] files = dir.listFiles();

        if (parent == null) {
            systemTree.setRoot(root);
        } else {
            parent.getChildren().add(root);
        }

    }

    @FXML
    private void folderNavigation() {
        TreeItem<File> selectedItem = systemTree.getSelectionModel().getSelectedItem();

        if (selectedItem != null) {

            //There is reverse logic for goUp and goDown
            //because treeView default double click expands folder first
            if (selectedItem.isExpanded() || selectedItem.isLeaf()) {

                //goUp(selectedItem);

                //Lazy load
                lazyLoad(selectedItem);

                // Usual (in time) loading
                // goDown(selectedItem);


            } else {

                //goDown(selectedItem);

                goUp(selectedItem);

            }
        }
    }

    private void lazyLoad(TreeItem<File> selectedItem){
        ProgressIndicator progressInd = new ProgressIndicator();
        progressInd.setPrefSize(40, 40);
        progressInd.setStyle(" -fx-progress-color: black;");
        selectedItem.setGraphic(progressInd);
        listView.setPlaceholder(new Label("Downloading..."));

        //New thread
        Thread update = new Thread() {
            public void run() {
                try {
                    //Delay 2sec
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
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
    }

    private void goUp(TreeItem<File> item) {
        setDirImage(item, true);
        item.getChildren().clear();
        if (isRoot(item)) {
            displayFiles(item.getValue());
        } else {
            displayFiles(item.getParent().getValue());
        }
        collapseAllNodes(item);
    }

    private void goDown(TreeItem<File> item) {
        setDirImage(item, false);
        item.setExpanded(true);
        File currentFile = item.getValue();
        File[] leafs = currentFile.listFiles();
        for (File leaf : leafs) {
            if (leaf.isDirectory()) {
                createTree(leaf, item);
            }
        }
       String tmp = systemTree.getSelectionModel().getSelectedItem().getValue().getPath();
        if(tmp.equals(item.getValue().getPath())) {
            displayFiles(currentFile);
        }
    }

    private boolean isRoot(TreeItem<File> treeItem) {
        return treeItem.getParent() == null;
    }

    private void displayFiles(File directory) {
        currentPath = directory.getPath();
        listView.getItems().clear();
        File[] dirFiles = directory.listFiles();
        currentFolderNameLable.setText(directory.getName());
        if (dirFiles.length != 0) {

            for (File file : dirFiles) {
                listView.getItems().add(file);
            }

        } else {
            listView.setPlaceholder(new Label(directory.getName() + " is empty "));
        }
    }

    private void collapseAllNodes(TreeItem<File> item) {
        if (item != null) {
            setDirImage(item, true);
            for (TreeItem<File> child : item.getChildren()) {
                collapseAllNodes(child);
            }
        }
    }

    private void setDirImage(TreeItem<File> treeItem, boolean closed) {

        ImageView icon = new ImageView();
        icon.setFitHeight(40);
        icon.setFitWidth(40);

        if (closed) {
            icon.setImage(closedFolderIco);
        } else {
            icon.setImage(openFolderIco);
        }
        treeItem.setGraphic(icon);

    }

    private void renameFile() {

        File fileToRename = listView.getSelectionModel().getSelectedItem();

        String oldName = fileToRename.getName();
        String newName = "";

        if (fileToRename.isFile()) {
            newName = newFileName(oldName);
        } else {
            newName = newDirName(oldName);
        }

        if (newName != null) {
            String filePath = fileToRename.getPath();
            String renamedPath = filePath.replace(oldName, newName);
            File renamedFile = new File(renamedPath);
            if (!fileToRename.renameTo(renamedFile)) {
                informationAlert("Error! Cannot rename the file.");
            }

        }
        displayFiles(new File(currentPath));
    }

    //Get new file name from user
    private String newFileName(String inputFileName) {

        //Create window for input
        TextInputDialog textInputDialog = new TextInputDialog();
        textInputDialog.setHeaderText("New file name");
        textInputDialog.getEditor().setText(inputFileName);


        //While name is not valid
        String fileName;

        do {
            textInputDialog.showAndWait();
            fileName = textInputDialog.getResult();
        }
        while (!checkFileName(fileName));


        return fileName;
    }

    private String newDirName(String inputDirName) {

        //Create window for input
        TextInputDialog textInputDialog = new TextInputDialog();
        textInputDialog.setHeaderText("New dir name");
        textInputDialog.getEditor().setText(inputDirName);


        //While name is not valid
        String dirName;

        do {
            textInputDialog.showAndWait();
            dirName = textInputDialog.getResult();
        }
        while (!checkDirName(dirName));


        return dirName;
    }

    private String getNewPath(File file) {

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
            for (String dir : dirs) {
                if (checkDirName(dir)) {
                    wrongPath = false;
                } else {
                    wrongPath = true;
                    break;
                }
            }
        }
        while (wrongPath);

        return name;

    }

    //Check file name for regexp
    private boolean checkFileName(String str) {
        if (str != null) {
            Pattern p = Pattern.compile("[^?:\"<>*\\/\\|]+\\.[A-Za-z0-9]+");
            Matcher m = p.matcher(str);
            return m.matches();
        }
        return true;

    }

    //Check directory name for regexp
    private boolean checkDirName(String str) {
        if (str != null) {
            Pattern p = Pattern.compile("[^~#%&*{}\\:<>/?\\+\\|\"\\.]+");
            Matcher m = p.matcher(str);
            return m.matches();
        }
        return true;
    }

    //Copy file
    private void copyFile() {
        String name = new File(copyPath).getName();
        Path source = Paths.get(copyPath);
        Path dest = Paths.get(currentPath + "/" + name);
        if (!new File(currentPath + "/" + name).exists()) {
            try {
                Files.copy(source, dest);
                displayFiles(new File(currentPath));
            } catch (IOException ioe) {
                System.err.println(ioe.getStackTrace());
            }
        } else {
            informationAlert("File already exists");
        }
        displayFiles(new File(currentPath));
    }

    //Move file
    private void moveFile() {
        File fileToMove = listView.getSelectionModel().getSelectedItem();
        String newPath;
        newPath = getNewPath(fileToMove);
        if(!(newPath.endsWith("/") || newPath.endsWith("\\"))){
            newPath += "\\";
        }

        //If replace was successful
        if (fileToMove.renameTo(new File(newPath + fileToMove.getName()))) {

            //If replacing object was directory

            if(fileToMove.isFile()){
                System.out.println("lalishe");
            }
            if(fileToMove.isDirectory()){

                //Delete old node from tree
                TreeItem<File> deleteNode = searchTreeItem(systemTree.getRoot(), fileToMove.getPath());
                System.out.println(deleteNode.getParent().getChildren().remove(deleteNode));
            }
        }else {
            informationAlert("Cannot replace the file. Check the filepath and try again");
        }

        //Update tree and add new node
        globalDirUpdate(newPath);
        //Update files
        displayFiles(new File(currentPath));
    }

    //Delete file
    private void deleteFile() {
        File fileToDel = listView.getSelectionModel().getSelectedItem();

        String mes = "Delete " + fileToDel.getName() + "?";

        //Take confirmation
        if (confirmationAlert(mes)) {
            if (!fileToDel.delete()) {
                informationAlert("Cannot delete file. Try again later or check the file");
            }
        }
        displayFiles(new File(currentPath));

    }

    //Confirmation func
    private boolean confirmationAlert(String mes) {

        //Prepare alert
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, mes, ButtonType.YES, ButtonType.CANCEL);
        alert.showAndWait();

        //Get choice
        return alert.getResult() == ButtonType.YES;
    }

    private void informationAlert(String mes) {

        //Prepare alert
        Alert alert = new Alert(Alert.AlertType.ERROR, mes, ButtonType.CLOSE);
        alert.showAndWait();
    }

    private void createNewFile() {
        String fileName = newFileName("");
        if(fileName != null) {
            File newFile = new File(currentPath + "/" + fileName);
            try {
                if (!newFile.createNewFile()) {
                    informationAlert("Cannot create file. Check the existing files");
                }
            } catch (IOException ioe) {
                System.err.println(ioe.getStackTrace());
            }
            displayFiles(new File(currentPath));
        }
    }

    private void createNewDir() {
        String dirName = newDirName("");
        if(dirName != null) {
            File newDir = new File(currentPath + "/" + dirName);
            if (!newDir.mkdirs()) {
                informationAlert("Cannot create directory. Check the file tree.");
            }
            displayFiles(new File(currentPath));
        }
    }

    //Move and copy
    private void globalDirUpdate(String path){
        if(path.endsWith("\\") || path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        File file = new File(path);
        TreeItem<File> updateItem = searchTreeItem(systemTree.getRoot(), path);
        if(updateItem != null){
            updateItem.getChildren().clear();
            lazyLoad(updateItem);
        }
    }

    private TreeItem<File> searchTreeItem(TreeItem<File> root, String path){
        for(TreeItem<File> item : root.getChildren()){
            if(item.getValue().getPath().equals(path)){
                return item;
            }else {
                searchTreeItem(item, path);
            }
        }
        return null;
    }

}
