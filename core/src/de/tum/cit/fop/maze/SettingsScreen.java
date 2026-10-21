package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.EnumMap;
import java.util.Map;

/**
 * The SettingsScreen class is responsible for remapping controls and saving them to a config file.

 */
public class SettingsScreen implements Screen {

    /**
     * Input actions used for key bindings.
     * This is a minimal set and may be extended after discussion with the input system.
     */
    public enum Action {
        MOVE_UP,
        MOVE_DOWN,
        MOVE_LEFT,
        MOVE_RIGHT,
        RUN
    }

    public static final String CONFIG_PATH = "settings/keybindings.json";

    private final MazeRunnerGame game;
    private final Stage stage;

    //UI
    private final Label infoLabel;
    private Action waitingForAction = null;

    private final EnumMap<Action,Integer> bindings = new EnumMap<>(Action.class);
    private final EnumMap<Action,Label> valueLabels = new EnumMap<>(Action.class);

    /**
     * Constructor for MenuScreen. Sets up the camera, viewport, stage, and UI elements.
     *
     * @param game The main game class, used to access global resources and methods.
     */
    public SettingsScreen(MazeRunnerGame game) {
        this.game = game;
        var camera = new OrthographicCamera();
        camera.zoom = 1.5f; // Set camera zoom for a closer view

        Viewport viewport = new ScreenViewport(camera); // Create a viewport with the camera
        stage = new Stage(viewport, game.getSpriteBatch()); // Create a stage for UI elements

        Table root = new Table(); // Create a root table for layout
        root.setFillParent(true); // Make the table fill the stage
        root.pad(20); // add margin
        stage.addActor(root); // Add the root to the stage

        // Add a label as a title
        root.add(new Label("Settings", game.getSkin(), "title")).padBottom(30).row();

        // Create and add info label for user
        infoLabel = new Label("", game.getSkin());
        root.add(infoLabel).padBottom(20).row();

        //Table for keybindings
        Table bindsTable = new Table();
        root.add(bindsTable).row();

        //Create rows
        addBindRow(bindsTable, Action.MOVE_UP, "Move up");
        addBindRow(bindsTable, Action.MOVE_DOWN, "Move down");
        addBindRow(bindsTable, Action.MOVE_LEFT, "Move left");
        addBindRow(bindsTable, Action.MOVE_RIGHT, "Move right");
        addBindRow(bindsTable, Action.RUN, "Run");

        //Table for other buttons
        Table bottom = new Table();
        root.add(bottom).padTop(30).row();

        // Create and add buttons to the settings screen
        TextButton saveButton = new TextButton("Save", game.getSkin());
        bottom.add(saveButton).width(160).pad(10);
        saveButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                saveBindings();
                infoLabel.setText("Saved");
            }
        });

        TextButton resetButton = new TextButton("Reset", game.getSkin());
        bottom.add(resetButton).width(160).pad(10);
        resetButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                setDefaults();
                refreshLabels();
                infoLabel.setText("Reset to defaults");
            }
        });

        TextButton backButton = new TextButton("Back", game.getSkin());
        bottom.add(backButton).width(160).pad(10);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToMenu();
            }
        });
        refreshLabels();
    }

    //Add a row Action Key Current Key Change Button
    private void addBindRow(Table table, Action action, String displayName) {
        Label nameLabel = new Label(displayName, game.getSkin());
        Label keyLabel = new Label("",game.getSkin());
        valueLabels.put(action, keyLabel);

        TextButton changeButton = new TextButton("Change", game.getSkin());
        changeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                waitingForAction = action;
                infoLabel.setText("Press a key for: " + displayName);
            }
        });

        table.add(nameLabel).left().pad(8);
        table.add(keyLabel).width(180).pad(8);
        table.add(changeButton).width(140).pad(8);
        table.row();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        infoLabel.setText("");
        waitingForAction = null;
    }

    @Override
    public void render(float delta) {
        if (waitingForAction != null) {
            int pressed = getFirstJustPressedKey();
            if (pressed != -1) {
                removeKeyFromOtherActions(pressed, waitingForAction);

                bindings.put(waitingForAction, pressed);
                refreshLabels();

                infoLabel.setText("Set " + waitingForAction + " to " + Input.Keys.toString(pressed));
                waitingForAction = null;
            }
        }

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {

    }

    //Find the first key that was just pressed
    private int getFirstJustPressedKey() {
        for(int key = 0; key < Input.Keys.MAX_KEYCODE; key++) {
            if (Gdx.input.isKeyJustPressed(key)) return key;
        }
        return-1;
    }

    //Remove the pressed key from other actions to avoid duplicates
    public void removeKeyFromOtherActions(int key, Action keep) {
        for (Map.Entry<Action, Integer> e : bindings.entrySet()) {
            if (e.getKey() != keep && e.getValue() != null && e.getValue() == key) {
                bindings.put(e.getKey(), -1);
            }
        }
    }

    //Update the UI labels to match current bindings
    private void refreshLabels() {
        for (Action a : Action.values()) {
            Integer code = bindings.get(a);
            String text = (code == null || code == -1) ? "-" : Input.Keys.toString(code);
            valueLabels.get(a).setText(text);
        }
    }

    //Default bindings
    private void setDefaults() {
        bindings.put(Action.MOVE_UP, Input.Keys.UP);
        bindings.put(Action.MOVE_DOWN, Input.Keys.DOWN);
        bindings.put(Action.MOVE_LEFT, Input.Keys.LEFT);
        bindings.put(Action.MOVE_RIGHT, Input.Keys.RIGHT);
        bindings.put(Action.RUN, Input.Keys.SHIFT_LEFT);
    }

    //Loads bindings from local file, otherwise uses defaults
    private void loadOrDefault() {
        FileHandle file = Gdx.files.local(CONFIG_PATH);
        if(!file.exists()) {
            setDefaults();
            return;
        } try {
            Json json = new Json();
            @SuppressWarnings("unchecked")
            Map<String, Integer> loaded = json.fromJson(Map.class, file);
            setDefaults();

            for (Action a : Action.values()) {
                Integer code = loaded.get(a.name());
                if (code == null) bindings.put(a, code);
            }
        } catch (Exception e) {
            setDefaults();

        }
    }

    private void saveBindings() {
        Json json = new Json();
        EnumMap<Action, Integer> copy = new EnumMap<>(bindings);

        FileHandle dir = Gdx.files.local("settings");
        if (!dir.exists()) dir.mkdirs();

        java.util.HashMap<String, Integer> out = new java.util.HashMap<>();
        for (Action a : Action.values()) {
            out.put(a.name(), bindings.get(a));
        }

        Gdx.files.local(CONFIG_PATH).writeString(json.prettyPrint(out), false);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }


    // The following methods are part of the Screen interface but are not used in this screen.
    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }
}