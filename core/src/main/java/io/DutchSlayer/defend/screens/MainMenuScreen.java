package io.DutchSlayer.defend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.DutchSlayer.Main;
import io.DutchSlayer.utils.Constant;

public class MainMenuScreen implements Screen {

    private final Main game;
    private final Stage stage;
    private Table mainTable, modeTable, stageTable;
    private Skin skin;
    private Stack stack;
    private String selectedMode = null;

    public MainMenuScreen(Main game) {
        this.game = game;

        FitViewport viewport = new FitViewport(Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT);
        this.stage = new Stage(viewport);
        this.skin = new Skin(Gdx.files.internal("uiskin/uiskin.json"));
        this.stack = new Stack();
        stack.setFillParent(true);
        stage.addActor(stack);

// === TABEL UTAMA ===
        mainTable = new Table();
        mainTable.setFillParent(true);

        Label title = new Label("Dutch Slayer", skin, "default");
        TextButton startButton = new TextButton("Start Game", skin);
        TextButton aboutButton = new TextButton("About Us", skin);
        TextButton fullscreenButton = new TextButton("Toggle Fullscreen", skin);

// Button Listeners
        startButton.addListener(event -> {
            if (startButton.isPressed()) {
                showModePanel();
            }
            return false;
        });
        aboutButton.addListener(event -> {
            if (aboutButton.isPressed()) {
                game.setScreen(new AboutScreen(game));
            }
            return false;
        });
        fullscreenButton.addListener(event -> {
            if (fullscreenButton.isPressed()) {
                toggleFullscreen();
            }
            return false;
        });

        mainTable.add(title).padBottom(40).row();
        mainTable.add(startButton).size(Constant.BUTTON_WIDTH, Constant.BUTTON_HEIGHT).padBottom(20).row();
        mainTable.add(aboutButton).size(Constant.BUTTON_WIDTH, Constant.BUTTON_HEIGHT).padBottom(20).row();
        mainTable.add(fullscreenButton).size(Constant.BUTTON_WIDTH, Constant.BUTTON_HEIGHT);

// === TABEL MODE ===
        modeTable = new Table();
        modeTable.setFillParent(true);
        modeTable.setVisible(false); // default hidden

        Label modeLabel = new Label("Pilih Mode Game", skin);
        TextButton towerButton = new TextButton("Tower Defense", skin);
        TextButton platformerButton = new TextButton("Platformer", skin);
        TextButton cancelMode = new TextButton("Kembali", skin);

        towerButton.addListener(e -> {
            selectedMode = "tower";
            showStagePanel();
            return false;
        });
        platformerButton.addListener(e -> {
            selectedMode = "platformer";
            showStagePanel();
            return false;
        });
        cancelMode.addListener(e -> {
            modeTable.setVisible(false);
            mainTable.setVisible(true);
            return false;
        });

        modeTable.add(modeLabel).padBottom(30).row();
        modeTable.add(towerButton).size(Constant.BUTTON_WIDTH, Constant.BUTTON_HEIGHT).padBottom(20).row();
        modeTable.add(platformerButton).size(Constant.BUTTON_WIDTH, Constant.BUTTON_HEIGHT).padBottom(20).row();
        modeTable.add(cancelMode).size(Constant.BUTTON_WIDTH, Constant.BUTTON_HEIGHT);

// === TABEL STAGE ===
        stageTable = new Table();
        stageTable.setFillParent(true);
        stageTable.setVisible(false);

        Label stageLabel = new Label("Pilih Stage", skin);
        stageTable.add(stageLabel).padBottom(20).row();

        for (int i = 1; i <= 5; i++) {
            final int stageNumber = i;
            TextButton stageBtn = new TextButton("Stage " + i, skin);
            stageBtn.addListener(e -> {
                if (selectedMode != null) {
                    if (selectedMode.equals("tower")) {
                        game.setScreen(new TowerDefenseScreen(game, stageNumber));
                    } else {
                        game.setScreen(new GameScreen(game, stageNumber));
                    }
                }
                return false;
            });
            stageTable.add(stageBtn).size(Constant.BUTTON_WIDTH, Constant.BUTTON_HEIGHT).padBottom(15).row();
        }

        TextButton backToMode = new TextButton("Kembali", skin);
        backToMode.addListener(e -> {
            stageTable.setVisible(false);
            modeTable.setVisible(true);
            return false;
        });
        stageTable.add(backToMode).size(Constant.BUTTON_WIDTH, Constant.BUTTON_HEIGHT).padTop(20);

// Tambahkan semua ke stack
        stack.add(mainTable);
        stack.add(modeTable);
        stack.add(stageTable);

        Gdx.input.setInputProcessor(stage);

        Skin skin = new Skin(Gdx.files.internal("uiskin/uiskin.json"));
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);


