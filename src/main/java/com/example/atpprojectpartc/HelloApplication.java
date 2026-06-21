package com.example.atpprojectpartc;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main entry point of the JavaFX application.
 * Loads the main view of the maze game.
 */
public class HelloApplication extends Application {

    /**
     * Starts the JavaFX application.
     * @param stage the main application window
     * @throws IOException if the FXML file cannot be loaded
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                HelloApplication.class.getResource("View/MyView.fxml")
        );

        Scene scene = new Scene(fxmlLoader.load(), 900, 650);

        stage.setTitle("Maze Game");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Launches the JavaFX application.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch();
    }
}