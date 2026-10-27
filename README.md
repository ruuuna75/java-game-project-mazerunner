# The-initials



# Agent in Rouge (Maze Game)



You are **Agent Rouge**. Your mission (should you accept it) is to recover **5 stolen documents** containing classified information.

Your colleagues are counting on you — can you save the network?



---



## Gameplay Overview



### Objective

- Main objective: Collect **all 5 documents**.

- Level objective: Find the **key** and reach the **exit** to clear the level.



### Highlights

- Hand-crafted animations

- HUD with live status + exit direction arrow

- Skill Tree upgrades (diamond currency)

- Developer Console for runtime "cheats."



---



## How to Run
>In order for the achievement system to work, you need to delete the save_achievements.json and the save_game.json from the root folder.


### Requirements

- **Java:** 17+

- **Build tool:** Gradle



---



## Project Structure

🏗️ Code Structure

📦 Package Organization

de.tum.cit.fop.maze/



├── Player.java

├── Entity

├── Enemy.java

├── EnemyState.java



├── Screen (the classes below inherit from from Screen)

    ├── GameScreen.java

    ├── EndlessScreen.java

    ├── GameOverScreen.java

    ├── IntroCutScene.java

    ├── MenuScreen.java

    ├── PauseScreen.java

    ├── SettingsScreen.java

    ├── DeveloperConsole.java

    ├── VictoryScreen.java



├── GameObject.java (the class below inherit from GameObject)

    ├── Collectible.java (the classes below inherit Collectible)

        ├── Diamond.java

        ├── Heart.java

        ├── Key.java

        ├── Shield.java

        ├── Wand.java

        ├── Heel.java

        ├── Document.java



├── Tiles.java (the classes below inherit from Tiles)

    ├── Wall.java

    ├── Entry.java

    ├── Exit.java

    ├── Trap.java

    ├── Substance.java

    ├── Floor.java



├── StaticMaze.java

├── MazeRunnerGame.java

├── GameSaveData.java

├── SkillTree.java

├── SoundManager.java

├── GameSaveData.java

├── Achievement.java

├── AchievementManager.java

├── Toast.java

├── Score.java

├── SaveManager.java

├── GameEvent.java

├── PlayerSaveData.java

├── LevelSaveData.java

├── LeaderboardManager.java

├── LevelSaveData.java

├── Constants.java

├── Direction.java

└── HUD.java



### Folder / Package Layout

- .gradle

- .idea

- assets

- core

- core/build

- core/src

- desktop

- maps

- settings



### Controls



- Move Up: Arrow Up

- Move Down: Arrow Down

- Move Left: Arrow Left

- Move Right: Arrow Right

- Run: Left Shift



Notes:

- Controls can be changed via the `SettingsScreen`.

- If no configuration file exists, default bindings are used.



### Extra Controls

- Zoom: I (in), O (out)



## Important Classes explained



#### Player (Player.java)

- Fields and logic for:

    - hasKey

    - lives/maxLives

    - invincibleTime (damage cooldown)

    - freezeTime (substance freeze mechanic)

    - shieldCount

    - base speed + running speed

    - animation



### Screens and Game Flow



#### MenuScreen



- Select Level 1–5

- Shows cleared levels with “CLEARED!”

- Opens Settings

- “Load a new game” (resets save data and keybindings)

- Displays recent scores via LeaderboardManager



#### IntroCutScene



- Plays for Level 1 before gameplay starts, explains the story

- Click to advance slides

- Transitions into Level 1 automatically after the last slide



#### GameScreen



- Core gameplay update + rendering loop

- Player movement + collision

- Collectibles pickup + effects

- Enemy update + rendering

- HUD overlay (lives, key status, diamonds, shields, exit arrow)

- Handles transitions:

    - Pause (ESC)

    - Victory (exit with key)

    - GameOver (lives reach 0)



#### PauseScreen



- Scene2D overlay with:

    - Continue (resumes gameplay)

    - Settings

    - Exit (saves diamonds + quits)



#### VictoryScreen



- Shown when the player reaches the exit successfully

- Marks level complete + saves progress

- Returns to menu



#### GameOverScreen



- Shown when lives reach zero

- Options:

    - Return to menu (reset run stats)

    - Exit application



#### SettingsScreen



- Remap keys (Move + Run)

- Saves to settings/keybindings.json

- Includes navigation to:

    - Developer Console

    - Skill Tree



#### SkillTree



- Permanent upgrades purchased with diamonds

- Purchases are saved

- Skills:

    - playerSpeed = 7f

    - Gain Life +1

    - Gain Life +2

    - Add Shield +1

    - Add Shield +2

- Prerequisites apply



#### DeveloperConsole



- Runtime adjustments:

    - Max lives

    - Player speed

    - Running speed

- Applied when entering gameplay



#### HUD



