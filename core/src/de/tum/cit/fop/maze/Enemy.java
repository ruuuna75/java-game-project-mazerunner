package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Enemy extends GameObject implements Obstacle{
    public Enemy(int x, int y, TextureRegion region) {
        super(x, y, region);
    }
}
