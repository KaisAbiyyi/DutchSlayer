package io.DutchSlayer.defend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.DutchSlayer.Main;
import io.DutchSlayer.defend.untils.Constant;

class SettingsScreen implements Screen {

    private final Main game;
    private final Stage stage;
    private final FitViewport viewport;
    private final Skin skin;

    private final Texture background;
    private final Texture titleTexture;
    private final Texture toggleTexture;
    private final Texture backButtonTexture;
    private final Texture volumeTexture; // Tambahan

    public SettingsScreen(Main game) {
        this.game = game;
        this.viewport = new FitViewport(Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT);
        this.stage = new Stage(viewport);
        this.skin = new Skin(Gdx.files.internal("uiskin/uiskin.json"));

        this.background = new Texture(Gdx.files.internal("backgrounds/Main Menu.png"));
        this.titleTexture = new Texture(Gdx.files.internal("button/SettingScreen.png"));
        this.toggleTexture = new Texture(Gdx.files.internal("button/ToogleButton.png"));
        this.backButtonTexture = new Texture(Gdx.files.internal("button/backbutton.png"));
        this.volumeTexture = new Texture(Gdx.files.internal("button/volume.png")); // Load texture
        createUI();
    }

    private void createUI() {
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.top();
        stage.addActor(rootTable);

        // Baris untuk tombol back
        Table topBar = new Table();
        ImageButton backButton = new ImageButton(new TextureRegionDrawable(backButtonTexture));
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                game.setScreen(new MainMenuScreen(game));
            }
        });
        topBar.add(backButton).size(100, 100).pad(10).left();
        rootTable.add(topBar).expandX().left().row();

        // Title
        Image titleImg = new Image(titleTexture);
        rootTable.add(titleImg).size(800, 450).padTop(-180).center().row();

        // Content Table (Volume dulu, lalu Toggle)
        Table contentTable = new Table();
        contentTable.defaults().padBottom(10);

        // 1) Volume image (pengganti label)
        Image volumeImage = new Image(volumeTexture);
        contentTable.add(volumeImage)
            .size(150, 100)
            .padTop(-600)
            .row();

        // 2) Slider di bawah gambar Volume
        Slider volumeSlider = new Slider(0f, 1f, 0.01f, false, skin);
        volumeSlider.setValue(game.bgMusic.getVolume());
        volumeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.bgMusic.setVolume(volumeSlider.getValue());
            }
        });
        contentTable.add(volumeSlider)
            .width(350)
            .padTop(-550)
            .row();

        // 3) Baru Toggle button di bawah Slider
        ImageButton toggleBtn = new ImageButton(new TextureRegionDrawable(toggleTexture));
        toggleBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                if (Gdx.graphics.isFullscreen()) {
                    Gdx.graphics.setWindowedMode(Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT);
                } else {
                    Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
                }
            }
        });
        contentTable.add(toggleBtn)
            .size(300, 100)
            .padTop(-350) // sesuaikan agar jaraknya pas
            .padBottom(20)
            .row();

        // Tambahkan contentTable ke rootTable
        rootTable.add(contentTable).expand().center().padTop(80);
    }


    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
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

    @Override public void resize(int w, int h) { viewport.update(w, h, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        background.dispose();
        titleTexture.dispose();
        toggleTexture.dispose();
        backButtonTexture.dispose();
        volumeTexture.dispose(); // Tambahan
    }
}
