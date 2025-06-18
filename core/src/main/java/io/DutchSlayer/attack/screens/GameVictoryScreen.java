package io.DutchSlayer.attack.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.DutchSlayer.Main;
import io.DutchSlayer.screens.MainMenuScreen;
import io.DutchSlayer.screens.ModeSelectionScreen; // Import ModeSelectionScreen
import io.DutchSlayer.utils.Constant;

public class GameVictoryScreen implements Screen {

    private final Main game;
    private Stage stage;
    private Skin skin;
    private Music backgroundMusic;
    private Texture backgroundTexture;
    private TextButton.TextButtonStyle customButtonStyle;
    private Texture buttonUpTexture;
    private Texture buttonDownTexture;

    private static final int BUTTON_WIDTH = 380;
    private static final int BUTTON_HEIGHT = 100;
    private static final int OUTER_BORDER = 5;
    private static final int INNER_PADDING = 10;
    private static final int INNER_BORDER = 3;
    private static final float FONT_SCALE = 1.8f;

    private final int currentStage;
    private static final int MAX_STAGE = 3; // Define your maximum stage number here

    public GameVictoryScreen(Main game, int currentStage) {
        this.game = game;
        this.currentStage = currentStage;

        stage = new Stage(new FitViewport(Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT), game.batch);
        Gdx.input.setInputProcessor(stage);
        skin = new Skin(Gdx.files.internal("uiskin/uiskin.json"));

        backgroundTexture = new Texture(Gdx.files.internal("backgrounds/Main Menu.png"));

        initializeCustomButtonStyle();

        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("backgrounds/WinMusic.mp3"));
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(0.5f);

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label congrats = new Label("CONGRATULATIONS!", skin);
        congrats.setFontScale(2.5f);
        congrats.setColor(new Color(0.25f, 0.15f, 0.05f, 1.0f));

