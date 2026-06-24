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

import java.io.IOException;

/**
 * Main entry point of the JavaFX application.
 * Creates and connects the Model, ViewModel, and View.
 */
public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                HelloApplication.class.getResource("View/MyView.fxml")
        );

        Pane root = fxmlLoader.load();

        IModel model = new MyModel();
        MyViewModel viewModel = new MyViewModel(model);

        MyViewController controller = fxmlLoader.getController();
        controller.setViewModel(viewModel);

        Scene scene = new Scene(root, 900, 650);

        controller.setSceneEvents(scene);
        stage.setOnCloseRequest(controller::handleWindowClose);

        stage.setTitle("Maze Game");
        stage.setScene(scene);
        stage.show();

        controller.requestMazeFocus();
    }

    public static void main(String[] args) {
        launch();
    }
}
