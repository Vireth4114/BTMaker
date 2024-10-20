package BTMaker.BTMaker;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
    	stage.getIcons().add(new Image("/icon.png"));
    	stage.setTitle("BTMaker 0.2.0");
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/btMaker.fxml"));
        Controller controller = new Controller();
        fxmlLoader.setController(controller);
        scene = new Scene(fxmlLoader.load(), 1200, 675);
        scene.addEventFilter(KeyEvent.KEY_PRESSED, evt -> controller.onKeyPress(evt));
        scene.setOnKeyReleased(evt -> controller.onKeyReleased(evt));
        scene.getStylesheets().add("/test.css");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setMinWidth(950);
        stage.setMinHeight(500);
        stage.widthProperty().addListener((o, p, n) -> controller.resetPane());
        stage.heightProperty().addListener((o, p, n) -> controller.resetPane());
        controller.cursor.bindBidirectional(scene.cursorProperty());
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}