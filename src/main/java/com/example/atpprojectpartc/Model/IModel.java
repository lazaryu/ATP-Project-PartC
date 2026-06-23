package com.example.atpprojectpartc.Model;

import algorithms.mazeGenerators.Maze;
import algorithms.search.Solution;

import java.io.File;
import java.io.IOException;
import java.util.Observer;

/**
 * IModel defines the operations of the Model layer.
 */
@SuppressWarnings("deprecation")
public interface IModel {

    void generateMaze(int rows, int columns);

    void movePlayer(int rowChange, int columnChange);

    void solveMaze();

    void saveMaze(File file) throws IOException;

    void loadMaze(File file) throws IOException;

    Maze getMaze();

    Solution getSolution();

    int getPlayerRow();

    int getPlayerColumn();

    void assignObserver(Observer observer);

    void stopProgram();
}