package com.example.atpprojectpartc.Model;

import algorithms.mazeGenerators.Maze;

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
     * Returns the current maze.
     *
     * @return current maze
     */
    Maze getMaze();
}