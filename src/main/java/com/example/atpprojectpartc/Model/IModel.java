package com.example.atpprojectpartc.Model;

import algorithms.mazeGenerators.Maze;

import java.util.Observer;

/**
 * IModel defines the operations of the Model layer.
 */
public interface IModel {

    /**
     * Generates a new maze.
     *
     * @param rows number of rows
     * @param columns number of columns
     */
    void generateMaze(int rows, int columns);

    /**
     * Moves the player if the requested movement is legal.
     *
     * @param rowChange row movement
     * @param columnChange column movement
     */
    void movePlayer(int rowChange, int columnChange);

    /**
     * Returns the current maze.
     *
     * @return current maze
     */
    Maze getMaze();

    /**
     * Returns the player's current row.
     *
     * @return player row
     */
    int getPlayerRow();

    /**
     * Returns the player's current column.
     *
     * @return player column
     */
    int getPlayerColumn();

    /**
     * Adds an observer to the model.
     *
     * @param observer observer to add
     */
    void assignObserver(Observer observer);

    /**
     * Stops resources before closing the application.
     */
    void stopProgram();
}