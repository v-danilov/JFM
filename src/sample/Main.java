package sample;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javax.swing.text.html.ImageView;
import java.io.File;

public class Main extends Application {

   /* private final Node rootIcon =
            new ImageView(new Image(getClass().getResourceAsStream("root.png")));*/

   @FXML
   TreeView<String> treeView;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");

        File root_file = new File("C://");
        File[] files = root_file.listFiles();
        TreeItem<String> tree_route = new TreeItem<String>(root_file.getName());
        tree_route.setExpanded(true);
        for(File f : files){
            TreeItem<String> file_tree_item = new TreeItem<>(f.getName());
            tree_route.getChildren().add(file_tree_item);
        }

        VBox box = new VBox();
        final Scene scene = new Scene(box, 400, 300);
        scene.setFill(Color.LIGHTGRAY);

        treeView = new TreeView<>(tree_route);

        box.getChildren().add(treeView);
        primaryStage.setScene(scene);
        primaryStage.show();



    }


    public static void main(String[] args) {
        launch(args);
    }
}
