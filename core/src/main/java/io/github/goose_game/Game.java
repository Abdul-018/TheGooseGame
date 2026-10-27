package io.github.goose_game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Game {

    private final float mapWidth = 24f;
    private final float mapHeight = 15f;

    private Texture backgroundTexture;

    private SpriteBatch spriteBatch;
    private FitViewport viewport;

    private Player player;
    private Timer Timer;

    public void create() {
        backgroundTexture = new Texture("test_background.png");

        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(8, 5);

        player = new Player(mapWidth, mapHeight);
    }

    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    public void render() {
        player.update(viewport, mapWidth, mapHeight);
        draw();
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();

        spriteBatch.draw(backgroundTexture, 0, 0, mapWidth, mapHeight);
        player.draw(spriteBatch);

        spriteBatch.end();
    }

    public void dispose() {
        spriteBatch.dispose();
        backgroundTexture.dispose();
        player.dispose();
    }
}
