package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Load the map with static objects (wall, entry, exit, trap)
public class staticMaze {
    private int width;
    private int height;
    private List<Tiles> tilesList;

    // Constructor
    public staticMaze(String filePath,
                      TextureRegion wall,
                      TextureRegion entry,
                      TextureRegion exit,
                      TextureRegion trap,
                      TextureRegion substance){
        Map <String, String> map1 = loadrawMap(filePath);
        this.tilesList = parsedMap(map1, wall, entry, exit, trap, substance);
    }

    // Getters and setters

    public List<Tiles> getTilesList() {
        return tilesList;
    }

    // Methods
    // load the map into a Map<String, String> with the same format
    public Map<String, String> loadrawMap(String filePath){
        Map<String, String> rawMap = new HashMap<>();

        try{
            BufferedReader br = new BufferedReader(Gdx.files.internal(filePath).reader());
            String oneLine;
            while((oneLine = br.readLine()) != null){
                String[] myArray = oneLine.split("=");
                if (myArray.length == 2){
                    // add trim() as well?
                    rawMap.put(myArray[0], myArray[1]);
                }
            }
            br.close();
        }
        catch(IOException e){
            throw new RuntimeException("Cannot load map");
        }
        return rawMap;
    }

    // Create a List of Tiles, like Wall, Exit, Entry (each with its own coordinates)
    // Associate the TextureRegion with it as well
    public List<Tiles> parsedMap(Map <String, String> map,
                                 TextureRegion w,
                                 TextureRegion en,
                                 TextureRegion ex,
                                 TextureRegion tr,
                                 TextureRegion su){
        List<Tiles> tiles1 = new ArrayList<>();

        for (Map.Entry<String, String> entries : map.entrySet()){
            String key = entries.getKey();
            String[] coords = key.split(",");
            int idCell = Integer.parseInt(entries.getValue());
            // Check if length of coords is 2
            int xC = Integer.parseInt(coords[0]);
            int yC = Integer.parseInt(coords[1]);

            // Use a switch expression
            Tiles t = switch (idCell)
            {
                case 0 -> new Wall(xC, yC, true, w);
                case 1 -> new Entry(xC, yC, true, en);
                case 2-> new Exit(xC, yC, true, ex);
                case 3 -> new Trap(xC, yC, true, tr);
                case 6 -> new Substance(xC, yC, true, su);
                default-> null;
            };
            if (t != null){
                tiles1.add(t);
            }
        }
        return tiles1;
    }

    // Make a similar method to parsedMap to load the spawn points of enemies and collectibles
    // (also maybe player)
    // Objects o = switch (idCell)
    //{
//        case 4 -> new Enemy(xC, yC, true, region of enemy)
//        case 5 -> new Key(xC, yC, true, region of key)
        // should return a List<Objects> objectList like: Enemy, Key, Enemy, Enemy
    // Each objects in the list objectList will have an initial x and initial y
    //}
}