        // === Listener Tombol ===
        TextButton finalStartButton = startButton;
        startButton.addListener(event -> {
            if (finalStartButton.isPressed()) {
                showModePanel();  // Mulai dari pemilihan mode
            }
            return false;
        });

        aboutButton.addListener(event -> {
            if (aboutButton.isPressed()) {
                game.setScreen(new AboutScreen(game));
            }
            return false;
        });

        fullscreenButton.addListener(event -> {
            if (fullscreenButton.isPressed()) {
                toggleFullscreen();
            }
            return false;
        });

        // === Layout ===
        table.add(title).padBottom(40).row();
        table.add(startButton).size(Constant.BUTTON_WIDTH, Constant.BUTTON_HEIGHT).padBottom(20).row();
        table.add(aboutButton).size(Constant.BUTTON_WIDTH, Constant.BUTTON_HEIGHT).padBottom(20).row();
        table.add(fullscreenButton).size(Constant.BUTTON_WIDTH, Constant.BUTTON_HEIGHT);
    }


    // === Dialog Pilih Mode ===
    private void showModeSelectionDialog(Skin skin) {
        Dialog dialog = new Dialog("Pilih Mode Game", skin) {
            @Override
            protected void result(Object object) {
                if (object != null && (object.equals("tower") || object.equals("platformer"))) {
                    showStageSelectionDialog((String) object, skin);
                }
            }
        };

        dialog.text("Pilih mode yang ingin kamu mainkan:");
        dialog.button("Tower Defense", "tower");
        dialog.button("Platformer", "platformer");
        dialog.button("Batal", "cancel");
        dialog.show(stage);
    }

    // === Dialog Pilih Stage ===
    private void showStageSelectionDialog(String mode, Skin skin) {
        Dialog dialog = new Dialog("Pilih Stage", skin) {
            @Override
            protected void result(Object object) {
                if (object != null && !object.equals("cancel")) {
                    try {
                        int selectedStage = Integer.parseInt((String) object);

                        if (mode.equals("tower")) {
                            game.setScreen(new TowerDefenseScreen(game, selectedStage));
                        } else if (mode.equals("platformer")) {
                            game.setScreen(new GameScreen(game, selectedStage));
                        }
                    } catch (NumberFormatException e) {
                        Gdx.app.log("MainMenuScreen", "Stage pilihan tidak valid.");
                    }
                }
            }
        };

        dialog.text("Pilih stage yang ingin dimainkan:");
        for (int i = 1; i <= 5; i++) {
            dialog.button(String.valueOf(i), String.valueOf(i));
        }

        dialog.button("Batal", "cancel");
        dialog.show(stage);
    }

    private void toggleFullscreen() {
        if (Gdx.graphics.isFullscreen()) {
            Gdx.graphics.setWindowedMode(Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT);
        } else {
            Graphics.DisplayMode currentMode = Gdx.graphics.getDisplayMode();
            Gdx.graphics.setFullscreenMode(currentMode);
        }

    }
    private void showModePanel() {
        mainTable.setVisible(false);
        modeTable.setVisible(true);
        stageTable.setVisible(false);
    }

    private void showStagePanel() {
        modeTable.setVisible(false);
        stageTable.setVisible(true);
    }


    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
    }
}
