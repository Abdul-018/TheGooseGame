package io.github.goose_game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
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
    private GooseSprites gooseSprites;
    private Array<Goose> geese = new Array<>();
    private GooseSwarmEvent swarmEvent;
    private NightEvent nightEvent;

    public void create() {
        backgroundTexture = new Texture("test_background.png");

        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(8, 5);

        player = new Player(mapWidth, mapHeight);
        gooseSprites = new GooseSprites();
        for (int i = 0; i < 3; i++) {
            float x = (float)(Math.random() * mapWidth);
            float y = (float)(Math.random() * mapHeight);
            geese.add(new Goose(gooseSprites, x, y, 3));
        }

        // placeholder zone on the test background, needs a real spot once the map is ready
        swarmEvent = new GooseSwarmEvent(18f, 10f, 2f, 2f, gooseSprites);
        nightEvent = new NightEvent();
    }

    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        player.update(viewport, mapWidth, mapHeight, this);

        Vector2 playerPosition = player.getPosition();
        swarmEvent.update(playerPosition, geese, mapWidth, mapHeight);
        nightEvent.update(delta);
        for (Goose g : geese) {
            g.setNight(nightEvent.isNight());
            g.update(delta, playerPosition, geese);
        }
        draw();
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();

        // geese skip the night tint so the red eyes stand out
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
        gooseSprites.dispose();
    }

    public Array<Goose> getGeese(){
        return this.geese;
    }

}
