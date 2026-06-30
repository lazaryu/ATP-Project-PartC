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
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;

/**
 * MyViewController is the View layer controller.
 * It receives user actions from the GUI, forwards them to the ViewModel,
 * and updates the maze display when the ViewModel notifies about changes.
 */
@SuppressWarnings("deprecation")
public class MyViewController implements IView, Observer {

    private static final Logger logger = LogManager.getLogger(MyViewController.class);

    private static final double MIN_MAZE_ZOOM_SCALE = 0.6;
    private static final double MAX_MAZE_ZOOM_SCALE = 2.5;
    private static final double MAZE_ZOOM_STEP = 0.1;

    @FXML
    private VBox startPane;

    @FXML
    private VBox setupPane;

    @FXML
    private BorderPane gamePane;

    @FXML
    private Spinner<Integer> rowsSpinner;

    @FXML
    private Spinner<Integer> columnsSpinner;

    @FXML
    private Pane mazePane;

    @FXML
    private Label statusLabel;

    private MazeDisplayer mazeDisplayer;
    private MyViewModel viewModel;

    private double mazeZoomScale = 1.0;

    private final SoundManager soundManager = new SoundManager();

    /**
     * Initializes the custom MazeDisplayer and places it inside the maze pane.
     */
    @FXML
    public void initialize() {
        logger.info("Initializing MyViewController.");

        mazeDisplayer = new MazeDisplayer();

        initializeDimensionSpinners();

        mazeDisplayer.widthProperty().bind(mazePane.widthProperty());
        mazeDisplayer.heightProperty().bind(mazePane.heightProperty());

        mazePane.getChildren().add(mazeDisplayer);
        mazePane.setFocusTraversable(true);
        mazeDisplayer.setFocusTraversable(true);

        mazeDisplayer.setOnMouseMoved(this::mouseMoved);
        mazeDisplayer.setOnMouseDragged(this::mouseMoved);
        mazePane.setOnMouseMoved(this::mouseMoved);
        mazePane.setOnMouseDragged(this::mouseMoved);

        mazeDisplayer.setOnScroll(this::handleMazeZoom);
        mazePane.setOnScroll(this::handleMazeZoom);

        mazePane.widthProperty().addListener((observable, oldValue, newValue) -> redrawMaze());
        mazePane.heightProperty().addListener((observable, oldValue, newValue) -> redrawMaze());

        showStartScreen();
        soundManager.playIntroMusic();

        logger.info("MyViewController initialized successfully.");
    }

    /**
     * Connects the controller to the ViewModel.
     *
     * @param viewModel the ViewModel layer
     */
    @Override
    public void setViewModel(MyViewModel viewModel) {
        logger.info("Connecting MyViewController to MyViewModel.");

        this.viewModel = viewModel;
        this.viewModel.addObserver(this);
    }

    /**
     * Handles click on Start Game.
     */
    @FXML
    public void onStartGameClicked() {
        logger.info("User clicked Start Game.");
        showSetupScreen();
    }

    /**
     * Handles click on Back from the setup screen.
     */
    @FXML
    public void onBackToStartClicked() {
        logger.info("User clicked Back to start screen.");
        showStartScreen();
    }

    /**
     * Handles click on Generate Maze.
     */
    @FXML
    public void onGenerateMazeClicked() {
        logger.info("User clicked Generate Maze.");

        if (viewModel == null) {
            logger.error("Cannot generate maze because ViewModel is not connected.");
            displayMessage("ViewModel is not connected.");
            return;
        }

        try {
            commitSpinnerValue(rowsSpinner);
            commitSpinnerValue(columnsSpinner);

            int rows = rowsSpinner.getValue();
            int columns = columnsSpinner.getValue();

            logger.info("User requested maze generation. rows={}, columns={}", rows, columns);

            if (rows < 3 || columns < 3) {
                logger.warn("Invalid maze size. rows={}, columns={}", rows, columns);
                showErrorAlert("Invalid maze size", "Rows and columns must be at least 3.");
                return;
            }

            if (rows > 100 || columns > 100) {
                logger.warn("Maze size too large. rows={}, columns={}", rows, columns);
                showErrorAlert("Invalid maze size", "Rows and columns must be 100 or less.");
                return;
            }

            viewModel.generateMaze(rows, columns);
            requestMazeFocus();

        } catch (Exception e) {
            logger.warn("Invalid maze input from spinners.", e);
            showErrorAlert("Invalid input", "Rows and columns must be valid numbers.");
        }
    }

