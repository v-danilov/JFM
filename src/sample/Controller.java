package sample;

import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;

import java.io.File;

public class Controller {

    @FXML
    private void showFiles(){
        System.out.println("Hello");

        File root_file = new File("C://");
        File[] files = root_file.listFiles();
        TreeItem<String> tree_route = new TreeItem<String>(root_file.getName());
        for(File f : files){
            System.out.println(f.getName());
            TreeItem<String> file_tree_item = new TreeItem<>(f.getName());

            tree_route.getChildren().add(file_tree_item);
        }
    }

}
