package de.tum.cit.fop.maze;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import games.spooky.gdx.nativefilechooser.NativeFileChooser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * The MazeRunnerGame class represents the core of the Maze Runner game.
 * It manages the screens and global resources like SpriteBatch and Skin.
 */

// the DesktopLauncher opens the window, this class decides what actually happens inside it.
// Because it extends Game, it gains the ability to switch between different Screen objects
// (like moving from a Menu to the actual Maze).

// things like Textures, SpriteBatches, and Music are stored in GPU memory (Video RAM).
// The Garbage Collector cannot reach there. You must manually call .dispose() on these objects
// to prevent memory leaks.
public class MazeRunnerGame extends Game {

    // Screens
    private MenuScreen menuScreen;
    private GameScreen gameScreen;
    private PauseScreen pauseScreen;
    private GameOverScreen gameOverScreen;
    private VictoryScreen victoryScreen;
    private SettingsScreen settingsScreen;

    private String selectedMapFile = "maps/level-1.properties";


    // skins + sprites  these use a lot of memory, you create them once here and share them with all screens,
    // rather than creating new ones every time you switch rooms.

    // Sprite Batch for rendering
    private SpriteBatch spriteBatch;


    // A "Skin" is a container for UI assets. It holds your fonts, button textures, and slider styles.
    // Loading it once in create() ensures your menus look consistent

    // UI Skin
    private Skin skin;

    // TextureRegion: Think of this as a "window" that looks at a specific part of your image.
    private Animation<TextureRegion> characterDownAnimation;

    // atlas
    private TextureAtlas atlas;

    private GameWorld world;


    /**
     * Constructor for MazeRunnerGame.
     *
     * @param fileChooser The file chooser for the game, typically used in desktop environment.
     */
    public MazeRunnerGame(NativeFileChooser fileChooser) {
        super();
    }


    public GameWorld getWorld() {
        return world;
    }

    /**
     * Called when the game is created. Initializes the SpriteBatch and Skin.
     */
    @Override
    public void create() {
        spriteBatch = new SpriteBatch(); // Create SpriteBatch

        // what is this "craft/craftacular-ui.json"
        skin = new Skin(Gdx.files.internal("craft/craftacular-ui.json")); // Load UI skin
        this.loadCharacterAnimation(); // Load character animation

        // Play some background music
        // Background sound
        Music backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("background.mp3"));
        backgroundMusic.setLooping(true);
        backgroundMusic.play();



        atlas = new TextureAtlas(Gdx.files.internal("atlas/game_tiles.atlas"));
        TextureRegion wallRegion = atlas.findRegion("wall");
        TextureRegion entryRegion = atlas.findRegion("entry");
        TextureRegion exitRegion = atlas.findRegion("exit");
        TextureRegion trapRegion = atlas.findRegion("trap");
        TextureRegion substanceRegion = atlas.findRegion("substance");

        // initialize a staticMaze object,then a GameWorld object,
        // load the map of level 4 (Runa u can use this for choosing the levels in MenuScreen)
        staticMaze maze = new staticMaze(selectedMapFile, wallRegion, entryRegion,
                exitRegion, trapRegion, substanceRegion);
        world = new GameWorld(maze, atlas);

        reloadWorld();

        goToMenu(); // Navigate to the menu screen

    }

    /**
     * Switches to the menu screen.
     */
    public void goToMenu() {

        // setScreen(Screen screen): This is the most important method.
        // When you call this, LibGDX stops calling the render() method on the old
        // screen and starts calling it on the new one
        if (menuScreen == null) {
            menuScreen = new MenuScreen(this);
        }
        setScreen(menuScreen);


        if (gameScreen != null) {
            gameScreen.dispose(); // Dispose the game screen if it exists
            gameScreen = null;
        }
    }

    /**
     * Switches to the game screen.
     */
    public void goToGame() {
        goToGame(selectedMapFile);
    }

    public void goToGame(String mapFile) {
        this.selectedMapFile = mapFile;

        reloadWorld();

        if (gameScreen != null) {
            gameScreen.dispose();
        }
        gameScreen = new GameScreen(this);
        setScreen(gameScreen);
    }

    /**
     * Switches to the pause screen.
     */
    public void goToPause() {
        if (pauseScreen == null) pauseScreen = new PauseScreen(this);
        setScreen(pauseScreen);
    }

    /**
     * Switches to the game over screen.
     */
    public void goToGameOver() {
        this.setScreen(new GameOverScreen(this));
    }

    /**
     * Switches to the victory screen.
     */
    public void goToVictory() {
        this.setScreen(new VictoryScreen(this));
    }

    /**
     * Switches to the settings screen.
     */
    public void goToSettings() {
        this.setScreen(new SettingsScreen(this));
    }

    public String getSelectedMapFile() {
        return selectedMapFile;
    }

    /**
     * Loads the character animation from the character.png file.
     */

    // loadCharacterAnimation() method demonstrates how to turn a flat image into a moving character.
    private void loadCharacterAnimation() {

        // Texture walkSheet: This is your entire image file.
        Texture walkSheet = new Texture(Gdx.files.internal("character.png"));


        // 16 pixels wide and 32 pixels height
        int frameWidth = 16;
        int frameHeight = 32;

        // frames per seconds
        int animationFrames = 4;

        // libGDX internal Array instead of ArrayList because of performance
        Array<TextureRegion> walkFrames = new Array<>(TextureRegion.class);

        // Add all frames to the animation
        // if you had multiple png for multiple movements ex: left right up down, would you add them here?

        // this loop creates 4 separate "views" of those frames and puts them in an Array.
        for (int col = 0; col < animationFrames; col++) {
            walkFrames.add(new TextureRegion(walkSheet, col * frameWidth, 0, frameWidth, frameHeight));
        }


        // Animation: This object takes the array of frames and a "frame duration" (0.1s).
        // It acts like a timer—when you ask it for a frame at time 0.5s, it calculates that
        // it should show the 5th frame.
        characterDownAnimation = new Animation<>(0.1f, walkFrames);
    }

    private void reloadWorld() {
        TextureRegion wallRegion = atlas.findRegion("wall");
        TextureRegion entryRegion = atlas.findRegion("entry");
        TextureRegion exitRegion = atlas.findRegion("exit");
        TextureRegion trapRegion = atlas.findRegion("trap");
        TextureRegion substanceRegion = atlas.findRegion("substance");

        staticMaze maze = new staticMaze(selectedMapFile, wallRegion, entryRegion, exitRegion, trapRegion, substanceRegion);
        world = new GameWorld(maze, atlas);
    }


    /**
     * Cleans up resources when the game is disposed.
     */
    @Override
    public void dispose() {
        getScreen().hide(); // Hide the current screen
        getScreen().dispose(); // Dispose the current screen
        spriteBatch.dispose(); // Dispose the spriteBatch
        skin.dispose(); // Dispose the skin
        if (atlas != null){
            atlas.dispose();
        }
    }

    // Getter methods
    public Skin getSkin() {
        return skin;
    }

    public Animation<TextureRegion> getCharacterDownAnimation() {
        return characterDownAnimation;
    }

    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }
}
