package btmaker;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
    	stage.getIcons().add(new Image("/icon.png"));
    	stage.setTitle("BTMaker 0.2.0");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}