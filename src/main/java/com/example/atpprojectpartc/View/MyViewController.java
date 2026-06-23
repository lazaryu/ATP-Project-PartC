package com.example.atpprojectpartc.View;

import com.example.atpprojectpartc.ViewModel.MyViewModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;

import java.util.Observable;
import java.util.Observer;

/**
 * MyViewController is the View layer controller.
 * It receives user actions from the GUI, forwards them to the ViewModel,
 * and updates the maze display when the ViewModel notifies about changes.
 */
@SuppressWarnings("deprecation")
public class MyViewController implements IView, Observer {

    @FXML
    private TextField textField_mazeRows;

    @FXML
    private TextField textField_mazeColumns;

    @FXML
    private Pane mazeContainer;

    @FXML
    private Label statusLabel;

    private MazeDisplayer mazeDisplayer;
    private MyViewModel viewModel;

    /**
     * Initializes the custom MazeDisplayer and places it inside the maze container.
     * This method is called automatically after the FXML file is loaded.
     */
    @FXML
    public void initialize() {
        mazeDisplayer = new MazeDisplayer();

        mazeDisplayer.widthProperty().bind(mazeContainer.widthProperty());
        mazeDisplayer.heightProperty().bind(mazeContainer.heightProperty());

        mazeContainer.getChildren().add(mazeDisplayer);
        mazeContainer.setFocusTraversable(true);

        mazeContainer.widthProperty().addListener((observable, oldValue, newValue) -> redrawMaze());
        mazeContainer.heightProperty().addListener((observable, oldValue, newValue) -> redrawMaze());
    }

    /**
     * Connects the controller to the ViewModel.
     *
     * @param viewModel the ViewModel layer
     */
    @Override
    public void setViewModel(MyViewModel viewModel) {
        this.viewModel = viewModel;
        this.viewModel.addObserver(this);
    }

    /**
     * Generates a new maze according to the rows and columns from the GUI.
     * This method is connected to the Generate Maze button in the FXML.
     */
    @FXML
    public void generateMaze() {
        if (viewModel == null) {
            displayMessage("ViewModel is not connected.");
            return;
        }

        try {
            int rows = Integer.parseInt(textField_mazeRows.getText());
            int columns = Integer.parseInt(textField_mazeColumns.getText());

            if (rows < 2 || columns < 2) {
                displayMessage("Maze size must be at least 2x2.");
                return;
            }

            viewModel.generateMaze(rows, columns);
            requestMazeFocus();

        } catch (NumberFormatException e) {
            displayMessage("Please enter valid numbers for rows and columns.");
        }
    }

    /**
     * Handles keyboard movement.
     * The required movement keys are:
     * 8 up, 2 down, 4 left, 6 right,
     * 7 up-left, 9 up-right, 1 down-left, 3 down-right.
     *
     * @param keyEvent keyboard event
     */
    @FXML
    public void keyPressed(KeyEvent keyEvent) {
        if (viewModel == null || viewModel.getMaze() == null) {
            return;
        }

        handleKeyPressed(keyEvent.getCode());
        keyEvent.consume();
    }

    /**
     * Converts a pressed key into a movement command.
     *
     * @param code pressed key code
     */
    private void handleKeyPressed(KeyCode code) {
        switch (code) {
            case NUMPAD8, UP -> viewModel.moveUp();
            case NUMPAD2, DOWN -> viewModel.moveDown();
            case NUMPAD4, LEFT -> viewModel.moveLeft();
            case NUMPAD6, RIGHT -> viewModel.moveRight();

            case NUMPAD7 -> viewModel.moveUpLeft();
            case NUMPAD9 -> viewModel.moveUpRight();
            case NUMPAD1 -> viewModel.moveDownLeft();
            case NUMPAD3 -> viewModel.moveDownRight();

            default -> {
                return;
            }
        }

        requestMazeFocus();
    }

    /**
     * Receives notifications from the ViewModel.
     *
     * @param observable the observable object
     * @param arg update message
     */
    @Override
    public void update(Observable observable, Object arg) {
        Platform.runLater(() -> {
            if (arg == null) {
                return;
            }

            String updateMessage = arg.toString();

            switch (updateMessage) {
                case "mazeGenerated" -> handleMazeGenerated();
                case "playerMoved" -> handlePlayerMoved();
                case "gameWon" -> handleGameWon();
                default -> System.out.println("Unknown update: " + updateMessage);
            }
        });
    }

    /**
     * Updates the view after a new maze was generated.
     */
    private void handleMazeGenerated() {
        if (mazeDisplayer == null) {
            return;
        }

        mazeDisplayer.setMaze(
                viewModel.getMaze(),
                viewModel.getPlayerRow(),
                viewModel.getPlayerColumn()
        );

        setStatusText("Use NumPad 8/2/4/6 and 7/9/1/3 to move.");
        requestMazeFocus();
    }

    /**
     * Updates the player location on the maze display.
     */
    private void handlePlayerMoved() {
        if (mazeDisplayer == null) {
            return;
        }

        mazeDisplayer.updatePlayerPosition(
                viewModel.getPlayerRow(),
                viewModel.getPlayerColumn()
        );

        setStatusText("Player: (" +
                viewModel.getPlayerRow() +
                ", " +
                viewModel.getPlayerColumn() +
                ")");
    }

    /**
     * Handles winning the game.
     */
    private void handleGameWon() {
        if (mazeDisplayer == null) {
            return;
        }

        mazeDisplayer.updatePlayerPosition(
                viewModel.getPlayerRow(),
                viewModel.getPlayerColumn()
        );

        setStatusText("You solved the maze!");
        displayMessage("Great job! You solved the maze.");
    }

    /**
     * Connects keyboard events to the scene.
     * Call this method from HelloApplication after creating the Scene.
     *
     * @param scene the main scene
     */
    public void setSceneEvents(Scene scene) {
        scene.setOnKeyPressed(this::keyPressed);
    }

    /**
     * Redraws the maze after resizing.
     */
    private void redrawMaze() {
        if (mazeDisplayer != null && viewModel != null && viewModel.getMaze() != null) {
            mazeDisplayer.setMaze(
                    viewModel.getMaze(),
                    viewModel.getPlayerRow(),
                    viewModel.getPlayerColumn()
            );
        }
    }

    /**
     * Requests keyboard focus for the maze container.
     */
    public void requestMazeFocus() {
        if (mazeContainer != null) {
            mazeContainer.requestFocus();
        }
    }

    /**
     * Updates the status label safely.
     *
     * @param text status text
     */
    private void setStatusText(String text) {
        if (statusLabel != null) {
            statusLabel.setText(text);
        }
    }

    /**
     * Displays an information alert to the user.
     *
     * @param message message to display
     */
    @Override
    public void displayMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Maze Game");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}