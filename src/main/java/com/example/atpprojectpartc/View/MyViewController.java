package com.example.atpprojectpartc.View;

import com.example.atpprojectpartc.ViewModel.MyViewModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
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
     * The intro music continues while the user chooses maze dimensions.
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
     * Handles click on Show Solution.
     */
    @FXML
    public void onShowSolutionClicked() {
        if (viewModel == null || viewModel.getMaze() == null) {
            showErrorAlert("No maze", "Please generate or load a maze first.");
            return;
        }

        viewModel.solveMaze();
        requestMazeFocus();
    }

    /**
     * Handles click on New Maze.
     * Returns to the setup screen and starts the intro music again.
     */
    @FXML
    public void onNewMazeClicked() {
        showSetupScreen();
        soundManager.playIntroMusic();
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
        if (gamePane == null || !gamePane.isVisible()) {
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

        if (gamePane == null || !gamePane.isVisible()) {
            return;
        }

        if (mazeDisplayer.getWidth() <= 0 || mazeDisplayer.getHeight() <= 0) {
            return;
        }

        int[][] mazeMap = viewModel.getMaze().getMaze();

        int rows = mazeMap.length;
        int columns = mazeMap[0].length;

        double cellWidth = mazeDisplayer.getWidth() / columns;
        double cellHeight = mazeDisplayer.getHeight() / rows;

        if (cellWidth <= 0 || cellHeight <= 0) {
            return;
        }

        int mouseColumn = (int) (mouseEvent.getX() / cellWidth);
        int mouseRow = (int) (mouseEvent.getY() / cellHeight);

        if (mouseRow < 0 || mouseRow >= rows || mouseColumn < 0 || mouseColumn >= columns) {
            return;
        }

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
     * Updates the view after a new maze was generated or loaded.
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
        setStatusText("Solution path is displayed.");
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

            if (viewModel.getSolution() != null) {
                mazeDisplayer.setSolution(viewModel.getSolution());
            }
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

    /**
     * Displays a general information message.
     *
     * @param message message to display
     */
    @Override
    public void displayMessage(String message) {
        showInformationAlert("Maze Game", message);
    }

    /**
     * Shows a Pac-Man styled error alert.
     *
     * @param title alert title
     * @param content alert content
     */
    private void showErrorAlert(String title, String content) {
        Alert alert = createPacmanAlert(Alert.AlertType.ERROR, title, content);

        alert.getButtonTypes().setAll(ButtonType.OK);

        styleAlertButtons(alert, false);

        alert.showAndWait();
    }

    /**
     * Shows a Pac-Man styled information alert.
     *
     * @param title alert title
     * @param content alert content
     */
    private void showInformationAlert(String title, String content) {
        Alert alert = createPacmanAlert(Alert.AlertType.INFORMATION, title, content);

        alert.getButtonTypes().setAll(ButtonType.OK);

        styleAlertButtons(alert, false);

        alert.showAndWait();
    }

    /**
     * Shows a Pac-Man styled confirmation alert.
     *
     * @param title alert title
     * @param content alert content
     * @return true if the user clicked OK
     */
    private boolean showConfirmationAlert(String title, String content) {
        Alert alert = createPacmanAlert(Alert.AlertType.CONFIRMATION, title, content);

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(cancelButtonType, okButtonType);

        styleAlertButtons(alert, true);

        return alert.showAndWait()
                .filter(buttonType -> buttonType == okButtonType)
                .isPresent();
    }

    /**
     * Creates a black and yellow Pac-Man styled Alert.
     *
     * @param alertType alert type
     * @param title alert title
     * @param content alert content
     * @return styled alert
     */
    private Alert createPacmanAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(null);
        alert.setGraphic(null);

        DialogPane dialogPane = alert.getDialogPane();

        dialogPane.setStyle(
                "-fx-background-color: #000000;" +
                        "-fx-border-color: #FFD700;" +
                        "-fx-border-width: 4;" +
                        "-fx-border-radius: 18;" +
                        "-fx-background-radius: 18;"
        );

        Label titleLabel = new Label(formatAlertTitle(title, alertType));
        titleLabel.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 38));
        titleLabel.setTextFill(Color.web("#FFD700"));
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setWrapText(true);
        titleLabel.setEffect(new DropShadow(16, Color.web("#FFD700")));

        Label messageLabel = new Label(content);
        messageLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        messageLabel.setTextFill(Color.web("#FFD700"));
        messageLabel.setAlignment(Pos.CENTER);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(520);

        VBox contentBox = new VBox(22);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new Insets(35, 45, 25, 45));
        contentBox.setStyle(
                "-fx-background-color: #000000;" +
                        "-fx-background-radius: 18;"
        );

        contentBox.getChildren().addAll(titleLabel, messageLabel);

        dialogPane.setContent(contentBox);

        dialogPane.setMinWidth(560);
        dialogPane.setMinHeight(260);

        return alert;
    }

    /**
     * Formats the title according to the alert type.
     *
     * @param title original title
     * @param alertType alert type
     * @return formatted title
     */
    private String formatAlertTitle(String title, Alert.AlertType alertType) {
        if (title == null || title.isBlank()) {
            return switch (alertType) {
                case ERROR -> "ERROR!";
                case CONFIRMATION -> "ARE YOU SURE?";
                case INFORMATION -> "MESSAGE";
                default -> "PAC-MAZE";
            };
        }

        return title.toUpperCase();
    }

    /**
     * Styles the buttons of a Pac-Man styled alert.
     *
     * @param alert alert whose buttons should be styled
     * @param hasCancelButton true if the alert has OK and Cancel buttons
     */
    private void styleAlertButtons(Alert alert, boolean hasCancelButton) {
        DialogPane dialogPane = alert.getDialogPane();

        dialogPane.applyCss();

        Node buttonBar = dialogPane.lookup(".button-bar");
        if (buttonBar != null) {
            buttonBar.setStyle("-fx-background-color: #000000;");
        }

        for (ButtonType buttonType : dialogPane.getButtonTypes()) {
            Node buttonNode = dialogPane.lookupButton(buttonType);

            if (buttonNode instanceof Button button) {
                boolean isCancelButton =
                        buttonType.getButtonData() == ButtonBar.ButtonData.CANCEL_CLOSE;

                if (hasCancelButton && isCancelButton) {
                    styleCancelButton(button);
                } else {
                    styleOkButton(button);
                }
            }
        }
    }

    /**
     * Styles an OK button in Pac-Man yellow.
     *
     * @param button button to style
     */
    private void styleOkButton(Button button) {
        button.setTextFill(Color.BLACK);
        button.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 16));
        button.setStyle(getOkButtonStyle());

        button.setOnMouseEntered(event -> button.setStyle(getOkButtonHoverStyle()));
        button.setOnMouseExited(event -> button.setStyle(getOkButtonStyle()));
    }

    /**
     * Styles a Cancel button in black and yellow.
     *
     * @param button button to style
     */
    private void styleCancelButton(Button button) {
        button.setTextFill(Color.web("#FFD700"));
        button.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 16));
        button.setStyle(getCancelButtonStyle());

        button.setOnMouseEntered(event -> button.setStyle(getCancelButtonHoverStyle()));
        button.setOnMouseExited(event -> button.setStyle(getCancelButtonStyle()));
    }

    /**
     * @return regular OK button style
     */
    private String getOkButtonStyle() {
        return "-fx-background-color: #FFD700;" +
                "-fx-background-radius: 20;" +
                "-fx-border-color: #FFFFFF;" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 20;" +
                "-fx-padding: 8 30 8 30;" +
                "-fx-cursor: hand;";
    }

    /**
     * @return hover OK button style
     */
    private String getOkButtonHoverStyle() {
        return "-fx-background-color: #FFF176;" +
                "-fx-background-radius: 20;" +
                "-fx-border-color: #FFFFFF;" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 20;" +
                "-fx-padding: 8 30 8 30;" +
                "-fx-cursor: hand;";
    }

    /**
     * @return regular Cancel button style
     */
    private String getCancelButtonStyle() {
        return "-fx-background-color: #111111;" +
                "-fx-background-radius: 20;" +
                "-fx-border-color: #FFD700;" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 20;" +
                "-fx-padding: 8 24 8 24;" +
                "-fx-cursor: hand;";
    }

    /**
     * @return hover Cancel button style
     */
    private String getCancelButtonHoverStyle() {
        return "-fx-background-color: #222222;" +
                "-fx-background-radius: 20;" +
                "-fx-border-color: #FFF176;" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 20;" +
                "-fx-padding: 8 24 8 24;" +
                "-fx-cursor: hand;";
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

        dialogPane.applyCss();

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setText("OK");
        styleOkButton(okButton);

        dialog.showAndWait();
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
}