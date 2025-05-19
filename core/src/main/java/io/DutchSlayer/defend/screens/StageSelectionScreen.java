package io.DutchSlayer.defend.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector3;

import io.DutchSlayer.Main;

public class StageSelectionScreen implements Screen {
    private final Main game;
    private final OrthographicCamera camera;
    private final BitmapFont font;
    private final GlyphLayout layout;

    private final boolean isDefendMode;

    public StageSelectionScreen(Main game, boolean isDefendMode) {
        this.game = game;
        this.isDefendMode = isDefendMode;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1280, 720);
        font = new BitmapFont();
        layout = new GlyphLayout();

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                Vector3 v = new Vector3(screenX, screenY, 0);
                camera.unproject(v);
                float x = v.x, y = v.y;

                // Contoh sederhana: misal klik di kiri layar = stage 1
                if (x < 640) {
                    if (isDefendMode) {
                        game.setScreen(new TowerDefenseScreen(game, 1)); // stage 1 defend
                    } else {
                        game.setScreen(new GameScreen(game, 1)); // stage 1 platformer
                    }
                } else {
                    if (isDefendMode) {
                        game.setScreen(new TowerDefenseScreen(game, 2)); // stage 2 defend
                    } else {
                        game.setScreen(new GameScreen(game, 2)); // stage 2 platformer
                    }
                }
                return true;
            }
        });
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        font.draw(game.batch, "Select Stage", 600, 650);
        font.draw(game.batch, "Stage 1", 300, 400);
        font.draw(game.batch, "Stage 2", 900, 400);
        game.batch.end();
    }

    @Override public void resize(int width, int height) {}
    @Override public void show() {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void dispose() {
        font.dispose();
    }
}
