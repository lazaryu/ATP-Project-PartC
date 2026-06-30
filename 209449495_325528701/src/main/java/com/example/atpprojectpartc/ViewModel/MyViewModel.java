package com.example.atpprojectpartc.ViewModel;
import algorithms.mazeGenerators.Maze;
import algorithms.search.Solution;
import com.example.atpprojectpartc.Model.IModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

/**
 * MyViewModel connects the View layer with the Model layer.
 * It observes the Model and notifies the View.
 */
@SuppressWarnings("deprecation")
public class MyViewModel extends Observable implements Observer {

    private static final Logger logger = LogManager.getLogger(MyViewModel.class);

    private final IModel model;

    /**
     * Constructor.
     *
     * @param model the model layer
     */
    public MyViewModel(IModel model) {
        logger.info("Creating MyViewModel.");

        this.model = model;
        this.model.assignObserver(this);

        logger.info("MyViewModel was created and assigned as observer to the model.");
    }

    /**
     * Generates a new maze.
     *
     * @param rows number of rows
     * @param columns number of columns
     */
    public void generateMaze(int rows, int columns) {
        logger.info("Forwarding generateMaze request to model. rows={}, columns={}", rows, columns);
        model.generateMaze(rows, columns);
    }

    /**
     * Requests solving the current maze.
     */
    public void solveMaze() {
        logger.info("Forwarding solveMaze request to model.");
        model.solveMaze();
    }

    /**
     * Returns the current maze.
     *
     * @return current maze
     */
    public Maze getMaze() {
        return model.getMaze();
    }

    /**
     * Returns the current solution.
     *
     * @return current solution
     */
    public Solution getSolution() {
        return model.getSolution();
    }

    /**
     * Returns the player row.
     *
     * @return player row
     */
    public int getPlayerRow() {
        return model.getPlayerRow();
    }

    /**
     * Returns the player column.
     *
     * @return player column
     */
    public int getPlayerColumn() {
        return model.getPlayerColumn();
    }

    /**
     * Moves the player up.
     */
    public void moveUp() {
        logger.debug("Forwarding moveUp request to model.");
        model.movePlayer(-1, 0);
    }

    /**
     * Moves the player down.
     */
    public void moveDown() {
        logger.debug("Forwarding moveDown request to model.");
        model.movePlayer(1, 0);
    }

    /**
     * Moves the player left.
     */
    public void moveLeft() {
        logger.debug("Forwarding moveLeft request to model.");
        model.movePlayer(0, -1);
    }

    /**
     * Moves the player right.
     */
    public void moveRight() {
        logger.debug("Forwarding moveRight request to model.");
        model.movePlayer(0, 1);
    }

    /**
     * Moves the player up-left.
     */
    public void moveUpLeft() {
        logger.debug("Forwarding moveUpLeft request to model.");
        model.movePlayer(-1, -1);
    }

    /**
     * Moves the player up-right.
     */
    public void moveUpRight() {
        logger.debug("Forwarding moveUpRight request to model.");
        model.movePlayer(-1, 1);
    }

    /**
     * Moves the player down-left.
     */
    public void moveDownLeft() {
        logger.debug("Forwarding moveDownLeft request to model.");
        model.movePlayer(1, -1);
    }

    /**
     * Moves the player down-right.
     */
    public void moveDownRight() {
        logger.debug("Forwarding moveDownRight request to model.");
        model.movePlayer(1, 1);
    }

    /**
     * Moves the player by a general row and column change.
     *
     * @param rowChange row movement
     * @param columnChange column movement
     */
    public void moveByDelta(int rowChange, int columnChange) {
        logger.debug(
                "Forwarding moveByDelta request to model. rowChange={}, columnChange={}",
                rowChange,
                columnChange
        );

        model.movePlayer(rowChange, columnChange);
    }

    /**
     * Stops program resources.
     */
    public void stopProgram() {
        logger.info("Forwarding stopProgram request to model.");
        model.stopProgram();
    }

    /**
     * Receives updates from the Model and notifies the View.
     *
     * @param observable observable object
     * @param arg update message
     */
    @Override
    public void update(Observable observable, Object arg) {
        if (observable == model) {
            logger.debug("Received update from model and notifying view. update={}", arg);

            setChanged();
            notifyObservers(arg);
        } else {
            logger.warn("Received update from an unknown observable: {}", observable);
        }
    }

    /**
     * Saves the current maze.
     *
     * @param file file to save to
     * @throws IOException if saving fails
     */
    public void saveMaze(File file) throws IOException {
        logger.info(
                "Forwarding saveMaze request to model. file={}",
                file != null ? file.getAbsolutePath() : "null"
        );

        model.saveMaze(file);
    }

    /**
     * Loads a maze from a file.
     *
     * @param file file to load from
     * @throws IOException if loading fails
     */
    public void loadMaze(File file) throws IOException {
        logger.info(
                "Forwarding loadMaze request to model. file={}",
                file != null ? file.getAbsolutePath() : "null"
        );

        model.loadMaze(file);
    }
}