    /**
     * Handles click on Load Maze.
     */
    @FXML
    public void onLoadMazeClicked() {
        logger.info("User clicked Load Maze.");

        if (viewModel == null) {
            logger.error("Cannot load maze because ViewModel is not connected.");
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
            logger.info("Load maze cancelled by user.");
            return;
        }

        logger.info("User selected maze file to load: {}", selectedFile.getAbsolutePath());

        try {
            viewModel.loadMaze(selectedFile);
            logger.info("Maze load request completed successfully.");

        } catch (IOException e) {
            logger.error("Could not load maze file: {}", selectedFile.getAbsolutePath(), e);
            showErrorAlert("Load Error", "Could not load the maze file.");

        } catch (Exception e) {
            logger.error("Selected file is not a valid maze file: {}", selectedFile.getAbsolutePath(), e);
            showErrorAlert("Load Error", "The selected file is not a valid maze file.");
        }
    }

    /**
     * Handles click on Save Maze.
     */
    @FXML
    public void onSaveMazeClicked() {
        logger.info("User clicked Save Maze.");

        if (viewModel == null || viewModel.getMaze() == null) {
            logger.warn("Save maze failed because there is no maze to save.");
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
            logger.info("Save maze cancelled by user.");
            requestMazeFocus();
            return;
        }

        File fileToSave = ensureMazeExtension(selectedFile);

        logger.info("User selected maze file to save: {}", fileToSave.getAbsolutePath());

        try {
            viewModel.saveMaze(fileToSave);
            logger.info("Maze saved successfully to: {}", fileToSave.getAbsolutePath());
            showInformationAlert("Maze Saved", "The maze was saved successfully.");

        } catch (IOException e) {
            logger.error("Could not save maze file: {}", fileToSave.getAbsolutePath(), e);
            showErrorAlert("Save Error", "Could not save the maze file.");

        } catch (Exception e) {
            logger.error("Unexpected error while saving maze to: {}", fileToSave.getAbsolutePath(), e);
            showErrorAlert("Save Error", e.getMessage());
        }

        requestMazeFocus();
    }

    /**
     * Handles click on Show Solution.
     */
    @FXML
    public void onShowSolutionClicked() {
        logger.info("User clicked Show Solution.");

        if (viewModel == null || viewModel.getMaze() == null) {
            logger.warn("Cannot show solution because no maze exists.");
            showErrorAlert("No maze", "Please generate or load a maze first.");
            return;
        }

        viewModel.solveMaze();
        requestMazeFocus();
    }

    /**
     * Handles click on New Maze.
     */
    @FXML
    public void onNewMazeClicked() {
        logger.info("User clicked New Maze.");
        showSetupScreen();
        soundManager.playIntroMusic();
    }

    /**
     * Handles click on Exit Game from the maze screen.
     */
    @FXML
    public void onExitGameClicked() {
        logger.info("User clicked Exit Game.");

        boolean shouldExit = showConfirmationAlert(
                "Exit Game",
                "Are you sure you want to leave the current maze?"
        );

        if (shouldExit) {
            logger.info("User confirmed exit from current game.");
            showStartScreen();
            soundManager.playLossThenIntroMusic();
        } else {
            logger.info("User cancelled exit from current game.");
            requestMazeFocus();
        }
    }

    /**
     * Handles click on Exit.
     */
    @FXML
    public void onExitClicked() {
        logger.info("User clicked Exit.");
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

        logger.debug("Movement key pressed: {}", code);
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
            logger.debug(
                    "Mouse movement requested player move. rowDifference={}, columnDifference={}",
                    rowDifference,
                    columnDifference
            );

            viewModel.moveByDelta(rowDifference, columnDifference);
        }

