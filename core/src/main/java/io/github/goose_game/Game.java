package io.github.goose_game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Game {

    private final float mapWidth = 24f;
    private final float mapHeight = 15f;
    private final Color nightTint = new Color(0.25f, 0.25f, 0.45f, 1f);

    private Texture backgroundTexture;

    private SpriteBatch spriteBatch;
    private FitViewport viewport;

    private Player player;
    private Timer timer;
    private Array<Goose> geese = new Array<>();
    private GooseSwarmEvent swarmEvent;
    private NightEvent nightEvent;

    public void create() {
        backgroundTexture = new Texture("test_background.png");

        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(8, 5);

        player = new Player(mapWidth, mapHeight);
        geese.add(new Goose("goose.png", 2f, 2f, 3));

        swarmEvent = new GooseSwarmEvent(18f, 10f, 2f, 2f); // placeholder spot until we have the map
        nightEvent = new NightEvent();
    }

    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        player.update(viewport, mapWidth, mapHeight);
        swarmEvent.update(player.getPosition(), geese, mapWidth, mapHeight);
        nightEvent.update(delta);
        for (Goose g : geese) {
            g.setRedEyes(nightEvent.isNight());
            g.update(delta, player.getPosition());
        }
        draw();
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();

        // geese are drawn without the tint so the red eyes stand out
        spriteBatch.setColor(nightEvent.isNight() ? nightTint : Color.WHITE);
        spriteBatch.draw(backgroundTexture, 0, 0, mapWidth, mapHeight);
        player.draw(spriteBatch);
        spriteBatch.setColor(Color.WHITE);
        for (Goose g : geese) g.draw(spriteBatch);

        spriteBatch.end();
    }

    public void dispose() {
        spriteBatch.dispose();
        backgroundTexture.dispose();
        player.dispose();
        for (Goose g : geese) g.dispose();
    }
}
