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

public class MainMenuScreen implements Screen {

    private final Main game;
    private final Stage stage;
    private final FitViewport viewport; // Ubah jadi FitViewport
    private final Skin skin;
    private final Texture background;

    private final Texture titleTexture;
    private final Texture startTexture;
    private final Texture aboutTexture;
    private final Texture settingsTexture;
    private final Texture exitTexture; // Tambahan


    public MainMenuScreen(Main game) {
        this.game      = game;
        this.viewport  = new FitViewport(Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT);
        this.stage     = new Stage(viewport);
        this.skin      = new Skin(Gdx.files.internal("uiskin/uiskin.json"));
        this.background= new Texture(Gdx.files.internal("backgrounds/Main Menu.png"));

        // Load textures
        this.titleTexture    = new Texture(Gdx.files.internal("button/DutchSlayer.png"));
        this.startTexture    = new Texture(Gdx.files.internal("button/StartButton.png"));
        this.settingsTexture = new Texture(Gdx.files.internal("button/SettingsButton.png"));
        this.aboutTexture    = new Texture(Gdx.files.internal("button/AboutButton.png"));
        this.exitTexture     = new Texture(Gdx.files.internal("button/Exit.png"));


        Gdx.input.setInputProcessor(stage);
        createUI();
    }

    private void createUI() {
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.top().left();
        stage.addActor(rootTable);

        // Title
        Image titleImg = new Image(titleTexture);
        rootTable.add(titleImg).width(800).height(450).padTop(-140).padBottom(-140).center().row();

        // Button Table
        Table buttonTable = new Table();
        buttonTable.defaults().padBottom(20);

        ImageButton startBtn = new ImageButton(new TextureRegionDrawable(startTexture));
        startBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                game.setScreen(new ModeSelectionScreen(game));
            }
        });
        buttonTable.add(startBtn).size(500, 120).row();

        ImageButton settingsBtn = new ImageButton(new TextureRegionDrawable(settingsTexture));
        settingsBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                game.setScreen(new SettingsScreen(game));
            }
        });
        buttonTable.add(settingsBtn).size(500, 120).row();

        ImageButton aboutBtn = new ImageButton(new TextureRegionDrawable(aboutTexture));
        aboutBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                game.setScreen(new AboutScreen(game));
            }
        });
        buttonTable.add(aboutBtn).size(500, 120).row();

        // Tambahan: Exit Button
        ImageButton exitBtn = new ImageButton(new TextureRegionDrawable(exitTexture));
        exitBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                Gdx.app.exit(); // Keluar dari game
            }
        });
        buttonTable.add(exitBtn).size(500, 120);

        rootTable.add(buttonTable).expand().center().padBottom(10);
    }


    @Override public void show() {
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
        startTexture.dispose();
        settingsTexture.dispose();
        aboutTexture.dispose();
        exitTexture.dispose(); // Tambahan

    }
}
