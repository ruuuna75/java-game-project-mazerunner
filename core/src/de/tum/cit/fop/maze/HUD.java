package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.graphics.Color;

/**
 * The HUD class is responsible for displaying game information overlay.
 * Shows player lives, key collection status, and directional arrow to exit.
 */
public class HUD {

    private final BitmapFont font;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;

    //HUD positioning constants
    private static final float PADDING = 20f;
    private static final float HEART_SIZE = 32f;
    private static final float HEART_SPACING = 10f;
    private static final float KEY_ICON_SIZE = 40f;
    private static final float ARROW_LENGTH = 60f;
    private static final float ARROW_WIDTH = 15f;

    // HUD camera/viewport (screen-space)
    private final OrthographicCamera uiCamera;
    private final Viewport uiViewport;

    // Sprites (from objects.png)
    private Texture objectsTexture;
    private TextureRegion heartFullRegion;
    private TextureRegion heartEmptyRegion;
    private TextureRegion keyRegion;

    // Animation
    private float animationTime = 0f;

    /**
     * Constructor for HUD.
     */
    public HUD(BitmapFont font, SpriteBatch batch) {
        this.font = font;
        this.batch = batch;
        this.shapeRenderer = new ShapeRenderer();

        // Screen-space camera
        this.uiCamera = new OrthographicCamera();
        this.uiViewport = new ScreenViewport(uiCamera);
        this.uiViewport.apply(true);

        loadSprites();

        font.setColor(Color.WHITE);
        font.getData().setScale(1.0f);
    }

    private void loadSprites() {
        System.out.println("Looking for objects.png: " + Gdx.files.internal("objects.png").path());
        System.out.println("Exists? " + Gdx.files.internal("objects.png").exists());
        try {
            objectsTexture = new Texture(Gdx.files.internal("objects.png"));
        } catch (Exception e) {
            System.err.println("Failed to load objects.png: " + e.getMessage());
            objectsTexture = null;
            heartFullRegion = null;
            heartEmptyRegion = null;
            keyRegion = null;
            return;
        }
        int s = 16;

        heartFullRegion  = new TextureRegion(objectsTexture, 3 * s, 0 * s, s, s);
        heartEmptyRegion = new TextureRegion(objectsTexture, 6 * s, 0 * s, s, s);
        keyRegion = new TextureRegion(objectsTexture, 9 * s, 1 * s, s, s);

        System.out.println("HUD sprites loaded successfully from objects.png.");
    }

    /**
     * Renders the complete HUD.
     *
     * @param delta Time since last frame
     * @param playerLives Current player lives
     * @param maxLives Maximum player lives
     * @param hasKey Whether player has collected a key
     * @param playerPos Player position in world coordinates
     * @param exitPos Exit position in world coordinates
     */
    public void render(float delta, int playerLives, int maxLives, boolean hasKey, Vector2 playerPos, Vector2 exitPos) {
        animationTime += delta;

        // Set up for HUD rendering (fixed screen coordinates)
        uiViewport.apply();
        batch.setProjectionMatrix(uiCamera.combined);

        float w = uiViewport.getWorldWidth();
        float h = uiViewport.getWorldHeight();

        batch.begin();

        // Draw lives (hearts) in top-left corner
        renderLives(PADDING, h - PADDING - HEART_SIZE, playerLives, maxLives);

        // Draw key status in top-right corner
        renderKeyStatus(w - PADDING - KEY_ICON_SIZE, h - PADDING - KEY_ICON_SIZE, hasKey);

        // Draw score/timer (optional - can be extended)
        renderGameInfo(PADDING, h - PADDING - HEART_SIZE - 45f);

        batch.end();

        float playerX = playerPos.x;
        float playerY = playerPos.y;
        float exitX = exitPos.x;
        float exitY = exitPos.y;

        renderDirectionalArrowFixedHUD(playerX, playerY, exitX, exitY);
    }