        requestMazeFocus();
    }

    /**
     * Handles Ctrl + mouse wheel zoom on the maze.
     *
     * @param scrollEvent mouse scroll event
     */
    private void handleMazeZoom(ScrollEvent scrollEvent) {
        if (mazeDisplayer == null) {
            return;
        }

        if (!scrollEvent.isControlDown()) {
            return;
        }

        double previousZoomScale = mazeZoomScale;

        if (scrollEvent.getDeltaY() > 0) {
            mazeZoomScale += MAZE_ZOOM_STEP;
        } else if (scrollEvent.getDeltaY() < 0) {
            mazeZoomScale -= MAZE_ZOOM_STEP;
        }

        mazeZoomScale = Math.max(
                MIN_MAZE_ZOOM_SCALE,
                Math.min(MAX_MAZE_ZOOM_SCALE, mazeZoomScale)
        );

        applyMazeZoom();

        logger.info(
                "Maze zoom changed. previousZoom={}, newZoom={}",
                previousZoomScale,
                mazeZoomScale
        );

        scrollEvent.consume();
        requestMazeFocus();
    }

    /**
     * Applies the current zoom scale to the maze canvas.
     */
    private void applyMazeZoom() {
        if (mazeDisplayer == null) {
            return;
        }

        mazeDisplayer.setScaleX(mazeZoomScale);
        mazeDisplayer.setScaleY(mazeZoomScale);
    }

    /**
     * Resets the maze zoom to the default size.
     */
    private void resetMazeZoom() {
        mazeZoomScale = 1.0;
        applyMazeZoom();

        logger.debug("Maze zoom was reset to default.");
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
                logger.warn("Received null update from ViewModel.");
                return;
            }

            String updateMessage = arg.toString();

            logger.debug("Received update from ViewModel: {}", updateMessage);

            switch (updateMessage) {
                case "mazeGenerated" -> handleMazeGenerated();
                case "playerMoved" -> handlePlayerMoved();
                case "gameWon" -> handleGameWon();
                case "solutionReady" -> handleSolutionReady();
                case "mazeGenerationFailed" -> showErrorAlert("Maze Error", "Could not generate the maze.");
                case "solutionFailed" -> showErrorAlert("Solution Error", "Could not solve the maze.");
                default -> logger.warn("Unknown update received from ViewModel: {}", updateMessage);
            }
        });
    }

    /**
     * Updates the view after a new maze was generated or loaded.
     */
    private void handleMazeGenerated() {
        if (mazeDisplayer == null) {
            logger.error("Cannot display generated maze because MazeDisplayer is null.");
            return;
        }

        logger.info("Displaying generated or loaded maze.");

        mazeDisplayer.setMaze(
                viewModel.getMaze(),
                viewModel.getPlayerRow(),
                viewModel.getPlayerColumn()
        );

        resetMazeZoom();

        soundManager.playGameMusic();

        showGameScreen();
        requestMazeFocus();
    }

    /**
     * Updates the player location on the maze display.
     */
    private void handlePlayerMoved() {
        if (mazeDisplayer == null) {
            logger.error("Cannot update player position because MazeDisplayer is null.");
            return;
        }

        mazeDisplayer.updatePlayerPosition(
                viewModel.getPlayerRow(),
                viewModel.getPlayerColumn()
        );
    }

    /**
     * Displays the solution path.
     */
    private void handleSolutionReady() {
        if (mazeDisplayer == null || viewModel == null || viewModel.getSolution() == null) {
            logger.warn("Cannot display solution because one of the required objects is null.");
            return;
        }

        logger.info("Displaying solution path on maze.");

        mazeDisplayer.setSolution(viewModel.getSolution());
        setStatusText("Solution path is displayed.");
        requestMazeFocus();
    }

    /**
     * Handles winning the game.
     */
    private void handleGameWon() {
        if (mazeDisplayer == null) {
            logger.error("Cannot handle game won because MazeDisplayer is null.");
            return;
        }

        logger.info("Game won by user.");

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
        logger.info("Keyboard events connected to scene.");
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

            applyMazeZoom();
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
        logger.debug("Showing start screen.");
        setPaneVisible(startPane, true);
        setPaneVisible(setupPane, false);
        setPaneVisible(gamePane, false);
    }

    /**
     * Shows the setup screen.
     */
    private void showSetupScreen() {
        logger.debug("Showing setup screen.");
        setPaneVisible(startPane, false);
        setPaneVisible(setupPane, true);
        setPaneVisible(gamePane, false);
    }

    /**
     * Shows the game screen.
     */
    private void showGameScreen() {
        logger.debug("Showing game screen.");
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
            File fileWithExtension = new File(filePath + ".maze");
            logger.debug("Added .maze extension. original={}, updated={}", filePath, fileWithExtension.getAbsolutePath());
            return fileWithExtension;
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

        logger.warn("Could not find current application window.");
        return null;
    }

    /**
     * Displays a general information message.
     *
     * @param message message to display
     */
    @Override
    public void displayMessage(String message) {
        logger.info("Displaying message to user: {}", message);
        showInformationAlert("Maze Game", message);
    }

    /**
     * Shows a Pac-Man styled error alert.
     *
     * @param title alert title
     * @param content alert content
     */
    private void showErrorAlert(String title, String content) {
        logger.warn("Showing error alert. title={}, content={}", title, content);

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
        logger.info("Showing information alert. title={}, content={}", title, content);

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
        logger.info("Showing confirmation alert. title={}, content={}", title, content);

        Alert alert = createPacmanAlert(Alert.AlertType.CONFIRMATION, title, content);

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(cancelButtonType, okButtonType);

        styleAlertButtons(alert, true);

        boolean confirmed = alert.showAndWait()
                .filter(buttonType -> buttonType == okButtonType)
                .isPresent();

        logger.info("Confirmation alert result. title={}, confirmed={}", title, confirmed);

        return confirmed;
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
        logger.info("Showing Pac-Man victory dialog.");

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
     * Handles Options -> Properties.
     */
    @FXML
    public void onPropertiesClicked() {
        logger.info("User clicked Properties.");
        showPropertiesAlert(getPropertiesText());
    }

    /**
     * Handles Help -> Help.
     */
    @FXML
    public void onHelpClicked() {
        logger.info("User clicked Help.");

        showInformationAlert(
                "Help",
                "Goal: move Pac-Man from the start point to the reward.\n\n" +
                        "Movement keys:\n" +
                        "8 / Up - move up\n" +
                        "2 / Down - move down\n" +
                        "4 / Left - move left\n" +
                        "6 / Right - move right\n\n" +
                        "Diagonal movement:\n" +
                        "7 - up left\n" +
                        "9 - up right\n" +
                        "1 - down left\n" +
                        "3 - down right\n\n" +
                        "You can also move with the mouse over adjacent cells.\n\n" +
                        "Zoom:\n" +
                        "Hold Ctrl and use the mouse wheel over the maze.\n\n" +
                        "Green circle = start position.\n" +
                        "Reward image = goal position.\n" +
                        "Show Solution displays the solution path."
        );
    }

    /**
     * Handles About -> About.
     */
    @FXML
    public void onAboutClicked() {
        logger.info("User clicked About.");

        showInformationAlert(
                "About",
                "PAC-MAZE\n\n" +
                        "JavaFX maze game using MVVM architecture.\n\n" +
                        "Maze generation algorithm: MyMazeGenerator through Part B server\n" +
                        "Maze solving algorithm: BestFirstSearch through Part B server\n\n" +
                        "Created as part of the ATP Maze Project.\n\n" +
                        "Developers: Shery and Yuval"
        );
    }

    /**
     * Handles closing the window with the X button.
     */
    public void handleWindowClose(WindowEvent event) {
        logger.info("User clicked window close button.");
        event.consume();
        cleanExit();
    }

    /**
     * Reads config.properties and formats it for display.
     *
     * @return formatted properties text
     */
    private String getPropertiesText() {
        Properties properties = new Properties();

        logger.info("Reading config.properties.");

        try (InputStream inputStream = openPropertiesInputStream()) {
            if (inputStream == null) {
                logger.warn("config.properties file was not found.");

                return "config.properties file was not found.\n\n" +
                        "Expected location:\n" +
                        "src/main/resources/config.properties";
            }

            properties.load(inputStream);

            if (properties.isEmpty()) {
                logger.warn("config.properties file is empty.");
                return "config.properties file is empty.";
            }

            StringBuilder builder = new StringBuilder();

            for (String propertyName : properties.stringPropertyNames()) {
                builder.append(propertyName)
                        .append(" = ")
                        .append(properties.getProperty(propertyName))
                        .append("\n");
            }

            logger.info("config.properties loaded successfully. numberOfProperties={}", properties.size());

            return builder.toString();

        } catch (Exception e) {
            logger.error("Could not read config.properties.", e);
            return "Could not read config.properties.";
        }
    }

    /**
     * Opens config.properties from resources.
     *
     * @return input stream of the properties file, or null if not found
     * @throws IOException if file access fails
     */
    private InputStream openPropertiesInputStream() throws IOException {
        InputStream inputStream = getClass().getResourceAsStream("/config.properties");

        if (inputStream != null) {
            logger.debug("config.properties loaded from resources.");
            return inputStream;
        }

        File configFile = new File("config.properties");

        if (configFile.exists()) {
            logger.debug("config.properties loaded from project root.");
            return Files.newInputStream(configFile.toPath());
        }

        return null;
    }

    /**
     * Exits the application cleanly.
     */
    private void cleanExit() {
        logger.info("Starting clean exit flow.");

        boolean shouldExit = showConfirmationAlert(
                "Exit",
                "Are you sure you want to exit PAC-MAZE?"
        );

        if (shouldExit) {
            logger.info("User confirmed application exit.");

            soundManager.stopAllMusic();

            if (viewModel != null) {
                viewModel.stopProgram();
            }

            Platform.exit();
            System.exit(0);
        } else {
            logger.info("User cancelled application exit.");
        }
    }

    /**
     * Shows the properties file in a Pac-Man styled alert with smaller text.
     *
     * @param propertiesText formatted properties text
     */
    private void showPropertiesAlert(String propertiesText) {
        logger.info("Showing Properties dialog.");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Properties");
        alert.setHeaderText(null);
        alert.setContentText(null);
        alert.setGraphic(null);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getButtonTypes().setAll(ButtonType.OK);

        dialogPane.setStyle(
                "-fx-background-color: #000000;" +
                        "-fx-border-color: #FFD700;" +
                        "-fx-border-width: 4;" +
                        "-fx-border-radius: 18;" +
                        "-fx-background-radius: 18;"
        );

        Label titleLabel = new Label("PROPERTIES");
        titleLabel.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 30));
        titleLabel.setTextFill(Color.web("#FFD700"));
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setEffect(new DropShadow(14, Color.web("#FFD700")));

        TextArea propertiesTextArea = new TextArea(propertiesText);
        propertiesTextArea.setEditable(false);
        propertiesTextArea.setWrapText(true);
        propertiesTextArea.setPrefWidth(520);
        propertiesTextArea.setPrefHeight(170);
        propertiesTextArea.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        propertiesTextArea.setStyle(
                "-fx-control-inner-background: #000000;" +
                        "-fx-background-color: #000000;" +
                        "-fx-text-fill: #FFD700;" +
                        "-fx-highlight-fill: #FFD700;" +
                        "-fx-highlight-text-fill: #000000;" +
                        "-fx-border-color: #FFD700;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;"
        );

        VBox contentBox = new VBox(18);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new Insets(25, 35, 20, 35));
        contentBox.setStyle(
                "-fx-background-color: #000000;" +
                        "-fx-background-radius: 18;"
        );

        contentBox.getChildren().addAll(titleLabel, propertiesTextArea);

        dialogPane.setContent(contentBox);
        dialogPane.setMinWidth(600);
        dialogPane.setMinHeight(300);

        styleAlertButtons(alert, false);

        alert.showAndWait();
    }

    /**
     * Initializes rows and columns spinners.
     */
    private void initializeDimensionSpinners() {
        if (rowsSpinner == null || columnsSpinner == null) {
            logger.warn("Cannot initialize dimension spinners because one of them is null.");
            return;
        }

        SpinnerValueFactory<Integer> rowsFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(3, 100, 20);

        SpinnerValueFactory<Integer> columnsFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(3, 100, 20);

        rowsSpinner.setValueFactory(rowsFactory);
        columnsSpinner.setValueFactory(columnsFactory);

        rowsSpinner.setEditable(true);
        columnsSpinner.setEditable(true);

        rowsSpinner.getEditor().setStyle(getSpinnerEditorStyle());
        columnsSpinner.getEditor().setStyle(getSpinnerEditorStyle());

        rowsSpinner.getEditor().setOnAction(event -> {
            commitSpinnerValue(rowsSpinner);
            columnsSpinner.requestFocus();
            columnsSpinner.getEditor().selectAll();
        });

        columnsSpinner.getEditor().setOnAction(event -> {
            commitSpinnerValue(columnsSpinner);
            onGenerateMazeClicked();
        });

        logger.info("Dimension spinners initialized successfully.");
    }

    /**
     * Commits manually typed text inside a Spinner editor.
     *
     * @param spinner spinner to commit
     */
    private void commitSpinnerValue(Spinner<Integer> spinner) {
        try {
            String text = spinner.getEditor().getText().trim();
            int value = Integer.parseInt(text);

            int minValue = 3;
            int maxValue = 100;

            if (value < minValue) {
                logger.debug("Spinner value was below minimum. value={}, min={}", value, minValue);
                value = minValue;
            }

            if (value > maxValue) {
                logger.debug("Spinner value was above maximum. value={}, max={}", value, maxValue);
                value = maxValue;
            }

            spinner.getValueFactory().setValue(value);
            spinner.getEditor().setText(String.valueOf(value));

        } catch (NumberFormatException e) {
            logger.warn("Invalid spinner input '{}'. Restoring previous value.", spinner.getEditor().getText());
            spinner.getEditor().setText(String.valueOf(spinner.getValue()));
        }
    }

    /**
     * Returns the visual style for the spinner text editor.
     *
     * @return CSS string
     */
    private String getSpinnerEditorStyle() {
        return "-fx-background-color: white;" +
                "-fx-text-fill: #222222;" +
                "-fx-font-size: 22px;" +
                "-fx-font-weight: bold;" +
                "-fx-border-color: #FFD700;" +
                "-fx-border-width: 3;" +
                "-fx-border-radius: 12;" +
                "-fx-background-radius: 12;";
    }
}
