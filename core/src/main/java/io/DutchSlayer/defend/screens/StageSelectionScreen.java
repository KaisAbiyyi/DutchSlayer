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
import io.DutchSlayer.defend.screens.TowerDefenseScreen;
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

    // Offset default untuk crown di atas tombol level 4
    private float crownOffsetX = 0f;
    private float crownOffsetY = 10f;

    public StageSelectionScreen(Main game, boolean isDefendMode) {
        this.game = game;
        this.isDefendMode = isDefendMode;

        camera = new OrthographicCamera();
        viewport = new FitViewport(Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT, camera);
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);

        // Skin untuk slider/button (meskipun di layar ini tidak terpakai)
        skin = new Skin(Gdx.files.internal("uiskin/uiskin.json"));

        // Muat semua texture yang diperlukan
        background        = new Texture(Gdx.files.internal("backgrounds/Main Menu.png"));
        crownTexture      = new Texture(Gdx.files.internal("button/crown.png"));
        backButtonTexture = new Texture(Gdx.files.internal("button/backbutton.png"));
        stageBannerTexture= new Texture(Gdx.files.internal("button/StageBanner.png"));

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

        // 1) Back button di pojok kiri atas
        ImageButton backButton = new ImageButton(new TextureRegionDrawable(backButtonTexture));
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new ModeSelectionScreen(game));
            }
        });
        rootTable.add(backButton)
            .size(100, 100)
            .pad(10)
            .left();
        rootTable.row();

        // 2) Banner judul (StageBanner.png)
        Image stageBanner = new Image(stageBannerTexture);
        // Ukuran dan padding banner bisa Anda ubah sesuai kebutuhan
        rootTable.add(stageBanner)
            .size(800, 400)      // ganti jika ingin ukuran lain
            .padTop(-155)          // naikkan atau turunkan banner relatif tombol Back
            .center()
            .row();

        // 3) Table horizontal untuk tombol level (1–4)
        Table levelTable = new Table();
        levelTable.defaults().pad(10).padTop(-120); // spacing antara tombol

        for (int i = 0; i < 4; i++) {
            final int levelNum = i + 1;

            // Buat ImageButton untuk tiap level
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

            // Bungkus tombol dalam container supaya kita bisa men‐empatkan crown dengan mudah
            Table btnContainer = new Table();
            btnContainer.add(levelBtn).size(120, 120);

            // Jika ini adalah tombol level ke‐4, tambahkan crown di atasnya
            if (levelNum == 4) {
                Image crown = new Image(crownTexture);
                crown.setSize(200, 200);
                // Posisi crown relatif ke pojok kiri‐bawah dari tombol
                // crownOffsetX dan crownOffsetY bisa Anda atur untuk menaikkan/menggeser crown
                crown.setPosition(
                    (100 - 85) / 2f + crownOffsetX,
                    9 + crownOffsetY
                );
                crown.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
                btnContainer.addActor(crown);
            }

            levelTable.add(btnContainer);
        }

        rootTable.add(levelTable)
            .expandX()
            .center()
            .padTop(40);
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
        game.batch.draw(background,
            0, 0,
            viewport.getWorldWidth(),
            viewport.getWorldHeight());
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
        if (background        != null) background.dispose();
        if (crownTexture      != null) crownTexture.dispose();
        if (backButtonTexture != null) backButtonTexture.dispose();
        if (stageBannerTexture!= null) stageBannerTexture.dispose();
        for (Texture tex : levelTextures) {
            if (tex != null) tex.dispose();
        }
    }
}
