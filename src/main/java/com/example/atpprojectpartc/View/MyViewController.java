package com.example.atpprojectpartc.View;

import com.example.atpprojectpartc.Model.MyModel;
import com.example.atpprojectpartc.ViewModel.MyViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

/**
 * MyViewController is the controller of the main JavaFX view.
 */
public class MyViewController implements IView {

    @FXML
    private Label welcomeText;

    @FXML
    private Pane mazePane;

    private MazeDisplayer mazeDisplayer;
    private MyViewModel viewModel;

    /**
     * Initializes the controller after the FXML is loaded.
     */
    @FXML
    public void initialize() {
        viewModel = new MyViewModel(new MyModel());

        mazeDisplayer = new MazeDisplayer();
        mazeDisplayer.setWidth(500);
        mazeDisplayer.setHeight(500);

        mazePane.getChildren().add(mazeDisplayer);

        displayMessage("Maze Game - click Generate Maze");
    }

    /**
     * Handles click on the Generate Maze button.
     */
    @FXML
    protected void onGenerateMazeClicked() {
        viewModel.generateMaze(20, 20);
        mazeDisplayer.setMaze(viewModel.getMaze());
        displayMessage("Maze generated successfully");
    }

    /**
     * Displays a message to the user.
     *
     * @param message the message to display
     */
    @Override
    public void displayMessage(String message) {
        welcomeText.setText(message);
    }
}