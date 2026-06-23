package com.example.atpprojectpartc.ViewModel;

import algorithms.mazeGenerators.Maze;
import com.example.atpprojectpartc.Model.IModel;

import java.util.Observable;
import java.util.Observer;

/**
 * MyViewModel connects the View and the Model.
 * It forwards user actions from the View to the Model,
 * and forwards updates from the Model back to the View.
 */
@SuppressWarnings("deprecation")
public class MyViewModel extends Observable implements Observer {

    private final IModel model;

    /**
     * Creates a ViewModel and connects it to the Model.
     *
     * @param model model layer
     */
    public MyViewModel(IModel model) {
        this.model = model;
        this.model.assignObserver(this);
    }

    /**
     * Called when the Model notifies that something has changed.
     *
     * @param observable observable object
     * @param arg update message
     */
    @Override
    public void update(Observable observable, Object arg) {
        setChanged();
        notifyObservers(arg);
    }

    /**
     * Requests maze generation from the Model.
     *
     * @param rows number of rows
     * @param columns number of columns
     */
    public void generateMaze(int rows, int columns) {
        model.generateMaze(rows, columns);
    }

    /**
     * Moves the player up.
     */
    public void moveUp() {
        model.movePlayer(-1, 0);
    }

    /**
     * Moves the player down.
     */
    public void moveDown() {
        model.movePlayer(1, 0);
    }

    /**
     * Moves the player left.
     */
    public void moveLeft() {
        model.movePlayer(0, -1);
    }

    /**
     * Moves the player right.
     */
    public void moveRight() {
        model.movePlayer(0, 1);
    }

    /**
     * Moves the player diagonally up-left.
     */
    public void moveUpLeft() {
        model.movePlayer(-1, -1);
    }

    /**
     * Moves the player diagonally up-right.
     */
    public void moveUpRight() {
        model.movePlayer(-1, 1);
    }

    /**
     * Moves the player diagonally down-left.
     */
    public void moveDownLeft() {
        model.movePlayer(1, -1);
    }

    /**
     * Moves the player diagonally down-right.
     */
    public void moveDownRight() {
        model.movePlayer(1, 1);
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
     * Returns the player's row.
     *
     * @return player row
     */
    public int getPlayerRow() {
        return model.getPlayerRow();
    }

    /**
     * Returns the player's column.
     *
     * @return player column
     */
    public int getPlayerColumn() {
        return model.getPlayerColumn();
    }

    /**
     * Stops the program resources.
     */
    public void stopProgram() {
        model.stopProgram();
    }

    /**
     * Moves the player by a general row and column change.
     *
     * @param rowChange row movement
     * @param columnChange column movement
     */
    public void moveByDelta(int rowChange, int columnChange) {
        model.movePlayer(rowChange, columnChange);
    }
}