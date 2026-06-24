package com.example.atpprojectpartc.View;

import algorithms.mazeGenerators.Maze;
import algorithms.search.AState;
import algorithms.search.Solution;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

import java.io.InputStream;

/**
 * MazeDisplayer is a custom JavaFX Canvas component.
 * It draws the maze, the player, the goal, the start marker,
 * and the solution path when requested.
 */
public class MazeDisplayer extends Canvas {

    private Maze maze;
    private Solution solution;

    private int playerRow;
    private int playerColumn;

    private int startRow;
    private int startColumn;

    /**
     * Pac-Man image faces right by default.
     *
     * 0 degrees   = right
     * 90 degrees  = down
     * 180 degrees = left
     * -90 degrees = up
     */
    private double pacmanRotationAngle = 0.0;

    private Image pacmanImage;
    private Image rewardImage;

    /**
     * Constructor.
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
     * Sets a new maze and player position.
     *
     * @param maze maze to draw
     * @param playerRow player row
     * @param playerColumn player column
     */
    public void setMaze(Maze maze, int playerRow, int playerColumn) {
        boolean isNewMaze = this.maze != maze;

        this.maze = maze;
        this.solution = null;

        this.playerRow = playerRow;
        this.playerColumn = playerColumn;

        this.startRow = maze.getStartPosition().getRowIndex();
        this.startColumn = maze.getStartPosition().getColumnIndex();

        if (isNewMaze) {
            pacmanRotationAngle = 0.0;
        }

        drawMaze();
    }

    /**
     * Sets the solution to display on the maze.
     *
     * @param solution maze solution
     */
    public void setSolution(Solution solution) {
        this.solution = solution;
        drawMaze();
    }

    /**
     * Clears the solution from the maze display.
     */
    public void clearSolution() {
        this.solution = null;
        drawMaze();
    }

    /**
     * Updates the player position and changes Pac-Man direction.
     *
     * @param newPlayerRow new player row
     * @param newPlayerColumn new player column
     */
    public void updatePlayerPosition(int newPlayerRow, int newPlayerColumn) {
        updatePacmanDirection(newPlayerRow, newPlayerColumn);

        this.playerRow = newPlayerRow;
        this.playerColumn = newPlayerColumn;

        drawMaze();
    }

    /**
     * Updates Pac-Man mouth direction according to the movement direction.
     *
     * The original image faces right, so:
     * moving right  -> angle 0
     * moving down   -> angle 90
     * moving left   -> angle 180
     * moving up     -> angle -90
     *
     * @param newPlayerRow new player row
     * @param newPlayerColumn new player column
     */
    private void updatePacmanDirection(int newPlayerRow, int newPlayerColumn) {
        int rowDifference = newPlayerRow - playerRow;
        int columnDifference = newPlayerColumn - playerColumn;

        if (rowDifference == 0 && columnDifference == 0) {
            return;
        }

        pacmanRotationAngle = Math.toDegrees(
                Math.atan2(rowDifference, columnDifference)
        );
    }

    /**
     * Draws the complete maze.
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
        drawStartPosition(graphicsContext, cellWidth, cellHeight);
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
     * Draws a wall cell.
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
        graphicsContext.strokeRect(
                x + 1,
                y + 1,
                cellWidth - 2,
                cellHeight - 2
        );
    }

    /**
     * Draws an empty path cell.
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
     * Draws a green marker on the start position.
     * The marker remains visible after Pac-Man moves away.
     *
     * @param graphicsContext graphics context
     * @param cellWidth cell width
     * @param cellHeight cell height
     */
    private void drawStartPosition(GraphicsContext graphicsContext, double cellWidth, double cellHeight) {
        double x = startColumn * cellWidth;
        double y = startRow * cellHeight;

        double paddingX = cellWidth * 0.20;
        double paddingY = cellHeight * 0.20;

        graphicsContext.setFill(Color.web("#00FF66"));
        graphicsContext.fillOval(
                x + paddingX,
                y + paddingY,
                cellWidth - 2 * paddingX,
                cellHeight - 2 * paddingY
        );

        graphicsContext.setStroke(Color.WHITE);
        graphicsContext.setLineWidth(2);
        graphicsContext.strokeOval(
                x + paddingX,
                y + paddingY,
                cellWidth - 2 * paddingX,
                cellHeight - 2 * paddingY
        );
    }

    /**
     * Draws solution dots if a solution exists.
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

            if (isPlayerPosition(row, column) ||
                    isGoalPosition(row, column) ||
                    isStartPosition(row, column)) {
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
     * Parses row and column from a search state.
     *
     * @param state maze state
     * @return array of row and column, or null if parsing failed
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
     * Checks if a cell is the start position.
     *
     * @param row row index
     * @param column column index
     * @return true if start position
     */
    private boolean isStartPosition(int row, int column) {
        return row == startRow && column == startColumn;
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
     * Draws the goal image.
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
     * Draws the Pac-Man player.
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

        double drawX = x + paddingX;
        double drawY = y + paddingY;
        double drawWidth = cellWidth - 2 * paddingX;
        double drawHeight = cellHeight - 2 * paddingY;

        if (pacmanImage != null) {
            drawRotatedPacmanImage(
                    graphicsContext,
                    drawX,
                    drawY,
                    drawWidth,
                    drawHeight
            );
        } else {
            drawRotatedDefaultPacman(
                    graphicsContext,
                    drawX,
                    drawY,
                    drawWidth,
                    drawHeight
            );
        }
    }

    /**
     * Draws the Pac-Man image rotated around its center.
     * The original image already faces right, so no rotation fix is needed.
     *
     * @param graphicsContext graphics context
     * @param x x coordinate
     * @param y y coordinate
     * @param width image width
     * @param height image height
     */
    private void drawRotatedPacmanImage(GraphicsContext graphicsContext, double x, double y, double width, double height) {
        double centerX = x + width / 2;
        double centerY = y + height / 2;

        graphicsContext.save();

        graphicsContext.translate(centerX, centerY);
        graphicsContext.rotate(pacmanRotationAngle);

        graphicsContext.drawImage(
                pacmanImage,
                -width / 2,
                -height / 2,
                width,
                height
        );

        graphicsContext.restore();
    }

    /**
     * Draws a default Pac-Man shape if the image was not loaded.
     *
     * @param graphicsContext graphics context
     * @param x x coordinate
     * @param y y coordinate
     * @param width shape width
     * @param height shape height
     */
    private void drawRotatedDefaultPacman(GraphicsContext graphicsContext, double x, double y, double width, double height) {
        double centerX = x + width / 2;
        double centerY = y + height / 2;

        graphicsContext.save();

        graphicsContext.translate(centerX, centerY);
        graphicsContext.rotate(pacmanRotationAngle);

        graphicsContext.setFill(Color.YELLOW);
        graphicsContext.fillArc(
                -width / 2,
                -height / 2,
                width,
                height,
                35,
                290,
                ArcType.ROUND
        );

        graphicsContext.restore();
    }
}