    /**
     * Renders the player's remaining lives as hearts.
     */
    private void renderLives(float x, float y, int currentLives, int maxLives) {
        for (int i = 0; i < maxLives; i++) {
            float heartX = x + i * (HEART_SIZE + HEART_SPACING);

            if (i < currentLives) {
                if (heartFullRegion != null) {
                    batch.draw(heartFullRegion, heartX, y, HEART_SIZE, HEART_SIZE);
                } else {
                    font.draw(batch, "♥", heartX, y + HEART_SIZE);
                }
            } else {
                if (heartEmptyRegion != null) {
                    batch.draw(heartEmptyRegion, heartX, y, HEART_SIZE, HEART_SIZE);
                } else {
                    font.setColor(Color.GRAY);
                    font.draw(batch, "♡", heartX, y + HEART_SIZE);
                    font.setColor(Color.WHITE);
                }
            }
            batch.setColor(Color.WHITE);
            font.setColor(Color.WHITE);
        }
    }

    /**
     * Renders the key collection status.
     */
    private void renderKeyStatus(float x, float y, boolean hasKey) {
        font.draw(batch, "DEBUG KEY", 100, 100);
        if (keyRegion != null) {
            batch.draw(keyRegion, x, y, KEY_ICON_SIZE, KEY_ICON_SIZE);
        } else {
            font.draw(batch, "KEY", x, y + KEY_ICON_SIZE);
        }

        if (hasKey) {
            font.setColor(Color.GREEN);
            font.draw(batch, "✓", x + KEY_ICON_SIZE + 8f, y + KEY_ICON_SIZE - 6f);
        } else {
            font.setColor(Color.RED);
            font.draw(batch, "✗", x + KEY_ICON_SIZE + 8f, y + KEY_ICON_SIZE - 6f);
        }

        batch.setColor(Color.WHITE);
        font.setColor(Color.WHITE);
    }

    private void renderGameInfo(float x, float y) {
        font.setColor(Color.LIGHT_GRAY);
        font.setColor(Color.WHITE);
    }

    /**
     * Renders an arrow pointing from player to exit.
     * This arrow can be attached to the player or shown at screen edge.
     */
    private void renderDirectionalArrowFixedHUD(float playerX, float playerY, float exitX, float exitY) {
        float dx = exitX - playerX;
        float dy = exitY - playerY;

        // same position -> no arrow needed
        if (dx == 0f && dy == 0f) return;

        // 4-direction decision
        float dirX = 0f;
        float dirY = 0f;
        if (Math.abs(dx) >= Math.abs(dy)) {
            dirX = (dx >= 0f) ? 1f : -1f;
        } else {
            dirY = (dy >= 0f) ? 1f : -1f;
        }

        // Arrow anchor on HUD
        float baseX = PADDING + 20f;
        float baseY = PADDING + 20f;

        float tipX = baseX + dirX * ARROW_LENGTH;
        float tipY = baseY + dirY * ARROW_LENGTH;

        float pulse = 1f + 0.15f * (float) Math.sin(animationTime * 4f);
        float thickness = ARROW_WIDTH * pulse;
        float head = 18f * pulse;

        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1f, 1f, 1f, 0.9f);

        shapeRenderer.rectLine(baseX, baseY, tipX, tipY, thickness);

        if (dirX == 1f) {
            shapeRenderer.triangle(tipX, tipY, tipX - head, tipY + head * 0.6f, tipX - head, tipY - head * 0.6f);
        } else if (dirX == -1f) {
            shapeRenderer.triangle(tipX, tipY, tipX + head, tipY + head * 0.6f, tipX + head, tipY - head * 0.6f);
        } else if (dirY == 1f) {
            shapeRenderer.triangle(tipX, tipY, tipX - head * 0.6f, tipY - head,  tipX + head * 0.6f, tipY - head);
        } else {
            shapeRenderer.triangle(tipX, tipY, tipX - head * 0.6f, tipY + head, tipX + head * 0.6f, tipY + head);
        }

        shapeRenderer.end();
    }

    /**
     * Disposes of HUD resources.
     */
    public void dispose() {
        shapeRenderer.dispose();
        if (objectsTexture != null) objectsTexture.dispose();
    }

    /**
     * Updates HUD for window resize.
     */
    public void resize(int width, int height) {
        uiViewport.update(width, height, true);
    }
}
