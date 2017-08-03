package sample;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.WindowEvent;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controller {

    //Icons
    private final Image closedFolderIco = new Image((getClass().getClassLoader().getResource("images/closedFolder.png")).toString());
    private final Image openFolderIco = new Image((getClass().getClassLoader().getResource("images/openFolder.png")).toString());
    private final Image fileIco = new Image((getClass().getClassLoader().getResource("images/fileIco.png")).toString());
    @FXML
    private TreeView<Directory> systemTree;
    @FXML
    private Button showButton;
    @FXML
    private TableView<File> filesTable;
    @FXML
    private ListView<File> listView;
    @FXML
    private Label currentFolderNameLable;
    //Context
    private ContextMenu contextMenu = new ContextMenu();

    //Create context
    private Menu createFileMenu = new Menu("Create new...");
    private MenuItem createFileItem = new MenuItem("File");
    private MenuItem createDirItem = new MenuItem("Directory");

    //Context functions
    private MenuItem renameItem = new MenuItem("Rename");
    private MenuItem copyItem = new MenuItem("Copy");
    private MenuItem pasteItem = new MenuItem("Paste");
    private MenuItem replaceItem = new MenuItem("Replace");
    private MenuItem deleteItem = new MenuItem("Delete");

    //Paths
    private String currentPath;
    private String copyPath;


    @FXML
    public void initialize() {

        //Alerting welcome message
        //currentPath = createOSTree();

        //Creating tree
        //createTree(currentPath);
        createOSTree();

        //Creating context menu
        createFileMenu.getItems().addAll(createFileItem, createDirItem);
        pasteItem.setDisable(true);
        contextMenu.getItems().addAll(createFileMenu, copyItem, pasteItem, renameItem, replaceItem, deleteItem);

        //Prepare list view
        listView.setContextMenu(contextMenu);
        listView.setPlaceholder(new Label("<- Choose folder..."));

        //Add images to list view rows
        listView.setCellFactory(listView -> new ListCell<File>() {
            private ImageView imageView = new ImageView();

            @Override
            public void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText("");
                } else {

                    //size of icon
                    imageView.setFitWidth(40);
                    imageView.setFitHeight(40);

                    //type of icon
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

        //Override default tree controls (double/single clicks)
        systemTree.addEventHandler(MouseEvent.ANY, event -> {

            //Double click - dive ito level
            if (event.getClickCount() == 2 && event.getButton().equals(MouseButton.PRIMARY)) {
                if (event.getEventType().equals(MouseEvent.MOUSE_CLICKED)) {
                    folderNavigation();
                }
                event.consume();
            }

            //Single click - show files in floder
            if (event.getClickCount() == 1 && event.getButton().equals(MouseButton.PRIMARY)) {
                if (event.getEventType().equals(MouseEvent.MOUSE_CLICKED)) {
                    TreeItem<Directory> selectedItem = systemTree.getSelectionModel().getSelectedItem();
                    if (selectedItem.isExpanded()) {
                        File selectedDir = systemTree.getSelectionModel().getSelectedItem().getValue();
                        displayFiles(selectedDir);
                    } else {
                        currentFolderNameLable.setText(selectedItem.getValue().getPath());
                        listView.getItems().clear();
                        listView.setPlaceholder(new Label("Double click on folder to open it"));
                    }

                }

                event.consume();
            }
        });

        //Prepearing context menu
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
                    replaceItem.setVisible(true);
                    deleteItem.setVisible(true);
                }
            }
        });

        /*
         * Handle context items
         */

        //Create new file
        createFileItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                createNewFile();
            }
        });

        //Create new directory
        createDirItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                createNewDir();
            }
        });

        //Rename file/directory
        renameItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                renameFile();
            }
        });

        //Copy file/directory
        copyItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                copyPath = listView.getSelectionModel().getSelectedItem().getPath();
                pasteItem.setDisable(false);
            }
        });

        //Paste file/directory
        pasteItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                pasteFile();
            }
        });

        //Move file/directory
        replaceItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                moveFile();
            }
        });

        //Delete file/directory
        deleteItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                deleteFile();
            }
        });

    }

    /**
     * Welcome message function.
     * The function creates text input dialog and gets the root path of file system.
     * Default value for root is local "Root" folder.
     *
     * @return Path to the system root.
     */
    private void createOSTree() {

        String OSname = System.getProperty("os.name");

        if (OSname.contains("Windows")) {
            File[] roots = File.listRoots();
            TreeItem<Directory> dummyRoot = new TreeItem<>();
            systemTree.setRoot(dummyRoot);
            systemTree.setShowRoot(false);
            for (int i = 0; i < roots.length; i++) {
                if (roots[i].isHidden()) {
                    createLevel(roots[i], dummyRoot);
                }
            }

        } else {
            createTree(System.getProperty("user.home"));
        }
    }

    /**
     * Create tree function.
     * It creates the file system tree.
     *
     * @param rootPath path to the current level root
     */
    private void createTree(String rootPath) {
        Directory root_directory = new Directory(rootPath);
        systemTree.setRoot(new TreeItem<>(root_directory));
        createLevel(root_directory, null);
    }

    /**
     * Create level function.
     * Create a tree level. Add directory to current root.System.out.println(System.getProperty("os.name"));
     System.out.println(System.getProperty("user.home"));

     *
     * @param dir    directory to add
     * @param parent parent tree item contains the directory
     */
    private void createLevel(File dir, TreeItem<Directory> parent) {
        TreeItem<Directory> root = new TreeItem<>(new Directory(dir));
        setDirImage(root, true);
        if (parent == null) {
            systemTree.setRoot(root);
        } else {
            parent.getChildren().add(root);
        }

    }


    /*
     * Folder navigation function.
     */

    private void folderNavigation() {
        TreeItem<Directory> selectedItem = systemTree.getSelectionModel().getSelectedItem();

        if (selectedItem != null) {

            /*
             * There is reverse logic for goUp and goDown
             * because treeView default double click expands folder first
             */

            //If folder is open or folder doesnt have children -> go to the lower level
            if (selectedItem.isExpanded() || selectedItem.isLeaf()) {

                //goUp(selectedItem);

                //Lazy load
                lazyLoad(selectedItem);

                /*
                 *  For usual (in time) loading use:
                 *  //goDown(selectedItem);
                 */


            }

            //Else -> go to the upper level and close the folder
            else {

                //goDown(selectedItem);

                goUp(selectedItem);

            }
        }
    }

    /**
     * Lazy loading fuction.
     * This function downloads content of the folder in tray.
     *
     * @param selectedItem selected tree item (folder to be downloaded)
     */
    private void lazyLoad(TreeItem<Directory> selectedItem) {

        //Show progress indicator
        ProgressIndicator progressInd = new ProgressIndicator();
        progressInd.setPrefSize(40, 40);
        progressInd.setStyle(" -fx-progress-color: black;");
        selectedItem.setGraphic(progressInd);

        //Show message of the process
        if (!selectedItem.getValue().getWasOpenned()) {
            listView.setPlaceholder(new Label("Downloading..."));
        }

        //Create new thread
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
        //Start thread
        update.start();
    }

    /**
     * Go up function.
     * Rise to the upper level of the current tree view item (folder).
     *
     * @param item selected tree item
     */
    private void goUp(TreeItem<Directory> item) {
        setDirImage(item, true);
        if (isRoot(item)) {
            displayFiles(item.getValue());
        } else {
            displayFiles(item.getParent().getValue());
        }
        collapseAllNodes(item);
    }

    /**
     * Go down funtion.
     * Dive into the lower level of current tree view item (folder).
     *
     * @param item selected tree item
     */
    private void goDown(TreeItem<Directory> item) {
        setDirImage(item, false);
        Directory currentDir = item.getValue();
        ArrayList<Directory> subDirs = currentDir.listDirectories();

        //Download the folder if it not done previously
        if (!item.getValue().getWasOpenned()) {
            for (File dir : subDirs) {
                createLevel(dir, item);
            }
            item.getValue().open();
        }

        //If it was downloaded earlier
        else {

            //Get tree nodes
            ObservableList<TreeItem<Directory>> children = item.getChildren();
            ArrayList<File> fileArrayList = new ArrayList<>();
            int size = children.size();

            //Get files
            for (TreeItem<Directory> ch : children) {
                fileArrayList.add(ch.getValue());
            }

            //Compare uploaded nodes and current file system data
            boolean check = new HashSet(subDirs).equals(new HashSet(fileArrayList));

            //If data has differences update the tree root.
            if (!check) {
                item.getChildren().clear();
                for (File dir : subDirs) {
                    createLevel(dir, item);
                }
            }
        }

        //Display files of the current folder
        String tmp = systemTree.getSelectionModel().getSelectedItem().getValue().getPath();
        if (tmp.equals(item.getValue().getPath())) {
            displayFiles(currentDir);
        }
    }

    /**
     * Defines the upper item of the tree
     *
     * @param treeItem current item
     * @return True if the item is the root of tree.
     */
    private boolean isRoot(TreeItem<Directory> treeItem) {
        if (treeItem.getParent() == null || treeItem.getParent().getValue() == null) {
            return true;
        }
        return false;
    }

    /**
     * Display folder content to the list view.
     *
     * @param directory folder to be content displayed
     */
    private void displayFiles(File directory) {

        currentPath = directory.getPath();
        listView.getItems().clear();

        //Ignore macOS files ".DS_Store"
        File[] dirFiles = directory.listFiles((dir, name) -> !(name.equals(".DS_Store") || !dir.isHidden()));
        currentFolderNameLable.setText(directory.getPath());
        if (dirFiles.length != 0) {

            for (File file : dirFiles) {
                listView.getItems().add(file);
            }

        }
        //Display nessage about empty folder
        else {
            listView.setPlaceholder(new Label(directory.getName() + " is empty "));
        }
    }

    /**
     * Collapse all children nodes of the tree
     *
     * @param item start node to collapse
     */
    private void collapseAllNodes(TreeItem<Directory> item) {
        if (item != null) {
            setDirImage(item, true);
            for (TreeItem<Directory> child : item.getChildren()) {
                collapseAllNodes(child);
            }
        }
    }

    /**
     * Set image of the folder to the item
     *
     * @param treeItem item for set graphics
     * @param closed   type of image. True means closed folder, False - opened folder.
     */
    private void setDirImage(TreeItem<Directory> treeItem, boolean closed) {

        ImageView icon = new ImageView();

        //Icon size
        icon.setFitHeight(40);
        icon.setFitWidth(40);

        if (closed) {
            icon.setImage(closedFolderIco);
            treeItem.setExpanded(false);
        } else {
            icon.setImage(openFolderIco);
            treeItem.setExpanded(true);
        }

        treeItem.setGraphic(icon);

    }

    /**
     * Rename file function
     */
    private void renameFile() {

        File fileToRename = listView.getSelectionModel().getSelectedItem();

        String oldName = fileToRename.getName();
        String newName = "";
        TreeItem<Directory> renamedItem = searchTreeItem(systemTree.getRoot(), fileToRename.getPath());

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
            renamedItem.setValue(new Directory(newName));
        }
        displayFiles(renamedItem.getValue());
    }

    /**
     * Creates a new name for file
     *
     * @param inputFileName old file name
     * @return new valid file name
     */
    private String newFileName(String inputFileName) {

        //Create window for input
        TextInputDialog textInputDialog = new TextInputDialog();
        textInputDialog.setHeaderText("New file name");
        textInputDialog.getEditor().setText(inputFileName);


        String fileName;

        //While name is not valid
        do {
            textInputDialog.showAndWait();
            fileName = textInputDialog.getResult();
        }
        while (!checkFileName(fileName));


        return fileName;
    }

    /**
     * Creates a new name for directory
     *
     * @param inputDirName old directory name
     * @return new valid directory name
     */
    private String newDirName(String inputDirName) {

        //Create window for input
        TextInputDialog textInputDialog = new TextInputDialog();
        textInputDialog.setHeaderText("New dir name");
        textInputDialog.getEditor().setText(inputDirName);


        String dirName;

        //While name is not valid
        do {
            textInputDialog.showAndWait();
            dirName = textInputDialog.getResult();
        }
        while (!checkDirName(dirName));


        return dirName;
    }

    /**
     * Gets new path for the file
     *
     * @param file file requires new path
     * @return new valid path for the file
     */
    private String getNewPath(File file) {

        String pathOnly = file.getParent();

        //Create window for input
        TextInputDialog textInputDialog = new TextInputDialog();
        textInputDialog.setHeaderText("New path for " + file.getName());

        //Fil old name
        textInputDialog.getEditor().setText(pathOnly);


        String name;
        boolean wrongPath = true;

        //While name is not valid
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

    //Maybe its possible to combine two "check" functions in one

    /**
     * Validate file name for normal format
     *
     * @param str file name
     * @return true, when name is valid; otherwise - false
     */
    private boolean checkFileName(String str) {
        if (str != null) {
            Pattern p = Pattern.compile("[^?:\"<>*\\/\\|]+\\.[A-Za-z0-9]+");
            Matcher m = p.matcher(str);
            return m.matches();
        }
        return true;

    }

    /**
     * Validate directory name for normal format
     *
     * @param str directory name
     * @return true, when name is valid; otherwise - false
     */
    private boolean checkDirName(String str) {
        if (str != null) {
            Pattern p = Pattern.compile("[^~#%&*{}\\:<>/?\\+\\|\"\\.]+");
            Matcher m = p.matcher(str);
            return m.matches();
        }
        return true;
    }

    /**
     * Paste file or directory in the current path
     */
    private void pasteFile() {

        File copiedFile = new File(copyPath);
        Path source = Paths.get(copyPath);
        Path dest = Paths.get(currentPath + "/" + copiedFile.getName());

        File pastedFile = new File(dest.toString());

        if (!pastedFile.exists()) {
            try {

                if (copiedFile.isDirectory()) {
                    FileUtils.copyDirectory(copiedFile, pastedFile);
                } else {
                    Files.copy(source, dest);
                }

                globalDirUpdate(dest.getParent().toString());
                //displayFiles(pasteFile.getParentFile());
            } catch (IOException ioe) {
                System.err.println(ioe.getStackTrace());
            }
        } else {
            informationAlert("File already exists");
        }
    }

    /**
     * Move file/directory to the specific path defined by user
     */
    private void moveFile() {
        File fileToMove = listView.getSelectionModel().getSelectedItem();
        String newPath;
        newPath = getNewPath(fileToMove);

        if (!(newPath.endsWith("/") || newPath.endsWith("\\"))) {
            newPath += "/";
        }

        boolean isDirectory = fileToMove.isDirectory();

        //If replace was successful
        if (fileToMove.renameTo(new File(newPath + fileToMove.getName()))) {

            //If replaced object was a directory
            if (isDirectory) {

                //Delete old node from tree
                TreeItem<Directory> deleteNode = searchTreeItem(systemTree.getRoot(), fileToMove.getPath());
                deleteNode.getParent().getChildren().remove(deleteNode);
                displayFiles(new File(currentPath));

                //Update tree and add new node
                globalDirUpdate(newPath);

            }
        } else {
            informationAlert("Cannot replace the file. Check the filepath and try again");
        }
    }

    /**
     * Delete file from the system.
     */
    private void deleteFile() {
        File fileToDel = listView.getSelectionModel().getSelectedItem();

        String mes = "Delete " + fileToDel.getName() + "?";

        //Get confirmation
        if (confirmationAlert(mes)) {
            if (fileToDel.delete()) {
                TreeItem<Directory> itemToDel = searchTreeItem(systemTree.getRoot(), fileToDel.getPath());
                itemToDel.getParent().getChildren().remove(itemToDel);
                displayFiles(new File(currentPath));
            } else {
                informationAlert("Cannot delete file. Try again later or check the file");
            }
        }

    }

    /**
     * Message visualizer. Displays messages in a new window and get confirmation from user.
     *
     * @param mes message to display
     * @return true when user agree an action; otherwise - false
     */
    private boolean confirmationAlert(String mes) {

        //Prepare alert
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, mes, ButtonType.YES, ButtonType.CANCEL);
        alert.showAndWait();

        //Get choice
        return alert.getResult() == ButtonType.YES;
    }

    /**
     * Message visualizer. Displays information in a new window.
     *
     * @param mes message to display
     */
    private void informationAlert(String mes) {
        //Prepare alert
        Alert alert = new Alert(Alert.AlertType.ERROR, mes, ButtonType.CLOSE);
        alert.showAndWait();
    }

    /**
     * Create new file function.
     */
    private void createNewFile() {

        //Create new file name
        String fileName = newFileName("");

        if (fileName != null) {

            //Create file
            File newFile = new File(currentPath + "/" + fileName);
            try {
                if (!newFile.createNewFile()) {
                    informationAlert("Cannot create file. Check the existing files");
                }
            } catch (IOException ioe) {
                System.err.println(ioe.getStackTrace());
            }

            //Update files list
            displayFiles(new File(currentPath));
        }
    }

    /**
     * Create new directory function
     */
    private void createNewDir() {

        //Create new directory name
        String dirName = newDirName("");

        if (dirName != null) {

            //Create directory
            File newDir = new File(currentPath + "/" + dirName);

            if (!newDir.mkdirs()) {
                informationAlert("Cannot create directory. Check the file tree.");
            }

            //Update files list
            globalDirUpdate(newDir.getParent());
            //displayFiles(new File(currentPath));

        }
    }

    /**
     * Update tree item by file path. In case of move or replacing files in the different levels
     * search the items and update it
     *
     * @param path file path to update the corresponding item
     */
    private void globalDirUpdate(String path) {
        if (path.endsWith("\\") || path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        TreeItem<Directory> updateItem = searchTreeItem(systemTree.getRoot(), path);
        if (updateItem != null && updateItem.getValue().getWasOpenned()) {
            lazyLoad(updateItem);
        }
    }

    /**
     * Search item in a tree by path name of the file (directory)
     *
     * @param root root of the tree
     * @param path file path to find item
     * @return searched item
     */
    private TreeItem<Directory> searchTreeItem(TreeItem<Directory> root, String path) {
        TreeItem<Directory> result;
        if (root.getValue().getPath().equals(path)) return root;
        for (TreeItem<Directory> item : root.getChildren()) {
            result = searchTreeItem(item, path);
            if (result != null) return result;
        }
        return null;
    }
}
