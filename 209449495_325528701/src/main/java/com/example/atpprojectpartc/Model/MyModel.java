package com.example.atpprojectpartc.Model;
import Client.Client;
import Client.IClientStrategy;
import IO.MyDecompressorInputStream;
import Server.Server;
import Server.ServerStrategyGenerateMaze;
import Server.ServerStrategySolveSearchProblem;
import algorithms.mazeGenerators.Maze;
import algorithms.search.Solution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.nio.file.Files;
import java.util.Observable;
import java.util.Observer;

/**
 * MyModel is responsible for the application logic.
 * It starts the Part B servers, communicates with them using clients,
 * stores the current maze, stores the player position,
 * validates player movement, solves the maze,
 * saves and loads mazes,
 * and notifies the ViewModel about changes.
 */
@SuppressWarnings("deprecation")
public class MyModel extends Observable implements IModel {

    private static final Logger logger = LogManager.getLogger(MyModel.class);

    private static final int MAZE_GENERATING_SERVER_PORT = 5400;
    private static final int MAZE_SOLVING_SERVER_PORT = 5401;
    private static final int SERVER_LISTENING_INTERVAL = 1000;

    /**
     * According to the Part B test code, the decompressed maze byte array size
     * is rows * columns + 24.
     */
    private static final int MAZE_BYTE_ARRAY_METADATA_SIZE = 24;

    private Server mazeGeneratingServer;
    private Server mazeSolvingServer;

    private Maze maze;
    private Solution solution;

    private int playerRow;
    private int playerColumn;

    private boolean gameFinished;
    private boolean serversStarted;

    /**
     * Constructor.
     * Starts the Part B servers when the model is created.
     */
    public MyModel() {
        startServers();
    }

    /**
     * Starts the maze generation server and maze solving server.
     */
    private void startServers() {
        if (serversStarted) {
            logger.debug("Servers were already started.");
            return;
        }

        logger.info(
                "Starting Part B servers. generatePort={}, solvePort={}",
                MAZE_GENERATING_SERVER_PORT,
                MAZE_SOLVING_SERVER_PORT
        );

        try {
            mazeGeneratingServer = new Server(
                    MAZE_GENERATING_SERVER_PORT,
                    SERVER_LISTENING_INTERVAL,
                    new ServerStrategyGenerateMaze()
            );

            mazeSolvingServer = new Server(
                    MAZE_SOLVING_SERVER_PORT,
                    SERVER_LISTENING_INTERVAL,
                    new ServerStrategySolveSearchProblem()
            );

            mazeSolvingServer.start();
            mazeGeneratingServer.start();

            serversStarted = true;

            /*
             * The servers start on separate threads.
             * This short wait gives them time to bind to their ports
             * before the first client request is sent.
             */
            Thread.sleep(500);

            logger.info("Part B servers started successfully.");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Server startup was interrupted.", e);

        } catch (Exception e) {
            logger.error("Failed to start Part B servers.", e);
        }
    }

    /**
     * Generates a new maze using the Part B maze generation server
     * and places the player at the start position.
     *
     * @param rows number of rows
     * @param columns number of columns
     */
    @Override
    public void generateMaze(int rows, int columns) {
        logger.info("Requesting maze generation from server. rows={}, columns={}", rows, columns);

        try {
            startServers();

            Maze generatedMaze = requestMazeFromServer(rows, columns);

            if (generatedMaze == null) {
                throw new IllegalStateException("Maze generation server returned null maze.");
            }

            maze = generatedMaze;
            solution = null;

            playerRow = maze.getStartPosition().getRowIndex();
            playerColumn = maze.getStartPosition().getColumnIndex();

            gameFinished = false;

            logger.info(
                    "Maze generated successfully by server. rows={}, columns={}, start={}, goal={}",
                    maze.getMaze().length,
                    maze.getMaze()[0].length,
                    maze.getStartPosition(),
                    maze.getGoalPosition()
            );

            setChanged();
            notifyObservers("mazeGenerated");

        } catch (Exception e) {
            logger.error("Failed to generate maze through server. rows={}, columns={}", rows, columns, e);

            setChanged();
            notifyObservers("mazeGenerationFailed");
        }
    }

    /**
     * Sends a maze generation request to the generation server.
     *
     * @param rows number of rows
     * @param columns number of columns
     * @return generated maze
     * @throws Exception if communication or decompression fails
     */
    private Maze requestMazeFromServer(int rows, int columns) throws Exception {
        final Maze[] generatedMaze = new Maze[1];
        final Exception[] clientException = new Exception[1];

        Client client = new Client(
                InetAddress.getLocalHost(),
                MAZE_GENERATING_SERVER_PORT,
                new IClientStrategy() {
                    @Override
                    public void clientStrategy(InputStream inFromServer, java.io.OutputStream outToServer) {
                        try {
                            ObjectOutputStream toServer = new ObjectOutputStream(outToServer);
                            toServer.flush();

                            ObjectInputStream fromServer = new ObjectInputStream(inFromServer);

                            int[] mazeDimensions = new int[]{rows, columns};

                            logger.debug(
                                    "Sending maze dimensions to generation server. rows={}, columns={}",
                                    rows,
                                    columns
                            );

                            toServer.writeObject(mazeDimensions);
                            toServer.flush();

                            byte[] compressedMaze = (byte[]) fromServer.readObject();

                            logger.debug(
                                    "Received compressed maze from server. compressedSizeBytes={}",
                                    compressedMaze.length
                            );

                            byte[] decompressedMaze = decompressMaze(compressedMaze, rows, columns);

                            generatedMaze[0] = new Maze(decompressedMaze);

                        } catch (Exception e) {
                            clientException[0] = e;
                            logger.error("Error while communicating with maze generation server.", e);
                        }
                    }
                }
        );

        client.communicateWithServer();

        if (clientException[0] != null) {
            throw clientException[0];
        }

        return generatedMaze[0];
    }

