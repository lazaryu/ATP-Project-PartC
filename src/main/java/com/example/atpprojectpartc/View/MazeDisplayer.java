package com.example.atpprojectpartc.View;

import algorithms.mazeGenerators.Maze;
import algorithms.search.AState;
import algorithms.search.Solution;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.io.InputStream;

/**
 * MazeDisplayer is responsible for drawing the maze in Pac-Man style.
 * It draws the maze walls, empty paths, player image, goal image,
 * and optionally solution dots only when a solution is requested.
 */
public class MazeDisplayer extends Canvas {

    private Maze maze;
    private Solution solution;

    private int playerRow;
    private int playerColumn;

    private Image pacmanImage;
    private Image rewardImage;

    /**
     * Constructor.
     * Loads the player and reward images.
     */
    public MazeDisplayer() {
        loadImages();
    }

    /**
     * Loads images from the resources folder.
     */
    private void loadImages() {
        try {
            InputStream pacmanStream = getClass().getResourceAsStream(
                    "/com/example/atpprojectpartc/View/Images/Pacman_image.png"
            );

            if (pacmanStream != null) {
                pacmanImage = new Image(pacmanStream);
            }

            InputStream rewardStream = getClass().getResourceAsStream(
                    "/com/example/atpprojectpartc/View/Images/reward_ball.png"
            );

            if (rewardStream != null) {
                rewardImage = new Image(rewardStream);
            }

        } catch (Exception e) {
            System.out.println("Could not load images. Default drawing will be used.");
        }
    }

    /**
     * Sets the maze and player position.
     *
     * @param maze maze to draw
     * @param playerRow player row
     * @param playerColumn player column
     */
    public void setMaze(Maze maze, int playerRow, int playerColumn) {
        this.maze = maze;
        this.solution = null;
        this.playerRow = playerRow;
        this.playerColumn = playerColumn;
        drawMaze();
    }

    /**
     * Sets the solution that should be displayed.
     *
     * @param solution maze solution
     */
    public void setSolution(Solution solution) {
        this.solution = solution;
        drawMaze();
    }

    /**
     * Clears the solution from the display.
     */
    public void clearSolution() {
        this.solution = null;
        drawMaze();
    }

    /**
     * Updates the player position.
     *
     * @param playerRow player row
     * @param playerColumn player column
     */
    public void updatePlayerPosition(int playerRow, int playerColumn) {
        this.playerRow = playerRow;
        this.playerColumn = playerColumn;
        drawMaze();
    }

