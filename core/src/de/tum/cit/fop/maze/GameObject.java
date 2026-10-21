package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public abstract class GameObject {
    protected int x;
    protected int y;
    protected TextureRegion region;

    public GameObject(int x, int y, TextureRegion region) {
        this.x = x;
        this.y = y;
        this.region = region;
    }
}
