package com.example.atpprojectpartc.ViewModel;

import algorithms.mazeGenerators.Maze;
import algorithms.search.Solution;
import com.example.atpprojectpartc.Model.IModel;

import java.util.Observable;
import java.util.Observer;

/**
 * MyViewModel connects the View layer with the Model layer.
 * It observes the Model and notifies the View.
 */
@SuppressWarnings("deprecation")
public class MyViewModel extends Observable implements Observer {

    private final IModel model;

    /**
     * Constructor.
     *
     * @param model the model layer
     */
    public MyViewModel(IModel model) {
        this.model = model;
        this.model.assignObserver(this);
    }

    /**
     * Generates a new maze.
     *
     * @param rows number of rows
     * @param columns number of columns
     */
    public void generateMaze(int rows, int columns) {
        model.generateMaze(rows, columns);
    }

    /**
     * Requests solving the current maze.
     */
    public void solveMaze() {
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
     * Moves the player up-left.
     */
    public void moveUpLeft() {
        model.movePlayer(-1, -1);
    }

    /**
     * Moves the player up-right.
     */
    public void moveUpRight() {
        model.movePlayer(-1, 1);
    }

    /**
     * Moves the player down-left.
     */
    public void moveDownLeft() {
        model.movePlayer(1, -1);
    }

    /**
     * Moves the player down-right.
     */
    public void moveDownRight() {
        model.movePlayer(1, 1);
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

    /**
     * Stops program resources.
     */
    public void stopProgram() {
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
            setChanged();
            notifyObservers(arg);
        }
    }
}