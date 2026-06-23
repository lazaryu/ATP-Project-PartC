package com.example.atpprojectpartc.View;

import com.example.atpprojectpartc.Model.MyModel;
import com.example.atpprojectpartc.ViewModel.MyViewModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.Observable;
import java.util.Observer;
import java.util.Optional;

/**
 * MyViewController is the View layer controller.
 * It handles GUI events and observes the ViewModel.
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
     * Initializes the controller after the FXML is loaded.
     */
    @FXML
    public void initialize() {
        MyModel model = new MyModel();
        viewModel = new MyViewModel(model);
        viewModel.addObserver(this);

        mazeDisplayer = new MazeDisplayer();
        mazeDisplayer.setWidth(620);
        mazeDisplayer.setHeight(520);

        mazePane.getChildren().add(mazeDisplayer);

        showStartScreen();
    }

    /**
     * Handles click on Start Game.
     */
    @FXML
    protected void onStartGameClicked() {
        showSetupScreen();
    }

    /**
     * Handles click on Back.
     */
    @FXML
    protected void onBackToStartClicked() {
        showStartScreen();
    }

    /**
     * Handles click on Generate Maze.
     */
    @FXML
    protected void onGenerateMazeClicked() {
        try {
            int rows = Integer.parseInt(rowsTextField.getText().trim());
            int columns = Integer.parseInt(columnsTextField.getText().trim());

            viewModel.generateMaze(rows, columns);

        } catch (NumberFormatException e) {
            showErrorAlert("Invalid input", "Rows and columns must be numbers.");
        }
    }

    /**
     * Handles click on Show Solution.
     */
    @FXML
    protected void onShowSolutionClicked() {
        showInformationAlert(
                "Solution",
                "In the next step we will connect this button to the solver from Part B."
        );
    }

    /**
     * Handles click on New Maze.
     */
    @FXML
    protected void onNewMazeClicked() {
        showSetupScreen();
    }

    /**
     * Handles click on Exit Game.
     */
    @FXML
    protected void onExitGameClicked() {
        boolean shouldExit = showConfirmationAlert(
                "Exit Game",
                "Are you sure you want to leave the current maze?"
        );

        if (shouldExit) {
            showStartScreen();
        }
    }

    /**
     * Handles click on Exit.
     */
    @FXML
    protected void onExitClicked() {
        cleanExit();
    }

    /**
     * Handles keyboard movement.
     *
     * @param code key code
     */
    private void handleKeyPressed(KeyCode code) {
        if (!gamePane.isVisible()) {
            return;
        }

        if (code == KeyCode.UP) {
            viewModel.moveUp();
        } else if (code == KeyCode.DOWN) {
            viewModel.moveDown();
        } else if (code == KeyCode.LEFT) {
            viewModel.moveLeft();
        } else if (code == KeyCode.RIGHT) {
            viewModel.moveRight();
        }
    }

    /**
     * Gets notified when the ViewModel changes.
     *
     * @param observable observable object
     * @param arg update message
     */
    @Override
    public void update(Observable observable, Object arg) {
        if (observable != viewModel) {
            return;
        }

        String message = String.valueOf(arg);

        switch (message) {
            case "mazeGenerated" -> {
                mazeDisplayer.setMaze(
                        viewModel.getMaze(),
                        viewModel.getPlayerRow(),
                        viewModel.getPlayerColumn()
                );

                statusLabel.setText("Maze generated. Use the arrow keys to move.");
                showGameScreen();
                registerKeyboardListener();
            }

            case "playerMoved" -> {
                mazeDisplayer.updatePlayerPosition(
                        viewModel.getPlayerRow(),
                        viewModel.getPlayerColumn()
                );

                statusLabel.setText("Keep going!");
            }

            case "gameWon" -> {
                mazeDisplayer.updatePlayerPosition(
                        viewModel.getPlayerRow(),
                        viewModel.getPlayerColumn()
                );

                statusLabel.setText("You solved the maze!");
                showInformationAlert("Victory", "You solved the maze!");
            }

            case "invalidMazeSize" -> showErrorAlert(
                    "Invalid maze size",
                    "Rows and columns must be at least 2."
            );

            case "mazeSizeTooLarge" -> showErrorAlert(
                    "Invalid maze size",
                    "Rows and columns must be 100 or less."
            );

            default -> displayMessage(message);
        }
    }

    /**
     * Registers keyboard listener on the current scene.
     */
    private void registerKeyboardListener() {
        Platform.runLater(() -> {
            if (mazePane.getScene() != null) {
                mazePane.getScene().setOnKeyPressed(event -> handleKeyPressed(event.getCode()));
                mazePane.requestFocus();
            }
        });
    }

    /**
     * Displays a message.
     *
     * @param message message to display
     */
    @Override
    public void displayMessage(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
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
                "Are you sure you want to exit Maze Quest?"
        );

        if (shouldExit) {
            viewModel.stopProgram();
            Platform.exit();
            System.exit(0);
        }
    }
}