    /**
     * Decompresses the compressed maze returned from the server.
     *
     * @param compressedMaze compressed maze byte array
     * @param rows maze rows
     * @param columns maze columns
     * @return decompressed maze byte array
     * @throws IOException if decompression fails
     */
    private byte[] decompressMaze(byte[] compressedMaze, int rows, int columns) throws IOException {
        int decompressedSize = rows * columns + MAZE_BYTE_ARRAY_METADATA_SIZE;

        byte[] decompressedMaze = new byte[decompressedSize];

        try (InputStream decompressor = new MyDecompressorInputStream(
                new ByteArrayInputStream(compressedMaze)
        )) {
            int totalBytesRead = 0;

            while (totalBytesRead < decompressedMaze.length) {
                int bytesRead = decompressor.read(
                        decompressedMaze,
                        totalBytesRead,
                        decompressedMaze.length - totalBytesRead
                );

                if (bytesRead == -1) {
                    break;
                }

                totalBytesRead += bytesRead;
            }

            logger.debug(
                    "Maze decompressed. expectedSizeBytes={}, actualBytesRead={}",
                    decompressedSize,
                    totalBytesRead
            );
        }

        return decompressedMaze;
    }

    /**
     * Moves the player if the movement is legal.
     *
     * @param rowChange row movement
     * @param columnChange column movement
     */
    @Override
    public void movePlayer(int rowChange, int columnChange) {
        if (maze == null) {
            logger.warn("Move ignored because maze is null.");
            return;
        }

        if (gameFinished) {
            logger.debug("Move ignored because game is already finished.");
            return;
        }

        int newRow = playerRow + rowChange;
        int newColumn = playerColumn + columnChange;

        if (!isMoveLegal(newRow, newColumn, rowChange, columnChange)) {
            logger.debug(
                    "Illegal move ignored. currentRow={}, currentColumn={}, rowChange={}, columnChange={}, targetRow={}, targetColumn={}",
                    playerRow,
                    playerColumn,
                    rowChange,
                    columnChange,
                    newRow,
                    newColumn
            );
            return;
        }

        logger.debug(
                "Player moved. from=({}, {}), to=({}, {})",
                playerRow,
                playerColumn,
                newRow,
                newColumn
        );

        playerRow = newRow;
        playerColumn = newColumn;

        setChanged();
        notifyObservers("playerMoved");

        if (isPlayerAtGoal()) {
            gameFinished = true;

            logger.info("Player reached the goal at row={}, column={}.", playerRow, playerColumn);

            setChanged();
            notifyObservers("gameWon");
        }
    }

    /**
     * Solves the current maze using the Part B solving server.
     */
    @Override
    public void solveMaze() {
        if (maze == null) {
            logger.warn("Cannot solve maze because maze is null.");
            return;
        }

        logger.info("Requesting maze solution from server.");

        try {
            startServers();

            Solution serverSolution = requestSolutionFromServer(maze);

            if (serverSolution == null) {
                throw new IllegalStateException("Maze solving server returned null solution.");
            }

            solution = serverSolution;

            logger.info(
                    "Maze solved successfully by server. solutionLength={}",
                    solution.getSolutionPath().size()
            );

            setChanged();
            notifyObservers("solutionReady");

        } catch (Exception e) {
            logger.error("Failed to solve maze through server.", e);

            setChanged();
            notifyObservers("solutionFailed");
        }
    }

    /**
     * Sends the current maze to the solving server and receives a Solution.
     *
     * @param mazeToSolve maze to solve
     * @return solution returned by the server
     * @throws Exception if communication fails
     */
    private Solution requestSolutionFromServer(Maze mazeToSolve) throws Exception {
        final Solution[] receivedSolution = new Solution[1];
        final Exception[] clientException = new Exception[1];

        Client client = new Client(
                InetAddress.getLocalHost(),
                MAZE_SOLVING_SERVER_PORT,
                new IClientStrategy() {
                    @Override
                    public void clientStrategy(InputStream inFromServer, java.io.OutputStream outToServer) {
                        try {
                            ObjectOutputStream toServer = new ObjectOutputStream(outToServer);
                            toServer.flush();

                            ObjectInputStream fromServer = new ObjectInputStream(inFromServer);

                            logger.debug("Sending maze object to solving server.");

                            toServer.writeObject(mazeToSolve);
                            toServer.flush();

                            receivedSolution[0] = (Solution) fromServer.readObject();

                            logger.debug("Received solution object from solving server.");

                        } catch (Exception e) {
                            clientException[0] = e;
                            logger.error("Error while communicating with maze solving server.", e);
                        }
                    }
                }
        );

        client.communicateWithServer();

        if (clientException[0] != null) {
            throw clientException[0];
        }

        return receivedSolution[0];
    }

