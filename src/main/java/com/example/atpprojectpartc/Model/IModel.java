package com.example.atpprojectpartc.Model;

import algorithms.mazeGenerators.Maze;
import algorithms.search.Solution;

import java.util.Observer;

/**
 * IModel defines the operations of the Model layer.
 */
@SuppressWarnings("deprecation")
public interface IModel {

    /**
     * Generates a new maze.
     *
     * @param rows number of rows
     * @param columns number of columns
     */
    void generateMaze(int rows, int columns);

    /**
     * Moves the player.
     *
     * @param rowChange row movement
     * @param columnChange column movement
     */
    void movePlayer(int rowChange, int columnChange);

    /**
     * Solves the current maze.
     */
    void solveMaze();

    /**
     * Returns the current maze.
     *
     * @return current maze
     */
    Maze getMaze();

    /**
     * Returns the current solution.
     *
     * @return current solution
     */
    Solution getSolution();

    /**
     * Returns the player row.
     *
     * @return player row
     */
    int getPlayerRow();

    /**
     * Returns the player column.
     *
     * @return player column
     */
    int getPlayerColumn();

    /**
     * Assigns an observer to the model.
     *
     * @param observer observer to add
     */
    void assignObserver(Observer observer);

    /**
     * Stops program resources.
     */
    void stopProgram();
}