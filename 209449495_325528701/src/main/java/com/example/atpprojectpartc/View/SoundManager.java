package com.example.atpprojectpartc.View;
import javafx.animation.PauseTransition;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;

/**
 * SoundManager is responsible for all game sounds.
 *
 * It manages:
 * 1. Intro music - Crazy Frog, played when the app opens / setup screen.
 * 2. Game music - Pacman, played during the maze.
 * 3. Victory music - Gangnam Style, played when the player wins.
 * 4. Loss music - Pacman loss sound, played when exiting the maze.
 */
public class SoundManager {

    private static final Logger logger = LogManager.getLogger(SoundManager.class);

    private MediaPlayer introMusicPlayer;
    private MediaPlayer gameMusicPlayer;
    private MediaPlayer victoryMusicPlayer;
    private MediaPlayer lossMusicPlayer;

    private PauseTransition lossTimer;

    private static final String INTRO_MUSIC_PATH =
            "/com/example/atpprojectpartc/View/Sound/crazy_frog.mp3";

    private static final String GAME_MUSIC_PATH =
            "/com/example/atpprojectpartc/View/Sound/Pacman_theme.mp3";

    private static final String VICTORY_MUSIC_PATH =
            "/com/example/atpprojectpartc/View/Sound/Gangnam_style.mp3";

    private static final String LOSS_MUSIC_PATH =
            "/com/example/atpprojectpartc/View/Sound/pacman_loss.mp3";

    /**
     * Crazy Frog starts from second 7.
     */
    private static final double INTRO_START_SECONDS = 7.0;

    /**
     * Pacman maze music starts from second 2.
     */
    private static final double GAME_START_SECONDS = 2.0;

    /**
     * Gangnam Style starts from 1 minute and 10 seconds.
     */
    private static final double VICTORY_START_SECONDS = 70.0;

    /**
     * Pacman loss sound starts at 00:01.
     */
    private static final double LOSS_START_SECONDS = 1.0;

    /**
     * Pacman loss sound stops at 00:03.
     */
    private static final double LOSS_END_SECONDS = 3.0;

    /**
     * Plays the intro music in a loop.
     * This music plays on the start/setup screens.
     */
    public void playIntroMusic() {
        logger.info("Starting intro music.");

        stopGameMusic();
        stopVictoryMusic();
        stopLossMusic();

        stopAndDisposeIntroMusic();

        introMusicPlayer = createMediaPlayer(INTRO_MUSIC_PATH);

        if (introMusicPlayer == null) {
            logger.warn("Intro music could not be started because MediaPlayer is null.");
            return;
        }

        introMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        introMusicPlayer.setVolume(0.35);

        playFromSecond(introMusicPlayer, INTRO_START_SECONDS);

        logger.info("Intro music started in loop from second {}.", INTRO_START_SECONDS);
    }

    /**
     * Stops the intro music without disposing it.
     */
    public void stopIntroMusic() {
        if (introMusicPlayer != null) {
            logger.debug("Stopping intro music.");
            introMusicPlayer.stop();
        }
    }

    /**
     * Fully stops and disposes intro music.
     */
    private void stopAndDisposeIntroMusic() {
        if (introMusicPlayer != null) {
            logger.debug("Stopping and disposing intro music.");
            introMusicPlayer.stop();
            introMusicPlayer.dispose();
            introMusicPlayer = null;
        }
    }

    /**
     * Plays the regular Pacman game music in a loop.
     * This music starts only when the maze screen opens.
     */
    public void playGameMusic() {
        logger.info("Starting game music.");

        stopIntroMusic();
        stopVictoryMusic();
        stopLossMusic();

        stopAndDisposeGameMusic();

        gameMusicPlayer = createMediaPlayer(GAME_MUSIC_PATH);

        if (gameMusicPlayer == null) {
            logger.warn("Game music could not be started because MediaPlayer is null.");
            return;
        }

        gameMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        gameMusicPlayer.setVolume(0.35);

        playFromSecond(gameMusicPlayer, GAME_START_SECONDS);

        logger.info("Game music started in loop from second {}.", GAME_START_SECONDS);
    }

    /**
     * Stops the regular game music.
     */
    public void stopGameMusic() {
        if (gameMusicPlayer != null) {
            logger.debug("Stopping game music.");
            gameMusicPlayer.stop();
        }
    }

    /**
     * Fully stops and disposes game music.
     */
    private void stopAndDisposeGameMusic() {
        if (gameMusicPlayer != null) {
            logger.debug("Stopping and disposing game music.");
            gameMusicPlayer.stop();
            gameMusicPlayer.dispose();
            gameMusicPlayer = null;
        }
    }