- The HUD displays:

    - Current Lives with heart icons

    - Key status (collected / missing)

    - Diamond count + icon

    - Shield count + icon

    - Direction arrow pointing from player → exit





#### Game Rules and Mechanics

- Movement

    - Smooth 4-direction movement

    - Running when Shift is held

    - Camera follows the player (player-centered)



- Collision System



#### Walls



- Player cannot pass through walls

- Boundary checks prevent leaving the map

- Player hitbox is slightly smaller than a full tile for smoother movement



#### Exit



- Exit behaves like blocked until the key is collected

- If player has key and enters exit → Victory flow



#### Trap / Hazard



- Trap tile deals damage (lose 1 life)

- Damage cooldown (temporary invincibility)

- Shield blocks damage if shieldCount > 0



#### Substance (Freeze Trap) - special obstacle



- Touching Substance freezes player movement, and a cage falls on top of him for a duration

- Freeze timer decreases over time, and the player escapes

- Input ignored while frozen



#### Collectibles



- Collected using proximity collision (~0.5 tiles):

    - Diamond: increases total diamonds (currency + achievements)

    - Document: mission progress item (objective + achievements)

    - Key: unlocks exit

    - Heart: restores 1 life (up to max)

    - Shield: increases shieldCount

    - Wand: temporary invincibility

    - Heel: speed boost



### Saving and Loading



Save system uses SaveManager.save(...) and SaveManager.load().



#### Data Layers



- Global (GameSaveData)

    - Total diamonds

    - Completed levels

    - Player stats (lives, maxLives, shields)

    - Skill Tree purchase flags



- Per-level (LevelSaveData)

    - Player position (x/y)

    - Player lives

    - Key status

    - Document collected status

    - Diamonds collected flags per level



#### Saving (saveGame)



- Persists player stats from active GameScreen

- Stores current level index + completed levels

- Ensures level array exists (correct length)

- Exports the current level state and merges collectible flags safely



#### Loading (loadGame)



- Loads save data, selects the correct map file

- Rebuilds world + enters the level normally

- Restores per-level state + player stats if available



#### Level Progress / Clearing

- `onLevelCleared()`

    - determines current level index

    - markLevelComplete(index)

    - saveGame() so completion persists before the Victory screen



- `markLevelComplete(index)`

    - updates completedLevels[index] = true

    - triggers achievements event “LEVEL_COMPLETED.”

    - persists save



#### World / Level Loading

- `selectedMapFile` determines active level map (`maps/level-1.properties` … `level-5.properties`)

- `reloadWorld()` rebuilds StaticMaze + GameWorld for selected map

- `getCurrentLevelIndex()` maps selectedMapFile → 0..4 index

- `currentMapPath(levelIndex)` returns the file path for a given index





#### StaticMaze / Tile Collision Flags (Map Loading Support)

- Adjusted which tiles are collideble:

    - Entry: non-blocking (player spawns there)

    - Trap: walkable but damaging

    - Substance: non-walkable tile that triggers freeze

    - Floor: walkable



---



## UML Class Diagram

![UML Diagram](UML%20Diagram%20FoP.svg)

---

## Added Mechanics



#### Animation Integration

- Hand-made sprites and animation atlases

- Exception: the wand and the heart animation

- The story is also hand-drawn using Procreate



#### HUD Integration

- HUD is rendered after the world render pass, always on top

- ExitPosition is updated by scanning the tiles list for the Exit tile each frame

- HUD receives: currentLives, maxLives, hasKey, totalDiamonds, shieldCount, playerPosition, exitPosition



#### Skill Tree / Developer Console Integration

- On `GameScreen` construction and on `show()`:

    - `applySkillUpgradesFromSave(...)` loads permanent upgrades (speed/max lives/shields) from save data

    - `applyDevOverridesIfEnabled()` applies DeveloperConsole overrides if enabled

- Developer settings override skill settings when enabled





#### Configuration / Custom Levels



- Level files are stored: src/maps

- File format: <.txt>

- How to add a new level: <download a file of type x,y=id, where id is one of the cases inside StaticMaze.>





Known Issues / Limitations



< Biggest issues we had were with the collision and git. Since our team consisted of beginner GitHub users,

It was hard to help navigate the merge conflicts. The more experienced people with git helped make the merge conflicts more manageable.

The collision wasn't working because of a mix-up: the character was in tile-based coordinates, but the collision was using pixel-based coordinates. To solve this issue, we decided on

one coordinate system, namely the tiles (floats), because we thought it was more appropriate. >



<Another smaller issue was the lack of communication at first, but we managed to solve it by scheduling meetings in between the deadlines, where everyone would show what they did that week.>



Authors

>Ileana-Maria Alexandru, Runa Shirata, Zehra Aynur Özpinar, Ngoc Nguyen Ha Ngo
