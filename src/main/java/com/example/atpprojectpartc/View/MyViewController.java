package com.example.atpprojectpartc.View;
import com.example.atpprojectpartc.ViewModel.MyViewModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.Observable;
import java.util.Observer;
import java.util.Optional;

/**
 * MyViewController is the View layer controller.
 * It receives user actions from the GUI, forwards them to the ViewModel,
 * and updates the maze display when the ViewModel notifies about changes.
 */
@SuppressWarnings("deprecation")
public class MyViewController implements IView, Observer {

    @FXML
    private VBox startPane;

    @FXML
    private VBox setupPane;

    @FXML
    private BorderPane gamePane;

    @FXML
    private TextField rowsTextField;

    @FXML
    private TextField columnsTextField;

    @FXML
    private Pane mazePane;

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

        mazeDisplayer.widthProperty().bind(mazePane.widthProperty());
        mazeDisplayer.heightProperty().bind(mazePane.heightProperty());

        mazePane.getChildren().add(mazeDisplayer);
        mazePane.setFocusTraversable(true);
        mazeDisplayer.setFocusTraversable(true);

        mazeDisplayer.setOnMouseMoved(this::mouseMoved);
        mazeDisplayer.setOnMouseDragged(this::mouseMoved);
        mazePane.setOnMouseMoved(this::mouseMoved);
        mazePane.setOnMouseDragged(this::mouseMoved);

        mazePane.widthProperty().addListener((observable, oldValue, newValue) -> redrawMaze());
        mazePane.heightProperty().addListener((observable, oldValue, newValue) -> redrawMaze());

        showStartScreen();
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
     * Handles click on Start Game.
     */
    @FXML
    public void onStartGameClicked() {
        showSetupScreen();
    }

    /**
     * Handles click on Back.
     */
    @FXML
    public void onBackToStartClicked() {
        showStartScreen();
    }

    /**
     * Handles click on Generate Maze.
     */
    @FXML
    public void onGenerateMazeClicked() {
        if (viewModel == null) {
            displayMessage("ViewModel is not connected.");
            return;
        }

        try {
            int rows = Integer.parseInt(rowsTextField.getText().trim());
            int columns = Integer.parseInt(columnsTextField.getText().trim());

            if (rows < 3 || columns < 3) {
                showErrorAlert("Invalid maze size", "Rows and columns must be at least 3.");
                return;
            }

            if (rows > 100 || columns > 100) {
                showErrorAlert("Invalid maze size", "Rows and columns must be 100 or less.");
                return;
            }

            viewModel.generateMaze(rows, columns);
            requestMazeFocus();

        } catch (NumberFormatException e) {
            showErrorAlert("Invalid input", "Rows and columns must be numbers.");
        }
    }

    /**
     * Handles click on Show Solution.
     */
    @FXML
    public void onShowSolutionClicked() {
        showInformationAlert(
                "Solution",
                "In the next step we will connect this button to the solver from Part B."
        );

        requestMazeFocus();
    }

    /**
     * Handles click on New Maze.
     */
    @FXML
    public void onNewMazeClicked() {
        showSetupScreen();
    }

    /**
     * Handles click on Exit Game.
     */
    @FXML
    public void onExitGameClicked() {
        boolean shouldExit = showConfirmationAlert(
                "Exit Game",
                "Are you sure you want to leave the current maze?"
        );

        if (shouldExit) {
            showStartScreen();
        } else {
            requestMazeFocus();
        }
    }

    /**
     * Handles click on Exit.
     */
    @FXML
    public void onExitClicked() {
        cleanExit();
    }

    /**
     * Handles keyboard movement.
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
        if (!gamePane.isVisible()) {
            return;
        }

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

        if (!gamePane.isVisible()) {
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
        showGameScreen();
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
        showInformationAlert("Victory", "Great job! You solved the maze.");
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
        Platform.runLater(() -> {
            if (mazePane != null) {
                mazePane.requestFocus();
            }

            if (mazeDisplayer != null) {
                mazeDisplayer.requestFocus();
            }
        });
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
     * Shows the start screen.
     */
    private void showStartScreen() {
        setPaneVisible(startPane, true);
        setPaneVisible(setupPane, false);
        setPaneVisible(gamePane, false);
    }

    /**
     * Shows the setup screen.
     */
    private void showSetupScreen() {
        setPaneVisible(startPane, false);
        setPaneVisible(setupPane, true);
        setPaneVisible(gamePane, false);
    }

    /**
     * Shows the game screen.
     */
    private void showGameScreen() {
        setPaneVisible(startPane, false);
        setPaneVisible(setupPane, false);
        setPaneVisible(gamePane, true);
    }

    /**
     * Sets node visibility and managed status.
     *
     * @param node node to update
     * @param visible true if visible
     */
    private void setPaneVisible(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    /**
     * Displays an information alert to the user.
     *
     * @param message message to display
     */
    @Override
    public void displayMessage(String message) {
        showInformationAlert("Maze Game", message);
    }

    /**
     * Shows error alert.
     *
     * @param title alert title
     * @param content alert content
     */
    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Shows information alert.
     *
     * @param title alert title
     * @param content alert content
     */
    private void showInformationAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Shows confirmation alert.
     *
     * @param title alert title
     * @param content alert content
     * @return true if confirmed
     */
    private boolean showConfirmationAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Exits the application cleanly.
     */
    private void cleanExit() {
        boolean shouldExit = showConfirmationAlert(
                "Exit",
                "Are you sure you want to exit PAC-MAZE?"
        );

        if (shouldExit) {
            if (viewModel != null) {
                viewModel.stopProgram();
            }

            Platform.exit();
            System.exit(0);
        }
    }
}
