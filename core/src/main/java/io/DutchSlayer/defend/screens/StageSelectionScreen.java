package io.DutchSlayer.defend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.DutchSlayer.Main;
import io.DutchSlayer.defend.untils.AudioManager;
import io.DutchSlayer.defend.untils.Constant;

public class StageSelectionScreen implements Screen {
    private final Main game;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final Stage stage;
    private final Skin skin;
    private Texture background;
    private Texture crownTexture;
    private Texture backButtonTexture;
    private Texture stageBannerTexture;
    private Texture[] levelTextures;

    private final boolean isDefendMode;

    public StageSelectionScreen(Main game, boolean isDefendMode) {
        this.game = game;
        this.isDefendMode = isDefendMode;

        camera   = new OrthographicCamera();
        viewport = new FitViewport(Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT, camera);
        stage    = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("uiskin/uiskin.json"));

        background         = new Texture(Gdx.files.internal("backgrounds/Main Menu.png"));
        crownTexture       = new Texture(Gdx.files.internal("button/crown.png"));
        backButtonTexture  = new Texture(Gdx.files.internal("button/backbutton.png"));
        stageBannerTexture = new Texture(Gdx.files.internal("button/StageBanner.png"));

        levelTextures = new Texture[4];
        for (int i = 0; i < 4; i++) {
            levelTextures[i] = new Texture(Gdx.files.internal("button/Level" + (i + 1) + ".png"));
        }

        createUI();
    }

    private void createUI() {
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.top().left();
        stage.addActor(rootTable);

        // 1) Tombol Back
        ImageButton backButton = new ImageButton(new TextureRegionDrawable(backButtonTexture));
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new ModeSelectionScreen(game));
            }
        });
        rootTable.add(backButton).size(100, 100).pad(10).left();
        rootTable.row();

        // 2) Banner judul Stage
        Image stageBanner = new Image(stageBannerTexture);
        rootTable.add(stageBanner)
            .size(800, 400)
            .padTop(-155)
            .center()
            .row();

        // 3) Tombol level (1–4), dengan crown di level 4
        Table levelTable = new Table();
        levelTable.defaults().pad(10).padTop(-120);

        for (int i = 0; i < 4; i++) {
            final int levelNum = i + 1;
            ImageButton levelBtn = new ImageButton(new TextureRegionDrawable(levelTextures[i]));
            levelBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (isDefendMode) {
                        game.setScreen(new TowerDefenseScreen(game, levelNum));
                    } else {
                        game.setScreen(new GameScreen(game, levelNum));
                    }
                }
            });

            Table btnContainer = new Table();
            btnContainer.add(levelBtn).size(120, 120);

            if (levelNum == 4) {
                Image crown = new Image(crownTexture);
                crown.setSize(200, 200);
                crown.setPosition((100 - 85) / 2f, 9 + 10f);
                btnContainer.addActor(crown);
            }

            levelTable.add(btnContainer);
        }

        rootTable.add(levelTable).expandX().center().padTop(40);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        game.batch.draw(background, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        game.batch.end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }
    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        background.dispose();
        crownTexture.dispose();
        backButtonTexture.dispose();
        stageBannerTexture.dispose();
        for (Texture tex : levelTextures) {
            tex.dispose();
        }
    }
}
