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
import javafx.scene.input.MouseEvent;
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

        mazeDisplayer.setOnMouseMoved(this::mouseMoved);
        mazeDisplayer.setOnMouseDragged(this::mouseMoved);
        mazeContainer.setOnMouseMoved(this::mouseMoved);
        mazeContainer.setOnMouseDragged(this::mouseMoved);

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
            case NUMPAD8, DIGIT8, UP -> viewModel.moveUp();
            case NUMPAD2, DIGIT2, DOWN -> viewModel.moveDown();
            case NUMPAD4, DIGIT4, LEFT -> viewModel.moveLeft();
            case NUMPAD6, DIGIT6, RIGHT -> viewModel.moveRight();

            case NUMPAD7, DIGIT7 -> viewModel.moveUpLeft();
            case NUMPAD9, DIGIT9 -> viewModel.moveUpRight();
            case NUMPAD1, DIGIT1 -> viewModel.moveDownLeft();
            case NUMPAD3, DIGIT3 -> viewModel.moveDownRight();

            default -> {
                return;
            }
        }

        requestMazeFocus();
    }

    /**
     * Handles mouse movement over the maze.
     * If the mouse moves into a cell adjacent to the player,
     * the player tries to move to that cell.
     *
     * @param mouseEvent mouse movement event
     */
    private void mouseMoved(MouseEvent mouseEvent) {
        if (viewModel == null || viewModel.getMaze() == null || mazeDisplayer == null) {
            return;
        }

        int[][] mazeMap = viewModel.getMaze().getMaze();

        int rows = mazeMap.length;
        int columns = mazeMap[0].length;

        double cellWidth = mazeDisplayer.getWidth() / columns;
        double cellHeight = mazeDisplayer.getHeight() / rows;

        int mouseColumn = (int) (mouseEvent.getX() / cellWidth);
        int mouseRow = (int) (mouseEvent.getY() / cellHeight);

        int playerRow = viewModel.getPlayerRow();
        int playerColumn = viewModel.getPlayerColumn();

        int rowDifference = mouseRow - playerRow;
        int columnDifference = mouseColumn - playerColumn;

        if (isAdjacentCell(rowDifference, columnDifference)) {
            viewModel.moveByDelta(rowDifference, columnDifference);
        }

        requestMazeFocus();
    }

    /**
     * Checks if the target cell is adjacent to the player's current cell.
     *
     * @param rowDifference row difference
     * @param columnDifference column difference
     * @return true if the cell is adjacent
     */
    private boolean isAdjacentCell(int rowDifference, int columnDifference) {
        boolean sameCell = rowDifference == 0 && columnDifference == 0;

        return !sameCell
                && Math.abs(rowDifference) <= 1
                && Math.abs(columnDifference) <= 1;
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

        setStatusText("Move with arrows, NumPad 8/2/4/6/7/9/1/3, or move the mouse over adjacent cells.");
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