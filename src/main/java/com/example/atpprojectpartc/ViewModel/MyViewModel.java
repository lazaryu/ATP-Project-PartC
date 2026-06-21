package com.example.atpprojectpartc.ViewModel;

import algorithms.mazeGenerators.Maze;
import com.example.atpprojectpartc.Model.IModel;

/**
 * MyViewModel connects the View layer with the Model layer.
 */
public class MyViewModel {

    private final IModel model;

    /**
     * Constructor.
     *
     * @param model the model layer
     */
    public MyViewModel(IModel model) {
        this.model = model;
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
     * Returns the current maze.
     *
     * @return current maze
     */
    public Maze getMaze() {
        return model.getMaze();
    }
}