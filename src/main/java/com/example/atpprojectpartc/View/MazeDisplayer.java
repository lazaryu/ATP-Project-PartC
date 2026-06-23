package com.example.atpprojectpartc.View;

import algorithms.mazeGenerators.Maze;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * MazeDisplayer is a custom JavaFX control that draws the maze on a Canvas.
 */
public class MazeDisplayer extends Canvas {

    private Maze maze;
    private int playerRow;
    private int playerColumn;

    /**
     * Sets the maze and player position.
     *
     * @param maze maze to display
     * @param playerRow player row
     * @param playerColumn player column
     */
    public void setMaze(Maze maze, int playerRow, int playerColumn) {
        this.maze = maze;
        this.playerRow = playerRow;
        this.playerColumn = playerColumn;
        draw();
    }

    /**
     * Updates the player position and redraws the maze.
     *
     * @param playerRow player row
     * @param playerColumn player column
     */
    public void updatePlayerPosition(int playerRow, int playerColumn) {
        this.playerRow = playerRow;
        this.playerColumn = playerColumn;
        draw();
    }

    /**
     * Draws the maze, goal, and player.
     */
    private void draw() {
        GraphicsContext graphicsContext = getGraphicsContext2D();

        double canvasWidth = getWidth();
        double canvasHeight = getHeight();

        graphicsContext.clearRect(0, 0, canvasWidth, canvasHeight);

        graphicsContext.setFill(Color.web("#eceff1"));
        graphicsContext.fillRect(0, 0, canvasWidth, canvasHeight);

        if (maze == null) {
            return;
        }

        int[][] mazeMatrix = maze.getMaze();

        int rows = mazeMatrix.length;
        int columns = mazeMatrix[0].length;

        double cellWidth = canvasWidth / columns;
        double cellHeight = canvasHeight / rows;

        drawCells(graphicsContext, mazeMatrix, cellWidth, cellHeight);
        drawGoal(graphicsContext, cellWidth, cellHeight);
        drawPlayer(graphicsContext, cellWidth, cellHeight);
    }

    /**
     * Draws all maze cells.
     *
     * @param graphicsContext graphics context
     * @param mazeMatrix maze matrix
     * @param cellWidth cell width
     * @param cellHeight cell height
     */
    private void drawCells(GraphicsContext graphicsContext, int[][] mazeMatrix, double cellWidth, double cellHeight) {
        for (int row = 0; row < mazeMatrix.length; row++) {
            for (int column = 0; column < mazeMatrix[row].length; column++) {
                if (mazeMatrix[row][column] == 1) {
                    graphicsContext.setFill(Color.web("#102a43"));
                } else {
                    graphicsContext.setFill(Color.web("#f8f9fa"));
                }

                graphicsContext.fillRect(
                        column * cellWidth,
                        row * cellHeight,
                        cellWidth,
                        cellHeight
                );
            }
        }
    }

    /**
     * Draws the goal position.
     *
     * @param graphicsContext graphics context
     * @param cellWidth cell width
     * @param cellHeight cell height
     */
    private void drawGoal(GraphicsContext graphicsContext, double cellWidth, double cellHeight) {
        int goalRow = maze.getGoalPosition().getRowIndex();
        int goalColumn = maze.getGoalPosition().getColumnIndex();

        double x = goalColumn * cellWidth;
        double y = goalRow * cellHeight;

        graphicsContext.setFill(Color.web("#ffb703"));
        graphicsContext.fillOval(
                x + cellWidth * 0.15,
                y + cellHeight * 0.15,
                cellWidth * 0.7,
                cellHeight * 0.7
        );
    }

    /**
     * Draws the player.
     *
     * @param graphicsContext graphics context
     * @param cellWidth cell width
     * @param cellHeight cell height
     */
    private void drawPlayer(GraphicsContext graphicsContext, double cellWidth, double cellHeight) {
        double x = playerColumn * cellWidth;
        double y = playerRow * cellHeight;

        graphicsContext.setFill(Color.web("#0078d7"));
        graphicsContext.fillOval(
                x + cellWidth * 0.12,
                y + cellHeight * 0.12,
                cellWidth * 0.76,
                cellHeight * 0.76
        );

        graphicsContext.setStroke(Color.WHITE);
        graphicsContext.setLineWidth(2);
        graphicsContext.strokeOval(
                x + cellWidth * 0.12,
                y + cellHeight * 0.12,
                cellWidth * 0.76,
                cellHeight * 0.76
        );
    }
}