        // Main Menu Button
        TextButton mainMenuButton = new TextButton("MAIN MENU", customButtonStyle);
        mainMenuButton.getLabel().setFontScale(FONT_SCALE);
        mainMenuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game));
            }
        });

        TextButton nextActionButton; // Renamed for clarity

        if (currentStage < MAX_STAGE) { // If not the final stage
            nextActionButton = new TextButton("NEXT STAGE", customButtonStyle);
            nextActionButton.getLabel().setFontScale(FONT_SCALE);
            nextActionButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    game.setScreen(new GameScreen(game, currentStage + 1));
                }
            });
        } else { // If it is the final stage
            nextActionButton = new TextButton("MODE SELECTION", customButtonStyle); // Change text for final stage
            nextActionButton.getLabel().setFontScale(FONT_SCALE);
            nextActionButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    game.setScreen(new ModeSelectionScreen(game)); // Go to ModeSelectionScreen
                }
            });
        }

        table.add(congrats).padBottom(40).row();
        table.add(nextActionButton).size(BUTTON_WIDTH, BUTTON_HEIGHT).pad(10).row(); // Use the dynamic button
        table.add(mainMenuButton).size(BUTTON_WIDTH, BUTTON_HEIGHT).pad(10);
    }

    private void initializeCustomButtonStyle() {
        Color brownBorderOuter = new Color(0.3f, 0.15f, 0.05f, 1.0f);
        Color brownOuterFillUp = new Color(0.6f, 0.4f, 0.18f, 1.0f);
        Color brownOuterFillDown = new Color(0.48f, 0.32f, 0.12f, 1.0f);
        Color brownBorderInner = new Color(0.3f, 0.15f, 0.05f, 1.0f);
        Color brownInnerFillUp = new Color(0.57f, 0.38f, 0.16f, 1.0f);
        Color brownInnerFillDown = new Color(0.45f, 0.28f, 0.1f, 1.0f);
        Color textColor = new Color(0.25f, 0.15f, 0.05f, 1.0f);

        Pixmap pixmapUp = new Pixmap(BUTTON_WIDTH, BUTTON_HEIGHT, Pixmap.Format.RGBA8888);
        pixmapUp.setColor(brownBorderOuter);
        pixmapUp.fill();

        pixmapUp.setColor(brownOuterFillUp);
        pixmapUp.fillRectangle(
            OUTER_BORDER,
            OUTER_BORDER,
            BUTTON_WIDTH - (2 * OUTER_BORDER),
            BUTTON_HEIGHT - (2 * OUTER_BORDER)
        );

        int innerSquareX = OUTER_BORDER + INNER_PADDING;
        int innerSquareY = OUTER_BORDER + INNER_PADDING;
        int innerSquareWidth = BUTTON_WIDTH - (2 * OUTER_BORDER) - (2 * INNER_PADDING);
        int innerSquareHeight = BUTTON_HEIGHT - (2 * OUTER_BORDER) - (2 * INNER_PADDING);

        if (innerSquareWidth > 0 && innerSquareHeight > 0) {
            pixmapUp.setColor(brownBorderInner);
            pixmapUp.fillRectangle(
                innerSquareX,
                innerSquareY,
                innerSquareWidth,
                innerSquareHeight
            );

            pixmapUp.setColor(brownInnerFillUp);
            pixmapUp.fillRectangle(
                innerSquareX + INNER_BORDER,
                innerSquareY + INNER_BORDER,
                innerSquareWidth - (2 * INNER_BORDER),
                innerSquareHeight - (2 * INNER_BORDER)
            );
        }

        buttonUpTexture = new Texture(pixmapUp);
        pixmapUp.dispose();

        Pixmap pixmapDown = new Pixmap(BUTTON_WIDTH, BUTTON_HEIGHT, Pixmap.Format.RGBA8888);
        pixmapDown.setColor(brownBorderOuter);
        pixmapDown.fill();

        pixmapDown.setColor(brownOuterFillDown);
        pixmapDown.fillRectangle(
            OUTER_BORDER,
            OUTER_BORDER,
            BUTTON_WIDTH - (2 * OUTER_BORDER),
            BUTTON_HEIGHT - (2 * OUTER_BORDER)
        );

        if (innerSquareWidth > 0 && innerSquareHeight > 0) {
            pixmapDown.setColor(brownBorderInner);
            pixmapDown.fillRectangle(
                innerSquareX,
                innerSquareY,
                innerSquareWidth,
                innerSquareHeight
            );

            pixmapDown.setColor(brownInnerFillDown);
            pixmapDown.fillRectangle(
                innerSquareX + INNER_BORDER,
                innerSquareY + INNER_BORDER,
                innerSquareWidth - (2 * INNER_BORDER),
                innerSquareHeight - (2 * INNER_BORDER)
            );
        }

        buttonDownTexture = new Texture(pixmapDown);
        pixmapDown.dispose();

        TextureRegionDrawable upDrawable = new TextureRegionDrawable(buttonUpTexture);
        TextureRegionDrawable downDrawable = new TextureRegionDrawable(buttonDownTexture);

        BitmapFont defaultFont = skin.getFont("default-font");
        if (defaultFont == null) {
            Gdx.app.error("GameVictoryScreen", "Default font not found in uiskin.json. Please ensure 'default-font' is defined.");
            defaultFont = new BitmapFont();
        }
        defaultFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        customButtonStyle = new TextButton.TextButtonStyle();
        customButtonStyle.up = upDrawable;
        customButtonStyle.down = downDrawable;
        customButtonStyle.font = defaultFont;
        customButtonStyle.fontColor = textColor;
    }

    @Override
    public void show() {
        backgroundMusic.play();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();
        game.batch.draw(backgroundTexture, 0, 0, stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());
        game.batch.end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        backgroundMusic.stop();
    }

    @Override
    public void dispose() {
        stage.dispose();
        backgroundMusic.dispose();
        backgroundTexture.dispose();
        buttonUpTexture.dispose();
        buttonDownTexture.dispose();
        skin.dispose();
    }
}