    /**
     * Draws the full maze.
     */
    public void drawMaze() {
        GraphicsContext graphicsContext = getGraphicsContext2D();

        double canvasWidth = getWidth();
        double canvasHeight = getHeight();

        graphicsContext.clearRect(0, 0, canvasWidth, canvasHeight);

        graphicsContext.setFill(Color.BLACK);
        graphicsContext.fillRect(0, 0, canvasWidth, canvasHeight);

        if (maze == null) {
            return;
        }

        int[][] mazeMatrix = maze.getMaze();

        int rows = mazeMatrix.length;
        int columns = mazeMatrix[0].length;

        double cellWidth = canvasWidth / columns;
        double cellHeight = canvasHeight / rows;

        drawMazeCells(graphicsContext, mazeMatrix, cellWidth, cellHeight);
        drawSolutionPath(graphicsContext, cellWidth, cellHeight);
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
    private void drawMazeCells(GraphicsContext graphicsContext, int[][] mazeMatrix, double cellWidth, double cellHeight) {
        for (int row = 0; row < mazeMatrix.length; row++) {
            for (int column = 0; column < mazeMatrix[row].length; column++) {
                double x = column * cellWidth;
                double y = row * cellHeight;

                if (mazeMatrix[row][column] == 1) {
                    drawWall(graphicsContext, x, y, cellWidth, cellHeight);
                } else {
                    drawPath(graphicsContext, x, y, cellWidth, cellHeight);
                }
            }
        }
    }

    /**
     * Draws a Pac-Man style wall.
     *
     * @param graphicsContext graphics context
     * @param x x coordinate
     * @param y y coordinate
     * @param cellWidth cell width
     * @param cellHeight cell height
     */
    private void drawWall(GraphicsContext graphicsContext, double x, double y, double cellWidth, double cellHeight) {
        graphicsContext.setFill(Color.web("#001b5e"));
        graphicsContext.fillRect(x, y, cellWidth, cellHeight);

        graphicsContext.setStroke(Color.web("#1e90ff"));
        graphicsContext.setLineWidth(2);
        graphicsContext.strokeRect(x + 1, y + 1, cellWidth - 2, cellHeight - 2);
    }

    /**
     * Draws an empty path cell.
     * No dots are drawn here.
     *
     * @param graphicsContext graphics context
     * @param x x coordinate
     * @param y y coordinate
     * @param cellWidth cell width
     * @param cellHeight cell height
     */
    private void drawPath(GraphicsContext graphicsContext, double x, double y, double cellWidth, double cellHeight) {
        graphicsContext.setFill(Color.BLACK);
        graphicsContext.fillRect(x, y, cellWidth, cellHeight);
    }

    /**
     * Draws Pac-Man dots only on the solution path.
     *
     * @param graphicsContext graphics context
     * @param cellWidth cell width
     * @param cellHeight cell height
     */
    private void drawSolutionPath(GraphicsContext graphicsContext, double cellWidth, double cellHeight) {
        if (solution == null) {
            return;
        }

        for (AState state : solution.getSolutionPath()) {
            int[] position = parsePositionFromState(state);

            if (position == null) {
                continue;
            }

            int row = position[0];
            int column = position[1];

            if (isPlayerPosition(row, column) || isGoalPosition(row, column)) {
                continue;
            }

            double x = column * cellWidth;
            double y = row * cellHeight;

            double dotSize = Math.min(cellWidth, cellHeight) * 0.25;

            graphicsContext.setFill(Color.web("#fff2b2"));
            graphicsContext.fillOval(
                    x + cellWidth / 2 - dotSize / 2,
                    y + cellHeight / 2 - dotSize / 2,
                    dotSize,
                    dotSize
            );
        }
    }

    /**
     * Parses row and column from a maze state string.
     * The expected format is usually "{row,column}".
     *
     * @param state maze state
     * @return int array with row and column, or null if parsing failed
     */
    private int[] parsePositionFromState(AState state) {
        try {
            String text = state.toString();

            text = text.replace("{", "")
                    .replace("}", "")
                    .replace("(", "")
                    .replace(")", "")
                    .replace("[", "")
                    .replace("]", "")
                    .trim();

            String[] parts = text.split(",");

            if (parts.length < 2) {
                return null;
            }

            int row = Integer.parseInt(parts[0].trim());
            int column = Integer.parseInt(parts[1].trim());

            return new int[]{row, column};

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Checks if a cell is the player position.
     *
     * @param row row index
     * @param column column index
     * @return true if player position
     */
    private boolean isPlayerPosition(int row, int column) {
        return row == playerRow && column == playerColumn;
    }

    /**
     * Checks if a cell is the goal position.
     *
     * @param row row index
     * @param column column index
     * @return true if goal position
     */
    private boolean isGoalPosition(int row, int column) {
        return row == maze.getGoalPosition().getRowIndex()
                && column == maze.getGoalPosition().getColumnIndex();
    }

    /**
     * Draws the reward image.
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

        double paddingX = cellWidth * 0.12;
        double paddingY = cellHeight * 0.12;

        if (rewardImage != null) {
            graphicsContext.drawImage(
                    rewardImage,
                    x + paddingX,
                    y + paddingY,
                    cellWidth - 2 * paddingX,
                    cellHeight - 2 * paddingY
            );
        } else {
            graphicsContext.setFill(Color.web("#ff2d55"));
            graphicsContext.fillOval(
                    x + paddingX,
                    y + paddingY,
                    cellWidth - 2 * paddingX,
                    cellHeight - 2 * paddingY
            );
        }
    }

    /**
     * Draws the Pac-Man player image.
     *
     * @param graphicsContext graphics context
     * @param cellWidth cell width
     * @param cellHeight cell height
     */
    private void drawPlayer(GraphicsContext graphicsContext, double cellWidth, double cellHeight) {
        double x = playerColumn * cellWidth;
        double y = playerRow * cellHeight;

        double paddingX = cellWidth * 0.08;
        double paddingY = cellHeight * 0.08;

        if (pacmanImage != null) {
            graphicsContext.drawImage(
                    pacmanImage,
                    x + paddingX,
                    y + paddingY,
                    cellWidth - 2 * paddingX,
                    cellHeight - 2 * paddingY
            );
        } else {
            graphicsContext.setFill(Color.YELLOW);
            graphicsContext.fillOval(
                    x + paddingX,
                    y + paddingY,
                    cellWidth - 2 * paddingX,
                    cellHeight - 2 * paddingY
            );
        }
    }
}