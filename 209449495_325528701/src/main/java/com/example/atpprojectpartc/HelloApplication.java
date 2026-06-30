package com.example.atpprojectpartc;
import com.example.atpprojectpartc.Model.IModel;
import com.example.atpprojectpartc.Model.MyModel;
import com.example.atpprojectpartc.View.MyViewController;
import com.example.atpprojectpartc.ViewModel.MyViewModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Main entry point of the JavaFX application.
 * Creates and connects the Model, ViewModel, and View.
 */
public class HelloApplication extends Application {

    private static final Logger logger = LogManager.getLogger(HelloApplication.class);

    /**
     * Starts the JavaFX application.
     * Loads the FXML file, creates the MVVM layers,
     * connects the controller to the ViewModel,
     * and displays the main window.
     *
     * @param stage main application stage
     * @throws IOException if the FXML file cannot be loaded
     */
    @Override
    public void start(Stage stage) throws IOException {
        logger.info("Starting PAC-MAZE JavaFX application.");

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(
                    HelloApplication.class.getResource("View/MyView.fxml")
            );

            logger.info("Loading main FXML file: View/MyView.fxml");

            Pane root = fxmlLoader.load();

            logger.info("FXML loaded successfully.");

            IModel model = new MyModel();
            MyViewModel viewModel = new MyViewModel(model);

            logger.info("Model and ViewModel were created successfully.");

            MyViewController controller = fxmlLoader.getController();

            if (controller == null) {
                logger.error("FXML controller is null. Application cannot continue.");
                throw new IllegalStateException("FXML controller is null.");
            }

            controller.setViewModel(viewModel);

            Scene scene = new Scene(root, 900, 650);

            controller.setSceneEvents(scene);
            stage.setOnCloseRequest(controller::handleWindowClose);

            stage.setTitle("Maze Game");
            stage.setScene(scene);
            stage.show();

            logger.info("Main stage was shown successfully.");

            controller.requestMazeFocus();

        } catch (IOException e) {
            logger.error("Failed to load the JavaFX application.", e);
            throw e;

        } catch (Exception e) {
            logger.fatal("Unexpected error while starting the application.", e);
            throw e;
        }
    }

    /**
     * Application main method.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        logger.info("Launching PAC-MAZE application.");
        launch(args);
    }
}