    /**
     * Checks if a movement is legal.
     * For regular movement, the target cell must be inside the maze and not a wall.
     * For diagonal movement, the target cell must be open and at least one L-shaped path
     * to the target must also be open.
     *
     * @param row target row index
     * @param column target column index
     * @param rowChange row movement
     * @param columnChange column movement
     * @return true if the movement is legal
     */
    private boolean isMoveLegal(int row, int column, int rowChange, int columnChange) {
        if (!isOpenCell(row, column)) {
            return false;
        }

        boolean isDiagonalMove = rowChange != 0 && columnChange != 0;

        if (!isDiagonalMove) {
            return true;
        }

        int middleRowOption1 = playerRow + rowChange;
        int middleColumnOption1 = playerColumn;

        int middleRowOption2 = playerRow;
        int middleColumnOption2 = playerColumn + columnChange;

        boolean firstLPathIsOpen = isOpenCell(middleRowOption1, middleColumnOption1);
        boolean secondLPathIsOpen = isOpenCell(middleRowOption2, middleColumnOption2);

        boolean diagonalMoveIsLegal = firstLPathIsOpen || secondLPathIsOpen;

        if (!diagonalMoveIsLegal) {
            logger.debug(
                    "Diagonal move blocked by walls. current=({}, {}), target=({}, {}), option1=({}, {}), option2=({}, {})",
                    playerRow,
                    playerColumn,
                    row,
                    column,
                    middleRowOption1,
                    middleColumnOption1,
                    middleRowOption2,
                    middleColumnOption2
            );
        }

        return diagonalMoveIsLegal;
    }

    /**
     * Checks if a cell is inside the maze and is not a wall.
     *
     * @param row row index
     * @param column column index
     * @return true if the cell is inside the maze and open
     */
    private boolean isOpenCell(int row, int column) {
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
        logger.debug("Observer assigned to MyModel: {}", observer.getClass().getSimpleName());
        addObserver(observer);
    }

    /**
     * Stops program resources.
     */
    @Override
    public void stopProgram() {
        logger.info("Stopping model resources and Part B servers.");

        if (mazeGeneratingServer != null) {
            mazeGeneratingServer.stop();
        }

        if (mazeSolvingServer != null) {
            mazeSolvingServer.stop();
        }

        serversStarted = false;
    }

    /**
     * Saves the current maze to a file.
     *
     * @param file file to save to
     * @throws IOException if saving fails
     */
    @Override
    public void saveMaze(File file) throws IOException {
        if (maze == null) {
            logger.warn("Save maze failed because there is no maze to save.");
            throw new IllegalStateException("There is no maze to save.");
        }

        if (file == null) {
            logger.warn("Save maze failed because file is null.");
            throw new IllegalArgumentException("File cannot be null.");
        }

        logger.info("Saving maze to file: {}", file.getAbsolutePath());

        try {
            Files.write(file.toPath(), maze.toByteArray());

            logger.info(
                    "Maze saved successfully. file={}, sizeBytes={}",
                    file.getAbsolutePath(),
                    Files.size(file.toPath())
            );

        } catch (IOException e) {
            logger.error("Failed to save maze to file: {}", file.getAbsolutePath(), e);
            throw e;
        }
    }

    /**
     * Loads a maze from a file.
     *
     * @param file file to load from
     * @throws IOException if loading fails
     */
    @Override
    public void loadMaze(File file) throws IOException {
        if (file == null) {
            logger.warn("Load maze failed because file is null.");
            throw new IllegalArgumentException("File cannot be null.");
        }

        logger.info("Loading maze from file: {}", file.getAbsolutePath());

        try {
            byte[] mazeBytes = Files.readAllBytes(file.toPath());

            maze = new Maze(mazeBytes);
            solution = null;

            playerRow = maze.getStartPosition().getRowIndex();
            playerColumn = maze.getStartPosition().getColumnIndex();

            gameFinished = false;

            logger.info(
                    "Maze loaded successfully. file={}, rows={}, columns={}, start={}, goal={}",
                    file.getAbsolutePath(),
                    maze.getMaze().length,
                    maze.getMaze()[0].length,
                    maze.getStartPosition(),
                    maze.getGoalPosition()
            );

            setChanged();
            notifyObservers("mazeGenerated");

        } catch (IOException e) {
            logger.error("Failed to load maze from file: {}", file.getAbsolutePath(), e);
            throw e;

        } catch (Exception e) {
            logger.error("File was read but could not be converted to a valid maze: {}", file.getAbsolutePath(), e);
            throw new IOException("Invalid maze file.", e);
        }
    }
}
