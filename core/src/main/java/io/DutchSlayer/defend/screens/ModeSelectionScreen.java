package io.DutchSlayer.defend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;

import io.DutchSlayer.Main;
import io.DutchSlayer.defend.untils.Constant;
import io.DutchSlayer.defend.untils.GameMode;

public class ModeSelectionScreen implements Screen {

    private final Main game;
    private final Stage stage;
    private final FitViewport viewport;
    private final Skin skin;

    private final Texture background;
    private final Texture titleTexture;
    private final Texture defenseTexture;
    private final Texture platformerTexture;
    private final Texture backButtonTexture;

    public ModeSelectionScreen(Main game) {
        this.game = game;
        this.viewport = new FitViewport(Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT);
        this.stage = new Stage(viewport);
        this.skin = new Skin(Gdx.files.internal("uiskin/uiskin.json"));

        this.background = new Texture(Gdx.files.internal("backgrounds/Main Menu.png"));
        this.titleTexture = new Texture(Gdx.files.internal("button/ModeSelection.png"));
        this.defenseTexture = new Texture(Gdx.files.internal("button/DefenseButton.png"));
        this.platformerTexture = new Texture(Gdx.files.internal("button/PlatformerButton.png"));
        this.backButtonTexture = new Texture(Gdx.files.internal("button/backbutton.png"));

        Gdx.input.setInputProcessor(stage);
        createUI();
    }

    private void createUI() {
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.top().left();
        stage.addActor(rootTable);

        // Back Button (pojok kiri atas)
        Table topBar = new Table();
        ImageButton backButton = new ImageButton(new TextureRegionDrawable(backButtonTexture));
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game));
            }
        });
        topBar.add(backButton).size(100, 100).pad(10).left();
        rootTable.add(topBar).expandX().left().row();

        // Title
        Image titleImg = new Image(titleTexture);
        rootTable.add(titleImg).width(800).height(450).padTop(-180).center().row();

        // Button Table
        Table buttonTable = new Table();
        buttonTable.defaults().pad(5);

        ImageButton defenseBtn = new ImageButton(new TextureRegionDrawable(defenseTexture));
        defenseBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Main.currentMode = GameMode.TOWER_DEFENSE;
                game.setScreen(new StageSelectionScreen(game, true));
            }
        });
        buttonTable.add(defenseBtn).size(500, 120).padTop(-150).row();

        ImageButton platformerBtn = new ImageButton(new TextureRegionDrawable(platformerTexture));
        platformerBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Main.currentMode = GameMode.PLATFORMER;
                game.setScreen(new StageSelectionScreen(game, false));
            }
        });
        buttonTable.add(platformerBtn).size(500, 120).padBottom(170).row();

        rootTable.add(buttonTable).expand().center().padBottom(5);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.setProjectionMatrix(viewport.getCamera().combined);
        game.batch.begin();
        game.batch.draw(background, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        game.batch.end();

        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        background.dispose();
        titleTexture.dispose();
        defenseTexture.dispose();
        platformerTexture.dispose();
        backButtonTexture.dispose();
    }
}
