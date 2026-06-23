package com.example.atpprojectpartc.Model;

import algorithms.mazeGenerators.Maze;
import algorithms.mazeGenerators.MyMazeGenerator;

import java.util.Observable;
import java.util.Observer;

/**
 * MyModel is responsible for the application logic.
 * It creates the maze, stores the player position, validates movement,
 * and notifies the ViewModel when something changes.
 */
@SuppressWarnings("deprecation")
public class MyModel extends Observable implements IModel {

    private Maze maze;
    private int playerRow;
    private int playerColumn;
    private boolean gameFinished;

    /**
     * Generates a new maze and places the player at the start position.
     *
     * @param rows number of rows
     * @param columns number of columns
     */
    @Override
    public void generateMaze(int rows, int columns) {
        MyMazeGenerator generator = new MyMazeGenerator();
        maze = generator.generate(rows, columns);

        playerRow = maze.getStartPosition().getRowIndex();
        playerColumn = maze.getStartPosition().getColumnIndex();
        gameFinished = false;

        setChanged();
        notifyObservers("mazeGenerated");
    }

    /**
     * Moves the player only if the movement is legal.
     *
     * @param rowChange row movement
     * @param columnChange column movement
     */
    @Override
    public void movePlayer(int rowChange, int columnChange) {
        if (maze == null || gameFinished) {
            return;
        }

        int newRow = playerRow + rowChange;
        int newColumn = playerColumn + columnChange;

        if (!isInsideMaze(newRow, newColumn)) {
            return;
        }

        if (isWall(newRow, newColumn)) {
            return;
        }

        playerRow = newRow;
        playerColumn = newColumn;

        setChanged();
        notifyObservers("playerMoved");

        if (isPlayerAtGoal()) {
            gameFinished = true;
            setChanged();
            notifyObservers("gameWon");
        }
    }

    /**
     * Checks if a position is inside the maze.
     *
     * @param row row index
     * @param column column index
     * @return true if inside the maze
     */
    private boolean isInsideMaze(int row, int column) {
        int[][] mazeMatrix = maze.getMaze();

        return row >= 0
                && row < mazeMatrix.length
                && column >= 0
                && column < mazeMatrix[0].length;
    }

    /**
     * Checks if a position is a wall.
     *
     * @param row row index
     * @param column column index
     * @return true if the cell is a wall
     */
    private boolean isWall(int row, int column) {
        return maze.getMaze()[row][column] == 1;
    }

    /**
     * Checks if the player reached the goal.
     *
     * @return true if the player reached the goal
     */
    private boolean isPlayerAtGoal() {
        return playerRow == maze.getGoalPosition().getRowIndex()
                && playerColumn == maze.getGoalPosition().getColumnIndex();
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

    /**
     * Returns the player's current row.
     *
     * @return player row
     */
    @Override
    public int getPlayerRow() {
        return playerRow;
    }

    /**
     * Returns the player's current column.
     *
     * @return player column
     */
    @Override
    public int getPlayerColumn() {
        return playerColumn;
    }

    /**
     * Adds an observer to the model.
     *
     * @param observer observer to add
     */
    @Override
    public void assignObserver(Observer observer) {
        addObserver(observer);
    }

    /**
     * Stops resources before closing the application.
     * Currently there are no running resources to stop.
     */
    @Override
    public void stopProgram() {
        // Later, if we start servers from Part B, we will stop them here.
    }
}