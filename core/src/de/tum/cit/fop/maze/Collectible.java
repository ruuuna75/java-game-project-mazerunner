package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Collectible extends GameObject{
    protected boolean collected;
    public Collectible(int x, int y, TextureRegion region) {
        super(x, y, region);
    }
}
