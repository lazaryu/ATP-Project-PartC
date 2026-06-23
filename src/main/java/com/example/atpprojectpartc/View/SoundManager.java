package com.example.atpprojectpartc.View;

import javafx.animation.PauseTransition;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

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
        stopGameMusic();
        stopVictoryMusic();
        stopLossMusic();

        stopAndDisposeIntroMusic();

        introMusicPlayer = createMediaPlayer(INTRO_MUSIC_PATH);

        if (introMusicPlayer == null) {
            return;
        }

        introMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        introMusicPlayer.setVolume(0.35);

        playFromSecond(introMusicPlayer, INTRO_START_SECONDS);
    }

    /**
     * Stops the intro music without disposing it.
     */
    public void stopIntroMusic() {
        if (introMusicPlayer != null) {
            introMusicPlayer.stop();
        }
    }

    /**
     * Fully stops and disposes intro music.
     */
    private void stopAndDisposeIntroMusic() {
        if (introMusicPlayer != null) {
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
        stopIntroMusic();
        stopVictoryMusic();
        stopLossMusic();

        stopAndDisposeGameMusic();

        gameMusicPlayer = createMediaPlayer(GAME_MUSIC_PATH);

        if (gameMusicPlayer == null) {
            return;
        }

        gameMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        gameMusicPlayer.setVolume(0.35);

        playFromSecond(gameMusicPlayer, GAME_START_SECONDS);
    }

    /**
     * Stops the regular game music.
     */
    public void stopGameMusic() {
        if (gameMusicPlayer != null) {
            gameMusicPlayer.stop();
        }
    }

    /**
     * Fully stops and disposes game music.
     */
    private void stopAndDisposeGameMusic() {
        if (gameMusicPlayer != null) {
            gameMusicPlayer.stop();
            gameMusicPlayer.dispose();
            gameMusicPlayer = null;
        }
    }

    /**
     * Plays the victory music from second 70.
     */
    public void playVictoryMusic() {
        stopIntroMusic();
        stopGameMusic();
        stopLossMusic();

        stopVictoryMusic();

        victoryMusicPlayer = createMediaPlayer(VICTORY_MUSIC_PATH);

        if (victoryMusicPlayer == null) {
            return;
        }

        victoryMusicPlayer.setCycleCount(1);
        victoryMusicPlayer.setVolume(0.65);

        playFromSecond(victoryMusicPlayer, VICTORY_START_SECONDS);
    }

    /**
     * Stops the victory music.
     */
    public void stopVictoryMusic() {
        if (victoryMusicPlayer != null) {
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
        stopIntroMusic();
        stopGameMusic();
        stopVictoryMusic();
        stopLossMusic();

        lossMusicPlayer = createMediaPlayer(LOSS_MUSIC_PATH);

        if (lossMusicPlayer == null) {
            playIntroMusic();
            return;
        }

        lossMusicPlayer.setCycleCount(1);
        lossMusicPlayer.setVolume(0.70);

        Duration startTime = Duration.seconds(LOSS_START_SECONDS);
        Duration lossDuration = Duration.seconds(LOSS_END_SECONDS - LOSS_START_SECONDS);

        lossMusicPlayer.setOnReady(() -> {
            lossMusicPlayer.seek(startTime);
            lossMusicPlayer.play();

            lossTimer = new PauseTransition(lossDuration);
            lossTimer.setOnFinished(event -> {
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
            lossTimer.stop();
            lossTimer = null;
        }

        if (lossMusicPlayer != null) {
            lossMusicPlayer.stop();
            lossMusicPlayer.dispose();
            lossMusicPlayer = null;
        }
    }

    /**
     * Stops all music.
     */
    public void stopAllMusic() {
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
        Duration startTime = Duration.seconds(startSecond);

        mediaPlayer.setStartTime(startTime);

        mediaPlayer.setOnReady(() -> {
            mediaPlayer.seek(startTime);
            mediaPlayer.play();
        });
    }

    /**
     * Creates a MediaPlayer from a file inside the resources folder.
     *
     * @param resourcePath path to the sound file
     * @return MediaPlayer if the file exists, otherwise null
     */
    private MediaPlayer createMediaPlayer(String resourcePath) {
        try {
            URL soundResource = getClass().getResource(resourcePath);

            if (soundResource == null) {
                System.err.println("Sound file not found: " + resourcePath);
                return null;
            }

            Media media = new Media(soundResource.toExternalForm());
            return new MediaPlayer(media);

        } catch (Exception e) {
            System.err.println("Could not load sound: " + resourcePath);
            e.printStackTrace();
            return null;
        }
    }
}