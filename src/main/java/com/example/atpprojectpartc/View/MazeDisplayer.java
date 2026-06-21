package com.example.atpprojectpartc.View;

import algorithms.mazeGenerators.Maze;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * MazeDisplayer is responsible for drawing the maze on the screen.
 */
public class MazeDisplayer extends Canvas {

    private Maze maze;

    /**
     * Sets the maze and redraws it.
     *
     * @param maze the maze to display
     */
    public void setMaze(Maze maze) {
        this.maze = maze;
        drawMaze();
    }

    /**
     * Draws the current maze on the canvas.
     */
    public void drawMaze() {
        if (maze == null) {
            return;
        }

        int[][] mazeMatrix = maze.getMaze();

        int rows = mazeMatrix.length;
        int columns = mazeMatrix[0].length;

        double canvasWidth = getWidth();
        double canvasHeight = getHeight();

        double cellWidth = canvasWidth / columns;
        double cellHeight = canvasHeight / rows;

        GraphicsContext graphicsContext = getGraphicsContext2D();

        graphicsContext.clearRect(0, 0, canvasWidth, canvasHeight);

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {

                if (mazeMatrix[row][column] == 1) {
                    graphicsContext.setFill(Color.BLACK);
                } else {
                    graphicsContext.setFill(Color.WHITE);
                }

                graphicsContext.fillRect(
                        column * cellWidth,
                        row * cellHeight,
                        cellWidth,
                        cellHeight
                );
            }
        }

        drawStartAndGoal(graphicsContext, cellWidth, cellHeight);
    }

    /**
     * Draws the start and goal positions.
     *
     * @param graphicsContext graphics context
     * @param cellWidth width of one cell
     * @param cellHeight height of one cell
     */
    private void drawStartAndGoal(GraphicsContext graphicsContext, double cellWidth, double cellHeight) {
        int startRow = maze.getStartPosition().getRowIndex();
        int startColumn = maze.getStartPosition().getColumnIndex();

        int goalRow = maze.getGoalPosition().getRowIndex();
        int goalColumn = maze.getGoalPosition().getColumnIndex();

        graphicsContext.setFill(Color.GREEN);
        graphicsContext.fillRect(
                startColumn * cellWidth,
                startRow * cellHeight,
                cellWidth,
                cellHeight
        );

        graphicsContext.setFill(Color.RED);
        graphicsContext.fillRect(
                goalColumn * cellWidth,
                goalRow * cellHeight,
                cellWidth,
                cellHeight
        );
    }
}