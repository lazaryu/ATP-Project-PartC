package com.example.atpprojectpartc.ViewModel;

import algorithms.mazeGenerators.Maze;
import com.example.atpprojectpartc.Model.IModel;

import java.util.Observable;
import java.util.Observer;

/**
 * MyViewModel connects the View layer with the Model layer.
 * It observes the Model and is observed by the View.
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
     * Validates input and asks the model to generate a maze.
     *
     * @param rows number of rows
     * @param columns number of columns
     */
    public void generateMaze(int rows, int columns) {
        if (rows < 2 || columns < 2) {
            setChanged();
            notifyObservers("invalidMazeSize");
            return;
        }

        if (rows > 100 || columns > 100) {
            setChanged();
            notifyObservers("mazeSizeTooLarge");
            return;
        }

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
     * Returns the current maze.
     *
     * @return current maze
     */
    public Maze getMaze() {
        return model.getMaze();
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
     * Stops the program resources.
     */
    public void stopProgram() {
        model.stopProgram();
    }

    /**
     * Gets notified by the Model and notifies the View.
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