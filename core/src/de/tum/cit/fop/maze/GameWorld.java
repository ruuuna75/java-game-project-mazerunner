package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;

// use this to load a new level (or reload the level if the Player dies)
// owns dynamic entities

public class GameWorld {
    private staticMaze maze;
    // private Player player;
    // private List<Enemy> enemies
    // private List<Obstacle> obstacles
    // private List<Collectible> collectibles
    // private GameState state;

    // Constructor
    public GameWorld(staticMaze maze, TextureAtlas atlas){
        this.maze = maze;
        // implement the same for Player, and the lists: enemies, obstacles, collectibles
    }

    // Getter for the maze

    public staticMaze getMaze() {
        return maze;
    }
}
