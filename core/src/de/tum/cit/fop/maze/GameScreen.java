package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;


/**
 * The GameScreen class is responsible for rendering the gameplay screen.
 * It handles the game logic and rendering of the game elements.
 */

// where the specific "level" logic lives
public class GameScreen implements Screen {

    // Tile logic for grid its easier
    private static final int TILE_SIZE = 16; // each tile is 16 pixels
    private static final int MAP_WIDTH_TILES = 30;
    private static final int MAP_HEIGHT_TILES = 60;
    private static final float TILES_IN_VIEW = 2500f;

    // Tile logic for player
    private static final int PLAYER_WIDTH_TILES = 1;
    private static final int PLAYER_HEIGHT_TILES = 2;


    // Map in pixels
    private final float WORLD_WIDTH = 30 * 16; // How many pixels wide you want to see
    private final float WORLD_HEIGHT = 60 * 16; // How many pixels high


    private final MazeRunnerGame game;
    private final OrthographicCamera camera;
    private final com.badlogic.gdx.utils.viewport.Viewport viewport;
    private final HUD hud;


    // Game state tracking (these should eventually come from Player/GameWorld)
    private int playerLives = 3;
    private int maxLives = 5;
    private boolean hasKey = false;
    private Vector2 playerPosition = new Vector2(100, 100);
    private Vector2 exitPosition = new Vector2(500, 500);

    // add the players coordinates
    private float playerX, playerY;
    private float playerSpeed = 5f;

    private float runningSpeed = 8f; // pixels per second

    //Player state
    private int lives = 3;
    private float invincibleTime = 0f; //cooldown timer for damage
    private float damageCooldown = 2f; //2 seconds of invincibility after damag


    private float currentZoom = 0.2f;

    // A BitmapFont is an image that contains all the letters of the alphabet
    private final BitmapFont font;

    // only used for running in circle or has another purpose - no
    private float sinusInput = 0f;

    /**
     * Constructor for GameScreen. Sets up the camera and font.
     *
     * @param game The main game class, used to access global resources and methods.
     */
    public GameScreen(MazeRunnerGame game) {
        this.game = game;

        // Create and configure the camera for the game view
        camera = new OrthographicCamera();

        viewport = new ExtendViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.setToOrtho(false, WORLD_WIDTH, WORLD_HEIGHT);
        camera.zoom = currentZoom;

        // Get the font from the game's skin
        font = game.getSkin().getFont("font");

        // Initialize the HUD
        hud = new HUD(font, game.getSpriteBatch());

        // Logic to find the players starting point - entry
        for (Tiles tile: game.getWorld().getMaze().getTilesList()) {
            if (tile instanceof Entry) {
                this.playerX = tile.getX() + 2;
                this.playerY = tile.getY();
                break;
            }
        }
    }


    // Screen interface methods with necessary functionality
    @Override
    public void render(float delta) {
        // Check for escape key press to go back to the menu
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.goToPause();
            return;
        }

        // Player Movement
        float speed = playerSpeed;

        //Check if shift is pressed for running
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
            speed = runningSpeed;
        }

        //Store old postitons for collision checking
        float oldX = playerX;
        float oldY = playerY;

        //Handle movement input
        if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) {
            playerY += speed * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
            playerY -= speed * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            playerX -= speed * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            playerX += speed * delta;
        }

        //Collision detection
        // check wall collision
        if (isWall(playerX, playerY)) {
            //Revert to old position if hit a wall
            playerX = oldX;
            playerY = oldY;
        }

        //Check trap/ enemy collision (only if not invincible)
        if (invincibleTime <= 0) {
            if (isTrapOrEnemy(playerX, playerY)) {
                lives--;
                invincibleTime = damageCooldown; // Start invincibility timer
                System.out.println("Hit! Lives remaining:  " + lives);

                if (lives <= 0) {
                    game.goToGameOver();
                }
            }
        }

        // Update invincible timer
        if (invincibleTime > 0) {
            invincibleTime -= delta;
        }

        //Check key collision
        if (!hasKey && isKey(playerX, playerY)) {
            hasKey = true;
            System.out.println("Key collected!");
        }

        //Check exit collision(only works if player has key)
        if (hasKey && isExit(playerX, playerY)) {
            game.goToVictory();
        }

