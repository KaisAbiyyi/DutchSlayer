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
import io.DutchSlayer.defend.utils.TDConstants;

public class AboutScreen implements Screen {

    private final Main game;
    private final Stage stage;
    private final FitViewport viewport;
    private final Skin skin;

    private final Texture background;
    private final Texture titleTexture;
    private final Texture backButtonTexture;
    private final Texture kaisTexture;
    private final Texture alvinTexture;
    private final Texture haerulTexture;

    public AboutScreen(Main game) {
        this.game = game;
        this.viewport = new FitViewport(TDConstants.SCREEN_WIDTH, TDConstants.SCREEN_HEIGHT);
        this.stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);

        this.skin = new Skin(Gdx.files.internal("uiskin/uiskin.json"));

        // Muat semua texture
        this.background       = new Texture(Gdx.files.internal("backgrounds/Main Menu.png"));
        this.titleTexture     = new Texture(Gdx.files.internal("button/AboutUs.png"));
        this.backButtonTexture= new Texture(Gdx.files.internal("button/backbutton.png"));
        this.kaisTexture      = new Texture(Gdx.files.internal("button/Kais.png"));
        this.alvinTexture     = new Texture(Gdx.files.internal("button/Alvin.png"));
        this.haerulTexture    = new Texture(Gdx.files.internal("button/Haerul.png"));

        createUI();
    }

    private void createUI() {
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.top().left();
        stage.addActor(rootTable);

        // 1) Baris untuk tombol Back di pojok kiri atas
        ImageButton backButton = new ImageButton(new TextureRegionDrawable(backButtonTexture));
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game));
            }
        });
        rootTable.add(backButton)
            .size(100, 100)
            .pad(10)
            .left();
        rootTable.row();

        // 2) Judul About Us sebagai Image, dipusatkan
        Image titleImg = new Image(titleTexture);
        rootTable.add(titleImg)
            .size(800, 400)      // Ubah size sesuai kebutuhan
            .padTop(-160)          // Sesuaikan jarak dari tombol Back
            .center()
            .row();

        // 3) Table untuk nama anggota (gambar) satu kolom, dipusatkan
        Table memberTable = new Table();
        memberTable.defaults().padTop(-150);

        Image kaisImg = new Image(kaisTexture);
        memberTable.add(kaisImg)
            .size(400, 200)    // Ubah ukuran sesuai kebutuhan
            .row();

        Image alvinImg = new Image(alvinTexture);
        memberTable.add(alvinImg)
            .size(400, 200)
            .row();

        Image haerulImg = new Image(haerulTexture);
        memberTable.add(haerulImg)
            .size(400, 200)
            .row();

        rootTable.add(memberTable)
            .expand()
            .center()
            .padTop(-250);
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
        game.batch.draw(
            background,
            0, 0,
            viewport.getWorldWidth(),
            viewport.getWorldHeight()
        );
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
        titleTexture.dispose();
        backButtonTexture.dispose();
        kaisTexture.dispose();
        alvinTexture.dispose();
        haerulTexture.dispose();
    }
}
