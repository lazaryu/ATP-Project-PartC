package com.example.atpprojectpartc.Model;

import algorithms.mazeGenerators.Maze;
import algorithms.mazeGenerators.MyMazeGenerator;

/**
 * MyModel is responsible for the application logic.
 * It communicates with the maze generation and solving logic from Part B.
 */
public class MyModel implements IModel {

    private Maze maze;

    /**
     * Generates a new maze using the maze generator from Part B.
     *
     * @param rows number of maze rows
     * @param columns number of maze columns
     */
    @Override
    public void generateMaze(int rows, int columns) {
        MyMazeGenerator generator = new MyMazeGenerator();
        maze = generator.generate(rows, columns);

        System.out.println("Maze generated successfully:");
        maze.print();
    }

    /**
     * Returns the current maze.
     *
     * @return the current maze
     */
    @Override
    public Maze getMaze() {
        return maze;
    }
}