//        // player movements
//        if (Gdx.input.isKeyPressed(Input.Keys.W)) playerY += playerSpeed * delta;
//        if (Gdx.input.isKeyPressed(Input.Keys.S)) playerY -= playerSpeed * delta;
//        if (Gdx.input.isKeyPressed(Input.Keys.A)) playerX -= playerSpeed * delta;
//        if (Gdx.input.isKeyPressed(Input.Keys.D)) playerX += playerSpeed * delta;

        // camera panning
        // 2. Zoom Logic (U to zoom in, I to zoom out)
        if (Gdx.input.isKeyPressed(Input.Keys.U)) camera.zoom -= 0.5f * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.I)) camera.zoom += 0.5f * delta;

        ScreenUtils.clear(0, 0, 0, 1); // Clear the screen

        camera.position.set(playerX * 16, playerY * 16, 0);
        camera.update(); // Update the camera

        // Move text in a circular path to have an example of a moving object
        sinusInput += delta;
        // delta is the time (in seconds) passed since the last frame.
        // Adding this ensures that the movement speed is the same on a slow computer and a fast gaming PC
        float textX = (float) (camera.position.x + Math.sin(sinusInput) * 100);
        float textY = (float) (camera.position.y + Math.cos(sinusInput) * 100);

        // Set up and begin drawing with the sprite batch

        // batch.setProjectionMatrix(...): This is crucial. It tells the SpriteBatch:
        // "Don't just draw relative to the window pixels;
        // draw relative to where my camera is looking."
        game.getSpriteBatch().setProjectionMatrix(camera.combined);
        // camera.combined: This is a mathematical matrix. It combines the camera's position and its zoom.

        game.getSpriteBatch().begin(); // Important to call this before drawing anything

        // Render the text
        font.draw(game.getSpriteBatch(), "Press ESC to go to menu", textX, textY);


        // draw the List<Tiles> of tiles
        for (Tiles i : game.getWorld().getMaze().getTilesList()){
            i.draw(game.getSpriteBatch());
        }

        // Draw the character next to the text :) / We can reuse sinusInput here
        game.getSpriteBatch().draw(
                game.getCharacterDownAnimation().getKeyFrame(sinusInput, true),
                playerX * 16,
                playerY * 16,
                16,
                32
        );




        game.getSpriteBatch().end(); // Important to call this after drawing everything

        // Render the HUD (always rendered last, on top of everything)
        // TODO: Replace these with actual values from Player and GameWorld
        hud.render(
                delta,
                playerLives,
                maxLives,
                hasKey,
                playerPosition,
                exitPosition
        );

        updateTestControls();

    }

    /**
     * Temporary method for testing HUD functionality.
     * Remove this once Player and GameWorld are properly integrated.
     */
    private void updateTestControls() {
        // Press K to toggle key collection (for testing)
        if (Gdx.input.isKeyJustPressed(Input.Keys.K)) {
            hasKey = !hasKey;
        }

        // Press L to lose a life (for testing)
        if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            playerLives = Math.max(0, playerLives - 1);
        }

        // Press H to gain a life (for testing)
        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            playerLives = Math.min(maxLives, playerLives + 1);
        }
    }


        // resize(): This is called if the user drags the window corner.
    // You should update the camera here so the game doesn't look stretched.
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        camera.update();
        hud.resize(width, height);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    // show(): This is called the moment the screen becomes active.
    // Use this to reset the player's position or start level music.
    @Override
    public void show() {

    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
    }

    // Additional methods and logic can be added as needed for the game screen
    /**
     * Check if position is a wall
     * TODO: Replace with actual maze grid check from prev code
     */

    private int playerLeftTile() {return (int) playerX;}
    private int playerBottomTile() {return (int) playerY;}
    private int playerRightTile() {return playerLeftTile();} // width 1 tile
    private int playerCollisionTopTile() {
        return playerBottomTile(); // ONLY bottom tile collides
    }

    private boolean isWall(float x, float y) {
        // Placeholder: simple boundary check

        // conversion from float (pixels) to tiles (ints)
        float left = x;
        float bottom = y;

        float right = left + 1 - (float)(1/16);
        float top = bottom + 1 - (float)(1/16);

        // Simulate walls at screen edges
        if (left < 0 || right >= MAP_WIDTH_TILES
                || bottom < 0 || top >= MAP_HEIGHT_TILES) {
            return true; // Hit boundary wall
        }

        return intersectsWall(left, bottom, right, top);
    }

    private boolean intersectsWall(float left, float bottom, float right, float top) {
        for (Tiles tile : game.getWorld().getMaze().getTilesList()) {

            if (!(tile instanceof Wall)) continue;

            int tx = tile.getX();
            int ty = tile.getY();

            if (((tx < left && tx + 1 - 1/16 > left) || (tx < right && tx + 1 - 1/16 > right)) &&
                    ((ty < bottom && ty + 1 - 1/16 > bottom) || (ty < top && ty + 1 - 1/16 > top))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if position is a trap or enemy
     * TODO: Replace with actual maze grid check
     */
    private boolean isTrapOrEnemy(float x, float y) {
//        // Placeholder: fake trap at position (400,300)
//        float distance = (float) Math.sqrt(Math.pow(x - 400, 2) + Math.pow(y - 300, 2));
//        return distance < 32; // within 32 pixels = collision

        // tx -> X coordinate (column), playerBottomTile -> Y coordinate (row)
        int trapX = 25;
        int trapY = 18;

        // if player position matches the traps position then it will return true
        for (int tx = playerLeftTile(); tx <= playerRightTile(); tx++) {
            if (playerBottomTile() == trapY && tx == trapX) {
                return true;
            }

        }
        return false;
    }

    /**
     * check if position has the key
     * TODO: Replace with actual maze grid check
     */
    private boolean isKey(float x, float y) {
        // Placeholder: key at position (200, 200)
//        float distance = (float) Math.sqrt(Math.pow(x - 200, 2) + Math.pow(y - 200, 2));
//        return distance < 32;

        int keyX = 12;
        int keyY = 12;

        // if player position matches the keys position then it will return true
        for (int tx = playerLeftTile(); tx <= playerRightTile(); tx++) {
                if (tx == keyX && playerBottomTile() == keyY) {
                    return true;
                }
            }

        return false;
    }

    /**
     * check if position is the exit
     * TODO: Replace with actual maze grid check
     */
    private boolean isExit(float x, float y) {
//        // Placeholder: exit at position (600, 500)
//        float distance = (float) Math.sqrt(Math.pow(x - 600, 2) + Math.pow(y - 500, 2));
//        return distance < 32;

        int exitX = 37;
        int exitY = 31;

        // if player position matches the exits position then it will return true
        for (int tx = playerLeftTile(); tx <= playerRightTile(); tx++) {
            if (tx == exitX && playerBottomTile() == exitY) {
                return true;
            }

        }
        return false;
    }

    // Getter methods if other classes need player  state
    public float getPlayerX() {
        return playerX;
    }

    public float getPlayerY() {
        return playerY;
    }

    public int getLives() {
        return lives;
    }

    public boolean hasKey() {
        return hasKey;
    }
}
