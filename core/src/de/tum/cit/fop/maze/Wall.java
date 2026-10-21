package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Wall extends Tiles{
    public Wall(int x, int y, boolean collision, TextureRegion region) {
        super(x, y, collision, region);
    }
    // Wall already has the fields int x, int y, region, collidable
}
