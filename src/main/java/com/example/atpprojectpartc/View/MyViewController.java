package com.example.atpprojectpartc.View;

import com.example.atpprojectpartc.Model.MyModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * MyViewController is the controller of the main JavaFX view.
 * It handles user events from the GUI.
 */
public class MyViewController implements IView {

    @FXML
    private Label welcomeText;

    private MyModel model;

    /**
     * Initializes the controller after the FXML is loaded.
     */
    @FXML
    public void initialize() {
        model = new MyModel();
    }

    /**
     * Handles a click on the test button.
     */
    @FXML
    protected void onHelloButtonClick() {
        model.generateMaze(10, 10);
        displayMessage("Maze generated successfully! Check the console.");
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