package ku.cs;

import com.github.saacsos.FXRouter;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXRouter.bind(this, stage, "SA PROJECT", 1200, 700);
        configRoute();
        FXRouter.goTo("home");
    }

    public void configRoute(){
        String packageStr = "ku/cs/";
        FXRouter.when("home", packageStr + "hello-view.fxml");
        FXRouter.when("sale-order", packageStr + "sale-order-view.fxml");
        FXRouter.when("invoice", packageStr + "invoice.fxml");
    }

    public static void main(String[] args) {
        launch();
    }
}