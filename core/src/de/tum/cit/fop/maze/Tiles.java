package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import static de.tum.cit.fop.maze.Constants.TILE_SIZE;

public abstract class Tiles {
    // protected or private?
    protected int x;
    protected int y;
    protected boolean collision;
    protected TextureRegion region;

    //Constructor
    public Tiles(int x, int y, boolean collision, TextureRegion region) {
        this.x = x;
        this.y = y;
        this.collision = collision;
        this.region = region;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void draw (SpriteBatch theBatch){
        theBatch.draw(region, x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        // region, x-coor, y-coor, width, height
    }
}
