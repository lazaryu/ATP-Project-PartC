package com.example.atpprojectpartc.Model;

import algorithms.mazeGenerators.Maze;
import algorithms.mazeGenerators.MyMazeGenerator;
import algorithms.search.BestFirstSearch;
import algorithms.search.ISearchingAlgorithm;
import algorithms.search.SearchableMaze;
import algorithms.search.Solution;

import java.util.Observable;
import java.util.Observer;

/**
 * MyModel is responsible for the application logic.
 * It generates the maze, stores the player position,
 * validates player movement, solves the maze,
 * and notifies the ViewModel about changes.
 */
@SuppressWarnings("deprecation")
public class MyModel extends Observable implements IModel {

    private Maze maze;
    private Solution solution;

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
        solution = null;

        playerRow = maze.getStartPosition().getRowIndex();
        playerColumn = maze.getStartPosition().getColumnIndex();

        gameFinished = false;

        setChanged();
        notifyObservers("mazeGenerated");
    }

    /**
     * Moves the player if the movement is legal.
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

        if (!isMoveLegal(newRow, newColumn)) {
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
     * Solves the current maze using the searching algorithm from Part B.
     */
    @Override
    public void solveMaze() {
        if (maze == null) {
            return;
        }

        ISearchingAlgorithm searchingAlgorithm = new BestFirstSearch();
        SearchableMaze searchableMaze = new SearchableMaze(maze);

        solution = searchingAlgorithm.solve(searchableMaze);

        setChanged();
        notifyObservers("solutionReady");
    }

    /**
     * Checks if a movement is legal.
     *
     * @param row row index
     * @param column column index
     * @return true if legal
     */
    private boolean isMoveLegal(int row, int column) {
        return isInsideMaze(row, column) && !isWall(row, column);
    }

    /**
     * Checks if a cell is inside the maze.
     *
     * @param row row index
     * @param column column index
     * @return true if inside maze
     */
    private boolean isInsideMaze(int row, int column) {
        int[][] mazeMatrix = maze.getMaze();

        return row >= 0
                && row < mazeMatrix.length
                && column >= 0
                && column < mazeMatrix[0].length;
    }

    /**
     * Checks if a cell is a wall.
     *
     * @param row row index
     * @param column column index
     * @return true if wall
     */
    private boolean isWall(int row, int column) {
        return maze.getMaze()[row][column] == 1;
    }

    /**
     * Checks if the player reached the goal.
     *
     * @return true if player reached goal
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
     * Returns the current solution.
     *
     * @return current solution
     */
    @Override
    public Solution getSolution() {
        return solution;
    }

    /**
     * Returns the player row.
     *
     * @return player row
     */
    @Override
    public int getPlayerRow() {
        return playerRow;
    }

    /**
     * Returns the player column.
     *
     * @return player column
     */
    @Override
    public int getPlayerColumn() {
        return playerColumn;
    }

    /**
     * Assigns an observer to the model.
     *
     * @param observer observer to add
     */
    @Override
    public void assignObserver(Observer observer) {
        addObserver(observer);
    }

    /**
     * Stops program resources.
     */
    @Override
    public void stopProgram() {
        // Later, if servers are started from Part B, stop them here.
    }
}