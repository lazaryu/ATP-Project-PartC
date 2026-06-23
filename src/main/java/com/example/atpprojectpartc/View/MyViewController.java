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

import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;

import java.util.Observable;
import java.util.Observer;
import java.util.Optional;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.File;
import java.io.IOException;

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

    private final SoundManager soundManager = new SoundManager();

    /**
     * Initializes the custom MazeDisplayer and places it inside the maze pane.
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
        soundManager.playIntroMusic();
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
     * This only opens the setup screen.
     * The game music starts only after a maze is generated.
     */
    @FXML
    public void onStartGameClicked() {
        showSetupScreen();
    }

    /**
     * Handles click on Back from the setup screen.
     * The intro music continues playing.
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
        if (viewModel == null || viewModel.getMaze() == null) {
            showErrorAlert("No maze", "Please generate a maze first.");
            return;
        }

        viewModel.solveMaze();
        requestMazeFocus();
    }

    /**
     * Handles click on New Maze.
     * Stops the current game music and returns to the setup screen.
     */
    @FXML
    public void onNewMazeClicked() {
        soundManager.playIntroMusic();
        showSetupScreen();
    }

    /**
     * Handles click on Exit Game from the maze screen.
     * Plays the loss sound and then returns to the intro music.
     */
    @FXML
    public void onExitGameClicked() {
        boolean shouldExit = showConfirmationAlert(
                "Exit Game",
                "Are you sure you want to leave the current maze?"
        );

        if (shouldExit) {
            showStartScreen();
            soundManager.playLossThenIntroMusic();
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
     * Checks if the target cell is adjacent to the player.
     *
     * @param rowDifference row difference
     * @param columnDifference column difference
     * @return true if adjacent
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
     * @param observable observable object
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
                case "solutionReady" -> handleSolutionReady();
                default -> System.out.println("Unknown update: " + updateMessage);
            }
        });
    }

    /**
     * Updates the view after a new maze was generated.
     * This is the point where the actual maze screen opens,
     * so this is where the Pacman music starts.
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

        soundManager.playGameMusic();

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
     * Displays the solution path.
     */
    private void handleSolutionReady() {
        if (mazeDisplayer == null || viewModel == null || viewModel.getSolution() == null) {
            return;
        }

        mazeDisplayer.setSolution(viewModel.getSolution());
        setStatusText("Solution path is displayed with Pac-Man dots.");
        requestMazeFocus();
    }

    /**
     * Handles winning the game.
     * Stops the Pacman music and plays the victory music.
     */
    private void handleGameWon() {
        if (mazeDisplayer == null) {
            return;
        }

        mazeDisplayer.updatePlayerPosition(
                viewModel.getPlayerRow(),
                viewModel.getPlayerColumn()
        );

        soundManager.playVictoryMusic();

        setStatusText("You solved the maze!");
        showPacmanVictoryDialog();
    }

    /**
     * Shows a custom Pac-Man styled victory dialog.
     */
    private void showPacmanVictoryDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Victory");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().add(ButtonType.OK);

        dialogPane.setStyle(
                "-fx-background-color: #000000;" +
                        "-fx-border-color: #FFD700;" +
                        "-fx-border-width: 4;" +
                        "-fx-border-radius: 18;" +
                        "-fx-background-radius: 18;"
        );

        Label titleLabel = new Label("YOU WIN!");
        titleLabel.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 52));
        titleLabel.setTextFill(Color.web("#FFD700"));
        titleLabel.setEffect(new DropShadow(18, Color.web("#FFD700")));

        Label messageLabel = new Label("Great job! You solved the maze.");
        messageLabel.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 28));
        messageLabel.setTextFill(Color.web("#FFD700"));
        messageLabel.setWrapText(true);
        messageLabel.setAlignment(Pos.CENTER);

        VBox contentBox = new VBox(35);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new Insets(45, 60, 35, 60));
        contentBox.setStyle(
                "-fx-background-color: #000000;" +
                        "-fx-background-radius: 18;"
        );

        contentBox.getChildren().addAll(
                titleLabel,
                messageLabel
        );

        dialogPane.setContent(contentBox);

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setText("OK");
        okButton.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 18));
        okButton.setTextFill(Color.BLACK);
        okButton.setStyle(
                "-fx-background-color: #FFD700;" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: #FFFFFF;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 20;" +
                        "-fx-padding: 8 30 8 30;" +
                        "-fx-cursor: hand;"
        );

        okButton.setOnMouseEntered(event ->
                okButton.setStyle(
                        "-fx-background-color: #FFF176;" +
                                "-fx-background-radius: 20;" +
                                "-fx-border-color: #FFFFFF;" +
                                "-fx-border-width: 2;" +
                                "-fx-border-radius: 20;" +
                                "-fx-padding: 8 30 8 30;" +
                                "-fx-cursor: hand;"
                )
        );

        okButton.setOnMouseExited(event ->
                okButton.setStyle(
                        "-fx-background-color: #FFD700;" +
                                "-fx-background-radius: 20;" +
                                "-fx-border-color: #FFFFFF;" +
                                "-fx-border-width: 2;" +
                                "-fx-border-radius: 20;" +
                                "-fx-padding: 8 30 8 30;" +
                                "-fx-cursor: hand;"
                )
        );

        dialog.showAndWait();
    }

    /**
     * Connects keyboard events to the scene.
     *
     * @param scene main scene
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
     * Requests focus for keyboard movement.
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
        if (node != null) {
            node.setVisible(visible);
            node.setManaged(visible);
        }
    }

    /**
     * Displays a message.
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
            soundManager.stopAllMusic();

            if (viewModel != null) {
                viewModel.stopProgram();
            }

            Platform.exit();
            System.exit(0);
        }
    }

    /**
     * Handles click on Save Maze.
     * Saves the current maze to a .maze file.
     */
    @FXML
    public void onSaveMazeClicked() {
        if (viewModel == null || viewModel.getMaze() == null) {
            showErrorAlert("No maze", "There is no maze to save.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Maze");
        fileChooser.setInitialFileName("savedMaze.maze");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Maze Files (*.maze)", "*.maze")
        );

        File selectedFile = fileChooser.showSaveDialog(getCurrentWindow());

        if (selectedFile == null) {
            requestMazeFocus();
            return;
        }

        File fileToSave = ensureMazeExtension(selectedFile);

        try {
            viewModel.saveMaze(fileToSave);
            showInformationAlert("Maze Saved", "The maze was saved successfully.");
        } catch (IOException e) {
            showErrorAlert("Save Error", "Could not save the maze file.");
            e.printStackTrace();
        } catch (Exception e) {
            showErrorAlert("Save Error", e.getMessage());
            e.printStackTrace();
        }

        requestMazeFocus();
    }

    /**
     * Handles click on Load Maze.
     * Loads a maze from a .maze file and opens the game screen.
     */
    @FXML
    public void onLoadMazeClicked() {
        if (viewModel == null) {
            displayMessage("ViewModel is not connected.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Maze");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Maze Files (*.maze)", "*.maze")
        );

        File selectedFile = fileChooser.showOpenDialog(getCurrentWindow());

        if (selectedFile == null) {
            return;
        }

        try {
            viewModel.loadMaze(selectedFile);
        } catch (IOException e) {
            showErrorAlert("Load Error", "Could not load the maze file.");
            e.printStackTrace();
        } catch (Exception e) {
            showErrorAlert("Load Error", "The selected file is not a valid maze file.");
            e.printStackTrace();
        }
    }

    /**
     * Adds .maze extension if the user did not type it.
     *
     * @param file selected file
     * @return file with .maze extension
     */
    private File ensureMazeExtension(File file) {
        String filePath = file.getAbsolutePath();

        if (!filePath.toLowerCase().endsWith(".maze")) {
            return new File(filePath + ".maze");
        }

        return file;
    }

    /**
     * Returns the current application window for FileChooser dialogs.
     *
     * @return current window
     */
    private Window getCurrentWindow() {
        if (mazePane != null && mazePane.getScene() != null) {
            return mazePane.getScene().getWindow();
        }

        if (setupPane != null && setupPane.getScene() != null) {
            return setupPane.getScene().getWindow();
        }

        if (startPane != null && startPane.getScene() != null) {
            return startPane.getScene().getWindow();
        }

        return null;
    }
}