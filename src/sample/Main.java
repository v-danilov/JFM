package sample;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.File;

public class Main extends Application {

   /* private final Node rootIcon =
            new ImageView(new Image(getClass().getResourceAsStream("root.png")));*/

   @FXML
   private TreeView<File> systemTree;


    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");
        Scene scene = new Scene(root, 400, 300);



        File root_directory = new File("Root");

        //NullPointer
        systemTree.setRoot(new TreeItem<>(root_directory));

        createTree(root_directory, null);


        primaryStage.setScene(scene);
        primaryStage.show();



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



    public static void main(String[] args) {
        launch(args);
    }
}
