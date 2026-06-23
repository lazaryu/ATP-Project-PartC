package com.example.atpprojectpartc;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main entry point of the JavaFX application.
 */
public class HelloApplication extends Application {

    /**
     * Starts the JavaFX application.
     *
     * @param stage main window
     * @throws IOException if FXML cannot be loaded
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                HelloApplication.class.getResource("View/MyView.fxml")
        );

        Scene scene = new Scene(fxmlLoader.load(), 1000, 700);

        stage.setTitle("Maze Quest");
        stage.setScene(scene);

        stage.setOnCloseRequest(event -> {
            event.consume();
            Platform.exit();
            System.exit(0);
        });

        stage.show();
    }

    /**
     * Launches the application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch();
    }
}