    /**
     * Plays the victory music from second 70.
     */
    public void playVictoryMusic() {
        logger.info("Starting victory music.");

        stopIntroMusic();
        stopGameMusic();
        stopLossMusic();

        stopVictoryMusic();

        victoryMusicPlayer = createMediaPlayer(VICTORY_MUSIC_PATH);

        if (victoryMusicPlayer == null) {
            logger.warn("Victory music could not be started because MediaPlayer is null.");
            return;
        }

        victoryMusicPlayer.setCycleCount(1);
        victoryMusicPlayer.setVolume(0.65);

        playFromSecond(victoryMusicPlayer, VICTORY_START_SECONDS);

        logger.info("Victory music started from second {}.", VICTORY_START_SECONDS);
    }

    /**
     * Stops the victory music.
     */
    public void stopVictoryMusic() {
        if (victoryMusicPlayer != null) {
            logger.debug("Stopping and disposing victory music.");
            victoryMusicPlayer.stop();
            victoryMusicPlayer.dispose();
            victoryMusicPlayer = null;
        }
    }

    /**
     * Plays the loss sound from 00:01 to 00:03,
     * and after that starts the intro music again.
     */
    public void playLossThenIntroMusic() {
        logger.info("Starting loss sound and then intro music.");

        stopIntroMusic();
        stopGameMusic();
        stopVictoryMusic();
        stopLossMusic();

        lossMusicPlayer = createMediaPlayer(LOSS_MUSIC_PATH);

        if (lossMusicPlayer == null) {
            logger.warn("Loss sound could not be started. Returning to intro music.");
            playIntroMusic();
            return;
        }

        lossMusicPlayer.setCycleCount(1);
        lossMusicPlayer.setVolume(0.70);

        Duration startTime = Duration.seconds(LOSS_START_SECONDS);
        Duration lossDuration = Duration.seconds(LOSS_END_SECONDS - LOSS_START_SECONDS);

        lossMusicPlayer.setOnReady(() -> {
            logger.info(
                    "Loss sound is ready. Playing from second {} to second {}.",
                    LOSS_START_SECONDS,
                    LOSS_END_SECONDS
            );

            lossMusicPlayer.seek(startTime);
            lossMusicPlayer.play();

            lossTimer = new PauseTransition(lossDuration);
            lossTimer.setOnFinished(event -> {
                logger.info("Loss sound finished. Returning to intro music.");
                stopLossMusic();
                playIntroMusic();
            });

            lossTimer.play();
        });
    }

    /**
     * Stops the loss music.
     */
    public void stopLossMusic() {
        if (lossTimer != null) {
            logger.debug("Stopping loss timer.");
            lossTimer.stop();
            lossTimer = null;
        }

        if (lossMusicPlayer != null) {
            logger.debug("Stopping and disposing loss music.");
            lossMusicPlayer.stop();
            lossMusicPlayer.dispose();
            lossMusicPlayer = null;
        }
    }

    /**
     * Stops all music.
     */
    public void stopAllMusic() {
        logger.info("Stopping all music.");

        stopAndDisposeIntroMusic();
        stopAndDisposeGameMusic();
        stopVictoryMusic();
        stopLossMusic();
    }

    /**
     * Plays a MediaPlayer from a specific second.
     *
     * @param mediaPlayer player to play
     * @param startSecond second to start from
     */
    private void playFromSecond(MediaPlayer mediaPlayer, double startSecond) {
        if (mediaPlayer == null) {
            logger.warn("Cannot play media from second {} because MediaPlayer is null.", startSecond);
            return;
        }

        Duration startTime = Duration.seconds(startSecond);

        mediaPlayer.setStartTime(startTime);

        mediaPlayer.setOnReady(() -> {
            logger.debug("MediaPlayer is ready. Starting playback from second {}.", startSecond);
            mediaPlayer.seek(startTime);
            mediaPlayer.play();
        });

        mediaPlayer.setOnError(() -> logger.error(
                "MediaPlayer error occurred: {}",
                mediaPlayer.getError() != null ? mediaPlayer.getError().getMessage() : "Unknown media error"
        ));
    }

    /**
     * Creates a MediaPlayer from a file inside the resources folder.
     *
     * @param resourcePath path to the sound file
     * @return MediaPlayer if the file exists, otherwise null
     */
    private MediaPlayer createMediaPlayer(String resourcePath) {
        try {
            logger.debug("Loading sound resource: {}", resourcePath);

            URL soundResource = getClass().getResource(resourcePath);

            if (soundResource == null) {
                logger.error("Sound file not found: {}", resourcePath);
                return null;
            }

            Media media = new Media(soundResource.toExternalForm());
            MediaPlayer mediaPlayer = new MediaPlayer(media);

            mediaPlayer.setOnError(() -> logger.error(
                    "Could not play sound resource {}. Error: {}",
                    resourcePath,
                    mediaPlayer.getError() != null ? mediaPlayer.getError().getMessage() : "Unknown media error"
            ));

            logger.debug("Sound resource loaded successfully: {}", resourcePath);

            return mediaPlayer;

        } catch (Exception e) {
            logger.error("Could not load sound: {}", resourcePath, e);
            return null;
        }
    }
}
