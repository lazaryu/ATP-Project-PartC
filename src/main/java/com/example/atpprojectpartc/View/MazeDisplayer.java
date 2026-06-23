package com.example.atpprojectpartc.View;

import algorithms.mazeGenerators.Maze;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 * Custom JavaFX Canvas for drawing the maze, start position, goal position,
 * and player position.
 */
public class MazeDisplayer extends Canvas {

    private Maze maze;
    private int playerRow;
    private int playerColumn;

    /**
     * Sets the maze and player position, then redraws the board.
     *
     * @param maze the maze to display
     * @param playerRow the player's row
     * @param playerColumn the player's column
     */
    public void setMaze(Maze maze, int playerRow, int playerColumn) {
        this.maze = maze;
        this.playerRow = playerRow;
        this.playerColumn = playerColumn;
        draw();
    }

    /**
     * Updates only the player position and redraws.
     *
     * @param playerRow the player's row
     * @param playerColumn the player's column
     */
    public void updatePlayerPosition(int playerRow, int playerColumn) {
        this.playerRow = playerRow;
        this.playerColumn = playerColumn;
        draw();
    }

    /**
     * Draws the maze, entrance, exit, and player.
     */
    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();

        double canvasWidth = getWidth();
        double canvasHeight = getHeight();

        gc.clearRect(0, 0, canvasWidth, canvasHeight);

        gc.setFill(Color.web("#ECEFF1"));
        gc.fillRect(0, 0, canvasWidth, canvasHeight);

        if (maze == null) {
            return;
        }

        int[][] mazeMap = maze.getMaze();
        int rows = mazeMap.length;
        int columns = mazeMap[0].length;

        double cellWidth = canvasWidth / columns;
        double cellHeight = canvasHeight / rows;

        drawMazeCells(gc, mazeMap, cellWidth, cellHeight);
        drawStartCell(gc, cellWidth, cellHeight);
        drawGoalCell(gc, cellWidth, cellHeight);
        drawPlayer(gc, cellWidth, cellHeight);
        drawStartAndGoalLetters(gc, cellWidth, cellHeight);
    }

    /**
     * Draws the maze cells.
     */
    private void drawMazeCells(GraphicsContext gc, int[][] mazeMap, double cellWidth, double cellHeight) {
        for (int row = 0; row < mazeMap.length; row++) {
            for (int column = 0; column < mazeMap[row].length; column++) {
                if (mazeMap[row][column] == 1) {
                    gc.setFill(Color.web("#102A43"));
                } else {
                    gc.setFill(Color.web("#F8F9FA"));
                }

                gc.fillRect(
                        column * cellWidth,
                        row * cellHeight,
                        cellWidth,
                        cellHeight
                );
            }
        }
    }

    /**
     * Draws the entrance cell.
     */
    private void drawStartCell(GraphicsContext gc, double cellWidth, double cellHeight) {
        int startRow = maze.getStartPosition().getRowIndex();
        int startColumn = maze.getStartPosition().getColumnIndex();

        gc.setFill(Color.web("#2A9D8F"));
        gc.fillRect(
                startColumn * cellWidth,
                startRow * cellHeight,
                cellWidth,
                cellHeight
        );
    }

    /**
     * Draws the exit cell.
     */
    private void drawGoalCell(GraphicsContext gc, double cellWidth, double cellHeight) {
        int goalRow = maze.getGoalPosition().getRowIndex();
        int goalColumn = maze.getGoalPosition().getColumnIndex();

        gc.setFill(Color.web("#FFB703"));
        gc.fillRect(
                goalColumn * cellWidth,
                goalRow * cellHeight,
                cellWidth,
                cellHeight
        );
    }

    /**
     * Draws the player.
     */
    private void drawPlayer(GraphicsContext gc, double cellWidth, double cellHeight) {
        double x = playerColumn * cellWidth;
        double y = playerRow * cellHeight;

        gc.setFill(Color.web("#0078D7"));
        gc.fillOval(
                x + cellWidth * 0.18,
                y + cellHeight * 0.18,
                cellWidth * 0.64,
                cellHeight * 0.64
        );

        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeOval(
                x + cellWidth * 0.18,
                y + cellHeight * 0.18,
                cellWidth * 0.64,
                cellHeight * 0.64
        );
    }

    /**
     * Draws S on the start cell and E on the goal cell.
     */
    private void drawStartAndGoalLetters(GraphicsContext gc, double cellWidth, double cellHeight) {
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFont(Font.font(Math.max(10, Math.min(cellWidth, cellHeight) * 0.55)));
        gc.setFill(Color.WHITE);

        int startRow = maze.getStartPosition().getRowIndex();
        int startColumn = maze.getStartPosition().getColumnIndex();

        gc.fillText(
                "S",
                startColumn * cellWidth + cellWidth / 2,
                startRow * cellHeight + cellHeight / 2
        );

        int goalRow = maze.getGoalPosition().getRowIndex();
        int goalColumn = maze.getGoalPosition().getColumnIndex();

        gc.fillText(
                "E",
                goalColumn * cellWidth + cellWidth / 2,
                goalRow * cellHeight + cellHeight / 2
        );
    }
}