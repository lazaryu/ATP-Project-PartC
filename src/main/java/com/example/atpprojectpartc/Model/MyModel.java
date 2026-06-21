package com.example.atpprojectpartc.Model;

import algorithms.mazeGenerators.Maze;
import algorithms.mazeGenerators.MyMazeGenerator;

/**
 * MyModel is responsible for the application logic.
 */
public class MyModel implements IModel {

    private Maze maze;

    /**
     * Generates a maze using the maze generator from Part B.
     *
     * @param rows number of rows
     * @param columns number of columns
     */
    @Override
    public void generateMaze(int rows, int columns) {
        MyMazeGenerator generator = new MyMazeGenerator();
        maze = generator.generate(rows, columns);
    }

    /**
     * Returns the current maze.
     *
     * @return current maze
     */
    @Override
    public Maze getMaze() {
        return maze;
    }
}